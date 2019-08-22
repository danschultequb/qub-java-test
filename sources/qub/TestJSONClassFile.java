package qub;

public class TestJSONClassFile
{
    private Path relativePath;
    private DateTime lastModified;

    /**
     * Set the path to the class file relative to the test.json file.
     * @param relativePath The path to the class file relative to the test.json file.
     * @return This object for method chaining.
     */
    public TestJSONClassFile setRelativePath(String relativePath)
    {
        PreCondition.assertNotNullAndNotEmpty(relativePath, "relativePath");

        return setRelativePath(Path.parse(relativePath));
    }

    /**
     * Set the path to the class file relative to the test.json file.
     * @param relativePath The path to the class file relative to the test.json file.
     * @return This object for method chaining.
     */
    public TestJSONClassFile setRelativePath(Path relativePath)
    {
        PreCondition.assertNotNull(relativePath, "relativePath");
        PreCondition.assertFalse(relativePath.isRooted(), "relativePath.isRooted()");

        this.relativePath = relativePath;

        return this;
    }

    /**
     * Get the path to the class file relative to the test.json file.
     * @return The path to the class file relative to the test.json file.
     */
    public Path getRelativePath()
    {
        return relativePath;
    }

    /**
     * Get the full class name of the class file.
     * @return The full class name of the class file.
     */
    public String getFullClassName()
    {
        PreCondition.assertNotNull(getRelativePath(), "getRelativePath()");

        final String result = QubTest.getFullClassName(getRelativePath());

        PostCondition.assertNotNullAndNotEmpty(result, "result");

        return result;
    }

    /**
     * Set the last time that the class file was modified.
     * @param lastModified The last time that the class file was modified.
     * @return This object for method chaining.
     */
    public TestJSONClassFile setLastModified(DateTime lastModified)
    {
        PreCondition.assertNotNull(lastModified, "lastModified");

        this.lastModified = lastModified;

        return this;
    }

    /**
     * Get the last time that the class file was modified.
     * @return The last time that the class file was modified.
     */
    public DateTime getLastModified()
    {
        return lastModified;
    }

    /**
     * Write the contents of this TestJSONClassFile object to the provided writeStream.
     * @param writeStream The write stream to write this object to.
     * @return The result of writing this object to the provided write stream.
     */
    public Result<?> write(JSONObjectWriteStream writeStream)
    {
        PreCondition.assertNotNull(writeStream, "writeStream");
        PreCondition.assertNotNull(getLastModified(), "getLastModified()");

        return Result.create(() ->
        {
            writeStream.writeObjectProperty(getRelativePath().toString(), classFileObject ->
            {
                classFileObject.writeNumberProperty("lastModified", getLastModified().getMillisecondsSinceEpoch());
            });
        });
    }
}
