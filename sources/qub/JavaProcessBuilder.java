package qub;

public class JavaProcessBuilder extends ProcessBuilderDecorator<JavaProcessBuilder> implements JavaArguments<JavaProcessBuilder>
{
    public static final String executablePathString = "java";
    public static final Path executablePath = Path.parse(JavaProcessBuilder.executablePathString);

    protected JavaProcessBuilder(ProcessBuilder processBuilder)
    {
        super(processBuilder);
    }

    /**
     * Get a JavaProcessBuilder from the provided Process.
     * @param process The Process to get the JavaProcessBuilder from.
     * @return The JavaProcessBuilder.
     */
    public static Result<? extends JavaProcessBuilder> get(Process process)
    {
        PreCondition.assertNotNull(process, "process");

        return JavaProcessBuilder.get(process.getProcessFactory());
    }

    /**
     * Get a JavaProcessBuilder from the provided ProcessFactory.
     * @param processFactory The ProcessFactory to get the JavaProcessBuilder from.
     * @return The JavaProcessBuilder.
     */
    public static Result<? extends JavaProcessBuilder> get(ProcessFactory processFactory)
    {
        PreCondition.assertNotNull(processFactory, "processFactory");

        return Result.create(() ->
        {
            return new JavaProcessBuilder(processFactory.getProcessBuilder(JavaProcessBuilder.executablePath).await());
        });
    }
}
