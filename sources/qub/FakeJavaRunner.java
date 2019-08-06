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
    public Result<Void> run(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        if (isVerbose())
        {
            String command = "java.exe";

            if (getJacocoFolder() != null)
            {
                command += " -javaagent:" + getJacocoAgentJarFile().toString() + "=destfile=" + getCoverageExecFile().toString();
            }

            command += " -classpath " + getClassPath();

            command += " qub.ConsoleTestRunner";

            final CommandLineParameterProfiler profiler = getProfiler();
            if (profiler != null)
            {
                command += " --" + profiler.getName() + "=" + profiler.getValue().await();
            }

            final CommandLineParameterBoolean testJson = getTestJson();
            if (testJson != null)
            {
                command += " --" + testJson.getName() + "=" + testJson.getValue().await();
            }

            for (final String fullClassName : getFullClassNames())
            {
                command += " " + fullClassName;
            }

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
