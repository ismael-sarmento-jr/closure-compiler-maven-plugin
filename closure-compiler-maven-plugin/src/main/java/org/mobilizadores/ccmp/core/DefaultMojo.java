package org.mobilizadores.ccmp.core;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.kohsuke.args4j.Option;
import com.google.common.io.Files;
import com.google.javascript.jscomp.CommandLineRunner;
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
@Mojo(name = "compress", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class DefaultMojo extends AbstractMojo {

  @Parameter(defaultValue = "true")
  Boolean failOnNoInputFilesFound;

  @Parameter
  File inputDirectory;

  @Parameter(alias = "js")
  List<File> includeFiles;

  @Parameter(defaultValue = "target/js/")
  File outputDirectory;

  // @Parameter(alias = "level", defaultValue = "SIMPLE_OPTIMIZATIONS", required = true)
  // CompilationLevel compilationLevel;

  /**
   * If the outputFile is not specified, then the files are going to be minified seperately.
   */
  @Parameter(alias = "jsOutputFile")
  String outputFile;

  @Parameter
  Map<String, Object> args;

  @Parameter
  private Integer browserFeaturesetYear = 0;
  @Parameter
  private boolean displayHelp = false;
  @Parameter
  private boolean printTree = false;
  @Parameter
  private boolean printAst = false;
  @Parameter
  private boolean printPassGraph = false;
  @Parameter
  private boolean emitUseStrict = true;
  @Parameter
  private boolean strictModeInput = true;
  @Parameter
  private CompilerOptions.DevMode jscompDevMode = CompilerOptions.DevMode.OFF;
  @Parameter
  private String loggingLevel = Level.WARNING.getName();
  @Parameter
  private List<String> externs = new ArrayList<>();
  @Parameter
  private List<String> unusedJsZip = null;
  @Parameter
  private List<String> chunk = new ArrayList<>();
  @Parameter
  private String continueSavedCompilationFile = null;
  @Parameter
  private String saveAfterChecksFile = null;
  @Parameter
  private String variableMapOutputFile = "";
  @Parameter
  private boolean createNameMapFiles = false;
  @Parameter
  private boolean sourceMapIncludeSourcesContent = false;
  @Parameter
  private String propertyMapOutputFile = "";
  @Parameter
  private boolean thirdParty = false;
  @Parameter
  private int summaryDetailLevel = 1;
  @Parameter
  private IsolationMode isolationMode = IsolationMode.NONE;
  @Parameter
  private String outputWrapper = "";
  @Parameter
  private String outputWrapperFile = "";
  @Parameter
  private List<String> chunkWrapper = new ArrayList<>();
  @Parameter
  private String chunkOutputPathPrefix = "./";
  @Parameter
  private String createSourceMap = "";
  @Parameter
  private SourceMap.Format sourceMapFormat = SourceMap.Format.DEFAULT;
  @Parameter
  private List<String> sourceMapLocationMapping = new ArrayList<>();
  @Parameter
  private List<String> sourceMapInputs = new ArrayList<>();
  @Parameter
  private Boolean parseInlineSourceMaps = true;
  @Parameter
  private boolean applyInputSourceMaps = true;
  @Parameter
  private List<String> jscompError = new ArrayList<>();
  @Parameter
  private List<String> jscompWarning = new ArrayList<>();
  @Parameter
  private List<String> jscompOff = new ArrayList<>();
  @Parameter
  private List<String> define = new ArrayList<>();
  @Parameter
  private String charset = "";
  @Parameter
  private String compilationLevel = "SIMPLE";
  @Parameter
  private CompilationLevel compilationLevelParsed = null;
  @Parameter
  private int numParallelThreads = 1;
  @Parameter
  private boolean checksOnly = false;
  @Parameter
  private CompilerOptions.IncrementalCheckMode incrementalCheckMode =
      CompilerOptions.IncrementalCheckMode.OFF;
  @Parameter
  private boolean continueAfterErrors = false;
  @Parameter
  private boolean useTypesForOptimization = true;
  @Parameter
  private boolean assumeFunctionWrapper = false;
  @Parameter
  private WarningLevel warningLevel = WarningLevel.DEFAULT;
  @Parameter
  private boolean debug = false;
  @Parameter
  private boolean generateExports = false;
  @Parameter
  private boolean exportLocalPropertyDefinitions = false;
  // @Parameter
  // private List<FormattingOption> formatting = new ArrayList<>();
  @Parameter
  private boolean processCommonJsModules = false;
  @Parameter
  private List<String> commonJsPathPrefix = new ArrayList<>();
  @Parameter
  private List<String> moduleRoot = new ArrayList<>();
  @Parameter
  @Deprecated
  private String commonJsEntryModule;
  @Parameter
  @Deprecated
  private boolean transformAmdModules = false;
  @Parameter
  private boolean processClosurePrimitives = true;
  @Parameter
  @Deprecated
  private boolean manageClosureDependencies = false;
  @Parameter
  @Deprecated
  private boolean onlyClosureDependencies = false;
  @Parameter
  @Deprecated
  private List<String> closureEntryPoint = new ArrayList<>();
  @Parameter
  private boolean angularPass = false;
  @Parameter
  @Deprecated
  private boolean polymerPass = false;
  @Parameter
  private Integer polymerVersion = null;
  @Parameter
  private String polymerExportPolicy = PolymerExportPolicy.LEGACY.name();
  @Parameter
  private boolean chromePass = false;
  @Parameter
  private boolean dartPass = false;
  @Parameter
  private String j2clPassMode = "AUTO";
  @Parameter
  private String outputManifest = "";
  @Parameter
  private String outputChunkDependencies = "";
  @Parameter
  private String languageIn = "STABLE";
  @Parameter
  private String languageOut = "STABLE";
  @Parameter
  private boolean version = false;
  @Parameter
  private String translationsFile = "";
  @Parameter
  private String translationsProject = null;
  @Parameter
  private List<String> flagFiles = new ArrayList<>();
  @Parameter
  private String warningsWhitelistFile = "";
  @Parameter
  private List<String> hideWarningsFor = new ArrayList<>();
  @Parameter
  private List<String> extraAnnotationName = new ArrayList<>();
  @Parameter
  private CompilerOptions.TracerMode tracerMode = CompilerOptions.TracerMode.OFF;
  @Parameter
  private String renamePrefix = null;
  @Parameter
  private String renamePrefixNamespace = null;
  @Parameter
  private List<String> conformanceConfigs = new ArrayList<>();
  @Parameter
  private CompilerOptions.Environment environment = CompilerOptions.Environment.BROWSER;
  // @Parameter
  // private CompilerOptions.JsonStreamMode jsonStreamMode = CompilerOptions.JsonStreamMode.NONE;
  @Parameter
  private boolean preserveTypeAnnotations = false;
  @Parameter
  private boolean injectLibraries = true;
  @Parameter
  private List<String> forceInjectLibraries = new ArrayList<>();
  // @Parameter
  // private DependencyModeFlag dependencyMode = null; // so we can tell whether it was explicitly
  @Parameter
  private List<String> entryPoint = new ArrayList<>();
  @Parameter
  private boolean rewritePolyfills = true;
  @Parameter
  private boolean printSourceAfterEachPass = false;
  @Parameter
  private ModuleLoader.ResolutionMode moduleResolutionMode = ModuleLoader.ResolutionMode.BROWSER;
  @Parameter
  private Map<String, String> browserResolverPrefixReplacements = new HashMap<>();
  @Parameter
  private String packageJsonEntryNames = null;
  // @Parameter
  // private ErrorFormatOption errorFormat = ErrorFormatOption.STANDARD;
  @Parameter
  private boolean renaming = true;
  @Parameter
  private boolean helpMarkdown = false;


  public void execute() throws MojoExecutionException {
    if (this.inputDirectory == null && this.includeFiles == null)
      throw new MojoExecutionException(
          "Either parameter 'includeFiles' or 'inputDirectory' must be specified");

    mergeIncludeFilesList();
    if (this.outputFile == null) {
      this.includeFiles.stream().forEach(file -> {
        runCommand(getCommandLine(this.outputDirectory.getAbsolutePath() + "/" + file.getName(), file.getPath()));
      });
    } else {
      runCommand(getCommandLine(this.outputFile, this.includeFiles.toArray(new String[] {})));
    }
  }


  private void runCommand(String[] commandLine) {
    try {
      MethodUtils.invokeMethod(getCommandLineRunnerNewInstance(commandLine), true, "run");
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private String[] getCommandLine(String outputFile, String... inputFiles) {
    List<String> commandList = new ArrayList<>();
    try {
      Class<?> flagsClass = Class.forName("com.google.javascript.jscomp.CommandLineRunner$Flags");
      commandList.addAll( getPrimitiveArgs(flagsClass));
      
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


  private List<String>  getPrimitiveArgs(Class<?> flagsClass) {
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
  
  public static void main(String[] args) {
    new DefaultMojo().getPrimitiveArgs(null);
  }

  /**
   * Finds javascript files in the inputDirectory and adds them to the includeFiles list.
   */
  private void mergeIncludeFilesList() throws MojoExecutionException {
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

  public CommandLineRunner getCommandLineRunnerNewInstance(String[] args) {
    CommandLineRunner clr = null;
    try {
      Constructor<CommandLineRunner> constructor = CommandLineRunner.class
          .getDeclaredConstructor(String[].class, PrintStream.class, PrintStream.class);
      constructor.setAccessible(true);
      clr = constructor.newInstance(args, System.out, System.err);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
    }
    return clr;
  }
}

