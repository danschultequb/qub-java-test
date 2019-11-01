package qub;

public class FakeDefaultApplicationLauncher implements DefaultApplicationLauncher
{
    @Override
    public Result<Void> open(File fileToOpen)
    {
        PreCondition.assertNotNull(fileToOpen, "fileToOpen");

        return Result.success();
    }
}
