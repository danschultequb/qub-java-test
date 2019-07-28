package qub;

public interface FakeJavaRunnerTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(FakeJavaRunner.class, () ->
        {
            JavaRunnerTests.test(runner, FakeJavaRunner::new);
        });
    }
}
