package qub;

public class RealJavaRunner extends JavaRunner
{
    @Override
    public Result<Integer> run(Console console, boolean profile)
    {
        return Result.create(() ->
        {
            final ProcessBuilder javaExe = console.getProcessBuilder("java.exe").await();
            javaExe.redirectOutput(console.getOutputByteWriteStream());
            javaExe.redirectError(console.getErrorByteWriteStream());

            final Folder outputFolder = getOutputFolder();

            final Folder jacocoFolder = getJacocoFolder();
            if (jacocoFolder != null)
            {
                javaExe.addArgument("-javaagent:" + getJacocoAgentJarFile().toString() + "=destfile=" + getCoverageExecFile().toString());
            }

            if (profile)
            {
                javaExe.addArgument("-profile");
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

            int result = javaExe.run().await();

            if (jacocoFolder != null)
            {
                console.writeLine().await();
                console.writeLine("Analyzing coverage...").await();

                final File jacocoCLIJarFile = jacocoFolder.getFile("jacococli.jar").await();
                final Folder coverageFolder = outputFolder.getFolder("coverage").await();

                final ProcessBuilder jacococli = console.getProcessBuilder("java").await();
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
                    console.writeLine().await();
                    QubTest.verbose(console, jacococli.getCommand());
                    jacococli.redirectOutput(console.getOutputByteWriteStream());
                    jacococli.redirectError(console.getErrorByteWriteStream());
                }

                final int coverageExitCode = jacococli.run().await();
                if (result == 0)
                {
                    result = coverageExitCode;
                }

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
