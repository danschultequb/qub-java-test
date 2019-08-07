package qub;

public class RealJavaRunner extends JavaRunner
{
    public static Result<ProcessBuilder> getJavaProcessBuilder(Process process)
    {
        return RealJavaRunner.getJavaProcessBuilder(process, true);
    }

    public static Result<ProcessBuilder> getJavaProcessBuilder(Process process, boolean redirectStreams)
    {
        PreCondition.assertNotNull(process, "process");

        return Result.create(() ->
        {
            final ProcessBuilder javaProcessBuilder = process.getProcessBuilder("java").await();
            if (redirectStreams)
            {
                javaProcessBuilder.redirectOutput(process.getOutputByteWriteStream());
                javaProcessBuilder.redirectError(process.getErrorByteWriteStream());
                javaProcessBuilder.redirectInput(process.getInputByteReadStream());
            }
            return javaProcessBuilder;
        });
    }

    public void addTestArguments(ProcessBuilder processBuilder)
    {
        PreCondition.assertNotNull(processBuilder, "processBuilder");

        final Folder jacocoFolder = getJacocoFolder();
        if (jacocoFolder != null)
        {
            processBuilder.addArgument("-javaagent:" + getJacocoAgentJarFile().toString() + "=destfile=" + getCoverageExecFile().toString());
        }

        processBuilder.addArguments("-classpath", getClassPath());

        processBuilder.addArgument("qub.ConsoleTestRunner");

        final CommandLineParameterProfiler profiler = getProfiler();
        if (profiler != null)
        {
            processBuilder.addArgument("--" + profiler.getName() + "=" + profiler.getValue().await());
        }

        final CommandLineParameterBoolean testJson = getTestJson();
        if (testJson != null)
        {
            processBuilder.addArgument("--" + testJson.getName() + "=" + testJson.getValue().await());
        }

        final String pattern = getPattern();
        if (!Strings.isNullOrEmpty(pattern))
        {
            processBuilder.addArgument("--pattern=" + pattern);
        }

        processBuilder.addArguments(getFullClassNames());
    }

    public static Result<File> getJacocoCLIJarFile(Folder jacocoFolder)
    {
        PreCondition.assertNotNull(jacocoFolder, "jacocoFolder");

        return jacocoFolder.getFile("jacococli.jar");
    }

    public Result<Folder> getCoverageFolder()
    {
        return getOutputFolder().getFolder("coverage");
    }

    public void addCoverageArguments(Process process, ProcessBuilder processBuilder)
    {
        PreCondition.assertNotNull(process, "process");
        PreCondition.assertNotNull(processBuilder, "processBuilder");

        final Folder jacocoFolder = this.getJacocoFolder();
        final File jacocoCLIJarFile = RealJavaRunner.getJacocoCLIJarFile(jacocoFolder).await();
        processBuilder.addArguments("-jar", jacocoCLIJarFile.toString());
        processBuilder.addArgument("report");
        processBuilder.addArgument(getCoverageExecFile().toString());

        final Path currentFolderPath = process.getCurrentFolderPath();
        final Iterable<File> classFiles = getClassFilesForCoverage();
        for (final File classFile : classFiles)
        {
            processBuilder.addArguments("--classfiles", classFile.relativeTo(currentFolderPath).toString());
        }

        final Coverage coverage = this.getCoverage();
        if (coverage == Coverage.Sources || coverage == Coverage.All)
        {
            processBuilder.addArguments("--sourcefiles", getSourceFolder().toString());
        }
        if ((coverage == Coverage.Tests || coverage == Coverage.All) && getTestFolder() != null)
        {
            processBuilder.addArguments("--sourcefiles", getTestFolder().toString());
        }

        final Folder coverageFolder = this.getCoverageFolder().await();
        processBuilder.addArguments("--html", coverageFolder.toString());
    }

    public Result<File> getCoverageIndexHtmlFile()
    {
        final Folder coverageFolder = this.getCoverageFolder().await();
        return coverageFolder.getFile("index.html");
    }

    @Override
    public Result<Void> run(Console console)
    {
        PreCondition.assertNotNull(console, "console");

        return Result.create(() ->
        {
            final ProcessBuilder javaExe = this.getJavaProcessBuilder(console).await();
            this.addTestArguments(javaExe);

            writeVerboseLine(javaExe.getCommand()).await();

            console.writeLine().await();

            int result = javaExe.run().await();

            final Folder jacocoFolder = this.getJacocoFolder();
            if (jacocoFolder != null)
            {
                console.writeLine().await();
                console.writeLine("Analyzing coverage...").await();

                final ProcessBuilder jacococli = RealJavaRunner.getJavaProcessBuilder(console, this.isVerbose()).await();
                addCoverageArguments(console, jacococli);

                if (isVerbose())
                {
                    console.writeLine().await();
                    writeVerboseLine(jacococli.getCommand()).await();
                }

                final int coverageExitCode = jacococli.run().await();
                if (result == 0)
                {
                    result = coverageExitCode;
                }

                final File coverageIndexHtmlFile = this.getCoverageIndexHtmlFile().await();
                try
                {
                    java.awt.Desktop.getDesktop().open(new java.io.File(coverageIndexHtmlFile.toString()));
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
