package qub;

public interface QubTestRunTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubTestRun.class, () ->
        {
            runner.testGroup("getParameters(DesktopProcess,CommandLineAction)", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    final DesktopProcess process = null;
                    final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {});
                    test.assertThrows(() -> QubTestRun.getParameters(process, action),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null action", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final CommandLineAction action = null;
                        test.assertThrows(() -> QubTestRun.getParameters(process, action),
                            new PreConditionFailure("action cannot be null."));
                    }
                });

                runner.test("with --help", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create("--help"))
                    {
                        final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {})
                            .setDescription("fake-description");

                        test.assertNull(QubTestRun.getParameters(process, action));

                        test.assertEqual(
                            Iterable.create(
                                "Usage: fake-action-name [[--folder=]<folder-to-test>] [--pattern=<test-name-pattern>] [--coverage[=<None|Sources|Tests|All>]] [--testjson] [--verbose] [--profiler] [--help]",
                                "  fake-description",
                                "  --folder:      The folder to run tests in. Defaults to the current folder.",
                                "  --pattern:     The pattern to match against tests to determine if they will be run or not.",
                                "  --coverage(c): Whether or not to collect code coverage information while running tests.",
                                "  --testjson:    Whether or not to write the test results to a test.json file.",
                                "  --verbose(v):  Whether or not to show verbose logs.",
                                "  --profiler:    Whether or not this application should pause before it is run to allow a profiler to be attached.",
                                "  --help(?):     Show the help message for this application."),
                            Strings.getLines(process.getOutputWriteStream().getText().await()));
                    }
                });

                runner.test("with --?", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create("--?"))
                    {
                        final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {})
                            .setDescription("fake-description");

                        test.assertNull(QubTestRun.getParameters(process, action));

                        test.assertEqual(
                            Iterable.create(
                                "Usage: fake-action-name [[--folder=]<folder-to-test>] [--pattern=<test-name-pattern>] [--coverage[=<None|Sources|Tests|All>]] [--testjson] [--verbose] [--profiler] [--help]",
                                "  fake-description",
                                "  --folder:      The folder to run tests in. Defaults to the current folder.",
                                "  --pattern:     The pattern to match against tests to determine if they will be run or not.",
                                "  --coverage(c): Whether or not to collect code coverage information while running tests.",
                                "  --testjson:    Whether or not to write the test results to a test.json file.",
                                "  --verbose(v):  Whether or not to show verbose logs.",
                                "  --profiler:    Whether or not this application should pause before it is run to allow a profiler to be attached.",
                                "  --help(?):     Show the help message for this application."),
                            Strings.getLines(process.getOutputWriteStream().getText().await()));
                    }
                });

                runner.test("with no command line arguments", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final InMemoryFileSystem fileSystem = process.getFileSystem();
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final QubProjectFolder qubBuildProjectFolder = qubFolder.getProjectFolder("qub", "qub-build").await();
                        final Folder qubBuildDataFolder = qubBuildProjectFolder.getProjectDataFolder().await();
                        final File qubBuildCompiledSourcesFile = qubBuildProjectFolder.getCompiledSourcesFile("7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final Folder qubTestDataFolder = process.getQubProjectDataFolder().await();

                        process.getTypeLoader()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile);

                        final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {});
                        final QubTestRunParameters parameters = QubTestRun.getParameters(process, action);
                        test.assertNotNull(parameters);
                        test.assertTrue(parameters.getBuildJson());
                        test.assertEqual(Coverage.None, parameters.getCoverage());
                        test.assertSame(process.getDefaultApplicationLauncher(), parameters.getDefaultApplicationLauncher());
                        test.assertSame(process.getEnvironmentVariables(), parameters.getEnvironmentVariables());
                        test.assertSame(process.getErrorWriteStream(), parameters.getErrorWriteStream());
                        test.assertEqual(process.getCurrentFolder(), parameters.getFolderToBuild());
                        test.assertSame(process.getCurrentFolder(), parameters.getFolderToTest());
                        test.assertEqual(process.getJVMClasspath().await(), parameters.getJvmClassPath());
                        test.assertSame(process.getOutputWriteStream(), parameters.getOutputWriteStream());
                        test.assertNull(parameters.getPattern());
                        test.assertFalse(parameters.getProfiler());
                        test.assertEqual(qubBuildDataFolder, parameters.getQubBuildDataFolder());
                        test.assertEqual(qubTestDataFolder, parameters.getQubTestDataFolder());
                        test.assertTrue(parameters.getTestJson());
                        test.assertSame(process.getProcessFactory(), parameters.getProcessFactory());
                        final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                        test.assertNotNull(verbose);
                        test.assertFalse(verbose.isVerbose());
                        test.assertEqual(Warnings.Show, parameters.getWarnings());
                    }
                });

                runner.test("with unnamed folder argument to rooted folder", (Test test) ->
                {
                    final String iDontExistFolderPath = "/i/dont/exist/";

                    try (final FakeDesktopProcess process = FakeDesktopProcess.create(iDontExistFolderPath))
                    {
                        final InMemoryFileSystem fileSystem = process.getFileSystem();
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final QubProjectFolder qubBuildProjectFolder = qubFolder.getProjectFolder("qub", "qub-build").await();
                        final Folder qubBuildDataFolder = qubBuildProjectFolder.getProjectDataFolder().await();
                        final File qubBuildCompiledSourcesFile = qubBuildProjectFolder.getCompiledSourcesFile("7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final Folder qubTestDataFolder = process.getQubProjectDataFolder().await();

                        final Folder iDontExistFolder = fileSystem.getFolder(iDontExistFolderPath).await();

                        process.getTypeLoader()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile);

                        final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {});
                        final QubTestRunParameters parameters = QubTestRun.getParameters(process, action);
                        test.assertNotNull(parameters);
                        test.assertTrue(parameters.getBuildJson());
                        test.assertEqual(Coverage.None, parameters.getCoverage());
                        test.assertSame(process.getDefaultApplicationLauncher(), parameters.getDefaultApplicationLauncher());
                        test.assertSame(process.getEnvironmentVariables(), parameters.getEnvironmentVariables());
                        test.assertSame(process.getErrorWriteStream(), parameters.getErrorWriteStream());
                        test.assertEqual(iDontExistFolder, parameters.getFolderToBuild());
                        test.assertEqual(iDontExistFolder, parameters.getFolderToTest());
                        test.assertEqual(process.getJVMClasspath().await(), parameters.getJvmClassPath());
                        test.assertSame(process.getOutputWriteStream(), parameters.getOutputWriteStream());
                        test.assertNull(parameters.getPattern());
                        test.assertFalse(parameters.getProfiler());
                        test.assertEqual(qubBuildDataFolder, parameters.getQubBuildDataFolder());
                        test.assertEqual(qubTestDataFolder, parameters.getQubTestDataFolder());
                        test.assertTrue(parameters.getTestJson());
                        test.assertSame(process.getProcessFactory(), parameters.getProcessFactory());
                        final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                        test.assertNotNull(verbose);
                        test.assertFalse(verbose.isVerbose());
                        test.assertEqual(Warnings.Show, parameters.getWarnings());
                    }
                });

                runner.test("with unnamed folder argument to unrooted folder", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create("dont/exist/"))
                    {
                        process.setDefaultCurrentFolder("/i/");
                        
                        final InMemoryFileSystem fileSystem = process.getFileSystem();
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final QubProjectFolder qubBuildProjectFolder = qubFolder.getProjectFolder("qub", "qub-build").await();
                        final Folder qubBuildDataFolder = qubBuildProjectFolder.getProjectDataFolder().await();
                        final File qubBuildCompiledSourcesFile = qubBuildProjectFolder.getCompiledSourcesFile("7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final Folder qubTestDataFolder = process.getQubProjectDataFolder().await();
                        final Folder iDontExist = fileSystem.getFolder("/i/dont/exist/").await();
                        
                        process.getTypeLoader()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile);

                        final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {});
                        final QubTestRunParameters parameters = QubTestRun.getParameters(process, action);
                        test.assertNotNull(parameters);
                        test.assertTrue(parameters.getBuildJson());
                        test.assertEqual(Coverage.None, parameters.getCoverage());
                        test.assertSame(process.getDefaultApplicationLauncher(), parameters.getDefaultApplicationLauncher());
                        test.assertSame(process.getEnvironmentVariables(), parameters.getEnvironmentVariables());
                        test.assertSame(process.getErrorWriteStream(), parameters.getErrorWriteStream());
                        test.assertEqual(iDontExist, parameters.getFolderToBuild());
                        test.assertEqual(iDontExist, parameters.getFolderToTest());
                        test.assertEqual(process.getJVMClasspath().await(), parameters.getJvmClassPath());
                        test.assertSame(process.getOutputWriteStream(), parameters.getOutputWriteStream());
                        test.assertNull(parameters.getPattern());
                        test.assertFalse(parameters.getProfiler());
                        test.assertEqual(qubBuildDataFolder, parameters.getQubBuildDataFolder());
                        test.assertEqual(qubTestDataFolder, parameters.getQubTestDataFolder());
                        test.assertTrue(parameters.getTestJson());
                        test.assertSame(process.getProcessFactory(), parameters.getProcessFactory());
                        final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                        test.assertNotNull(verbose);
                        test.assertFalse(verbose.isVerbose());
                        test.assertEqual(Warnings.Show, parameters.getWarnings());
                    }
                });

                runner.test("with named folder argument to rooted folder", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create("-folder=/i/dont/exist/"))
                    {
                        final InMemoryFileSystem fileSystem = process.getFileSystem();
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final QubProjectFolder qubBuildProjectFolder = qubFolder.getProjectFolder("qub", "qub-build").await();
                        final Folder qubBuildDataFolder = qubBuildProjectFolder.getProjectDataFolder().await();
                        final File qubBuildCompiledSourcesFile = qubBuildProjectFolder.getCompiledSourcesFile("7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final Folder qubTestDataFolder = process.getQubProjectDataFolder().await();

                        final Folder iDontExist = fileSystem.getFolder("/i/dont/exist/").await();

                        process.getTypeLoader()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile);

                        final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {});
                        final QubTestRunParameters parameters = QubTestRun.getParameters(process, action);
                        test.assertNotNull(parameters);
                        test.assertTrue(parameters.getBuildJson());
                        test.assertEqual(Coverage.None, parameters.getCoverage());
                        test.assertSame(process.getDefaultApplicationLauncher(), parameters.getDefaultApplicationLauncher());
                        test.assertSame(process.getEnvironmentVariables(), parameters.getEnvironmentVariables());
                        test.assertSame(process.getErrorWriteStream(), parameters.getErrorWriteStream());
                        test.assertEqual(iDontExist, parameters.getFolderToBuild());
                        test.assertEqual(iDontExist, parameters.getFolderToTest());
                        test.assertEqual(process.getJVMClasspath().await(), parameters.getJvmClassPath());
                        test.assertSame(process.getOutputWriteStream(), parameters.getOutputWriteStream());
                        test.assertNull(parameters.getPattern());
                        test.assertFalse(parameters.getProfiler());
                        test.assertEqual(qubBuildDataFolder, parameters.getQubBuildDataFolder());
                        test.assertEqual(qubTestDataFolder, parameters.getQubTestDataFolder());
                        test.assertTrue(parameters.getTestJson());
                        test.assertSame(process.getProcessFactory(), parameters.getProcessFactory());
                        final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                        test.assertNotNull(verbose);
                        test.assertFalse(verbose.isVerbose());
                        test.assertEqual(Warnings.Show, parameters.getWarnings());
                    }
                });

                runner.test("with named folder argument to unrooted folder", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create("-folder=dont/exist/"))
                    {
                        process.setDefaultCurrentFolder("/i/");

                        final InMemoryFileSystem fileSystem = process.getFileSystem();
                        final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                        final QubProjectFolder qubBuildProjectFolder = qubFolder.getProjectFolder("qub", "qub-build").await();
                        final Folder qubBuildDataFolder = qubBuildProjectFolder.getProjectDataFolder().await();
                        final File qubBuildCompiledSourcesFile = qubBuildProjectFolder.getCompiledSourcesFile("7").await();
                        qubBuildCompiledSourcesFile.create().await();
                        final Folder qubTestDataFolder = process.getQubProjectDataFolder().await();

                        final Folder currentFolder = fileSystem.getFolder("/i/").await();
                        final Folder iDontExist = fileSystem.getFolder("/i/dont/exist/").await();

                        process.getTypeLoader()
                            .addTypeContainer(QubBuild.class, qubBuildCompiledSourcesFile);

                        final CommandLineAction action = CommandLineAction.create("fake-action-name", (DesktopProcess actionProcess) -> {});
                        final QubTestRunParameters parameters = QubTestRun.getParameters(process, action);
                        test.assertNotNull(parameters);
                        test.assertTrue(parameters.getBuildJson());
                        test.assertEqual(Coverage.None, parameters.getCoverage());
                        test.assertSame(process.getDefaultApplicationLauncher(), parameters.getDefaultApplicationLauncher());
                        test.assertSame(process.getEnvironmentVariables(), parameters.getEnvironmentVariables());
                        test.assertSame(process.getErrorWriteStream(), parameters.getErrorWriteStream());
                        test.assertEqual(iDontExist, parameters.getFolderToBuild());
                        test.assertEqual(iDontExist, parameters.getFolderToTest());
                        test.assertEqual(process.getJVMClasspath().await(), parameters.getJvmClassPath());
                        test.assertSame(process.getOutputWriteStream(), parameters.getOutputWriteStream());
                        test.assertNull(parameters.getPattern());
                        test.assertFalse(parameters.getProfiler());
                        test.assertEqual(qubBuildDataFolder, parameters.getQubBuildDataFolder());
                        test.assertEqual(qubTestDataFolder, parameters.getQubTestDataFolder());
                        test.assertTrue(parameters.getTestJson());
                        test.assertSame(process.getProcessFactory(), parameters.getProcessFactory());
                        final VerboseCharacterToByteWriteStream verbose = parameters.getVerbose();
                        test.assertNotNull(verbose);
                        test.assertFalse(verbose.isVerbose());
                        test.assertEqual(Warnings.Show, parameters.getWarnings());
                    }
                });
            });

            runner.testGroup("run(QubTestParameters)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> QubTestRun.run(null),
                        new PreConditionFailure("parameters cannot be null."));
                });

                runner.test("with non-existing folder to test", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final Folder folderToTest = fileSystem.getFolder("/folder/to/test/").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final String jvmClassPath = "fake-jvm-classpath";
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, folderToTest, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader);

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                        Iterable.create(
                            "ERROR: The file at \"/folder/to/test/project.json\" doesn't exist."),
                        Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(1, exitCode);

                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with no source files", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder);
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final String jvmClassPath = "fake-jvm-classpath";
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader);

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                        Iterable.create(
                            "ERROR: No java source files found in /current/folder/."),
                        Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(1, exitCode);

                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder.toString())
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A"))
                            .setFunction((ByteWriteStream functionOutput) ->
                            {
                                CharacterWriteStream.create(functionOutput).writeLine("Inside test runner!").await();
                            }));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader);

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "Inside test runner!"),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=false --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file and verbose", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder.toString())
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/ sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with different dependency", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final QubProjectVersionFolder meA5VersionFolder = qubFolder.getProjectVersionFolder("me", "a", "5").await();
                    meA5VersionFolder.getCompiledSourcesFile().await().create().await();
                    final QubProjectVersionFolder meB2VersionFolder = qubFolder.getProjectVersionFolder("me", "b", "2").await();
                    meB2VersionFolder.getCompiledSourcesFile().await().create().await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create()
                                .setDependencies(Iterable.create(
                                    meA5VersionFolder.getProjectSignature().await())))
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = meB2VersionFolder.toString();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(Iterable.create(outputsFolder.toString(), meA5VersionFolder.getCompiledSourcesFile().await().toString()))
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), meA5VersionFolder.getCompiledSourcesFile().await().toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar;/qub/me/b/versions/2/ qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar;/qub/me/b/versions/2/ qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with same dependency with newer version", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final QubProjectVersionFolder meA5VersionFolder = qubFolder.getProjectVersionFolder("me", "a", "5").await();
                    meA5VersionFolder.getCompiledSourcesFile().await().create().await();
                    final QubProjectVersionFolder meA6VersionFolder = qubFolder.getProjectVersionFolder("me", "a", "6").await();
                    meA6VersionFolder.getCompiledSourcesFile().await().create().await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create()
                                .setDependencies(Iterable.create(
                                    meA5VersionFolder.getProjectSignature().await())))
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = meA6VersionFolder.toString();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(Iterable.create(outputsFolder.toString(), meA5VersionFolder.getCompiledSourcesFile().await().toString()))
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), meA5VersionFolder.getCompiledSourcesFile().await().toString()))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with same dependency with older version", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final QubProjectVersionFolder meA5VersionFolder = qubFolder.getProjectVersionFolder("me", "a", "5").await();
                    meA5VersionFolder.getCompiledSourcesFile().await().create().await();
                    final QubProjectVersionFolder meA4VersionFolder = qubFolder.getProjectVersionFolder("me", "a", "4").await();
                    meA4VersionFolder.getCompiledSourcesFile().await().create().await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create()
                                .setDependencies(Iterable.create(
                                    meA5VersionFolder.getProjectSignature().await())))
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = meA4VersionFolder.toString();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(Iterable.create(outputsFolder.toString(), meA5VersionFolder.getCompiledSourcesFile().await().toString()))
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), meA5VersionFolder.getCompiledSourcesFile().await().toString()))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/a/versions/5/a.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, verbose, and jvm.classpath equal to current project", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = outputsFolder.toString();
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder.toString())
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(outputsFolder.toString())
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/ sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/ qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/ qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, testjson=true, and verbose", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder.toString())
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output))
                        .setTestJson(true);

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/ sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, testjson=false, and verbose", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder.toString())
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(false)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output))
                        .setTestJson(false);

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/ sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=false --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=false --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file and coverage=sources", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final QubProjectVersionFolder jacocoFolder = qubFolder.getProjectVersionFolder("jacoco", "jacococli", "0.8.1").await();
                    final File jacocoAgentJarFile = jacocoFolder.createFile("jacocoagent.jar").await();
                    final File jacocoCliJarFile = jacocoFolder.createFile("jacococli.jar").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File coverageExecFile = outputsFolder.getFile("coverage.exec").await();
                    final Folder coverageFolder = outputsFolder.getFolder("coverage").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder.toString())
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addJavaAgent(jacocoAgentJarFile + "=destfile=" + coverageExecFile)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(false)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.Sources)
                            .addFullClassNamesToTest(Iterable.create("A")))
                        .add(new FakeJacocoCliProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addJacocoCliJar(jacocoCliJarFile)
                            .addReport()
                            .addCoverageExec(coverageExecFile)
                            .addClassFile(outputsFolder.getFile("A.class").await())
                            .addSourceFiles(currentFolder.getFolder("sources").await())
                            .addHtml(coverageFolder)
                            .setFunction(() ->
                            {
                                coverageFolder.createFile("index.html").await();
                            }));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setCoverage(Coverage.Sources);

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "",
                                "Analyzing coverage..."),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -javaagent:/qub/jacoco/jacococli/versions/0.8.1/jacocoagent.jar=destfile=/current/folder/outputs/coverage.exec -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=false --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=Sources A",
                            "",
                            "",
                            "Analyzing coverage..."
                        ),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, verbose, and coverage=sources", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final QubProjectVersionFolder jacocoFolder = qubFolder.getProjectVersionFolder("jacoco", "jacococli", "0.8.1").await();
                    final File jacocoAgentJarFile = jacocoFolder.createFile("jacocoagent.jar").await();
                    final File jacocoCliJarFile = jacocoFolder.createFile("jacococli.jar").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File coverageExecFile = outputsFolder.getFile("coverage.exec").await();
                    final Folder coverageFolder = outputsFolder.getFolder("coverage").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder.toString())
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addJavaAgent(jacocoAgentJarFile + "=destfile=" + coverageExecFile)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.Sources)
                            .addFullClassNamesToTest(Iterable.create("A")))
                        .add(new FakeJacocoCliProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addJacocoCliJar(jacocoCliJarFile)
                            .addReport()
                            .addCoverageExec(coverageExecFile)
                            .addClassFile(outputsFolder.getFile("A.class").await())
                            .addSourceFiles(currentFolder.getFolder("sources").await())
                            .addHtml(coverageFolder)
                            .setFunction(() ->
                            {
                                coverageFolder.createFile("index.html").await();
                            }));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setCoverage(Coverage.Sources)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/ sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -javaagent:/qub/jacoco/jacococli/versions/0.8.1/jacocoagent.jar=destfile=/current/folder/outputs/coverage.exec -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=Sources A",
                                "",
                                "",
                                "Analyzing coverage...",
                                "VERBOSE: Running /current/folder/: java -jar /qub/jacoco/jacococli/versions/0.8.1/jacococli.jar report /current/folder/outputs/coverage.exec --classfiles outputs/A.class --sourcefiles /current/folder/sources/ --html /current/folder/outputs/coverage/"),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -javaagent:/qub/jacoco/jacococli/versions/0.8.1/jacocoagent.jar=destfile=/current/folder/outputs/coverage.exec -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=Sources A",
                            "",
                            "",
                            "Analyzing coverage...",
                            "VERBOSE: Running /current/folder/: java -jar /qub/jacoco/jacococli/versions/0.8.1/jacococli.jar report /current/folder/outputs/coverage.exec --classfiles outputs/A.class --sourcefiles /current/folder/sources/ --html /current/folder/outputs/coverage/"),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, verbose, coverage, and multiple jacoco installations", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final QubProjectFolder jacocoCliFolder = qubFolder.getProjectFolder("jacoco", "jacococli").await();
                    jacocoCliFolder.getProjectVersionFolder("0.5.0").await()
                        .createFile("jacocoagent.jar").await();
                    jacocoCliFolder.getProjectVersionFolder("0.8.1").await()
                        .createFile("jacocoagent.jar").await();
                    final QubProjectVersionFolder jacoco092Folder = jacocoCliFolder.getProjectVersionFolder("0.9.2").await();
                    final File jacocoAgentJarFile = jacoco092Folder.createFile("jacocoagent.jar").await();
                    final File jacocoCliJarFile = jacoco092Folder.createFile("jacococli.jar").await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setJava(ProjectJSONJava.create())
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final File coverageExecFile = outputsFolder.getFile("coverage.exec").await();
                    final Folder coverageFolder = outputsFolder.getFolder("coverage").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(outputsFolder.toString())
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addJavaAgent(jacocoAgentJarFile + "=destfile=" + coverageExecFile)
                            .addClasspath(Iterable.create(outputsFolder.toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.Sources)
                            .addFullClassNamesToTest(Iterable.create("A")))
                        .add(new FakeJacocoCliProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addJacocoCliJar(jacocoCliJarFile)
                            .addReport()
                            .addCoverageExec(coverageExecFile)
                            .addClassFile(outputsFolder.getFile("A.class").await())
                            .addSourceFiles(currentFolder.getFolder("sources").await())
                            .addHtml(coverageFolder)
                            .setFunction(() ->
                            {
                                coverageFolder.createFile("index.html").await();
                            }));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setCoverage(Coverage.Sources)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/ sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -javaagent:/qub/jacoco/jacococli/versions/0.9.2/jacocoagent.jar=destfile=/current/folder/outputs/coverage.exec -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=Sources A",
                                "",
                                "",
                                "Analyzing coverage...",
                                "VERBOSE: Running /current/folder/: java -jar /qub/jacoco/jacococli/versions/0.9.2/jacococli.jar report /current/folder/outputs/coverage.exec --classfiles outputs/A.class --sourcefiles /current/folder/sources/ --html /current/folder/outputs/coverage/"),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -javaagent:/qub/jacoco/jacococli/versions/0.9.2/jacocoagent.jar=destfile=/current/folder/outputs/coverage.exec -classpath /current/folder/outputs/;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=Sources A",
                            "",
                            "",
                            "Analyzing coverage...",
                            "VERBOSE: Running /current/folder/: java -jar /qub/jacoco/jacococli/versions/0.9.2/jacococli.jar report /current/folder/outputs/coverage.exec --classfiles outputs/A.class --sourcefiles /current/folder/sources/ --html /current/folder/outputs/coverage/"
                        ),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, one dependency, and verbose", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final QubProjectVersionFolder meB2Folder = qubFolder.getProjectVersionFolder("me", "b", "2").await();
                    meB2Folder.getCompiledSourcesFile().await().create().await();
                    meB2Folder.getProjectJSONFile().await()
                        .setContentsAsString(
                            ProjectJSON.create()
                                .setPublisher(meB2Folder.getPublisherName().await())
                                .setProject(meB2Folder.getProjectName().await())
                                .setVersion(meB2Folder.getVersion().await())
                                .toString())
                            .await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("1")
                            .setJava(ProjectJSONJava.create()
                                .setDependencies(Iterable.create(
                                    meB2Folder.getProjectSignature().await())))
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(Iterable.create(outputsFolder.toString(), meB2Folder.getCompiledSourcesFile().await().toString()))
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(outputsFolder.toString(), meB2Folder.getCompiledSourcesFile().await().toString(), jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/;/qub/me/b/versions/2/b.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/b/versions/2/b.jar;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/b/versions/2/b.jar;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });

                runner.test("with one source file, one transitive dependency, and verbose", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = InMemoryFileSystem.create(test.getClock());
                    fileSystem.createRoot("/").await();
                    final QubFolder qubFolder = QubFolder.get(fileSystem.getFolder("/qub/").await());
                    final Folder qubTestDataFolder = qubFolder.getProjectDataFolder("qub", "test-java").await();
                    final File logFile = qubTestDataFolder.getFile("logs/1.log").await();
                    final QubProjectVersionFolder meC3Folder = qubFolder.getProjectVersionFolder("me", "c", "3").await();
                    meC3Folder.getCompiledSourcesFile().await().create().await();
                    meC3Folder.getProjectJSONFile().await()
                        .setContentsAsString(
                            ProjectJSON.create()
                                .setPublisher(meC3Folder.getPublisherName().await())
                                .setProject(meC3Folder.getProjectName().await())
                                .setVersion(meC3Folder.getVersion().await())
                                .toString())
                            .await();
                    final QubProjectVersionFolder meB2Folder = qubFolder.getProjectVersionFolder("me", "b", "2").await();
                    meB2Folder.getCompiledSourcesFile().await().create().await();
                    meB2Folder.getProjectJSONFile().await()
                        .setContentsAsString(
                            ProjectJSON.create()
                                .setPublisher(meB2Folder.getPublisherName().await())
                                .setProject(meB2Folder.getProjectName().await())
                                .setVersion(meB2Folder.getVersion().await())
                                .setJava(ProjectJSONJava.create()
                                    .setDependencies(Iterable.create(
                                        meC3Folder.getProjectSignature().await())))
                                .toString())
                            .await();
                    final Folder currentFolder = fileSystem.getFolder("/current/folder/").await();
                    final File projectJsonFile = currentFolder.getFile("project.json").await();
                    projectJsonFile.setContentsAsString(
                        ProjectJSON.create()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("1")
                            .setJava(ProjectJSONJava.create()
                                .setDependencies(Iterable.create(
                                    meB2Folder.getProjectSignature().await())))
                            .toString())
                        .await();
                    final File aJavaFile = currentFolder.getFile("sources/A.java").await();
                    aJavaFile.setContentsAsString("A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    final EnvironmentVariables environmentVariables = EnvironmentVariables.create()
                        .set("QUB_HOME", qubFolder.toString());
                    final String jvmClassPath = "/fake-jvm-classpath";
                    final FakeProcessFactory processFactory = FakeProcessFactory.create(test.getParallelAsyncRunner(), currentFolder)
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addVersion()
                            .setVersionFunctionAutomatically("javac 14.0.1"))
                        .add(new FakeJavacProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addOutputFolder(outputsFolder)
                            .addXlintUnchecked()
                            .addXlintDeprecation()
                            .addClasspath(Iterable.create(
                                outputsFolder.toString(),
                                meB2Folder.getCompiledSourcesFile().await().toString(),
                                meC3Folder.getCompiledSourcesFile().await().toString()))
                            .addSourceFile(aJavaFile.relativeTo(currentFolder))
                            .setCompileFunctionAutomatically())
                        .add(new FakeConsoleTestRunnerProcessRun()
                            .setWorkingFolder(currentFolder)
                            .addClasspath(Iterable.create(
                                outputsFolder.toString(),
                                meB2Folder.getCompiledSourcesFile().await().toString(),
                                meC3Folder.getCompiledSourcesFile().await().toString(),
                                jvmClassPath))
                            .addConsoleTestRunnerFullClassName()
                            .addProfiler(false)
                            .addVerbose(true)
                            .addTestJson(true)
                            .addLogFile(logFile)
                            .addOutputFolder(outputsFolder)
                            .addCoverage(Coverage.None)
                            .addFullClassNamesToTest(Iterable.create("A")));
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = FakeDefaultApplicationLauncher.create(fileSystem);
                    final FakeTypeLoader typeLoader = FakeTypeLoader.create()
                        .addTypeContainer(QubBuild.class, qubFolder.getCompiledSourcesFile("qub", "build-java", "7").await());
                    final QubTestRunParameters parameters = new QubTestRunParameters(output, error, currentFolder, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath, qubTestDataFolder, typeLoader)
                        .setVerbose(VerboseCharacterToByteWriteStream.create(output));

                    final int exitCode = QubTestRun.run(parameters);

                    test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Getting javac version...",
                                "VERBOSE: Running /current/folder/: javac --version...",
                                "VERBOSE: javac 14.0.1",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /current/folder/: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /current/folder/outputs/;/qub/me/b/versions/2/b.jar;/qub/me/c/versions/3/c.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/b/versions/2/b.jar;/qub/me/c/versions/3/c.jar;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                                ""),
                            Strings.getLines(output.getText().await()));
                    test.assertEqual(
                        Iterable.create(),
                        Strings.getLines(error.getText().await()));
                    test.assertEqual(0, exitCode);

                    test.assertEqual(
                        Iterable.create(
                            "Running tests...",
                            "VERBOSE: Running /current/folder/: java -classpath /current/folder/outputs/;/qub/me/b/versions/2/b.jar;/qub/me/c/versions/3/c.jar;/fake-jvm-classpath qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --logfile=/qub/qub/test-java/data/logs/1.log --output-folder=/current/folder/outputs/ --coverage=None A",
                            ""),
                        Strings.getLines(qubTestDataFolder.getFileContentsAsString("logs/1.log").await()));
                });
            });
        });
    }
}
