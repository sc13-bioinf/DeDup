import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

public class ReverseMergedTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/test-resources/reverse_merged_test.bam");
        universalSetUp();
  }

  @Test
  public void resolveDuplicate_reverse_merged () throws IOException {
    RMDupper rmdup = new RMDupper(in, out);
    rmdup.readSAMFile();
    rmdup.finish();
    copier.start();
    Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
    assertEquals(observedReadNames, Data.RMDupperTest__resolveDuplicate_reverse_merged_expectedReadNames);
  }
}
