package test.java;

import static org.junit.Assert.assertEquals;

import main.java.RMDupper;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

public class AllReadsAsMergedTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/all_reads_as_merged_test.bam");
        allReadsAsMerged = Boolean.TRUE;
        unsorted = Boolean.FALSE;
        universalSetup();
  }

  @Test
  public void resolveDuplicate_forward_with_merged () throws IOException {
    RMDupper rmdup = new RMDupper(in, out, allReadsAsMerged, unsorted);
    rmdup.readSAMFile();
    rmdup.finish();
    copier.start();
    Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
    assertEquals(Data.RMDupperTest_allReadsAsMerged_expectedReadNames, observedReadNames);
  }
}
