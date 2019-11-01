package qub;

public class FakeJacocoCliProcessRun implements FakeProcessRun, JavaArguments<FakeJacocoCliProcessRun>, JacocoCliArguments<FakeJacocoCliProcessRun>
{
    private final FakeProcessRun fakeProcessRun;

    public FakeJacocoCliProcessRun()
    {
        this.fakeProcessRun = new BasicFakeProcessRun(JavaProcessBuilder.executablePath);
    }

    @Override
    public Path getExecutablePath()
    {
        return this.fakeProcessRun.getExecutablePath();
    }

    @Override
    public FakeJacocoCliProcessRun addArgument(String argument)
    {
        this.fakeProcessRun.addArgument(argument);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun addArguments(String... arguments)
    {
        this.fakeProcessRun.addArguments(arguments);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun addArguments(Iterable<String> arguments)
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
    public FakeJacocoCliProcessRun setWorkingFolder(String workingFolderPath)
    {
        this.fakeProcessRun.setWorkingFolder(workingFolderPath);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setWorkingFolder(Path workingFolderPath)
    {
        this.fakeProcessRun.setWorkingFolder(workingFolderPath);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setWorkingFolder(Folder workingFolder)
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
    public FakeJacocoCliProcessRun setFunction(int exitCode)
    {
        this.fakeProcessRun.setFunction(exitCode);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setFunction(Action0 action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setFunction(Function0<Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setFunction(Action1<ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setFunction(Function1<ByteWriteStream,Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setFunction(Action2<ByteWriteStream,ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setFunction(Function2<ByteWriteStream,ByteWriteStream,Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setFunction(Action3<ByteReadStream,ByteWriteStream,ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeJacocoCliProcessRun setFunction(Function3<ByteReadStream,ByteWriteStream,ByteWriteStream,Integer> function)
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
