package org.mobilizadores.ccmp.core;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import com.google.javascript.jscomp.CommandLineRunner;

public class CommandInfo {

  private String[] args;

  public CommandInfo(String[] args) {
    super();
    this.args = args;
  }

  public CommandInfo() {}

  public String[] getArgs() {
    return args;
  }

  public void setArgs(String[] args) {
    this.args = args;
  }
  
  public CommandLineRunner getCommandLineRunnerNewInstance() {
    CommandLineRunner clr = null;
    try {
      Constructor<CommandLineRunner> constructor = CommandLineRunner.class
          .getDeclaredConstructor(String[].class, PrintStream.class, PrintStream.class);
      constructor.setAccessible(true);
      clr = constructor.newInstance(this.args, System.out, System.err);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | NoSuchMethodException | InvocationTargetException e) {
      e.printStackTrace();
    } 
    return clr;
  }
}
