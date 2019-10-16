package org.mobilizadores.ccmp.core;

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

  public String[] getCommandLine(String outputFile, File inputDirectory, ClosureCompilerMojo mojo, String... inputFiles) {
    List<String> commandList = new ArrayList<>();
      commandList.addAll( getArgs(mojo));
      commandList.add("--js_output_file");
      commandList.add(outputFile);
      Arrays.asList(inputFiles).forEach(file -> {
        commandList.add("--js");
        commandList.add(file);
      });
    return commandList.toArray(new String[] {});
  }

  public List<String>  getArgs(ClosureCompilerMojo mojo) {
    List<String> commandList = new ArrayList<String>();
    List<String> mojoParameters = Arrays.stream( ClosureCompilerMojo.class.getDeclaredFields()).map(field -> field.getName()).collect(Collectors.toList());
    try {
      Class<?> flagsClass = Class.forName("com.google.javascript.jscomp.CommandLineRunner$Flags");
      Field[] options = flagsClass.getDeclaredFields();
      Arrays.asList(options).stream().forEach(option -> {
        try {
          if(option.isAnnotationPresent(Option.class)) {
//              if (ClassUtils.isPrimitiveOrWrapper(option.getType()) || option.getType().isAssignableFrom(String.class)){
//                commandList.addAll(getPrimitiveArgs(mojo, mojoParameters, option));
//              } else
                  if( Iterable.class.isAssignableFrom(option.getType())) {
                    Class<?> listTypeClass = Class.forName(((ParameterizedType) option.getGenericType()).getActualTypeArguments()[0].getTypeName());
                    if(ClassUtils.isPrimitiveOrWrapper(listTypeClass) || String.class.isAssignableFrom(listTypeClass)) {                      
                      commandList.addAll(getIterableArgs(mojo, mojoParameters, option));
                    }
                  } else {
                    commandList.addAll(getPrimitiveArgs(mojo, mojoParameters, option));
                  }
          }
        } catch (ClassNotFoundException e) {}
      });
    } catch (ClassNotFoundException e1) {}
    return commandList;
  }


  private List<String> getIterableArgs(ClosureCompilerMojo mojo, List<String> mojoParameters, Field option) {
    List<String> commandList = new ArrayList<String>();
    try {
      if(mojoParameters.contains(option.getName())) {
        Field mojoParameter = FieldUtils.getDeclaredField(ClosureCompilerMojo.class, option.getName(), true);
        if(mojoParameter.get(mojo) != null) {                  
          String optionName = option.getDeclaredAnnotation(Option.class).name();
          Consumer consumer = new Consumer() {
            @Override
            public void accept(Object value) {
              commandList.add(optionName);
              commandList.add(value.toString());
           }};
           MethodUtils.invokeMethod(mojoParameter.get(mojo), "forEach", consumer);
        }
      }
    } catch (SecurityException | IllegalArgumentException  | IllegalAccessException 
        | NoSuchMethodException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return commandList;
  }


  private List<String> getPrimitiveArgs(ClosureCompilerMojo mojo, List<String> mojoParameters,  Field option) {
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
