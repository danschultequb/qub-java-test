package qub;

/**
 * Parameters that are passed to QubTest.run().
 */
public class QubTestRunParameters extends QubBuildCompileParameters
{
    private final CharacterToByteWriteStream errorWriteStream;
    private final DefaultApplicationLauncher defaultApplicationLauncher;
    private final Folder qubTestDataFolder;
    private String jvmClassPath;
    private String pattern;
    private Coverage coverage;
    private boolean testJson;
    private boolean profiler;

    /**
     * Create a new QubTestParameters object.
     * @param outputByteWriteStream The ByteWriteStream that output should be written to.
     * @param errorWriteStream The ByteWriteStream that errors should be written to.
     * @param folderToTest The folder that should have its tests run.
     * @param environmentVariables The environment variables of the running process.
     * @param processFactory The factory that will be used to create new processes.
     * @param typeLoader The TypeLoader that will be used to locate where the qub-build data folder is.
     */
    public QubTestRunParameters(CharacterToByteWriteStream outputByteWriteStream, CharacterToByteWriteStream errorWriteStream,
                                Folder folderToTest, EnvironmentVariables environmentVariables, ProcessFactory processFactory,
                                DefaultApplicationLauncher defaultApplicationLauncher, String jvmClassPath,
                                QubFolder qubFolder, Folder qubTestDataFolder, TypeLoader typeLoader)
    {
        super(outputByteWriteStream, folderToTest, environmentVariables, processFactory, qubFolder, QubTestRunParameters.getQubBuildDataFolder(folderToTest, typeLoader));

        PreCondition.assertNotNull(outputByteWriteStream, "outputByteWriteStream");
        PreCondition.assertNotNull(errorWriteStream, "errorByteWriteStream");
        PreCondition.assertNotNull(folderToTest, "folderToTest");
        PreCondition.assertNotNull(environmentVariables, "environmentVariables");
        PreCondition.assertNotNull(processFactory, "processFactory");
        PreCondition.assertNotNull(defaultApplicationLauncher, "defaultApplicationLauncher");
        PreCondition.assertNotNullAndNotEmpty(jvmClassPath, "jvmClassPath");
        PreCondition.assertNotNull(qubTestDataFolder, "qubTestDataFolder");

        this.errorWriteStream = errorWriteStream;
        this.defaultApplicationLauncher = defaultApplicationLauncher;
        this.jvmClassPath = jvmClassPath;
        this.coverage = QubTestRunParameters.getCoverageDefault();
        this.testJson = QubTestRunParameters.getTestJsonDefault();
        this.qubTestDataFolder = qubTestDataFolder;
    }

    private static Folder getQubBuildDataFolder(Folder folderToTest, TypeLoader typeLoader)
    {
        PreCondition.assertNotNull(folderToTest, "folderToTest");
        PreCondition.assertNotNull(typeLoader, "typeLoader");

        final FileSystem fileSystem = folderToTest.getFileSystem();
        final Path path = typeLoader.getTypeContainerPath(QubBuild.class).await();
        Folder projectVersionFolder;
        if (fileSystem.fileExists(path).await())
        {
            projectVersionFolder = fileSystem.getFile(path).await().getParentFolder().await();
        }
        else
        {
            projectVersionFolder = fileSystem.getFolder(path).await();
        }
        return QubProjectVersionFolder.get(projectVersionFolder)
            .getProjectDataFolder().await();
    }

    /**
     * Get the ByteWriteStream that errors should be written to.
     * @return The ByteWriteStream that errors should be written to.
     */
    public CharacterToByteWriteStream getErrorWriteStream()
    {
        return this.errorWriteStream;
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

    public Folder getQubTestDataFolder()
    {
        return this.qubTestDataFolder;
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
    public QubTestRunParameters setPattern(String pattern)
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
    public QubTestRunParameters setCoverage(Coverage coverage)
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
    public QubTestRunParameters setTestJson(boolean testJson)
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
    public QubTestRunParameters setJvmClassPath(String jvmClassPath)
    {
        this.jvmClassPath = jvmClassPath;
        return this;
    }

    public boolean getProfiler()
    {
        return this.profiler;
    }

    public QubTestRunParameters setProfiler(boolean profiler)
    {
        this.profiler = profiler;
        return this;
    }

    @Override
    public QubTestRunParameters setWarnings(Warnings warnings)
    {
        return (QubTestRunParameters)super.setWarnings(warnings);
    }

    @Override
    public QubTestRunParameters setBuildJson(boolean buildJson)
    {
        return (QubTestRunParameters)super.setBuildJson(buildJson);
    }

    @Override
    public QubTestRunParameters setVerbose(VerboseCharacterToByteWriteStream verbose)
    {
        return (QubTestRunParameters)super.setVerbose(verbose);
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
