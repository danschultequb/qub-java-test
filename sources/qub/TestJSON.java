package qub;

/**
 * The data of a test.json file.
 */
public class TestJSON
{
    private Iterable<TestJSONClassFile> classFiles;

    /**
     * Set the TestJSONClassFile objects for a test.json file.
     * @param classFiles The TestJSONClassFile objects for a test.json file.
     * @return This object for method chaining.
     */
    public TestJSON setClassFiles(Iterable<TestJSONClassFile> classFiles)
    {
        PreCondition.assertNotNull(classFiles, "classFiles");

        this.classFiles = classFiles;
        return this;
    }

    /**
     * Get the TestJSONClassFile objects for a test.json file.
     * @return The TestJSONClassFile objects for a test.json file.
     */
    public Iterable<TestJSONClassFile> getClassFiles()
    {
        return this.classFiles;
    }

    /**
     * Write the contents of this TestJSON object to the provided File.
     * @param file The File to write this object to.
     * @return The result of writing this object to the provided File.
     */
    public Result<Void> write(File file)
    {
        PreCondition.assertNotNull(file, "file");

        return Result.create(() ->
        {
            try (final CharacterWriteStream writeStream = new BufferedByteWriteStream(file.getContentByteWriteStream().await()).asCharacterWriteStream())
            {
                this.write(writeStream).await();
            }
        });
    }

    /**
     * Write the contents of this TestJSON object to the provided writeStream.
     * @param writeStream The write stream to write this object to.
     * @return The result of writing this object to the provided write stream.
     */
    public Result<?> write(CharacterWriteStream writeStream)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotDisposed(writeStream, "writeStream.isDisposed()");

        return write(new JSONWriteStream(writeStream));
    }

    /**
     * Write the contents of this TestJSON object to the provided writeStream.
     * @param writeStream The write stream to write this object to.
     * @return The result of writing this object to the provided write stream.
     */
    public Result<?> write(JSONWriteStream writeStream)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotDisposed(writeStream, "writeStream.isDisposed()");

        return writeStream.writeObject(root ->
        {
            root.writeObjectProperty("classFiles", classFilesObject ->
            {
                final Iterable<TestJSONClassFile> classFiles = this.getClassFiles();
                if (!Iterable.isNullOrEmpty(classFiles))
                {
                    for (final TestJSONClassFile classFile : classFiles)
                    {
                        classFile.write(classFilesObject).await();
                    }
                }
            });
        });
    }
}
