package qub;

public interface DefaultApplicationLauncher
{
    /**
     * Open the provided file with the registered default application.
     * @param fileToOpen The file to open.
     * @return The result of opening the file.
     */
    Result<Void> open(File fileToOpen);
}
