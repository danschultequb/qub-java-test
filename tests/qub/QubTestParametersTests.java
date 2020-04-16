package qub;

public interface QubTestParametersTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubTestParameters.class, () ->
        {
            runner.testGroup("constructor()", () ->
            {
                runner.test("with null outputByteWriteStream", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = null;
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folderToTest = fileSystem.getFolder("/").await();
                    final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                    final FakeProcessFactory processFactory = new FakeProcessFactory(test.getParallelAsyncRunner(), test.getProcess().getCurrentFolderPath());
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                    final String jvmClassPath = "apples";
                    test.assertThrows(() -> new QubTestParameters(output, error, folderToTest, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath),
                        new PreConditionFailure("outputWriteStream cannot be null."));
                });

                runner.test("with valid arguments", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folderToTest = fileSystem.getFolder("/").await();
                    final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                    final FakeProcessFactory processFactory = new FakeProcessFactory(test.getParallelAsyncRunner(), test.getProcess().getCurrentFolderPath());
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                    final String jvmClassPath = "apples";
                    final QubTestParameters parameters = new QubTestParameters(output, error, folderToTest, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);
                    test.assertSame(output, parameters.getOutputWriteStream());
                    test.assertSame(error, parameters.getErrorWriteStream());
                    test.assertSame(folderToTest, parameters.getFolderToTest());
                    test.assertSame(folderToTest, parameters.getFolderToBuild());
                    test.assertSame(environmentVariables, parameters.getEnvironmentVariables());
                    test.assertSame(processFactory, parameters.getProcessFactory());
                    test.assertSame(defaultApplicationLauncher, parameters.getDefaultApplicationLauncher());
                    test.assertSame(jvmClassPath, parameters.getJvmClassPath());
                    test.assertEqual(Coverage.None, parameters.getCoverage());
                });
            });

            runner.test("getOutputCharacterWriteStream()", (Test test) ->
            {
                final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                final InMemoryCharacterToByteStream error = InMemoryCharacterToByteStream.create();
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                fileSystem.createRoot("/").await();
                final Folder folderToTest = fileSystem.getFolder("/").await();
                final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                final FakeProcessFactory processFactory = new FakeProcessFactory(test.getParallelAsyncRunner(), test.getProcess().getCurrentFolderPath());
                final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                final String jvmClassPath = "apples";
                final QubTestParameters parameters = new QubTestParameters(output, error, folderToTest, environmentVariables, processFactory, defaultApplicationLauncher, jvmClassPath);

                final CharacterWriteStream outputCharacterWriteStream = parameters.getOutputWriteStream();
                test.assertNotNull(outputCharacterWriteStream);
                outputCharacterWriteStream.write("hello").await();
                test.assertEqual(new byte[] { 104, 101, 108, 108, 111 }, output.getBytes());
            });
        });
    }
}
