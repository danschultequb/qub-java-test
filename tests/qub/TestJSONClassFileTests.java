package qub;

public interface TestJSONClassFileTests
{
    static void test(TestRunner runner)
    {
        PreCondition.assertNotNull(runner, "runner");

        runner.testGroup(TestJSONClassFile.class, () ->
        {
            runner.testGroup("create(String)", () ->
            {
                final Action2<String,Throwable> createErrorTest = (String classFileRelativePath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(classFileRelativePath), (Test test) ->
                    {
                        test.assertThrows(() -> TestJSONClassFile.create(classFileRelativePath),
                            expected);
                    });
                };

                createErrorTest.run(null, new PreConditionFailure("classFileRelativePath cannot be null."));
                createErrorTest.run("", new PreConditionFailure("classFileRelativePath cannot be empty."));
                createErrorTest.run("/file.class", new PreConditionFailure("classFileRelativePath.isRooted() cannot be true."));

                final Action1<String> createTest = (String classFileRelativePath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(classFileRelativePath), (Test test) ->
                    {
                        final TestJSONClassFile classFile = TestJSONClassFile.create(classFileRelativePath);
                        test.assertNotNull(classFile);
                        test.assertEqual(classFileRelativePath, classFile.getRelativePath().toString());
                        test.assertNull(classFile.getLastModified());
                        test.assertEqual(0, classFile.getPassedTestCount());
                        test.assertEqual(0, classFile.getSkippedTestCount());
                        test.assertEqual(0, classFile.getFailedTestCount());
                    });
                };

                createTest.run("hello");
                createTest.run("hello.class");
                createTest.run("qub/QubTestRunTests.class");
            });

            runner.testGroup("getFullClassName()", () ->
            {
                final Action2<String,String> getFullClassNameTest = (String relativePath, String expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(relativePath), (Test test) ->
                    {
                        final TestJSONClassFile classFile = TestJSONClassFile.create(relativePath);
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
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertThrows(() -> classFile.setLastModified(null),
                        new PreConditionFailure("lastModified cannot be null."));
                    test.assertNull(classFile.getLastModified());
                });

                runner.test("with non-null", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    final DateTime now = test.getClock().getCurrentDateTime();
                    test.assertSame(classFile, classFile.setLastModified(now));
                    test.assertEqual(now, classFile.getLastModified());
                });
            });

            runner.testGroup("setPassedTestCount(int)", () ->
            {
                runner.test("with -1", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertThrows(() -> classFile.setPassedTestCount(-1),
                        new PreConditionFailure("passedTestCount (-1) must be greater than or equal to 0."));
                    test.assertEqual(0, classFile.getPassedTestCount());
                });

                runner.test("with 0", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertSame(classFile, classFile.setPassedTestCount(0));
                    test.assertEqual(0, classFile.getPassedTestCount());
                });

                runner.test("with 1", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertSame(classFile, classFile.setPassedTestCount(1));
                    test.assertEqual(1, classFile.getPassedTestCount());
                });
            });

            runner.testGroup("setSkippedTestCount(int)", () ->
            {
                runner.test("with -1", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertThrows(() -> classFile.setSkippedTestCount(-1),
                        new PreConditionFailure("skippedTestCount (-1) must be greater than or equal to 0."));
                    test.assertEqual(0, classFile.getSkippedTestCount());
                });

                runner.test("with 0", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertSame(classFile, classFile.setSkippedTestCount(0));
                    test.assertEqual(0, classFile.getSkippedTestCount());
                });

                runner.test("with 1", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertSame(classFile, classFile.setSkippedTestCount(1));
                    test.assertEqual(1, classFile.getSkippedTestCount());
                });
            });

            runner.testGroup("setFailedTestCount(int)", () ->
            {
                runner.test("with -1", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertThrows(() -> classFile.setFailedTestCount(-1),
                        new PreConditionFailure("failedTestCount (-1) must be greater than or equal to 0."));
                    test.assertEqual(0, classFile.getFailedTestCount());
                });

                runner.test("with 0", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertSame(classFile, classFile.setFailedTestCount(0));
                    test.assertEqual(0, classFile.getFailedTestCount());
                });

                runner.test("with 1", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertSame(classFile, classFile.setFailedTestCount(1));
                    test.assertEqual(1, classFile.getFailedTestCount());
                });
            });

            runner.testGroup("toString()", () ->
            {
                runner.test("with no properties set", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class");
                    test.assertEqual("\"hello.class\":{}", classFile.toString());
                });

                runner.test("with all properties set", (Test test) ->
                {
                    final TestJSONClassFile classFile = TestJSONClassFile.create("hello.class")
                        .setLastModified(DateTime.create(2000, 10, 5))
                        .setPassedTestCount(10)
                        .setSkippedTestCount(20)
                        .setFailedTestCount(30);
                    test.assertEqual("\"hello.class\":{\"lastModified\":\"2000-10-05T00:00Z\",\"passedTestCount\":10,\"skippedTestCount\":20,\"failedTestCount\":30}", classFile.toString());
                });
            });
        });
    }
}
