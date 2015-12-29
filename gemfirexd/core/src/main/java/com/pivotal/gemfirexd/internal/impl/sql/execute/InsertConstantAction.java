/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.execute.InsertConstantAction

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

package com.pivotal.gemfirexd.internal.impl.sql.execute;








import com.pivotal.gemfirexd.internal.catalog.UUID;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.context.ContextManager;
import com.pivotal.gemfirexd.internal.iapi.services.io.ArrayUtil;
import com.pivotal.gemfirexd.internal.iapi.services.io.FormatIdUtil;
import com.pivotal.gemfirexd.internal.iapi.services.stream.HeaderPrintWriter;
import com.pivotal.gemfirexd.internal.iapi.sql.conn.LanguageConnectionContext;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDictionary;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.IndexRowGenerator;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.TableDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ConstantAction;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.store.access.StaticCompiledOpenConglomInfo;
import com.pivotal.gemfirexd.internal.iapi.types.RowLocation;
import com.pivotal.gemfirexd.internal.shared.common.StoredFormatIds;

import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

import java.util.Properties;

/**
 *	This class  describes compiled constants that are passed into
 *	InsertResultSets.
 *
 */

public class InsertConstantAction extends WriteCursorConstantAction
{
	/********************************************************
	**
	**	This class implements Formatable. But it is NOT used
 	**	across either major or minor releases.  It is only
	** 	written persistently in stored prepared statements, 
	**	not in the replication stage.  SO, IT IS OK TO CHANGE
	**	ITS read/writeExternal.
	**
	********************************************************/

	/* Which (0-based) columns are indexed */
	boolean[]	indexedCols;

	/* These variables are needed to support Autoincrement-- after an insert
	 * we need to remember the last autoincrement value inserted into the 
	 * table and the user could do a search based on schema,table,columnname
	 */
	private String schemaName;
	private String tableName;
	private String columnNames[];

	/**
	 * An array of row location objects (0 based), one for each
	 * column in the table. If the column is an 
	 * autoincrement table then the array points to
	 * the row location of the column in SYSCOLUMNS.
	 * if not, then it contains null.
	 */
	protected RowLocation[] autoincRowLocation;
	private long[] autoincIncrement;
	
	// CONSTRUCTORS

	/**
	 * Public niladic constructor. Needed for Formatable interface to work.
	 *
	 */
    public	InsertConstantAction() { super(); }

	/**
	 *	Make the ConstantAction for an INSERT statement.
	 *
	 *  @param conglomId	Conglomerate ID.
	 *	@param heapSCOCI	StaticCompiledOpenConglomInfo for heap.
	 *  @param irgs			Index descriptors
	 *  @param indexCIDS	Conglomerate IDs of indices
	 *	@param indexSCOCIs	StaticCompiledOpenConglomInfos for indexes.
	 *  @param indexNames   Names of indices on this table for error reporting.
	 *  @param deferred		True means process as a deferred insert.
	 *  @param targetProperties	Properties on the target table.
	 *	@param targetUUID	UUID of target table
	 *	@param lockMode		The lockMode to use on the target table
	 *	@param fkInfo		Array of structures containing foreign key info, 
	 *						if any (may be null)
	 *	@param triggerInfo	Array of structures containing trigger info, 
	 *						if any (may be null)
     *  @param streamStorableHeapColIds Null for non rep. (0 based)
	 *  @param indexedCols	boolean[] of which (0-based) columns are indexed.
	 *  @param singleRowSource		Whether or not source is a single row source
	 *  @param autoincRowLocation Array of rowlocations of autoincrement values
	 * 							  in SYSCOLUMNS for each ai column.
	 */
	public	InsertConstantAction(TableDescriptor tableDescriptor,
								long				conglomId,
								StaticCompiledOpenConglomInfo heapSCOCI,
								IndexRowGenerator[]	irgs,
								long[]				indexCIDS,
								StaticCompiledOpenConglomInfo[] indexSCOCIs,
								String[]			indexNames,
								boolean				deferred,
								Properties			targetProperties,
								UUID				targetUUID,
								int 				lockMode,
								FKInfo[]			fkInfo,
								TriggerInfo			triggerInfo,
								int[]               streamStorableHeapColIds,
								boolean[]			indexedCols,
								boolean				singleRowSource,
								RowLocation[]		autoincRowLocation)
	{
		super(conglomId, 
			  heapSCOCI,
			  irgs, 
			  indexCIDS, 
			  indexSCOCIs,
			  indexNames,
			  deferred, 
			  targetProperties,
			  targetUUID,
			  lockMode,
			  fkInfo,	
			  triggerInfo,
			  (ExecRow)null, // never need to pass in a heap row
			  null,
			  null,
			  streamStorableHeapColIds,
			  singleRowSource
			  );
		this.indexedCols = indexedCols;
		this.autoincRowLocation = autoincRowLocation;
		this.schemaName = tableDescriptor.getSchemaName();
		this.tableName  = tableDescriptor.getName();
		this.columnNames = tableDescriptor.getColumnNamesArray();
		this.autoincIncrement = tableDescriptor.getAutoincIncrementArray();
		this.indexNames = indexNames;
	}

