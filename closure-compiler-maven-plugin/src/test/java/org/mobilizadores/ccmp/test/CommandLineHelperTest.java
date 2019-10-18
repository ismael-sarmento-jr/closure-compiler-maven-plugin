package org.mobilizadores.ccmp.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mobilizadores.ccmp.ClosureCompilerMojo;
import org.mobilizadores.ccmp.CommandLineHelper;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommandLineHelperTest {
  
  ClosureCompilerMojo ccm;
  @Spy
  CommandLineHelper clh;
  
  @Before
  public void setUp() {
    this.ccm = new ClosureCompilerMojo();
    this.clh = new CommandLineHelper(this.ccm);
  }

  public void testGetFilesArgs() {
    
  }
  
  public void testGetPrimitiveArgs() {
    
  }
  
  public void testGetIterableArgs() {
    
  }
  
}
