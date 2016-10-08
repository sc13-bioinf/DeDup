package test.java;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import htsjdk.samtools.SAMRecord;

import java.util.HashSet;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import main.java.RMDupper;
import main.java.datastructure.DupStats;
import main.java.datastructure.OccurenceCounterMerged;

public class RMDupperUnsortedTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/queueOrOutput_test_unsorted.bam");
        allReadsAsMerged = Boolean.FALSE;
        universalSetup();
        inputSAMoutputSAMrecordBufferSetup();
  }

  @Test
  public void   queueOrOutput_unsorted_read_one_forward__read_two_reverse () throws IOException {
      DupStats dupStats = new DupStats();
      OccurenceCounterMerged occurenceCounterMerged = new OccurenceCounterMerged();
      Set<String> discardSet = new HashSet<String>();
      Iterator it = inputSam.iterator();
      for (int i = 0; i < 2; i++) {
          SAMRecord curr = (SAMRecord) it.next();
          RMDupper.queueOrOutput (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, duplicateBuffer, discardSet, curr);
      }

      RMDupper.checkForDuplication (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, duplicateBuffer, discardSet);

      while (recordBuffer.size() > 0) {
        outputSam.addAlignment(recordBuffer.poll().right);
      }
      inputSam.close();
      outputSam.close();
      copier.start();
      Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
      assertEquals(Data.RMDupperTest__queueOrOutput_unsorted_read_one_forward__read_two_reverse_expectedReadNames, observedReadNames);
  }

  @Test
  public void   queueOrOutput_unsorted_read_one_reverse__read_two_forward () throws IOException {
      DupStats dupStats = new DupStats();
      OccurenceCounterMerged occurenceCounterMerged = new OccurenceCounterMerged();
      Set<String> discardSet = new HashSet<String>();
      Iterator it = inputSam.iterator();
      for (int skip = 0; skip < 2; skip++) { it.next(); }
      for (int i = 0; i < 2; i++) {
          SAMRecord curr = (SAMRecord) it.next();
          RMDupper.queueOrOutput (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, duplicateBuffer, discardSet, curr);
      }

      RMDupper.checkForDuplication (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, duplicateBuffer, discardSet);

      while (recordBuffer.size() > 0) {
        outputSam.addAlignment(recordBuffer.poll().right);
      }
      inputSam.close();
      outputSam.close();
      copier.start();
      Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
      assertEquals(Data.RMDupperTest__queueOrOutput_unsorted_read_one_reverse__read_two_forward_expectedReadNames, observedReadNames);
  }

  @Test
  public void   queueOrOutput_unsorted_read_two_forward__read_one_reverse () throws IOException {
      DupStats dupStats = new DupStats();
      OccurenceCounterMerged occurenceCounterMerged = new OccurenceCounterMerged();
      Set<String> discardSet = new HashSet<String>();
      Iterator it = inputSam.iterator();
      for (int skip = 0; skip < 4; skip++) { it.next(); }
      for (int i = 0; i < 2; i++) {
          SAMRecord curr = (SAMRecord) it.next();
          RMDupper.queueOrOutput (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, duplicateBuffer, discardSet, curr);
      }

      RMDupper.checkForDuplication (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, duplicateBuffer, discardSet);

      while (recordBuffer.size() > 0) {
        outputSam.addAlignment(recordBuffer.poll().right);
      }
      inputSam.close();
      outputSam.close();
      copier.start();
      Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
      assertEquals(Data.RMDupperTest__queueOrOutput_unsorted_read_two_forward__read_one_reverse_expectedReadNames, observedReadNames);
  }

  @Test
  public void   queueOrOutput_unsorted_read_two_reverse__read_one_forward () throws IOException {
      DupStats dupStats = new DupStats();
      OccurenceCounterMerged occurenceCounterMerged = new OccurenceCounterMerged();
      Set<String> discardSet = new HashSet<String>();
      Iterator it = inputSam.iterator();
      for (int skip = 0; skip < 6; skip++) { it.next(); }
      for (int i = 0; i < 2; i++) {
          SAMRecord curr = (SAMRecord) it.next();
          RMDupper.queueOrOutput (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, duplicateBuffer, discardSet, curr);
      }

      RMDupper.checkForDuplication (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, duplicateBuffer, discardSet);

      while (recordBuffer.size() > 0) {
        outputSam.addAlignment(recordBuffer.poll().right);
      }
      inputSam.close();
      outputSam.close();
      copier.start();
      Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
      assertEquals(Data.RMDupperTest__queueOrOutput_unsorted_read_two_reverse__read_one_forward_expectedReadNames, observedReadNames);
  }
}
