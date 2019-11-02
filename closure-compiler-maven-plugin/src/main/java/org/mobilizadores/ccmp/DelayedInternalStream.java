package org.mobilizadores.ccmp;
/*
 * Licensed to Mobilizadores.org under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Mobilizadores licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 * Complete information can be found at: https://dev.mobilizadores.com/licenses
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
 */
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Keeps the stream of characters in a variable called report, instead of printing right away,
 * when the methods for printing are called. The "accumulated" stream is printed when the method
 * <i>report</i> is called.
 */
public class DelayedInternalStream extends PrintStream {
    
    private StringBuilder report = new StringBuilder();

    public DelayedInternalStream(OutputStream out) {
      super(out);
    }

    @Override
    public void print(char[] s) {
      this.report.append(s);
    }

    @Override
    public void print(String s) {
      this.report.append(s);
    }

    @Override
    public void println(char[] x) {
      this.report.append(x);
    }

    @Override
    public void println(String x) {
      this.report.append(x);
    }

    /**
     * Uses the default system <b>error</b> output stream for errors to print the characters appended
     * to {@link DelayedInternalStream#report}
     */
    public void report() {
      System.err.println(this.report.toString());
    }
  }