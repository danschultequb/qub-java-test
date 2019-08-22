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

    public ConsoleTestRunner(Console console, PathPattern pattern)
    {
        PreCondition.assertNotNull(console, "console");

        testRunner = new BasicTestRunner(console, pattern);

        this.console = console;
        consoleBackupWriteStream = console.getOutputCharacterWriteStream();
        writeStream = new IndentedCharacterWriteStream(consoleBackupWriteStream);
        console.setOutputCharacterWriteStream(writeStream);

        final List<TestGroup> testGroupsWrittenToConsole = new ArrayList<>();
        testRunner.afterTestGroup((TestGroup testGroup) ->
        {
            if (testGroupsWrittenToConsole.remove(testGroup))
            {
                decreaseIndent();
            }
        });
        testRunner.beforeTest((Test test) ->
        {
            final Stack<TestGroup> testGroupsToWrite = Stack.create();
            TestGroup currentTestGroup = test.getParentTestGroup();
            while (currentTestGroup != null && !testGroupsWrittenToConsole.contains(currentTestGroup))
            {
                testGroupsToWrite.push(currentTestGroup);
                currentTestGroup = currentTestGroup.getParentTestGroup();
            }

            while (testGroupsToWrite.any())
            {
                final TestGroup testGroupToWrite = testGroupsToWrite.pop().await();

                final String skipMessage = testGroupToWrite.getSkipMessage();
                final String testGroupMessage = testGroupToWrite.getName() + (!testGroupToWrite.shouldSkip() ? "" : " - Skipped" + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage));
                writeStream.writeLine(testGroupMessage).await();
                testGroupsWrittenToConsole.add(testGroupToWrite);
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

        writeStream.writeLine("Tests Run:      " + testRunner.getFinishedTestCount());
        if (testRunner.getPassedTestCount() > 0)
        {
            writeStream.writeLine("Tests Passed:   " + testRunner.getPassedTestCount());
        }
        if (testRunner.getFailedTestCount() > 0)
        {
            writeStream.writeLine("Tests Failed:   " + testRunner.getFailedTestCount());
        }
        if (testRunner.getSkippedTestCount() > 0)
        {
            writeStream.writeLine("Tests Skipped:  " + testRunner.getSkippedTestCount());
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
        final CommandLineParameterVerbose verbose = parameters.addVerbose(console);
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(console, ConsoleTestRunner.class);
        final CommandLineParameterBoolean testJsonParameter = parameters.addBoolean("testjson");
        final CommandLineParameterList<String> testClassNamesParameter = parameters.addPositionStringList("test-class");

        final PathPattern pattern = patternParameter.getValue().await();

        profilerParameter.await();

        verbose.writeLine("TestPattern: " + Strings.escapeAndQuote(pattern)).await();

        final Stopwatch stopwatch = console.getStopwatch();
        stopwatch.start();

        final boolean useTestJson = testJsonParameter.getValue().await();

        final ConsoleTestRunner runner = new ConsoleTestRunner(console, pattern);

        final Iterable<String> testClassNames = testClassNamesParameter.getValues().await();
        final List<String> invokedTestClassNames = List.create();
        for (final String testClassName : testClassNames)
        {
            verbose.writeLine("Looking for class " + Strings.escapeAndQuote(testClassName) + "...").await();

            final Class<?> testClass = Types.getClass(testClassName)
                .onValue(() -> verbose.writeLine("  Found!").await())
                .catchError(NotFoundException.class, () -> verbose.writeLine("Couldn't find " + Strings.escapeAndQuote(testClassName) + ".").await())
                .await();
            if (testClass != null)
            {
                verbose.writeLine("Looking for static " + Types.getMethodSignature(testClass, "test", TestRunner.class, Void.class) + "...").await();
                final StaticMethod1<?,TestRunner,?> testMethod = Types.getStaticMethod1(testClass, "test", TestRunner.class)
                    .onValue(() -> verbose.writeLine("Found!").await())
                    .catchError(NotFoundException.class, () -> verbose.writeLine("Couldn't find.").await())
                    .await();
                if (testMethod != null)
                {
                    try
                    {
                        testMethod.run(runner);
                        invokedTestClassNames.add(testClassName);
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        console.writeLine().await();
        runner.writeSummary();

        if (useTestJson)
        {
            final Folder outputFolder = console.getCurrentFolder().await()
                .getFolder("outputs").await();
            final File testJsonFile = outputFolder.getFile("test.json").await();
            final TestJSON testJson = new TestJSON();
            testJson.setClassFiles(invokedTestClassNames.map((String testClassName) ->
            {
                final String testClassFileRelativePath = testClassName.replace('.', '/') + ".class";
                final File testClassFile = outputFolder.getFile(testClassFileRelativePath).await();
                return new TestJSONClassFile()
                    .setRelativePath(testClassFileRelativePath)
                    .setLastModified(testClassFile.getLastModified().await());
            }));
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
