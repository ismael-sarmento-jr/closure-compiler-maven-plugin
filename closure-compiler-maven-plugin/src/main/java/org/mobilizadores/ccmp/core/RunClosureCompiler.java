package org.mobilizadores.ccmp.core;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Observable;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.google.javascript.jscomp.CommandLineRunner;

class RunClosureCompiler extends Observable implements Runnable {

  public static final Integer SUCCESS = 0;

  private Object lock;
  private String[] args;

  public RunClosureCompiler(String[] args, Object lock) {
    super();
    this.lock = lock;
    this.args = args;
  }

  @Override
    public void run() {
      try {
        CommandLineRunner clr = null;
        synchronized (this.lock) {
          clr = getCommandLineRunnerNewInstance();
          setChanged();
          notifyObservers(new Notification("Compressing files", this.args));          
        }
        MethodUtils.invokeMethod(clr, true, "run");
      } catch (NoSuchMethodException | IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        if(e.getCause().getClass().isAssignableFrom(SystemExitNotAllowedException.class)) {
          if(((SystemExitNotAllowedException) e.getCause()).getStatus() == SUCCESS){
            setChanged();
            notifyObservers(new Notification("Files compressed", this.args));
          }
        } else {
          e.printStackTrace();
        }
      }
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
