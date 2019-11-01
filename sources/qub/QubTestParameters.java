package qub;

/**
 * Parameters that are passed to QubTest.run().
 */
public class QubTestParameters
{
    private final CharacterWriteStream output;
    private final Folder folderToTest;
    private final EnvironmentVariables environmentVariables;
    private final ProcessFactory processFactory;
    private final VerboseCharacterWriteStream verbose;

    private String pattern;
    private Coverage coverage;
    private boolean testJson;
    private String jvmClassPath;
    private boolean profiler;

    /**
     * Create a new QubTestParameters object.
     * @param output The CharacterWriteStream that output lines should be written to.
     * @param folderToTest The folder that should have its tests run.
     * @param environmentVariables The environment variables of the running process.
     * @param processFactory The factory that will be used to create new processes.
     * @param verbose The CharacterWriteStream that verbose lines should be written to.
     */
    public QubTestParameters(CharacterWriteStream output, Folder folderToTest, EnvironmentVariables environmentVariables, ProcessFactory processFactory, VerboseCharacterWriteStream verbose)
    {
        PreCondition.assertNotNull(output, "output");
        PreCondition.assertNotNull(folderToTest, "folderToTest");
        PreCondition.assertNotNull(environmentVariables, "environmentVariables");
        PreCondition.assertNotNull(processFactory, "processFactory");
        PreCondition.assertNotNull(verbose, "verbose");

        this.output = output;
        this.folderToTest = folderToTest;
        this.environmentVariables = environmentVariables;
        this.processFactory = processFactory;
        this.verbose = verbose;
    }

    /**
     * Get the CharacterWriteStream that output lines should be written to.
     * @return The CharacterWriteStream that output lines should be written to.
     */
    public CharacterWriteStream getOutput()
    {
        return this.output;
    }

    /**
     * Get the folder that should have its tests run.
     * @return The folder that should have its tests run.
     */
    public Folder getFolderToTest()
    {
        return this.folderToTest;
    }

    /**
     * Get the environment variables of the running process.
     * @return The environment variables of the running process.
     */
    public EnvironmentVariables getEnvironmentVariables()
    {
        return this.environmentVariables;
    }

    /**
     * Get the factory that will be used to create new processes.
     * @return The factory that will be used to create new processes.
     */
    public ProcessFactory getProcessFactory()
    {
        return this.processFactory;
    }

    /**
     * Get the CharacterWriteStream that verbose lines should be written to.
     * @return The CharacterWriteStream that verbose lines should be written to.
     */
    public VerboseCharacterWriteStream getVerbose()
    {
        return this.verbose;
    }

    /**
     * Set the pattern that will be used to determine whether or not a test should run.
     * @param pattern The pattern that will be used to determine whether or not a test should run.
     * @return This object for method chaining.
     */
    public QubTestParameters setPattern(String pattern)
    {
        this.pattern = pattern;
        return this;
    }

    /**
     * Get the pattern that will be used to determine whether or not a test should run.
     * @return The pattern that will be used to determine whether or not a test should run.
     */
    public String getPattern()
    {
        return this.pattern;
    }

    /**
     * Set which group of source files code coverage information should be collected for.
     * @param coverage Which group of source files code coverage information should be collected
     *                 for.
     * @return This object for method chaining.
     */
    public QubTestParameters setCoverage(Coverage coverage)
    {
        this.coverage = coverage;
        return this;
    }

    /**
     * Get which group of source files code coverage information should be collected for.
     * @return Which group of source files code coverage information should be collected for.
     */
    public Coverage getCoverage()
    {
        return this.coverage;
    }

    /**
     * Set whether or not a test.json file should be written after the tests are done.
     * @param testJson Whether or not a test.json file should be written after the tests are done.
     * @return This object for method chaining.
     */
    public QubTestParameters setTestJson(Boolean testJson)
    {
        PreCondition.assertNotNull(testJson, "testJson");

        return this.setTestJson(testJson.booleanValue());
    }

    /**
     * Set whether or not a test.json file should be written after the tests are done.
     * @param testJson Whether or not a test.json file should be written after the tests are done.
     * @return This object for method chaining.
     */
    public QubTestParameters setTestJson(boolean testJson)
    {
        this.testJson = testJson;
        return this;
    }

    /**
     * Get whether or not a test.json file should be written after the tests are done.
     * @return Whether or not a test.json file should be written after the tests are done.
     */
    public boolean getTestJson()
    {
        return this.testJson;
    }

    /**
     * Set the classpath that was passed to this application's JVM.
     * @param jvmClassPath The classpath that was passed to this application's JVM.
     * @return This object for method chaining.
     */
    public QubTestParameters setJvmClassPath(String jvmClassPath)
    {
        this.jvmClassPath = jvmClassPath;
        return this;
    }

    /**
     * Get the classpath that was passed to this application's JVM.
     * @return The classpath that was passed to this application's JVM.
     */
    public String getJvmClassPath()
    {
        return this.jvmClassPath;
    }

    /**
     * Set whether or not this application should be profiled.
     * @param profiler Whether or not this application should be profiled.
     * @return This object for method chaining.
     */
    public QubTestParameters setProfiler(Boolean profiler)
    {
        this.profiler = profiler;
        return this;
    }

    /**
     * Get whether or not this application should be profiled.
     * @return Whether or not this application should be profiled.
     */
    public boolean getProfiler()
    {
        return this.profiler;
    }
}
