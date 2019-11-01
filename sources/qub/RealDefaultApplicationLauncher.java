package qub;

public class RealDefaultApplicationLauncher implements DefaultApplicationLauncher
{
    @Override
    public Result<Void> open(File fileToOpen)
    {
        PreCondition.assertNotNull(fileToOpen, "fileToOpen");

        return Result.create(() ->
        {
            try
            {
                java.awt.Desktop.getDesktop().open(new java.io.File(fileToOpen.toString()));
            }
            catch (java.io.IOException e)
            {
                throw Exceptions.asRuntime(e);
            }
        });
    }
}
