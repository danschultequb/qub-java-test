package qub;

public interface QubTest
{
    static void main(String[] args)
    {
        DesktopProcess.run(args, QubTest::run);
    }

    static void run(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        process.createCommandLineActions()
            .setApplicationName("qub-test")
            .setApplicationDescription("Used to run tests in a source code projects.")
            .addAction(QubTestRun::addAction)
            .addAction(CommandLineLogsAction::addAction)
            .run();
    }
}