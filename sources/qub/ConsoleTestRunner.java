package qub;

/**
 * A Console object that is used for running unit tests for other applications.
 */
public class ConsoleTestRunner implements TestRunner, Disposable
{
    private final BasicTestRunner testRunner;
    private final IndentedCharacterWriteStream writeStream;
    private final CharacterWriteStream consoleBackupWriteStream;
    private final Console console;
    private boolean isDisposed;
    private int unmodifiedPassedTests;
    private int unmodifiedSkippedTests;

    public ConsoleTestRunner(Console console, PathPattern pattern)
    {
        PreCondition.assertNotNull(console, "console");

        testRunner = new BasicTestRunner(console, pattern);

        this.console = console;
        consoleBackupWriteStream = console.getOutputCharacterWriteStream();
        writeStream = new IndentedCharacterWriteStream(consoleBackupWriteStream);
        console.setOutputCharacterWriteStream(writeStream);

        final List<TestParent> testParentsWrittenToConsole = new ArrayList<>();
        testRunner.afterTestClass((TestClass testClass) ->
        {
            if (testParentsWrittenToConsole.remove(testClass))
            {
                decreaseIndent();
            }
        });
        testRunner.afterTestGroup((TestGroup testGroup) ->
        {
            if (testParentsWrittenToConsole.remove(testGroup))
            {
                decreaseIndent();
            }
        });
        testRunner.beforeTest((Test test) ->
        {
            final Stack<TestParent> testParentsToWrite = Stack.create();
            TestParent currentTestParent = test.getParent();
            while (currentTestParent != null && !testParentsWrittenToConsole.contains(currentTestParent))
            {
                testParentsToWrite.push(currentTestParent);
                currentTestParent = currentTestParent.getParent();
            }

            while (testParentsToWrite.any())
            {
                final TestParent testParentToWrite = testParentsToWrite.pop().await();

                final String skipMessage = testParentToWrite.getSkipMessage();
                final String testGroupMessage = testParentToWrite.getName() + (!testParentToWrite.shouldSkip() ? "" : " - Skipped" + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage));
                writeStream.writeLine(testGroupMessage).await();
                testParentsWrittenToConsole.add(testParentToWrite);
                increaseIndent();
            }

            writeStream.write(test.getName()).await();
            increaseIndent();
        });
        testRunner.afterTestSuccess((Test test) ->
        {
            writeStream.writeLine(" - Passed");
        });
        testRunner.afterTestFailure((Test test, TestError failure) ->
        {
            writeStream.writeLine(" - Failed");
            writeFailure(failure);
        });
        testRunner.afterTestSkipped((Test test) ->
        {
            final String skipMessage = test.getSkipMessage();
            writeStream.writeLine(" - Skipped" + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage));
        });
        testRunner.afterTest((Test test) ->
        {
            decreaseIndent();
        });
    }

    @Override
    public Result<Boolean> dispose()
    {
        Result<Boolean> result;
        if (isDisposed())
        {
            result = Result.successFalse();
        }
        else
        {
            console.setOutputCharacterWriteStream(consoleBackupWriteStream);
            isDisposed = true;
            result = Result.successTrue();
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    @Override
    public boolean isDisposed()
    {
        return isDisposed;
    }

    private void addUnmodifiedPassedTests(int unmodifiedPassedTests)
    {
        PreCondition.assertGreaterThanOrEqualTo(unmodifiedPassedTests, 0, "unmodifiedPassedTests");

        this.unmodifiedPassedTests += unmodifiedPassedTests;
    }

    private void addUnmodifiedSkippedTests(int unmodifiedSkippedTests)
    {
        PreCondition.assertGreaterThanOrEqualTo(unmodifiedSkippedTests, 0, "unmodifiedSkippedTests");

        this.unmodifiedSkippedTests += unmodifiedSkippedTests;
    }

    /**
     * Increase the indent of the Console output.
     */
    private void increaseIndent()
    {
        writeStream.increaseIndent();
    }

    /**
     * Decrease the indent of the Console output.
     */
    private void decreaseIndent()
    {
        writeStream.decreaseIndent();
    }

    public int getFailedTestCount()
    {
        return testRunner.getFailedTestCount();
    }

    public void writeFailure(TestError failure)
    {
        PreCondition.assertNotNull(failure, "failure");

        increaseIndent();
        writeMessageLines(failure);
        writeStackTrace(failure);
        decreaseIndent();

        Throwable cause = failure.getCause();
        if (cause != null)
        {
            writeFailureCause(cause);
        }
    }

    public void writeMessageLines(TestError failure)
    {
        PreCondition.assertNotNull(failure, "failure");

        for (final String messageLine : failure.getMessageLines())
        {
            if (messageLine != null)
            {
                writeStream.writeLine(messageLine);
            }
        }
    }

    private void writeMessage(Throwable throwable)
    {
        if (throwable instanceof TestError)
        {
            writeMessageLines((TestError)throwable);
        }
        else if (!Strings.isNullOrEmpty(throwable.getMessage()))
        {
            writeStream.writeLine("Message: " + throwable.getMessage());
        }
    }

    private void writeFailureCause(Throwable cause)
    {
        if (cause instanceof ErrorIterable)
        {
            final ErrorIterable errors = (ErrorIterable)cause;

            writeStream.writeLine("Caused by:");
            int causeNumber = 0;
            for (final Throwable innerCause : errors)
            {
                ++causeNumber;
                writeStream.write(causeNumber + ") " + innerCause.getClass().getName());

                increaseIndent();
                writeMessage(innerCause);
                writeStackTrace(innerCause);
                decreaseIndent();

                final Throwable nextCause = innerCause.getCause();
                if (nextCause != null && nextCause != innerCause)
                {
                    increaseIndent();
                    writeFailureCause(nextCause);
                    decreaseIndent();
                }
            }
        }
        else
        {
            writeStream.writeLine("Caused by: " + cause.getClass().getName());

            increaseIndent();
            writeMessage(cause);
            writeStackTrace(cause);
            decreaseIndent();

            final Throwable nextCause = cause.getCause();
            if (nextCause != null && nextCause != cause)
            {
                increaseIndent();
                writeFailureCause(nextCause);
                decreaseIndent();
            }
        }
    }

    @Override
    public Skip skip()
    {
        return testRunner.skip();
    }

    @Override
    public Skip skip(boolean toSkip)
    {
        return testRunner.skip(toSkip);
    }

    @Override
    public Skip skip(boolean toSkip, String message)
    {
        return testRunner.skip(toSkip, message);
    }

    @Override
    public Skip skip(String message)
    {
        return testRunner.skip(message);
    }

    @Override
    public Result<Void> testClass(String fullClassName)
    {
        return testRunner.testClass(fullClassName);
    }

    @Override
    public Result<Void> testClass(Class<?> testClass)
    {
        return testRunner.testClass(testClass);
    }

    @Override
    public void testGroup(String testGroupName, Action0 testGroupAction)
    {
        testRunner.testGroup(testGroupName, testGroupAction);
    }

    @Override
    public void testGroup(Class<?> testClass, Action0 testGroupAction)
    {
        testRunner.testGroup(testClass, testGroupAction);
    }

    @Override
    public void testGroup(String testGroupName, Skip skip, Action0 testGroupAction)
    {
        testRunner.testGroup(testGroupName, skip, testGroupAction);
    }

    @Override
    public void testGroup(Class<?> testClass, Skip skip, Action0 testGroupAction)
    {
        testRunner.testGroup(testClass, skip, testGroupAction);
    }

    @Override
    public void test(String testName, Action1<Test> testAction)
    {
        testRunner.test(testName, testAction);
    }

    @Override
    public void test(String testName, Skip skip, Action1<Test> testAction)
    {
        testRunner.test(testName, skip, testAction);
    }

    @Override
    public void speedTest(String testName, Duration maximumDuration, Action1<Test> testAction)
    {
        testRunner.speedTest(testName, maximumDuration, testAction);
    }

    @Override
    public void beforeTestClass(Action1<TestClass> beforeTestClassAction)
    {
        testRunner.beforeTestClass(beforeTestClassAction);
    }

    @Override
    public void afterTestClass(Action1<TestClass> afterTestClassAction)
    {
        testRunner.afterTestClass(afterTestClassAction);
    }

    @Override
    public void beforeTestGroup(Action1<TestGroup> beforeTestGroupAction)
    {
        testRunner.beforeTestGroup(beforeTestGroupAction);
    }

    @Override
    public void afterTestGroupFailure(Action2<TestGroup,TestError> afterTestGroupFailureAction)
    {
        testRunner.afterTestGroupFailure(afterTestGroupFailureAction);
    }

    @Override
    public void afterTestGroupSkipped(Action1<TestGroup> afterTestGroupSkipped)
    {
        testRunner.afterTestGroupSkipped(afterTestGroupSkipped);
    }

    @Override
    public void afterTestGroup(Action1<TestGroup> afterTestGroupAction)
    {
        testRunner.afterTestGroup(afterTestGroupAction);
    }

    @Override
    public void beforeTest(Action1<Test> beforeTestAction)
    {
        testRunner.beforeTest(beforeTestAction);
    }

    @Override
    public void afterTestFailure(Action2<Test,TestError> afterTestFailureAction)
    {
        testRunner.afterTestFailure(afterTestFailureAction);
    }

    @Override
    public void afterTestSuccess(Action1<Test> afterTestSuccessAction)
    {
        testRunner.afterTestSuccess(afterTestSuccessAction);
    }

    @Override
    public void afterTestSkipped(Action1<Test> afterTestSkippedAction)
    {
        testRunner.afterTestSkipped(afterTestSkippedAction);
    }

    @Override
    public void afterTest(Action1<Test> afterTestAction)
    {
        testRunner.afterTest(afterTestAction);
    }

    @Override
    public Result<Boolean> hasNetworkConnection()
    {
        return testRunner.hasNetworkConnection();
    }

    /**
     * Write the stack trace of the provided Throwable to the output stream.
     * @param t The Throwable to writeByte the stack trace of.
     */
    private void writeStackTrace(Throwable t)
    {
        final StackTraceElement[] stackTraceElements = t.getStackTrace();
        if (stackTraceElements != null && stackTraceElements.length > 0)
        {
            writeStream.writeLine("Stack Trace:");
            increaseIndent();
            for (StackTraceElement stackTraceElement : stackTraceElements)
            {
                writeStream.writeLine("at " + stackTraceElement.toString());
            }
            decreaseIndent();
        }
    }

    /**
     * Write the current statistics of this ConsoleTestRunner.
     */
    public void writeSummary()
    {
        final Iterable<Test> skippedTests = testRunner.getSkippedTests();
        if (skippedTests.any())
        {
            writeStream.writeLine("Skipped Tests:");
            increaseIndent();
            int testSkippedNumber = 1;
            for (final Test skippedTest : skippedTests)
            {
                final String skipMessage = skippedTest.getSkipMessage();
                writeStream.writeLine(testSkippedNumber + ") " + skippedTest.getFullName() + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage));
                ++testSkippedNumber;
            }
            decreaseIndent();

            writeStream.writeLine();
        }

        final Iterable<TestError> testFailures = testRunner.getTestFailures();
        if (testFailures.any())
        {
            writeStream.writeLine("Test failures:");
            increaseIndent();

            int testFailureNumber = 1;
            for (final TestError failure : testFailures)
            {
                writeStream.writeLine(testFailureNumber + ") " + failure.getTestScope());
                ++testFailureNumber;
                increaseIndent();
                writeFailure(failure);
                decreaseIndent();

                writeStream.writeLine();
            }

            decreaseIndent();
        }

        if (unmodifiedPassedTests > 0 || unmodifiedSkippedTests > 0)
        {
            writeStream.writeLine("Unmodified Tests:         " + (unmodifiedPassedTests + unmodifiedSkippedTests)).await();
            if (unmodifiedPassedTests > 0)
            {
                writeStream.writeLine("Unmodified Passed Tests:  " + unmodifiedPassedTests).await();
            }
            if (unmodifiedSkippedTests > 0)
            {
                writeStream.writeLine("Unmodified Skipped Tests: " + unmodifiedSkippedTests).await();
            }
        }

        if (testRunner.getFinishedTestCount() > 0)
        {
            writeStream.writeLine("Tests Run:                " + testRunner.getFinishedTestCount()).await();
            if (testRunner.getPassedTestCount() > 0)
            {
                writeStream.writeLine("Tests Passed:             " + testRunner.getPassedTestCount()).await();
            }
            if (testRunner.getFailedTestCount() > 0)
            {
                writeStream.writeLine("Tests Failed:             " + testRunner.getFailedTestCount()).await();
            }
            if (testRunner.getSkippedTestCount() > 0)
            {
                writeStream.writeLine("Tests Skipped:            " + testRunner.getSkippedTestCount()).await();
            }
        }
    }

    public static void run(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        final CommandLineParameters parameters = console.createCommandLineParameters();
        final CommandLineParameter<PathPattern> patternParameter = parameters.add("pattern", (String argumentValue) ->
        {
            return Result.success(Strings.isNullOrEmpty(argumentValue)
                ? null
                : PathPattern.parse(argumentValue));
        });
        final CommandLineParameter<String> outputFolderParameter = parameters.addString("output-folder");
        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(console, ConsoleTestRunner.class);
        final CommandLineParameterBoolean testJsonParameter = parameters.addBoolean("testjson", true);
        final CommandLineParameterList<String> testClassNamesParameter = parameters.addPositionStringList("test-class");

        final PathPattern pattern = patternParameter.getValue().await();

        String outputFolderPath = outputFolderParameter.getValue().await();
        if (Strings.isNullOrEmpty(outputFolderPath))
        {
            outputFolderPath = console.getCurrentFolderPath().concatenateSegment("outputs").toString();
        }
        final Folder outputFolder = console.getFileSystem().getFolder(outputFolderPath).await();

        profilerParameter.await();

        verbose.writeLine("TestPattern: " + Strings.escapeAndQuote(pattern)).await();

        final Stopwatch stopwatch = console.getStopwatch();
        stopwatch.start();

        final boolean useTestJson = testJsonParameter.getValue().await();

        final ConsoleTestRunner runner = new ConsoleTestRunner(console, pattern);

        final List<TestJSONClassFile> testJSONClassFiles = List.create();
        final Iterable<String> testClassNames = testClassNamesParameter.getValues().await();

        MutableMap<String,TestJSONClassFile> fullClassNameToTestJSONClassFileMap = Map.create();
        if (useTestJson)
        {
            final TestJSON testJson = TestJSON.parse(outputFolder.getFile("test.json").await())
                .catchError(FileNotFoundException.class)
                .await();
            if (testJson != null)
            {
                verbose.writeLine("Found and parsed test.json file.").await();
                for (final TestJSONClassFile testJSONClassFile : testJson.getClassFiles())
                {
                    fullClassNameToTestJSONClassFileMap.set(testJSONClassFile.getFullClassName(), testJSONClassFile);
                }
            }

            runner.afterTestClass((TestClass testClass) ->
            {
                verbose.writeLine("Updating test.json class file for " + testClass.getFullName() + "...").await();
                final File testClassFile = QubTest.getClassFile(outputFolder, testClass.getFullName());
                testJSONClassFiles.add(new TestJSONClassFile()
                    .setRelativePath(testClassFile.relativeTo(outputFolder))
                    .setLastModified(testClassFile.getLastModified().await())
                    .setPassedTestCount(testClass.getPassedTestCount())
                    .setSkippedTestCount(testClass.getSkippedTestCount())
                    .setFailedTestCount(testClass.getFailedTestCount()));
            });
        }

        for (final String testClassName : testClassNames)
        {
            boolean runTestClass;

            if (!useTestJson)
            {
                runTestClass = true;
            }
            else
            {
                final TestJSONClassFile testJSONClassFile = fullClassNameToTestJSONClassFileMap.get(testClassName)
                    .catchError(NotFoundException.class)
                    .await();
                if (testJSONClassFile == null)
                {
                    verbose.writeLine("Found class that didn't exist in previous test run: " + testClassName);
                    runTestClass = true;
                }
                else
                {
                    verbose.writeLine("Found class entry for " + testClassName + ". Checking timestamps...").await();
                    final File testClassFile = outputFolder.getFile(testJSONClassFile.getRelativePath()).await();
                    final DateTime testClassFileLastModified = testClassFile.getLastModified().await();
                    if (!testClassFileLastModified.equals(testJSONClassFile.getLastModified()))
                    {
                        verbose.writeLine("Timestamp of " + testClassName + " from the previous run (" + testJSONClassFile.getLastModified() + ") was not the same as the current class file timestamp (" + testClassFileLastModified + "). Running test class tests.").await();
                        runTestClass = true;
                    }
                    else if (testJSONClassFile.getFailedTestCount() > 0)
                    {
                        verbose.writeLine("Previous run of " + testClassName + " contained errors. Running test class tests...").await();
                        runTestClass = true;
                    }
                    else
                    {
                        verbose.writeLine("Previous run of " + testClassName + " didn't contain errors and the test class hasn't changed since then. Skipping test class tests.").await();
                        runner.addUnmodifiedPassedTests(testJSONClassFile.getPassedTestCount());
                        runner.addUnmodifiedSkippedTests(testJSONClassFile.getSkippedTestCount());
                        testJSONClassFiles.add(testJSONClassFile);
                        runTestClass = false;
                    }
                }
            }

            if (runTestClass)
            {
                runner.testClass(testClassName)
                    .catchError((Throwable error) -> verbose.writeLine(error.getMessage()).await())
                    .await();
            }
        }

        console.writeLine().await();
        runner.writeSummary();

        if (useTestJson)
        {
            final File testJsonFile = outputFolder.getFile("test.json").await();
            final TestJSON testJson = new TestJSON()
                .setClassFiles(testJSONClassFiles);
            testJson.write(testJsonFile).await();
        }

        final Duration totalTestsDuration = stopwatch.stop();
        console.writeLine("Tests Duration: " + totalTestsDuration.toSeconds().toString("0.0")).await();

        console.setExitCode(runner.getFailedTestCount());
    }

    public static void main(String[] args)
    {
        Console.run(args, ConsoleTestRunner::run);
    }
}
