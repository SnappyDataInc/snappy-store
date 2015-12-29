/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.sql.execute.DistinctScalarAggregateResultSet

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








import com.pivotal.gemfirexd.internal.engine.distributed.utils.GemFireXDUtils;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.io.FormatableArrayHolder;
import com.pivotal.gemfirexd.internal.iapi.services.loader.GeneratedMethod;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.sql.Activation;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecIndexRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.NoPutResultSet;
import com.pivotal.gemfirexd.internal.iapi.store.access.ColumnOrdering;
import com.pivotal.gemfirexd.internal.iapi.store.access.ScanController;
import com.pivotal.gemfirexd.internal.iapi.store.access.SortController;
import com.pivotal.gemfirexd.internal.iapi.store.access.SortObserver;
import com.pivotal.gemfirexd.internal.iapi.store.access.TransactionController;
import com.pivotal.gemfirexd.internal.impl.sql.execute.xplain.XPLAINUtil;

import java.util.Properties;


/**
 * This ResultSet evaluates scalar aggregates where
 * 1 (or more, in the future) of the aggregates are distinct.
 * It will scan the entire source result set and calculate
 * the scalar aggregates when scanning the source during the 
 * first call to next().
 *
 */
class DistinctScalarAggregateResultSet extends ScalarAggregateResultSet
{
	private ColumnOrdering[] order;
	private int maxRowSize;
	private boolean dropDistinctAggSort;
	private	long sortId;

	// set in open and not modified thereafter
    private ScanController scanController;

// GemStone changes BEGIN
        //increasing visibility
	//private ExecIndexRow sortResultRow;
        ExecIndexRow sortResultRow;

        /**
         * Set by RowCountResultSet in case of FETCH FIRST/NEXT clause.
         * Allows the sorter to be more intelligent and keep discarding higher
         * elements where possible during the sort.
         */
        private long maxSortLimit;
// GemStone changes END

	// remember whether or not any sort was performed
	private boolean sorted;

    /**
	 * Constructor
	 *
	 * @param	s			input result set
	 * @param	isInSortedOrder	true if the source results are in sorted order
	 * @param	aggregateItem	indicates the number of the
	 *		SavedObject off of the PreparedStatement that holds the
	 *		AggregatorInfoList used by this routine. 
	 * @param	a				activation
	 * @param	ra				generated method to build an empty
	 *	 	output row 
	 * @param	resultSetNumber	The resultSetNumber for this result set
	 *
	 * @exception StandardException Thrown on error
	 */
    DistinctScalarAggregateResultSet(NoPutResultSet s,
					boolean isInSortedOrder,
					int	aggregateItem,
					int	orderingItem,
					Activation a,
					GeneratedMethod ra,
					int maxRowSize,
					int resultSetNumber,
					boolean singleInputRow,
				    double optimizerEstimatedRowCount,
				    double optimizerEstimatedCost) throws StandardException 
	{
		super(s, isInSortedOrder, aggregateItem, a, ra,
			  resultSetNumber, 
			  singleInputRow,
			  optimizerEstimatedRowCount,
			  optimizerEstimatedCost);

		order = (ColumnOrdering[])
					((FormatableArrayHolder)
						(a.getSavedObject(orderingItem)))
					.getArray(ColumnOrdering.class);

		this.maxRowSize = maxRowSize;

		recordConstructorTime();
    }


