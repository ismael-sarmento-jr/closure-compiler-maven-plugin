package org.mobilizadores.ccmp.core;

public interface IExposer<T> {

  public IExposer<T> initializeAndExpose();
  
  public T getPrivateObjectNewInstance(String className);
  
  public T get();
}
