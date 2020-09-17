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

            runner.testGroup("run(QubProcess)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> QubTest.run(null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with --?", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    try (final QubProcess process = QubProcess.create("--?"))
                    {
                        process.setOutputWriteStream(output);

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
                            Strings.getLines(output.getText().await()));
                        test.assertEqual(-1, process.getExitCode());
                    }
                });

                runner.test("with foo", (Test test) ->
                {
                    final InMemoryCharacterToByteStream output = InMemoryCharacterToByteStream.create();
                    try (final QubProcess process = QubProcess.create("foo"))
                    {
                        process.setOutputWriteStream(output);

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
                            Strings.getLines(output.getText().await()));
                        test.assertEqual(-1, process.getExitCode());
                    }
                });
            });
        });
    }
}
