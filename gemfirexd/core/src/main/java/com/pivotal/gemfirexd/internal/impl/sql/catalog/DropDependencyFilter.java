/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.catalog.DropDependencyFilter

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









import com.pivotal.gemfirexd.internal.catalog.UUID;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.context.ContextService;
import com.pivotal.gemfirexd.internal.iapi.services.monitor.Monitor;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.services.uuid.UUIDFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.conn.LanguageConnectionContext;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecutionFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.TupleFilter;
import com.pivotal.gemfirexd.internal.iapi.types.BooleanDataValue;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueFactory;


/**
* A Filter to qualify tuples coming from a scan of SYSDEPENDS.
* Tuples qualify if they have the right providerID.
*
*/
public class DropDependencyFilter implements TupleFilter
{
	//////////////////////////////////////////////////////////////////////////////////////
	//
	//	CONSTANTS
	//
	//////////////////////////////////////////////////////////////////////////////////////


	//////////////////////////////////////////////////////////////////////////////////////
	//
	//	STATE
	//
	//////////////////////////////////////////////////////////////////////////////////////

	UUID				providerID;

	UUIDFactory			uuidFactory = null;
	DataValueFactory	dataValueFactory = null;

	BooleanDataValue	trueValue;
	BooleanDataValue	falseValue;

	//////////////////////////////////////////////////////////////////////////////////////
	//
	//	CONSTRUCTORS
	//
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	  *	Construct a TupleFilter to qualify SYSDEPENDS rows with the
	  * designated providerID.
	  *
	  *	@param	providerID	UUID of provider. Tuples with this ID qualify.
	  */
	public	DropDependencyFilter( UUID providerID )
	{
		this.providerID = providerID;
	}


	//////////////////////////////////////////////////////////////////////////////////////
	//
	//	TupleFilter BEHAVIOR
	//
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	  *	Initialize a Filter with a vector of parameters. This is a NOP.
	  * We initialize this filter at Constructor time.
	  *
	  *	@param	parameters	An ExecRow of parameter values
	  *
	  * @exception StandardException		Thrown on error
	  */
    public	void	init( ExecRow parameters )
		throws StandardException
	{}

	/**
	  *	Pump a SYSDEPENDS row through the Filter. If the providerID of the
	  * row matches our providerID, we return true. Otherwise we return false.
	  *
	  *	@param	currentRow		SYSDEPENDS row
	  *
	  *	@return	True if the row has our providerID. False otherwise.
	  *
	  * @exception StandardException		Thrown on error
	  */
    public	BooleanDataValue	execute( ExecRow currentRow )
		throws StandardException
	{
		/* 3rd column is PROVIDERID (UUID - char(36)) */
		DataValueDescriptor	col = currentRow.getColumn(SYSDEPENDSRowFactory.SYSDEPENDS_PROVIDERID);
		String	providerIDstring = col.getString();
		UUID	providerUUID = getUUIDFactory().recreateUUID(providerIDstring);

		if ( providerID.equals( providerUUID ) ) { return getTrueValue(); }
		else { return getFalseValue(); }
	}


	//////////////////////////////////////////////////////////////////////////////////////
	//
	//	MINIONS
	//
	//////////////////////////////////////////////////////////////////////////////////////


	/**
	  *	Get the UUID factory
	  *
	  *	@return	the UUID factory
	  *
	  * @exception   StandardException thrown on failure
	  */
	private	UUIDFactory	getUUIDFactory()
		throws StandardException
	{
		if ( uuidFactory == null )
		{
			uuidFactory = Monitor.getMonitor().getUUIDFactory();
		}
		return	uuidFactory;
	}

	/**
	  *	Gets the DataValueFactory for this connection.
	  *
	  *	@return	the data value factory for this connection
	  */
	private DataValueFactory	getDataValueFactory()
	{
		if ( dataValueFactory == null )
		{
			LanguageConnectionContext	lcc = (LanguageConnectionContext) 
					                          ContextService.getContext
							                  (LanguageConnectionContext.CONTEXT_ID);

			dataValueFactory = lcc.getDataValueFactory();
		}

		return	dataValueFactory;
	}

	/**
	  *	Gets a BooleanDataValue representing TRUE.
	  *
	  *	@return	a TRUE value
	  * @exception StandardException		Thrown on error
	  */
	private BooleanDataValue	getTrueValue()
		throws StandardException
	{
		if ( trueValue == null )
		{
			trueValue = getDataValueFactory().getDataValue( true );
		}

		return	trueValue;
	}

	/**
	  *	Gets a BooleanDataValue representing FALSE
	  *
	  *	@return	a FALSE value
	  * @exception StandardException		Thrown on error
	  */
	private BooleanDataValue	getFalseValue()
		throws StandardException
	{
		if ( falseValue == null )
		{
			falseValue = getDataValueFactory().getDataValue( false );
		}

		return	falseValue;
	}

}
