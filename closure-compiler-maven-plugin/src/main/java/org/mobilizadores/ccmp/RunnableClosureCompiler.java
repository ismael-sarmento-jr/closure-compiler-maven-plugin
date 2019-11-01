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
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ConcurrentModificationException;
import java.util.Observable;
import java.util.concurrent.locks.Lock;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.google.javascript.jscomp.CommandLineRunner;

/**
 * Runs the compiler according to the arguments passed and notify the observers on the results
 * of the run.
 */
public class RunnableClosureCompiler extends Observable implements Runnable {
  
  public static final Integer SUCCESS = 0;
  public static final Integer ERROR = 1;

  private Lock lock;
  private String[] args;
  private PrintStream stream;

  public RunnableClosureCompiler(String[] args, Lock lock, PrintStream stream) {
    super();
    this.lock = lock;
    this.args = args;
    this.stream = stream;
  }

  /**
   * Invokes the method <i>run</i> in the command line runner instance (clr). 
   * The list of'mixedJsSources' in {@link CommandLineRunner#Flags} is static, set during a new <i>clr</i>
   * instantiation and read during configuration initiation, therefore the instantiation of <i>clr</i> 
   * and subsequent access to the list are synchronized.
   * The results of the tasks are reported to the observers, so they can be logged out.
   */
  @Override
    public void run() {
      try {
        CommandLineRunner clr = null;
        lock.lock();
        clr = getCommandLineRunnerNewInstance();
        if(clr != null) {            
          setChanged();
          notifyObservers(new Notification(SUCCESS, "Compressing files", this.args));          
          lock.unlock();
          runClosureCompiler(clr);
        } 
      } catch (NoSuchMethodException | IllegalAccessException e) {
        setChanged();
        notifyObservers(new Notification(ERROR, "Couldn't invoke method 'run' on CommandLineRunner instance: " + e.getMessage(), this.args));
      } catch (InvocationTargetException e) {
        setChanged();
        if(e.getCause().getClass().isAssignableFrom(SystemExitNotAllowedException.class)) {
          if(((SystemExitNotAllowedException) e.getCause()).getStatus() == SUCCESS){
            notifyObservers(new Notification(SUCCESS, "Files compressed", this.args));
          } else {
            notifyObservers(new Notification(ERROR, "Error reported by the compressor trying to compress ", this.args));
          }
        } else if (e.getCause().getClass().isAssignableFrom(ConcurrentModificationException.class)) {
          notifyObservers(new Notification(ERROR, "Concurrent modification error trying to compress ", this.args));
        } else {
          notifyObservers(new Notification(ERROR, "Error in the plugin, trying to compress ", this.args));
        }
      }
    }

  public void runClosureCompiler(CommandLineRunner clr)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    MethodUtils.invokeMethod(clr, true, "run");
  }


  /**
   * Uses reflection to get the proper constructor and then gets a new instance of 
   * {@link CommandLineRunner} with the arguments and input and output streams.
   * @throws InvocationTargetException if any other exception, not related with reflection calls,
   *                                    is thrown during CommandLineRunner instantiation 
   */
  public CommandLineRunner getCommandLineRunnerNewInstance() throws InvocationTargetException {
    CommandLineRunner clr = null;
    try {
      Constructor<CommandLineRunner> constructor = CommandLineRunner.class
          .getDeclaredConstructor(String[].class, PrintStream.class, PrintStream.class);
      constructor.setAccessible(true);
      clr = constructor.newInstance(this.args, this.stream, this.stream);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | NoSuchMethodException e) {
      notifyObservers(new Notification(ERROR, "Couldn't instantiate CommandLineRunner: " + e.getMessage(), this.args));
    } 
    return clr;
  }
}
