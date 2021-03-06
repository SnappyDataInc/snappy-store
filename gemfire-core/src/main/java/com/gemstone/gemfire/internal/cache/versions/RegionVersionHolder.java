/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.internal.cache.versions;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.Assert;
import com.gemstone.gemfire.internal.InternalDataSerializer;
import com.gemstone.gemfire.internal.cache.versions.RVVException.ReceivedVersionsIterator;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;

/**
 * RegionVersionHolders are part of a RegionVersionVector.  A RVH holds the
 * current version for a member and a list of exceptions, which are
 * holes in the list of versions received from that member.
 *
 * RegionVersionHolders should be modified under synchronization on the holder.
 * 
 * Starting in 7.0.1 the holder has a BitSet that records the most recent
 * versions.  The variable bitSetVersion corresponds to bit zero, and
 * subsequent bits represent bitSetVersion+1, +2, etc.  The method
 * mergeBitSet() should be used to dump the BitSet's exceptions into
 * the regular exceptions list prior to performing operations like exceptions-
 * comparisons or dominance checks.
 * 
 * Starting in 7.5, the holder introduced a special exception to describe following use case of unfinished operation:
 * Operation R4 and R5 are applied locally, but never distributed to P. So P's RVV for R is still 3.
 * After R GIIed from P, R's RVV becomes R5(3-6), i.e. Exception's nextVersion is currentVersion+1. 
 * 
 * @author Bruce Schuchardt
 */
public final class RegionVersionHolder<T> implements Cloneable, DataSerializable {
  
  private static List<RVVException> EMPTY_EXCEPTIONS = Collections.emptyList();
    
  long version = -1; // received version
  transient T id;
  private List<RVVException> exceptions;
  boolean isDepartedMember;

  //non final for tests
  public static int BIT_SET_WIDTH = 64 * 16;  // should be a multiple of 4 64-bit longs

  private long bitSetVersion = 1;
  private BitSet bitSet;
  
  /**
   * This contructor should only be used for cloning a RegionVersionHolder
   * or initializing and invalid version holder (with version -1)
   * @param ver
   */
  public RegionVersionHolder(long ver) {
    this.version = ver;
  }
  
  public RegionVersionHolder(T id) {
    this.id = id;
    this.version = 0;
    this.bitSetVersion = 1;
    this.bitSet = new BitSet(RegionVersionHolder.BIT_SET_WIDTH);
  }
  
  public RegionVersionHolder(DataInput in) throws IOException {
    fromData(in);
  }
  
  public synchronized long getVersion() {
    RVVException e = null;
    List<RVVException> exs = getExceptions();
    if (!exs.isEmpty()) {
      e = exs.get(0);
    }
    if (isSpecialException(e, this)) {
      return e.getHighestReceivedVersion();
    } else {
      return this.version;
    }
  }

  private synchronized RVVException getSpecialException() {
    RVVException e = null;
    if (this.exceptions != null && !this.exceptions.isEmpty()) {
      e = this.exceptions.get(0);
    }
    if (isSpecialException(e, this)) {
      return e;
    } else {
      return null;
    }
  }

  public long getBitSetVersionForTesting() {
    return this.bitSetVersion;
  }
  
  private synchronized List<RVVException> getExceptions() {
    mergeBitSet();
    if (this.exceptions != null) {
      return this.exceptions;
    } else {
      return EMPTY_EXCEPTIONS;
    }
  }
  
  public synchronized List<RVVException> getExceptionForTest() {
    return getExceptions();
  }
  
  public synchronized int getExceptionCount() {
    return getExceptions().size();
  }
  
  public synchronized String exceptionsToString() {
    return getExceptions().toString();
  }
  
  
  /* test only method */
  public void setVersion(long ver) {
    this.version = ver;
  }
  
