package org.mobilizadores.ccmp.test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mobilizadores.ccmp.Notification;
import org.mobilizadores.ccmp.RunnableClosureCompiler;
import org.mobilizadores.ccmp.SystemExitNotAllowedException;
import static  org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import com.google.javascript.jscomp.CommandLineRunner;
import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class RunnableClosureCompilerTest {

  RunnableClosureCompiler rcc;
  RunnableClosureCompiler spiedRcc;
  String[] js = {"src/test/resources/dir1/file11.js","src/test/resources/dir1/dir2/file21.js"};
  String[] externs = {"src/test/resources/extern1.js"};
  String outputFile = "src/test/resources/ouptput.js";
  String[] args = {"--js","src/test/resources/dir1/file11.js","--js","src/test/resources/dir1/dir2/file21.js",
      "--externs","src/test/resources/extern1.js","--js_output_file","src/test/resources/ouptput.js"};
  boolean notified;
  
  @Before
  public void setUp() {
    this.rcc = new RunnableClosureCompiler(args , new ReentrantLock(), System.out);
    this.spiedRcc = spy(rcc);
    Assert.assertNotNull(rcc);
  }
  
  @Test
  public void testConcurrentCompression() {
    rcc.addObserver((Observable o, Object arg) -> {
      Notification notif = (Notification) arg;
      Assert.assertNotNull(notif);
    });
    rcc.run();
  }
  
  @Test
  public void testNotifyObserversOnSuccess() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    doThrow(new InvocationTargetException(new SystemExitNotAllowedException(RunnableClosureCompiler.SUCCESS))
            ).when(spiedRcc).runClosureCompiler(any());
    spiedRcc.addObserver((Observable o, Object arg) -> {
      notified = true;
      Notification notif = (Notification) arg;
      Assert.assertNotNull(notif);
      Assert.assertEquals(this.args, notif.getArgs());
    });
    spiedRcc.run();
    Assert.assertTrue(notified);
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testGetNewCompilerInstanceWithConsistentConfiguration() {
    CommandLineRunner clr = rcc.getCommandLineRunnerNewInstance();
    Assert.assertNotNull(clr);
    Object config = getConfig(clr);
    
    List<Object> flagsEntries = (List<Object>) getConfigParam(config, "mixedJsSources");
    List<String> jsSources = getJsSources(flagsEntries);
    Assert.assertTrue(jsSources.containsAll(Arrays.asList(this.js)));
    
    String outputFile = (String) getConfigParam(config, "jsOutputFile");
    Assert.assertEquals(this.outputFile, outputFile);
    
    List<String> externs = (List<String>) getConfigParam(config, "externs");
    Assert.assertTrue(externs.containsAll(Arrays.asList(this.externs)));
  }
  
  public Object getConfigParam(Object config, String paramName) {
    Object configParamValue = null;
    try {
      configParamValue = FieldUtils.readDeclaredField(config, paramName, true);
    } catch (IllegalAccessException e) {
      Assert.fail("Error in the test: could not read field " + paramName 
          + " in AbstractCommandLineRunner.CommandLineConfig: ");
    };
    return configParamValue;
  }

  public List<String> getJsSources(List<Object> flagsEntries) {
    List<String> sources = new ArrayList<>();
      flagsEntries.stream().forEach((flagEntry) -> {
        try {
          sources.add((String) FieldUtils.readDeclaredField(flagEntry, "value", true));
        } catch (IllegalAccessException e) {
          Assert.fail("Error in the test: could not read fields in AbstractCommandLineRunner.FlagEntry: " + e.getMessage());
        }
      });
    return sources;
  }

  public Object getConfig(CommandLineRunner clr) {
    Object config = null;
    try {
      config = FieldUtils.readField(clr, "config", true);
    } catch (IllegalAccessException e) {
      Assert.fail("Error in the test: could not read 'config' in AbstractCommandLineRunner: " + e.getMessage());
    }
    return config;
  }
}
