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
package com.gemstone.gemfire.management.internal.cli.util.spring;

/**
 * Replaces org.springframework.shell.support.util.Assert which is
 * now removed from SPring Shell & the same class is referred from Spring Core
 * to avoid GemFire code dependency on Spring Core.
 * Internally uses ({@link com.gemstone.gemfire.internal.Assert}
 */
public class Assert {

  public static void isTrue(boolean b, String message) {
    com.gemstone.gemfire.internal.Assert.assertTrue(b, message);
  }

  public static void notNull(Object object, String message) {
    com.gemstone.gemfire.internal.Assert.assertTrue(object != null, message);
  }

}