  @Override
  public synchronized RegionVersionHolder<T> clone() {
    RegionVersionHolder<T> clone = new RegionVersionHolder<T>(this.version);
    clone.id = this.id;
    clone.isDepartedMember = this.isDepartedMember;
    boolean hasSpecialEx = true;
    if (this.exceptions != null) {
      clone.exceptions = new LinkedList<RVVException>();
      for (RVVException e: this.exceptions) {
        if(isSpecialException(e,this)){
          hasSpecialEx = true;
        }
        clone.exceptions.add(e.clone());
      }
    }
    if(hasSpecialEx) {
      if (this.bitSet != null) {
        clone.bitSet = (BitSet)this.bitSet.clone();
        clone.bitSetVersion = this.bitSetVersion;
        clone.mergeBitSet();
      }
    }/*else {
      if (this.bitSet != null) {
        clone.bitSet = (BitSet)this.bitSet.clone();
        clone.bitSetVersion = this.bitSetVersion;
        clone.mergeBitSetWithoutException();
      }
    }*/
    // remove other exceptions if there are special exceptions
    // if there are speacial exception..that means

    return clone;
  }
  
  @Override
  public synchronized String toString() {
//    mergeBitSet();
    StringBuilder sb = new StringBuilder();
    sb.append("{rv").append(this.version)
      .append(" bsv").append(this.bitSetVersion)
      .append(" bs=[");
    if (this.bitSet != null) {
      int i=this.bitSet.nextSetBit(0);
      if (i>=0) {
        sb.append("0");
        for (i=this.bitSet.nextSetBit(1); i > 0; i=this.bitSet.nextSetBit(i+1)) {
          sb.append(',').append(i);
        }
      }
    }
    sb.append(']');
    if (this.exceptions != null && !this.exceptions.isEmpty()) {
      sb.append(this.exceptions.toString()); 
    }
    return sb.toString();
  }
  
  /** add a version that is older than this.bitSetVersion */
  private void addOlderVersion(long missingVersion, LogWriterI18n logger) {
    // exceptions iterate in reverse order on their previousVersion variable
    if (this.exceptions == null) {
      return;
    }
    int i = 0;
    for (Iterator<RVVException> it = this.exceptions.iterator(); it.hasNext(); ) {
      RVVException e = it.next();
      if (e.nextVersion <= missingVersion) {
        if(isSpecialException(e,this))
          continue;
        else
          return;  // there is no RVVException for this version
      }
      if (e.previousVersion < missingVersion  &&  missingVersion < e.nextVersion) {
        String fine = null;
        boolean spEx = isSpecialException(e,this);
        if (RegionVersionVector.DEBUG && logger != null) {
          fine = e.toString();
        }
        e.add(missingVersion);
        if (e.isFilled()) {
          if (fine != null) {
            logger.info(LocalizedStrings.DEBUG, "Filled exception " + fine);
          }
          it.remove();
        } else if(e.shouldChangeForm()) {
          this.exceptions.set(i, e.changeForm());
        }
        if (this.exceptions.isEmpty()) {
          this.exceptions = null;
        }
       if(spEx)
          continue;
        else
          return;
      }
      i++;
    }
  }
  
  void flushBitSetDuringRecording(long version, LogWriterI18n logger) {
    int length = BIT_SET_WIDTH;
    int bitCountToFlush = length * 3 / 4;
    if (RegionVersionVector.DEBUG && logger != null) {
      logger.info(LocalizedStrings.DEBUG, "flushing RVV bitset bitSetVersion="+this.bitSetVersion
          + "; bits=" + this.bitSet.toString());
    }
    // see if we can shift part of the bits so that exceptions in the recent bits can
    // be kept in the bitset and later filled without having to create real exception objects
    if (version >= this.bitSetVersion + length + bitCountToFlush) {
      // nope - flush the whole bitset
      addBitSetExceptions(length, version, logger);
    } else {
      // yes - flush the lower part.  We can only flush up to the last set bit because
      // the exceptions list includes a "next version" that indicates a received version.
      addBitSetExceptions(bitCountToFlush, this.bitSetVersion+bitCountToFlush, logger);
    }
    if (RegionVersionVector.DEBUG && logger != null) {
      logger.info(LocalizedStrings.DEBUG, "after flushing bitSetVersion="+this.bitSetVersion
          + "; bits=" + this.bitSet.toString());
    }
  }


  private synchronized void mergeBitSetWithoutException() {
    if (this.bitSet != null && this.bitSetVersion < this.version) {
      addBitSet((int)(this.version-this.bitSetVersion), this.version, null);
    }
  }

  /** merge bit-set exceptions into the regular exceptions list */
  private synchronized void mergeBitSet() {
    if (this.bitSet != null && this.bitSetVersion < this.version) {
      addBitSetExceptions((int)(this.version-this.bitSetVersion), this.version, null);
    }
  }

