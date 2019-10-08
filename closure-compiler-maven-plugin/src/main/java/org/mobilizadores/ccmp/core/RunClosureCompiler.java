package org.mobilizadores.ccmp.core;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.google.javascript.jscomp.CommandLineRunner;

class RunClosureCompiler implements Runnable {
    
    String[] args;

    public RunClosureCompiler(String[] args) {
      super();
      this.args = args;
    }

    @Override
    public void run() {
      try {
        MethodUtils.invokeMethod(getCommandLineRunnerNewInstance(args), true, "run");
      } catch (NoSuchMethodException | IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        if(e.getCause().getClass().isAssignableFrom(SecurityException.class)) {
          System.err.println("yay");
        } else {
          //
        }
      }
      finally {
        //enableSystemExit();
      }
    }
    
    public CommandLineRunner getCommandLineRunnerNewInstance(String[] args) {
      CommandLineRunner clr = null;
      try {
        Constructor<CommandLineRunner> constructor = CommandLineRunner.class
            .getDeclaredConstructor(String[].class, PrintStream.class, PrintStream.class);
        constructor.setAccessible(true);
        clr = constructor.newInstance(args, System.out, System.err);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException e) {
        e.printStackTrace();
      }
      return clr;
    }
 }