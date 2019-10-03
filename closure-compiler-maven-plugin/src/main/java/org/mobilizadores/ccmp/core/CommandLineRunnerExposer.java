package org.mobilizadores.ccmp.core;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import com.google.javascript.jscomp.CommandLineRunner;

/**
 * Makes use of {@link CommandLineRunner} implementation by holding an instance of it
 * and making heavy use of reflection to expose and call its private auto configuration methods.
 */
public class CommandLineRunnerExposer extends AbstractExposer<CommandLineRunner> {

  private FlagsExposer flags = 
      (FlagsExposer) new FlagsExposer("com.google.javascript.jscomp.CommandLineRunner$Flags").initializeAndExpose();
  private ConfigExposer config = 
      (ConfigExposer) new ConfigExposer("com.google.javascript.jscomp.AbstractCommandLineRunner$CommandLineConfig").initializeAndExpose();

  public CommandLineRunnerExposer(String className) {
    super(className);
  }
  /**
   * Assign the flags values to CommandLineRunner instance and process the flags. This method
   * does part of what {@link CommandLineRunner#initConfigFromFlags()} does. 
   * 
   * getCommandLineConfig()
          .setPrintTree(flags.printTree)
          .setPrintAst(flags.printAst)
          .setPrintPassGraph(flags.printPassGraph)
          .setJscompDevMode(flags.jscompDevMode)
          .setLoggingLevel(flags.loggingLevel)
          .setExterns(flags.externs)
          .setMixedJsSources(mixedSources)
          .setJsOutputFile(flags.jsOutputFile)
          .setSaveAfterChecksFileName(flags.saveAfterChecksFile)
          .setContinueSavedCompilationFileName(flags.continueSavedCompilationFile)
          .setModule(flags.chunk)
          .setVariableMapOutputFile(flags.variableMapOutputFile)
          .setCreateNameMapFiles(flags.createNameMapFiles)
          .setPropertyMapOutputFile(flags.propertyMapOutputFile)
          .setCodingConvention(conv)
          .setSummaryDetailLevel(flags.summaryDetailLevel)
          .setOutputWrapper(flags.outputWrapper)
          .setModuleWrapper(flags.chunkWrapper)
          .setModuleOutputPathPrefix(flags.chunkOutputPathPrefix)
          .setCreateSourceMap(flags.createSourceMap)
          .setSourceMapFormat(flags.sourceMapFormat)
          .setSourceMapLocationMappings(mappings)
          .setSourceMapInputFiles(sourceMapInputs)
          .setParseInlineSourceMaps(parseInlineSourceMaps)
          .setApplyInputSourceMaps(applyInputSourceMaps)
          .setWarningGuards(Flags.guardLevels)
          .setDefine(flags.define)
          .setBrowserFeaturesetYear(flags.browserFeaturesetYear)
          .setCharset(flags.charset)
          .setDependencyOptions(dependencyOptions)
          .setOutputManifest(ImmutableList.of(flags.outputManifest))
          .setOutputBundle(bundleFiles)
          .setSkipNormalOutputs(skipNormalOutputs)
          .setOutputModuleDependencies(flags.outputChunkDependencies)
          .setProcessCommonJSModules(flags.processCommonJsModules)
          .setModuleRoots(moduleRoots)
          .setTransformAMDToCJSModules(flags.transformAmdModules)
          .setWarningsWhitelistFile(flags.warningsWhitelistFile)
          .setHideWarningsFor(flags.hideWarningsFor)
          .setAngularPass(flags.angularPass)
          .setJsonStreamMode(flags.jsonStreamMode)
          .setErrorFormat(flags.errorFormat);
   */
  private void setAndProcessFlags() {
    try {
      FieldUtils.writeDeclaredField(this.privateObject, "flags", this.flags.get(), true);
      //MethodUtils.invokeMethod(this.privateObject, true, "processFlagFiles");
      //mixedSources = flags.getMixedJsSources();
      FieldUtils.writeDeclaredField(this.config.get(), 
                                     "mixedJsSources", 
                                     MethodUtils.invokeMethod(flags.get(), true, "getMixedJsSources"), // create FlagEntry instead
                                     true);
      //mappings = flags.getSourceMapLocationMappings();
      FieldUtils.writeDeclaredField(this.config.get(), 
                                    "sourceMapLocationMappings", 
                                     MethodUtils.invokeMethod(flags.get(), true, "getSourceMapLocationMappings"), 
                                     true);
      //sourceMapInputs = flags.getSourceMapInputs();
      FieldUtils.writeDeclaredField(this.config.get(), 
                                    "sourceMapInputFiles", 
                                    MethodUtils.invokeMethod(flags.get(), true, "getSourceMapInputs"), 
                                    true);
      //parseInlineSourceMaps = flags.parseInlineSourceMaps;
      FieldUtils.writeDeclaredField(this.config.get(),
                                    "parseInlineSourceMaps", 
                                    FieldUtils.getDeclaredField(flags.get().getClass(), "parseInlineSourceMaps", true), 
                                    true);
      //applyInputSourceMaps = flags.parseInlineSourceMaps;
      FieldUtils.writeDeclaredField(this.config.get(), 
                                    "applyInputSourceMaps", 
                                    FieldUtils.getDeclaredField(flags.get().getClass(), "parseInlineSourceMaps", true), 
                                    true);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
    }
  }
  
  public void run(String outputFile, List<String> inputFiles, DefaultMojo mojo) {
    try {
      // test: this order must be preserved, so 'special' args won't be overriden by simple args
      flags.setSimpleFlags(mojo.args);
      setComplexFlags(outputFile, inputFiles, mojo);
      
      setAndProcessFlags();
      MethodUtils.invokeMethod(this.privateObject, true, "run");
      
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private void setComplexFlags(String outputFile, List<String> inputFiles, DefaultMojo mojo) {
    try {
      FieldUtils.writeDeclaredField(flags.get(), "compilationLevelParsed", mojo.compilationLevel, true);
      FieldUtils.writeDeclaredField(flags.get(), "jsOutputFile", outputFile, true);
      FieldUtils.writeDeclaredField(flags.get(), "js", inputFiles, true);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * Instantiates a {@link CommandLineRunner} and sets its field 'flags'.
   */
  @Override
  public CommandLineRunner getPrivateObjectNewInstance(String className) {
    CommandLineRunner clr = null;
    try {
      Constructor<CommandLineRunner> constructor = CommandLineRunner.class.getDeclaredConstructor(String[].class, PrintStream.class, PrintStream.class);
      constructor.setAccessible(true);
      clr = constructor.newInstance(new String[]{}, System.out, System.err);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
    }
    return clr;
  }
  
}
