package qub;

/**
 * The data of a test.json file.
 */
public class TestJSON
{
    private static final String classFilesPropertyName = "classFiles";

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

    @Override
    public boolean equals(Object rhs)
    {
        return rhs instanceof TestJSON && equals((TestJSON)rhs);
    }

    /**
     * Get whether or not this TestJSON object is equal to the provided TestJSON object.
     * @param rhs The TestJSON object to compare against this TestJSON object.
     * @return Whether or not this TestJSON object is equal to the provided TestJSON object.
     */
    public boolean equals(TestJSON rhs)
    {
        return rhs != null &&
            Comparer.equal(classFiles, rhs.classFiles);
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
            root.writeObjectProperty(classFilesPropertyName, classFilesObject ->
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

    /**
     * Parse a TestJSON object from the provided test.json file.
     * @param testJsonFile The test.json file to parse.
     * @return The parsed TestJSON object.
     */
    public static Result<TestJSON> parse(File testJsonFile)
    {
        PreCondition.assertNotNull(testJsonFile, "testJsonFile");

        return Result.create(() ->
        {
            TestJSON result;
            try (final ByteReadStream readStream = testJsonFile.getContentByteReadStream().await())
            {
                result = TestJSON.parse(readStream).await();
            }
            return result;
        });
    }

    public static Result<TestJSON> parse(ByteReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream.isDisposed()");

        return parse(readStream.asCharacterReadStream());
    }

    public static Result<TestJSON> parse(CharacterReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream.isDisposed()");

        return Result.create(() ->
        {
            final JSONDocument jsonDocument = JSON.parse(readStream);
            final JSONObject rootObject = jsonDocument.getRootObject().await();
            final JSONObject classFilesObject = rootObject.getObjectPropertyValue(classFilesPropertyName)
                .catchError()
                .await();
            final List<TestJSONClassFile> classFiles = List.create();
            if (classFilesObject != null)
            {
                for (final JSONProperty property : classFilesObject.getProperties())
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.parse(property)
                        .catchError()
                        .await();
                    if (classFile != null)
                    {
                        classFiles.add(classFile);
                    }
                }
            }
            return new TestJSON()
                .setClassFiles(classFiles);
        });
    }
}
