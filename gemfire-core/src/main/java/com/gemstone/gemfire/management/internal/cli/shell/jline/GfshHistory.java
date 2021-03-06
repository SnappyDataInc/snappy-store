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
package com.gemstone.gemfire.management.internal.cli.shell.jline;

import com.gemstone.gemfire.management.internal.cli.parser.preprocessor.PreprocessorUtils;

import jline.History;

/**
 * Overrides jline.History to add History without newline characters.
 * 
 * @author Abhishek Chaudhari
 * @since 7.0
 */
public class GfshHistory extends History {
  // let the history from history file get added initially
  private boolean autoFlush = true;
  
  @Override
  public void addToHistory(String buffer) {
    if (isAutoFlush()) {
      super.addToHistory(toHistoryLoggable(buffer));
    }
  }

  public boolean isAutoFlush() {
    return autoFlush;
  }

  public void setAutoFlush(boolean autoFlush) {
    this.autoFlush = autoFlush;
  }
  
  public static String toHistoryLoggable(String buffer) {
    return PreprocessorUtils.trim(buffer, false).getString();
  }
}
