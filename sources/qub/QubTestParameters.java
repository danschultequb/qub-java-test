package qub;

/**
 * Parameters that are passed to QubTest.run().
 */
public class QubTestParameters extends QubBuildParameters
{
    private final ByteWriteStream outputByteWriteStream;
    private final ByteWriteStream errorByteWriteStream;
    private final DefaultApplicationLauncher defaultApplicationLauncher;
    private String jvmClassPath;
    private String pattern;
    private Coverage coverage;
    private boolean testJson;
    private boolean profiler;

    /**
     * Create a new QubTestParameters object.
     * @param outputByteWriteStream The ByteWriteStream that output should be written to.
     * @param errorByteWriteStream The ByteWriteStream that errors should be written to.
     * @param folderToTest The folder that should have its tests run.
     * @param environmentVariables The environment variables of the running process.
     * @param processFactory The factory that will be used to create new processes.
     */
    public QubTestParameters(ByteWriteStream outputByteWriteStream, ByteWriteStream errorByteWriteStream, Folder folderToTest, EnvironmentVariables environmentVariables, ProcessFactory processFactory, DefaultApplicationLauncher defaultApplicationLauncher, String jvmClassPath)
    {
        super(outputByteWriteStream == null ? null : outputByteWriteStream.asCharacterWriteStream(), folderToTest, environmentVariables, processFactory);

        PreCondition.assertNotNull(outputByteWriteStream, "outputByteWriteStream");
        PreCondition.assertNotNull(errorByteWriteStream, "errorByteWriteStream");
        PreCondition.assertNotNull(folderToTest, "folderToTest");
        PreCondition.assertNotNull(environmentVariables, "environmentVariables");
        PreCondition.assertNotNull(processFactory, "processFactory");
        PreCondition.assertNotNull(defaultApplicationLauncher, "defaultApplicationLauncher");
        PreCondition.assertNotNullAndNotEmpty(jvmClassPath, "jvmClassPath");

        this.outputByteWriteStream = outputByteWriteStream;
        this.errorByteWriteStream = errorByteWriteStream;
        this.defaultApplicationLauncher = defaultApplicationLauncher;
        this.jvmClassPath = jvmClassPath;
        this.coverage = QubTestParameters.getCoverageDefault();
        this.testJson = QubTestParameters.getTestJsonDefault();
    }

    /**
     * Get the ByteWriteStream that output should be written to.
     * @return The ByteWriteStream that output should be written to.
     */
    public ByteWriteStream getOutputByteWriteStream()
    {
        return this.outputByteWriteStream;
    }

    /**
     * Get the ByteWriteStream that errors should be written to.
     * @return The ByteWriteStream that errors should be written to.
     */
    public ByteWriteStream getErrorByteWriteStream()
    {
        return this.errorByteWriteStream;
    }

    /**
     * Get the DefaultApplicationLauncher.
     * @return The DefaultApplicationLauncher.
     */
    public DefaultApplicationLauncher getDefaultApplicationLauncher()
    {
        return this.defaultApplicationLauncher;
    }

    /**
     * Get the folder that should have its tests run.
     * @return The folder that should have its tests run.
     */
    public Folder getFolderToTest()
    {
        return this.getFolderToBuild();
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
     * Get which group of source files code coverage information should be collected for.
     * @return Which group of source files code coverage information should be collected for.
     */
    public Coverage getCoverage()
    {
        return this.coverage;
    }

    /**
     * Set which group of source files code coverage information should be collected for.
     * @param coverage Which group of source files code coverage information should be collected
     *                 for.
     * @return This object for method chaining.
     */
    public QubTestParameters setCoverage(Coverage coverage)
    {
        PreCondition.assertNotNull(coverage, "coverage");

        this.coverage = coverage;
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
     * Get the classpath that was passed to this application's JVM.
     * @return The classpath that was passed to this application's JVM.
     */
    public String getJvmClassPath()
    {
        return this.jvmClassPath;
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

    public boolean getProfiler()
    {
        return this.profiler;
    }

    public QubTestParameters setProfiler(boolean profiler)
    {
        this.profiler = profiler;
        return this;
    }

    @Override
    public QubTestParameters setWarnings(Warnings warnings)
    {
        return (QubTestParameters)super.setWarnings(warnings);
    }

    @Override
    public QubTestParameters setBuildJson(boolean buildJson)
    {
        return (QubTestParameters)super.setBuildJson(buildJson);
    }

    @Override
    public QubTestParameters setVerbose(VerboseCharacterWriteStream verbose)
    {
        return (QubTestParameters)super.setVerbose(verbose);
    }

    /**
     * Get the default value for the --testjson parameter.
     * @return The default value for the --testjson parameter.
     */
    static boolean getTestJsonDefault()
    {
        return true;
    }

    /**
     * Get the default value for the --coverage parameter.
     * @return The default value for the --coverage parameter.
     */
    static Coverage getCoverageDefault()
    {
        return Coverage.None;
    }
}
