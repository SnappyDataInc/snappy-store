/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.catalog.SYSFOREIGNKEYSRowFactory

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

package com.pivotal.gemfirexd.internal.impl.sql.catalog;

import java.sql.Types;

import com.pivotal.gemfirexd.internal.catalog.UUID;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.services.uuid.UUIDFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.StatementType;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.CatalogRowFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDescriptorGenerator;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDictionary;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.ForeignKeyConstraintDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.ReferencedKeyConstraintDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.SubKeyConstraintDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.SystemColumn;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.TupleDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecIndexRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecutionFactory;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueFactory;
import com.pivotal.gemfirexd.internal.iapi.types.SQLChar;

/**
 * Factory for creating a SYSFOREIGNKEYS row.
 *
 */

public class SYSFOREIGNKEYSRowFactory extends CatalogRowFactory
{
	private  static final String	TABLENAME_STRING = "SYSFOREIGNKEYS";

	protected static final int		SYSFOREIGNKEYS_COLUMN_COUNT = 5;
	protected static final int		SYSFOREIGNKEYS_CONSTRAINTID = 1;
	protected static final int		SYSFOREIGNKEYS_CONGLOMERATEID = 2;
	protected static final int		SYSFOREIGNKEYS_KEYCONSTRAINTID = 3;
	protected static final int		SYSFOREIGNKEYS_DELETERULE = 4;
	protected static final int		SYSFOREIGNKEYS_UPDATERULE = 5;

	// Column widths
	protected static final int		SYSFOREIGNKEYS_CONSTRAINTID_WIDTH = 36;

	protected static final int		SYSFOREIGNKEYS_INDEX1_ID = 0;
	protected static final int		SYSFOREIGNKEYS_INDEX2_ID = 1;

	private static final int[][] indexColumnPositions = 
	{
		{SYSFOREIGNKEYS_CONSTRAINTID},
		{SYSFOREIGNKEYS_KEYCONSTRAINTID}
	};

    private	static	final	boolean[]	uniqueness = {
		                                               true,
													   false
	                                                 };

	private	static	final	String[]	uuids =
	{
		 "8000005b-00d0-fd77-3ed8-000a0a0b1900"	// catalog UUID
		,"80000060-00d0-fd77-3ed8-000a0a0b1900"	// heap UUID
		,"8000005d-00d0-fd77-3ed8-000a0a0b1900"	// SYSFOREIGNKEYS_INDEX1
		,"8000005f-00d0-fd77-3ed8-000a0a0b1900"	// SYSFOREIGNKEYS_INDEX2
	};

	/////////////////////////////////////////////////////////////////////////////
	//
	//	CONSTRUCTORS
	//
	/////////////////////////////////////////////////////////////////////////////

    SYSFOREIGNKEYSRowFactory(UUIDFactory uuidf, ExecutionFactory ef, DataValueFactory dvf)
	{
		super(uuidf,ef,dvf);
		initInfo(SYSFOREIGNKEYS_COLUMN_COUNT, TABLENAME_STRING, 
				 indexColumnPositions, uniqueness, uuids );
	}

	/////////////////////////////////////////////////////////////////////////////
	//
	//	METHODS
	//
	/////////////////////////////////////////////////////////////////////////////

  /**
	 * Make a SYSFOREIGNKEYS row
	 *
	 * @return	Row suitable for inserting into SYSFOREIGNKEYS.
	 *
	 * @exception   StandardException thrown on failure
	 */
	public ExecRow makeRow(TupleDescriptor td, TupleDescriptor parent)
					throws StandardException 
	{
		DataValueDescriptor		col;
		ExecIndexRow			row;
		String					constraintId = null;
		String					keyConstraintId = null;
		String					conglomId = null;
		String	                raDeleteRule="N";
		String					raUpdateRule="N";

		if (td != null)
		{
			ForeignKeyConstraintDescriptor cd = (ForeignKeyConstraintDescriptor)td;
			constraintId = cd.getUUID().toString();
			
			ReferencedKeyConstraintDescriptor refCd = cd.getReferencedConstraint();
			if (SanityManager.DEBUG)
			{
				SanityManager.ASSERT(refCd != null, "this fk returned a null referenced key");
			}
			keyConstraintId = refCd.getUUID().toString();
			conglomId = cd.getIndexUUIDString();

			raDeleteRule = getRefActionAsString(cd.getRaDeleteRule());
			raUpdateRule = getRefActionAsString(cd.getRaUpdateRule());
		}
			
			
		/* Build the row  */
		row = getExecutionFactory().getIndexableRow(SYSFOREIGNKEYS_COLUMN_COUNT);

		/* 1st column is CONSTRAINTID (UUID - char(36)) */
		row.setColumn(SYSFOREIGNKEYS_CONSTRAINTID, new SQLChar(constraintId));

		/* 2nd column is CONGLOMERATEID (UUID - char(36)) */
		row.setColumn(SYSFOREIGNKEYS_CONGLOMERATEID, new SQLChar(conglomId));

		/* 3rd column is KEYCONSTRAINTID (UUID - char(36)) */
		row.setColumn(SYSFOREIGNKEYS_KEYCONSTRAINTID, new SQLChar(keyConstraintId));

		// currently, DELETERULE and UPDATERULE are always "R" for restrict
		/* 4th column is DELETERULE char(1) */
		row.setColumn(SYSFOREIGNKEYS_DELETERULE, new SQLChar(raDeleteRule));

		/* 5th column is UPDATERULE char(1) */
		row.setColumn(SYSFOREIGNKEYS_UPDATERULE, new SQLChar(raUpdateRule));

		return row;
	}

