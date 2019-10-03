package org.mobilizadores.ccmp.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import com.google.javascript.jscomp.CommandLineRunner;

/**
 * This class offers an abstraction for the properties of the private 
 * inner class com.google.javascript.jscomp.CommandLineRunner$Flags.
 */
public class Flags {
  
  private Object clrFlags;

  public Object getClrFlags() {
    return clrFlags;
  }

  public Flags() {
    Class<?> flagsClass = Arrays.asList(CommandLineRunner.class.getDeclaredClasses()).stream()
        .filter(klass -> klass.getName().equals("com.google.javascript.jscomp.CommandLineRunner$Flags"))
        .findFirst().get();
    try {
      Constructor<?> constructor = flagsClass.getDeclaredConstructor();
      constructor.setAccessible(true);
      this.clrFlags = constructor.newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
    }
  }


  public void setClrFlag(String flagAlias, Object flagValue) {
    try {
      FieldUtils.writeDeclaredField(clrFlags, flagAlias, flagValue, true);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
