/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.store.raw.log.FlushedScanHandle

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

package com.pivotal.gemfirexd.internal.impl.store.raw.log;


import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.reference.SQLState;
import com.pivotal.gemfirexd.internal.iapi.services.io.ArrayInputStream;
import com.pivotal.gemfirexd.internal.iapi.services.sanity.SanityManager;
import com.pivotal.gemfirexd.internal.iapi.store.access.DatabaseInstant;
import com.pivotal.gemfirexd.internal.iapi.store.raw.Loggable;
import com.pivotal.gemfirexd.internal.iapi.store.raw.ScanHandle;
import com.pivotal.gemfirexd.internal.iapi.store.raw.ScannedTransactionHandle;
import com.pivotal.gemfirexd.internal.iapi.store.raw.log.LogFactory;
import com.pivotal.gemfirexd.internal.iapi.store.raw.log.LogInstant;
import com.pivotal.gemfirexd.internal.iapi.store.raw.xact.TransactionId;
import com.pivotal.gemfirexd.internal.impl.store.raw.log.LogCounter;
import com.pivotal.gemfirexd.internal.impl.store.raw.log.LogRecord;
import com.pivotal.gemfirexd.internal.impl.store.raw.log.StreamLogScan;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class FlushedScanHandle implements ScanHandle
{
	LogFactory lf;
	StreamLogScan fs;
	
	LogRecord lr = null;
	boolean readOptionalData = false;
	int groupsIWant;
	
	ArrayInputStream rawInput = new ArrayInputStream(new byte[4096]);
	
	FlushedScanHandle(LogToFile lf, DatabaseInstant start, int groupsIWant)
		 throws StandardException
	{
		this.lf = lf;
		fs = new FlushedScan(lf,((LogCounter)start).getValueAsLong());
		this.groupsIWant = groupsIWant;
	}
	
	public boolean next() throws StandardException
	{
		readOptionalData = false;
		lr = null; 

		// filter the log stream so that only log records that belong to these
		// interesting groups will be returned

		try
		{
			lr = fs.getNextRecord(rawInput,null, groupsIWant);
			if (lr==null) return false; //End of flushed log
			if (SanityManager.DEBUG)
            {
                if ((groupsIWant & lr.group()) == 0)
                    SanityManager.THROWASSERT(groupsIWant + "/" + lr.group());
            }

			return true;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			fs.close();
			fs = null;
			throw lf.markCorrupt(
                    StandardException.newException(SQLState.LOG_IO_ERROR, ioe));
		}
	}

	/**
	  Get the group for the current log record.
	  @exception StandardException Oops
	  */
	public int getGroup() throws StandardException
	{
		return lr.group();
	}

	/**
	  Get the Loggable associated with the currentLogRecord
	  @exception StandardException Oops
	  */
	public Loggable getLoggable() throws StandardException
	{
		try {
			return lr.getLoggable();
		}

		catch (IOException ioe)
		{
			ioe.printStackTrace();
			fs.close();
			fs = null;
			throw lf.markCorrupt(
                    StandardException.newException(SQLState.LOG_IO_ERROR, ioe));
		}

		catch (ClassNotFoundException cnfe)
		{
			fs.close();
			fs = null;
			throw lf.markCorrupt(
                StandardException.newException(SQLState.LOG_CORRUPTED, cnfe));
		}
	}

	//This may be called only once per log record.
    public InputStream getOptionalData()
		 throws StandardException
	{
		if (SanityManager.DEBUG) SanityManager.ASSERT(!readOptionalData);
		if (lr == null) return null;
		try
		{
			int dataLength = rawInput.readInt();
			readOptionalData = true;
			rawInput.setLimit(dataLength);
			return rawInput;
		}

		catch (IOException ioe)
		{
			fs.close();
			fs = null;
			throw lf.markCorrupt(
                    StandardException.newException(SQLState.LOG_IO_ERROR, ioe));
		}
	}

    public DatabaseInstant getInstant()
		 throws StandardException
	{
		return fs.getLogInstant();
	}

	public Object getTransactionId()
		 throws StandardException
	{  
		try
        {
			return lr.getTransactionId();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			fs.close();
			fs = null;
			throw lf.markCorrupt(
                    StandardException.newException(SQLState.LOG_IO_ERROR, ioe));
		}
		catch (ClassNotFoundException cnfe)
		{
			fs.close();
			fs = null;
			throw lf.markCorrupt(
                StandardException.newException(SQLState.LOG_CORRUPTED, cnfe));
		}
	}

    public void close()
	{
		if (fs != null) fs.close();
		fs = null;
	}
}
