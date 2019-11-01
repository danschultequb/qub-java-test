package qub;

public class JacocoCliProcessBuilder extends JavaProcessBuilderDecorator<JacocoCliProcessBuilder> implements JacocoCliArguments<JacocoCliProcessBuilder>
{
    private Folder outputFolder;

    private JacocoCliProcessBuilder(JavaProcessBuilder javaProcessBuilder)
    {
        super(javaProcessBuilder);
    }

    /**
     * Get a JavaProcessBuilder from the provided Process.
     * @param process The Process to get the JavaProcessBuilder from.
     * @return The JavaProcessBuilder.
     */
    public static Result<JacocoCliProcessBuilder> get(Process process)
    {
        PreCondition.assertNotNull(process, "process");

        return JacocoCliProcessBuilder.get(process.getProcessFactory());
    }

    /**
     * Get a JavaProcessBuilder from the provided ProcessFactory.
     * @param processFactory The ProcessFactory to get the JavaProcessBuilder from.
     * @return The JavaProcessBuilder.
     */
    public static Result<JacocoCliProcessBuilder> get(ProcessFactory processFactory)
    {
        PreCondition.assertNotNull(processFactory, "processFactory");

        return Result.create(() ->
        {
            return new JacocoCliProcessBuilder(JavaProcessBuilder.get(processFactory).await());
        });
    }
}
