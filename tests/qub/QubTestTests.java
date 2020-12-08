package qub;

public interface QubTestTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(QubTest.class, () ->
        {
            runner.testGroup("main(String[])", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> QubTest.main((String[])null),
                        new PreConditionFailure("args cannot be null."));
                });
            });

            runner.testGroup("run(DesktopProcess)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> QubTest.run(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with --?", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create("--?"))
                    {
                        QubTest.run(process);

                        test.assertEqual(
                            Iterable.create(
                                "Usage: qub-test [--action=]<action-name> [--help]",
                                "  Used to run tests in a source code projects.",
                                "  --action(a): The name of the action to invoke.",
                                "  --help(?):   Show the help message for this application.",
                                "",
                                "Actions:",
                                "  logs:          Show the logs folder.",
                                "  run (default): Run tests in a source code project."),
                            Strings.getLines(process.getOutputWriteStream().getText().await()));
                        test.assertEqual(-1, process.getExitCode());
                    }
                });

                runner.test("with foo", (Test test) ->
                {
                    try (final FakeDesktopProcess process = FakeDesktopProcess.create("foo"))
                    {
                        QubTest.run(process);

                        test.assertEqual(
                            Iterable.create(
                                "Unrecognized action: \"foo\"",
                                "",
                                "Usage: qub-test [--action=]<action-name> [--help]",
                                "  Used to run tests in a source code projects.",
                                "  --action(a): The name of the action to invoke.",
                                "  --help(?):   Show the help message for this application.",
                                "",
                                "Actions:",
                                "  logs:          Show the logs folder.",
                                "  run (default): Run tests in a source code project."),
                            Strings.getLines(process.getOutputWriteStream().getText().await()));
                        test.assertEqual(-1, process.getExitCode());
                    }
                });
            });
        });
    }
}
