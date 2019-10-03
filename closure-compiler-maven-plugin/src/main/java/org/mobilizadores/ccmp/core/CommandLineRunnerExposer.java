package org.mobilizadores.ccmp.core;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.google.javascript.jscomp.CommandLineRunner;

public class CommandLineRunnerExposer {

  private CommandLineRunner clr;
  private Flags flags = new Flags();

  public CommandLineRunner initializeAndExpose(DefaultMojo mojo) {
    // test: this order must be preserved, so 'special' args won't be overriden by simple args
    setSingleFlags(mojo.args);
    this.clr = getCommandLineRunnerInstance();
    return this.clr;
  }

  public void run(String outputFile, List<String> inputFiles) {
    try {
      FieldUtils.writeDeclaredField(flags.getClrFlags(), "jsOutputFile", outputFile, true);
      FieldUtils.writeDeclaredField(flags.getClrFlags(), "js", inputFiles, true);
      MethodUtils.invokeMethod(this.clr, true, "run");
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  /**
   * Instantiates a {@link CommandLineRunner} and sets its field 'flags'.
   */
  private CommandLineRunner getCommandLineRunnerInstance() {
    CommandLineRunner clr = null;
    try {
      Constructor<CommandLineRunner> constructor = CommandLineRunner.class.getDeclaredConstructor(String[].class, PrintStream.class, PrintStream.class);
      constructor.setAccessible(true);
      clr = constructor.newInstance(new String[]{}, System.out, System.err);
      FieldUtils.writeDeclaredField(clr, "flags", this.flags.getClrFlags(), true);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
    }
    return clr;
  }
  

  /**
  *   Set any flags which are not collections.
  */
  public void setSingleFlags(Map<String, Object> args) {
    if(args != null ){
      args.keySet().stream().forEach((key) -> {
        flags.setClrFlag(key, args.get(key));
      });
    }    
  }
}
