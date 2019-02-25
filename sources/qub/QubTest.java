package qub;

public class QubTest
{
    private Build build;
    private JavaRunner javaRunner;

    /**
     * Set the Build object that will be used to build the source code.
     * @param build The Build object that will be used to build the source code.
     * @return This object for method chaining.
     */
    public QubTest setBuild(Build build)
    {
        this.build = build;
        return this;
    }

    /**
     * Get the Build object that will be used to build the source code. If no Build object has been
     * set, a default one will be created and returned.
     * @return The Build object that will be used to build the source code.
     */
    public Build getBuild()
    {
        if (build == null)
        {
            build = new Build();
        }
        final Build result = build;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Set the JavaRunner that will be used to run tests.
     * @param javaRunner The JavaRunner that will be used to run tests.
     * @return This object for method chaining.
     */
    public QubTest setJavaRunner(JavaRunner javaRunner)
    {
        PreCondition.assertNotNull(javaRunner, "javaRunner");

        this.javaRunner = javaRunner;

        return this;
    }

    public JavaRunner getJavaRunner()
    {
        if (javaRunner == null)
        {
            javaRunner = new RealJavaRunner();
        }
        final JavaRunner result = javaRunner;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        if (shouldShowUsage(console))
        {
            console.writeLine("Usage: qub-test [[-folder=]<folder-path-to-test>] [-pattern=<test-name-pattern>] [-coverage] [-verbose]");
            console.writeLine("  Used to run tests in source code projects.");
            console.writeLine("  -folder: The folder to run tests in. This can be specified either with the");
            console.writeLine("           -folder argument name or without it.");
            console.writeLine("  -pattern: The pattern to match against tests to determine if they will be run");
            console.writeLine("            or not.");
            console.writeLine("  -coverage: Whether or not to collect code coverage information while running tests.");
            console.writeLine("  -verbose: Whether or not to show verbose logs.");
            console.setExitCode(-1);
        }
        else
        {
            final Stopwatch stopwatch = console.getStopwatch();
            stopwatch.start();
            try
            {
                final Build build = getBuild();
                build.setShowTotalDuration(false);
                build.main(console);

                if (console.getExitCode() == 0)
                {
                    console.writeLine("Running tests...").await();

                    final Folder folderToTest = getFolderToTest(console);
                    final String pattern = getPattern(console);
                    final boolean coverage = getCoverage(console);
                    final Folder outputFolder = folderToTest.getFolder("outputs").await();
                    final Folder sourceFolder = folderToTest.getFolder("sources").await();

                    final String qubHome = console.getEnvironmentVariable("QUB_HOME");
                    final Folder qubFolder = console.getFileSystem().getFolder(qubHome).await();

                    final List<String> classPaths = List.create(outputFolder.toString());

                    final File projectJsonFile = folderToTest.getFile("project.json").await();
                    final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).await();

                    final Iterable<Dependency> dependencies = projectJson.getJava().getDependencies();
                    if (!Iterable.isNullOrEmpty(dependencies))
                    {
                        classPaths.addAll(dependencies.map((Dependency dependency) ->
                        {
                            final String dependencyRelativePath =
                                dependency.getPublisher() + "/" +
                                    dependency.getProject() + "/" +
                                    dependency.getVersion() + "/" +
                                    dependency.getProject() + ".jar";
                            return qubFolder.getFile(dependencyRelativePath).await().toString();
                        }));
                    }

                    Folder jacocoFolder = null;
                    if (coverage)
                    {
                        jacocoFolder = qubFolder.getFolder("jacoco/jacococli").await()
                            .getFolders().await()
                            .maximum((Folder lhs, Folder rhs) -> VersionNumber.parse(lhs.getName()).compareTo(VersionNumber.parse(rhs.getName())));
                    }

                    final JavaRunner javaTestRunner = getJavaRunner();
                    javaTestRunner.setClassPaths(classPaths);
                    javaTestRunner.setPattern(pattern);
                    javaTestRunner.setOutputFolder(outputFolder);
                    javaTestRunner.setJacocoFolder(jacocoFolder);
                    javaTestRunner.setSourceFolder(sourceFolder);
                    javaTestRunner.run(console).await();
                }
            }
            finally
            {
                final Duration compilationDuration = stopwatch.stop().toSeconds();
                console.writeLine("Done (" + compilationDuration.toString("0.0") + ")").await();
            }
        }
    }

