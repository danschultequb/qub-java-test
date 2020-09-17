package qub;

public interface QubTest
{
    String applicationName = "qub-test";
    String applicationDescription = "Used to run tests in a source code projects.";

    static void main(String[] args)
    {
        QubProcess.run(args, QubTest::run);
    }

    static void run(QubProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final CommandLineActions<QubProcess> actions = process.<QubProcess>createCommandLineActions()
            .setApplicationName(QubTest.applicationName)
            .setApplicationDescription(QubTest.applicationDescription);

        actions.addAction(QubTestRun.actionName, QubTestRun::getParameters, QubTestRun::run)
            .setDescription(QubTestRun.actionDescription)
            .setDefaultAction();

        actions.addAction(QubTestLogs.actionName, QubTestLogs::run)
            .setDescription(QubTestLogs.actionDescription);

        actions.run(process);
    }

    static String getActionFullName(String actionName)
    {
        PreCondition.assertNotNullAndNotEmpty(actionName, "actionName");

        return QubTest.applicationName + " " + actionName;
    }

    static Folder getLogsFolder(Folder qubTestDataFolder)
    {
        PreCondition.assertNotNull(qubTestDataFolder, "qubTestDataFolder");

        return qubTestDataFolder.getFolder("logs").await();
    }
}