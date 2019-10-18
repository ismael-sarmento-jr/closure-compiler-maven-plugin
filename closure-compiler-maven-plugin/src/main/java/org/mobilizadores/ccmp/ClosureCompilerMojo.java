package org.mobilizadores.ccmp;
/*
 * Licensed to Mobilizadores.org under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Mobilizadores licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 * Complete information can be found at: https://dev.mobilizadores.com/licenses
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.google.javascript.jscomp.PolymerExportPolicy;

/**
 * This class is the default maven mojo of the closure compiler plugin. Its default
 * goal is "compress" and the default lifecycle phase is "prepare package". 
 * It uses multithreading to optimize the compression of the javascript files. It uses 
 * a simple fixed size thread pool, initially set to length 10. It is an {@link Observer},
 * that is adds itself to the runnables {@link RunnableClosureCompiler}, so information
 * about the compression can be logged.
 */
@Mojo(name = "compress", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class ClosureCompilerMojo extends AbstractMojo implements Observer {
  
  FilesHandler filesHandler = new FilesHandler();
  CommandLineHelper clh = new CommandLineHelper(this);
  Object lock = new Object();
  ContextHoldingSecurityManager securityManager = new ContextHoldingSecurityManager();
  ExecutorService executorPoolService;
  InternalStream stream;
 
  /*
   * *************************
   * FILES PROPS
   * *************************/
  @Parameter
  String suffix;
  @Parameter(required = true)
  File inputDirectory;
  @Parameter(alias = "js")
  List<File> includeFiles;
  @Parameter(defaultValue = "target/${project.build.finalName}/WEB-INF/js")
  File outputDirectory;
  /**
   * If the outputFile is not specified, then the files are going to be compressed separately.
   */
  @Parameter(alias = "jsOutputFile")
  File outputFile;
  
  
  /*
   * *************************
   * ADVANCED PROPS
   * *************************/
  /**
   * Defines the size of a thread pool; it can be tunned by the plugin user,
   * according to the number of output files to be produced, to obtain
   * better performance.
   */
  @Parameter(defaultValue = "10")
  Integer maxNumberOfThreads;
  @Parameter(defaultValue = "true")
  Boolean failOnNoInputFilesFound;
  
  
  /*
   * *************************
   * DERIVED PROPS
   * *************************/

  @Parameter
  Integer browserFeaturesetYear;
  @Parameter
  Boolean displayHelp;
  @Parameter
  Boolean printTree;
  @Parameter
  Boolean printAst;
  @Parameter
  Boolean printPassGraph;
  @Parameter(defaultValue = "true")
  Boolean emitUseStrict;
  @Parameter(defaultValue = "true")
  Boolean strictModeInput;
  @Parameter(defaultValue = "OFF")
  String jscompDevMode;
  @Parameter(defaultValue = "WARNING")
  String loggingLevel;
  @Parameter
  List<String> externs;
  @Parameter
  List<String> unusedJsZip;
  @Parameter
  List<String> chunk;
  @Parameter
  String continueSavedCompilationFile;
  @Parameter
  String saveAfterChecksFile;
  @Parameter
  String variableMapOutputFile;
  @Parameter
  Boolean createNameMapFiles;
  @Parameter
  Boolean sourceMapIncludeSourcesContent;
  @Parameter
  String propertyMapOutputFile;
  @Parameter
  Boolean thirdParty;
  @Parameter
  Integer summaryDetailLevel;
  @Parameter(defaultValue = "NONE")
  String isolationMode;
  @Parameter
  String outputWrapper;
  @Parameter
  String outputWrapperFile;
  @Parameter
  List<String> chunkWrapper;
  @Parameter(defaultValue = "./")
  String chunkOutputPathPrefix;
  @Parameter
  String createSourceMap;
  @Parameter(defaultValue = "DEFAULT")
  String sourceMapFormat;
  @Parameter
  List<String> sourceMapLocationMapping;
  @Parameter
  List<String> sourceMapInputs;
  @Parameter
  Boolean parseInlineSourceMaps;
  @Parameter
  Boolean applyInputSourceMaps;
  @Parameter
  List<String> define;
  @Parameter
  String charset;
  @Parameter(defaultValue = "SIMPLE")
  String compilationLevel;
  @Parameter
  Integer numParallelThreads;
  @Parameter
  Boolean checksOnly;
  @Parameter(defaultValue = "OFF")
  String incrementalCheckMode;
  @Parameter
  Boolean continueAfterErrors;
  @Parameter(defaultValue = "true")
  Boolean useTypesForOptimization;
  @Parameter
  Boolean assumeFunctionWrapper;
  @Parameter(defaultValue = "QUIET")
  String warningLevel;
  @Parameter
  Boolean debug;
  @Parameter
  Boolean generateExports;
  @Parameter
  Boolean exportLocalPropertyDefinitions;
  @Parameter
  List<String> formatting;
  @Parameter
  Boolean processCommonJsModules;
  @Parameter
  List<String> commonJsPathPrefix;
  @Parameter(defaultValue = "true")
  Boolean processClosurePrimitives;
  @Parameter
  Boolean angularPass;
  @Parameter
  Integer polymerVersion;
  @Parameter
  String polymerExportPolicy = PolymerExportPolicy.LEGACY.name();
  @Parameter
  Boolean chromePass;
  @Parameter
  Boolean dartPass;
  @Parameter
  String j2clPassMode = "AUTO";
  @Parameter
  String outputManifest;
  @Parameter
  String outputChunkDependencies;
  @Parameter(defaultValue = "STABLE")
  String languageIn;
  @Parameter(defaultValue = "STABLE")
  String languageOut;
  @Parameter
  Boolean version;
  @Parameter
  String translationsFile;
  @Parameter
  String translationsProject;
  @Parameter
  List<String> flagFiles;
  @Parameter
  String warningsWhitelistFile;
  @Parameter
  List<String> hideWarningsFor;
  @Parameter
  List<String> extraAnnotationName;
  @Parameter(defaultValue = "OFF")
  String tracerMode;
  @Parameter
  String renamePrefix;
  @Parameter
  String renamePrefixNamespace;
  @Parameter
  List<String> conformanceConfigs;
  @Parameter(defaultValue = "BROWSER")
  String environment;
  @Parameter(defaultValue = "NONE")
  String jsonStreamMode;
  @Parameter
  Boolean preserveTypeAnnotations;
  @Parameter(defaultValue = "true")
  Boolean injectLibraries;
  @Parameter
  List<String> forceInjectLibraries;
  @Parameter
  List<String> entryPoint;
  @Parameter(defaultValue = "true")
  Boolean rewritePolyfills;
  @Parameter
  Boolean printSourceAfterEachPass;
  @Parameter(defaultValue = "BROWSER")
  String moduleResolutionMode;
  @Parameter
  Map<String, String> browserResolverPrefixReplacements;
  @Parameter
  String packageJsonEntryNames;
  @Parameter(defaultValue = "STANDARD")
  String errorFormat;
  @Parameter(defaultValue = "true")
  Boolean renaming;
  @Parameter
  Boolean helpMarkdown;
  @Parameter
  List<String> moduleRoot;
  
  public ClosureCompilerMojo() {
    super();
    System.setSecurityManager(this.securityManager);
    try {
      this.stream = new InternalStream(new FileOutputStream("log.out"));
    } catch (FileNotFoundException e) {}
  }

  /**
   * Initiates the thread pool, calls the proper methods to get the complete list of files - 
   * with their dependencies - and pass to the threads for compression. 
   * The result of the 
   * compression is delayed until the end of all threads, to avoid cross writing to the 
   * default output. 
   * The system exiting is disabled before the execution of the calls and re-enabled after
   * all the tasks are finished. 
   */
  public void execute() throws MojoExecutionException {
    if (this.inputDirectory == null && this.includeFiles == null)
      throw new MojoExecutionException(
          "Either parameter 'includeFiles' or 'inputDirectory' must be specified");
    
    this.executorPoolService = Executors.newFixedThreadPool(this.maxNumberOfThreads);
    List<File> effectiveInputFilesList = this.filesHandler.getEffectiveInputFilesList(
                                                                                      this.inputDirectory, 
                                                                                      this.includeFiles, 
                                                                                      this.failOnNoInputFilesFound
                                                                                      );
    
    String tempFolder = this.suffix != null ? "" : File.separator + "temp";
    if (this.outputFile == null) {
          effectiveInputFilesList.stream().forEach(file -> {
            try {
              Set<String> fileWithDeps = this.filesHandler.getFileWithDepsList(file);
              if(fileWithDeps.size() > 0) {
                String outputFilePath = this.outputDirectory.getAbsolutePath() 
                                            + tempFolder
                                            + this.filesHandler.getResultFileRelativePath(this.inputDirectory, file, this.suffix);
                queueCompilation(outputFilePath, fileWithDeps.toArray(new String[]{}));
              }
            }  catch (IOException e) {
              e.printStackTrace();
            }
          });
    } else {
        String[] inputArray = effectiveInputFilesList.stream().map(file -> {
                                                return file.getPath();
                                              }).collect(Collectors.toList()).toArray(new String[] {});
        if(inputArray.length > 0) {
          queueCompilation(this.outputFile.getPath(), inputArray);
        }
    }
    
    awaitTasksTermination(executorPoolService);
    this.stream.report();
    this.filesHandler.copyFilesFromTempFolder(this.outputDirectory.getAbsolutePath() + tempFolder, this.outputDirectory.getAbsolutePath());
    this.securityManager.enableSystemExit();
  }

  /**
   * Uses the input and output files passed as parameters to get the formatted command line args
   * and request a new instance of a observable runnable. Adds this mojo to the list of observers
   * and then adds the runnable to the execution queue.
   */
  private void queueCompilation(String outputFilePath, String[] inputArray) {
    String[] commandLine = this.clh.getCommandLine(outputFilePath, this.inputDirectory, inputArray);
    RunnableClosureCompiler runnableCc = getNewRunnableClosureCompiler(commandLine );
    runnableCc.addObserver(this);
    executorPoolService.execute(runnableCc);
  }

  /**
   * @return A new instance of a runnable with the thread's lock, for synchronization, the mojo's stream, for
   * delayed report and the args for the final command line.
   */
  public RunnableClosureCompiler getNewRunnableClosureCompiler(final String[] commandLine) {
    return new RunnableClosureCompiler(commandLine, this.lock, this.stream);
  }


  private void awaitTasksTermination(ExecutorService executorService) {
    executorService.shutdown();
    try {
      while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {}
    } catch (InterruptedException e) {
      e.printStackTrace();
    }  
  }
  

  /**
   * Displays information in the terminal, using default {@link AbstractMojo} logger
   */
  @Override
  public void update(Observable o, Object obj) {
    if(obj != null) {   
      Notification notif = (Notification) obj;
      int pos = IntStream.range(0, notif.getArgs().length).filter(i -> "--js_output_file".equals(notif.getArgs()[i])).findFirst().getAsInt();
      getLog().info(notif.getDescription() + " to: " + notif.getArgs()[pos + 1]);
    }
  }
  
}

