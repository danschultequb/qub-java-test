package qub;

public class RealJavaRunner extends JavaRunner
{
    @Override
    public Result<Integer> run(Console console)
    {
        final ProcessBuilder javaExe = console.getProcessBuilder("java.exe").await();
        javaExe.redirectOutput(console.getOutputAsByteWriteStream());
        javaExe.redirectError(console.getErrorAsByteWriteStream());

        final Folder outputFolder = getOutputFolder();

        final Folder jacocoFolder = getJacocoFolder();
        if (jacocoFolder != null)
        {
            javaExe.addArgument("-javaagent:" + getJacocoAgentJarFile().toString() + "=destfile=" + getCoverageExecFile().toString());
        }

        javaExe.addArguments("-classpath", getClassPath());

        javaExe.addArgument("qub.ConsoleTestRunner");

        javaExe.addArguments(getFullClassNames());

        final String pattern = getPattern();
        if (!Strings.isNullOrEmpty(pattern))
        {
            javaExe.addArgument("-pattern=" + pattern);
        }

        final boolean isVerbose = QubTest.isVerbose(console);
        if (isVerbose)
        {
            QubTest.verbose(console, javaExe.getCommand());
        }

        console.writeLine().await();

        return javaExe.run()
            .thenResult((Integer exitCode) ->
            {
                Result<Integer> result;
                if (exitCode == null || exitCode != 0 || jacocoFolder == null)
                {
                    result = Result.success(exitCode);
                }
                else
                {
                    console.writeLine();
                    console.writeLine("Analyzing coverage...");

                    final File jacocoCLIJarFile = jacocoFolder.getFile("jacococli.jar").await();
                    final Folder coverageFolder = outputFolder.getFolder("coverage").await();

                    final ProcessBuilder jacococli = console.getProcessBuilder("java").getValue();
                    if (isVerbose)
                    {
                        jacococli.redirectOutput(console.getOutputAsByteWriteStream());
                        jacococli.redirectError(console.getErrorAsByteWriteStream());
                    }
                    jacococli.addArguments("-jar", jacocoCLIJarFile.toString());
                    jacococli.addArgument("report");
                    jacococli.addArgument(getCoverageExecFile().toString());

                    final Path currentFolderPath = console.getCurrentFolderPath();
                    final Iterable<File> classFiles = getClassFiles();
                    for (final File classFile : classFiles)
                    {
                        jacococli.addArguments("--classfiles", classFile.relativeTo(currentFolderPath).toString());
                    }
                    jacococli.addArguments("--sourcefiles", getSourceFolder().toString());
                    jacococli.addArguments("--html", coverageFolder.toString());

                    if (isVerbose)
                    {
                        console.writeLine();
                        QubTest.verbose(console, jacococli.getCommand());
                        jacococli.redirectOutput(console.getOutputAsByteWriteStream());
                        jacococli.redirectError(console.getErrorAsByteWriteStream());
                    }
                    result = jacococli.run();

                    final File coverageHtmlFile = coverageFolder.getFile("index.html").await();
                    try
                    {
                        java.awt.Desktop.getDesktop().open(new java.io.File(coverageHtmlFile.toString()));
                    }
                    catch (java.io.IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                return result;
            });
    }
}
