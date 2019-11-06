package qub;

public class FakeConsoleTestRunnerProcessRun extends FakeProcessRunDecorator<FakeConsoleTestRunnerProcessRun> implements JavaArguments<FakeConsoleTestRunnerProcessRun>, ConsoleTestRunnerArguments<FakeConsoleTestRunnerProcessRun>
{
    public FakeConsoleTestRunnerProcessRun()
    {
        super(new BasicFakeProcessRun(JavaProcessBuilder.executablePath));
    }
}
