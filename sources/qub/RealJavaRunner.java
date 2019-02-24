package qub;

public class RealJavaRunner extends JavaRunner
{
    @Override
    public Result<Integer> run(Console console)
    {
        final ProcessBuilder java = console.getProcessBuilder("java.exe").await();
        java.redirectOutput(console.getOutputAsByteWriteStream());
        java.redirectError(console.getErrorAsByteWriteStream());

        final File jacocoAgentJarFile = getJacocoAgentJarFile();
        if (jacocoAgentJarFile != null)
        {
            final File coverageExecFile = getCoverageExecFile();
            java.addArgument("-javaagent:" + getJacocoAgentJarFile().toString() + "=destfile=" + coverageExecFile.toString());
        }

        java.addArguments("-classpath", getClassPath());

        java.addArgument("qub.ConsoleTestRunner");

        java.addArguments(getFullClassNames());

        final String pattern = getPattern();
        if (!Strings.isNullOrEmpty(pattern))
        {
            java.addArgument("-pattern=" + pattern);
        }

        if (QubTest.isVerbose(console))
        {
            QubTest.verbose(console, java.getCommand());
        }

        console.writeLine().await();

        return java.run();
    }
}
