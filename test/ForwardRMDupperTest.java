
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import htsjdk.samtools.SAMRecord;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class ForwardRMDupperTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/test-resources/queueOrOutput_test_trigger_checkForDuplication_only_f.bam");
        universalSetup();
        inputSAMoutputSAMrecordBufferSetup();
  }

  @Test
  public void   queueOrOutput_nextAlignmentStartBeyondFirstEndTriggersCheckForDuplication_pre () throws IOException {
      Iterator it = inputSam.iterator();
      for (int i = 0; i < 3; i++) {
          SAMRecord curr = (SAMRecord) it.next();
          RMDupper.queueOrOutput (outputSam, recordBuffer, curr);
      }
      while (recordBuffer.size() > 0) {
        outputSam.addAlignment(recordBuffer.poll().right);
      }
      inputSam.close();
      outputSam.close();
      copier.start();
      Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
      assertEquals(Data.RMDupperTest__queueOrOutput_nextAlignmentStartBeyondFirstEndTriggersCheckForDuplication_pre_expectedReadNames, observedReadNames);
  }

  @Test
  public void   queueOrOutput_nextAlignmentStartBeyondFirstEndTriggersCheckForDuplication_post () throws IOException {
      Iterator it = inputSam.iterator();
      while (it.hasNext()) {
          SAMRecord curr = (SAMRecord) it.next();
          RMDupper.queueOrOutput (outputSam, recordBuffer, curr);
      }
      while (recordBuffer.size() > 0) {
        outputSam.addAlignment(recordBuffer.poll().right);
      }
      inputSam.close();
      outputSam.close();
      copier.start();
      Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
      assertEquals(Data.RMDupperTest__queueOrOutput_nextAlignmentStartBeyondFirstEndTriggersCheckForDuplication_post_expectedReadNames, observedReadNames);
  }
}
