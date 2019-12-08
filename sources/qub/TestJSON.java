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
        return this.toJson().toString();
    }

    public JSONObject toJson()
    {
        return JSON.object(this::toJson);
    }

    public void toJson(JSONObjectBuilder json)
    {
        PreCondition.assertNotNull(json, "json");

        json.objectProperty(TestJSON.classFilesPropertyName, classFilesJson ->
        {
            if (!Iterable.isNullOrEmpty(this.classFiles))
            {
                for (final TestJSONClassFile classFile : this.classFiles)
                {
                    classFile.toJson(json);
                }
            }
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