	///////////////////////////////////////////////////////////////////////////////
	//
	// ResultSet interface (leftover from NoPutResultSet)
	//
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Open the scan.  Load the sorter and prepare to get
	 * rows from it.
	 *
	 * @exception StandardException thrown if cursor finished.
     */
	public void	openCore() throws StandardException 
	{
		beginTime = statisticsTimingOn ? XPLAINUtil.nanoTime() : 0;
		// REVISIT: through the direct DB API, this needs to be an
		// error, not an ASSERT; users can open twice. Only through JDBC
		// is access to open controlled and ensured valid.
		if (SanityManager.DEBUG)
	    	SanityManager.ASSERT( ! isOpen, "DistinctScalarResultSet already open");

		isOpen = true;
		sortResultRow = getExecutionFactory().getIndexableRow(sortTemplateRow.getClone());
		sourceExecIndexRow = getExecutionFactory().getIndexableRow(sortTemplateRow.getClone());

        source.openCore();

		/*
		** Load up the sorter because we have something to sort.
		*/
		scanController = loadSorter();
		sorted = true;

	    //isOpen = true;
		numOpens++;

		if (statisticsTimingOn) openTime += getElapsedNanos(beginTime);
	}

	@Override
	public final void setMaxSortingLimit(long limit) {
	  this.maxSortLimit = limit;
	}

	/* RESOLVE - THIS NEXT METHOD IS ONLY INCLUDED BECAUSE OF A JIT ERROR. THERE IS NO OTHER
	 * REASON TO OVERRIDE IT IN DistinctScalarAggregateResultSet.  THE BUG WAS FOUND IN
	 * 1.1.6 WITH THE JIT.
	 */
	/**
	 * Return the next row.  If it is a scalar aggregate scan
	 *
	 * @exception StandardException thrown on failure.
	 * @exception StandardException ResultSetNotOpen thrown if not yet open.
	 *
	 * @return the next row in the result
	 */
	public ExecRow	getNextRowCore() throws StandardException 
 {
    ExecIndexRow execIndexRow = null;
    ExecIndexRow aggResult = null;
    boolean cloneArg = true;

    beginTime = statisticsTimingOn ? XPLAINUtil.nanoTime() : 0;
    if (isOpen) {
      /*
       * * We are dealing with a scalar aggregate.* Zip through each row and
       * accumulate.* Accumulate into the first row. Only* the first row is
       * cloned.
       */
      while ((execIndexRow = getRowFromResultSet(cloneArg)) != null) {
        /*
         * * Use a clone of the first row as our result.* We need to get a clone
         * since we will be reusing* the original as the wrapper of the source
         * row.* Turn cloning off since we wont be keeping any* other rows.
         */
        if (aggResult == null) {
          cloneArg = false;
          aggResult = (ExecIndexRow) execIndexRow.getClone();
        } else {
          /*
           * * Accumulate all aggregates. For the distinct* aggregates, we'll be
           * accumulating, for the nondistinct* we'll be merging.
           */
          accumulateScalarAggregation(execIndexRow, aggResult, true);
        }
      }

      /*
       * * If we have aggregates, we need to generate a* value for them now.
       * Only finish the aggregation* if we haven't yet (i.e. if countOfRows ==
       * 0).* If there weren't any input rows, we'll allocate* one here.
       */
      if (countOfRows == 0) {
        aggResult = (ExecIndexRow) finishAggregation(aggResult);
        setCurrentRow(aggResult);
        countOfRows++;
      }
    }

    if (statisticsTimingOn)
      nextTime += getElapsedNanos(beginTime);
    return aggResult;
  }

	/**
	 * reopen a scan on the table. scan parameters are evaluated
	 * at each open, so there is probably some way of altering
	 * their values...
	 *
	 * @exception StandardException thrown if cursor finished.
	 */
	public void	reopenCore() throws StandardException 
	{
		beginTime = statisticsTimingOn ? XPLAINUtil.nanoTime() : 0;
		if (SanityManager.DEBUG)
	    	SanityManager.ASSERT(isOpen, "NormalizeResultSet already open");

		if (scanController != null)
		{
			scanController.close();
			scanController = null;
		}

		source.reopenCore();

		/*
		** Load up the sorter because we have something to sort.
		*/
		scanController = loadSorter();
		sorted = true;
		numOpens++;
        countOfRows = 0;

		if (statisticsTimingOn) openTime += getElapsedNanos(beginTime);
	}

