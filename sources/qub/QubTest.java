package qub;

public class QubTest
{
    private QubBuild qubBuild;
    private JavaRunner javaRunner;
    private Boolean showTotalDuration;

    /**
     * Set the QubBuild object that will be used to build the source code.
     * @param qubBuild The QubBuild object that will be used to build the source code.
     * @return This object for method chaining.
     */
    public QubTest setQubBuild(QubBuild qubBuild)
    {
        this.qubBuild = qubBuild;
        return this;
    }

    /**
     * Get the Build object that will be used to qubBuild the source code. If no Build object has been
     * set, a default one will be created and returned.
     * @return The Build object that will be used to qubBuild the source code.
     */
    public QubBuild getQubBuild()
    {
        if (qubBuild == null)
        {
            qubBuild = new QubBuild();
        }
        final QubBuild result = qubBuild;

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

    public void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final CommandLineParameters parameters = console.createCommandLineParameters();
        final CommandLineParameter<Folder> folderToTestParameter = parameters.addPositionalFolder("folder", console)
            .setValueName("<folder-to-test>")
            .setDescription("The folder to run tests in. Defaults to the current folder.");
        final CommandLineParameter<String> patternParameter = parameters.addString("pattern")
            .setValueName("<test-name-pattern>")
            .setDescription("The pattern to match against tests to determine if they will be run or not.");
        final CommandLineParameterBoolean coverageParameter = parameters.addBoolean("coverage")
            .setDescription("Whether or not to collect code coverage information while running tests.");
        final CommandLineParameterBoolean testJsonParameter = parameters.addBoolean("testjson")
            .setDescription("Whether or not to write the test results to a test.json file.");
        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(console, QubTest.class);
        final CommandLineParameterBoolean help = parameters.addHelp();

        if (help.getValue().await())
        {
            parameters.writeHelpLines(console, "qub-test", "Used to run tests in source code projects.").await();
            console.setExitCode(-1);
        }
        else
        {
            profilerParameter.await();
            profilerParameter.removeValue().await();

            final boolean showTotalDuration = getShowTotalDuration();
            final Stopwatch stopwatch = console.getStopwatch();
            if (showTotalDuration)
            {
                stopwatch.start();
            }
            try
            {
                final QubBuild qubBuild = getQubBuild();
                qubBuild.setShowTotalDuration(false);
                qubBuild.main(console);

                if (console.getExitCode() == 0)
                {
                    console.writeLine("Running tests...").await();

                    final Folder folderToTest = folderToTestParameter.getValue().await();
                    final String pattern = patternParameter.getValue().await();
                    final boolean coverage = coverageParameter.getValue().await();

                    final Folder outputFolder = folderToTest.getFolder("outputs").await();
                    final Folder sourceFolder = folderToTest.getFolder("sources").await();
                    final Folder testFolder = folderToTest.getFolder("tests").await();

                    final List<String> classPaths = List.create(outputFolder.toString());

                    final File projectJsonFile = folderToTest.getFile("project.json").await();
                    final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).await();

                    Iterable<Dependency> dependencies = projectJson.getJava().getDependencies();
                    if (!Iterable.isNullOrEmpty(dependencies))
                    {
                        final Folder qubFolder = getQubHomeFolder(console);
                        dependencies = QubBuild.getAllDependencies(qubFolder, dependencies).getKeys();
                        classPaths.addAll(dependencies.map((Dependency dependency) ->
                        {
                            return QubBuild.resolveDependencyReference(qubFolder, dependency).toString();
                        }));
                    }

                    Folder jacocoFolder = null;
                    if (coverage)
                    {
                        final Folder qubFolder = getQubHomeFolder(console);
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
                    javaTestRunner.setTestFolder(testFolder);
                    javaTestRunner.setVerbose(verbose);
                    javaTestRunner.setProfiler(profilerParameter);
                    javaTestRunner.setTestJson(testJsonParameter);
                    javaTestRunner.run(console).await();
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

    private static Folder getQubHomeFolder(Console console)
    {
        PreCondition.assertNotNullAndNotEmpty(console.getEnvironmentVariable("QUB_HOME"), "console.getEnvironmentVariable(\"QUB_HOME\")");

        final String qubHome = console.getEnvironmentVariable("QUB_HOME");
        final Folder result = console.getFileSystem().getFolder(qubHome).await();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public static void main(String[] args)
    {
        final QubTest qubTest = new QubTest();
        Console.run(args, qubTest::main);
    }
}