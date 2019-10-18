package org.mobilizadores.ccmp.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.FilesHandler;
import org.mobilizadores.ccmp.InvalidRelativePathException;

@RunWith(BlockJUnit4ClassRunner.class)
public class FilesHandlerTest {

  FilesHandler filesHandler = new FilesHandler();
  
  @Test
  public void testDepsAbsPath() {
    String moduleAbsPath = "abs/mod/path";

    String depRelPath = "./path.dep";
    String depAbsPath = filesHandler.getDepAbsolutePath(depRelPath, moduleAbsPath);
    Assert.assertEquals("abs"+File.separator +"mod"+File.separator +"path" +File.separator +"path.dep", depAbsPath);
    
    depRelPath = "../path.dep";
    depAbsPath = filesHandler.getDepAbsolutePath(depRelPath, moduleAbsPath);
    Assert.assertEquals("abs"+File.separator +"mod"+File.separator +"path.dep", depAbsPath);
    
    depRelPath = "../../path.dep";
    depAbsPath = filesHandler.getDepAbsolutePath(depRelPath, moduleAbsPath);
    Assert.assertEquals("abs"+File.separator +"path.dep", depAbsPath);
  }
  
  @Test
  public void testDepsAbsPathInvalidPath() {
    boolean exception = false;
    try {
      String moduleAbsPath = "abs/mod/path";
      String depRelPath = "../../../../path.dep";
      filesHandler.getDepAbsolutePath(depRelPath, moduleAbsPath);
    } catch (InvalidRelativePathException e) {
      exception = true;
    }
    Assert.assertTrue(exception);
  }

  
  @Test
  public void testGetDeps() {
    Set<String> list = null;
    File dep = new File("src/test/resources/dir1/file11.js");
    try {
      list = filesHandler.getFileWithDepsList(new File("src/test/resources/dir1/dir2/file21.js"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    Assert.assertNotNull(list);
    Assert.assertTrue(!list.isEmpty());
    Assert.assertTrue(list.contains(dep.getPath()));
  }
  
  @Test
  public void testGetRelativePath() {
    String suffix = null;
    File baseDirectory = new File("src/test/resources");
    File file = new File("src/test/resources/dir1/dir2/file22.mjs");
    
    String relativePath = filesHandler.getResultFileRelativePath(baseDirectory, file, suffix);
    Assert.assertEquals(File.separator +"dir1"+File.separator +"dir2"+File.separator +"file22.mjs", relativePath);
    
    suffix = "min";
    baseDirectory = new File("src/test/resources/dir1");
    relativePath = filesHandler.getResultFileRelativePath(baseDirectory, file, suffix);
    Assert.assertEquals(File.separator +"dir2"+File.separator +"file22.min.mjs", relativePath);
    
    baseDirectory = new File("src/test");
    relativePath = filesHandler.getResultFileRelativePath(baseDirectory, file, suffix);
    Assert.assertEquals(File.separator +"resources"+File.separator +"dir1"+File.separator +"dir2"+File.separator +"file22.min.mjs", relativePath);
  }
  
  @Test
  public void testFindDistinctJsFilesInDirectories() {
    File inputDirectory = new File("src/test/resources/dir1");
    List<File> includeFiles = new ArrayList<>();
    File duplicateFile = new File("src/test/resources/dir1/file11.js");
    includeFiles.add(duplicateFile);
    List<File> list = null;
    try {
      list = filesHandler.getEffectiveInputFilesList(inputDirectory, includeFiles, false);
    } catch (MojoExecutionException e) {
      Assert.fail();
    }
    Assert.assertNotNull(list);
    Assert.assertTrue(!list.isEmpty());
    long count = list.stream().filter(file -> file.equals(duplicateFile)).count();
    Assert.assertEquals(1L, count);
    
  }
  
  @Test
  public void testFindDistinctJsFilesInDirectoriesFailOnNotFound() {
    boolean exception = false;
    File inputDirectory = new File("src/test/resources/dir1/dir2/dir3");
    try {
      filesHandler.getEffectiveInputFilesList(inputDirectory, new ArrayList<File>(), true);
    } catch (MojoExecutionException e) {
      exception = true;
    }
    Assert.assertTrue(exception);
  }
}
