/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.catalog.DDColumnDependableFinder

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

package com.pivotal.gemfirexd.internal.impl.sql.catalog;


import com.pivotal.gemfirexd.internal.catalog.Dependable;
import com.pivotal.gemfirexd.internal.catalog.UUID;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.io.FormatableBitSet;
import com.pivotal.gemfirexd.internal.iapi.services.io.FormatableHashtable;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDictionary;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.TableDescriptor;

import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

/**
 *	Class for implementation of DependableFinder in the core DataDictionary 
 *	for referenced columns in a table.
 *
 *
 */

public class DDColumnDependableFinder extends DDdependableFinder
{
	////////////////////////////////////////////////////////////////////////
	//
	//  STATE
	//
	////////////////////////////////////////////////////////////////////////

	// write least amount of data to disk, just the byte array, not even
	// a FormatableBitSet
	private byte[] columnBitMap;

    ////////////////////////////////////////////////////////////////////////
    //
    //  CONSTRUCTORS
    //
    ////////////////////////////////////////////////////////////////////////

	/**
	 * Constructor same as in parent.
	 */
	public  DDColumnDependableFinder(int formatId)
	{
		super(formatId);
	}

        // GemStone changes BEGIN
        /**
         *  Zero-arg constructor to support Formatable interface
         */
        public  DDColumnDependableFinder()
        {
        }
        // GemStone changes END

	/**
	 * Constructor given referenced column bit map byte array as in FormatableBitSet
	 */
	public  DDColumnDependableFinder(int formatId, byte[] columnBitMap)
	{
		super(formatId);
		this.columnBitMap = columnBitMap;
	}

    ////////////////////////////////////////////////////////////////////////
    //
    //  DDColumnDependable METHODS
    //
    ////////////////////////////////////////////////////////////////////////

	/**
	 * Get the byte array encoding the bitmap of referenced columns in
	 * a table.
	 *
	 * @return		byte array as in a FormatableBitSet encoding column bit map
	 */
	public 	byte[]	getColumnBitMap()
	{
		return columnBitMap;
	}

	/**
	 * Set the byte array encoding the bitmap of referenced columns in
	 * a table.
	 *
	 * @param	columnBitMap	byte array as in a FormatableBitSet encoding column bit map
	 */
	public	void	setColumnBitMap(byte[] columnBitMap)
	{
		this.columnBitMap = columnBitMap;
	}

	/**
	 * Find a dependable object, which is essentially a table descriptor with
	 * referencedColumnMap field set.
	 *
	 * @param	dd data dictionary
	 * @param	dependableObjectID dependable object ID (table UUID)
	 * @return	a dependable, a table descriptor with referencedColumnMap
	 *			field set
	 */
	Dependable findDependable(DataDictionary dd, UUID dependableObjectID)
		throws StandardException
	{
		TableDescriptor td = dd.getTableDescriptor(dependableObjectID);
		if (td != null)  // see beetle 4444
			td.setReferencedColumnMap(new FormatableBitSet(columnBitMap));
		return td;
	}

    //////////////////////////////////////////////////////////////////
    //
    //  FORMATABLE METHODS
    //
    //////////////////////////////////////////////////////////////////

	/**
	 * Read this object from a stream of stored objects.  Just read the
	 * byte array, besides what the parent does.
	 *
	 * @param in read this.
	 */
	public void readExternal( ObjectInput in )
			throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		FormatableHashtable fh = (FormatableHashtable)in.readObject();
		columnBitMap = (byte[])fh.get("columnBitMap");
	}

	/**
	 * Write this object to a stream of stored objects.  Just write the
	 * byte array, besides what the parent does.
	 *
	 * @param out write bytes here.
	 */
	public void writeExternal( ObjectOutput out )
			throws IOException
	{
		super.writeExternal(out);
		FormatableHashtable fh = new FormatableHashtable();
		fh.put("columnBitMap", columnBitMap);
		out.writeObject(fh);
	}
}
