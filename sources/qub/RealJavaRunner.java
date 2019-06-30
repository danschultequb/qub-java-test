package qub;

public class RealJavaRunner extends JavaRunner
{
    @Override
    public Result<Void> run(Console console, CommandLineParameterProfiler profile)
    {
        PreCondition.assertNotNull(console, "console");
        PreCondition.assertNotNull(profile, "profile");

        return Result.create(() ->
        {
            final ProcessBuilder javaExe = console.getProcessBuilder("java.exe").await();
            javaExe.redirectOutput(console.getOutputByteWriteStream());
            javaExe.redirectError(console.getErrorByteWriteStream());
            javaExe.redirectInput(console.getInputByteReadStream());

            final Folder outputFolder = getOutputFolder();

            final Folder jacocoFolder = getJacocoFolder();
            if (jacocoFolder != null)
            {
                javaExe.addArgument("-javaagent:" + getJacocoAgentJarFile().toString() + "=destfile=" + getCoverageExecFile().toString());
            }

            javaExe.addArguments("-classpath", getClassPath());

            javaExe.addArgument("qub.ConsoleTestRunner");

            if (profile.getValue().await())
            {
                javaExe.addArgument("--" + profile.getName());
            }

            javaExe.addArguments(getFullClassNames());

            final String pattern = getPattern();
            if (!Strings.isNullOrEmpty(pattern))
            {
                javaExe.addArgument("--pattern=" + pattern);
            }

            writeVerboseLine(javaExe.getCommand()).await();

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
                if (getTestFolder() != null)
                {
                    jacococli.addArguments("--sourcefiles", getTestFolder().toString());
                }
                jacococli.addArguments("--html", coverageFolder.toString());

                if (isVerbose())
                {
                    console.writeLine().await();
                    writeVerboseLine(jacococli.getCommand()).await();
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

            console.setExitCode(result);
        });
    }
}
