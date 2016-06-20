
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

public class ForwardWithMergedTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/test-resources/forward_unmerged_duplicate_must_overlap.bam");
        universalSetup();
  }

  @Test
  public void resolveDuplicate_forward_with_merged () throws IOException {
    RMDupper rmdup = new RMDupper(in, out);
    rmdup.readSAMFile();
    rmdup.finish();
    copier.start();
    Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
    assertEquals(Data.RMDupperTest__resolveDuplicate_forward_with_merged_expectedReadNames, observedReadNames);
  }
}