  /**
   * Add exceptions from the BitSet array to the exceptions list.  Assumes that
   * the BitSet[0] corresponds to this.bitSetVersion.  This scans the bitset
   * looking for gaps that are recorded as RVV exceptions.  The scan terminates
   * at numBits or when the last set bit is found.  The bitSet is adjusted and
   * a new bitSetVersion is established.
   *
   * @param newVersion  the desired new bitSetVersion, which may be > the max representable in the bitset
   * @param numBits the desired number of bits to flush from the bitset
   * @param logger
   */
  private void addBitSetExceptions(int numBits, long newVersion, LogWriterI18n logger) {
    int lastSetIndex = -1;

    if (RegionVersionVector.DEBUG && logger != null) {
      logger.info(LocalizedStrings.DEBUG, "addBitSetExceptions("+numBits+","+newVersion+")");
    }

    for (int idx = 0; idx < numBits; ) {
      int nextMissingIndex = this.bitSet.nextClearBit(idx);
      if (nextMissingIndex < 0) {
        break;
      }

      lastSetIndex = nextMissingIndex-1;

      int nextReceivedIndex = this.bitSet.nextSetBit(nextMissingIndex+1);
      long nextReceivedVersion = -1;
      if (nextReceivedIndex > 0) {
        lastSetIndex = nextReceivedIndex;
        nextReceivedVersion = (long)(nextReceivedIndex) + this.bitSetVersion;
        idx = nextReceivedIndex+1;
        if (RegionVersionVector.DEBUG && logger != null) {
          logger.info(LocalizedStrings.DEBUG, "found gap in bitSet: missing bit at index="+nextMissingIndex+"; next set index="+nextReceivedIndex);
        }
      } else {
        // We can't flush any more bits from the bit set because there
        //are no more received versions
        if (RegionVersionVector.DEBUG && logger != null) {
          logger.info(LocalizedStrings.DEBUG, "terminating flush at bit " + lastSetIndex + " because of missing entries");
        }
        this.bitSetVersion += lastSetIndex;
        this.bitSet.clear();
        if(lastSetIndex != -1) {
          this.bitSet.set(0);
        }
        return;
      }
      long nextMissingVersion = Math.max(1, nextMissingIndex+this.bitSetVersion);
      if (nextReceivedVersion > nextMissingVersion) {
        addException(nextMissingVersion-1, nextReceivedVersion);
        if (RegionVersionVector.DEBUG && logger != null) {
          logger.info(LocalizedStrings.DEBUG, "added rvv exception e{rv" + (nextMissingVersion-1) + " - rv" + nextReceivedVersion + "}");
        }
      }
    }
    this.bitSet = this.bitSet.get(lastSetIndex, Math.max(lastSetIndex+1, bitSet.size()));
    if (lastSetIndex > 0) {
      this.bitSetVersion = this.bitSetVersion + (long)lastSetIndex;
    }
  }

  private void addBitSet(int numBits, long newVersion, LogWriterI18n logger) {
    int lastSetIndex = -1;

    if (RegionVersionVector.DEBUG && logger != null) {
      logger.info(LocalizedStrings.DEBUG, "addBitSetExceptions("+numBits+","+newVersion+")");
    }

    for (int idx = 0; idx < numBits; ) {
      int nextMissingIndex = this.bitSet.nextClearBit(idx);
      if (nextMissingIndex < 0) {
        break;
      }

      lastSetIndex = nextMissingIndex-1;

      int nextReceivedIndex = this.bitSet.nextSetBit(nextMissingIndex+1);
      long nextReceivedVersion = -1;
      if (nextReceivedIndex > 0) {
        lastSetIndex = nextReceivedIndex;
        nextReceivedVersion = (long)(nextReceivedIndex) + this.bitSetVersion;
        idx = nextReceivedIndex+1;
        if (RegionVersionVector.DEBUG && logger != null) {
          logger.info(LocalizedStrings.DEBUG, "found gap in bitSet: missing bit at index="+nextMissingIndex+"; next set index="+nextReceivedIndex);
        }
      } else {
        // We can't flush any more bits from the bit set because there
        //are no more received versions
        if (RegionVersionVector.DEBUG && logger != null) {
          logger.info(LocalizedStrings.DEBUG, "terminating flush at bit " + lastSetIndex + " because of missing entries");
        }
        this.bitSetVersion += lastSetIndex;
        this.bitSet.clear();
        if(lastSetIndex != -1) {
          this.bitSet.set(0);
        }
        return;
      }
      long nextMissingVersion = Math.max(1, nextMissingIndex+this.bitSetVersion);
      if (nextReceivedVersion > nextMissingVersion) {
        //addException(nextMissingVersion-1, nextReceivedVersion);
        if (RegionVersionVector.DEBUG && logger != null) {
          logger.info(LocalizedStrings.DEBUG, "added rvv exception e{rv" + (nextMissingVersion-1) + " - rv" + nextReceivedVersion + "}");
        }
      }
    }
    this.bitSet = this.bitSet.get(lastSetIndex, Math.max(lastSetIndex+1, bitSet.size()));
    if (lastSetIndex > 0) {
      this.bitSetVersion = this.bitSetVersion + (long)lastSetIndex;
    }
  }

