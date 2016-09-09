package test.java;

import static org.junit.Assert.assertEquals;

import main.java.RMDupper;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PipedInputStream;
//import java.io.PipedOutputStream;
//import java.io.PrintStream;
import java.io.IOException;

public class ReverseTest extends AbstractTest {

  public void setUp () throws IOException {
        in = getClass().getResourceAsStream("/reverse_test.bam");
        allReadsAsMerged = Boolean.FALSE;
        universalSetup();
  }

  @Test
  public void resolveDuplicate_forward () throws IOException {
    RMDupper rmdup = new RMDupper(in, out, allReadsAsMerged);
    rmdup.readSAMFile();
    rmdup.finish();
    copier.start();
    Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
    assertEquals(Data.RMDupperTest__resolveDuplicate_reverse_expectedReadNames, observedReadNames);
  }
}
