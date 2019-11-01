package org.mobilizadores.ccmp.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.plugin.Mojo;
import junit.framework.Assert;

public class SimpleIntegrationTestSupport {
  
  public static final String BASE_DIR = "${project.basedir}";
  public static final String BUILD_DIRECTORY = "${project.build.directory}";
  public static final String BUILD_OUTPUT_DIRECTORY = "${project.build.outputDirectory}";
  public static final String BUILD_TEST_OUTPUT_DIRECTORY = "${project.build.testOutputDirectory}";
  
  static final List<String> properties = new ArrayList<>();
  
  static {
    properties.add(BASE_DIR);
    properties.add(BUILD_DIRECTORY);
    properties.add(BUILD_OUTPUT_DIRECTORY);
    properties.add(BUILD_TEST_OUTPUT_DIRECTORY);
  }

  private static String getProjectBaseDir() {
    File projectRoot = new File("./");
    return projectRoot.getAbsolutePath().substring(0, projectRoot.getAbsolutePath().length() - 1);
  }
  
  public static void setMavenFilesProperties(Mojo mojo) {
    FieldUtils.getAllFieldsList(mojo.getClass()).stream().filter(field -> File.class.isAssignableFrom(field.getType())).forEach(field -> {
      try {
        File file = (File) FieldUtils.readDeclaredField(mojo, field.getName(), true);
        FieldUtils.writeDeclaredField(mojo, field.getName(), new File(file.getAbsolutePath()));
      } catch (IllegalAccessException e) {
        Assert.fail(e.getMessage());
      }
    });
  }
}
