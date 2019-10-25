package org.mobilizadores.ccmp.test;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.ClosureCompilerMojo;
import org.mobilizadores.ccmp.CommandLineHelper;

@RunWith(BlockJUnit4ClassRunner.class)
public class CommandLineHelperTest extends AbstractMojoTestCase {
  
  ClosureCompilerMojo ccm;
  CommandLineHelper clh;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.ccm = (ClosureCompilerMojo) lookupEmptyMojo("compress", "");
    this.clh = new CommandLineHelper(this.ccm);
  }
  
  @Test
  public void testGetPrimitiveArgs() {
    String[] options = {"compilationLevel", "jscompDevMode", "loggingLevel", "warningLevel", "environment"};
    String[] flags = {"--compilation_level", "--jscomp_dev_mode", "--logging_level", "--warning_level", "--environment"};
    for (int i = 0; i < options.length; i++) {
      List<String> args = clh.getPrimitiveArgPair(FieldUtils.getDeclaredField(ccm.getClass(), options[i], true));
      Assert.assertEquals(flags[i], args.get(0));
    }
  }
  
  public void testGetIterableArgs() {
    
  }
  
  @Test
  public void testGetFilesArgsConsistentPairs() {
    String outputFile = "src/test/resource/test-results/output.js";
    String[] inputFiles = {"src/test/resource/dir1/file11", "src/test/resource/dir1/dir2/file21"};
    List<String> filesArgs = clh.getFilesArgs(outputFile, inputFiles);
    boolean isArg, isPreviousArg = false;
    Iterator<String> iterator = filesArgs.iterator();
    while (iterator.hasNext()) {
      isArg = iterator.next().startsWith("-");
      Assert.assertTrue(isArg ^ isPreviousArg);
      isPreviousArg = isArg;
    }
  }
  
}
