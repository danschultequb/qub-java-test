package qub;

public interface JavaProcessBuilderTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(JavaProcessBuilder.class, () ->
        {
            runner.testGroup("get(Process)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProcessBuilder.get((Process)null),
                        new PreConditionFailure("process cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final JavaProcessBuilder java = JavaProcessBuilder.get(test.getProcess()).await();
                    test.assertNotNull(java);
                    test.assertEqual("java", java.getExecutablePath().toString());
                    test.assertEqual(Iterable.create(), java.getArguments());
                    test.assertEqual(test.getProcess().getCurrentFolderPath(), java.getWorkingFolderPath());
                });
            });

            runner.testGroup("get(ProcessFactory)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> JavaProcessBuilder.get((ProcessFactory)null),
                        new PreConditionFailure("processFactory cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final JavaProcessBuilder java = JavaProcessBuilder.get(test.getProcess().getProcessFactory()).await();
                    test.assertNotNull(java);
                    test.assertEqual("java", java.getExecutablePath().toString());
                    test.assertEqual(Iterable.create(), java.getArguments());
                    test.assertEqual(test.getProcess().getCurrentFolderPath(), java.getWorkingFolderPath());
                });
            });

            runner.testGroup("addClasspath(String)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaProcessBuilder java = JavaProcessBuilder.get(test.getProcess()).await();
                    test.assertThrows(() -> java.addClasspath((String)null),
                        new PreConditionFailure("classpath cannot be null."));
                    test.assertEqual(Iterable.create(), java.getArguments());
                });

                runner.test("with empty", (Test test) ->
                {
                    final JavaProcessBuilder java = JavaProcessBuilder.get(test.getProcess()).await();
                    test.assertThrows(() -> java.addClasspath(""),
                        new PreConditionFailure("classpath cannot be empty."));
                    test.assertEqual(Iterable.create(), java.getArguments());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final JavaProcessBuilder java = JavaProcessBuilder.get(test.getProcess()).await();
                    test.<JavaProcessBuilder>assertSame(java, java.addClasspath("hello"));
                    test.assertEqual(Iterable.create("-classpath", "hello"), java.getArguments());
                });
            });

            runner.testGroup("addJavaAgent()", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final JavaProcessBuilder java = JavaProcessBuilder.get(test.getProcess()).await();
                    test.assertThrows(() -> java.addJavaAgent(null),
                        new PreConditionFailure("javaAgent cannot be null."));
                    test.assertEqual(Iterable.create(), java.getArguments());
                });

                runner.test("with empty", (Test test) ->
                {
                    final JavaProcessBuilder java = JavaProcessBuilder.get(test.getProcess()).await();
                    test.assertThrows(() -> java.addJavaAgent(""),
                        new PreConditionFailure("javaAgent cannot be empty."));
                    test.assertEqual(Iterable.create(), java.getArguments());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final JavaProcessBuilder java = JavaProcessBuilder.get(test.getProcess()).await();
                    test.<JavaProcessBuilder>assertSame(java, java.addJavaAgent("hello"));
                    test.assertEqual(Iterable.create("-javaagent:hello"), java.getArguments());
                });
            });
        });
    }
}
