/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.compile.Level2OptimizerFactoryImpl

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

package com.pivotal.gemfirexd.internal.impl.sql.compile;









import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.reference.Property;
import com.pivotal.gemfirexd.internal.iapi.services.context.ContextManager;
import com.pivotal.gemfirexd.internal.iapi.services.monitor.ModuleControl;
import com.pivotal.gemfirexd.internal.iapi.services.property.PropertyUtil;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
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
import com.pivotal.gemfirexd.internal.impl.sql.compile.OptimizerFactoryImpl;

import java.util.Properties;

/**
	This is simply the factory for creating an optimizer.
 */

public class Level2OptimizerFactoryImpl
	extends OptimizerFactoryImpl 
{

	//
	// ModuleControl interface
	//

	public void boot(boolean create, Properties startParams)
			throws StandardException 
	{
		super.boot(create, startParams);
	}

	//
	// OptimizerFactory interface
	//

	/**
	 * @see OptimizerFactory#supportsOptimizerTrace
	 */
	public boolean supportsOptimizerTrace()
	{
		return true;
	}

	//
	// class interface
	//
	public Level2OptimizerFactoryImpl() 
	{
	}

	protected Optimizer getOptimizerImpl(
								  OptimizableList optimizableList,
								  OptimizablePredicateList predList,
								  DataDictionary dDictionary,
								  RequiredRowOrdering requiredRowOrdering,
								  int numTablesInQuery,
								  LanguageConnectionContext lcc)
				throws StandardException
	{

		return new Level2OptimizerImpl(
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
		return new Level2CostEstimateImpl();
	}
}

