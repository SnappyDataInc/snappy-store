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
package flashcache;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import static com.gemstone.gemfire.cache.RegionShortcut.*;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/** 
 * This class is an interactive command-line application that looks up
 * NASDAQ stock symbols and displays current quote information.  A
 * cache loader, QuoteLoader, is used to read quotes from
 * quotes.nasdaq.com and cache them in the distributed GemFire cache
 * for quicker access.
 *
 * <p>
 *
 * Quotes are cached for 20 seconds.  At that time the cached data is
 * invalidated by GemFire and, if the symbol is queried again, the
 * QuoteLoader will be invoked to read an updated quote from NASDAQ.
 *
 * <p>
 *    
 * The program is run like this, assuming that you have followed the <A
 * href="../../.././EnvSetup.html">instructions</a> for setting up your
 * environment.
 *
 * <pre>
 * $ java flashcache.Quote 
 * Enter symbol: ORCL
 * (QuoteLoader netSearching for ORCL)
 * (QuoteLoader querying nasdaq for ORCL)
 * ORCL: last sale=9.85  net change=-0.34  volume=26,526,419
 * Enter next symbol: AMZN
 * (QuoteLoader netSearching for AMZN)
 * (QuoteLoader querying nasdaq for AMZN)
 * AMZN: last sale=36.37  net change=-0.19  volume=5,668,938
 * Enter next symbol: 
 *
 * </pre>
 *
 * The main prompts for a NASDAQ stock symbol.  Enter a symbol, such
 * as SUNW, to request a quote.  Enter no symbol to exit.  The cached
 * quotes are distributed to other GemFire caches connected to the same 
 * distributed system.  Optimistic distribution is used, so there is no 
 * locking or acknowledgment of updates, and quotes are distributed
 * asynchronously.
 *
 * @author GemStone Systems, Inc.
 *
 * @since 2.0
 */
public class Quote {

  /** The GemFire region that caches quotes obtained from NASDAQ */
  private Region<String, Object> quotes;

public static void main(String args[]) throws Exception {
  for (int i = 0; i < args.length; i++) {
    System.out.println("Ignoring command line argument: " + args[i]);
  }

  Quote instance = new Quote();
  instance.initializeDistSystem(System.getProperty("systemDirectory"));

  BufferedReader reader = 
      new BufferedReader(new InputStreamReader(System.in));
  try {
      System.out.print("Enter symbol: ");
      String symbol = reader.readLine();
      while (symbol != null && symbol.length() != 0) {
	  System.out.println(instance.getQuote(symbol));
	  System.out.print("Enter next symbol: ");
	  symbol = reader.readLine();
      }
    } catch (IOException e) {
	System.out.println(e.getMessage());
    }
  System.exit(0);
}

/** this method returns a cached quote */
public String getQuote(String symbol) throws Exception {
   String anObj = (String)quotes.get(symbol);
   return anObj;
}


/** this method initializes the distributed system */
public void initializeDistSystem(String sysDir) throws Exception {
  
  Properties dsProps = new java.util.Properties();
  dsProps.put("log-file", "quote.log");
  dsProps.put("mcast-port", "0");
  
  // use default properties to connect to the distributed system 
  // and create the cache and region
  Cache cache = new CacheFactory(dsProps).create();
  initializeCache(cache, null);

}

/** this method initializes the cache */
public void initializeCache(Cache cache, CacheListener<String, Object> aListener) throws Exception {

  RegionFactory<String,Object> rf = cache.createRegionFactory(REPLICATE);
  rf.setCacheLoader(new QuoteLoader());
  rf.setEntryTimeToLive(new ExpirationAttributes(20));
  if ( aListener != null ) {
    rf.addCacheListener(aListener);
  }

  //create the region
  quotes = rf.create("NASDAQ Quotes");

}

}
