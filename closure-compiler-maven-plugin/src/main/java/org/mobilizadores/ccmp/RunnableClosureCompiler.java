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
import java.util.Observable;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.google.javascript.jscomp.CommandLineRunner;

class RunnableClosureCompiler extends Observable implements Runnable {

  public static final Integer SUCCESS = 0;

  private Object lock;
  private String[] args;
  private PrintStream stream;

  public RunnableClosureCompiler(String[] args, Object lock, PrintStream stream) {
    super();
    this.lock = lock;
    this.args = args;
    this.stream = stream;
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
      clr = constructor.newInstance(this.args, this.stream, this.stream);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | NoSuchMethodException | InvocationTargetException e) {
      e.printStackTrace();
    } 
    return clr;
  }
}
