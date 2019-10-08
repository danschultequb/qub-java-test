package qub;

public interface QubTestTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubTest.class, () ->
        {
            runner.testGroup("getJavaRunner()", () ->
            {
                runner.test("with no JavaRunner set", (Test test) ->
                {
                    final QubTest qubTest = new QubTest();
                    final JavaRunner javaRunner = qubTest.getJavaRunner();
                    test.assertNotNull(javaRunner);
                    test.assertTrue(javaRunner instanceof RealJavaRunner);
                    test.assertSame(javaRunner, qubTest.getJavaRunner());
                });

                runner.test("with null JavaRunner set", (Test test) ->
                {
                    final QubTest qubTest = new QubTest();
                    test.assertSame(qubTest, qubTest.setJavaRunner(null));
                    final JavaRunner javaRunner = qubTest.getJavaRunner();
                    test.assertNotNull(javaRunner);
                    test.assertTrue(javaRunner instanceof RealJavaRunner);
                    test.assertSame(javaRunner, qubTest.getJavaRunner());
                });

                runner.test("with non-null JavaRunner set", (Test test) ->
                {
                    final QubTest qubTest = new QubTest();
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();
                    test.assertSame(qubTest, qubTest.setJavaRunner(javaRunner));
                    test.assertSame(javaRunner, qubTest.getJavaRunner());
                });
            });

            runner.testGroup("getShowTotalDuration()", () ->
            {
                runner.test("with no value set", (Test test) ->
                {
                    final QubTest qubTest = new QubTest();
                    test.assertTrue(qubTest.getShowTotalDuration());
                });

                runner.test("with false set", (Test test) ->
                {
                    final QubTest qubTest = new QubTest();
                    test.assertSame(qubTest, qubTest.setShowTotalDuration(false));
                    test.assertFalse(qubTest.getShowTotalDuration());
                });

                runner.test("with true set", (Test test) ->
                {
                    final QubTest qubTest = new QubTest();
                    test.assertSame(qubTest, qubTest.setShowTotalDuration(true));
                    test.assertTrue(qubTest.getShowTotalDuration());
                });
            });

            runner.testGroup("main(String[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> QubTest.main((String[])null), new PreConditionFailure("args cannot be null."));
                });
            });

            runner.testGroup("main(Console)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> main((Console)null), new PreConditionFailure("console cannot be null."));
                });

                runner.test("with -? command line argument", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    try (final Console console = createConsole(output, "-?"))
                    {
                        main(console);
                        test.assertEqual(-1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Usage: qub-test [--folder=<folder-to-test>] [--pattern=<test-name-pattern>] [--coverage[=<None|Sources|Tests|All>]] [--testjson] [--jvm.classpath=<jvm.classpath-value>] [--verbose] [--profiler] [--help]",
                            "  Used to run tests in source code projects.",
                            "  --folder: The folder to run tests in. Defaults to the current folder.",
                            "  --pattern: The pattern to match against tests to determine if they will be run or not.",
                            "  --coverage(c): Whether or not to collect code coverage information while running tests.",
                            "  --testjson: Whether or not to write the test results to a test.json file.",
                            "  --jvm.classpath: The classpath that was passed to the JVM when this application was started.",
                            "  --verbose(v): Whether or not to show verbose logs.",
                            "  --profiler: Whether or not this application should pause before it is run to allow a profiler to be attached.",
                            "  --help(?): Show the help message for this application."),
                        Strings.getLines(output.getText().await()));
                });

                runner.test("with unnamed folder argument to rooted folder that doesn't exist", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    try (final Console console = createConsole(output, currentFolder, "/i/dont/exist"))
                    {
                        main(console);
                        test.assertEqual(1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with unnamed folder argument to unrooted folder that doesn't exist", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    try (final Console console = createConsole(output, currentFolder, "i/dont/exist"))
                    {
                        main(console);
                        test.assertEqual(1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with named folder argument to rooted folder that doesn't exist", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    try (final Console console = createConsole(output, currentFolder, "-folder=/i/dont/exist"))
                    {
                        main(console);
                        test.assertEqual(1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with named folder argument to unrooted folder that doesn't exist", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    try (final Console console = createConsole(output, currentFolder, "-folder=i/dont/exist"))
                    {
                        main(console);
                        test.assertEqual(1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with no source files", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(new ProjectJSON().setJava(new ProjectJSONJava()).toString()).await();

                    try (final Console console = createConsole(output, currentFolder))
                    {
                        main(console, false);
                        test.assertEqual(1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "ERROR: No java source files found in /."),
                        Strings.getLines(output.getText().await()));
                });

                runner.test("with one source file", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(new ProjectJSON().setJava(new ProjectJSONJava()).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder))
                    {
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "Compiling 1 file...",
                            "Running tests...",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file and verbose", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(new ProjectJSON().setJava(new ProjectJSONJava()).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose"))
                    {
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, and jvm.classpath=/foo/subfolder", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(new ProjectJSON().setJava(new ProjectJSONJava()).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose", "--jvm.classpath=/foo/subfolder"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        console.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs;/foo/subfolder qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with different dependency", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    final ProjectJSON projectJSON = new ProjectJSON()
                        .setJava(new ProjectJSONJava()
                            .setDependencies(Iterable.create(
                                new Dependency()
                                    .setPublisher("me")
                                    .setProject("a")
                                    .setVersion("5"))));
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(projectJSON.toString())
                        .await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/b/2/b.jar"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        qubFolder.createFile("me/b/2/b.jar").await();
                        console.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs;/qub/me/a/5/a.jar", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/a/5/a.jar sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs;/qub/me/a/5/a.jar;/qub/me/b/2/b.jar qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with same dependency", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    final ProjectJSON projectJSON = new ProjectJSON()
                        .setJava(new ProjectJSONJava()
                            .setDependencies(Iterable.create(
                                new Dependency()
                                    .setPublisher("me")
                                    .setProject("a")
                                    .setVersion("5"))));
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(projectJSON.toString())
                        .await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/a/5/a.jar"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        qubFolder.createFile("me/b/2/b.jar").await();
                        console.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs;/qub/me/a/5/a.jar", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/a/5/a.jar sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs;/qub/me/a/5/a.jar qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with same dependency with newer version", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    final ProjectJSON projectJSON = new ProjectJSON()
                        .setJava(new ProjectJSONJava()
                            .setDependencies(Iterable.create(
                                new Dependency()
                                    .setPublisher("me")
                                    .setProject("a")
                                    .setVersion("5"))));
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(projectJSON.toString())
                        .await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/a/6/a.jar"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        qubFolder.createFile("me/a/6/a.jar").await();
                        console.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs;/qub/me/a/5/a.jar", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/a/5/a.jar sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs;/qub/me/a/5/a.jar qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, 1 dependency, and jvm.classpath with same dependency with older version", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    final ProjectJSON projectJSON = new ProjectJSON()
                        .setJava(new ProjectJSONJava()
                            .setDependencies(Iterable.create(
                                new Dependency()
                                    .setPublisher("me")
                                    .setProject("a")
                                    .setVersion("5"))));
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(projectJSON.toString())
                        .await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/a/4/a.jar"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/a/4/a.jar").await();
                        qubFolder.createFile("me/a/5/a.jar").await();
                        console.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", "/qub/"));
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs;/qub/me/a/5/a.jar", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/a/5/a.jar sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs;/qub/me/a/5/a.jar qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, and jvm.classpath equal to current project", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    final ProjectJSON projectJSON = new ProjectJSON()
                        .setPublisher("me")
                        .setProject("stuff")
                        .setVersion("7")
                        .setJava(new ProjectJSONJava());
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(projectJSON.toString())
                        .await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose", "--jvm.classpath=/qub/me/stuff/7/stuff.jar"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("me/stuff/7/stuff.jar").await();
                        console.setEnvironmentVariables(new EnvironmentVariables()
                            .set("QUB_HOME", qubFolder.toString()));
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file and --testjson and --verbose", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(new ProjectJSON().setJava(new ProjectJSONJava()).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "--testjson", "--verbose"))
                    {
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file and --testjson=true and --verbose", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(new ProjectJSON().setJava(new ProjectJSONJava()).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "--testjson=true", "--verbose"))
                    {
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file and --testjson=false and --verbose", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(new ProjectJSON().setJava(new ProjectJSONJava()).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "--testjson=false", "--verbose"))
                    {
                        console.setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                            .add(new FakeProcessRun("javac")
                                .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                .setFunction(() ->
                                {
                                    currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs qub.ConsoleTestRunner --profiler=false --testjson=false --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file and coverage", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(JSON.object(projectJson ->
                        {
                            projectJson.objectProperty("java");
                        }).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-coverage"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("jacoco/jacococli/0.8.1/jacocoagent.jar").await();
                        console
                            .setEnvironmentVariables(new EnvironmentVariables()
                                .set("QUB_HOME", "/qub/"))
                            .setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                                .add(new FakeProcessRun("javac")
                                    .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                    .setFunction(() ->
                                    {
                                        currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                    })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "Compiling 1 file...",
                            "Running tests...",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, and coverage", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(new ProjectJSON().setJava(new ProjectJSONJava()).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose", "-coverage"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("jacoco/jacococli/0.8.1/jacocoagent.jar").await();
                        console
                            .setEnvironmentVariables(new EnvironmentVariables()
                                .set("QUB_HOME", "/qub/"))
                            .setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                                .add(new FakeProcessRun("javac")
                                    .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                    .setFunction(() ->
                                    {
                                        currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                    })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -javaagent:/qub/jacoco/jacococli/0.8.1/jacocoagent.jar=destfile=/outputs/coverage.exec -classpath /outputs qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=Sources A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, coverage, and multiple jacoco installations", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(JSON.object(projectJson ->
                        {
                            projectJson.objectProperty("java");
                        }).toString()).await();
                    currentFolder.getFile("sources/A.java").await()
                        .setContentsAsString("A.java source").await();

                    try (final Console console = createConsole(output, currentFolder, "-verbose", "-coverage"))
                    {
                        final Folder qubFolder = console.getFileSystem().createFolder("/qub/").await();
                        qubFolder.createFile("jacoco/jacococli/0.5.0/jacocoagent.jar").await();
                        qubFolder.createFile("jacoco/jacococli/0.8.1/jacocoagent.jar").await();
                        qubFolder.createFile("jacoco/jacococli/0.9.2/jacocoagent.jar").await();
                        console
                            .setEnvironmentVariables(new EnvironmentVariables()
                                .set("QUB_HOME", "/qub/"))
                            .setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                                .add(new FakeProcessRun("javac")
                                    .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs", "sources/A.java")
                                    .setFunction(() ->
                                    {
                                        currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                    })));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -javaagent:/qub/jacoco/jacococli/0.9.2/jacocoagent.jar=destfile=/outputs/coverage.exec -classpath /outputs qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=Sources A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, one dependency, and verbose", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    final ProjectJSON aProjectJSON = new ProjectJSON()
                        .setPublisher("me")
                        .setProject("a")
                        .setVersion("1")
                        .setJava(new ProjectJSONJava()
                            .setDependencies(Iterable.create(
                                new Dependency()
                                    .setPublisher("me")
                                    .setProject("b")
                                    .setVersion("2"))));
                    final ProjectJSON bProjectJSON = new ProjectJSON()
                        .setPublisher("me")
                        .setProject("b")
                        .setVersion("2");
                    currentFolder.setFileContentsAsString("project.json", JSON.object(aProjectJSON::write).toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();
                    try (final Console console = createConsole(output, currentFolder, "-verbose"))
                    {
                        final Folder qubFolder = console.getFileSystem().getFolder("/qub/").await();
                        qubFolder.setFileContentsAsString("me/b/2/project.json", JSON.object(bProjectJSON::write).toString()).await();
                        qubFolder.createFile("me/b/2/b.jar").await();

                        console
                            .setEnvironmentVariables(new EnvironmentVariables()
                                .set("QUB_HOME", qubFolder.toString()))
                            .setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                                .add(new FakeProcessRun("javac")
                                    .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs;/qub/me/b/2/b.jar", "sources/A.java")
                                    .setFunction(() ->
                                    {
                                        currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                    })));

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/b/2/b.jar sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs;/qub/me/b/2/b.jar qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, one transitive dependency, and verbose", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    final ProjectJSON aProjectJSON = new ProjectJSON()
                        .setPublisher("me")
                        .setProject("a")
                        .setVersion("1")
                        .setJava(new ProjectJSONJava()
                            .setDependencies(Iterable.create(
                                new Dependency()
                                    .setPublisher("me")
                                    .setProject("b")
                                    .setVersion("2"))));
                    final ProjectJSON bProjectJSON = new ProjectJSON()
                        .setPublisher("me")
                        .setProject("b")
                        .setVersion("2")
                        .setJava(new ProjectJSONJava()
                            .setDependencies(Iterable.create(
                                new Dependency()
                                    .setPublisher("me")
                                    .setProject("c")
                                    .setVersion("3"))));
                    final ProjectJSON cProjectJSON = new ProjectJSON()
                        .setPublisher("me")
                        .setProject("c")
                        .setVersion("3");
                    currentFolder.setFileContentsAsString("project.json", JSON.object(aProjectJSON::write).toString()).await();
                    currentFolder.setFileContentsAsString("sources/A.java", "A.java source").await();
                    try (final Console console = createConsole(output, currentFolder, "-verbose"))
                    {
                        final Folder qubFolder = console.getFileSystem().getFolder("/qub/").await();
                        qubFolder.setFileContentsAsString("me/b/2/project.json", JSON.object(bProjectJSON::write).toString()).await();
                        qubFolder.createFile("me/b/2/b.jar").await();
                        qubFolder.setFileContentsAsString("me/c/3/project.json", JSON.object(cProjectJSON::write).toString()).await();
                        qubFolder.createFile("me/c/3/c.jar").await();

                        console
                            .setEnvironmentVariables(new EnvironmentVariables()
                                .set("QUB_HOME", "/qub/"))
                            .setProcessFactory(new FakeProcessFactory(test.getParallelAsyncRunner(), console.getCurrentFolderPath())
                                .add(new FakeProcessRun("javac")
                                    .addArguments("-d", "/outputs", "-Xlint:unchecked", "-Xlint:deprecation", "-classpath", "/outputs;/qub/me/b/2/b.jar;/qub/me/c/3/c.jar", "sources/A.java")
                                    .setFunction(() ->
                                    {
                                        currentFolder.setFileContentsAsString("outputs/A.class", "A.java bytecode").await();
                                    })));

                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Updating outputs/build.json...",
                            "VERBOSE: Setting project.json...",
                            "VERBOSE: Setting source files...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "Compiling 1 file...",
                            "VERBOSE: Running /: javac -d /outputs -Xlint:unchecked -Xlint:deprecation -classpath /outputs;/qub/me/b/2/b.jar;/qub/me/c/3/c.jar sources/A.java...",
                            "VERBOSE: Compilation finished.",
                            "VERBOSE: Writing build.json file...",
                            "VERBOSE: Done writing build.json file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs;/qub/me/b/2/b.jar;/qub/me/c/3/c.jar qub.ConsoleTestRunner --profiler=false --testjson=true --output-folder=/outputs --coverage=None A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });
            });
        });
    }

    static InMemoryCharacterStream getInMemoryCharacterStream(Test test)
    {
        return new InMemoryCharacterStream();
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

    static Console createConsole(CharacterWriteStream output, String... commandLineArguments)
    {
        PreCondition.assertNotNull(output, "output");
        PreCondition.assertNotNull(commandLineArguments, "commandLineArguments");

        final Console result = new Console(CommandLineArguments.create(commandLineArguments));
        result.setLineSeparator("\n");
        result.setOutputCharacterWriteStream(output);

        return result;
    }

    static Console createConsole(CharacterWriteStream output, Folder currentFolder, String... commandLineArguments)
    {
        PreCondition.assertNotNull(output, "output");
        PreCondition.assertNotNull(currentFolder, "currentFolder");
        PreCondition.assertNotNull(commandLineArguments, "commandLineArguments");

        final Console result = createConsole(output, commandLineArguments);
        result.setFileSystem(currentFolder.getFileSystem());
        result.setCurrentFolderPath(currentFolder.getPath());

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    static void main(Console console)
    {
        main(console, true);
    }

    static void main(Console console, boolean showTotalDuration)
    {
        PreCondition.assertNotNull(console, "console");

        final QubTest test = new QubTest();
        test.setJavaRunner(new FakeJavaRunner());
        test.setShowTotalDuration(showTotalDuration);

        test.main(console);
    }
}
