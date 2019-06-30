package qub;

public class FakeJavaRunner extends JavaRunner
{
    private int exitCode;

    public FakeJavaRunner setExitCode(int exitCode)
    {
        this.exitCode = exitCode;

        return this;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    @Override
    public Result<Void> run(Console console, CommandLineParameterProfiler profile)
    {
        if (isVerbose())
        {
            String command = "java.exe";

            if (getJacocoFolder() != null)
            {
                command += " -javaagent:" + getJacocoAgentJarFile().toString() + "=destfile=" + getCoverageExecFile().toString();
            }

            if (profile != null && profile.getValue().await())
            {
                command += profile.toString();
            }

            command += " -classpath " + getClassPath();

            command += " qub.ConsoleTestRunner";

            command += " " + Strings.join(' ', getFullClassNames());

            final String pattern = getPattern();
            if (!Strings.isNullOrEmpty(pattern))
            {
                command += " --pattern=" + pattern;
            }

            writeVerboseLine(command).await();
        }

        console.writeLine().await();

        console.setExitCode(exitCode);

        return Result.success();
    }
}
