package qub;

public interface RealJavaRunnerTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(RealJavaRunner.class, () ->
        {
            JavaRunnerTests.test(runner, RealJavaRunner::new);
        });
    }
}
