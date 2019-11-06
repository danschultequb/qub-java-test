package qub;

public class FakeJacocoCliProcessRun extends FakeProcessRunDecorator<FakeJacocoCliProcessRun> implements JavaArguments<FakeJacocoCliProcessRun>, JacocoCliArguments<FakeJacocoCliProcessRun>
{
    public FakeJacocoCliProcessRun()
    {
        super(new BasicFakeProcessRun(JavaProcessBuilder.executablePath));
    }
}
