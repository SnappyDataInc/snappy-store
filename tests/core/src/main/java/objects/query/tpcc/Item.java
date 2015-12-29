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
package objects.query.tpcc;

import java.io.Serializable;

public class Item implements Serializable {

  public int   i_id; // PRIMARY KEY
  public int   i_im_id;
  public float i_price; 
  public String i_name; 
  public String i_data; 

  public String toString()
  {
    return (
      "\n***************** Item ********************" +
      "\n*    i_id = " + i_id +
      "\n*  i_name = " + i_name +
      "\n* i_price = " + i_price +
      "\n*  i_data = " + i_data +
      "\n* i_im_id = " + i_im_id +
      "\n**********************************************"
      );
  }
}
