package qub;

public class QubTest
{
    public static void main(String[] args)
    {
        final QubTest qubTest = new QubTest();
        Console.run(args, qubTest::main);
    }

    private DefaultApplicationLauncher defaultApplicationLauncher;
    private Boolean showTotalDuration;

    /**
     * Set the JavaRunner that will be used to run tests.
     * @param javaRunner The JavaRunner that will be used to run tests.
     * @return This object for method chaining.
     */
    public QubTest setDefaultApplicationLauncher(DefaultApplicationLauncher javaRunner)
    {
        this.defaultApplicationLauncher = javaRunner;

        return this;
    }

    public DefaultApplicationLauncher getDefaultApplicationLauncher()
    {
        if (defaultApplicationLauncher == null)
        {
            defaultApplicationLauncher = new RealDefaultApplicationLauncher();
        }
        final DefaultApplicationLauncher result = defaultApplicationLauncher;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public QubTest setShowTotalDuration(boolean showTotalDuration)
    {
        this.showTotalDuration = showTotalDuration;
        return this;
    }

    public boolean getShowTotalDuration()
    {
        if (showTotalDuration == null)
        {
            showTotalDuration = true;
        }
        return showTotalDuration;
    }

    /**
     * Get the parameters for QubTest.run().
     * @param process The Process that is running.
     * @return The parameters for QubTest.run(), or null if QubTest.run() should not be run.
     */
    public static QubTestParameters getParameters(Process process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineParameters parameters = process.createCommandLineParameters()
            .setApplicationName("qub-test")
            .setApplicationDescription("Used to run tests in source code projects.");
        final CommandLineParameter<Folder> folderToTestParameter = parameters.addFolder("folder", process)
            .setValueName("<folder-to-test>")
            .setDescription("The folder to run tests in. Defaults to the current folder.");
        final CommandLineParameter<String> patternParameter = parameters.addString("pattern")
            .setValueName("<test-name-pattern>")
            .setDescription("The pattern to match against tests to determine if they will be run or not.");
        final CommandLineParameter<Coverage> coverageParameter = parameters.addEnum("coverage", Coverage.None, Coverage.Sources)
            .setValueRequired(false)
            .setValueName("<None|Sources|Tests|All>")
            .addAlias("c")
            .setDescription("Whether or not to collect code coverage information while running tests.");
        final CommandLineParameterBoolean testJsonParameter = parameters.addBoolean("testjson", true)
            .setDescription("Whether or not to write the test results to a test.json file.");
        final CommandLineParameter<String> jvmClassPathParameter = parameters.addString("jvm.classpath")
            .setDescription("The classpath that was passed to the JVM when this application was started.");
        final CommandLineParameterVerbose verboseParameter = parameters.addVerbose(process);
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(process, QubTest.class);
        final CommandLineParameterHelp helpParameter = parameters.addHelp();

        QubTestParameters result = null;
        if (!helpParameter.showApplicationHelpLines(process).await())
        {
            profilerParameter.await();
            profilerParameter.removeValue().await();

            final CharacterWriteStream output = process.getOutputCharacterWriteStream();
            final Folder folderToTest = folderToTestParameter.getValue().await();
            final EnvironmentVariables environmentVariables = process.getEnvironmentVariables();
            final ProcessFactory processFactory = process.getProcessFactory();
            final VerboseCharacterWriteStream verbose = verboseParameter.getVerboseCharacterWriteStream().await();

            result = new QubTestParameters(output, folderToTest, environmentVariables, processFactory, verbose)
                .setPattern(patternParameter.getValue().await())
                .setCoverage(coverageParameter.getValue().await())
                .setTestJson(testJsonParameter.getValue().await())
                .setJvmClassPath(jvmClassPathParameter.getValue().await())
                .setProfiler(profilerParameter.getValue().await());
        }

        return result;
    }

    public void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final QubTestParameters parameters = QubTest.getParameters(console);
        if (parameters != null)
        {
            final boolean showTotalDuration = getShowTotalDuration();
            final Stopwatch stopwatch = console.getStopwatch();
            if (showTotalDuration)
            {
                stopwatch.start();
            }
            try
            {
                console.setExitCode(QubBuild.run(QubBuild.getParameters(console)));

                if (console.getExitCode() == 0)
                {
                    console.writeLine("Running tests...").await();

                    final Folder folderToTest = parameters.getFolderToTest();
                    final String pattern = parameters.getPattern();
                    final Coverage coverage = parameters.getCoverage();
                    final VerboseCharacterWriteStream verbose = parameters.getVerbose();

                    final Folder outputFolder = folderToTest.getFolder("outputs").await();
                    final Folder sourceFolder = folderToTest.getFolder("sources").await();
                    final Folder testFolder = folderToTest.getFolder("tests").await();

                    final Folder coverageFolder = outputFolder.getFolder("coverage").await();

                    final List<String> classPaths = List.create(outputFolder.toString());

                    final File projectJsonFile = folderToTest.getFile("project.json").await();
                    final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).await();

                    final String qubHome = console.getEnvironmentVariable("QUB_HOME").await();
                    final Folder qubFolder = console.getFileSystem().getFolder(qubHome).await();
                    Iterable<Dependency> dependencies = projectJson.getJava().getDependencies();
                    if (!Iterable.isNullOrEmpty(dependencies))
                    {
                        dependencies = QubBuild.getAllDependencies(qubFolder, dependencies).getKeys();
                        classPaths.addAll(dependencies.map((Dependency dependency) ->
                        {
                            return QubBuild.resolveDependencyReference(qubFolder, dependency).toString();
                        }));
                    }

                    final String jvmClassPath = parameters.getJvmClassPath();
                    if (!Strings.isNullOrEmpty(jvmClassPath))
                    {
                        final String[] jvmClassPaths = jvmClassPath.split(";");
                        for (final String jvmClassPathString : jvmClassPaths)
                        {
                            final Path relativeJvmClassPath = Path.parse(jvmClassPathString).relativeTo(qubFolder);
                            final Indexable<String> segments = relativeJvmClassPath.getSegments();
                            final Dependency jvmDependency = new Dependency()
                                .setPublisher(segments.get(0))
                                .setProject(segments.get(1))
                                .setVersion(segments.get(2));
                            if (!QubTest.equal(jvmDependency, projectJson.getPublisher(), projectJson.getProject()) &&
                                (Iterable.isNullOrEmpty(dependencies) || !dependencies.contains(dep -> QubTest.equalIgnoreVersion(dep, jvmDependency))))
                            {
                                classPaths.add(jvmClassPathString);
                            }
                        }
                    }

                    Folder jacocoFolder = null;
                    if (coverage != Coverage.None)
                    {
                        jacocoFolder = qubFolder.getFolder("jacoco/jacococli").await()
                            .getFolders().await()
                            .maximum((Folder lhs, Folder rhs) -> VersionNumber.parse(lhs.getName()).compareTo(VersionNumber.parse(rhs.getName())));
                    }

                    final ConsoleTestRunnerProcessBuilder consoleTestRunner = ConsoleTestRunnerProcessBuilder.get(console).await()
                        .redirectOutput(console.getOutputByteWriteStream())
                        .redirectError(console.getErrorByteWriteStream());

                    if (jacocoFolder != null)
                    {
                        final File jacocoAgentJarFile = jacocoFolder.getFile("jacocoagent.jar").await();
                        final File coverageExecFile = outputFolder.getFile("coverage.exec").await();
                        consoleTestRunner.addJavaAgent(jacocoAgentJarFile + "=destfile=" + coverageExecFile);
                    }

                    consoleTestRunner.addClasspath(classPaths);
                    consoleTestRunner.addConsoleTestRunnerFullClassName();
                    consoleTestRunner.addProfiler(parameters.getProfiler());
                    if (verbose.isVerbose())
                    {
                        consoleTestRunner.addVerbose(true);
                    }
                    consoleTestRunner.addTestJson(parameters.getTestJson());

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
                        .map((File classFile) -> QubTest.getFullClassName(outputFolder, classFile)));

                    verbose.writeLine("Running " + consoleTestRunner.getCommand()).await();

                    console.writeLine().await();

                    console.setExitCode(consoleTestRunner.run().await());

                    if (jacocoFolder != null)
                    {
                        console.writeLine().await();
                        console.writeLine("Analyzing coverage...").await();

                        final JacocoCliProcessBuilder jacococli = JacocoCliProcessBuilder.get(console).await()
                            .addJacocoCliJar(jacocoFolder.getFile("jacococli.jar").await())
                            .addReport()
                            .addCoverageExec(outputFolder.getFile("coverage.exec").await())
                            .addClassFiles(QubTest.getClassFilesForCoverage(coverage, outputFolder, sourceFolder, testFolder))
                            .addSourceFiles(coverage, sourceFolder, testFolder)
                            .addHtml(coverageFolder);

                        if (verbose.isVerbose())
                        {
                            jacococli.redirectOutput(console.getOutputByteWriteStream());
                            jacococli.redirectError(console.getErrorByteWriteStream());

                            verbose.writeLine("Running " + jacococli.getCommand()).await();
                        }

                        final int coverageExitCode = jacococli.run().await();
                        if (console.getExitCode() == 0)
                        {
                            console.setExitCode(coverageExitCode);
                        }
                    }

                    if (jacocoFolder != null)
                    {
                        final DefaultApplicationLauncher defaultApplicationLauncher = this.getDefaultApplicationLauncher();
                        defaultApplicationLauncher.open(coverageFolder.getFile("index.html").await()).await();
                    }
                }
            }
            finally
            {
                if (showTotalDuration)
                {
                    final Duration compilationDuration = stopwatch.stop().toSeconds();
                    console.writeLine("Done (" + compilationDuration.toString("0.0") + ")").await();
                }
            }
        }
    }

    public static boolean equalIgnoreVersion(Dependency lhs, Dependency rhs)
    {
        PreCondition.assertNotNull(lhs, "lhs");
        PreCondition.assertNotNull(rhs, "rhs");

        return equal(lhs, rhs.getPublisher(), rhs.getProject());
    }

    public static boolean equal(Dependency dependency, String publisher, String project)
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
    public static boolean isSourceClassFile(Folder outputFolder, File outputClassFile, Folder sourceFolder, Iterable<File> sourceJavaFiles)
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

    public static Iterable<File> getSourceClassFiles(Folder outputFolder, Iterable<File> outputClassFiles, Folder sourceFolder, Iterable<File> sourceJavaFiles)
    {
        return outputClassFiles
            .where((File outputClassFile) ->
            {
                return QubTest.isSourceClassFile(outputFolder, outputClassFile, sourceFolder, sourceJavaFiles);
            });
    }

    /**
     * Get the full class name of the provided class file.
     * @param outputFolder The output folder that contains the class file.
     * @param classFile The class file to get the full class name of.
     * @return The full name of the class file.
     */
    public static String getFullClassName(Folder outputFolder, File classFile)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(classFile, "classFile");

        final Path classFileRelativePath = classFile.relativeTo(outputFolder);
        final String result = QubTest.getFullClassName(classFileRelativePath);

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    /**
     * Get the full class name of the class file at the provided relative path.
     * @param classFileRelativePath The path to the class file relative to the output folder.
     * @return The full name of the class file.
     */
    public static String getFullClassName(Path classFileRelativePath)
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

    public static File getClassFile(Folder outputFolder, String fullClassName)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNullAndNotEmpty(fullClassName, "fullClassName");

        final String testClassFileRelativePath = fullClassName.replace('.', '/') + ".class";
        final File result = outputFolder.getFile(testClassFileRelativePath).await();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    private static Result<Iterable<File>> getAllClassFiles(Folder outputFolder)
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

    private static Iterable<File> getClassFilesForCoverage(Coverage coverage, Folder outputFolder, Folder sourceFolder, Folder testFolder)
    {
        PreCondition.assertNotNull(coverage, "coverage");
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");
        PreCondition.assertNotNull(testFolder, "testFolder");

        Iterable<File> result;

        if (coverage == Coverage.All)
        {
            result = QubTest.getAllClassFiles(outputFolder).await();
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
                    final Iterable<File> allClassFiles = QubTest.getAllClassFiles(outputFolder).await();
                    allFolderClassFiles.addAll(QubTest.getSourceClassFiles(outputFolder, allClassFiles, folder, javaFiles));
                }
                result = allFolderClassFiles;
            }
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }
}