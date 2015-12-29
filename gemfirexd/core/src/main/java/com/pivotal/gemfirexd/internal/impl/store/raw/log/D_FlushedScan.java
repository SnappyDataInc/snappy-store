/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.store.raw.log.D_FlushedScan

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
import com.pivotal.gemfirexd.internal.iapi.services.diag.DiagnosticUtil;
import com.pivotal.gemfirexd.internal.iapi.services.diag.Diagnosticable;
import com.pivotal.gemfirexd.internal.iapi.services.diag.DiagnosticableGeneric;
import com.pivotal.gemfirexd.internal.impl.store.raw.log.LogCounter;

public class D_FlushedScan
extends DiagnosticableGeneric
{
	/**
	  @exception StandardException Oops.
	  @see Diagnosticable#diag
	  */
    public String diag()
 		 throws StandardException
    {
		FlushedScan fs = (FlushedScan)diag_object;
		StringBuilder r = new StringBuilder();
		r.append("FlushedScan: \n");
		r.append("    Open: "+fs.open+"\n");
		r.append("    currentLogFileNumber: "+fs.currentLogFileNumber+"\n");
		r.append("    currentLogFirstUnflushedPosition: "+
				 fs.currentLogFileFirstUnflushedPosition+"\n");
		r.append("    currentInstant: "+fs.currentInstant+"\n");
		r.append("    firstUnflushed: "+fs.firstUnflushed+"\n");
		r.append("    firstUnflushedFileNumber: "+fs.firstUnflushedFileNumber+"\n");
		r.append("    firstUnflushedFilePosition: "+fs.firstUnflushedFilePosition+"\n");
		r.append("    logFactory: \n"+
				 DiagnosticUtil.toDiagString(fs.logFactory));
		r.append("flushedScanEnd\n");
		return r.toString();
	}
}
