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
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.kohsuke.args4j.Option;

public class CommandLineHelper {
  
  private ClosureCompilerMojo mojo;

  public CommandLineHelper(ClosureCompilerMojo mojo) {
    this.mojo = mojo;
  }

  public String[] getCommandLine(String outputFile, File inputDirectory, String... inputFiles) {
    List<String> commandList = new ArrayList<>();
    commandList.addAll(getArgs());
    commandList.addAll(getFilesArgs(outputFile, inputFiles));
    return commandList.toArray(new String[] {});
  }

  private List<String> getFilesArgs(String outputFile, String... inputFiles) {
    List<String> commandList = new ArrayList<>();
    commandList.add("--js_output_file");
    commandList.add(outputFile);
    Arrays.asList(inputFiles).forEach(file -> {
      commandList.add("--js");
      commandList.add(file);
    });
    return commandList;
  }

  public List<String>  getArgs() {
    List<String> commandList = new ArrayList<String>();
    List<String> mojoParameters = Arrays.stream( ClosureCompilerMojo.class.getDeclaredFields()).map(field -> field.getName()).collect(Collectors.toList());
    try {
      Class<?> flagsClass = Class.forName("com.google.javascript.jscomp.CommandLineRunner$Flags");
      Field[] options = flagsClass.getDeclaredFields();
      Arrays.asList(options).stream().forEach(option -> {
        try {
          if(option.isAnnotationPresent(Option.class)) {
              if( Iterable.class.isAssignableFrom(option.getType())) {
                 Class<?> listTypeClass = Class.forName(((ParameterizedType) option.getGenericType()).getActualTypeArguments()[0].getTypeName());
                 if(ClassUtils.isPrimitiveOrWrapper(listTypeClass) || String.class.isAssignableFrom(listTypeClass)) {                      
                   commandList.addAll(getIterableArgs( mojoParameters, option));
                 }
              } else {
                 commandList.addAll(getPrimitiveArgs( mojoParameters, option));
              }
          }
        } catch (ClassNotFoundException e) {}
      });
    } catch (ClassNotFoundException e1) {}
    return commandList;
  }


  private List<String> getIterableArgs(List<String> mojoParameters, Field option) {
    List<String> commandList = new ArrayList<String>();
    try {
      if(mojoParameters.contains(option.getName())) {
        Field mojoParameter = FieldUtils.getDeclaredField(ClosureCompilerMojo.class, option.getName(), true);
        if(mojoParameter.get(mojo) != null) {                  
          String optionName = option.getDeclaredAnnotation(Option.class).name();
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


  private List<String> getPrimitiveArgs(List<String> mojoParameters,  Field option) {
    List<String> commandList = new ArrayList<String>();
    try {
      if(mojoParameters.contains(option.getName())) {
        Field mojoParameter = ClosureCompilerMojo.class.getDeclaredField(option.getName());
        if(mojoParameter.get(mojo) != null 
            && (ClassUtils.isPrimitiveOrWrapper(mojoParameter.getType()) || mojoParameter.getType().isAssignableFrom(String.class))) {                  
          commandList.add(option.getDeclaredAnnotation(Option.class).name());
          commandList.add(String.valueOf(mojoParameter.get(mojo)));
        }
      }
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException  | IllegalAccessException e) {
      e.printStackTrace();
    }
    return commandList;
  }
}
