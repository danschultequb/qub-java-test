package qub;

public interface ConsoleTestRunnerTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(ConsoleTestRunner.class, () ->
        {
            runner.testGroup("constructor()", () ->
            {
                runner.test("with null console", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    test.assertThrows(() -> new ConsoleTestRunner(null, output, null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with null output", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        test.assertThrows(() -> new ConsoleTestRunner(process, null, null),
                        new PreConditionFailure("output cannot be null."));
                    }
                });

                runner.test("with null pattern", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);
                        test.assertEqual(0, testRunner.getFailedTestCount());
                    }
                });
            });

            runner.testGroup("writeFailure()", () ->
            {
                runner.test("with null error", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.writeFailure(null),
                            new PreConditionFailure("failure cannot be null."));

                        test.assertEqual("", process.getOutputWriteStream().getText().await());
                        test.assertEqual("", process.getErrorWriteStream().getText().await());
                    }
                });

                runner.test("with non-null error with no inner cause", runner.skip(), (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        testRunner.writeFailure(new TestError("fake test scope", Iterable.create("message line 1", "message line 2")));

                        test.assertEqual(
                            Iterable.create(
                                "  message line 1",
                                "  message line 2",
                                "  Stack Trace:",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$6(ConsoleTestRunnerTests.java:56)",
                                "    at qub.BasicTestRunner.test(BasicTestRunner.java:179)",
                                "    at qub.BasicTestRunner.test(BasicTestRunner.java:146)",
                                "    at qub.ConsoleTestRunner.test(ConsoleTestRunner.java:232)",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$8(ConsoleTestRunnerTests.java:46)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:110)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:81)",
                                "    at qub.ConsoleTestRunner.testGroup(ConsoleTestRunner.java:208)",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$13(ConsoleTestRunnerTests.java:27)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:110)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:72)",
                                "    at qub.ConsoleTestRunner.testGroup(ConsoleTestRunner.java:212)",
                                "    at qub.ConsoleTestRunnerTests.test(ConsoleTestRunnerTests.java:7)",
                                "    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
                                "    at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)",
                                "    at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)",
                                "    at java.lang.reflect.Method.invoke(Unknown Source)",
                                "    at qub.ConsoleTestRunner.run(ConsoleTestRunner.java:467)",
                                "    at qub.Console.run(Console.java:84)",
                                "    at qub.ConsoleTestRunner.main(ConsoleTestRunner.java:488)"),
                            Strings.getLines(process.getOutputWriteStream().getText().await()));
                        test.assertEqual("", process.getErrorWriteStream().getText().await());
                    }
                });

                runner.test("with non-null error with inner cause", runner.skip(), (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        testRunner.writeFailure(
                            new TestError("fake test scope", Iterable.create("message line 1", "message line 2"),
                                new Exception("hello world!")));

                        test.assertEqual(
                            Iterable.create(
                                "  message line 1",
                                "  message line 2",
                                "  Stack Trace:",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$7(ConsoleTestRunnerTests.java:98)",
                                "    at qub.BasicTestRunner.test(BasicTestRunner.java:179)",
                                "    at qub.BasicTestRunner.test(BasicTestRunner.java:146)",
                                "    at qub.ConsoleTestRunner.test(ConsoleTestRunner.java:232)",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$8(ConsoleTestRunnerTests.java:87)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:110)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:81)",
                                "    at qub.ConsoleTestRunner.testGroup(ConsoleTestRunner.java:208)",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$13(ConsoleTestRunnerTests.java:27)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:110)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:72)",
                                "    at qub.ConsoleTestRunner.testGroup(ConsoleTestRunner.java:212)",
                                "    at qub.ConsoleTestRunnerTests.test(ConsoleTestRunnerTests.java:7)",
                                "    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
                                "    at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)",
                                "    at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)",
                                "    at java.lang.reflect.Method.invoke(Unknown Source)",
                                "    at qub.ConsoleTestRunner.run(ConsoleTestRunner.java:467)",
                                "    at qub.Console.run(Console.java:84)",
                                "    at qub.ConsoleTestRunner.main(ConsoleTestRunner.java:488)",
                                "Caused by: java.lang.Exception",
                                "  Message: hello world!",
                                "  Stack Trace:",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$7(ConsoleTestRunnerTests.java:98)",
                                "    at qub.BasicTestRunner.test(BasicTestRunner.java:179)",
                                "    at qub.BasicTestRunner.test(BasicTestRunner.java:146)",
                                "    at qub.ConsoleTestRunner.test(ConsoleTestRunner.java:232)",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$8(ConsoleTestRunnerTests.java:87)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:110)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:81)",
                                "    at qub.ConsoleTestRunner.testGroup(ConsoleTestRunner.java:208)",
                                "    at qub.ConsoleTestRunnerTests.lambda$test$13(ConsoleTestRunnerTests.java:27)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:110)",
                                "    at qub.BasicTestRunner.testGroup(BasicTestRunner.java:72)",
                                "    at qub.ConsoleTestRunner.testGroup(ConsoleTestRunner.java:212)",
                                "    at qub.ConsoleTestRunnerTests.test(ConsoleTestRunnerTests.java:7)",
                                "    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
                                "    at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)",
                                "    at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)",
                                "    at java.lang.reflect.Method.invoke(Unknown Source)",
                                "    at qub.ConsoleTestRunner.run(ConsoleTestRunner.java:467)",
                                "    at qub.Console.run(Console.java:84)",
                                "    at qub.ConsoleTestRunner.main(ConsoleTestRunner.java:488)"),
                            Strings.getLines(process.getOutputWriteStream().getText().await()));
                        test.assertEqual("", process.getErrorWriteStream().getText().await());
                    }
                });
            });

            runner.testGroup("writeMessageLines()", () ->
            {
                runner.test("with null failure", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.writeMessageLines(null),
                            new PreConditionFailure("failure cannot be null."));

                        test.assertEqual("", process.getOutputWriteStream().getText().await());
                        test.assertEqual("", process.getErrorWriteStream().getText().await());
                    }
                });

                runner.test("with one empty message line", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        testRunner.writeMessageLines(new TestError("fake test scope", Iterable.create("")));

                        test.assertEqual(
                            Iterable.create(""),
                            Strings.getLines(process.getOutputWriteStream().getText().await()));
                        test.assertEqual("", process.getErrorWriteStream().getText().await());
                    }
                });
            });

            runner.test("skip()", (Test test) ->
            {
                try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                {
                    final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                    final Skip skip1 = testRunner.skip();
                    test.assertNotNull(skip1);
                    test.assertEqual("", skip1.getMessage());
                    final Skip skip2 = testRunner.skip();
                    test.assertEqual("", skip2.getMessage());
                    test.assertNotSame(skip1, skip2);
                }
            });

            runner.testGroup("skip(boolean)", () ->
            {
                runner.test("with false", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertNull(testRunner.skip(false));
                    }
                });

                runner.test("with true", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        final Skip skip1 = testRunner.skip(true);
                        test.assertNotNull(skip1);
                        test.assertEqual("", skip1.getMessage());
                        final Skip skip2 = testRunner.skip(true);
                        test.assertNotNull(skip2);
                        test.assertEqual("", skip2.getMessage());
                        test.assertNotSame(skip1, skip2);
                    }
                });
            });

            runner.testGroup("skip(boolean,String)", () ->
            {
                runner.test("with false and null message", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.skip(false, null),
                            new PreConditionFailure("message cannot be null."));
                    }
                });

                runner.test("with false and empty message", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.skip(false, ""),
                            new PreConditionFailure("message cannot be empty."));
                    }
                });

                runner.test("with false and non-empty message", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertNull(testRunner.skip(false, "hello"));
                    }
                });

                runner.test("with true and non-empty message", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        final Skip skip1 = testRunner.skip(true, "hello");
                        test.assertNotNull(skip1);
                        test.assertEqual("hello", skip1.getMessage());
                        final Skip skip2 = testRunner.skip(true, "there");
                        test.assertNotNull(skip2);
                        test.assertEqual("there", skip2.getMessage());
                        test.assertNotSame(skip1, skip2);
                    }
                });
            });

            runner.testGroup("skip(String)", () ->
            {
                runner.test("with null message", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.skip(null),
                            new PreConditionFailure("message cannot be null."));
                    }
                });

                runner.test("with empty message", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.skip(""),
                            new PreConditionFailure("message cannot be empty."));
                    }
                });

                runner.test("with non-empty message", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        final Skip skip1 = testRunner.skip("hello");
                        test.assertNotNull(skip1);
                        test.assertEqual("hello", skip1.getMessage());
                        final Skip skip2 = testRunner.skip("there");
                        test.assertNotNull(skip2);
                        test.assertEqual("there", skip2.getMessage());
                        test.assertNotSame(skip1, skip2);
                    }
                });
            });

            runner.testGroup("testGroup(String,Action0)", () ->
            {
                runner.test("with null testGroupName", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.testGroup((String)null, Action0.empty),
                            new PreConditionFailure("testGroupName cannot be null."));
                    }
                });

                runner.test("with empty testGroupName", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.testGroup("", Action0.empty),
                            new PreConditionFailure("testGroupName cannot be empty."));
                    }
                });

                runner.test("with null testGroupAction", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create())
                    {
                        final ConsoleTestRunner testRunner = new ConsoleTestRunner(process, process.getOutputWriteStream(), null);

                        test.assertThrows(() -> testRunner.testGroup("hello", null),
                            new PreConditionFailure("testGroupAction cannot be null."));
                    }
                });
            });
        });
    }
}
