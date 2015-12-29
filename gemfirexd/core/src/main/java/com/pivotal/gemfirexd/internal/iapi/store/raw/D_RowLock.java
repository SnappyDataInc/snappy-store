/*

   Derby - Class com.pivotal.gemfirexd.internal.iapi.store.raw.D_RowLock

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

package com.pivotal.gemfirexd.internal.iapi.store.raw;


import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.services.diag.DiagnosticUtil;
import com.pivotal.gemfirexd.internal.iapi.services.diag.Diagnosticable;
import com.pivotal.gemfirexd.internal.iapi.services.diag.DiagnosticableGeneric;

/**

The D_RowLock class provides diagnostic information about the 
RowLock qualifer, and is used for output in lock debugging.

**/

public class D_RowLock extends DiagnosticableGeneric
{
    // Names of locks for lock trace print out.
	private static String[] names = { "RS2", "RS3", "RU2", "RU3", "RIP", "RI", "RX2", "RX3" };

    /**
		Return the string for the qualifier.
     *
     * @exception StandardException	Standard Derby error policy
     **/
    public String diag()
        throws StandardException
    {
        RowLock mode = (RowLock) diag_object;

        return(names[mode.getType()]);
    }
}