    private static boolean shouldShowUsage(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        return console.getCommandLine().contains(
            (CommandLineArgument argument) ->
            {
                final String argumentString = argument.toString();
                return argumentString.equals("/?") || argumentString.equals("-?");
            });
    }

    private static Path getFolderPathToTest(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        Path result = null;
        final CommandLine commandLine = console.getCommandLine();
        if (commandLine.any())
        {
            CommandLineArgument folderArgument = commandLine.get("folder");
            if (folderArgument == null)
            {
                folderArgument = commandLine.getArguments()
                    .first((CommandLineArgument argument) -> argument.getName() == null);
            }
            if (folderArgument != null)
            {
                result = Path.parse(folderArgument.getValue());
            }
        }

        if (result == null)
        {
            result = console.getCurrentFolderPath();
        }

        if (!result.isRooted())
        {
            result = console.getCurrentFolderPath().resolve(result).await();
        }

        PostCondition.assertNotNull(result, "result");
        PostCondition.assertTrue(result.isRooted(), "result.isRooted()");

        return result;
    }

    private static Folder getFolderToTest(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final Folder result = console.getFileSystem().getFolder(getFolderPathToTest(console)).await();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    private static String getPattern(Console console)
    {
        String pattern = null;
        CommandLineArgument patternArgument = console.getCommandLine().get("pattern");
        if (patternArgument != null)
        {
            pattern = patternArgument.getValue();
        }
        return pattern;
    }

    private static boolean getCoverage(Console console)
    {
        boolean result = false;

        CommandLineArgument coverageArgument = console.getCommandLine().get("coverage");
        if (coverageArgument != null)
        {
            final String coverageArgumentValue = coverageArgument.getValue();
            result = Strings.isNullOrEmpty(coverageArgumentValue) ||
                Booleans.isTrue(java.lang.Boolean.valueOf(coverageArgumentValue));
        }

        return result;
    }

    public static boolean isVerbose(Console console)
    {
        boolean result = false;

        CommandLineArgument verboseArgument = console.getCommandLine().get("verbose");
        if (verboseArgument != null)
        {
            final String verboseArgumentValue = verboseArgument.getValue();
            result = Strings.isNullOrEmpty(verboseArgumentValue) ||
                Booleans.isTrue(java.lang.Boolean.valueOf(verboseArgumentValue));
        }

        return result;
    }

    public static Result<Void> verbose(Console console, String message)
    {
        return verbose(console, false, message);
    }

    public static Result<Void> verbose(Console console, boolean showTimestamp, String message)
    {
        PreCondition.assertNotNull(console, "console");
        PreCondition.assertNotNull(message, "message");

        Result<Void> result = Result.success();
        if (isVerbose(console))
        {
            result = console.writeLine("VERBOSE" + (showTimestamp ? "(" + System.currentTimeMillis() + ")" : "") + ": " + message)
                .then(() -> {});
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public static Result<Void> error(Console console, String message)
    {
        return error(console, false, message);
    }

    public static Result<Void> error(Console console, boolean showTimestamp, String message)
    {
        PreCondition.assertNotNull(console, "console");
        PreCondition.assertNotNull(message, "message");

        final Result<Void> result = console.writeLine("ERROR" + (showTimestamp ? "(" + System.currentTimeMillis() + ")" : "") + ": " + message).then(() -> {});
        console.incrementExitCode();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public static void main(String[] args)
    {
        Console.run(args, (Console console) -> new QubTest().main(console));
    }
}