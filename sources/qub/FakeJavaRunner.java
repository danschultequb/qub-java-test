package qub;

public class FakeJavaRunner extends JavaRunner
{
    private int exitCode;

    public void setExitCode(int exitCode)
    {
        this.exitCode = exitCode;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    @Override
    public Result<Integer> run(Console console)
    {
        if (QubTest.isVerbose(console))
        {
            String command = "java.exe";

            final File jacocoAgentJarFile = getJacocoAgentJarFile();
            if (jacocoAgentJarFile != null)
            {
                final File coverageExecFile = getCoverageExecFile();
                command += " -javaagent:" + getJacocoAgentJarFile().toString() + "=destfile=" + coverageExecFile.toString();
            }

            command += " -classpath " + getClassPath();

            command += " qub.ConsoleTestRunner";

            command += " " + Strings.join(' ', getFullClassNames());

            final String pattern = getPattern();
            if (!Strings.isNullOrEmpty(pattern))
            {
                command += " -pattern=" + pattern;
            }

            QubTest.verbose(console, command);
        }
        return Result.success(exitCode);
    }
}
