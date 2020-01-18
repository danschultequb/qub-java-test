package qub;

public interface TestJSONClassFileTests
{
    static void test(TestRunner runner)
    {
        PreCondition.assertNotNull(runner, "runner");

        runner.testGroup(TestJSONClassFile.class, () ->
        {
            runner.test("constructor()", (Test test) ->
            {
                final TestJSONClassFile classFile = new TestJSONClassFile();
                test.assertNull(classFile.getRelativePath());
                test.assertNull(classFile.getLastModified());
                test.assertEqual(0, classFile.getPassedTestCount());
                test.assertEqual(0, classFile.getSkippedTestCount());
                test.assertEqual(0, classFile.getFailedTestCount());
            });

            runner.testGroup("setRelativePath(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setRelativePath((String)null),
                        new PreConditionFailure("relativePath cannot be null."));
                    test.assertNull(classFile.getRelativePath());
                });

                runner.test("with empty", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setRelativePath(""),
                        new PreConditionFailure("relativePath cannot be empty."));
                    test.assertNull(classFile.getRelativePath());
                });

                runner.test("with rooted path", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setRelativePath("/hello.class"),
                        new PreConditionFailure("relativePath.isRooted() cannot be true."));
                    test.assertNull(classFile.getRelativePath());
                });

                runner.test("with relative path", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertSame(classFile, classFile.setRelativePath("hello.class"));
                    test.assertEqual(Path.parse("hello.class"), classFile.getRelativePath());
                });
            });

            runner.testGroup("setRelativePath(Path)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setRelativePath((Path)null),
                        new PreConditionFailure("relativePath cannot be null."));
                    test.assertNull(classFile.getRelativePath());
                });

                runner.test("with rooted path", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setRelativePath(Path.parse("/hello.class")),
                        new PreConditionFailure("relativePath.isRooted() cannot be true."));
                    test.assertNull(classFile.getRelativePath());
                });

                runner.test("with relative path", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertSame(classFile, classFile.setRelativePath(Path.parse("hello.class")));
                    test.assertEqual(Path.parse("hello.class"), classFile.getRelativePath());
                });
            });

            runner.testGroup("getFullClassName()", () ->
            {
                runner.test("with no relative path set", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(classFile::getFullClassName,
                        new PreConditionFailure("getRelativePath() cannot be null."));
                });

                final Action2<String,String> getFullClassNameTest = (String relativePath, String expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(relativePath), (Test test) ->
                    {
                        final TestJSONClassFile classFile = new TestJSONClassFile()
                            .setRelativePath(relativePath);
                        test.assertEqual(expected, classFile.getFullClassName());
                    });
                };

                getFullClassNameTest.run("hello.class", "hello");
                getFullClassNameTest.run("hello/there.class", "hello.there");
                getFullClassNameTest.run("a/b/c.class", "a.b.c");
            });

            runner.testGroup("setLastModified(DateTime)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setLastModified(null),
                        new PreConditionFailure("lastModified cannot be null."));
                    test.assertNull(classFile.getLastModified());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    final DateTime now = test.getClock().getCurrentDateTime();
                    test.assertSame(classFile, classFile.setLastModified(now));
                    test.assertEqual(now, classFile.getLastModified());
                });
            });

            runner.testGroup("setPassedTestCount(int)", () ->
            {
                runner.test("with -1", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setPassedTestCount(-1),
                        new PreConditionFailure("passedTestCount (-1) must be greater than or equal to 0."));
                    test.assertEqual(0, classFile.getPassedTestCount());
                });

                runner.test("with 0", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertSame(classFile, classFile.setPassedTestCount(0));
                    test.assertEqual(0, classFile.getPassedTestCount());
                });

                runner.test("with 1", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertSame(classFile, classFile.setPassedTestCount(1));
                    test.assertEqual(1, classFile.getPassedTestCount());
                });
            });

            runner.testGroup("setSkippedTestCount(int)", () ->
            {
                runner.test("with -1", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setSkippedTestCount(-1),
                        new PreConditionFailure("skippedTestCount (-1) must be greater than or equal to 0."));
                    test.assertEqual(0, classFile.getSkippedTestCount());
                });

                runner.test("with 0", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertSame(classFile, classFile.setSkippedTestCount(0));
                    test.assertEqual(0, classFile.getSkippedTestCount());
                });

                runner.test("with 1", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertSame(classFile, classFile.setSkippedTestCount(1));
                    test.assertEqual(1, classFile.getSkippedTestCount());
                });
            });

            runner.testGroup("setFailedTestCount(int)", () ->
            {
                runner.test("with -1", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertThrows(() -> classFile.setFailedTestCount(-1),
                        new PreConditionFailure("failedTestCount (-1) must be greater than or equal to 0."));
                    test.assertEqual(0, classFile.getFailedTestCount());
                });

                runner.test("with 0", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertSame(classFile, classFile.setFailedTestCount(0));
                    test.assertEqual(0, classFile.getFailedTestCount());
                });

                runner.test("with 1", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertSame(classFile, classFile.setFailedTestCount(1));
                    test.assertEqual(1, classFile.getFailedTestCount());
                });
            });

            runner.testGroup("toString()", () ->
            {
                runner.test("with no properties set", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile();
                    test.assertEqual("\"null\":{\"lastModified\":null,\"passedTestCount\":0,\"skippedTestCount\":0,\"failedTestCount\":0}", classFile.toString());
                });

                runner.test("with all properties set", (Test test) ->
                {
                    final TestJSONClassFile classFile = new TestJSONClassFile()
                        .setRelativePath("a/b/c.class")
                        .setLastModified(DateTime.create(2000, 10, 5))
                        .setPassedTestCount(10)
                        .setSkippedTestCount(20)
                        .setFailedTestCount(30);
                    test.assertEqual("\"a/b/c.class\":{\"lastModified\":\"2000-10-05T00:00Z\",\"passedTestCount\":10,\"skippedTestCount\":20,\"failedTestCount\":30}", classFile.toString());
                });
            });
        });
    }
}
