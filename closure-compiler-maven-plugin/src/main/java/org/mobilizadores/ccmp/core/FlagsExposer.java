package org.mobilizadores.ccmp.core;

import java.util.Map;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * This class offers an abstraction for the properties of the private 
 * inner class com.google.javascript.jscomp.CommandLineRunner$Flags.
 */
public class FlagsExposer extends AbstractExposer<Object> {
  
  public FlagsExposer(String className) {
    super(className);
  }

  /**
   *   Set any flags which are not collections.
   */
   public void setSimpleFlags(Map<String, Object> args) {
     if(args != null ){
       args.keySet().stream().forEach((key) -> {
         this.setClrFlag(key, args.get(key));
       });
     }    
   }

  private void setClrFlag(String flagAlias, Object flagValue) {
    try {
      FieldUtils.writeDeclaredField(this.privateObject, flagAlias, flagValue, true);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
