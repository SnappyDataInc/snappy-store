/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.execute.BasicSortObserver

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/*
 * Changes for GemFireXD distributed data platform (some marked by "GemStone changes")
 *
 * Portions Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
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

package com.pivotal.gemfirexd.internal.impl.sql.execute;

import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.io.Storable;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.store.access.SortObserver;
import com.pivotal.gemfirexd.internal.iapi.types.CloneableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This is the most basic sort observer.  It
 * handles distinct sorts and non-distinct sorts.
 *
 */
public class BasicSortObserver implements SortObserver
{
	protected boolean 	doClone;
	protected final boolean	distinct;
	private final	  boolean	reuseWrappers;
	private 	  ExecRow	execRow;
	private final	  List<ExecRow> array;

	/**
	 * Simple constructor
	 *
	 * @param doClone If true, then rows that are retained
	 *		by the sorter will be cloned.  This is needed
	 *		if language is reusing row wrappers.
	 *
	 * @param distinct	If true, toss out duplicates.  
	 *		Otherwise, retain them.
	 *
	 * @param execRow	ExecRow to use as source of clone for store.
	 *
	 * @param reuseWrappers	Whether or not we can reuse the wrappers
	 */
	public BasicSortObserver(final boolean doClone, final boolean distinct, final ExecRow	execRow, final boolean reuseWrappers)
	{
		this.doClone = doClone;	
		this.distinct = distinct;
		this.execRow = execRow;
		this.reuseWrappers = reuseWrappers;
		array = new ArrayList<ExecRow>();
	}

	/**
	 * Called prior to inserting a distinct sort
	 * key.  
	 *
	 * @param insertRow the current row that the sorter
	 * 		is on the verge of retaining
	 *
	 * @return the row to be inserted by the sorter.  If null,
	 *		then nothing is inserted by the sorter.  Distinct
	 *		sorts will want to return null.
	 *
	 * @exception StandardException never thrown
	 */
	public ExecRow insertNonDuplicateKey(final ExecRow insertRow)
		throws StandardException
	{
		return (doClone) ? 
					getClone(insertRow) :
					insertRow;
	}	
	/**
	 * Called prior to inserting a duplicate sort
	 * key.  
	 *
	 * @param insertRow the current row that the sorter
	 * 		is on the verge of retaining.  It is a duplicate
	 * 		of existingRow.
	 *
	 * @param existingRow the row that is already in the
	 * 		the sorter which is a duplicate of insertRow
	 *
	 * @exception StandardException never thrown
	 */
	public ExecRow insertDuplicateKey(final ExecRow insertRow, final ExecRow existingRow) 
			throws StandardException
	{
		return (distinct) ?
					null :
						(doClone) ? 
							getClone(insertRow) :
							insertRow;

	}

	public void addToFreeList(final ExecRow objectArray, final int maxFreeListSize)
	{
		if (reuseWrappers && array.size() < maxFreeListSize)
		{
			array.add(objectArray);
		}
	}

	public ExecRow getArrayClone()
		throws StandardException
	{
		final int lastElement = array.size();

		if (lastElement > 0)
		{
                        final ExecRow retval = array.remove(lastElement - 1);
			return retval;
		}
		return execRow.getClone();
	}

// GemStone changes BEGIN
	@Override
	public boolean eliminateDuplicate(Object insertRow,
	    Object existingRow) {
	  return this.distinct;
	}

	@Override
	public boolean canSkipDuplicate() {
          return this.distinct;
        }

	@Override
	public void setTemplateRow(ExecRow row) {
	  this.execRow = row;
	}

	@Override
	public ExecRow getRowArray() throws StandardException {
          final int lastElement = array.size();
      
          if (lastElement > 0) {
            final ExecRow retval = array.remove(lastElement - 1);
            return retval;
          }
          return execRow;
        }
        // GemStone changes END

	private ExecRow getClone(final ExecRow origArray)
	{
		/* If the free list is not empty, then
		 * get an DataValueDescriptor[] from there and swap
		 * objects between that DataValueDescriptor[] and 
		 * origArray, returning the DataValueDescriptor[]
		 * from the free list.  That will save
		 * on unnecessary cloning.
		 */
/* RESOLVE - We can't enable this code
 * until Bug 2829 is fixed.
 * (Close bug 2828 when enabling the code.
		if (vector.size() > 0)
		{
			DataValueDescriptor[] retval = getArrayClone();
			for (int index = 0; index < retval.length; index++)
			{
				DataValueDescriptor tmp = origArray[index];
				origArray[index] = retval[index];
				retval[index] = tmp;
			}
			return retval;
		}
*/
	       /*
		DataValueDescriptor[] newArray = new DataValueDescriptor[origArray.nColumns()];
		for (int i = 0; i < origArray.length; i++)
		{
			// the only difference between getClone and cloneObject is cloneObject does
			// not objectify a stream.  We use getClone here.  Beetle 4896.
			newArray[i] = origArray[i].getClone();
		}

		return newArray;
		*/
	        final ExecRow clone = origArray.getClone();
	        clone.setAllRegionAndKeyInfo(origArray.getAllRegionAndKeyInfo());
	        return clone;
	}
	
	// GemStone changes BEGIN
	public final boolean isDistinct() {
	  return distinct;
	}
	// GemStone changes END
}