  synchronized void recordVersion(long version, LogWriterI18n logger) {
    updateVersion(version, logger);
  }

  private void updateVersion(long version, LogWriterI18n logger) {
    RVVException sp = null;
    if (this.version != version) {
      if (this.bitSet == null) {
        if (this.version < version-1) {
          this.addException(this.version, version);
          if (RegionVersionVector.DEBUG && logger != null) {
            logger.info(LocalizedStrings.DEBUG, "added rvv exception e{rv" + this.version + " - rv" + version + "}");
          }
        } else if (this.version > version) {
          sp = this.getSpecialException();
          this.addOlderVersion(version, logger);
        }
      } else { // have a bitSet
        if (this.bitSetVersion + BIT_SET_WIDTH - 1 < version) {
          this.flushBitSetDuringRecording(version, logger);
        }
        if (version < this.bitSetVersion) {
          this.addOlderVersion(version, logger);
        } else {
          // If there's special exception, version maybe >= this.bitSetVersion. We need to fill the hole
          // in the special exception. For example, holder=R5(3,6), bitSetVersion=3, bs=[0]. Adding version=4
          // will become: holder=R5(4,6), bitsetVersion=3, bs[0,1]
          sp = this.getSpecialException();
          if (sp != null) {
            this.addOlderVersion(version, logger);
          }
          this.bitSet.set((int)(version-this.bitSetVersion));
        }
      }
      if (sp != null && version >= this.version) {
        removeSpecialException();
      }
      this.version = Math.max(this.version, version);

      // when we set this.version we need to make sure that special exception if any needs to made normal exception
      /*if (this.version == version) {
        this.exceptions.remove(0);
      }*/
      //convert special exception to normal exception if


    } else {
      if (this.bitSet != null && version>=this.bitSetVersion) {
        this.bitSet.set((int)(version - this.bitSetVersion));
      }
      sp = this.getSpecialException();
      if (sp != null) {
        removeSpecialException();
      }
      this.addOlderVersion(version, logger);
    }

  }

  synchronized long getNextAndRecordVersion(LogWriterI18n logger) {
    final long nextVersion = this.version + 1;
    updateVersion(nextVersion, logger);
    return nextVersion;
  }

  /**
   * Add an exception that is older than this.bitSetVersion.
   */
  protected synchronized void addException(long previousVersion, long nextVersion) {
    if (this.exceptions == null) {
      this.exceptions = new LinkedList<RVVException>();
    }
    int i = 0;
    for (Iterator<RVVException> it=this.exceptions.iterator(); it.hasNext(); i++) {
      RVVException e = it.next();
      if (previousVersion >= e.nextVersion) {
        RVVException except = RVVException.createException(previousVersion, nextVersion);
        this.exceptions.add(i, except);
        return;
      }
    }
    this.exceptions.add(RVVException.createException(previousVersion, nextVersion));
  }
  
  synchronized void removeExceptionsOlderThan(long v) {
    mergeBitSet();
    if (this.exceptions != null) {
      for (Iterator<RVVException> it=this.exceptions.iterator(); it.hasNext();) {
        RVVException e = it.next();
        if (e.nextVersion <= v) {
          it.remove();
        }
      }
      if (this.exceptions.isEmpty()) {
        this.exceptions = null;
      }
    }
  }
  
