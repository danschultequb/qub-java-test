package qub;

/**
 * The saved result of running the tests for a Java class file.
 */
public class TestJSONClassFile
{
    private static final String lastModifiedPropertyName = "lastModified";
    private static final String passedTestCountPropertyName = "passedTestCount";
    private static final String skippedTestCountPropertyName = "skippedTestCount";
    private static final String failedTestCountPropertyName = "failedTestCount";

    private Path relativePath;
    private DateTime lastModified;
    private int passedTestCount;
    private int skippedTestCount;
    private int failedTestCount;

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

    public TestJSONClassFile setPassedTestCount(int passedTestCount)
    {
        PreCondition.assertGreaterThanOrEqualTo(passedTestCount, 0, "passedTestCount");

        this.passedTestCount = passedTestCount;
        return this;
    }

    public int getPassedTestCount()
    {
        return passedTestCount;
    }

    public TestJSONClassFile setSkippedTestCount(int skippedTestCount)
    {
        PreCondition.assertGreaterThanOrEqualTo(skippedTestCount, 0, "skippedTestCount");

        this.skippedTestCount = skippedTestCount;
        return this;
    }

    public int getSkippedTestCount()
    {
        return skippedTestCount;
    }

    public TestJSONClassFile setFailedTestCount(int failedTestCount)
    {
        PreCondition.assertGreaterThanOrEqualTo(failedTestCount, 0, "failedTestCount");

        this.failedTestCount = failedTestCount;
        return this;
    }

    public int getFailedTestCount()
    {
        return failedTestCount;
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

    public void toJson(JSONObjectBuilder testJson)
    {
        PreCondition.assertNotNull(testJson, "testJson");

        testJson.objectProperty(Objects.toString(this.getRelativePath()), classFileJson ->
        {
            classFileJson.stringOrNullProperty(TestJSONClassFile.lastModifiedPropertyName, this.lastModified == null ? null : this.lastModified.toString());
            classFileJson.numberProperty(TestJSONClassFile.passedTestCountPropertyName, this.passedTestCount);
            classFileJson.numberProperty(TestJSONClassFile.skippedTestCountPropertyName, this.skippedTestCount);
            classFileJson.numberProperty(TestJSONClassFile.failedTestCountPropertyName, this.failedTestCount);
        });
    }

    public static Result<TestJSONClassFile> parse(JSONProperty property)
    {
        PreCondition.assertNotNull(property, "property");

        return Result.create(() ->
        {
            final JSONObject propertyValue = property.getObjectValue().await();
            final TestJSONClassFile classFile = new TestJSONClassFile()
                .setRelativePath(property.getName());
            propertyValue.getStringPropertyValue(lastModifiedPropertyName)
                .then((String lastModified) -> DateTime.parse(lastModified).await())
                .then(classFile::setLastModified)
                .catchError()
                .await();
            propertyValue.getNumberPropertyValue(passedTestCountPropertyName)
                .then((Double passedTestCount) -> classFile.setPassedTestCount(passedTestCount.intValue()))
                .catchError()
                .await();
            propertyValue.getNumberPropertyValue(skippedTestCountPropertyName)
                .then((Double skippedTestCount) -> classFile.setSkippedTestCount(skippedTestCount.intValue()))
                .catchError()
                .await();
            propertyValue.getNumberPropertyValue(failedTestCountPropertyName)
                .then((Double failedTestCount) -> classFile.setFailedTestCount(failedTestCount.intValue()))
                .catchError()
                .await();
            return classFile;
        });
    }
}
