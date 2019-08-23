package qub;

public interface FakeJavaRunnerTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(FakeJavaRunner.class, () ->
        {
            JavaRunnerTests.test(runner, FakeJavaRunner::new);

            runner.test("constructor()", (Test test) ->
            {
                final FakeJavaRunner javaRunner = new FakeJavaRunner();
                test.assertEqual(0, javaRunner.getExitCode());
            });

            runner.test("setExitCode()", (Test test) ->
            {
                final FakeJavaRunner javaRunner = new FakeJavaRunner();
                final FakeJavaRunner setResult = javaRunner.setExitCode(13);
                test.assertSame(javaRunner, setResult);
                test.assertEqual(13, javaRunner.getExitCode());
            });

            runner.testGroup("run()", () ->
            {
                runner.test("with null Console", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    test.assertThrows(() -> javaRunner.run(null),
                        new PreConditionFailure("console cannot be null."));
                });

                runner.test("with non-null console", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    try (final Console console = new Console())
                    {
                        console.setOutputCharacterWriteStream(outputStream);

                        test.assertNull(javaRunner.run(console).await());

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("\n", outputStream.getText().await());
                    }
                });

                runner.test("with --verbose but no class paths set", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose");
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();
                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        test.assertThrows(() -> javaRunner.run(console),
                            new PostConditionFailure("result cannot be null."));

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("", outputStream.getText().await());
                    }
                });

                runner.test("with --verbose but no outputFolder", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose");
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();
                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        javaRunner.setClassPaths(Iterable.create("a", "b"));

                        test.assertThrows(() -> javaRunner.run(console),
                            new PostConditionFailure("result cannot be null."));

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("", outputStream.getText().await());
                    }
                });

                runner.test("with --verbose when output folder doesn't exist", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose");
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();
                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        javaRunner.setClassPaths(Iterable.create("a", "b"));

                        javaRunner.setOutputFolder(fileSystem.getFolder("/outputs/").await());

                        test.assertNull(javaRunner.run(console).await());

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual(
                            Iterable.create(
                                "VERBOSE: java.exe -classpath a;b qub.ConsoleTestRunner --output-folder=/outputs",
                                ""),
                            Strings.getLines(outputStream.getText().await()));
                    }
                });

                runner.test("with --verbose", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose");
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();
                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        javaRunner.setClassPaths(Iterable.create("a", "b"));

                        javaRunner.setOutputFolder(outputFolder);

                        test.assertNull(javaRunner.run(console).await());

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("VERBOSE: java.exe -classpath a;b qub.ConsoleTestRunner --output-folder=/outputs\n\n", outputStream.getText().await());
                    }
                });

                runner.test("with --verbose and --profiler", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose", "--profiler");
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();

                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        final CommandLineParameterProfiler profiler = parameters.addProfiler(console, QubTest.class);
                        javaRunner.setProfiler(profiler);

                        javaRunner.setClassPaths(Iterable.create("a", "b"));

                        javaRunner.setOutputFolder(outputFolder);

                        test.assertNull(javaRunner.run(console).await());

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("VERBOSE: java.exe -classpath a;b qub.ConsoleTestRunner --profiler=true --output-folder=/outputs\n\n", outputStream.getText().await());
                    }
                });

                runner.test("with --verbose and --testjson", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose", "--testjson");
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();

                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        final CommandLineParameterBoolean testjson = parameters.addBoolean("testjson");
                        javaRunner.setTestJson(testjson);

                        javaRunner.setClassPaths(Iterable.create("a", "b"));

                        javaRunner.setOutputFolder(outputFolder);

                        test.assertNull(javaRunner.run(console).await());

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("VERBOSE: java.exe -classpath a;b qub.ConsoleTestRunner --testjson=true --output-folder=/outputs\n\n", outputStream.getText().await());
                    }
                });

                runner.test("with --verbose and with pattern set", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose");
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();

                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        javaRunner.setPattern("foo");

                        javaRunner.setClassPaths(Iterable.create("a", "b"));

                        javaRunner.setOutputFolder(outputFolder);

                        test.assertNull(javaRunner.run(console).await());

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("VERBOSE: java.exe -classpath a;b qub.ConsoleTestRunner --pattern=foo --output-folder=/outputs\n\n", outputStream.getText().await());
                    }
                });

                runner.test("with --verbose and jacocoFolder set", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose");
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    final Folder jacocoFolder = fileSystem.createFolder("/jacoco/").await();
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();

                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        javaRunner.setJacocoFolder(jacocoFolder);

                        javaRunner.setClassPaths(Iterable.create("a", "b"));

                        javaRunner.setOutputFolder(outputFolder);

                        test.assertNull(javaRunner.run(console).await());

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("VERBOSE: java.exe -javaagent:/jacoco/jacocoagent.jar=destfile=/outputs/coverage.exec -classpath a;b qub.ConsoleTestRunner --output-folder=/outputs\n\n", outputStream.getText().await());
                    }
                });

                runner.test("with --verbose and class files", (Test test) ->
                {
                    final FakeJavaRunner javaRunner = new FakeJavaRunner();

                    final InMemoryCharacterStream outputStream = new InMemoryCharacterStream();
                    final CommandLineArguments arguments = CommandLineArguments.create("--verbose");
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    outputFolder.createFile("C.class").await();
                    outputFolder.createFile("d/E.class").await();
                    try (final Console console = new Console(arguments))
                    {
                        console.setOutputCharacterWriteStream(outputStream);
                        final CommandLineParameters parameters = console.createCommandLineParameters();

                        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
                        javaRunner.setVerbose(verbose);

                        javaRunner.setClassPaths(Iterable.create("a", "b"));

                        javaRunner.setOutputFolder(outputFolder);

                        test.assertNull(javaRunner.run(console).await());

                        test.assertEqual(0, console.getExitCode());
                        test.assertEqual("VERBOSE: java.exe -classpath a;b qub.ConsoleTestRunner --output-folder=/outputs C d.E\n\n", outputStream.getText().await());
                    }
                });
            });
        });
    }
}
