package test.java;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

import main.java.RMDupper;

public class SingleTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/single_read.bam");
        allReadsAsMerged = Boolean.FALSE;
        universalSetup();
  }

  @Test
  public void resolveDuplicate_strand_forward () throws IOException {
    RMDupper rmdup = new RMDupper(in, out, allReadsAsMerged);
    rmdup.readSAMFile();
    rmdup.finish();
    copier.start();
    Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
    assertEquals(Data.RMDupperTest__single_expectedReadNames, observedReadNames);
  }
}
