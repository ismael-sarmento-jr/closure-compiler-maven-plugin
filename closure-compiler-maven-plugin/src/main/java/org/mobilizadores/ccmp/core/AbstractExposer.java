package org.mobilizadores.ccmp.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Forces the instantiation of child classes to inform class name for the private object.
 */
public abstract class AbstractExposer<T> implements IExposer<T>{

  T privateObject;
  private String className;
  
  public AbstractExposer (String className) {
    this.className = className;
  }
  
  @Override
  public T get() {
    return this.privateObject;
  }

  @Override
  public IExposer<T> initializeAndExpose() {
    this.privateObject = getPrivateObjectNewInstance(this.className);
    return this;
  }

  public T getPrivateObjectNewInstance(String className) {
    try {
      Constructor<?> constructor = Class.forName(className).getDeclaredConstructor();
      constructor.setAccessible(true);
      @SuppressWarnings("unchecked")
      T newInstance = (T) constructor.newInstance();
      return newInstance;
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}
