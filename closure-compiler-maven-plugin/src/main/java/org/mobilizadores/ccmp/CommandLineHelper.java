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
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.maven.plugins.annotations.Parameter;
import org.kohsuke.args4j.Option;

/**
 * This class contains utility methods to properly build the command line
 * to be executed by the compressor. It wraps an instance of the maven mojo and 
 * makes heavy use of reflection to set the values to the respective parameters.
 * The terms args and options are used as synonyms.
 */
public class CommandLineHelper {
  
  Logger logger = Logger.getLogger(CommandLineHelper.class.getName());
  
  /**
   * @Option.name in {@link com.google.javascript.jscomp.CommandLineRunner$Flags#jsOutputFile}
   */
  public static final String JS_OUTPUT_FILE_OPTION = "--js_output_file";
  /**
   * @Option.name in {@link com.google.javascript.jscomp.CommandLineRunner$Flags#js}
   */
  private static final String JS_OPTION = "--js";

  /**
   * Instance of the maven mojo with its parameters set
   */
  private ClosureCompilerMojo mojo;
  
  /**
   * The class in the closure compiler that contains the definition of each command line option
   */
  Class<?> optionsClass;
  
  /**
   * The fields in the maven mojo class annotated with {@link Parameter}
   */
  List<String> mojoParameters;

  public CommandLineHelper(ClosureCompilerMojo mojo) {
    this.mojo = mojo;
    this.mojoParameters = Arrays.stream( mojo.getClass().getDeclaredFields())
                              .map(field -> field.getName()) //as Parameter annotation has retentionPolicy=Class, it's not being used as filter here
                              .collect(Collectors.toList());
    try {
      this.optionsClass = Class.forName("com.google.javascript.jscomp.CommandLineRunner$Flags");
    } catch (ClassNotFoundException e) { e.printStackTrace(); }
  }
  

  /**
   * @return the final array of options for the command
   */
  public String[] getCommandLine(String outputFile, File inputDirectory, String... inputFiles) {
    List<String> commandList = new ArrayList<>();
    commandList.addAll(getArgs());
    commandList.addAll(getFilesArgs(outputFile, inputFiles));
    return commandList.toArray(new String[] {});
  }

  /**
   * @param outputFile
   * @param inputFiles
   * @return
   */
  public List<String> getFilesArgs(String outputFile, String... inputFiles) {
    List<String> commandList = new ArrayList<>();
    commandList.add(JS_OUTPUT_FILE_OPTION);
    commandList.add(outputFile);
    Arrays.asList(inputFiles).forEach(file -> {
      commandList.add(JS_OPTION);
      commandList.add(file);
    });
    return commandList;
  }

  /**
   * @return 
   *        the list of primitive and iterable args
   */
  public List<String>  getArgs() {
    List<String> commandList = new ArrayList<String>();
    Field[] options = this.optionsClass.getDeclaredFields();
    Arrays.asList(options).stream().forEach(option -> {
        try {
          if(option.isAnnotationPresent(Option.class)) {
              if( Iterable.class.isAssignableFrom(option.getType())) {
                 Class<?> listTypeClass = Class.forName(((ParameterizedType) option.getGenericType()).getActualTypeArguments()[0].getTypeName());
                 if(ClassUtils.isPrimitiveOrWrapper(listTypeClass) || String.class.isAssignableFrom(listTypeClass)) {                      
                   commandList.addAll(getIterableArgsPairs( option));
                 }
              } else {
                 commandList.addAll(getPrimitiveArgPair( option));
              }
          }
        } catch (ClassNotFoundException e) {}
    });
    return commandList;
  }

  /**
   * 
   * @param option
   *               the arg to be set, as defined in the {@link CommandLineHelper#optionsClass}
   * @return
   *        a list of pairs containing the final option name and a value for each copy of option added
   */
  public List<String> getIterableArgsPairs(Field option) {
    
    final String optionName = option.getDeclaredAnnotation(Option.class).name();
    List<String> commandList = new ArrayList<String>();
    try {
      if(mojoParameters.contains(option.getName())) {
        Field mojoParameter = FieldUtils.getDeclaredField(mojo.getClass(), option.getName(), true);
        if(mojoParameter.get(mojo) != null) {                  
          Consumer consumer = (value) -> {
              commandList.add(optionName);
              commandList.add(value.toString());
          };
          MethodUtils.invokeMethod(mojoParameter.get(mojo), "forEach", consumer);
        }
      }
    } catch (SecurityException | IllegalArgumentException  | IllegalAccessException 
        | NoSuchMethodException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return commandList;
  }


  /**
   * @param option
   *            the arg to be set, as defined in the {@link CommandLineHelper#optionsClass}
   * @return
   *        a pair of strings with the option name followed by it's value
   */
  public List<String> getPrimitiveArgPair(Field option) {
    List<String> commandList = new ArrayList<String>();
    try {
      if(mojoParameters.contains(option.getName())) {
        Field mojoParameter = FieldUtils.getDeclaredField(mojo.getClass(), option.getName(), true);
        if( FieldUtils.readField(mojoParameter, mojo) != null 
            && (ClassUtils.isPrimitiveOrWrapper(mojoParameter.getType()) || mojoParameter.getType().isAssignableFrom(String.class))) {                  
          commandList.add(option.getDeclaredAnnotation(Option.class).name());
          commandList.add(String.valueOf(FieldUtils.readField(mojoParameter, mojo)));
        }
      }
    } catch ( IllegalAccessException e) {logger.severe("Couldn't create one or more of the command line args");}
    return commandList;
  }
}
