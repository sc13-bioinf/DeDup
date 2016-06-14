
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ForwardTest.class,
	ReverseMergedTest.class,
	ReverseTest.class,
	YieldTest.class
})
public class DuplicateTestSuite {}
