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

package com.gemstone.gemfire.i18n;

import com.gemstone.gemfire.internal.i18n.AbstractStringIdResourceBundle;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * This class forms the basis of the i18n strategy. Its primary function is to
 * be used as a key to be passed to an instance of StringIdResourceBundle.
 * @author kbanks
 * @since 6.0 
 */
public class StringIdImpl implements StringId {
  /** The root name of the ResourceBundle */
  private static final String RESOURCE_CLASS = "com/gemstone/gemfire/internal/i18n/StringIdResourceBundle";
  
  /** A unique identifier that is written when this StringId is logged to 
   * allow for reverse translation.
   * @see com.gemstone.gemfire.internal.logging.LogWriterImpl
   */
  public final int id;
  /** the English translation of text */
  private final String text;
  /** ResourceBundle to use for translation, shared amongst all instances */
  private static volatile AbstractStringIdResourceBundle rb = null;
  /** The locale of the current ResourceBundle, 
   * if this changes we must update the ResourceBundle.
   */
  private static volatile Locale currentLocale = null;
  
  private static boolean includeMsgIDs;
  
  /** A StringId to allow users to log a literal String using the {@link com.gemstone.gemfire.i18n.LogWriterI18n} */
  public static final StringId LITERAL = new StringIdImpl(1, "{0}"); 
  
  static {
    setLocale(Locale.getDefault());
  }

  /*
   * Update {@link #currentlocale} and {@link #rb}
   * This method should be used sparingly as there is 
   * a small window for a race condition.
   * @params locale switch to use this locale. if null 
   *                then {@link Locale#getDefault()} is used.
   */
  public static void setLocale(Locale l) {
    Locale locale = l;
    if ( locale == null ) {
      locale = Locale.getDefault();
    }
    
    if ( locale != currentLocale ) {
      AbstractStringIdResourceBundle tempResourceBundle = 
        StringIdImpl.getBundle(locale);
      currentLocale = locale;      
      rb = tempResourceBundle;
      // do we want message ids included in output?
      // Only if we are using a resource bundle that has localized strings.
      includeMsgIDs = !rb.usingRawMode();
    }
  }
  
  /*
   * @return AbstractStringIdResourceBundle for the locale 
   */
  private static AbstractStringIdResourceBundle getBundle(Locale l) {
    return AbstractStringIdResourceBundle.getBundle(RESOURCE_CLASS, l);
  }
  
  /** 
   * Gemstone internal constructor, customers have no need to  
   * create instances of this class.
   */  
  public StringIdImpl(int id, String text) {
    this.id = id;
    this.text = text;
  }
  
  /**
   * Accessor for the raw (unformatted) text of this StringId
   * @return unformated text
   **/ 
  public String getRawText() {
        return this.text;
  }  
  
  /**
   * @return the English translation of this StringId
   **/ 
  @Override
  public String toString() {
    return MessageFormat.format(this.text, (Object[])null);
  }


  /**
   * Substitutes parameter Objects into the text
   * @see java.text.MessageFormat
   * @return the English translation of this StringId
   **/ 
  public String toString(Object ... params) {
  return MessageFormat.format(this.text, params);
  }

  /**
   * @return the translation of this StringId based on the current {@link java.util.Locale}
   **/ 
  public String toLocalizedString() {
    String idStr = "";
    if (includeMsgIDs) {
      idStr = "msgID "+this.id+": ";
    }
    return MessageFormat.format(idStr + StringIdImpl.rb.getString(this), (Object[])null);
  }
  
  /**
   * Substitutes parameter Objects into the text
   * @see java.text.MessageFormat
   * @return the translation of this StringId based on the current {@link java.util.Locale}
   **/ 
  public String toLocalizedString(Object ... params) {
    String idStr = "";
    if (includeMsgIDs) {
      idStr = "msgID "+this.id+": ";
    }
    return MessageFormat.format(idStr + StringIdImpl.rb.getString(this), params);
  }
  
  /**
   * Gemstone internal test method to access {@link #currentLocale}
   */
  static Locale getCurrentLocale() {
    return currentLocale;
  }
  
  /**
   * Gemstone internal test method to access {@link #rb}
   */
  static AbstractStringIdResourceBundle getActiveResourceBundle() {
    return rb;
  }
}
