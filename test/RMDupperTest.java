
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import htsjdk.samtools.SAMRecord;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class RMDupperTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/test-resources/queueOrOutput_test_trigger_checkForDuplication_only_f.bam");
        universalSetup();
        inputSAMoutputSAMrecordBufferSetup();
  }

  @Test
  public void   queueOrOutput_nextAlignmentStartBeyondFirstEndTriggersCheckForDuplication () throws IOException {
      Iterator it = inputSam.iterator();
      while (it.hasNext()) {
          SAMRecord curr = (SAMRecord) it.next();
          RMDupper.queueOrOutput (outputSam, recordBuffer, curr);
      }
      RMDupper.flushQueue(outputSam, recordBuffer);
      inputSam.close();
      outputSam.close();
      copier.start();
      Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
      assertEquals(Data.RMDupperTest__queueOrOutput_nextAlignmentStartBeyondFirstEndTriggersCheckForDuplication_expectedReadNames, observedReadNames);
  }
}
