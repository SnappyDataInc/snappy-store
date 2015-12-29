/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.compile.OptimizerFactoryImpl

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

package com.pivotal.gemfirexd.internal.impl.sql.compile;








import com.pivotal.gemfirexd.internal.engine.sql.compile.DistributedHashFetchRowsJoinStrategy;
import com.pivotal.gemfirexd.internal.engine.sql.compile.DistributedJoinStrategy;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.reference.Property;
import com.pivotal.gemfirexd.internal.iapi.services.context.ContextManager;
import com.pivotal.gemfirexd.internal.iapi.services.monitor.ModuleControl;
import com.pivotal.gemfirexd.internal.iapi.services.property.PropertyUtil;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.CompilerContext;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.CostEstimate;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.JoinStrategy;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.OptimizableList;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.OptimizablePredicateList;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.Optimizer;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.OptimizerFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.compile.RequiredRowOrdering;
import com.pivotal.gemfirexd.internal.iapi.sql.conn.LanguageConnectionContext;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.DataDictionary;
import com.pivotal.gemfirexd.internal.iapi.store.access.TransactionController;

import java.util.Properties;

/**
	This is simply the factory for creating an optimizer.
 */

public class OptimizerFactoryImpl
	implements ModuleControl, OptimizerFactory {

	protected String optimizerId = null;
	protected boolean ruleBasedOptimization = false;
	protected boolean noTimeout = false;
	protected boolean useStatistics = true;
	protected int maxMemoryPerTable = 1048576;

	/*
	** The fact that we have one set of join strategies for use by all
	** optimizers means that the JoinStrategy[] must be immutable, and
	** also each JoinStrategy must be immutable.
	*/
	protected JoinStrategy[] joinStrategySet;

	// GemStone changes BEGIN
	protected DistributedJoinStrategy[] distJoinStrategySet;
	// GemStone changes END
	//
	// ModuleControl interface
	//

	public void boot(boolean create, Properties startParams)
			throws StandardException {

		/*
		** This property determines whether to use rule-based or cost-based
		** optimization.  It is used mainly for testing - there are many tests
		** that assume rule-based optimization.  The default is cost-based
		** optimization.
		*/
		ruleBasedOptimization =
				Boolean.valueOf(
					PropertyUtil.getSystemProperty(Optimizer.RULE_BASED_OPTIMIZATION)
								).booleanValue();

		/*
		** This property determines whether the optimizer should ever stop
		** optimizing a query because it has spent too long in optimization.
		** The default is that it will.
		*/
		noTimeout =
				Boolean.valueOf(
					PropertyUtil.getSystemProperty(Optimizer.NO_TIMEOUT)
								).booleanValue();

		/*
		** This property determines the maximum size of memory (in KB)
		** the optimizer can use for each table.  If an access path takes
		** memory larger than that size for a table, the access path is skipped.
		** Default is 1024 (KB).
		*/
		String maxMemValue = PropertyUtil.getSystemProperty(Optimizer.MAX_MEMORY_PER_TABLE);
		if (maxMemValue != null)
		{
			int intValue = Integer.parseInt(maxMemValue);
			if (intValue >= 0)
				maxMemoryPerTable = intValue * 1024;
		}

		String us =	PropertyUtil.getSystemProperty(Optimizer.USE_STATISTICS); 
		if (us != null)
			useStatistics = (Boolean.valueOf(us)).booleanValue();

		/* Allocation of joinStrategySet deferred til
		 * getOptimizer(), even though we only need 1
		 * array for this factory.  We defer allocation
		 * to improve boot time on small devices.
		 */
	}

	public void stop() {
	}

	//
	// OptimizerFactory interface
	//

	/**
	 * @see OptimizerFactory#getOptimizer
	 *
	 * @exception StandardException		Thrown on error
	 */
	public Optimizer getOptimizer(OptimizableList optimizableList,
								  OptimizablePredicateList predList,
								  DataDictionary dDictionary,
								  RequiredRowOrdering requiredRowOrdering,
								  int numTablesInQuery,
								  LanguageConnectionContext lcc)
				throws StandardException
	{
		/* Get/set up the array of join strategies.
		 * See comment in boot().  If joinStrategySet
		 * is null, then we may do needless allocations
		 * in a multi-user environment if multiple
		 * users find it null on entry.  However, 
		 * assignment of array is atomic, so system
		 * will be consistent even in rare case
		 * where users get different arrays.
		 */
		if (joinStrategySet == null)
		{
			JoinStrategy[] jss = new JoinStrategy[2];
			jss[0] = new NestedLoopJoinStrategy();
			jss[1] = new HashJoinStrategy();
			joinStrategySet = jss;
		}

		// GemStone changes BEGIN
                if (distJoinStrategySet == null) {
                  DistributedJoinStrategy[] djss = new DistributedJoinStrategy[1];
                  djss[0] = new DistributedHashFetchRowsJoinStrategy();
                  distJoinStrategySet = djss;
                }
		// GemStone changes END
		return getOptimizerImpl(optimizableList,
							predList,
							dDictionary,
							requiredRowOrdering,
							numTablesInQuery,
							lcc);
	}

	/**
	 * @see OptimizerFactory#getCostEstimate
	 *
	 * @exception StandardException		Thrown on error
	 */
	public CostEstimate getCostEstimate()
		throws StandardException
	{
		return new CostEstimateImpl();
	}

	/**
	 * @see OptimizerFactory#supportsOptimizerTrace
	 */
	public boolean supportsOptimizerTrace()
	{
		return false;
	}

	//
	// class interface
	//
	public OptimizerFactoryImpl() {
	}

	protected Optimizer getOptimizerImpl(OptimizableList optimizableList,
								  OptimizablePredicateList predList,
								  DataDictionary dDictionary,
								  RequiredRowOrdering requiredRowOrdering,
								  int numTablesInQuery,
								  LanguageConnectionContext lcc)
				throws StandardException
	{

		return new OptimizerImpl(
							optimizableList,
							predList,
							dDictionary,
							ruleBasedOptimization,
							noTimeout,
							useStatistics,
							maxMemoryPerTable,
							joinStrategySet,
							distJoinStrategySet,
							lcc.getLockEscalationThreshold(),
							requiredRowOrdering,
							numTablesInQuery);
	}

	/**
	 * @see OptimizerFactory#getMaxMemoryPerTable
	 */
	public int getMaxMemoryPerTable()
	{
		return maxMemoryPerTable;
	}
}

