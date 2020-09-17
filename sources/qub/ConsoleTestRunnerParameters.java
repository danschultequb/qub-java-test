package qub;

/**
 * Parameters that are passed to ConsoleTestRunner.run().
 */
public class ConsoleTestRunnerParameters
{
    private final QubProcess process;
    private final VerboseCharacterWriteStream verbose;
    private final Folder outputFolder;
    private final Iterable<String> testClassNames;

    private PathPattern pattern;
    private Coverage coverage;
    private Boolean testJson;
    private File logFile;

    /**
     * Create a new ConsoleTestRunnerParameters object.
     * @param process The process that is running the tests.
     * @param verbose The stream that verbose text will be written to.
     * @param outputFolder The folder where test classes to run will be found.
     * @param testClassNames The names of the classes that should have their tests run.
     */
    public ConsoleTestRunnerParameters(QubProcess process, VerboseCharacterWriteStream verbose, Folder outputFolder, Iterable<String> testClassNames)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(verbose, "verbose");
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(testClassNames, "testClassNames");

        this.process = process;
        this.verbose = verbose;
        this.outputFolder = outputFolder;
        this.testClassNames = testClassNames;
    }

    /**
     * Get the process that is running the tests.
     * @return The process that is running the tests.
     */
    public QubProcess getProcess()
    {
        return this.process;
    }

    /**
     * Get the stream that verbose text will be written to.
     * @return The stream that verbose text will be written to.
     */
    public VerboseCharacterWriteStream getVerbose()
    {
        return this.verbose;
    }

    /**
     * Get the folder where test classes to run will be found.
     * @return The folder where test classes to run will be found.
     */
    public Folder getOutputFolder()
    {
        return this.outputFolder;
    }

    /**
     * Get the names of the classes that should have their tests run.
     * @return The names of the classes that should have their tests run.
     */
    public Iterable<String> getTestClassNames()
    {
        return this.testClassNames;
    }

    /**
     * Set the pattern that test names will be compared against to determine whether or not they
     * will be run.
     * @param pattern The pattern that test names will be compared against to determine whether or
     *                not they will be run.
     * @return This object for method chaining.
     */
    public ConsoleTestRunnerParameters setPattern(PathPattern pattern)
    {
        this.pattern = pattern;
        return this;
    }

    /**
     * Get the pattern that test names will be compared against to determine whether or not they
     * will be run.
     * @return The pattern that test names will be compared against to determine whether or not
     * they will be run.
     */
    public PathPattern getPattern()
    {
        return this.pattern;
    }

    public ConsoleTestRunnerParameters setCoverage(Coverage coverage)
    {
        this.coverage = coverage;
        return this;
    }

    public Coverage getCoverage()
    {
        return this.coverage;
    }

    public ConsoleTestRunnerParameters setTestJson(Boolean testJson)
    {
        this.testJson = testJson;
        return this;
    }

    public Boolean getTestJson()
    {
        return this.testJson;
    }

    public ConsoleTestRunnerParameters setLogFile(File logFile)
    {
        this.logFile = logFile;
        return this;
    }

    public File getLogFile()
    {
        return this.logFile;
    }
}
