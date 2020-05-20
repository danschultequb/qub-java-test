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

    @Override
    public String toString()
    {
        return this.toString(JSONFormat.consise);
    }

    public String toString(JSONFormat format)
    {
        PreCondition.assertNotNull(format, "format");

        return this.toJson().toString(format);
    }

    public JSONObject toJson()
    {
        return JSONObject.create()
            .set(TestJSON.classFilesPropertyName, JSONObject.create()
                .setAll(this.classFiles.map(TestJSONClassFile::toJsonProperty)));
    }

    /**
     * Parse a TestJSON object from the provided test.json file.
     * @param testJsonFile The test.json file to parse.
     * @return The parsed TestJSON object.
     */
    public static Result<TestJSON> parse(File testJsonFile)
    {
        PreCondition.assertNotNull(testJsonFile, "testJsonFile");

        return JSON.parseObject(testJsonFile)
            .then((JSONObject json) -> TestJSON.parse(json).await());
    }

    public static Result<TestJSON> parse(ByteReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream.isDisposed()");

        return JSON.parseObject(readStream)
            .then((JSONObject json) -> TestJSON.parse(json).await());
    }

    public static Result<TestJSON> parse(CharacterReadStream readStream)
    {
        PreCondition.assertNotNull(readStream, "readStream");
        PreCondition.assertNotDisposed(readStream, "readStream.isDisposed()");

        return JSON.parseObject(readStream)
            .then((JSONObject json) -> TestJSON.parse(json).await());
    }

    public static Result<TestJSON> parse(JSONObject rootObject)
    {
        PreCondition.assertNotNull(rootObject, "rootObject");

        return Result.create(() ->
        {
            final JSONObject classFilesObject = rootObject.getObject(classFilesPropertyName)
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
