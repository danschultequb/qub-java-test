package qub;

public class FakeJavaProcessRun implements FakeProcessRun, JavaArguments<FakeJavaProcessRun>
{
    private final FakeProcessRun fakeProcessRun;

    public FakeJavaProcessRun()
    {
        this.fakeProcessRun = new BasicFakeProcessRun(JavaProcessBuilder.executablePath);
    }

    @Override
    public Path getExecutablePath()
    {
        return this.fakeProcessRun.getExecutablePath();
    }

    @Override
    public FakeJavaProcessRun addArgument(String argument)
    {
        this.fakeProcessRun.addArgument(argument);
        return this;
    }

    @Override
    public FakeJavaProcessRun addArguments(String... arguments)
    {
        this.fakeProcessRun.addArguments(arguments);
        return this;
    }

    @Override
    public FakeJavaProcessRun addArguments(Iterable<String> arguments)
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
    public FakeJavaProcessRun setWorkingFolder(String workingFolderPath)
    {
        this.fakeProcessRun.setWorkingFolder(workingFolderPath);
        return this;
    }

    @Override
    public FakeJavaProcessRun setWorkingFolder(Path workingFolderPath)
    {
        this.fakeProcessRun.setWorkingFolder(workingFolderPath);
        return this;
    }

    @Override
    public FakeJavaProcessRun setWorkingFolder(Folder workingFolder)
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
    public FakeJavaProcessRun setFunction(int exitCode)
    {
        this.fakeProcessRun.setFunction(exitCode);
        return this;
    }

    @Override
    public FakeJavaProcessRun setFunction(Action0 action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeJavaProcessRun setFunction(Function0<Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeJavaProcessRun setFunction(Action1<ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeJavaProcessRun setFunction(Function1<ByteWriteStream,Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeJavaProcessRun setFunction(Action2<ByteWriteStream,ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeJavaProcessRun setFunction(Function2<ByteWriteStream,ByteWriteStream,Integer> function)
    {
        this.fakeProcessRun.setFunction(function);
        return this;
    }

    @Override
    public FakeJavaProcessRun setFunction(Action3<ByteReadStream,ByteWriteStream,ByteWriteStream> action)
    {
        this.fakeProcessRun.setFunction(action);
        return this;
    }

    @Override
    public FakeJavaProcessRun setFunction(Function3<ByteReadStream,ByteWriteStream,ByteWriteStream,Integer> function)
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
