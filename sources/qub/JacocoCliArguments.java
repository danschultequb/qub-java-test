package qub;

public interface JacocoCliArguments<T>
{
    /**
     * Add the provided arguments to the list of arguments that will be provided to the executable
     * when this ProcessBuilder is run.
     * @param arguments The arguments to add.
     * @return This object for method chaining.
     */
    T addArguments(String... arguments);

    /**
     * Get the path to the folder that this ProcessBuilder will run the executable in.
     * @return The path to the folder that this ProcessBuilder will run the executable in.
     */
    Path getWorkingFolderPath();

    /**
     * Add the Jacoco CLI jar file to the arguments.
     * @param jacococliJarFilePath The Jacoco CLI Jar file path.
     * @return This object for method chaining.
     */
    default T addJacocoCliJar(String jacococliJarFilePath)
    {
        PreCondition.assertNotNullAndNotEmpty(jacococliJarFilePath, "jacococliJarFilePath");

        return this.addJacocoCliJar(Path.parse(jacococliJarFilePath));
    }

    /**
     * Add the Jacoco CLI jar file to the arguments.
     * @param jacococliJarFilePath The Jacoco CLI Jar file path.
     * @return This object for method chaining.
     */
    default T addJacocoCliJar(Path jacococliJarFilePath)
    {
        PreCondition.assertNotNull(jacococliJarFilePath, "jacococliJarFilePath");

        return this.addArguments("-jar", jacococliJarFilePath.toString());
    }

    /**
     * Add the Jacoco CLI jar file to the arguments.
     * @param jacococliJarFile The Jacoco CLI Jar file.
     * @return This object for method chaining.
     */
    default T addJacocoCliJar(File jacococliJarFile)
    {
        PreCondition.assertNotNull(jacococliJarFile, "jacococliJarFile");

        return this.addJacocoCliJar(jacococliJarFile.getPath());
    }



    /**
     * Add the report argument to the arguments.
     * @return This object for method chaining.
     */
    default T addReport()
    {
        return this.addArguments("report");
    }

    /**
     * Add the coverage.exec file path argument to the arguments.
     * @param coverageExecFilePath The coverage.exec file path.
     * @return This object for method chaining.
     */
    default T addCoverageExec(String coverageExecFilePath)
    {
        PreCondition.assertNotNullAndNotEmpty(coverageExecFilePath, "coverageExecFilePath");

        return this.addCoverageExec(Path.parse(coverageExecFilePath));
    }

    /**
     * Add the coverage.exec file path argument to the arguments.
     * @param coverageExecFilePath The coverage.exec file path.
     * @return This object for method chaining.
     */
    default T addCoverageExec(Path coverageExecFilePath)
    {
        PreCondition.assertNotNull(coverageExecFilePath, "coverageExecFilePath");

        return this.addArguments(coverageExecFilePath.toString());
    }

    /**
     * Add the coverage.exec file path argument to the arguments.
     * @param coverageExecFile The coverage.exec file.
     * @return This object for method chaining.
     */
    default T addCoverageExec(File coverageExecFile)
    {
        PreCondition.assertNotNull(coverageExecFile, "coverageExecFile");

        return this.addCoverageExec(coverageExecFile.getPath());
    }

    /**
     * Add the provided class file to the arguments.
     * @param classFile The class file to add to the arguments.
     * @return This object for method chaining.
     */
    default T addClassFile(File classFile)
    {
        PreCondition.assertNotNull(classFile, "classFile");

        Path classFilePath = classFile.getPath();
        final Path workingFolderPath = this.getWorkingFolderPath();
        if (workingFolderPath != null)
        {
            classFilePath = classFilePath.relativeTo(workingFolderPath);
        }
        return this.addArguments("--classfiles", classFilePath.toString());
    }

    /**
     * Add the provided class files to the arguments.
     * @param classFiles The class files to add to the arguments.
     * @return This object for method chaining.
     */
    default T addClassFiles(Iterable<File> classFiles)
    {
        PreCondition.assertNotNullAndNotEmpty(classFiles, "classFiles");

        T result = null;
        for (final File classFile : classFiles)
        {
            result = this.addClassFile(classFile);
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Add the provided source folder to the arguments as a sourcefiles argument.
     * @param sourceFolder The folder that sources should be read from.
     * @return This object for method chaining.
     */
    default T addSourceFiles(Folder sourceFolder)
    {
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");

        return this.addArguments("--sourcefiles", sourceFolder.toString());
    }

    /**
     * Add the source code folder to the arguments based on which coverage option is provided.
     * @param coverage The coverage option for the tests.
     * @param sourceFolder The folder that contains the source code files.
     * @param testFolder The folder that contains the test source code files.
     * @return This object for method chaining.
     */
    @SuppressWarnings("unchecked")
    default T addSourceFiles(Coverage coverage, Folder sourceFolder, Folder testFolder)
    {
        PreCondition.assertNotNull(coverage, "coverage");
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");

        T result = (T)this;
        if (coverage == Coverage.Sources || coverage == Coverage.All)
        {
            result = this.addSourceFiles(sourceFolder);
        }
        if ((coverage == Coverage.Tests || coverage == Coverage.All) && testFolder != null)
        {
            result = this.addSourceFiles(testFolder);
        }

        return result;
    }

    /**
     * Add the folder where the HTML coverage output should be written to.
     * @param htmlFolder The folder where the HTML coverage output should be written to.
     * @return This object for method chaining.
     */
    default T addHtml(Folder htmlFolder)
    {
        PreCondition.assertNotNull(htmlFolder, "htmlFolder");

        return this.addArguments("--html", htmlFolder.toString());
    }
}