  /**
   * Initialize this version holder from another version holder
   * This is called during GII.
   * 
   * It's more likely that the other holder has seen most of the
   * versions, and this version holder only has
   * a few updates that happened since the GII started. So we apply
   * our seen versions to the other version holder and then initialize
   * this version holder from the other version holder. 
   */
  public synchronized void initializeFrom(RegionVersionHolder<T> source) {
    //Make sure the bitsets are merged in both the source
    //and this vector
    mergeBitSet();
    
    RegionVersionHolder<T> other = source.clone();
    other.mergeBitSet();

    //Get a copy of the local version and exceptions
    long myVersion = this.version;

    List<RVVException> myexception = this.exceptions;
    //initialize our version and exceptions to match the others
    this.exceptions = other.exceptions;
    this.version = other.version;
    
    //Initialize the bit set to be empty. Merge bit set should
    //have already done this, but just to be sure.
    if(this.bitSet != null) {
      this.bitSetVersion=this.version;
    //Make sure the bit set is empty except for the first, bit, indicating
    //that the version has been received.
      this.bitSet.set(0);
    }
    
    // Now if this.version/exceptions overlap with myVersion/myExceptions, use this'
    // The only case needs special handling is: if myVersion is newer than this.version,
    // should create an exception (this.version+1, myversion) and set this.version=myversion
    if (myVersion > this.version) {
      // this is
      RVVException e = RVVException.createException(this.version, myVersion+1);
      /*// only for those exception for which next is greater than myVersion
      if (myexception != null) {
        for (RVVException exception: this.exceptions) {
          if (e.compareTo(exception) >= 0) {
            break;
          }
          i++;
        }
      }*/
      // add special exception
      if (this.exceptions == null) {
        this.exceptions = new LinkedList<RVVException>();
      }
      int i=0;
      for (RVVException exception: this.exceptions) {
        if (e.compareTo(exception) >= 0) {
          break;
        }
        i++;
      }
      this.exceptions.add(i, e);
      this.version = myVersion;
      /*if(this.bitSet !=null) {
        this.bitSetVersion = myVersion;
        this.bitSet.set(0);
      }*/
    }
  }
  
  /**
   * initialize a holder that was cloned from another holder so it is
   * ready for use in a live vector
   */
  void makeReadyForRecording() {
    if (this.bitSet == null) {
      this.bitSet = new BitSet(BIT_SET_WIDTH);
      this.bitSetVersion = this.version;
      this.bitSet.set(0);
    }
  }
  

  /**
   * returns true if this version holder has seen the given version number
   */
  synchronized public boolean contains(long v) {
    if (v > getVersion()) {
      return false;
    } else {
      if (this.bitSet != null && v >= this.bitSetVersion) {
        return this.bitSet.get((int)(v-this.bitSetVersion));
      }
      if (this.exceptions == null) {
        return true;
      }
      for (Iterator<RVVException> it = this.exceptions.iterator(); it.hasNext(); ) {
        RVVException e = it.next();

        if (e.nextVersion <= v) {
          return true ;  // there is no RVVException for this version
        }
        if (e.previousVersion < v  &&  v < e.nextVersion) {
          return e.contains(v);
        }
      }
      return true;
    }
  }
  
  /**
   * Returns true if this version hold has an exception in the exception list
   * for the given version number.
   * 
   * This differs from contains because it returns true if v is greater
   * than the last seen version for this holder.
   */
  synchronized boolean hasExceptionFor(long v) {
    if (this.bitSet != null && v >= this.bitSetVersion) {
      if (v > this.bitSetVersion+this.bitSet.length()) {
        return false;
      }
      return this.bitSet.get((int)(v-this.bitSetVersion));
    }
    if (this.exceptions == null) {
      return false;
    }
    for (Iterator<RVVException> it = this.exceptions.iterator(); it.hasNext(); ) {
      RVVException e = it.next();
      if (e.nextVersion <= v) {
        return false;  // there is no RVVException for this version
      }
      if (e.previousVersion < v  &&  v < e.nextVersion) {
        return !e.contains(v);
      }
    }
    return false;
  }
  
  public boolean dominates(RegionVersionHolder<T> other) {
    return !other.isNewerThanOrCanFillExceptionsFor(this);
  }

  public boolean isSpecialException(RVVException e, RegionVersionHolder holder) {
    // deltaGII introduced a special exception, i.e. the hone is not in the middle, but at the end
    // For example, P was at P3, operation P4 is on-going and identified as unfinished operation. 
    // The next operation from P should be P5, but P's currentVersion() should be 3. In holder,
    // it's described as P3(2-4), i.e. exception.nextVersion == holder.version + 1
    return (e != null && e.nextVersion == holder.version + 1);
  }