	///////////////////////////////////////////////////////////////////////////
	//
	//	ABSTRACT METHODS TO BE IMPLEMENTED BY CHILDREN OF CatalogRowFactory
	//
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Make a ViewDescriptor out of a SYSFOREIGNKEYS row
	 *
	 * @param row a SYSFOREIGNKEYS row
	 * @param parentTupleDescriptor	Null for this kind of descriptor.
	 * @param dd dataDictionary
	 *
	 * @exception   StandardException thrown on failure
	 */
	public TupleDescriptor buildDescriptor(
		ExecRow					row,
		TupleDescriptor			parentTupleDescriptor,
		DataDictionary 			dd )
					throws StandardException
	{

		if (SanityManager.DEBUG)
		{
			SanityManager.ASSERT(
				row.nColumns() == SYSFOREIGNKEYS_COLUMN_COUNT, 
				"Wrong number of columns for a SYSKEYS row");
		}

		DataValueDescriptor		col;
		DataDescriptorGenerator ddg;
		UUID					constraintUUID;
		UUID					conglomerateUUID;
		UUID					keyConstraintUUID;
		String					constraintUUIDString;
		String					conglomerateUUIDString;
		String                  raRuleString;
		int                     raDeleteRule;
		int                     raUpdateRule;

		ddg = dd.getDataDescriptorGenerator();

		/* 1st column is CONSTRAINTID (UUID - char(36)) */
		col = row.getColumn(SYSFOREIGNKEYS_CONSTRAINTID);
		constraintUUIDString = col.getString();
		constraintUUID = getUUIDFactory().recreateUUID(constraintUUIDString);

		/* 2nd column is CONGLOMERATEID (UUID - char(36)) */
		col = row.getColumn(SYSFOREIGNKEYS_CONGLOMERATEID);
		conglomerateUUIDString = col.getString();
		conglomerateUUID = getUUIDFactory().recreateUUID(conglomerateUUIDString);

		/* 3rd column is KEYCONSTRAINTID (UUID - char(36)) */
		col = row.getColumn(SYSFOREIGNKEYS_KEYCONSTRAINTID);
		constraintUUIDString = col.getString();
		keyConstraintUUID = getUUIDFactory().recreateUUID(constraintUUIDString);


		/* 4th column is DELETERULE char(1) */
		col= row.getColumn(SYSFOREIGNKEYS_DELETERULE);
		raRuleString = col.getString();
		raDeleteRule  = getRefActionAsInt(raRuleString);
		
		/* 5th column is UPDATERULE char(1) */
		col = row.getColumn(SYSFOREIGNKEYS_UPDATERULE);
		raRuleString = col.getString();
		raUpdateRule  = getRefActionAsInt(raRuleString);


		/* now build and return the descriptor */
		return new SubKeyConstraintDescriptor(
										constraintUUID,
										conglomerateUUID,
										keyConstraintUUID,
										raDeleteRule,
										raUpdateRule);
	}

	/**
	 * Builds a list of columns suitable for creating this Catalog.
	 *
	 *
	 * @return array of SystemColumn suitable for making this catalog.
	 */
	public SystemColumn[]	buildColumnList()
        throws StandardException
	{
            return new SystemColumn[] {
                 SystemColumnImpl.getUUIDColumn("CONSTRAINTID", false),
                 SystemColumnImpl.getUUIDColumn("CONGLOMERATEID", false),
                 SystemColumnImpl.getUUIDColumn("KEYCONSTRAINTID", false),
                 SystemColumnImpl.getIndicatorColumn("DELETERULE"),
                 SystemColumnImpl.getIndicatorColumn("UPDATERULE"),
           
            };
	}


	int getRefActionAsInt(String raRuleString)
	{
		int raRule ;
		switch (raRuleString.charAt(0)){
		case 'C': 
			raRule = StatementType.RA_CASCADE;
			break;
		case 'S':
			raRule = StatementType.RA_RESTRICT;
			break;
		case 'R':
			raRule = StatementType.RA_NOACTION;
			break;
		case 'U':
			raRule = StatementType.RA_SETNULL;
			break;
		case 'D':
			raRule = StatementType.RA_SETDEFAULT;
			break;
		default: 
			raRule =StatementType.RA_NOACTION; ;
			if (SanityManager.DEBUG)
			{
				SanityManager.THROWASSERT("Invalid  value '"
										  +raRuleString+ "' for a referetial Action");
			}
		}
		return raRule ;
	}


	String getRefActionAsString(int raRule)
	{
		String raRuleString ;
		switch (raRule){
		case StatementType.RA_CASCADE:
			raRuleString = "C";
			break;
		case StatementType.RA_RESTRICT:
			raRuleString = "S";
				break;
		case StatementType.RA_NOACTION:
			raRuleString = "R";
			break;
		case StatementType.RA_SETNULL:
			raRuleString = "U";
			break;
		case StatementType.RA_SETDEFAULT:
			raRuleString = "D";
			raRule = StatementType.RA_SETDEFAULT;
			break;
		default: 
			raRuleString ="N" ; // NO ACTION (default value)
			if (SanityManager.DEBUG)
			{
				SanityManager.THROWASSERT("Invalid  value '"
							+raRule+ "' for a referetial Action");
			}

		}
		return raRuleString ;
	}


}
