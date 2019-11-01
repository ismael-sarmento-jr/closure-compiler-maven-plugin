package org.mobilizadores.ccmp.test;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.ClosureCompilerMojo;
import org.mobilizadores.ccmp.ContextHoldingSecurityManager;

@RunWith(BlockJUnit4ClassRunner.class)
public class ClosureCompilerMojoIntegrationTest extends AbstractMojoTestCase {
  
  ClosureCompilerMojo ccm;
  ContextHoldingSecurityManager securityManager = new ContextHoldingSecurityManager();
  File outputFolder = new File("src/test/resources/output");
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    System.setSecurityManager(securityManager );
    FileUtils.cleanDirectory(outputFolder);
    assertOutputFolderIsEmpty();
  }
  
  @Test
  public void testCompressionSingleOutputFile() throws Exception {
    this.ccm = (ClosureCompilerMojo) lookupMojo("compress", "src/test/resources/poms/single.output.pom.xml");
    try {
      ccm.execute();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  private void assertOutputFolderIsEmpty() {
    Assert.assertEquals(0, outputFolder.list().length);
  }
}
