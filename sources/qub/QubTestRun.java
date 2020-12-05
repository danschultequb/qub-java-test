package qub;

public interface QubTestRun
{
    String actionName = "run";
    String actionDescription = "Run tests in a source code project.";

    static CommandLineParameter<Folder> addFolderToTestParameter(CommandLineParameters parameters, DesktopProcess process)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNull(process, "process");

        return parameters.addPositionalFolder("folder", process)
            .setValueName("<folder-to-test>")
            .setDescription("The folder to run tests in. Defaults to the current folder.");
    }

    static CommandLineParameter<String> addPatternParameter(CommandLineParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        return parameters.addString("pattern")
            .setValueName("<test-name-pattern>")
            .setDescription("The pattern to match against tests to determine if they will be run or not.");
    }

    static CommandLineParameter<Coverage> addCoverageParameter(CommandLineParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        return parameters.addEnum("coverage", QubTestRunParameters.getCoverageDefault(), Coverage.Sources)
            .setValueRequired(false)
            .setValueName("<None|Sources|Tests|All>")
            .addAlias("c")
            .setDescription("Whether or not to collect code coverage information while running tests.");
    }

    static CommandLineParameterBoolean addTestJsonParameter(CommandLineParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        return parameters.addBoolean("testjson", QubTestRunParameters.getTestJsonDefault())
            .setDescription("Whether or not to write the test results to a test.json file.");
    }

    /**
     * Get the parameters for QubTest.run().
     * @param process The Process that is running.
     * @return The parameters for QubTest.run(), or null if QubTest.run() should not be run.
     */
    static QubTestRunParameters getParameters(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final Folder projectDataFolder = process.getQubProjectDataFolder().await();
        return QubTestRun.getParameters(process, projectDataFolder);
    }

    /**
     * Get the parameters for QubTest.run().
     * @param process The Process that is running.
     * @return The parameters for QubTest.run(), or null if QubTest.run() should not be run.
     */
    static QubTestRunParameters getParameters(DesktopProcess process, Folder projectDataFolder)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(projectDataFolder, "projectDataFolder");

        final CommandLineParameters parameters = process.createCommandLineParameters()
            .setApplicationName(QubTest.getActionFullName(QubTestRun.actionName))
            .setApplicationDescription(QubTestRun.actionDescription);
        final CommandLineParameter<Folder> folderToTestParameter = QubTestRun.addFolderToTestParameter(parameters, process);
        final CommandLineParameter<String> patternParameter = QubTestRun.addPatternParameter(parameters);
        final CommandLineParameter<Coverage> coverageParameter = QubTestRun.addCoverageParameter(parameters);
        final CommandLineParameterBoolean testJsonParameter = QubTestRun.addTestJsonParameter(parameters);
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(process, QubTest.class);
        final CommandLineParameterHelp helpParameter = parameters.addHelp();

        QubTestRunParameters result = null;
        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            profilerParameter.await();
            profilerParameter.removeValue().await();

            final CharacterToByteReadStream input = process.getInputReadStream();
            final CharacterToByteWriteStream output = process.getOutputWriteStream();
            final CharacterToByteWriteStream error = process.getErrorWriteStream();
            final DefaultApplicationLauncher defaultApplicationLauncher = process.getDefaultApplicationLauncher();
            final Folder folderToTest = folderToTestParameter.getValue().await();
            final EnvironmentVariables environmentVariables = process.getEnvironmentVariables();
            final ProcessFactory processFactory = process.getProcessFactory();
            final VerboseCharacterToByteWriteStream verbose = verboseParameter.getVerboseCharacterToByteWriteStream().await();
            final boolean profiler = profilerParameter.getValue().await();
            final String jvmClassPath = process.getJVMClasspath().await();
            final TypeLoader typeLoader = process.getTypeLoader();

            result = new QubTestRunParameters(input, output, error, folderToTest, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, projectDataFolder, typeLoader)
                .setPattern(patternParameter.removeValue().await())
                .setCoverage(coverageParameter.removeValue().await())
                .setTestJson(testJsonParameter.removeValue().await())
                .setVerbose(verbose)
                .setProfiler(profiler);
        }

        return result;
    }

    static int run(QubTestRunParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");

        final Folder folderToTest = parameters.getFolderToTest();
        final String pattern = parameters.getPattern();
        final Coverage coverage = parameters.getCoverage();
        final CharacterToByteReadStream inputReadStream = parameters.getInputReadStream();
        final CharacterToByteWriteStream parametersOutput = parameters.getOutputWriteStream();
        final CharacterToByteWriteStream parametersError = parameters.getErrorWriteStream();
        final VerboseCharacterToByteWriteStream parametersVerbose = parameters.getVerbose();
        final DefaultApplicationLauncher defaultApplicationLauncher = parameters.getDefaultApplicationLauncher();
        final EnvironmentVariables environmentVariables = parameters.getEnvironmentVariables();
        final ProcessFactory processFactory = parameters.getProcessFactory();
        final boolean profiler = parameters.getProfiler();
        final boolean testJson = parameters.getTestJson();
        final Folder qubTestDataFolder = parameters.getQubTestDataFolder();

        LogStreams logStreams = CommandLineLogsAction.addLogStream(qubTestDataFolder, parametersOutput, parametersVerbose);
        CharacterToByteWriteStream output = logStreams.getOutput();
        VerboseCharacterToByteWriteStream verbose = logStreams.getVerbose();

        int result;
        try
        {
            result = QubBuildCompile.run(parameters);
            if (result == 0)
            {
                output.writeLine("Running tests...").await();

                final Folder outputFolder = folderToTest.getFolder("outputs").await();
                final Folder sourceFolder = folderToTest.getFolder("sources").await();
                final Folder testFolder = folderToTest.getFolder("tests").await();

                final Folder coverageFolder = outputFolder.getFolder("coverage").await();

                final List<String> classPaths = List.create(outputFolder.toString());

                final File projectJsonFile = folderToTest.getFile("project.json").await();
                final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).await();
                final ProjectJSONJava projectJsonJava = projectJson.getJava();

                final String qubHome = environmentVariables.get("QUB_HOME").await();
                final QubFolder qubFolder = QubFolder.get(folderToTest.getFileSystem().getFolder(qubHome).await());
                Iterable<ProjectSignature> dependencies = projectJsonJava.getDependencies();
                if (!Iterable.isNullOrEmpty(dependencies))
                {
                    dependencies = projectJsonJava.getTransitiveDependencies(qubFolder);
                    classPaths.addAll(dependencies.map((ProjectSignature dependency) ->
                    {
                        final String publisher = dependency.getPublisher();
                        final String project = dependency.getProject();
                        final VersionNumber version = dependency.getVersion();
                        final File compiledSourcesFile = qubFolder.getCompiledSourcesFile(publisher, project, version).await();
                        return compiledSourcesFile.toString();
                    }));
                }

                final String jvmClassPath = parameters.getJvmClassPath();
                if (!Strings.isNullOrEmpty(jvmClassPath))
                {
                    final String[] jvmClassPaths = jvmClassPath.split(";");
                    for (final String jvmClassPathString : jvmClassPaths)
                    {
                        boolean addJvmClassPathString;
                        if (!qubFolder.isAncestorOf(jvmClassPathString).await())
                        {
                            addJvmClassPathString = !classPaths.contains(jvmClassPathString);
                        }
                        else
                        {
                            final Path relativeJvmClassPath = Path.parse(jvmClassPathString).relativeTo(qubFolder);
                            final Indexable<String> segments = relativeJvmClassPath.getSegments();
                            final ProjectSignature jvmProjectSignature = ProjectSignature.create(segments.get(0), segments.get(1), segments.get(2));
                            addJvmClassPathString = !QubTestRun.equal(jvmProjectSignature, projectJson.getPublisher(), projectJson.getProject()) &&
                                (Iterable.isNullOrEmpty(dependencies) || !dependencies.contains(jvmProjectSignature::equalsIgnoreVersion));
                        }

                        if (addJvmClassPathString)
                        {
                            classPaths.addAll(jvmClassPathString);
                        }
                    }
                }

                Folder jacocoFolder = null;
                if (coverage != Coverage.None)
                {
                    final QubProjectFolder jacococliProjectFolder = qubFolder.getProjectFolder("jacoco", "jacococli").await();
                    jacocoFolder = jacococliProjectFolder.getLatestProjectVersionFolder().await();
                }

                final ConsoleTestRunnerProcessBuilder consoleTestRunner = ConsoleTestRunnerProcessBuilder.create(processFactory).await()
                    .redirectInput(inputReadStream)
                    .redirectOutput(parametersOutput)
                    .redirectError(parametersError);

                if (jacocoFolder != null)
                {
                    final File jacocoAgentJarFile = jacocoFolder.getFile("jacocoagent.jar").await();
                    final File coverageExecFile = outputFolder.getFile("coverage.exec").await();
                    consoleTestRunner.addJavaAgent(jacocoAgentJarFile + "=destfile=" + coverageExecFile);
                }

                consoleTestRunner.addClasspath(classPaths);
                consoleTestRunner.addConsoleTestRunnerFullClassName();
                consoleTestRunner.addProfiler(profiler);
                consoleTestRunner.addVerbose(parametersVerbose.isVerbose());
                consoleTestRunner.addTestJson(testJson);
                consoleTestRunner.addLogFile(logStreams.getLogFile());

                if (!Strings.isNullOrEmpty(pattern))
                {
                    consoleTestRunner.addPattern(pattern);
                }

                consoleTestRunner.addOutputFolder(outputFolder);

                if (coverage != null)
                {
                    consoleTestRunner.addArgument("--coverage=" + coverage);
                }

                consoleTestRunner.addArguments(outputFolder.getFilesRecursively()
                    .catchError(FolderNotFoundException.class, () -> Iterable.create())
                    .await()
                    .where((File file) -> Comparer.equal(file.getFileExtension(), ".class"))
                    .map((File classFile) -> QubTestRun.getFullClassName(outputFolder, classFile)));

                verbose.writeLine("Running " + consoleTestRunner.getCommand()).await();

                output.writeLine().await();

                logStreams.getLogStream().dispose().await();

                result = consoleTestRunner.run().await();

                logStreams = CommandLineLogsAction.addLogStream(logStreams.getLogFile(), parametersOutput, parametersVerbose);
                output = logStreams.getOutput();
                verbose = logStreams.getVerbose();

                if (jacocoFolder != null)
                {
                    output.writeLine().await();
                    output.writeLine("Analyzing coverage...").await();

                    final JacocoCliProcessBuilder jacococli = JacocoCliProcessBuilder.create(processFactory).await()
                        .addJacocoCliJar(jacocoFolder.getFile("jacococli.jar").await())
                        .addReport()
                        .addCoverageExec(outputFolder.getFile("coverage.exec").await())
                        .addClassFiles(QubTestRun.getClassFilesForCoverage(coverage, outputFolder, sourceFolder, testFolder))
                        .addSourceFiles(coverage, sourceFolder, testFolder)
                        .addHtml(coverageFolder);

                    if (parametersVerbose.isVerbose())
                    {
                        jacococli.redirectOutput(parametersOutput);
                        jacococli.redirectError(parametersError);

                        verbose.writeLine("Running " + jacococli.getCommand()).await();
                    }

                    final int coverageExitCode = jacococli.run().await();
                    if (result == 0)
                    {
                        result = coverageExitCode;
                    }
                }

                if (jacocoFolder != null)
                {
                    defaultApplicationLauncher.openFileWithDefaultApplication(coverageFolder.getFile("index.html").await()).await();
                }
            }
        }
        finally
        {
            logStreams.getLogStream().dispose().await();
        }

        return result;
    }

    static boolean equal(ProjectSignature dependency, String publisher, String project)
    {
        PreCondition.assertNotNull(dependency, "dependency");

        return Comparer.equal(dependency.getPublisher(), publisher) &&
            Comparer.equal(dependency.getProject(), project);
    }

    /**
     * Get whether or not the provided outputClassFile was created from one of the provided java
     * files.
     * @param outputFolder The output folder that the outputClassFiles are entries of.
     * @param outputClassFile The outputClassFile to check.
     * @param sourceFolder The source folder that the sourceJavaFiles are entries of.
     * @param sourceJavaFiles The possible source files that the class file may have come from.
     * @return Whether or not the provided outputClassFile was created from one of the provided java
     * files.
     */
    static boolean isSourceClassFile(Folder outputFolder, File outputClassFile, Folder sourceFolder, Iterable<File> sourceJavaFiles)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(outputClassFile, "outputClassFile");
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");
        PreCondition.assertNotNull(sourceJavaFiles, "sourceJavaFiles");

        Path outputClassFilePath = outputClassFile.relativeTo(outputFolder).withoutFileExtension();
        if (outputClassFilePath.getSegments().last().contains("$"))
        {
            final String outputClassFileRelativePathString = outputClassFilePath.toString();
            final int dollarSignIndex = outputClassFileRelativePathString.lastIndexOf('$');
            final String outputClassFileRelativePathStringWithoutDollarSign = outputClassFileRelativePathString.substring(0, dollarSignIndex);
            outputClassFilePath = Path.parse(outputClassFileRelativePathStringWithoutDollarSign);
        }
        final Path outputClassFileRelativePath = outputClassFilePath;
        return sourceJavaFiles.contains((File sourceJavaFile) ->
        {
            final Path sourceJavaFileRelativePath = sourceJavaFile.relativeTo(sourceFolder).withoutFileExtension();
            return outputClassFileRelativePath.equals(sourceJavaFileRelativePath);
        });
    }

    static Iterable<File> getSourceClassFiles(Folder outputFolder, Iterable<File> outputClassFiles, Folder sourceFolder, Iterable<File> sourceJavaFiles)
    {
        return outputClassFiles
            .where((File outputClassFile) ->
            {
                return QubTestRun.isSourceClassFile(outputFolder, outputClassFile, sourceFolder, sourceJavaFiles);
            });
    }

    /**
     * Get the full class name of the provided class file.
     * @param outputFolder The output folder that contains the class file.
     * @param classFile The class file to get the full class name of.
     * @return The full name of the class file.
     */
    static String getFullClassName(Folder outputFolder, File classFile)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(classFile, "classFile");

        final Path classFileRelativePath = classFile.relativeTo(outputFolder);
        final String result = QubTestRun.getFullClassName(classFileRelativePath);

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    /**
     * Get the full class name of the class file at the provided relative path.
     * @param classFileRelativePath The path to the class file relative to the output folder.
     * @return The full name of the class file.
     */
    static String getFullClassName(Path classFileRelativePath)
    {
        PreCondition.assertNotNull(classFileRelativePath, "classFileRelativePath");
        PreCondition.assertFalse(classFileRelativePath.isRooted(), "classFileRelativePath.isRooted()");
        PreCondition.assertEqual(".class", classFileRelativePath.getFileExtension(), "classFileRelativePath.getFileExtension()");

        final String result = classFileRelativePath
            .withoutFileExtension()
            .toString()
            .replace('/', '.')
            .replace('\\', '.');

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    static File getClassFile(Folder outputFolder, String fullClassName)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNullAndNotEmpty(fullClassName, "fullClassName");

        final String testClassFileRelativePath = fullClassName.replace('.', '/') + ".class";
        final File result = outputFolder.getFile(testClassFileRelativePath).await();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    static Result<Iterable<File>> getAllClassFiles(Folder outputFolder)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");

        return Result.create(() ->
        {
            final Iterable<File> allOutputFiles = outputFolder.getFilesRecursively()
                .catchError(FolderNotFoundException.class, () -> Iterable.create())
                .await();
            return allOutputFiles.where((File file) -> Comparer.equal(file.getFileExtension(), ".class"));
        });
    }

    static Iterable<File> getClassFilesForCoverage(Coverage coverage, Folder outputFolder, Folder sourceFolder, Folder testFolder)
    {
        PreCondition.assertNotNull(coverage, "coverage");
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");
        PreCondition.assertNotNull(testFolder, "testFolder");

        Iterable<File> result;

        if (coverage == Coverage.All)
        {
            result = QubTestRun.getAllClassFiles(outputFolder).await();
        }
        else
        {
            Folder folder = null;
            if (coverage == Coverage.Sources)
            {
                folder = sourceFolder;
            }
            else if (coverage == Coverage.Tests)
            {
                folder = testFolder;
            }

            if (folder == null)
            {
                result = Iterable.create();
            }
            else
            {
                final List<File> allFolderClassFiles = List.create();
                final Iterable<File> javaFiles = folder.getFilesRecursively()
                        .catchError(FolderNotFoundException.class, () -> Iterable.create())
                        .await()
                        .where((File file) -> Comparer.equal(file.getFileExtension(), ".java"));
                if (javaFiles.any())
                {
                    final Iterable<File> allClassFiles = QubTestRun.getAllClassFiles(outputFolder).await();
                    allFolderClassFiles.addAll(QubTestRun.getSourceClassFiles(outputFolder, allClassFiles, folder, javaFiles));
                }
                result = allFolderClassFiles;
            }
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }
}