	// INTERFACE METHODS

	// Formatable methods
	public void readExternal (ObjectInput in)
		 throws IOException, ClassNotFoundException
	{
		Object[] objectArray = null;
		super.readExternal(in);
		indexedCols = ArrayUtil.readBooleanArray(in);

		// RESOLVEAUTOINCREMENT: this is the new stuff-- probably version!!
		objectArray = ArrayUtil.readObjectArray(in);
		
		if (objectArray != null)
		{
			// is there a better way to do cast the whole array?
			autoincRowLocation = new RowLocation[objectArray.length];
			for (int i = 0; i < objectArray.length; i++)
				autoincRowLocation[i] = (RowLocation)objectArray[i];
		}
		
		schemaName = (String)in.readObject();
		tableName  = (String)in.readObject();
		objectArray = ArrayUtil.readObjectArray(in);
		if (objectArray != null)
		{
			// is there a better way to do cast the whole array?
			columnNames = new String[objectArray.length];
			for (int i = 0; i < objectArray.length; i++)
				columnNames[i] = (String)objectArray[i];
		}
		
		autoincIncrement = ArrayUtil.readLongArray(in);
	}



	/**
	 * Write this object to a stream of stored objects.
	 *
	 * @param out write bytes here.
	 *
	 * @exception IOException		thrown on error
	 */
	public void writeExternal( ObjectOutput out )
		 throws IOException
	{
		super.writeExternal(out);
		ArrayUtil.writeBooleanArray(out, indexedCols);
		ArrayUtil.writeArray(out, autoincRowLocation);
		out.writeObject(schemaName);
		out.writeObject(tableName);
		ArrayUtil.writeArray(out, columnNames);
		ArrayUtil.writeLongArray(out, autoincIncrement);
	}

	/**
	  *	Gets the name of the schema that the table is in
	  *
	  *	@return	schema name
	  */
	public String getSchemaName() { return schemaName; }

	/**
	  *	Gets the name of the table being inserted into
	  *
	  *	@return	name of table being inserted into
	  */
	public String getTableName() { return tableName; }


	/**
	 * gets the name of the desired column in the taget table.
	 * 
	 * @param 	i	the column number
	 */
	public String getColumnName(int i) { return columnNames[i]; }

	/**
	 * gets the increment value for a column.
	 *
	 * @param 	i 	the column number
	 */
	public long   getAutoincIncrement(int i) { return autoincIncrement[i]; }

	/**
	 * Does the target table has autoincrement columns.
	 *
	 * @return 	True if the table has ai columns
	 */
	public boolean hasAutoincrement()
	{
		return (autoincRowLocation != null);
	}

	/**
	 * gets the row location 
	 */
	public RowLocation[] getAutoincRowLocation()
	{
		return autoincRowLocation;
	}
	
	/**
	 * Get the formatID which corresponds to this class.
	 *
	 *	@return	the formatID of this class
	 */
	public	int	getTypeFormatId()	{ return StoredFormatIds.INSERT_CONSTANT_ACTION_V01_ID; }

	// CLASS METHODS

}
