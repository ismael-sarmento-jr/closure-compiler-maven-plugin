package org.mobilizadores.ccmp.test;

import java.util.Arrays;
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
    String[] flags = {"--compilation_level", "--jscomp_dev_mode", "--logging_level", "--warning_level", "--env"};
    List<String> args = null;
    for (int i = 0; i < options.length; i++) {
      args = clh.getPrimitiveArgPair(options[i]);
      Assert.assertEquals(flags[i], args.get(0));
    }
    
    String nullOption = "displayHelp";
    args = clh.getPrimitiveArgPair(nullOption);
    Assert.assertTrue(args.isEmpty());
  }
  
  @Test
  public void testGetIterableArgs() throws IllegalAccessException {
    List<String> externs = Arrays.asList(new String[]{"extern1", "extern2", "extern3"});
    FieldUtils.writeDeclaredField(ccm, "externs", externs, true);
    List<String> args = clh.getIterableArgsPairs("externs");
    Assert.assertFalse(args.isEmpty());
    Assert.assertEquals(6, args.size());
    Assert.assertTrue(args.containsAll(externs));
    assertAlternateArgs(args);
  }
  
  @Test
  public void testFilesArgsConsistentPairs() {
    String outputFile = "src/test/resource/test-results/output.js";
    String[] inputFiles = {"src/test/resource/dir1/file11", "src/test/resource/dir1/dir2/file21"};
    List<String> filesArgs = clh.getFilesArgs(outputFile, inputFiles);
    assertAlternateArgs(filesArgs);
    
  }

  /**
   * Asserts that args are alternated between flag and value
   */
  private void assertAlternateArgs(List<String> args) {
    boolean isArg, isPreviousArg = false;
    Iterator<String> iterator = args.iterator();
    while (iterator.hasNext()) {
      isArg = iterator.next().startsWith("-");
      Assert.assertTrue(isArg ^ isPreviousArg);
      isPreviousArg = isArg;
    } 
  }
  
}
