/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
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
package objects.query.broker;

import java.util.ArrayList;
import java.util.List;

import objects.query.QueryPrms;

public class GFXDBrokerQueryFactory extends SQLBrokerQueryFactory {


  public List getTableStatements() {
    List stmts = new ArrayList();
    int dataPolicy =  BrokerPrms.getBrokerDataPolicy();
    if (dataPolicy == QueryPrms.REPLICATE) {
      stmts.add("create table " + Broker.getTableName() +
          " (id int not null, name varchar(100))");
    }
    else if (dataPolicy == QueryPrms.PARTITION) {
      stmts.add("create table " + Broker.getTableName() +
          " (id int not null, name varchar(100))");
      //get partitioning clause from broker prms
    }
    else {
      stmts.add("create table " + Broker.getTableName() +
          " (id int not null, name varchar(100))");
    }
    stmts.addAll(brokerTicketQueryFactory.getTableStatements());
    return stmts;
  }
}
