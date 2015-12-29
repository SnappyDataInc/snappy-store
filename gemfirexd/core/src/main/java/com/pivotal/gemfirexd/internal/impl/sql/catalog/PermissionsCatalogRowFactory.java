/*

   Derby - Class com.pivotal.gemfirexd.internal.iapi.sql.dictionary.PermissionsCatalogRowFactory

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

import com.pivotal.gemfirexd.internal.iapi.error.StandardException;
import com.pivotal.gemfirexd.internal.iapi.reference.Limits;
import com.pivotal.gemfirexd.internal.iapi.services.uuid.UUIDFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.CatalogRowFactory;
import com.pivotal.gemfirexd.internal.iapi.sql.dictionary.PermissionsDescriptor;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecIndexRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecRow;
import com.pivotal.gemfirexd.internal.iapi.sql.execute.ExecutionFactory;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueDescriptor;
import com.pivotal.gemfirexd.internal.iapi.types.DataValueFactory;
import com.pivotal.gemfirexd.internal.iapi.types.RowLocation;
import com.pivotal.gemfirexd.internal.iapi.types.SQLVarchar;
import com.pivotal.gemfirexd.internal.iapi.types.StringDataValue;

abstract class PermissionsCatalogRowFactory extends CatalogRowFactory
{
    PermissionsCatalogRowFactory(UUIDFactory uuidf, ExecutionFactory ef, DataValueFactory dvf)
    {
        super(uuidf,ef,dvf);
    }

    DataValueDescriptor getAuthorizationID( String value)
    {
        return new SQLVarchar(value);
    }

    DataValueDescriptor getNullAuthorizationID()
    {
        return new SQLVarchar();
    }

    /**
     * Extract an internal authorization ID from a row.
     *
     * @param row
     * @param columnPos 1 based
     *
     * @return The internal authorization ID
     */
    String getAuthorizationID( ExecRow row, int columnPos)
        throws StandardException
    {
        return row.getColumn( columnPos).getString();
    }

    /**
     * Build an index key row from a permission descriptor. A key row does not include the RowLocation column.
     *
     * @param indexNumber
     * @param perm a permission descriptor of the appropriate class for this PermissionsCatalogRowFactory class.
     *
     * @exception StandardException standard error policy
     */
    abstract ExecIndexRow buildIndexKeyRow( int indexNumber,
                                                   PermissionsDescriptor perm)
        throws StandardException;

    /**
     * Or a set of permissions in with a row from this catalog table
     *
     * @param row an existing row
     * @param perm a permission descriptor of the appropriate class for this PermissionsCatalogRowFactory class.
     * @param colsChanged An array with one element for each column in row. It is updated to
     *                    indicate which columns in row were changed
     *
     * @return The number of columns that were changed.
     *
     * @exception StandardException standard error policy
     */
    abstract int orPermissions( ExecRow row, PermissionsDescriptor perm, boolean[] colsChanged)
        throws StandardException;

    /**
     * Remove a set of permissions from a row from this catalog table
     *
     * @param row an existing row
     * @param perm a permission descriptor of the appropriate class for this PermissionsCatalogRowFactory class.
     * @param colsChanged An array with one element for each column in row. It is updated to
     *                    indicate which columns in row were changed
     *
     * @return -1 if there are no permissions left in the row, otherwise the number of columns that were changed.
     *
     * @exception StandardException standard error policy
     */
    abstract int removePermissions( ExecRow row, PermissionsDescriptor perm, boolean[] colsChanged)
        throws StandardException;

    /**
     * Set the uuid of the passed permission descriptor to the uuid of the row
     * from the system table. DataDictionary will make this call before calling 
     * the dependency manager to send invalidation messages to the objects 
     * dependent on the permission descriptor's uuid.
     * 
     * @param row The row from the system table for the passed permission descriptor
     * @param perm Permission descriptor
     * @throws StandardException
     */
    abstract void setUUIDOfThePassedDescriptor(ExecRow row, PermissionsDescriptor perm) throws StandardException;
// GemStone changes BEGIN

    /**
     * Return the column number where LDAP group is stored, or -1 if no such
     * column is present.
     */
    public int getLdapGroupColumnNumber() {
      return -1;
    }
// GemStone changes END
}
