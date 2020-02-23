package qub;

public interface QubTestTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubTest.class, () ->
        {
            runner.testGroup("main(String[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> QubTest.main((String[])null),
                        new PreConditionFailure("arguments cannot be null."));
                });
            });

            runner.testGroup("main(Process)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> QubTest.main((QubProcess)null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with -? command line argument", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    try (final QubProcess process = createProcess(output, "-?"))
                    {
                        QubTest.main(process);
                        test.assertEqual(-1, process.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Usage: qub-test [[--folder=]<folder-to-test>] [--pattern=<test-name-pattern>] [--coverage[=<None|Sources|Tests|All>]] [--testjson] [--verbose] [--profiler] [--help]",
                            "  Used to run tests in source code projects.",
                            "  --folder: The folder to run tests in. Defaults to the current folder.",
                            "  --pattern: The pattern to match against tests to determine if they will be run or not.",
                            "  --coverage(c): Whether or not to collect code coverage information while running tests.",
                            "  --testjson: Whether or not to write the test results to a test.json file.",
                            "  --verbose(v): Whether or not to show verbose logs.",
                            "  --profiler: Whether or not this application should pause before it is run to allow a profiler to be attached.",
                            "  --help(?): Show the help message for this application."),
                        Strings.getLines(output.asCharacterReadStream().getText().await()));
                });

                runner.test("with unnamed folder argument to rooted folder that doesn't exist", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    try (final QubProcess process = createProcess(output, currentFolder, "/i/dont/exist"))
                    {
                        QubTest.main(process);
                        test.assertEqual(
                            Iterable.create(
                                "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());
                        test.assertEqual(1, process.getExitCode());
                    }
                });

                runner.test("with unnamed folder argument to unrooted folder that doesn't exist", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    try (final QubProcess process = createProcess(output, currentFolder, "i/dont/exist"))
                    {
                        QubTest.main(process);
                        test.assertEqual(
                            Iterable.create(
                                "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());
                        test.assertEqual(1, process.getExitCode());
                    }
                });

                runner.test("with named folder argument to rooted folder that doesn't exist", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    try (final QubProcess process = createProcess(output, currentFolder, "-folder=/i/dont/exist"))
                    {
                        QubTest.main(process);
                        test.assertEqual(
                            Iterable.create(
                                "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());
                        test.assertEqual(1, process.getExitCode());
                    }
                });

                runner.test("with named folder argument to unrooted folder that doesn't exist", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    try (final QubProcess process = createProcess(output, currentFolder, "-folder=i/dont/exist"))
                    {
                        QubTest.main(process);
                        test.assertEqual(
                            Iterable.create(
                                "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());
                        test.assertEqual(1, process.getExitCode());
                    }
                });

                runner.test("with no source files", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();

                    try (final QubProcess process = createProcess(output, currentFolder))
                    {
                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "ERROR: No java source files found in /."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());
                        test.assertEqual(1, process.getExitCode());
                    }
                });

                runner.test("with one source file", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder))
                    {
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(false)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file and verbose", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose"))
                    {
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, verbose, and jvm.classpath=/foo/subfolder", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        process.setJVMClasspath("/outputs;/foo/subfolder");
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs;/foo/subfolder")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs;/foo/subfolder qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with different dependency", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava()
                                .setDependencies(Iterable.create(
                                    new ProjectSignature("me", "a", "5"))))
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        qubFolder.createFile("me/b/2/b.jar").await();
                        process.setJVMClasspath("/outputs;/qub/me/b/2/b.jar");
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(Iterable.create("/outputs", "/qub/me/a/5/a.jar"))
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs;/qub/me/a/5/a.jar;/qub/me/b/2/b.jar")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/a/5/a.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs;/qub/me/a/5/a.jar;/qub/me/b/2/b.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with same dependency", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava()
                                .setDependencies(Iterable.create(
                                    new ProjectSignature("me", "a", "5"))))
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/a/5/a.jar"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        qubFolder.createFile("me/b/2/b.jar").await();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(Iterable.create("/outputs", "/qub/me/a/5/a.jar"))
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs;/qub/me/a/5/a.jar")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/a/5/a.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs;/qub/me/a/5/a.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with same dependency with newer version", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava()
                                .setDependencies(Iterable.create(
                                    new ProjectSignature("me", "a", "5"))))
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/a/6/a.jar"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        qubFolder.createFile("me/a/6/a.jar").await();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(Iterable.create("/outputs", "/qub/me/a/5/a.jar"))
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs;/qub/me/a/5/a.jar")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/a/5/a.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs;/qub/me/a/5/a.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with same dependency with older version", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava()
                                .setDependencies(Iterable.create(
                                    new ProjectSignature("me", "a", "5"))))
                        .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/a/4/a.jar"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/4/a.jar").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(Iterable.create("/outputs", "/qub/me/a/5/a.jar"))
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs;/qub/me/a/5/a.jar")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/a/5/a.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs;/qub/me/a/5/a.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, verbose, and jvm.classpath equal to current project", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setPublisher("me")
                            .setProject("stuff")
                            .setVersion("7")
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/stuff/7/stuff.jar"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/stuff/7/stuff.jar").await();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", qubFolder.toString()));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), process.getCurrentFolderPath())
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file and --testjson and --verbose", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "--testjson", "--verbose"))
                    {
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), process.getCurrentFolderPath())
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file and --testjson=true and --verbose", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "--testjson=true", "--verbose"))
                    {
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), process.getCurrentFolderPath())
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file and --testjson=false and --verbose", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "--testjson=false", "--verbose"))
                    {
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), process.getCurrentFolderPath())
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(false)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=false --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file and coverage", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-coverage"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("jacoco/jacococli/0.8.1/jacocoagent.jar").await();
                        process
                            .setJVMClasspath(currentFolder.getFolder("outputs").await().toString())
                            .setDefaultApplicationLauncher(new FakeDefaultApplicationLauncher())
                            .setEnvironmentVariables(new EnvironmentVariables()
                                .set("QUB_HOME", "/qub/"))
                            .setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), process.getCurrentFolderPath())
                                .add(new FakeJavacProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addOutputFolder(currentFolder.getFolder("outputs").await())
                                    .addXlintUnchecked()
                                    .addXlintDeprecation()
                                    .addClasspath("/outputs")
                                    .addSourceFile("sources/A.java")
                                    .setFunctionAutomatically())
                                .add(new FakeConsoleTestRunnerProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addJavaAgent("/qub/jacoco/jacococli/0.8.1/jacocoagent.jar=destfile=/outputs/coverage.exec")
                                    .addClasspath("/outputs")
                                    .addConsoleTestRunnerFullClassName()
                                    .addProfiler(false)
                                    .addVerbose(false)
                                    .addTestJson(true)
                                    .addOutputFolder(currentFolder.getFolder("outputs").await())
                                    .addCoverage(Coverage.Sources)
                                    .addFullClassNamesToTest(Iterable.create("A")))
                                .add(new FakeJacocoCliProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addJacocoCliJar("/qub/jacoco/jacococli/0.8.1/jacococli.jar")
                                    .addReport()
                                    .addCoverageExec("/outputs/coverage.exec")
                                    .addClassFile(currentFolder.getFile("outputs/A.class").await())
                                    .addSourceFiles(currentFolder.getFolder("sources").await())
                                    .addHtml(currentFolder.getFolder("outputs/coverage").await())));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "Compiling 1 file...",
                                "Running tests...",
                                "",
                                "",
                                "Analyzing coverage..."),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, verbose, and coverage", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose", "-coverage"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("jacoco/jacococli/0.8.1/jacocoagent.jar").await();
                        process
                            .setJVMClasspath(currentFolder.getFolder("outputs").await().toString())
                            .setDefaultApplicationLauncher(new FakeDefaultApplicationLauncher())
                            .setEnvironmentVariables(new EnvironmentVariables()
                                .set("QUB_HOME", "/qub/"))
                            .setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), process.getCurrentFolderPath())
                                .add(new FakeJavacProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addOutputFolder(currentFolder.getFolder("outputs").await())
                                    .addXlintUnchecked()
                                    .addXlintDeprecation()
                                    .addClasspath("/outputs")
                                    .addSourceFile("sources/A.java")
                                    .setFunctionAutomatically())
                                .add(new FakeConsoleTestRunnerProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addJavaAgent("/qub/jacoco/jacococli/0.8.1/jacocoagent.jar=destfile=/outputs/coverage.exec")
                                    .addClasspath("/outputs")
                                    .addConsoleTestRunnerFullClassName()
                                    .addProfiler(false)
                                    .addVerbose(true)
                                    .addTestJson(true)
                                    .addOutputFolder(currentFolder.getFolder("outputs").await())
                                    .addCoverage(Coverage.Sources)
                                    .addFullClassNamesToTest(Iterable.create("A")))
                                .add(new FakeJacocoCliProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addJacocoCliJar("/qub/jacoco/jacococli/0.8.1/jacococli.jar")
                                    .addReport()
                                    .addCoverageExec("/outputs/coverage.exec")
                                    .addClassFile(currentFolder.getFile("outputs/A.class").await())
                                    .addSourceFiles(currentFolder.getFolder("sources").await())
                                    .addHtml(currentFolder.getFolder("outputs/coverage").await())));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -javaagent:/qub/jacoco/jacococli/0.8.1/jacocoagent.jar=destfile=/outputs/coverage.exec -classpath /outputs qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=Sources A",
                                "",
                                "",
                                "Analyzing coverage...",
                                "VERBOSE: Running /: java -jar /qub/jacoco/jacococli/0.8.1/jacococli.jar report /outputs/coverage.exec --classfiles outputs/A.class --sourcefiles /sources --html /outputs/coverage"),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, verbose, coverage, and multiple jacoco installations", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setJava(new ProjectJSONJava())
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();

                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose", "-coverage"))
                    {
                        final Folder qubFolder = process.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("jacoco/jacococli/0.5.0/jacocoagent.jar").await();
                        qubFolder.createFile("jacoco/jacococli/0.8.1/jacocoagent.jar").await();
                        qubFolder.createFile("jacoco/jacococli/0.9.2/jacocoagent.jar").await();
                        process.setJVMClasspath(currentFolder.getFolder("outputs").await().toString());
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        process.setDefaultApplicationLauncher(new FakeDefaultApplicationLauncher());
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath("/outputs")
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addJavaAgent("/qub/jacoco/jacococli/0.9.2/jacocoagent.jar=destfile=/outputs/coverage.exec")
                                .addClasspath("/outputs")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(currentFolder.getFolder("outputs").await())
                                .addCoverage(Coverage.Sources)
                                .addFullClassNamesToTest(Iterable.create("A")))
                            .add(new FakeJacocoCliProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addJacocoCliJar("/qub/jacoco/jacococli/0.9.2/jacococli.jar")
                                .addReport()
                                .addCoverageExec("/outputs/coverage.exec")
                                .addClassFile(currentFolder.getFile("outputs/A.class").await())
                                .addSourceFiles(currentFolder.getFolder("sources").await())
                                .addHtml(currentFolder.getFolder("outputs/coverage").await())));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -javaagent:/qub/jacoco/jacococli/0.9.2/jacocoagent.jar=destfile=/outputs/coverage.exec -classpath /outputs qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=Sources A",
                                "",
                                "",
                                "Analyzing coverage...",
                                "VERBOSE: Running /: java -jar /qub/jacoco/jacococli/0.9.2/jacococli.jar report /outputs/coverage.exec --classfiles outputs/A.class --sourcefiles /sources --html /outputs/coverage"),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, one dependency, and verbose", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("1")
                            .setJava(new ProjectJSONJava()
                                .setDependencies(Iterable.create(
                                    new ProjectSignature("me", "b", "2"))))
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose"))
                    {
                        final Folder qubFolder = process.getFileSystem().getFolder("/qub/").await();
                        qubFolder.setFileContentsAsString("me/b/2/project.json",
                            new ProjectJSON()
                                .setPublisher("me")
                                .setProject("b")
                                .setVersion("2")
                                .toString()).await();
                        qubFolder.createFile("me/b/2/b.jar").await();

                        process.setJVMClasspath(outputsFolder.toString());
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", qubFolder.toString()));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                            .add(new FakeJavacProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addOutputFolder(outputsFolder)
                                .addXlintUnchecked()
                                .addXlintDeprecation()
                                .addClasspath(Iterable.create("/outputs", "/qub/me/b/2/b.jar"))
                                .addSourceFile("sources/A.java")
                                .setFunctionAutomatically())
                            .add(new FakeConsoleTestRunnerProcessRun()
                                .setWorkingFolder(currentFolder)
                                .addClasspath("/outputs;/qub/me/b/2/b.jar")
                                .addConsoleTestRunnerFullClassName()
                                .addProfiler(false)
                                .addVerbose(true)
                                .addTestJson(true)
                                .addOutputFolder(outputsFolder)
                                .addCoverage(Coverage.None)
                                .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/b/2/b.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs;/qub/me/b/2/b.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, one transitive dependency, and verbose", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("1")
                            .setJava(new ProjectJSONJava()
                                .setDependencies(Iterable.create(
                                    new ProjectSignature("me", "b", "2"))))
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose"))
                    {
                        final Folder qubFolder = process.getFileSystem().getFolder("/qub/").await();
                        qubFolder.setFileContentsAsString("me/b/2/project.json",
                            new ProjectJSON()
                                .setPublisher("me")
                                .setProject("b")
                                .setVersion("2")
                                .setJava(new ProjectJSONJava()
                                    .setDependencies(Iterable.create(
                                        new ProjectSignature("me", "c", "3"))))
                                .toString()).await();
                        qubFolder.createFile("me/b/2/b.jar").await();
                        qubFolder.setFileContentsAsString("me/c/3/project.json",
                            new ProjectJSON()
                                .setPublisher("me")
                                .setProject("c")
                                .setVersion("3")
                                .toString()).await();
                        qubFolder.createFile("me/c/3/c.jar").await();

                        process.setJVMClasspath(outputsFolder.toString());
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                                .add(new FakeJavacProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addOutputFolder(outputsFolder)
                                    .addXlintUnchecked()
                                    .addXlintDeprecation()
                                    .addClasspath(Iterable.create("/outputs", "/qub/me/b/2/b.jar", "/qub/me/c/3/c.jar"))
                                    .addSourceFile("sources/A.java")
                                    .setFunctionAutomatically())
                                .add(new FakeConsoleTestRunnerProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addClasspath("/outputs;/qub/me/b/2/b.jar;/qub/me/c/3/c.jar")
                                    .addConsoleTestRunnerFullClassName()
                                    .addProfiler(false)
                                    .addVerbose(true)
                                    .addTestJson(true)
                                    .addOutputFolder(outputsFolder)
                                    .addCoverage(Coverage.None)
                                    .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/b/2/b.jar;/qub/me/c/3/c.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs;/qub/me/b/2/b.jar;/qub/me/c/3/c.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });

                runner.test("with one source file, one transitive dependency under versions folder, and verbose", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.setFileContentsAsString("project.json",
                        new ProjectJSON()
                            .setPublisher("me")
                            .setProject("a")
                            .setVersion("1")
                            .setJava(new ProjectJSONJava()
                                .setDependencies(Iterable.create(
                                    new ProjectSignature("me", "b", "2"))))
                            .toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();
                    final Folder outputsFolder = currentFolder.getFolder("outputs").await();
                    try (final QubProcess process = createProcess(output, currentFolder, "-verbose"))
                    {
                        final Folder qubFolder = process.getFileSystem().getFolder("/qub/").await();
                        qubFolder.setFileContentsAsString("me/b/2/project.json",
                            new ProjectJSON()
                                .setPublisher("me")
                                .setProject("b")
                                .setVersion("2")
                                .setJava(new ProjectJSONJava()
                                    .setDependencies(Iterable.create(
                                        new ProjectSignature("me", "c", "3"))))
                                .toString()).await();
                        qubFolder.createFile("me/b/2/b.jar").await();
                        qubFolder.setFileContentsAsString("me/c/versions/3/project.json",
                            new ProjectJSON()
                                .setPublisher("me")
                                .setProject("c")
                                .setVersion("3")
                                .toString()).await();
                        qubFolder.createFile("me/c/versions/3/c.jar").await();

                        process.setJVMClasspath(outputsFolder.toString());
                        process.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        process.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), currentFolder)
                                .add(new FakeJavacProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addOutputFolder(outputsFolder)
                                    .addXlintUnchecked()
                                    .addXlintDeprecation()
                                    .addClasspath(Iterable.create("/outputs", "/qub/me/b/2/b.jar", "/qub/me/c/versions/3/c.jar"))
                                    .addSourceFile("sources/A.java")
                                    .setFunctionAutomatically())
                                .add(new FakeConsoleTestRunnerProcessRun()
                                    .setWorkingFolder(currentFolder)
                                    .addClasspath("/outputs;/qub/me/b/2/b.jar;/qub/me/c/versions/3/c.jar")
                                    .addConsoleTestRunnerFullClassName()
                                    .addProfiler(false)
                                    .addVerbose(true)
                                    .addTestJson(true)
                                    .addOutputFolder(outputsFolder)
                                    .addCoverage(Coverage.None)
                                    .addFullClassNamesToTest(Iterable.create("A"))));

                        QubTest.main(process);

                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: Parsing project.json...",
                                "VERBOSE: Updating outputs/build.json...",
                                "VERBOSE: Setting project.json...",
                                "VERBOSE: Setting source files...",
                                "VERBOSE: Detecting java source files to compile...",
                                "VERBOSE: Compiling all source files.",
                                "Compiling 1 file...",
                                "VERBOSE: Running /: javac -d outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/b/2/b.jar;/qub/me/c/versions/3/c.jar sources/A.java...",
                                "VERBOSE: Compilation finished.",
                                "VERBOSE: Writing build.json file...",
                                "VERBOSE: Done writing build.json file.",
                                "Running tests...",
                                "VERBOSE: Running /: java -classpath /outputs;/qub/me/b/2/b.jar;/qub/me/c/versions/3/c.jar qub.ConsoleTestRunner --profiler=false --verbose=true --testjson=true --output-folder=/outputs --coverage=None A",
                                ""),
                            Strings.getLines(output.asCharacterReadStream().getText().await()).skipLast());

                        test.assertEqual(0, process.getExitCode());
                    }
                });
            });
        });
    }

    static InMemoryFileSystem getInMemoryFileSystem(Test test)
    {
        PreCondition.assertNotNull(test, "test");

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
        fileSystem.createRoot("/");

        return fileSystem;
    }

    static Folder getInMemoryCurrentFolder(Test test)
    {
        PreCondition.assertNotNull(test, "test");

        return getInMemoryFileSystem(test).getFolder("/").await();
    }

    static QubProcess createProcess(ByteWriteStream output, String... commandLineArguments)
    {
        PreCondition.assertNotNull(output, "output");
        PreCondition.assertNotNull(commandLineArguments, "commandLineArguments");

        final QubProcess result = QubProcess.create(commandLineArguments);
        result.setLineSeparator("\n");
        result.setOutputByteWriteStream(output);

        return result;
    }

    static QubProcess createProcess(ByteWriteStream output, Folder currentFolder, String... commandLineArguments)
    {
        PreCondition.assertNotNull(output, "output");
        PreCondition.assertNotNull(currentFolder, "currentFolder");
        PreCondition.assertNotNull(commandLineArguments, "commandLineArguments");

        final QubProcess result = createProcess(output, commandLineArguments);
        result.setFileSystem(currentFolder.getFileSystem());
        result.setCurrentFolderPath(currentFolder.getPath());

        PostCondition.assertNotNull(result, "result");

        return result;
    }
}
