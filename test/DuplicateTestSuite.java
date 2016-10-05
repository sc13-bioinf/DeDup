
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	AllReadsAsMergedTest.class,
	ForwardTest.class,
	ForwardWithMergedTest.class,
	ReverseTest.class,
	ReverseWithMergedTest.class,
	SingleTest.class,
	StackTest.class,
	StackReadOneTest.class,
	StrandForwardTest.class,
	StrandReverseTest.class,
	YieldTest.class
})
public class DuplicateTestSuite {}
