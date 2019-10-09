package org.mobilizadores.ccmp.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.lang3.ClassUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.kohsuke.args4j.Option;
import com.google.javascript.jscomp.CompilationLevel;
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
public class DefaultMojo extends AbstractMojo implements Observer {
  
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
  @Parameter(alias = "jsOutputFile")
  File outputFile;

//  @Parameter
//  Map<String, Object> args;

  FilesHandler filesHandler = new FilesHandler();
  Object lock = new Object();
  StealingSecurityManager securityManager = new StealingSecurityManager();
  
  public DefaultMojo() {
    super();
    System.setSecurityManager(this.securityManager);
  }
 

  public void execute() throws MojoExecutionException {
    if (this.inputDirectory == null && this.includeFiles == null)
      throw new MojoExecutionException(
          "Either parameter 'includeFiles' or 'inputDirectory' must be specified");
    
    List<File> effectiveInputFilesList = this.filesHandler.getEffectiveInputFilesList(this.inputDirectory, this.includeFiles, this.failOnNoInputFilesFound);
    ExecutorService executorService = Executors.newFixedThreadPool(this.maxNumberOfThreads);
    if (this.outputFile == null) {
          effectiveInputFilesList.stream().forEach(file -> {
          try {
            Set<String> fileList = this.filesHandler.getFileWithDepsList(file);
            if(fileList.size() > 0) {
              String outputFilePath = this.outputDirectory.getAbsolutePath() + File.separator + this.filesHandler.getResultFileRelativePath(file, this.suffix);
              RunClosureCompiler compilerRunner = new RunClosureCompiler(getCommandLine(outputFilePath, fileList.toArray(new String[]{}) ), this.lock);
              compilerRunner.addObserver(this);
              executorService.execute(compilerRunner);
              //TODO put file in respective relative path
              //TODO make compiler use relative path for module name
              //TODO execute so WAR file without overriding compressed files without suffix
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
          RunClosureCompiler compilerRunner = new RunClosureCompiler(getCommandLine(this.outputFile.getPath(), inputArray), this.lock);
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

  @Override
  public void update(Observable o, Object obj) {
    if(obj != null) {   
      Notification notif = (Notification) obj;
      int pos = IntStream.range(0, notif.getArgs().length).filter(i -> "--js_output_file".equals(notif.getArgs()[i])).findFirst().getAsInt();
      getLog().info(notif.getDescription() + " to: " + notif.getArgs()[pos + 1]);
    }
  }
  
  String[] getCommandLine(String outputFile, String... inputFiles) {
    List<String> commandList = new ArrayList<>();
    try {
      Class<?> flagsClass = Class.forName("com.google.javascript.jscomp.CommandLineRunner$Flags");
      commandList.addAll( getPrimitiveArgs(flagsClass));
      
      commandList.add("--js_module_root");
      commandList.add(this.inputDirectory.getPath());
      commandList.add("--js_output_file");
      commandList.add(outputFile);
      Arrays.asList(inputFiles).forEach(file -> {
        commandList.add("--js");
        commandList.add(file);
      });
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return commandList.toArray(new String[] {});
  }


  List<String>  getPrimitiveArgs(Class<?> flagsClass) {
    List<String> commandList = new ArrayList<String>();
    Field[] parameters = DefaultMojo.class.getDeclaredFields();
    parameters[0].isAnnotationPresent(Parameter.class);
    Arrays.asList(parameters).stream().forEach(parameter -> {
      if(ClassUtils.isPrimitiveOrWrapper(parameter.getType())) {
        if (parameter.isAnnotationPresent(Parameter.class)) {
          try {
            Field flag = flagsClass.getDeclaredField(parameter.getAnnotation(Parameter.class).alias());
            if (flag.isAnnotationPresent(Option.class)) {
              commandList.add(flag.getDeclaredAnnotation(Option.class).name());
              commandList.add(String.valueOf(parameter.get(this)));
            }
          } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
              | IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    });
    return commandList;
  }
  
  
  /*
   * ***************************************
   * PRIMITIVE AND WRAPPER PROPERTIES
   * ***************************************/
  
  @Parameter
  Integer browserFeaturesetYear = 0;
  @Parameter
  boolean displayHelp = false;
  @Parameter
  boolean printTree = false;
  @Parameter
  boolean printAst = false;
  @Parameter
  boolean printPassGraph = false;
  @Parameter
  boolean emitUseStrict = true;
  @Parameter
  boolean strictModeInput = true;
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
  boolean createNameMapFiles = false;
  @Parameter
  boolean sourceMapIncludeSourcesContent = false;
  @Parameter
  String propertyMapOutputFile = "";
  @Parameter
  boolean thirdParty = false;
  @Parameter
  int summaryDetailLevel = 1;
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
  boolean applyInputSourceMaps = true;
  @Parameter
  List<String> jscompError = new ArrayList<>();
  @Parameter
  List<String> jscompWarning = new ArrayList<>();
  @Parameter
  List<String> jscompOff = new ArrayList<>();
  @Parameter
  List<String> define = new ArrayList<>();
  @Parameter
  String charset = "";
  @Parameter
  String compilationLevel = "SIMPLE";
  @Parameter
  CompilationLevel compilationLevelParsed = null;
  @Parameter
  int numParallelThreads = 1;
  @Parameter
  boolean checksOnly = false;
  @Parameter
  CompilerOptions.IncrementalCheckMode incrementalCheckMode =
      CompilerOptions.IncrementalCheckMode.OFF;
  @Parameter
  boolean continueAfterErrors = false;
  @Parameter
  boolean useTypesForOptimization = true;
  @Parameter
  boolean assumeFunctionWrapper = false;
  @Parameter
  WarningLevel warningLevel = WarningLevel.DEFAULT;
  @Parameter
  boolean debug = false;
  @Parameter
  boolean generateExports = false;
  @Parameter
  boolean exportLocalPropertyDefinitions = false;
  // @Parameter
  // List<FormattingOption> formatting = new ArrayList<>();
  @Parameter
  boolean processCommonJsModules = false;
  @Parameter
  List<String> commonJsPathPrefix = new ArrayList<>();
  @Parameter
  List<String> moduleRoot = new ArrayList<>();
  @Parameter
  @Deprecated
  String commonJsEntryModule;
  @Parameter
  @Deprecated
  boolean transformAmdModules = false;
  @Parameter
  boolean processClosurePrimitives = true;
  @Parameter
  @Deprecated
  boolean manageClosureDependencies = false;
  @Parameter
  @Deprecated
  boolean onlyClosureDependencies = false;
  @Parameter
  @Deprecated
  List<String> closureEntryPoint = new ArrayList<>();
  @Parameter
  boolean angularPass = false;
  @Parameter
  @Deprecated
  boolean polymerPass = false;
  @Parameter
  Integer polymerVersion = null;
  @Parameter
  String polymerExportPolicy = PolymerExportPolicy.LEGACY.name();
  @Parameter
  boolean chromePass = false;
  @Parameter
  boolean dartPass = false;
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
  boolean version = false;
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
  boolean preserveTypeAnnotations = false;
  @Parameter
  boolean injectLibraries = true;
  @Parameter
  List<String> forceInjectLibraries = new ArrayList<>();
  // @Parameter
  // DependencyModeFlag dependencyMode = null; // so we can tell whether it was explicitly
  @Parameter
  List<String> entryPoint = new ArrayList<>();
  @Parameter
  boolean rewritePolyfills = true;
  @Parameter
  boolean printSourceAfterEachPass = false;
  @Parameter
  ModuleLoader.ResolutionMode moduleResolutionMode = ModuleLoader.ResolutionMode.BROWSER;
  @Parameter
  Map<String, String> browserResolverPrefixReplacements = new HashMap<>();
  @Parameter
  String packageJsonEntryNames = null;
  // @Parameter
  // ErrorFormatOption errorFormat = ErrorFormatOption.STANDARD;
  @Parameter
  boolean renaming = true;
  @Parameter
  boolean helpMarkdown = false;
}

