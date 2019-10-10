package org.mobilizadores.ccmp.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.apache.maven.plugins.annotations.Parameter;
import org.kohsuke.args4j.Option;

public class CommandLineHelper {

  public String[] getCommandLine(String outputFile, File inputDirectory, String... inputFiles) {
    List<String> commandList = new ArrayList<>();
    try {
      Class<?> flagsClass = Class.forName("com.google.javascript.jscomp.CommandLineRunner$Flags");
      commandList.addAll( getPrimitiveArgs(flagsClass));
      
      commandList.add("--js_module_root");
      commandList.add(inputDirectory.getPath());
      commandList.add("--js_output_file");
      commandList.add(outputFile);
      Arrays.asList(inputFiles).forEach(file -> {
        commandList.add("--js");
        commandList.add(file);
      });
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return commandList.toArray(new String[] {});
  }


  public List<String>  getPrimitiveArgs(Class<?> flagsClass) {
    List<String> commandList = new ArrayList<String>();
    Field[] parameters = DefaultMojo.class.getDeclaredFields();
    parameters[0].isAnnotationPresent(Parameter.class);
    Arrays.asList(parameters).stream().forEach(parameter -> {
      if(ClassUtils.isPrimitiveOrWrapper(parameter.getType())) {
        if (parameter.isAnnotationPresent(Parameter.class)) {
          try {
            Field flag = flagsClass.getDeclaredField(parameter.getAnnotation(Parameter.class).alias());
            if (flag.isAnnotationPresent(Option.class)) {
              commandList.add(flag.getDeclaredAnnotation(Option.class).name());
              commandList.add(String.valueOf(parameter.get(this)));
            }
          } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
              | IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    });
    return commandList;
  }
}
