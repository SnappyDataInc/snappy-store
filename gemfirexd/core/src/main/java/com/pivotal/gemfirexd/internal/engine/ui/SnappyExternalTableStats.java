/*
 * Copyright (c) 2017-2019 TIBCO Software Inc. All rights reserved.
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

package com.pivotal.gemfirexd.internal.engine.ui;

public class SnappyExternalTableStats {
  private String tableFullyQualifiedName;
  private String tableName;
  private String tableType;
  private Object schema;
  private String provider;
  private String dataSourcePath;
  private String driverClass;
  private Object externalStore;

  public SnappyExternalTableStats(String tableName, String tableType, Object schema,
      String provider, Object externalStore, String dataSourcePath, String driverClass) {
    this.tableName = tableName;
    this.tableType = tableType;
    this.schema = schema;
    this.tableFullyQualifiedName = schema.toString().concat(".").concat(tableName);
    this.provider = provider;
    this.externalStore = externalStore;
    this.dataSourcePath = dataSourcePath;
    this.driverClass = driverClass;
  }

  public String getTableFullyQualifiedName() {
    return tableFullyQualifiedName;
  }

  public String getTableName() {
    return tableName;
  }

  public String getTableType() {
    return tableType;
  }

  public Object getSchema() {
    return schema;
  }

  public String getProvider() {
    return provider;
  }

  public Object getExternalStore() {
    return externalStore;
  }

  public String getDataSourcePath() {
    return dataSourcePath;
  }

  public String getDriverClass() {
    return driverClass;
  }
}
