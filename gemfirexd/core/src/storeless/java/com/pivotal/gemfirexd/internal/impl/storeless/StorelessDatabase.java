/*

   Derby - Class org.apache.impl.storeless.StorelessDatabase

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package com.pivotal.gemfirexd.internal.impl.storeless;

import java.util.Properties;

import com.pivotal.gemfirexd.internal.catalog.UUID;
import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.reference.EngineType;
import com.pivotal.gemfirexd.internal.iapi.services.context.ContextManager;
import com.pivotal.gemfirexd.internal.iapi.services.monitor.Monitor;
import com.pivotal.gemfirexd.internal.iapi.store.access.TransactionController;
import com.pivotal.gemfirexd.internal.impl.db.BasicDatabase;

/**
 * Database implementation that drives the storeless engine
 * as a Database service with no store.
 *
 */
public class StorelessDatabase extends BasicDatabase {
	
	public StorelessDatabase() {
	}
    
	public	int	getEngineType() {
		return EngineType.STORELESS_ENGINE;
    }

    /**
     * The key, don't boot a store!
     */
	protected void bootStore(boolean create, Properties startParams)
	{
	}
	
	protected void createFinished()
	{
		
	}
	
	protected	UUID	makeDatabaseID(boolean create, Properties startParams)
	{
		return Monitor.getMonitor().getUUIDFactory().createUUID();
	}
	
	protected Properties getAllDatabaseProperties()
	throws StandardException
	{
		return new Properties();
	}
	
	protected TransactionController getConnectionTransaction(ContextManager cm)
            throws StandardException {

        // start a local transaction
        return new NoOpTransaction();
    }
	public boolean isReadOnly()
	{
		return true;
	}

  @Override
  public void cleanupOnError(Throwable e) {
    // TODO Auto-generated method stub
    
  }
}
