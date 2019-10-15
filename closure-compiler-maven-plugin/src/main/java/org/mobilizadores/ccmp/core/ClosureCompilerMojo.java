package org.mobilizadores.ccmp.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.IsolationMode;
import com.google.javascript.jscomp.PolymerExportPolicy;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.WarningLevel;
import com.google.javascript.jscomp.deps.ModuleLoader;

/**
 * @goal
 */
@Mojo(name = "compress", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class ClosureCompilerMojo extends AbstractMojo implements Observer {
  
  @Parameter(defaultValue = "true")
  Boolean failOnNoInputFilesFound;
  
  @Parameter(defaultValue = "10")
  Integer maxNumberOfThreads;
  
  @Parameter
  String suffix;

  @Parameter(required = true)
  File inputDirectory;

  @Parameter(alias = "js")
  List<File> includeFiles;

  @Parameter(defaultValue = "target/${project.build.finalName}/WEB-INF/js")
  File outputDirectory;

  // @Parameter(alias = "level", defaultValue = "SIMPLE_OPTIMIZATIONS", required = true)
  // CompilationLevel compilationLevel;

  /**
   * If the outputFile is not specified, then the files are going to be minified seperately.
   */
  @Parameter(alias = "outputFile")
  File jsOutputFile;

//  @Parameter
//  Map<String, Object> args;

  FilesHandler filesHandler = new FilesHandler();
  CommandLineHelper clh = new CommandLineHelper();
  Object lock = new Object();
  ContextHoldingSecurityManager securityManager = new ContextHoldingSecurityManager();
  
  public ClosureCompilerMojo() {
    super();
    System.setSecurityManager(this.securityManager);
  }
 

  public void execute() throws MojoExecutionException {
    if (this.inputDirectory == null && this.includeFiles == null)
      throw new MojoExecutionException(
          "Either parameter 'includeFiles' or 'inputDirectory' must be specified");
    
    List<File> effectiveInputFilesList = this.filesHandler.getEffectiveInputFilesList(this.inputDirectory, this.includeFiles, this.failOnNoInputFilesFound);
    ExecutorService executorService = Executors.newFixedThreadPool(this.maxNumberOfThreads);
    if (this.jsOutputFile == null) {
          effectiveInputFilesList.stream().forEach(file -> {
          try {
            Set<String> fileList = this.filesHandler.getFileWithDepsList(file);
            if(fileList.size() > 0) {
              String outputFilePath = this.outputDirectory.getAbsolutePath() + File.separator + this.filesHandler.getResultFileRelativePath(this.inputDirectory, file, this.suffix);
              RunClosureCompiler compilerRunner = new RunClosureCompiler(this.clh.getCommandLine(outputFilePath, this.inputDirectory, this, fileList.toArray(new String[]{}) ), this.lock);
              compilerRunner.addObserver(this);
              executorService.execute(compilerRunner);
              //FIXME implement compression overriding files without suffix
              //FIXME compiler log and plugin log superpose each other and output is mixed
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
          RunClosureCompiler compilerRunner = new RunClosureCompiler(this.clh.getCommandLine(this.jsOutputFile.getPath(), this.inputDirectory, this, inputArray), this.lock);
          compilerRunner.addObserver(this);
          executorService.execute(compilerRunner);
        }
    }
    awaitTasksTermination(executorService);
    this.securityManager.enableSystemExit();
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
  
  
  /*
   * ***************************************
   * PRIMITIVE AND WRAPPER PROPERTIES
   * ***************************************/
  
  @Parameter
  Integer browserFeaturesetYear = 0;
  @Parameter
  Boolean displayHelp = false;
  @Parameter
  Boolean printTree = false;
  @Parameter
  Boolean printAst = false;
  @Parameter
  Boolean printPassGraph = false;
  @Parameter
  Boolean emitUseStrict = true;
  @Parameter
  Boolean strictModeInput = true;
  @Parameter
  CompilerOptions.DevMode jscompDevMode = CompilerOptions.DevMode.OFF;
  @Parameter
  String loggingLevel = Level.WARNING.getName();
  @Parameter
  List<String> externs = new ArrayList<>();
  @Parameter
  List<String> unusedJsZip = null;
  @Parameter
  List<String> chunk = new ArrayList<>();
  @Parameter
  String continueSavedCompilationFile = null;
  @Parameter
  String saveAfterChecksFile = null;
  @Parameter
  String variableMapOutputFile = "";
  @Parameter
  Boolean createNameMapFiles = false;
  @Parameter
  Boolean sourceMapIncludeSourcesContent = false;
  @Parameter
  String propertyMapOutputFile = "";
  @Parameter
  Boolean thirdParty = false;
  @Parameter
  Integer summaryDetailLevel = 1;
  @Parameter
  IsolationMode isolationMode = IsolationMode.NONE;
  @Parameter
  String outputWrapper = "";
  @Parameter
  String outputWrapperFile = "";
  @Parameter
  List<String> chunkWrapper = new ArrayList<>();
  @Parameter
  String chunkOutputPathPrefix = "./";
  @Parameter
  String createSourceMap = "";
  @Parameter
  SourceMap.Format sourceMapFormat = SourceMap.Format.DEFAULT;
  @Parameter
  List<String> sourceMapLocationMapping = new ArrayList<>();
  @Parameter
  List<String> sourceMapInputs = new ArrayList<>();
  @Parameter
  Boolean parseInlineSourceMaps = true;
  @Parameter
  Boolean applyInputSourceMaps = true;
  //TODO remove: it's marked as unused in the clr
//  @Parameter
//  List<String> jscompError = new ArrayList<>();
//  @Parameter
//  List<String> jscompWarning = new ArrayList<>();
//  @Parameter
//  List<String> jscompOff = new ArrayList<>();
  @Parameter
  List<String> define = new ArrayList<>();
  @Parameter
  String charset = "";
  @Parameter(defaultValue = "SIMPLE")
  String compilationLevel;
  @Parameter
  Integer numParallelThreads = 1;
  @Parameter
  Boolean checksOnly = false;
  @Parameter
  CompilerOptions.IncrementalCheckMode incrementalCheckMode =
      CompilerOptions.IncrementalCheckMode.OFF;
  @Parameter
  Boolean continueAfterErrors = false;
  @Parameter
  Boolean useTypesForOptimization = true;
  @Parameter
  Boolean assumeFunctionWrapper = false;
  //TODO set to default and implement method to handle parameter
  @Parameter
  WarningLevel warningLevel = WarningLevel.QUIET;
  @Parameter
  Boolean debug = false;
  @Parameter
  Boolean generateExports = false;
  @Parameter
  Boolean exportLocalPropertyDefinitions = false;
  // @Parameter
  // List<FormattingOption> formatting = new ArrayList<>();
  @Parameter
  Boolean processCommonJsModules = false;
  @Parameter
  List<String> commonJsPathPrefix = new ArrayList<>();
  @Parameter
  Boolean processClosurePrimitives = true;
  @Parameter
  Boolean angularPass = false;
  @Parameter
  Integer polymerVersion = null;
  @Parameter
  String polymerExportPolicy = PolymerExportPolicy.LEGACY.name();
  @Parameter
  Boolean chromePass = false;
  @Parameter
  Boolean dartPass = false;
  @Parameter
  String j2clPassMode = "AUTO";
  @Parameter
  String outputManifest = "";
  @Parameter
  String outputChunkDependencies = "";
  @Parameter
  String languageIn = "STABLE";
  @Parameter
  String languageOut = "STABLE";
  @Parameter
  Boolean version = false;
  @Parameter
  String translationsFile = "";
  @Parameter
  String translationsProject = null;
  @Parameter
  List<String> flagFiles = new ArrayList<>();
  @Parameter
  String warningsWhitelistFile = "";
  @Parameter
  List<String> hideWarningsFor = new ArrayList<>();
  @Parameter
  List<String> extraAnnotationName = new ArrayList<>();
  @Parameter
  CompilerOptions.TracerMode tracerMode = CompilerOptions.TracerMode.OFF;
  @Parameter
  String renamePrefix = null;
  @Parameter
  String renamePrefixNamespace = null;
  @Parameter
  List<String> conformanceConfigs = new ArrayList<>();
  @Parameter
  CompilerOptions.Environment environment = CompilerOptions.Environment.BROWSER;
  // @Parameter
  // CompilerOptions.JsonStreamMode jsonStreamMode = CompilerOptions.JsonStreamMode.NONE;
  @Parameter
  Boolean preserveTypeAnnotations = false;
  @Parameter
  Boolean injectLibraries = true;
  @Parameter
  List<String> forceInjectLibraries = new ArrayList<>();
  // @Parameter
  // DependencyModeFlag dependencyMode = null; // so we can tell whether it was explicitly
  @Parameter
  List<String> entryPoint = new ArrayList<>();
  @Parameter
  Boolean rewritePolyfills = true;
  @Parameter
  Boolean printSourceAfterEachPass = false;
  @Parameter
  ModuleLoader.ResolutionMode moduleResolutionMode = ModuleLoader.ResolutionMode.BROWSER;
  @Parameter
  Map<String, String> browserResolverPrefixReplacements = new HashMap<>();
  @Parameter
  String packageJsonEntryNames = null;
  // @Parameter
  // ErrorFormatOption errorFormat = ErrorFormatOption.STANDARD;
  @Parameter
  Boolean renaming = true;
  @Parameter
  Boolean helpMarkdown = false;
  @Parameter
  List<String> moduleRoot = new ArrayList<>();
}

