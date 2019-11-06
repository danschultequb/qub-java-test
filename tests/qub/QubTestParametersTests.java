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
                    final InMemoryByteStream output = null;
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folderToTest = fileSystem.getFolder("/").await();
                    final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                    final FakeProcessFactory processFactory = new FakeProcessFactory(test.getParallelAsyncRunner(), test.getProcess().getCurrentFolderPath());
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                    test.assertThrows(() -> new QubTestParameters(output, error, folderToTest, environmentVariables, processFactory, defaultApplicationLauncher),
                        new PreConditionFailure("outputCharacterWriteStream cannot be null."));
                });

                runner.test("with valid arguments", (Test test) ->
                {
                    final InMemoryByteStream output = new InMemoryByteStream();
                    final InMemoryByteStream error = new InMemoryByteStream();
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                    fileSystem.createRoot("/").await();
                    final Folder folderToTest = fileSystem.getFolder("/").await();
                    final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                    final FakeProcessFactory processFactory = new FakeProcessFactory(test.getParallelAsyncRunner(), test.getProcess().getCurrentFolderPath());
                    final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                    final QubTestParameters parameters = new QubTestParameters(output, error, folderToTest, environmentVariables, processFactory, defaultApplicationLauncher);
                    test.assertSame(output, parameters.getOutputByteWriteStream());
                    test.assertSame(error, parameters.getErrorByteWriteStream());
                    test.assertSame(folderToTest, parameters.getFolderToTest());
                    test.assertSame(folderToTest, parameters.getFolderToBuild());
                    test.assertSame(environmentVariables, parameters.getEnvironmentVariables());
                    test.assertSame(processFactory, parameters.getProcessFactory());
                    test.assertSame(defaultApplicationLauncher, parameters.getDefaultApplicationLauncher());
                    test.assertEqual(Coverage.None, parameters.getCoverage());
                });
            });

            runner.test("getOutputCharacterWriteStream()", (Test test) ->
            {
                final InMemoryByteStream output = new InMemoryByteStream();
                final InMemoryByteStream error = new InMemoryByteStream();
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getClock());
                fileSystem.createRoot("/").await();
                final Folder folderToTest = fileSystem.getFolder("/").await();
                final EnvironmentVariables environmentVariables = new EnvironmentVariables();
                final FakeProcessFactory processFactory = new FakeProcessFactory(test.getParallelAsyncRunner(), test.getProcess().getCurrentFolderPath());
                final FakeDefaultApplicationLauncher defaultApplicationLauncher = new FakeDefaultApplicationLauncher();
                final QubTestParameters parameters = new QubTestParameters(output, error, folderToTest, environmentVariables, processFactory, defaultApplicationLauncher);

                final CharacterWriteStream outputCharacterWriteStream = parameters.getOutputCharacterWriteStream();
                test.assertNotNull(outputCharacterWriteStream);
                outputCharacterWriteStream.write("hello").await();
                test.assertEqual(new byte[] { 104, 101, 108, 108, 111 }, output.getBytes());
            });
        });
    }
}
