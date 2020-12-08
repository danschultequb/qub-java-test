package qub;

public interface QubTest
{
    String applicationName = "qub-test";
    String applicationDescription = "Used to run tests in a source code projects.";

    static void main(String[] args)
    {
        DesktopProcess.run(args, QubTest::run);
    }

    static void run(DesktopProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineActions actions = process.createCommandLineActions()
            .setApplicationName(QubTest.applicationName)
            .setApplicationDescription(QubTest.applicationDescription);

        actions.addAction(QubTestRun.actionName, QubTestRun::getParameters, QubTestRun::run)
            .setDescription(QubTestRun.actionDescription)
            .setDefaultAction();

        CommandLineLogsAction.addAction(actions);

        actions.run(process);
    }

    static String getActionFullName(String actionName)
    {
        PreCondition.assertNotNullAndNotEmpty(actionName, "actionName");

        return QubTest.applicationName + " " + actionName;
    }
}