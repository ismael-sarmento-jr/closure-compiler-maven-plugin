package org.mobilizadores.ccmp.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.RunnableClosureCompiler;
import com.google.javascript.jscomp.CommandLineRunner;
import junit.framework.Assert;

@RunWith(BlockJUnit4ClassRunner.class)
public class RunnableClosureCompilerTest {

  RunnableClosureCompiler rcc;
  String[] js = {"file1.js","file2.js"};
  String[] externs = {"extern1.js"};
  String outputFile = "ouptput.js";
  String[] args = {"--js","file1.js","--js","file2.js","--externs","extern1.js","--js_output_file","ouptput.js"};
  
  @Before
  public void setUp() {
    this.rcc = new RunnableClosureCompiler(args , new ReentrantLock(), System.out);
    Assert.assertNotNull(rcc);
  }
  
  @Test
  public void testConcurrentCompression() {
    rcc.run();
  }
  
  @Test
  public void testNotifyObservers() {
    rcc.run();
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
