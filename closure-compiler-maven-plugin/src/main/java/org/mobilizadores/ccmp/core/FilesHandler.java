package org.mobilizadores.ccmp.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import com.google.common.io.Files;
import com.google.javascript.jscomp.LoggerErrorManager;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.deps.DependencyInfo;
import com.google.javascript.jscomp.deps.JsFileRegexParser;

public class FilesHandler {
  
  Logger logger = Logger.getLogger(FileHandler.class.getName());
  
  JsFileRegexParser jsParser = new JsFileRegexParser(new LoggerErrorManager(this.logger));

  private List<File> includeFiles;

  private File inputDirectory;

  private boolean failOnNoInputFilesFound;
  

  public FilesHandler(List<File> includeFiles, File inputDirectory,
      boolean failOnNoInputFilesFound) {
    super();
    this.includeFiles = includeFiles;
    this.inputDirectory = inputDirectory;
    this.failOnNoInputFilesFound = failOnNoInputFilesFound;
  }

  public String getAbsolutePath(String depRelPath, String moduleAbsPath) {
    String sysSeparator = File.separator;
    String escape = "\\";
    String[] modPath = moduleAbsPath.contains(sysSeparator) ? 
                                moduleAbsPath.substring(0, moduleAbsPath.lastIndexOf(sysSeparator)) //get path without file name
                                .split(escape + sysSeparator) :
                                new String[] {moduleAbsPath};
    String prevDir = "../";
    int countChangeDir = StringUtils.countMatches(depRelPath, prevDir); //TODO benchmark solution...taking too long
    if(modPath.length < countChangeDir)
      return null; //FIXME
    return String.join(sysSeparator, Arrays.copyOf(modPath, modPath.length - countChangeDir )) 
                            + sysSeparator
                            + depRelPath.substring(depRelPath.lastIndexOf(prevDir) + prevDir.length(), depRelPath.length());
  }
  
  /**
   * @param file 
   *            the file to be compressed.
   * @return a list with the absolute paths of the main file and its dependencies.
   * @throws IOException
   */
  public Set<String> getFileWithDepsList(File file) throws IOException {
    DependencyInfo dependencyInfo = this.jsParser.parseFile(file.getPath(), file.getName(), SourceFile.fromFile(file.getPath()).getCode());
    Set<String> resultList = new HashSet<>();
    dependencyInfo.getRequires().stream().forEach(dep -> {
          try {
            resultList.addAll( getFileWithDepsList(new File( getAbsolutePath(dep.getRawText(), file.getPath()))));
          } catch (IOException e) {
            e.printStackTrace();
          }
    });
    
    resultList.add(file.getPath());
    return resultList;
  }
  
  /**
   * @return the relative path - including the name of the file - in which the compressed file will be written.
   */
  public String getResultFileRelativePath(File file, String suffix) {
    String fileExtension = Files.getFileExtension(file.getName());
    String fileName = suffix != null ? file.getName().replace(fileExtension, suffix + "." + fileExtension) : file.getName();
    return fileName;
  }
  
  /**
   * Finds javascript files in the inputDirectory and adds them to the includeFiles list.
   */
  void mergeIncludeFilesList() throws MojoExecutionException {
    this.includeFiles = this.includeFiles == null ? new ArrayList<>() : this.includeFiles;
    if (this.inputDirectory != null) {
      Files.fileTraverser().breadthFirst(this.inputDirectory).forEach(file -> {
        if (file.isFile() && "js".equals(Files.getFileExtension(file.getName()))) {
          this.includeFiles.add(file);
        }
      });
    }
    if (this.failOnNoInputFilesFound && this.includeFiles.isEmpty())
      throw new MojoExecutionException("No javascript files were found.");
  }
}
