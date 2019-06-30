package qub;

public abstract class JavaRunner
{
    private Iterable<String> classPaths;
    private String pattern;
    private Folder outputFolder;
    private Folder sourceFolder;
    private Folder testFolder;
    private Folder jacocoFolder;
    private CommandLineParameterVerbose verbose;

    public JavaRunner setClassPaths(Iterable<String> classPaths)
    {
        PreCondition.assertNotNull(classPaths, "classPaths");

        this.classPaths = classPaths;

        return this;
    }

    public Iterable<String> getClassPaths()
    {
        final Iterable<String> result = classPaths;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public String getClassPath()
    {
        return Strings.join(';', getClassPaths());
    }

    public JavaRunner setPattern(String pattern)
    {
        this.pattern = pattern;

        return this;
    }

    public String getPattern()
    {
        return pattern;
    }

    public JavaRunner setOutputFolder(Folder outputFolder)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");

        this.outputFolder = outputFolder;

        return this;
    }

    public Folder getOutputFolder()
    {
        final Folder result = outputFolder;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public JavaRunner setSourceFolder(Folder sourceFolder)
    {
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");

        this.sourceFolder = sourceFolder;

        return this;
    }

    public Folder getSourceFolder()
    {
        final Folder result = sourceFolder;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public JavaRunner setTestFolder(Folder testFolder)
    {
        PreCondition.assertNotNull(testFolder, "testFolder");

        this.testFolder = testFolder;

        return this;
    }

    public Folder getTestFolder()
    {
        return testFolder;
    }

    public Iterable<File> getClassFiles()
    {
        return getOutputFolder().getFilesRecursively().await()
            .where((File file) -> Comparer.equal(file.getFileExtension(), ".class"));
    }

    public Iterable<String> getFullClassNames()
    {
        final Folder outputFolder = getOutputFolder();
        final Iterable<String> result = getClassFiles()
            .map((File classFile) -> classFile.relativeTo(outputFolder)
                .withoutFileExtension().toString()
                .replace('/', '.').replace('\\', '.'));

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public JavaRunner setJacocoFolder(Folder jacocoFolder)
    {
        this.jacocoFolder = jacocoFolder;

        return this;
    }

    public Folder getJacocoFolder()
    {
        return jacocoFolder;
    }

    public File getJacocoAgentJarFile()
    {
        PreCondition.assertNotNull(getJacocoFolder(), "getJacocoFolder()");

        return getJacocoFolder().getFile("jacocoagent.jar").await();
    }

    public File getCoverageExecFile()
    {
        return getOutputFolder().getFile("coverage.exec").await();
    }

    public JavaRunner setVerbose(CommandLineParameterVerbose verbose)
    {
        this.verbose = verbose;
        return this;
    }

    public boolean isVerbose()
    {
        return verbose != null && verbose.getValue().await();
    }

    public CommandLineParameterVerbose getVerbose()
    {
        return verbose;
    }

    public Result<Void> writeVerboseLine(String message)
    {
        return Result.create(() ->
        {
            if (verbose != null)
            {
                verbose.writeLine(message).await();
            }
        });
    }

    public abstract Result<Void> run(Console console, CommandLineParameterProfiler profile);
}
