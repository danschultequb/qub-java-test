package qub;

public class FakeConsoleTestRunnerProcessRun implements FakeProcessRun, JavaArguments<FakeConsoleTestRunnerProcessRun>, ConsoleTestRunnerArguments<FakeConsoleTestRunnerProcessRun>
{
    private final FakeProcessRun fakeProcessRun;

    public FakeConsoleTestRunnerProcessRun()
    {
        this.fakeProcessRun = new BasicFakeProcessRun(JavaProcessBuilder.executablePath);
    }

    @Override
    public Path getExecutablePath()
    {
        return this.fakeProcessRun.getExecutablePath();
    }

    @Override
    public FakeConsoleTestRunnerProcessRun addArgument(String argument)
    {
        this.fakeProcessRun.addArgument(argument);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun addArguments(String... arguments)
    {
        this.fakeProcessRun.addArguments(arguments);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun addArguments(Iterable<String> arguments)
    {
        this.fakeProcessRun.addArguments(arguments);
        return this;
    }

    @Override
    public Iterable<String> getArguments()
    {
        return this.fakeProcessRun.getArguments();
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setWorkingFolder(String workingFolderPath)
    {
        this.fakeProcessRun.setWorkingFolder(workingFolderPath);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setWorkingFolder(Path workingFolderPath)
    {
        this.fakeProcessRun.setWorkingFolder(workingFolderPath);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setWorkingFolder(Folder workingFolder)
    {
        this.fakeProcessRun.setWorkingFolder(workingFolder);
        return this;
    }

    @Override
    public Path getWorkingFolderPath()
    {
        return this.fakeProcessRun.getWorkingFolderPath();
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(int exitCode)
    {
        this.fakeProcessRun.setFunction(exitCode);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(Action0 action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(Function0<Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(Action1<ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(Function1<ByteWriteStream,Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(Action2<ByteWriteStream,ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(Function2<ByteWriteStream,ByteWriteStream,Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(Action3<ByteReadStream,ByteWriteStream,ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeConsoleTestRunnerProcessRun setFunction(Function3<ByteReadStream,ByteWriteStream,ByteWriteStream,Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public Function3<ByteReadStream,ByteWriteStream,ByteWriteStream,Integer> getFunction()
    {
        return this.fakeProcessRun.getFunction();
    }
}
