/*

   Derby - Class com.pivotal.gemfirexd.internal.catalog.Dependable

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

/*
 * Changes for GemFireXD distributed data platform (some marked by "GemStone changes")
 *
 * Portions Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
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

package com.pivotal.gemfirexd.internal.catalog;

/**
	
  * A Dependable is an in-memory representation of an object managed
  *	by the Dependency System.
  * 
  * There are two kinds of Dependables:
  * Providers and Dependents. Dependents depend on Providers and
  *	are responsible for executing compensating logic when their
  *	Providers change.
  * <P>
  * The fields represent the known Dependables.
  * <P>
  * Persistent dependencies (those between database objects) are
  * stored in SYS.SYSDEPENDS.
  *
  * @see com.pivotal.gemfirexd.internal.catalog.DependableFinder
  */
public interface Dependable
{
	/*
	  *	Universe of known Dependables. 
	  */

	public static final String ALIAS						= "Alias";
	public static final String CONGLOMERATE					= "Conglomerate";
	public static final String CONSTRAINT					= "Constraint";
	public static final String DEFAULT						= "Default";
	public static final String HEAP							= "Heap";
	public static final String INDEX						= "Index";
	public static final String PREPARED_STATEMENT 			= "PreparedStatement";
	public static final String FILE                         = "File";
	public static final String STORED_PREPARED_STATEMENT	= "StoredPreparedStatement";
	public static final String TABLE						= "Table";
	public static final String COLUMNS_IN_TABLE				= "ColumnsInTable";
	public static final String TRIGGER						= "Trigger";
	public static final String VIEW							= "View";
	public static final String SCHEMA						= "Schema";
	public static final String TABLE_PERMISSION             = "TablePrivilege";
	public static final String COLUMNS_PERMISSION           = "ColumnsPrivilege";
	public static final String ROUTINE_PERMISSION           = "RoutinePrivilege";
// Gemstone changes BEGIN
        public static final String TRIGGER_ACTION_MAP = "TriggerMap";
        public static final String JAR_DROP_REPLACE = "JarDropReplace";
        public static final String ASYNC_LISTENER = "AsyncListener";
// Gemstone changes END

	/**
	  *	Get an object which can be written to disk and which,
	  *	when read from disk, will find or reconstruct this in-memory
	  * Dependable.
	  *
	  *	@return		A Finder object that can be written to disk if this is a
	  *					Persistent Dependable.
	  *				Null if this is not a persistent dependable.
	  */
	public	DependableFinder	getDependableFinder();


	/**
	  *	Get the name of this Dependable OBJECT. This is useful
	  *	for diagnostic messages.
	  *
	  *	@return	Name of Dependable OBJECT.
	  */
	public	String	getObjectName();


	/**
	  *	Get the UUID of this Dependable OBJECT.
	  *
	  *	@return	UUID of this OBJECT.
	  */
	public	UUID	getObjectID();


	/**
	  *	Return whether or not this Dependable is persistent. Persistent
	  *	dependencies are stored in SYS.SYSDEPENDS.
	  *
	  *	@return	true if this Dependable is persistent.
	  */
	public	boolean	isDescriptorPersistent();


	/**
	  * Get the unique class id for the Dependable.
	  *	Every Dependable belongs to a class of Dependables. 
	  *
	  *	@return	type of this Dependable.
	  */
	public	String	getClassType();
}
