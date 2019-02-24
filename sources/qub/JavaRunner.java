package qub;

public abstract class JavaRunner
{
    private Iterable<String> classPaths;
    private String pattern;
    private Folder outputFolder;
    private Iterable<String> fullClassNames;
    private File jacocoAgentJarFile;

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

    public void setFullClassNames(Iterable<String> fullClassNames)
    {
        PreCondition.assertNotNull(fullClassNames, "fullClassNames");

        this.fullClassNames = fullClassNames;
    }

    public Iterable<String> getFullClassNames()
    {
        final Iterable<String> result = fullClassNames;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public void setJacocoAgentJarFile(File jacocoAgentJarFile)
    {
        this.jacocoAgentJarFile = jacocoAgentJarFile;
    }

    public File getJacocoAgentJarFile()
    {
        return jacocoAgentJarFile;
    }

    public File getCoverageExecFile()
    {
        return getOutputFolder().getFile("coverage.exec").await();
    }

    public abstract Result<Integer> run(Console console);
}
