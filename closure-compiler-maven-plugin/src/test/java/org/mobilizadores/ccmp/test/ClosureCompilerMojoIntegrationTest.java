package org.mobilizadores.ccmp.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.ClosureCompilerMojo;
import org.mobilizadores.ccmp.ContextHoldingSecurityManager;
import org.mobilizadores.ccmp.Notification;
import org.mobilizadores.ccmp.RunnableClosureCompiler;

@RunWith(BlockJUnit4ClassRunner.class)
public class ClosureCompilerMojoIntegrationTest extends AbstractMojoTestCase {
  
  ClosureCompilerMojo ccm;
  ContextHoldingSecurityManager securityManager = new ContextHoldingSecurityManager();
  File outputFolder = new File("src/test/resources/output/compressed");
  List<Notification> results = new ArrayList<>();
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    FileUtils.cleanDirectory(outputFolder);
    Assert.assertEquals(0, outputFolder.list().length);
  }
  
  @Test
  public void testCompressionSingleOutputFile() throws Exception {
    this.ccm = setUpClosureCompilerMojo("src/test/resources/poms/single.output.pom.xml");
    executeCompression();
    assertResultSuccessAndCompressedFiles();
  }
  
  @Test
  public void testCompressionMultipleOutputFilesWithSuffix() throws Exception {
    this.ccm = setUpClosureCompilerMojo("src/test/resources/poms/single.output.pom.xml");
    executeCompression();
    assertResultSuccessAndCompressedFiles();
  }
  
  @Test
  public void testCompressionMultipleOutputFilesNoSuffix() throws Exception {
    this.ccm = setUpClosureCompilerMojo("src/test/resources/poms/single.output.pom.xml");
    executeCompression();
    assertResultSuccessAndCompressedFiles();
  }

  private ClosureCompilerMojo setUpClosureCompilerMojo(String pomFile) throws Exception {
    ClosureCompilerMojo ccm = (ClosureCompilerMojo) lookupMojo("compress", pomFile);
    System.setSecurityManager(securityManager );
    ccm.addExternalObserver((Observable o, Object arg) -> {
        if(arg != null) results.add((Notification) arg);
    });
    return ccm;
  }

  private void executeCompression() {
    try {
      ccm.execute();
      this.securityManager.enableSystemExit();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  public void assertResultSuccessAndCompressedFiles() {
    this.results.stream().forEach(notif -> {
      Assert.assertNotNull(notif);
      Assert.assertEquals(RunnableClosureCompiler.SUCCESS, notif.getStatus());
    });
  }
  
}
