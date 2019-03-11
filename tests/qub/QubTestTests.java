package qub;

public class QubTestTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(QubTest.class, () ->
        {
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

                runner.test("with /? command line argument", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    try (final Console console = createConsole(output, "/?"))
                    {
                        main(console);
                        test.assertEqual(-1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Usage: qub-test [[-folder=]<folder-path-to-test>] [-pattern=<test-name-pattern>] [-coverage] [-verbose]",
                            "  Used to run tests in source code projects.",
                            "  -folder: The folder to run tests in. This can be specified either with the",
                            "           -folder argument name or without it.",
                            "  -pattern: The pattern to match against tests to determine if they will be run",
                            "            or not.",
                            "  -coverage: Whether or not to collect code coverage information while running tests.",
                            "  -verbose: Whether or not to show verbose logs."),
                        Strings.getLines(output.getText().await()));
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
                            "Usage: qub-test [[-folder=]<folder-path-to-test>] [-pattern=<test-name-pattern>] [-coverage] [-verbose]",
                            "  Used to run tests in source code projects.",
                            "  -folder: The folder to run tests in. This can be specified either with the",
                            "           -folder argument name or without it.",
                            "  -pattern: The pattern to match against tests to determine if they will be run",
                            "            or not.",
                            "  -coverage: Whether or not to collect code coverage information while running tests.",
                            "  -verbose: Whether or not to show verbose logs."),
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
                            "Compiling...",
                            "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with named folder argument to unrooted folder that doesn't exist", (Test test) ->
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
                            "Compiling...",
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
                            "Compiling...",
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
                            "Compiling...",
                            "ERROR: The file at \"/i/dont/exist/project.json\" doesn't exist."),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with no source files", (Test test) ->
                {
                    final InMemoryCharacterStream output = getInMemoryCharacterStream(test);
                    final Folder currentFolder = getInMemoryCurrentFolder(test);
                    currentFolder.getFile("project.json").await()
                        .setContentsAsString(JSON.object(projectJson ->
                        {
                            projectJson.objectProperty("java");
                        }).toString()).await();

                    try (final Console console = createConsole(output, currentFolder))
                    {
                        main(console);
                        test.assertEqual(1, console.getExitCode());
                    }
                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "ERROR: No java source files found in /."),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file", (Test test) ->
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

                    try (final Console console = createConsole(output, currentFolder))
                    {
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "Creating jar file...",
                            "Running tests...",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file and verbose", (Test test) ->
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

                    try (final Console console = createConsole(output, currentFolder, "-verbose"))
                    {
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "VERBOSE: Starting compilation...",
                            "VERBOSE: Compilation finished.",
                            "Creating jar file...",
                            "Running tests...",
                            "VERBOSE: java.exe -classpath /outputs qub.ConsoleTestRunner A",
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
                        console.setEnvironmentVariables(Map.<String,String>create()
                            .set("QUB_HOME", "/qub/"));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "Creating jar file...",
                            "Running tests...",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });

                runner.test("with one source file, verbose, and coverage", (Test test) ->
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
                        qubFolder.createFile("jacoco/jacococli/0.8.1/jacocoagent.jar").await();
                        console.setEnvironmentVariables(Map.<String,String>create()
                            .set("QUB_HOME", "/qub/"));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "VERBOSE: Starting compilation...",
                            "VERBOSE: Compilation finished.",
                            "Creating jar file...",
                            "Running tests...",
                            "VERBOSE: java.exe -javaagent:/qub/jacoco/jacococli/0.8.1/jacocoagent.jar=destfile=/outputs/coverage.exec -classpath /outputs qub.ConsoleTestRunner A",
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
                        console.setEnvironmentVariables(Map.<String,String>create()
                            .set("QUB_HOME", "/qub/"));
                        main(console);
                        test.assertEqual(0, console.getExitCode());
                    }

                    test.assertEqual(
                        Iterable.create(
                            "Compiling...",
                            "VERBOSE: Parsing project.json...",
                            "VERBOSE: Detecting java source files to compile...",
                            "VERBOSE: Compiling all source files.",
                            "VERBOSE: Starting compilation...",
                            "VERBOSE: Compilation finished.",
                            "Creating jar file...",
                            "Running tests...",
                            "VERBOSE: java.exe -javaagent:/qub/jacoco/jacococli/0.9.2/jacocoagent.jar=destfile=/outputs/coverage.exec -classpath /outputs qub.ConsoleTestRunner A",
                            ""),
                        Strings.getLines(output.getText().await()).skipLast());
                });
            });
        });
    }

    private static InMemoryCharacterStream getInMemoryCharacterStream(Test test)
    {
        return new InMemoryCharacterStream(test.getParallelAsyncRunner());
    }

    private static InMemoryFileSystem getInMemoryFileSystem(Test test)
    {
        PreCondition.assertNotNull(test, "test");

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getParallelAsyncRunner(), test.getClock());
        fileSystem.createRoot("/");

        return fileSystem;
    }

    private static Folder getInMemoryCurrentFolder(Test test)
    {
        PreCondition.assertNotNull(test, "test");

        return getInMemoryFileSystem(test).getFolder("/").await();
    }

    private static Console createConsole(CharacterWriteStream output, String... commandLineArguments)
    {
        PreCondition.assertNotNull(output, "output");
        PreCondition.assertNotNull(commandLineArguments, "commandLineArguments");

        final Console result = new Console(Iterable.create(commandLineArguments));
        result.setLineSeparator("\n");
        result.setOutput(output);

        return result;
    }

    private static Console createConsole(CharacterWriteStream output, Folder currentFolder, String... commandLineArguments)
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

    private static void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final QubBuild build = new QubBuild();
        build.setJavaCompiler(new FakeJavaCompiler());
        build.setJarCreator(new FakeJarCreator());

        final QubTest test = new QubTest();
        test.setJavaRunner(new FakeJavaRunner());
        test.setQubBuild(build);

        test.main(console);
    }
}
