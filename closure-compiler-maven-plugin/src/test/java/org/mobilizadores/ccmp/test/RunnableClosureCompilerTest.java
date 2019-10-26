package org.mobilizadores.ccmp.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.ContextHoldingSecurityManager;
import org.mobilizadores.ccmp.FilesHandler;
import org.mobilizadores.ccmp.Notification;
import org.mobilizadores.ccmp.RunnableClosureCompiler;
import org.mobilizadores.ccmp.SystemExitNotAllowedException;
import com.google.javascript.jscomp.CommandLineRunner;
import junit.framework.Assert;

@RunWith(BlockJUnit4ClassRunner.class)
public class RunnableClosureCompilerTest {

  Lock lock = new ReentrantLock();
  RunnableClosureCompiler rcc;
  RunnableClosureCompiler spiedRcc;
  String[] js = {"src/test/resources/dir1/file11.js","src/test/resources/dir1/dir2/file21.js"};
  String[] externs = {"src/test/resources/extern1.js"};
  String outputFile = "src/test/resources/ouptput.js";
  String[] args = {"--js","src/test/resources/dir1/file11.js","--js","src/test/resources/dir1/dir2/file21.js",
      "--externs","src/test/resources/extern1.js","--js_output_file","src/test/resources/ouptput.js"};
  boolean notified;
  private ContextHoldingSecurityManager securityManager = new ContextHoldingSecurityManager();
  
  @Before
  public void setUp() {
    System.setSecurityManager(securityManager );
    this.rcc = new RunnableClosureCompiler(args , lock, System.out);
    this.spiedRcc = spy(rcc);
    Assert.assertNotNull(rcc);
  }
  
  @Test
  public void testConcurrentCompression() {
    try {      
      for (int i = 0; i < 100; i++) {        
        RunnableClosureCompiler rcc = new RunnableClosureCompiler(args , lock, System.out);
        rcc.addObserver((Observable o, Object arg) -> {
          Notification notif = (Notification) arg;
          Assert.assertNotNull(notif);
          Assert.assertEquals(RunnableClosureCompiler.SUCCESS, notif.getStatus());
        });
        Thread thread = new Thread(rcc);
        thread.start();
      }
      securityManager.enableSystemExit();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
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
    CommandLineRunner clr;
    try {
      clr = rcc.getCommandLineRunnerNewInstance();

      Assert.assertNotNull(clr);
      Object config = getConfig(clr);
      
      List<Object> flagsEntries = (List<Object>) getConfigParam(config, "mixedJsSources");
      List<String> jsSources = getJsSources(flagsEntries);
      
      Assert.assertTrue( jsSources.containsAll(FilesHandler.getNormalizedPaths(this.js)));
      
      String outputFile = (String) getConfigParam(config, "jsOutputFile");
      Assert.assertEquals(this.outputFile, outputFile);
      
      List<String> externs = (List<String>) getConfigParam(config, "externs");
      Assert.assertTrue(externs.containsAll(Arrays.asList(this.externs)));
    } catch (InvocationTargetException e) {
      Assert.fail(e.getCause().toString());
    }
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