  /** returns true if this holder has seen versions that the other holder hasn't */
  public synchronized boolean isNewerThanOrCanFillExceptionsFor(RegionVersionHolder<T> source) {
    if (source == null || getVersion() > source.getVersion()) {
      return true;
    }
    
    //Prevent synhronization issues if other is a live version vector.
    RegionVersionHolder<T> other = source.clone();
    
    // since the exception sets are sorted with most recent ones first
    // we can make one pass over both sets to see if there are overlapping
    // exceptions or exceptions I don't have that the other does
    mergeBitSet(); // dump the bit-set exceptions into the regular exceptions list
    other.mergeBitSet();
    List<RVVException> mine = canonicalExceptions(this.exceptions);
    Iterator<RVVException> myIterator = mine.iterator();
    List<RVVException> his = canonicalExceptions(other.exceptions);
    Iterator<RVVException> otherIterator = his.iterator();
//    System.out.println("comparing " + mine + " with " + his);
    RVVException myException = myIterator.hasNext()? myIterator.next() : null;
    RVVException otherException = otherIterator.hasNext()? otherIterator.next() : null;
    // I can't fill exceptions that are newer than anything I've seen, so skip them
    while ((otherException != null && otherException.previousVersion > this.version)
        || isSpecialException(otherException, other)) {
      otherException = otherIterator.hasNext()? otherIterator.next() : null;
    }
    while (otherException != null) {
//      System.out.println("comparing " + myException + " with " + otherException);
      if (myException == null) {
        return true;
      }
      if (isSpecialException(myException, this)) {
        // skip special exception
        myException = myIterator.hasNext()? myIterator.next() : null;
        continue;
      }
      if (isSpecialException(otherException, other)) {
        // skip special exception
        otherException = otherIterator.hasNext()? otherIterator.next() : null;
        continue;
      }
      if (myException.previousVersion >= otherException.nextVersion) {
        //        |____|  my exception
        // |____|         other exception
        // my exception is newer than the other exception, so get the next one in the sorted list
        myException = myIterator.hasNext()? myIterator.next() : null;
        continue;
      }
      if (otherException.previousVersion >= myException.nextVersion) {
        // |____|         my exception
        //        |____|  other exception
        // my exception is older than the other exception, so I have seen changes
        // it has not
        return true;
      }
      if ((myException.previousVersion == otherException.previousVersion)
          && (myException.nextVersion == otherException.nextVersion)) {
        // |____| my exception
        // |____|   -- other exception
        // If the exceptions are identical we can skip both of them and
        // go to the next pair
        myException = myIterator.hasNext()? myIterator.next() : null;
        otherException = otherIterator.hasNext()? otherIterator.next() : null;
        continue;
      }
      // There is some overlap between my exception and the other exception.
      //
      //     |_________________|       my exception
      //   |____|                   \
      //            |____|*          \ the other exception is one of
      //                    |____|   / these
      //   |_____________________|  /
      //
      // Unless my exception completely contains the other exception (*)
      // I have seen changes the other hasn't
      if ((otherException.previousVersion < myException.previousVersion)
          || (myException.nextVersion < otherException.nextVersion)) {
        return true;
      }
      // My exception completely contains the other exception and I have not
      // received any thing within its exception's range that it has not also seen
      otherException = otherIterator.hasNext()? otherIterator.next() : null;
    }
//    System.out.println("Done iterating and returning false");
    return false;
  }

  /* (non-Javadoc)
   * @see com.gemstone.gemfire.DataSerializable#toData(java.io.DataOutput)
   * 
   * Version Holders serialized to disk, so if the serialization
   * format of version holder changes, we need to upgrade our persistence
   * format.
   */
  public synchronized void toData(DataOutput out) throws IOException {
    mergeBitSet();
    InternalDataSerializer.writeUnsignedVL(this.version, out);
    int size = (this.exceptions == null) ? 0 : this.exceptions.size();
    InternalDataSerializer.writeUnsignedVL(size, out);
    out.writeBoolean(this.isDepartedMember);
    if (size > 0) {
      for (RVVException e: this.exceptions) {
        InternalDataSerializer.invokeToData(e, out);
      }
    }
  }

