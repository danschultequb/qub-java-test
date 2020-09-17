package qub;

public interface ConsoleTestRunnerArguments<T>
{
    /**
     * Add the provided arguments to the list of arguments that will be provided to the executable
     * when this ProcessBuilder is run.
     * @param arguments The arguments to add.
     * @return This object for method chaining.
     */
    T addArguments(String... arguments);

    /**
     * Add the provided arguments to the list of arguments that will be provided to the executable
     * when this ProcessBuilder is run.
     * @param arguments The arguments to add.
     * @return This object for method chaining.
     */
    T addArguments(Iterable<String> arguments);

    /**
     * Add the full class name of ConsoleTestRunner to the arguments.
     * @return This object for method chaining.
     */
    default T addConsoleTestRunnerFullClassName()
    {
        return this.addArguments(Types.getFullTypeName(ConsoleTestRunner.class));
    }

    /**
     * Add a profiler argument to this process builder.
     * @param profiler The value of the profiler argument to add.
     * @return This object for method chaining.
     */
    default T addProfiler(boolean profiler)
    {
        return this.addArguments("--profiler=" + profiler);
    }

    /**
     * Add a verbose argument to this process builder.
     * @param verbose The value of the verbose argument to add.
     * @return This object for method chaining.
     */
    default T addVerbose(boolean verbose)
    {
        return this.addArguments("--verbose=" + verbose);
    }

    /**
     * Add a testjson argument to this process builder.
     * @param testJson The value of the testjson argument to add.
     * @return This object for method chaining.
     */
    default T addTestJson(boolean testJson)
    {
        return this.addArguments("--testjson=" + testJson);
    }

    /**
     * Add a logfile argument to this process builder.
     * @param logFile The path to the log file to use.
     * @return This object for method chaining.
     */
    default T addLogFile(File logFile)
    {
        return this.addArguments("--logfile=" + logFile);
    }

    /**
     * Add a pattern argument to this process builder.
     * @param pattern The value of the pattern argument to add.
     * @return This object for method chaining.
     */
    default T addPattern(String pattern)
    {
        PreCondition.assertNotNullAndNotEmpty(pattern, "pattern");

        return this.addArguments("--pattern=" + pattern);
    }

    /**
     * Add an output-folder argument to this process builder.
     * @param outputFolderPath The value of the output-folder argument to add.
     * @return This object for method chaining.
     */
    default T addOutputFolder(String outputFolderPath)
    {
        PreCondition.assertNotNullAndNotEmpty(outputFolderPath, "outputFolderPath");

        return this.addOutputFolder(Path.parse(outputFolderPath));
    }

    /**
     * Add an output-folder argument to this process builder.
     * @param outputFolderPath The value of the output-folder argument to add.
     * @return This object for method chaining.
     */
    default T addOutputFolder(Path outputFolderPath)
    {
        PreCondition.assertNotNull(outputFolderPath, "outputFolderPath");

        return this.addArguments("--output-folder=" + outputFolderPath.toString());
    }

    /**
     * Add an output-folder argument to this process builder.
     * @param outputFolder The value of the output-folder argument to add.
     * @return This object for method chaining.
     */
    default T addOutputFolder(Folder outputFolder)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");

        return this.addOutputFolder(outputFolder.getPath());
    }

    /**
     * Add a coverage argument to this process builder.
     * @param coverage The value of the coverage argument to add.
     * @return This object for method chaining.
     */
    default T addCoverage(Coverage coverage)
    {
        PreCondition.assertNotNull(coverage, "coverage");

        return this.addArguments("--coverage=" + coverage);
    }

    /**
     * Add the full class name arguments to this process builder.
     * @param fullClassNamesToTest The full names of the classes to test.
     * @return This object for method chaining.
     */
    default T addFullClassNamesToTest(Iterable<String> fullClassNamesToTest)
    {
        PreCondition.assertNotNullAndNotEmpty(fullClassNamesToTest, "fullClassNamesToTest");

        return this.addArguments(fullClassNamesToTest);
    }

    /**
     * Add the class files to test to this process builder.
     * @param classFilesToTest The class files to test.
     * @return This object for method chaining.
     */
    default T addClassesToTest(Iterable<File> classFilesToTest, Folder outputFolder)
    {
        PreCondition.assertNotNullAndNotEmpty(classFilesToTest, "classesToTest");
        PreCondition.assertNotNull(outputFolder, "outputFolder");

        return this.addFullClassNamesToTest(classFilesToTest.map((File classFileToTest) ->
        {
            return QubTestRun.getFullClassName(outputFolder, classFileToTest);
        }));
    }
}
