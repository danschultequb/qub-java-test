package qub;

public abstract class JavaRunner
{
    private Iterable<String> classPaths;
    private String pattern;
    private Folder outputFolder;
    private Folder sourceFolder;
    private Folder jacocoFolder;

    public void setClassPaths(Iterable<String> classPaths)
    {
        PreCondition.assertNotNull(classPaths, "classPaths");

        this.classPaths = classPaths;
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

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public String getPattern()
    {
        return pattern;
    }

    public void setOutputFolder(Folder outputFolder)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");

        this.outputFolder = outputFolder;
    }

    public Folder getOutputFolder()
    {
        final Folder result = outputFolder;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public void setSourceFolder(Folder sourceFolder)
    {
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");

        this.sourceFolder = sourceFolder;
    }

    public Folder getSourceFolder()
    {
        final Folder result = sourceFolder;

        PostCondition.assertNotNull(result, "result");

        return result;
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

    public void setJacocoFolder(Folder jacocoFolder)
    {
        this.jacocoFolder = jacocoFolder;
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

    public abstract Result<Integer> run(Console console, boolean profile);
}