  /* (non-Javadoc)
   * @see com.gemstone.gemfire.DataSerializable#fromData(java.io.DataInput)
   */
  public void fromData(DataInput in) throws IOException {
    this.version = InternalDataSerializer.readUnsignedVL(in);
    int size = (int) InternalDataSerializer.readUnsignedVL(in);
    this.isDepartedMember = in.readBoolean();
    if (size > 0) {
      this.exceptions = new LinkedList<RVVException>();
      for (int i=0; i<size; i++) {
        RVVException e = RVVException.createException(in);
        this.exceptions.add(e);
      }
    }
  }
  
  

  /* Warning: this hashcode uses mutable state and is only good for as long
   * as the holder is not modified.  It was added for unit testing.
   * 
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public synchronized int hashCode() {
    mergeBitSet();
    final int prime = 31;
    int result = 1;
    result = prime * result + (int)version;
    result = prime * result + (int)(version>>32);
    result = prime * result + canonicalExceptions(exceptions).hashCode();
    return result;
  }

  // special exception will be kept in clone, but sometime we need to remove it for comparing
  // 2 RegionVersionHolders are actually the same
  void removeSpecialException() {
    if (this.exceptions != null && !this.exceptions.isEmpty()) {
      for (Iterator<RVVException> it=this.exceptions.iterator(); it.hasNext();) {
        RVVException e = it.next();
        if (isSpecialException(e, this)) {
          it.remove();
        }
      }
      if (this.exceptions.isEmpty()) {
        this.exceptions = null;
      }
    }
  }
  
  /** For test purposes only. Two
   * RVVs that have effectively same exceptions
   * may represent the exceptions differently. This
   * method will test to see if the exception lists are
   * effectively the same, regardless of representation.
   */
  public synchronized boolean sameAs(RegionVersionHolder<T> other) {
    mergeBitSet();
    if (getVersion() != other.getVersion()) {
      return false;
    }
    RegionVersionHolder<T> vh1 = this.clone();
    RegionVersionHolder<T> vh2 = other.clone();
    vh1.removeSpecialException();
    vh2.removeSpecialException();
    if (vh1.exceptions == null || vh1.exceptions.isEmpty()) {
      if (vh2.exceptions != null && !vh2.exceptions.isEmpty()) {
        return false;
      }
    } else {
      List<RVVException> e1 = canonicalExceptions(vh1.exceptions);
      List<RVVException> e2 = canonicalExceptions(vh2.exceptions);
      Iterator<RVVException> it1 = e1.iterator();
      Iterator<RVVException> it2 = e2.iterator();
      while (it1.hasNext() && it2.hasNext()) {
        if (!it1.next().sameAs(it2.next())) {
          return false;
        }
      }
      return (!it1.hasNext() && !it2.hasNext());
    }
    
    return true;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RegionVersionHolder)) {
      return false;
    }
    return sameAs((RegionVersionHolder)obj);
  }

  /**
   * Canonicalize an ordered set of exceptions. In the canonical form,
   * none of the RVVExceptions have any received versions.
   * @param exceptions
   * @return The canonicalized set of exceptions.
   */
  protected List<RVVException> canonicalExceptions(List<RVVException> exceptions) {
    LinkedList<RVVException> canon = new LinkedList<RVVException>();
    if (exceptions != null) {
      //Iterate through the set of exceptions
      for(RVVException exception : exceptions) {
        if (exception.isEmpty()) {
          canon.add(exception);
        } else {
          long previous = exception.previousVersion;
          //Iterate through the set of received versions for this exception
          int insertAt = canon.size();
          for(ReceivedVersionsIterator it = exception.receivedVersionsIterator(); it.hasNext(); ) {
            Long received = it.next();
            //If we find a gap between the previous received version and the
            //next received version, add an exception.
            if(received != previous + 1) {
              canon.add(insertAt, RVVException.createException(previous, received));
            }
            //move the previous reference
            previous = received;
          }
          
          //if there is a gap between the last received version and the next
          //version, add an exception
          //this also handles the case where the RVV has no received versions,
          //because previous==exception.previousVersion in that case.
          if(exception.nextVersion != previous + 1) {
            canon.add(insertAt, RVVException.createException(previous, exception.nextVersion));
          }
        }
      }
    }
    return canon;
  }
  

  
}
