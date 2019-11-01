package qub;

public class JavaProcessBuilderDecorator<T extends JavaProcessBuilder> extends JavaProcessBuilder
{
    private final JavaProcessBuilder javaProcessBuilder;

    protected JavaProcessBuilderDecorator(JavaProcessBuilder javaProcessBuilder)
    {
        super(javaProcessBuilder);

        this.javaProcessBuilder = javaProcessBuilder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T addClasspath(String classpath)
    {
        this.javaProcessBuilder.addClasspath(classpath);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T addJavaAgent(String javaAgent)
    {
        this.javaProcessBuilder.addJavaAgent(javaAgent);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T addArgument(String argument)
    {
        this.javaProcessBuilder.addArgument(argument);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T addArguments(String... arguments)
    {
        this.javaProcessBuilder.addArguments(arguments);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T addArguments(Iterable<String> arguments)
    {
        this.javaProcessBuilder.addArguments(arguments);
        return (T)this;
    }

    @Override
    public Iterable<String> getArguments()
    {
        return this.javaProcessBuilder.getArguments();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setWorkingFolder(String workingFolderPath)
    {
        this.javaProcessBuilder.setWorkingFolder(workingFolderPath);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setWorkingFolder(Path workingFolderPath)
    {
        this.javaProcessBuilder.setWorkingFolder(workingFolderPath);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setWorkingFolder(Folder workingFolder)
    {
        this.javaProcessBuilder.setWorkingFolder(workingFolder);
        return (T)this;
    }

    @Override
    public Path getWorkingFolderPath()
    {
        return this.javaProcessBuilder.getWorkingFolderPath();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectInput(ByteReadStream redirectedInputStream)
    {
        this.javaProcessBuilder.redirectInput(redirectedInputStream);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectOutput(Action1<ByteReadStream> redirectOutputAction)
    {
        this.javaProcessBuilder.redirectOutput(redirectOutputAction);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectOutput(ByteWriteStream redirectedOutputStream)
    {
        this.javaProcessBuilder.redirectOutput(redirectedOutputStream);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectOutputLines(Action1<String> onOutputLine)
    {
        this.javaProcessBuilder.redirectOutputLines(onOutputLine);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectOutputTo(StringBuilder builder)
    {
        this.javaProcessBuilder.redirectOutputTo(builder);
        return (T)this;
    }

    @Override
    public StringBuilder redirectOutput()
    {
        return this.javaProcessBuilder.redirectOutput();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectError(Action1<ByteReadStream> redirectErrorAction)
    {
        this.javaProcessBuilder.redirectError(redirectErrorAction);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectError(ByteWriteStream redirectedErrorStream)
    {
        this.javaProcessBuilder.redirectError(redirectedErrorStream);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectErrorLines(Action1<String> onErrorLine)
    {
        this.javaProcessBuilder.redirectErrorLines(onErrorLine);
        return (T)this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T redirectErrorTo(StringBuilder builder)
    {
        this.javaProcessBuilder.redirectErrorTo(builder);
        return (T)this;
    }
}
