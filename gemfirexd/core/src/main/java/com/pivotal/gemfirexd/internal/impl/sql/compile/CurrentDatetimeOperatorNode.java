/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.compile.CurrentDatetimeOperatorNode

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

package	com.pivotal.gemfirexd.internal.impl.sql.compile;







import java.lang.reflect.Modifier;

import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.compiler.LocalField;
import com.pivotal.gemfirexd.internal.iapi.services.compiler.MethodBuilder;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.CompilerContext;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDictionary;
import com.pivotal.gemfirexd.internal.iapi.store.access.Qualifier;
import com.pivotal.gemfirexd.internal.iapi.types.DataTypeDescriptor;
import com.pivotal.gemfirexd.internal.iapi.types.TypeId;
import com.pivotal.gemfirexd.internal.impl.sql.compile.ExpressionClassBuilder;

import java.sql.Types;

import java.util.Vector;

/**
 * The CurrentDatetimeOperator operator is for the builtin CURRENT_DATE,
 * CURRENT_TIME, and CURRENT_TIMESTAMP operations.
 *
 */
public class CurrentDatetimeOperatorNode extends ValueNode {

	public static final int CURRENT_DATE = 0;
	public static final int CURRENT_TIME = 1;
	public static final int CURRENT_TIMESTAMP = 2;

	static private final int jdbcTypeId[] = { 
		Types.DATE, 
		Types.TIME,
		Types.TIMESTAMP
	};
	static private final String methodName[] = { // used in toString only
		"CURRENT DATE",
		"CURRENT TIME",
		"CURRENT TIMESTAMP"
	};

	private int whichType;

	public void init(Object whichType) {
		this.whichType = ((Integer) whichType).intValue();

		if (SanityManager.DEBUG)
			SanityManager.ASSERT(this.whichType >= 0 && this.whichType <= 2);
	}

	//
	// QueryTreeNode interface
	//

	/**
	 * Binding this expression means setting the result DataTypeServices.
	 * In this case, the result type is based on the operation requested.
	 *
	 * @param fromList			The FROM list for the statement.  This parameter
	 *							is not used in this case.
	 * @param subqueryList		The subquery list being built as we find 
	 *							SubqueryNodes. Not used in this case.
	 * @param aggregateVector	The aggregate vector being built as we find 
	 *							AggregateNodes. Not used in this case.
	 *
	 * @return	The new top of the expression tree.
	 *
	 * @exception StandardException		Thrown on error
	 */
	public ValueNode bindExpression(FromList fromList, SubqueryList subqueryList,
							Vector	aggregateVector)
					throws StandardException
	{
		checkReliability( methodName[whichType], CompilerContext.DATETIME_ILLEGAL );

		setType(DataTypeDescriptor.getBuiltInDataTypeDescriptor(
						jdbcTypeId[whichType],
						false		/* Not nullable */
					)
				);
		return this;
	}

	/**
	 * Return the variant type for the underlying expression.
	 * The variant type can be:
	 *		VARIANT				- variant within a scan
	 *							  (method calls and non-static field access)
	 *		SCAN_INVARIANT		- invariant within a scan
	 *							  (column references from outer tables)
	 *		QUERY_INVARIANT		- invariant within the life of a query
	 *							  (constant expressions)
	 *
	 * @return	The variant type for the underlying expression.
	 */
	protected int getOrderableVariantType()
	{
		// CurrentDate, Time, Timestamp are invariant for the life of the query
		return Qualifier.QUERY_INVARIANT;
	}

	/**
	 * CurrentDatetimeOperatorNode is used in expressions.
	 * The expression generated for it invokes a static method
	 * on a special Derby type to get the system time and
	 * wrap it in the right java.sql type, and then wrap it
	 * into the right shape for an arbitrary value, i.e. a column
	 * holder. This is very similar to what constants do.
	 *
	 * @param acb	The ExpressionClassBuilder for the class being built
	 * @param mb	The method the code to place the code
	 *
	 * @exception StandardException		Thrown on error
	 */
	public void generateExpression(ExpressionClassBuilder acb,
											MethodBuilder mb)
									throws StandardException
	{
		/*
		** First, we generate the current expression to be stuffed into
		** the right shape of holder.
		*/
		switch (whichType) {
			case CURRENT_DATE: 
				acb.getCurrentDateExpression(mb);
				break;
			case CURRENT_TIME: 
				acb.getCurrentTimeExpression(mb);
				break;
			case CURRENT_TIMESTAMP: 
				acb.getCurrentTimestampExpression(mb);
				break;
		}

		acb.generateDataValue(mb, getTypeCompiler(), 
				getTypeServices().getCollationType(), (LocalField)null);
	}

	/*
		print the non-node subfields
	 */
	public String toString() {
		if (SanityManager.DEBUG)
		{
			return super.toString()+"method = "+methodName[whichType]+"\n";
		}
		else
		{
			return "";
		}
	}
        
        /**
         * {@inheritDoc}
         */
	protected boolean isEquivalent(ValueNode o)
	{
		if (isSameNodeType(o)) 
		{
			CurrentDatetimeOperatorNode other = (CurrentDatetimeOperatorNode)o;
			return other.whichType == whichType;
		}
		return false;
	}
// GemStone changes BEGIN
	public String getMethodName() {
	  return methodName[whichType];
	}
// GemStone changes END
}