        /**
         * If the result set has been opened,
		 * close the open scan.
         */
        public void close(boolean cleanupOnError) throws StandardException
        {
            super.close(cleanupOnError);
            closeSource(cleanupOnError);
        }
	///////////////////////////////////////////////////////////////////////////////
	//
	// SCAN ABSTRACTION UTILITIES
	//
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Get a row from the sorter.  Side effects:
	 * sets currentRow.
	 *
	 * @exception StandardException Thrown on error
	 */
	public ExecIndexRow getRowFromResultSet(boolean doClone)
		throws StandardException
	{
		ExecIndexRow			inputRow = null;	
		
		if (scanController.next())
		{
			// REMIND: HACKALERT we are assuming that result will
			// point to what sortResult is manipulating when
			// we complete the fetch.
			currentRow = doClone ? 
				sortResultRow.getClone() : sortResultRow;

			inputRow = getExecutionFactory().getIndexableRow(currentRow);

			scanController.fetch(inputRow /* .getRowArray() GemStone change */);
		}
		return inputRow;
	}

	/**
	 * Close the source of whatever we have been scanning.
	 *
	 * @exception StandardException thrown on error
	 */
	protected void	closeSource(final boolean cleanupOnError) throws StandardException
	{
		if (scanController != null)
		{
			
			scanController.close();
			if (dropDistinctAggSort)
      {
        try
        {
          getTransactionController().dropSort(sortId);
        }
        catch (StandardException se)
        {
          // Eat all errors at close() time
        }
        dropDistinctAggSort = false;
      }
			scanController = null;
		}
		source.close(cleanupOnError);
	}

	///////////////////////////////////////////////////////////////////////////////
	//
	// MISC UTILITIES
	//
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Load up the sorter.  Feed it every row from the
	 * source scan.  If we have a vector aggregate, initialize
	 * the aggregator for each source row.  When done, close
	 * the source scan and open the sort.  Return the sort
	 * scan controller.
	 *
	 * @exception StandardException thrown on failure.
	 *
	 * @return	the sort controller
 	 */
	private ScanController loadSorter()
		throws StandardException
	{
		SortController 			sorter;
		ExecRow 				sourceRow;
		int						inputRowCountEstimate = (int) optimizerEstimatedRowCount;

		TransactionController tc = getTransactionController();

		/*
		** We have a distinct aggregate so, we'll need
		** to do a sort.  We use all of the sorting columns and
		** drop the aggregation on the distinct column.  Then
		** we'll feed this into the sorter again w/o the distinct
		** column in the ordering list.
		*/
		GenericAggregator[] aggsNoDistinct = getSortAggregators(aggInfoList, true,
				activation.getLanguageConnectionContext(), source);
		SortObserver sortObserver = new AggregateSortObserver(true, aggsNoDistinct, aggregates,
															  sortTemplateRow);

		sortId = tc.createSort((Properties)null, 
					sortTemplateRow,
					order,
					sortObserver,
					false,			// not in order
					inputRowCountEstimate,				// est rows, -1 means no idea	
					maxRowSize,		// est rowsize
					maxSortLimit // number of rows to fetch
					);
		sorter = tc.openSort(sortId);
		dropDistinctAggSort = true;
		while ((sourceRow = source.getNextRowCore())!=null) 
		{
		  boolean rowInserted = false;		 
			rowInserted = sorter.insert(sourceRow);
			rowsInput++;
			if(GemFireXDUtils.isOffHeapEnabled()) {
			  source.releasePreviousByteSource();
			}
		  
		}

		/*
		** End the sort and open up the result set
		*/
		sorter.completedInserts();

		scanController = 
            tc.openSortScan(sortId, activation.getResultSetHoldability());
			
		/*
		** Aggs are initialized and input rows
		** are in order.
		*/	
		inputRowCountEstimate = rowsInput;
	
		return scanController;
	}

}
