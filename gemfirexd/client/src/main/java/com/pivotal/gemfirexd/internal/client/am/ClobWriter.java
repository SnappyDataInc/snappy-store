/*

   Derby - Class com.pivotal.gemfirexd.internal.client.am.ClobWriter

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

package com.pivotal.gemfirexd.internal.client.am;

import com.pivotal.gemfirexd.internal.shared.common.reference.SQLState;

public class ClobWriter extends java.io.Writer {
    private Clob clob_;
    private long offset_;

    public ClobWriter() {
    }

    public ClobWriter(Clob clob, long offset) throws SqlException {
        clob_ = clob;
        offset_ = offset;

        if (offset_ - 1 > clob_.sqlLength()) {
            throw new SqlException(clob_.agent_.logWriter_, 
                new ClientMessageId(SQLState.BLOB_INVALID_OFFSET),
                new Long(offset));
        }
    }

    public void write(int c) {
        StringBuilder sb = new StringBuilder(clob_.string_.substring(0, (int) offset_ - 1));
        sb.append((char)c);
        updateClob(sb);
    }

    public void write(char cbuf[], int off, int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder(clob_.string_.substring(0, (int) offset_ - 1));
        sb.append(cbuf, off, len);
        updateClob(sb);
    }


    public void write(String str) {
        StringBuilder sb = new StringBuilder(clob_.string_.substring(0, (int) offset_ - 1));
        sb.append(str);
        updateClob(sb);
    }


    public void write(String str, int off, int len) {
        StringBuilder sb = new StringBuilder(clob_.string_.substring(0, (int) offset_ - 1));
        sb.append(str.substring(off, off + len));
        updateClob(sb);
    }

    public void flush() {
    }

    public void close() throws java.io.IOException {
    }
    
    private void updateClob(StringBuilder sb) 
    {
        clob_.string_ = sb.toString();
        clob_.asciiStream_ = new java.io.StringBufferInputStream(clob_.string_);
        clob_.unicodeStream_ 
            = new java.io.StringBufferInputStream(clob_.string_);
        clob_.characterStream_ = new java.io.StringReader(clob_.string_);
        clob_.setSqlLength(clob_.string_.length());
        offset_ = clob_.string_.length() + 1;
    }
}

