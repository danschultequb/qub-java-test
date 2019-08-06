package qub;

public interface RealJavaRunnerTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(RealJavaRunner.class, () ->
        {
            JavaRunnerTests.test(runner, RealJavaRunner::new);

            runner.testGroup("getJavaProcessBuilder()", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    test.assertThrows(() -> RealJavaRunner.getJavaProcessBuilder(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null process", (Test test) ->
                {
                    final ProcessBuilder javaProcessBuilder = RealJavaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    test.assertNotNull(javaProcessBuilder);
                    test.assertEqual("java", javaProcessBuilder.getExecutableFile().getNameWithoutFileExtension());
                });
            });

            runner.testGroup("addTestArguments()", () ->
            {
                runner.test("with null ProcessBuilder", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    test.assertThrows(() -> javaRunner.addTestArguments(null),
                        new PreConditionFailure("processBuilder cannot be null."));
                });

                runner.test("with no class paths set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    test.assertThrows(() -> javaRunner.addTestArguments(javaProcessBuilder),
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("with no output folder set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    test.assertThrows(() -> javaRunner.addTestArguments(javaProcessBuilder),
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("with output folder that doesn't exist", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    test.assertThrows(() -> javaRunner.addTestArguments(javaProcessBuilder),
                        new FolderNotFoundException("/outputs"));
                });

                runner.test("with output folder that exists", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with jacoco folder that doesn't exist", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    javaRunner.setJacocoFolder(fileSystem.getFolder("/jacoco/").await());
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-javaagent:/jacoco/jacocoagent.jar=destfile=/outputs/coverage.exec",
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with --profiler", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    final Process process = test.getProcess();
                    final CommandLineParameters commandLineParameters = process.createCommandLineParameters();
                    commandLineParameters.setArguments(CommandLineArguments.create("--profiler"));
                    final CommandLineParameterProfiler profiler = commandLineParameters.addProfiler(process, RealJavaRunner.class);
                    javaRunner.setProfiler(profiler);
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner",
                            "--profiler=true"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with --profiler=false", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    final Process process = test.getProcess();
                    final CommandLineParameters commandLineParameters = process.createCommandLineParameters();
                    commandLineParameters.setArguments(CommandLineArguments.create("--profiler=false"));
                    final CommandLineParameterProfiler profiler = commandLineParameters.addProfiler(process, RealJavaRunner.class);
                    javaRunner.setProfiler(profiler);
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner",
                            "--profiler=false"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with --profiler=true", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    final Process process = test.getProcess();
                    final CommandLineParameters commandLineParameters = process.createCommandLineParameters();
                    commandLineParameters.setArguments(CommandLineArguments.create("--profiler=true"));
                    final CommandLineParameterProfiler profiler = commandLineParameters.addProfiler(process, RealJavaRunner.class);
                    javaRunner.setProfiler(profiler);
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner",
                            "--profiler=true"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with --testjson", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    final Process process = test.getProcess();
                    final CommandLineParameters commandLineParameters = process.createCommandLineParameters();
                    commandLineParameters.setArguments(CommandLineArguments.create("--testjson"));
                    final CommandLineParameterBoolean testjson = commandLineParameters.addBoolean("testjson");
                    javaRunner.setTestJson(testjson);
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner",
                            "--testjson=true"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with --testjson=false", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    final Process process = test.getProcess();
                    final CommandLineParameters commandLineParameters = process.createCommandLineParameters();
                    commandLineParameters.setArguments(CommandLineArguments.create("--testjson=false"));
                    final CommandLineParameterBoolean testjson = commandLineParameters.addBoolean("testjson");
                    javaRunner.setTestJson(testjson);
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner",
                            "--testjson=false"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with --testjson=true", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    final Process process = test.getProcess();
                    final CommandLineParameters commandLineParameters = process.createCommandLineParameters();
                    commandLineParameters.setArguments(CommandLineArguments.create("--testjson=true"));
                    final CommandLineParameterBoolean testjson = commandLineParameters.addBoolean("testjson");
                    javaRunner.setTestJson(testjson);
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner",
                            "--testjson=true"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with empty pattern", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    javaRunner.setPattern("");
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with non-empty pattern", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    javaRunner.setPattern("hello");
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner",
                            "--pattern=hello"),
                        javaProcessBuilder.getArguments());
                });

                runner.test("with non-empty pattern and class files", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    outputFolder.createFile("qub/ClassToTest.class").await();
                    outputFolder.createFile("other/folder/TestMe.class").await();
                    javaRunner.setClassPaths(Iterable.create("/outputs/"));
                    javaRunner.setOutputFolder(outputFolder);
                    javaRunner.setPattern("hello");
                    final ProcessBuilder javaProcessBuilder = javaRunner.getJavaProcessBuilder(test.getProcess()).await();
                    javaRunner.addTestArguments(javaProcessBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-classpath",
                            "/outputs/",
                            "qub.ConsoleTestRunner",
                            "--pattern=hello",
                            "qub.ClassToTest",
                            "other.folder.TestMe"),
                        javaProcessBuilder.getArguments());
                });
            });

            runner.testGroup("getJacocoCLIJarFile()", () ->
            {
                runner.test("with null jacocoFolder", (Test test) ->
                {
                    test.assertThrows(() -> RealJavaRunner.getJacocoCLIJarFile(null),
                        new PreConditionFailure("jacocoFolder cannot be null."));
                });

                runner.test("with non-existing jacocoFolder", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder jacocoFolder = fileSystem.getFolder("/apples").await();
                    final File jacocoCLIJarFile = RealJavaRunner.getJacocoCLIJarFile(jacocoFolder).await();
                    test.assertNotNull(jacocoCLIJarFile);
                    test.assertEqual("/apples/jacococli.jar", jacocoCLIJarFile.toString());
                });
            });

            runner.testGroup("getCoverageFolder()", () ->
            {
                runner.test("with no outputFolder set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    test.assertThrows(() -> javaRunner.getCoverageFolder(),
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("with non-existing outputFolder set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/outputs/").await();
                    javaRunner.setOutputFolder(outputFolder);
                    final Folder coverageFolder = javaRunner.getCoverageFolder().await();
                    test.assertNotNull(coverageFolder);
                    test.assertEqual("/outputs/coverage", coverageFolder.toString());
                });
            });

            runner.testGroup("addCoverageArguments()", () ->
            {
                runner.test("with null process", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    test.assertThrows(() -> javaRunner.addCoverageArguments(null, null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null processBuilder", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final Process process = test.getProcess();
                    test.assertThrows(() -> javaRunner.addCoverageArguments(process, null),
                        new PreConditionFailure("processBuilder cannot be null."));
                });

                runner.test("with no jacocoFolder set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final Process process = test.getProcess();
                    final ProcessBuilder processBuilder = RealJavaRunner.getJavaProcessBuilder(process).await();
                    test.assertThrows(() -> javaRunner.addCoverageArguments(process, processBuilder),
                        new PreConditionFailure("jacocoFolder cannot be null."));
                });

                runner.test("with no output folder set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder jacocoFolder = fileSystem.getFolder("/jacoco/").await();
                    javaRunner.setJacocoFolder(jacocoFolder);
                    final Process process = test.getProcess();
                    final ProcessBuilder processBuilder = RealJavaRunner.getJavaProcessBuilder(process).await();
                    test.assertThrows(() -> javaRunner.addCoverageArguments(process, processBuilder),
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("with non-existing output folder", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder jacocoFolder = fileSystem.getFolder("/jacoco/").await();
                    javaRunner.setJacocoFolder(jacocoFolder);
                    final Folder outputFolder = fileSystem.getFolder("/outputs/").await();
                    javaRunner.setOutputFolder(outputFolder);
                    final Process process = test.getProcess();
                    final ProcessBuilder processBuilder = RealJavaRunner.getJavaProcessBuilder(process).await();
                    test.assertThrows(() -> javaRunner.addCoverageArguments(process, processBuilder),
                        new FolderNotFoundException("/outputs/"));
                });

                runner.test("with no source folder set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder jacocoFolder = fileSystem.getFolder("/jacoco/").await();
                    javaRunner.setJacocoFolder(jacocoFolder);
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setOutputFolder(outputFolder);
                    final Process process = test.getProcess();
                    final ProcessBuilder processBuilder = RealJavaRunner.getJavaProcessBuilder(process).await();
                    test.assertThrows(() -> javaRunner.addCoverageArguments(process, processBuilder),
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("with no class files", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder jacocoFolder = fileSystem.getFolder("/jacoco/").await();
                    javaRunner.setJacocoFolder(jacocoFolder);
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setOutputFolder(outputFolder);
                    final Folder sourceFolder = fileSystem.getFolder("/sources/").await();
                    javaRunner.setSourceFolder(sourceFolder);
                    final Process process = test.getProcess();
                    final ProcessBuilder processBuilder = RealJavaRunner.getJavaProcessBuilder(process).await();
                    javaRunner.addCoverageArguments(process, processBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-jar",
                            "/jacoco/jacococli.jar",
                            "report",
                            "/outputs/coverage.exec",
                            "--sourcefiles",
                            "/sources",
                            "--html",
                            "/outputs/coverage"),
                        processBuilder.getArguments());
                });

                runner.test("with class files", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder jacocoFolder = fileSystem.getFolder("/jacoco/").await();
                    javaRunner.setJacocoFolder(jacocoFolder);
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setOutputFolder(outputFolder);
                    outputFolder.createFile("qub/Test.class").await();
                    outputFolder.createFile("other/folder/Test2.class").await();
                    final Folder sourceFolder = fileSystem.getFolder("/sources/").await();
                    javaRunner.setSourceFolder(sourceFolder);
                    final Process process = test.getProcess();
                    final ProcessBuilder processBuilder = RealJavaRunner.getJavaProcessBuilder(process).await();
                    javaRunner.addCoverageArguments(process, processBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-jar",
                            "/jacoco/jacococli.jar",
                            "report",
                            "/outputs/coverage.exec",
                            "--classfiles", "/outputs/qub/Test.class",
                            "--classfiles", "/outputs/other/folder/Test2.class",
                            "--sourcefiles", "/sources",
                            "--html",
                            "/outputs/coverage"),
                        processBuilder.getArguments());
                });

                runner.test("with non-existing test folder set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder jacocoFolder = fileSystem.getFolder("/jacoco/").await();
                    javaRunner.setJacocoFolder(jacocoFolder);
                    final Folder outputFolder = fileSystem.createFolder("/outputs/").await();
                    javaRunner.setOutputFolder(outputFolder);
                    outputFolder.createFile("qub/Test.class").await();
                    outputFolder.createFile("other/folder/Test2.class").await();
                    final Folder sourceFolder = fileSystem.getFolder("/sources/").await();
                    javaRunner.setSourceFolder(sourceFolder);
                    final Folder testFolder = fileSystem.getFolder("/tests/").await();
                    javaRunner.setTestFolder(testFolder);
                    final Process process = test.getProcess();
                    final ProcessBuilder processBuilder = RealJavaRunner.getJavaProcessBuilder(process).await();
                    javaRunner.addCoverageArguments(process, processBuilder);
                    test.assertEqual(
                        Iterable.create(
                            "-jar",
                            "/jacoco/jacococli.jar",
                            "report",
                            "/outputs/coverage.exec",
                            "--classfiles", "/outputs/qub/Test.class",
                            "--classfiles", "/outputs/other/folder/Test2.class",
                            "--sourcefiles", "/sources",
                            "--sourcefiles", "/tests",
                            "--html",
                            "/outputs/coverage"),
                        processBuilder.getArguments());
                });
            });

            runner.testGroup("getCoverageIndexHtmlFile()", () ->
            {
                runner.test("with no coverage folder set", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    test.assertThrows(() -> javaRunner.getCoverageIndexHtmlFile(),
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("with non-existing output folder", (Test test) ->
                {
                    final RealJavaRunner javaRunner = new RealJavaRunner();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/outputs/").await();
                    javaRunner.setOutputFolder(outputFolder);
                    final File coverageIndexHtmlFile = javaRunner.getCoverageIndexHtmlFile().await();
                    test.assertNotNull(coverageIndexHtmlFile);
                    test.assertEqual("/outputs/coverage/index.html", coverageIndexHtmlFile.toString());
                });
            });
        });
    }
}
