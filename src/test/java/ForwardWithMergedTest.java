package test.java;

import static org.junit.Assert.assertEquals;

import main.java.RMDupper;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

public class ForwardWithMergedTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/forward_unmerged_duplicate_must_overlap.bam");
        allReadsAsMerged = Boolean.FALSE;
        universalSetup();
  }

  @Test
  public void resolveDuplicate_forward_with_merged () throws IOException {
    RMDupper rmdup = new RMDupper(in, out, allReadsAsMerged);
    rmdup.readSAMFile();
    rmdup.finish();
    copier.start();
    Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
    assertEquals(Data.RMDupperTest__resolveDuplicate_forward_with_merged_expectedReadNames, observedReadNames);
  }
}
