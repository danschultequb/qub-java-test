package qub;

public abstract class JavaRunner
{
    private Iterable<String> classPaths;
    private String pattern;
    private Folder outputFolder;
    private Folder sourceFolder;
    private Folder testFolder;
    private Coverage coverage = Coverage.None;
    private Folder jacocoFolder;
    private CommandLineParameterVerbose verbose;
    private CommandLineParameterProfiler profiler;
    private CommandLineParameterBoolean testJson;

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

    public Result<Iterable<File>> getAllClassFiles()
    {
        return Result.create(() ->
        {
            final Folder outputFolder = getOutputFolder();
            final Iterable<File> allOutputFiles = outputFolder.getFilesRecursively()
                .catchError(FolderNotFoundException.class, () -> Iterable.create())
                .await();
            return allOutputFiles.where((File file) -> Comparer.equal(file.getFileExtension(), ".class"));
        });
    }

    public Result<Iterable<File>> getAllSourceClassFiles()
    {
        return getAllFolderClassFiles(getSourceFolder());
    }

    public Result<Iterable<File>> getAllTestClassFiles()
    {
        return getAllFolderClassFiles(getTestFolder());
    }

    private Result<Iterable<File>> getAllFolderClassFiles(Folder sourceFolder)
    {
        return Result.create(() ->
        {
            final List<File> allFolderClassFiles = List.create();
            if (sourceFolder != null)
            {
                final Iterable<File> javaFiles = sourceFolder.getFilesRecursively()
                        .catchError(FolderNotFoundException.class, () -> Iterable.create())
                        .await()
                        .where((File file) -> Comparer.equal(file.getFileExtension(), ".java"));
                if (javaFiles.any())
                {
                    final Iterable<File> allClassFiles = getAllClassFiles().await();
                    allFolderClassFiles.addAll(QubTest.getSourceClassFiles(getOutputFolder(), allClassFiles, sourceFolder, javaFiles));
                }
            }
            return allFolderClassFiles;
        });
    }

    public Iterable<File> getClassFilesForCoverage()
    {
        Iterable<File> result;

        final Coverage coverage = this.getCoverage();
        if (coverage == Coverage.All)
        {
            result = getAllClassFiles().await();
        }
        else if (coverage == Coverage.Sources)
        {
            result = getAllSourceClassFiles().await();
        }
        else if (coverage == Coverage.Tests)
        {
            result = getAllTestClassFiles().await();
        }
        else
        {
            result = Iterable.create();
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public Iterable<String> getFullClassNames()
    {
        final Folder outputFolder = getOutputFolder();
        final Iterable<String> result = getAllClassFiles().await()
            .map((File classFile) -> QubTest.getFullClassName(outputFolder, classFile));

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Set the code coverage strategy that this JavaRunner will use.
     * @param coverage The code coverage strategy that this JavaRunner will use.
     * @return This object for method chaining.
     */
    public JavaRunner setCoverage(Coverage coverage)
    {
        PreCondition.assertNotNull(coverage, "coverage");

        this.coverage = coverage;

        return this;
    }

    /**
     * Get the code coverage strategy that this JavaRunner will use.
     * @return The code coverage strategy that this JavaRunner will use.
     */
    public Coverage getCoverage()
    {
        final Coverage result = this.coverage;

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

    public JavaRunner setProfiler(CommandLineParameterProfiler profiler)
    {
        this.profiler = profiler;
        return this;
    }

    public CommandLineParameterProfiler getProfiler()
    {
        return profiler;
    }

    public JavaRunner setTestJson(CommandLineParameterBoolean testJson)
    {
        this.testJson = testJson;
        return this;
    }

    public CommandLineParameterBoolean getTestJson()
    {
        return testJson;
    }

    public abstract Result<Void> run(Console console);
}
