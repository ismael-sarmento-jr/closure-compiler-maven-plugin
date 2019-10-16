package org.mobilizadores.ccmp;

import java.io.OutputStream;
import java.io.PrintStream;

public class InternalStream extends PrintStream {
    
    private StringBuilder report = new StringBuilder();

    public InternalStream(OutputStream out) {
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

    public void report() {
      System.err.println(this.report.toString());
    }
  }