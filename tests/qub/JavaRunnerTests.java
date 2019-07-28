package qub;

public interface JavaRunnerTests
{
    static void test(TestRunner runner, Function0<JavaRunner> creator)
    {
        runner.testGroup(JavaRunner.class, () ->
        {
            runner.testGroup("setClassPaths()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(() -> javaRunner.setClassPaths(null),
                        new PreConditionFailure("classPaths cannot be null."));
                });

                runner.test("with empty", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final Iterable<String> classPaths = Iterable.create();
                    test.assertSame(javaRunner, javaRunner.setClassPaths(classPaths));
                    test.assertSame(classPaths, javaRunner.getClassPaths());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final Iterable<String> classPaths = Iterable.create("a", "b");
                    test.assertSame(javaRunner, javaRunner.setClassPaths(classPaths));
                    test.assertSame(classPaths, javaRunner.getClassPaths());
                });
            });

            runner.testGroup("getClassPaths()", () ->
            {
                runner.test("when it hasn't been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(javaRunner::getClassPaths,
                        new PostConditionFailure("result cannot be null."));
                });
            });

            runner.testGroup("getClassPath()", () ->
            {
                runner.test("when no classPaths have been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(javaRunner::getClassPath,
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("when an empty classPaths have been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    javaRunner.setClassPaths(Iterable.create());
                    test.assertEqual("", javaRunner.getClassPath());
                });

                runner.test("when a single classPath has been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    javaRunner.setClassPaths(Iterable.create("a"));
                    test.assertEqual("a", javaRunner.getClassPath());
                });

                runner.test("when two classPaths have been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    javaRunner.setClassPaths(Iterable.create("a", "b"));
                    test.assertEqual("a;b", javaRunner.getClassPath());
                });
            });

            runner.testGroup("setPattern()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setPattern(null));
                    test.assertNull(javaRunner.getPattern());
                });

                runner.test("with empty", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setPattern(""));
                    test.assertEqual("", javaRunner.getPattern());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setPattern("hello"));
                    test.assertEqual("hello", javaRunner.getPattern());
                });
            });

            runner.testGroup("getPattern()", () ->
            {
                runner.test("when no pattern has been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertNull(javaRunner.getPattern());
                });
            });

            runner.testGroup("setOutputFolder()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(() -> javaRunner.setOutputFolder(null),
                        new PreConditionFailure("outputFolder cannot be null."));
                });

                runner.test("with folder that doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folder = fileSystem.getFolder("/i/dont/exist").await();
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setOutputFolder(folder));
                    test.assertSame(folder, javaRunner.getOutputFolder());
                });

                runner.test("with folder that exists", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folder = fileSystem.getFolder("/i/dont/exist").await();
                    folder.create().await();
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setOutputFolder(folder));
                    test.assertSame(folder, javaRunner.getOutputFolder());
                });
            });

            runner.testGroup("getOutputFolder()", () ->
            {
                runner.test("when no outputFolder has been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(javaRunner::getOutputFolder,
                        new PostConditionFailure("result cannot be null."));
                });
            });

            runner.testGroup("setSourceFolder()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(() -> javaRunner.setSourceFolder(null),
                        new PreConditionFailure("sourceFolder cannot be null."));
                });

                runner.test("with folder that doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folder = fileSystem.getFolder("/i/dont/exist").await();
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setSourceFolder(folder));
                    test.assertSame(folder, javaRunner.getSourceFolder());
                });

                runner.test("with folder that exists", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folder = fileSystem.getFolder("/i/dont/exist").await();
                    folder.create().await();
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setSourceFolder(folder));
                    test.assertSame(folder, javaRunner.getSourceFolder());
                });
            });

            runner.testGroup("getSourceFolder()", () ->
            {
                runner.test("when no sourceFolder has been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(javaRunner::getSourceFolder,
                        new PostConditionFailure("result cannot be null."));
                });
            });

            runner.testGroup("setTestFolder()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(() -> javaRunner.setTestFolder(null),
                        new PreConditionFailure("testFolder cannot be null."));
                });

                runner.test("with folder that doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folder = fileSystem.getFolder("/i/dont/exist").await();
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setTestFolder(folder));
                    test.assertSame(folder, javaRunner.getTestFolder());
                });

                runner.test("with folder that exists", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folder = fileSystem.getFolder("/i/dont/exist").await();
                    folder.create().await();
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setTestFolder(folder));
                    test.assertSame(folder, javaRunner.getTestFolder());
                });
            });

            runner.testGroup("getTestFolder()", () ->
            {
                runner.test("when no testFolder has been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertNull(javaRunner.getTestFolder());
                });
            });

            runner.testGroup("getClassFiles()", () ->
            {
                runner.test("when outputFolder hasn't been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(javaRunner::getClassFiles,
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("when outputFolder doesn't exist", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/i/dont/exist").await();
                    javaRunner.setOutputFolder(outputFolder);
                    test.assertThrows(javaRunner::getClassFiles,
                        new FolderNotFoundException("/i/dont/exist"));
                });

                runner.test("when outputFolder is empty", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/i/exist").await();
                    outputFolder.create().await();
                    javaRunner.setOutputFolder(outputFolder);
                    test.assertEqual(Iterable.create(), javaRunner.getClassFiles());
                });

                runner.test("when outputFolder doesn't contain any *.class files", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/i/exist").await();
                    outputFolder.create().await();
                    outputFolder.createFile("code.stuff").await();
                    outputFolder.createFile("hello/there.txt").await();
                    javaRunner.setOutputFolder(outputFolder);
                    test.assertEqual(Iterable.create(), javaRunner.getClassFiles());
                });

                runner.test("when outputFolder contains *.class files", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/i/exist").await();
                    outputFolder.create().await();
                    outputFolder.createFile("code.class").await();
                    outputFolder.createFile("hello/there.class").await();
                    javaRunner.setOutputFolder(outputFolder);
                    test.assertEqual(
                        Iterable.create(
                            "/i/exist/code.class",
                            "/i/exist/hello/there.class"),
                        javaRunner.getClassFiles().map(File::toString));
                });
            });

            runner.testGroup("getFullClassNames()", () ->
            {
                runner.test("when outputFolder hasn't been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(javaRunner::getFullClassNames,
                        new PostConditionFailure("result cannot be null."));
                });

                runner.test("when outputFolder doesn't exist", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/i/dont/exist").await();
                    javaRunner.setOutputFolder(outputFolder);
                    test.assertThrows(javaRunner::getFullClassNames,
                        new FolderNotFoundException("/i/dont/exist"));
                });

                runner.test("when outputFolder is empty", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/i/exist").await();
                    outputFolder.create().await();
                    javaRunner.setOutputFolder(outputFolder);
                    test.assertEqual(Iterable.create(), javaRunner.getFullClassNames());
                });

                runner.test("when outputFolder doesn't contain any *.class files", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/i/exist").await();
                    outputFolder.create().await();
                    outputFolder.createFile("code.stuff").await();
                    outputFolder.createFile("hello/there.txt").await();
                    javaRunner.setOutputFolder(outputFolder);
                    test.assertEqual(Iterable.create(), javaRunner.getFullClassNames());
                });

                runner.test("when outputFolder contains *.class files", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder outputFolder = fileSystem.getFolder("/i/exist").await();
                    outputFolder.create().await();
                    outputFolder.createFile("code.class").await();
                    outputFolder.createFile("hello/there.class").await();
                    javaRunner.setOutputFolder(outputFolder);
                    test.assertEqual(
                        Iterable.create(
                            "code",
                            "hello.there"),
                        javaRunner.getFullClassNames());
                });
            });

            runner.testGroup("setJacocoFolder()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setJacocoFolder(null));
                    test.assertNull(javaRunner.getJacocoFolder());
                });

                runner.test("with folder that doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folder = fileSystem.getFolder("/i/dont/exist").await();
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setJacocoFolder(folder));
                    test.assertSame(folder, javaRunner.getJacocoFolder());
                });

                runner.test("with folder that exists", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folder = fileSystem.getFolder("/i/exist").await();
                    folder.create().await();
                    final JavaRunner javaRunner = creator.run();
                    test.assertSame(javaRunner, javaRunner.setJacocoFolder(folder));
                    test.assertSame(folder, javaRunner.getJacocoFolder());
                });
            });

            runner.testGroup("getJacocoFolder()", () ->
            {
                runner.test("when no jacocoFolder has been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertNull(javaRunner.getJacocoFolder());
                });
            });

            runner.testGroup("getJacocoAgentJarFile()", () ->
            {
                runner.test("when no jacocoFolder has been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(javaRunner::getJacocoAgentJarFile,
                        new PreConditionFailure("getJacocoFolder() cannot be null."));
                });

                runner.test("when jacocoFolder doesn't exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    final Folder folder = fileSystem.getFolder("/i/dont/exist").await();
                    final JavaRunner javaRunner = creator.run();
                    javaRunner.setJacocoFolder(folder);
                    test.assertEqual(folder.getFile("jacocoagent.jar").await(), javaRunner.getJacocoAgentJarFile());
                });

                runner.test("when jacocoFolder does exist", (Test test) ->
                {
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folder = fileSystem.getFolder("/i/exist").await();
                    folder.create().await();
                    final JavaRunner javaRunner = creator.run();
                    javaRunner.setJacocoFolder(folder);
                    test.assertEqual(folder.getFile("jacocoagent.jar").await(), javaRunner.getJacocoAgentJarFile());
                });
            });

            runner.testGroup("getCoverageExecFile()", () ->
            {
                runner.test("when outputFolder hasn't been set", (Test test) ->
                {
                    final JavaRunner javaRunner = creator.run();
                    test.assertThrows(javaRunner::getCoverageExecFile,
                        new PostConditionFailure("result cannot be null."));
                });
            });
        });
    }
}
