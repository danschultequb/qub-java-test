package qub;

public class QubTest
{
    private QubBuild qubBuild;
    private JavaRunner javaRunner;
    private Boolean showTotalDuration;

    /**
     * Set the QubBuild object that will be used to build the source code.
     * @param qubBuild The QubBuild object that will be used to build the source code.
     * @return This object for method chaining.
     */
    public QubTest setQubBuild(QubBuild qubBuild)
    {
        this.qubBuild = qubBuild;
        return this;
    }

    /**
     * Get the Build object that will be used to qubBuild the source code. If no Build object has been
     * set, a default one will be created and returned.
     * @return The Build object that will be used to qubBuild the source code.
     */
    public QubBuild getQubBuild()
    {
        if (qubBuild == null)
        {
            qubBuild = new QubBuild();
        }
        final QubBuild result = qubBuild;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Set the JavaRunner that will be used to run tests.
     * @param javaRunner The JavaRunner that will be used to run tests.
     * @return This object for method chaining.
     */
    public QubTest setJavaRunner(JavaRunner javaRunner)
    {
        this.javaRunner = javaRunner;

        return this;
    }

    public JavaRunner getJavaRunner()
    {
        if (javaRunner == null)
        {
            javaRunner = new RealJavaRunner();
        }
        final JavaRunner result = javaRunner;

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public QubTest setShowTotalDuration(boolean showTotalDuration)
    {
        this.showTotalDuration = showTotalDuration;
        return this;
    }

    public boolean getShowTotalDuration()
    {
        if (showTotalDuration == null)
        {
            showTotalDuration = true;
        }
        return showTotalDuration;
    }

    public void main(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final CommandLineParameters parameters = console.createCommandLineParameters();
        final CommandLineParameter<Folder> folderToTestParameter = parameters.addFolder("folder", console)
            .setValueName("<folder-to-test>")
            .setDescription("The folder to run tests in. Defaults to the current folder.");
        final CommandLineParameter<String> patternParameter = parameters.addString("pattern")
            .setValueName("<test-name-pattern>")
            .setDescription("The pattern to match against tests to determine if they will be run or not.");
        final CommandLineParameter<Coverage> coverageParameter = parameters.addEnum("coverage", Coverage.None, Coverage.Sources)
            .setValueRequired(false)
            .setValueName("<None|Sources|Tests|All>")
            .addAlias("c")
            .setDescription("Whether or not to collect code coverage information while running tests.");
        final CommandLineParameterBoolean testJsonParameter = parameters.addBoolean("testjson", true)
            .setDescription("Whether or not to write the test results to a test.json file.");
        final CommandLineParameter<String> jvmClassPathParameter = parameters.addString("jvm.classpath")
            .setDescription("The classpath that was passed to the JVM when this application was started.");
        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(console, QubTest.class);
        final CommandLineParameterBoolean help = parameters.addHelp();

        if (help.getValue().await())
        {
            parameters.writeHelpLines(console, "qub-test", "Used to run tests in source code projects.").await();
            console.setExitCode(-1);
        }
        else
        {
            profilerParameter.await();
            profilerParameter.removeValue().await();

            final boolean showTotalDuration = getShowTotalDuration();
            final Stopwatch stopwatch = console.getStopwatch();
            if (showTotalDuration)
            {
                stopwatch.start();
            }
            try
            {
                final QubBuild qubBuild = getQubBuild();
                qubBuild.setShowTotalDuration(false);
                qubBuild.main(console);

                if (console.getExitCode() == 0)
                {
                    console.writeLine("Running tests...").await();

                    final Folder folderToTest = folderToTestParameter.getValue().await();
                    final String pattern = patternParameter.getValue().await();
                    final Coverage coverage = coverageParameter.getValue().await();

                    final Folder outputFolder = folderToTest.getFolder("outputs").await();
                    final Folder sourceFolder = folderToTest.getFolder("sources").await();
                    final Folder testFolder = folderToTest.getFolder("tests").await();

                    final List<String> classPaths = List.create(outputFolder.toString());

                    final File projectJsonFile = folderToTest.getFile("project.json").await();
                    final ProjectJSON projectJson = ProjectJSON.parse(projectJsonFile).await();

                    final Folder qubFolder = getQubHomeFolder(console);
                    Iterable<Dependency> dependencies = projectJson.getJava().getDependencies();
                    if (!Iterable.isNullOrEmpty(dependencies))
                    {
                        dependencies = QubBuild.getAllDependencies(qubFolder, dependencies).getKeys();
                        classPaths.addAll(dependencies.map((Dependency dependency) ->
                        {
                            return QubBuild.resolveDependencyReference(qubFolder, dependency).toString();
                        }));
                    }

                    final String jvmClassPath = jvmClassPathParameter.getValue().await();
                    if (!Strings.isNullOrEmpty(jvmClassPath))
                    {
                        final String[] jvmClassPaths = jvmClassPath.split(";");
                        for (final String jvmClassPathString : jvmClassPaths)
                        {
                            final Path relativeJvmClassPath = Path.parse(jvmClassPathString).relativeTo(qubFolder);
                            final Indexable<String> segments = relativeJvmClassPath.getSegments();
                            final Dependency jvmDependency = new Dependency()
                                .setPublisher(segments.get(0))
                                .setProject(segments.get(1))
                                .setVersion(segments.get(2));
                            if (!QubTest.equal(jvmDependency, projectJson.getPublisher(), projectJson.getProject()) &&
                                (Iterable.isNullOrEmpty(dependencies) || !dependencies.contains(dep -> QubTest.equalIgnoreVersion(dep, jvmDependency))))
                            {
                                classPaths.add(jvmClassPathString);
                            }
                        }
                    }

                    Folder jacocoFolder = null;
                    if (coverage != Coverage.None)
                    {
                        jacocoFolder = qubFolder.getFolder("jacoco/jacococli").await()
                            .getFolders().await()
                            .maximum((Folder lhs, Folder rhs) -> VersionNumber.parse(lhs.getName()).compareTo(VersionNumber.parse(rhs.getName())));
                    }

                    final JavaRunner javaTestRunner = getJavaRunner();
                    javaTestRunner.setClassPaths(classPaths);
                    javaTestRunner.setPattern(pattern);
                    javaTestRunner.setOutputFolder(outputFolder);
                    javaTestRunner.setJacocoFolder(jacocoFolder);
                    javaTestRunner.setCoverage(coverage);
                    javaTestRunner.setSourceFolder(sourceFolder);
                    javaTestRunner.setTestFolder(testFolder);
                    javaTestRunner.setVerbose(verbose);
                    javaTestRunner.setProfiler(profilerParameter);
                    javaTestRunner.setTestJson(testJsonParameter);
                    javaTestRunner.run(console).await();
                }
            }
            finally
            {
                if (showTotalDuration)
                {
                    final Duration compilationDuration = stopwatch.stop().toSeconds();
                    console.writeLine("Done (" + compilationDuration.toString("0.0") + ")").await();
                }
            }
        }
    }

    public static boolean equalIgnoreVersion(Dependency lhs, Dependency rhs)
    {
        PreCondition.assertNotNull(lhs, "lhs");
        PreCondition.assertNotNull(rhs, "rhs");

        return equal(lhs, rhs.getPublisher(), rhs.getProject());
    }

    public static boolean equal(Dependency dependency, String publisher, String project)
    {
        PreCondition.assertNotNull(dependency, "dependency");

        return Comparer.equal(dependency.getPublisher(), publisher) &&
            Comparer.equal(dependency.getProject(), project);
    }

    private static Folder getQubHomeFolder(Console console)
    {
        PreCondition.assertNotNullAndNotEmpty(console.getEnvironmentVariable("QUB_HOME"), "console.getEnvironmentVariable(\"QUB_HOME\")");

        final String qubHome = console.getEnvironmentVariable("QUB_HOME");
        final Folder result = console.getFileSystem().getFolder(qubHome).await();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    /**
     * Get whether or not the provided outputClassFile was created from one of the provided java
     * files.
     * @param outputFolder The output folder that the outputClassFiles are entries of.
     * @param outputClassFile The outputClassFile to check.
     * @param sourceFolder The source folder that the sourceJavaFiles are entries of.
     * @param sourceJavaFiles The possible source files that the class file may have come from.
     * @return Whether or not the provided outputClassFile was created from one of the provided java
     * files.
     */
    public static boolean isSourceClassFile(Folder outputFolder, File outputClassFile, Folder sourceFolder, Iterable<File> sourceJavaFiles)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(outputClassFile, "outputClassFile");
        PreCondition.assertNotNull(sourceFolder, "sourceFolder");
        PreCondition.assertNotNull(sourceJavaFiles, "sourceJavaFiles");

        Path outputClassFilePath = outputClassFile.relativeTo(outputFolder).withoutFileExtension();
        if (outputClassFilePath.getSegments().last().contains("$"))
        {
            final String outputClassFileRelativePathString = outputClassFilePath.toString();
            final int dollarSignIndex = outputClassFileRelativePathString.lastIndexOf('$');
            final String outputClassFileRelativePathStringWithoutDollarSign = outputClassFileRelativePathString.substring(0, dollarSignIndex);
            outputClassFilePath = Path.parse(outputClassFileRelativePathStringWithoutDollarSign);
        }
        final Path outputClassFileRelativePath = outputClassFilePath;
        return sourceJavaFiles.contains((File sourceJavaFile) ->
        {
            final Path sourceJavaFileRelativePath = sourceJavaFile.relativeTo(sourceFolder).withoutFileExtension();
            return outputClassFileRelativePath.equals(sourceJavaFileRelativePath);
        });
    }

    public static Iterable<File> getSourceClassFiles(Folder outputFolder, Iterable<File> outputClassFiles, Folder sourceFolder, Iterable<File> sourceJavaFiles)
    {
        return outputClassFiles
            .where((File outputClassFile) ->
            {
                return QubTest.isSourceClassFile(outputFolder, outputClassFile, sourceFolder, sourceJavaFiles);
            });
    }

    /**
     * Get the full class name of the provided class file.
     * @param outputFolder The output folder that contains the class file.
     * @param classFile The class file to get the full class name of.
     * @return The full name of the class file.
     */
    public static String getFullClassName(Folder outputFolder, File classFile)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNull(classFile, "classFile");

        final Path classFileRelativePath = classFile.relativeTo(outputFolder);
        final String result = QubTest.getFullClassName(classFileRelativePath);

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    /**
     * Get the full class name of the class file at the provided relative path.
     * @param classFileRelativePath The path to the class file relative to the output folder.
     * @return The full name of the class file.
     */
    public static String getFullClassName(Path classFileRelativePath)
    {
        PreCondition.assertNotNull(classFileRelativePath, "classFileRelativePath");
        PreCondition.assertFalse(classFileRelativePath.isRooted(), "classFileRelativePath.isRooted()");
        PreCondition.assertEqual(".class", classFileRelativePath.getFileExtension(), "classFileRelativePath.getFileExtension()");

        final String result = classFileRelativePath
            .withoutFileExtension()
            .toString()
            .replace('/', '.')
            .replace('\\', '.');

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    public static File getClassFile(Folder outputFolder, String fullClassName)
    {
        PreCondition.assertNotNull(outputFolder, "outputFolder");
        PreCondition.assertNotNullAndNotEmpty(fullClassName, "fullClassName");

        final String testClassFileRelativePath = fullClassName.replace('.', '/') + ".class";
        final File result = outputFolder.getFile(testClassFileRelativePath).await();

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public static void main(String[] args)
    {
        final QubTest qubTest = new QubTest();
        Console.run(args, qubTest::main);
    }
}