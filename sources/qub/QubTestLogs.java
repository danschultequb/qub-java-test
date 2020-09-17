package qub;

public interface QubTestLogs
{
    String actionName = "logs";
    String actionDescription = "Show the logs folder.";

    static void run(QubProcess process)
    {
        PreCondition.assertNotNull(process, "process");

        final Folder qubTestDataFolder = process.getQubProjectDataFolder().await();
        final Folder qubTestLogsFolder = QubTest.getLogsFolder(qubTestDataFolder);
        if (!qubTestLogsFolder.exists().await())
        {
            process.getOutputWriteStream().writeLine("The logs folder (" + qubTestLogsFolder + ") doesn't exist.").await();
        }
        else
        {
            final Path qubTestLogsFolderPath = qubTestLogsFolder.getPath();
            final DefaultApplicationLauncher applicationLauncher = process.getDefaultApplicationLauncher();
            applicationLauncher.openFileWithDefaultApplication(qubTestLogsFolderPath).await();
        }
    }
}
