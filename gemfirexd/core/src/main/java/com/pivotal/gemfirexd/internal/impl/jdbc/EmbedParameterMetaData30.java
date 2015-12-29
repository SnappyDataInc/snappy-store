/*

   Derby - Class com.pivotal.gemfirexd.internal.impl.jdbc.EmbedParameterMetaData30

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

package com.pivotal.gemfirexd.internal.impl.jdbc;

import com.pivotal.gemfirexd.internal.iapi.sql.ParameterValueSet;
import com.pivotal.gemfirexd.internal.iapi.types.DataTypeDescriptor;

import java.sql.ParameterMetaData;

/**
 * This class implements the ParameterMetaData interface from JDBC3.0
 * It provides the parameter meta data for callable & prepared statements
 * But note that the bulk of it resides in its parent class.  The reason is
 * we want to provide the functionality to the JDKs before JDBC3.0.
 *
  <P><B>Supports</B>
   <UL>
   <LI> JDBC 3.0 - java.sql.ParameterMetaData introduced in JDBC3
   </UL>

 * @see java.sql.ParameterMetaData
 *
 */
class EmbedParameterMetaData30 extends com.pivotal.gemfirexd.internal.impl.jdbc.EmbedParameterSetMetaData
    implements ParameterMetaData {

	//////////////////////////////////////////////////////////////
	//
	// CONSTRUCTORS
	//
	//////////////////////////////////////////////////////////////
    EmbedParameterMetaData30(ParameterValueSet pvs, DataTypeDescriptor[] types)  {
		super(pvs, types);
    }

}

