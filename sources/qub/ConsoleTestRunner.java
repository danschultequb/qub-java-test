package qub;

/**
 * A Console object that is used for running unit tests for other applications.
 */
public class ConsoleTestRunner implements TestRunner
{
    private final Console console;
    private final BasicTestRunner testRunner;
    private final IndentedCharacterWriteStream indentedCharacterWriteStream;

    public ConsoleTestRunner(Console console, PathPattern pattern)
    {
        PreCondition.assertNotNull(console, "console");

        this.console = console;
        testRunner = new BasicTestRunner(console, pattern);
        indentedCharacterWriteStream = new IndentedCharacterWriteStream(console.getOutputCharacterWriteStream());
        console.setOutputCharacterWriteStream(indentedCharacterWriteStream);

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
                console.writeLine(testGroupMessage).await();
                testGroupsWrittenToConsole.add(testGroupToWrite);
                increaseIndent();
            }

            console.write(test.getName()).await();
            increaseIndent();
        });
        testRunner.afterTestSuccess((Test test) ->
        {
            console.writeLine(" - Passed");
        });
        testRunner.afterTestFailure((Test test, TestError failure) ->
        {
            console.writeLine(" - Failed");
            writeFailure(failure);
        });
        testRunner.afterTestSkipped((Test test) ->
        {
            final String skipMessage = test.getSkipMessage();
            console.writeLine(" - Skipped" + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage));
        });
        testRunner.afterTest((Test test) ->
        {
            decreaseIndent();
        });
    }

    /**
     * Increase the indent of the Console output.
     */
    private void increaseIndent()
    {
        indentedCharacterWriteStream.increaseIndent();
    }

    /**
     * Decrease the indent of the Console output.
     */
    private void decreaseIndent()
    {
        indentedCharacterWriteStream.decreaseIndent();
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
                console.writeLine(messageLine);
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
            console.writeLine("Message: " + throwable.getMessage());
        }
    }

    private void writeFailureCause(Throwable cause)
    {
        if (cause instanceof ErrorIterable)
        {
            final ErrorIterable errors = (ErrorIterable)cause;

            console.writeLine("Caused by:");
            int causeNumber = 0;
            for (final Throwable innerCause : errors)
            {
                ++causeNumber;
                console.write(causeNumber + ") " + innerCause.getClass().getName());

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
            console.writeLine("Caused by: " + cause.getClass().getName());

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
            console.writeLine("Stack Trace:");
            increaseIndent();
            for (StackTraceElement stackTraceElement : stackTraceElements)
            {
                console.writeLine("at " + stackTraceElement.toString());
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
            console.writeLine("Skipped Tests:");
            increaseIndent();
            int testSkippedNumber = 1;
            for (final Test skippedTest : skippedTests)
            {
                final String skipMessage = skippedTest.getSkipMessage();
                console.writeLine(testSkippedNumber + ") " + skippedTest.getFullName() + (Strings.isNullOrEmpty(skipMessage) ? "" : ": " + skipMessage));
                ++testSkippedNumber;
            }
            decreaseIndent();

            console.writeLine();
        }

        final Iterable<TestError> testFailures = testRunner.getTestFailures();
        if (testFailures.any())
        {
            console.writeLine("Test failures:");
            increaseIndent();

            int testFailureNumber = 1;
            for (final TestError failure : testFailures)
            {
                console.writeLine(testFailureNumber + ") " + failure.getTestScope());
                ++testFailureNumber;
                increaseIndent();
                writeFailure(failure);
                decreaseIndent();

                console.writeLine();
            }

            decreaseIndent();
        }

        console.writeLine("Tests Run:      " + testRunner.getFinishedTestCount());
        if (testRunner.getPassedTestCount() > 0)
        {
            console.writeLine("Tests Passed:   " + testRunner.getPassedTestCount());
        }
        if (testRunner.getFailedTestCount() > 0)
        {
            console.writeLine("Tests Failed:   " + testRunner.getFailedTestCount());
        }
        if (testRunner.getSkippedTestCount() > 0)
        {
            console.writeLine("Tests Skipped:  " + testRunner.getSkippedTestCount());
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
        final CommandLineParameterBoolean debugParameter = parameters.addDebug();
        final CommandLineParameterProfiler profilerParameter = parameters.addProfiler(console, ConsoleTestRunner.class);
        final CommandLineParameterList<String> testClassNamesParameter = parameters.addPositionStringList("test-class");

        final PathPattern pattern = patternParameter.getValue().await();
        final boolean debug = debugParameter.getValue().await();

        profilerParameter.await();

        if (debug)
        {
            console.writeLine("TestPattern: " + Strings.escapeAndQuote(pattern)).await();
        }

        final Stopwatch stopwatch = console.getStopwatch();
        stopwatch.start();

        final ConsoleTestRunner runner = new ConsoleTestRunner(console, pattern);

        final Iterable<String> testClassNames = testClassNamesParameter.getValues().await();
        for (final String testClassName : testClassNames)
        {
            if (debug)
            {
                console.write("Looking for class " + Strings.escapeAndQuote(testClassName) + "...");
            }

            Class<?> testClass = null;
            try
            {
                testClass = ConsoleTestRunner.class.getClassLoader().loadClass(testClassName);
                if (debug)
                {
                    console.writeLine("Found!");
                }
            }
            catch (ClassNotFoundException e)
            {
                if (debug)
                {
                    console.writeLine("Couldn't find " + Strings.escapeAndQuote(testClassName) + ".");
                }
            }

            if (testClass != null)
            {
                if (debug)
                {
                    console.write("Looking for static test(TestRunner) method in " + Strings.escapeAndQuote(testClassName) + "...");
                }

                java.lang.reflect.Method testMethod = null;
                try
                {
                    testMethod = testClass.getMethod("test", TestRunner.class);
                    if (debug)
                    {
                        console.writeLine("Found!");
                    }
                }
                catch (NoSuchMethodException e)
                {
                    if (debug)
                    {
                        console.writeLine("Couldn't find.");
                    }
                }

                if (testMethod != null)
                {
                    try
                    {
                        testMethod.invoke(null, runner);
                    }
                    catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        console.writeLine().await();
        runner.writeSummary();

        final Duration totalTestsDuration = stopwatch.stop();
        console.writeLine("Tests Duration: " + totalTestsDuration.toSeconds().toString("0.0")).await();

        console.setExitCode(runner.getFailedTestCount());
    }

    public static void main(String[] args)
    {
        Console.run(args, ConsoleTestRunner::run);
    }
}
