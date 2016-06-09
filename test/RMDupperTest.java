
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;

import java.util.Set;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.IOException;


public class RMDupperTest {
    InputStream in = null;
    ByteArrayOutputStream out = null;
    PipedOutputStream outFromResult = null;
    PipedInputStream inFromResult = null;
    Thread copier;
    PrintStream console = null;

    @Before
    public void setUp() throws IOException {
      in = getClass().getResourceAsStream("/test-resources/yield_test.bam");
      out = new ByteArrayOutputStream();
      inFromResult = new PipedInputStream();
      outFromResult = new PipedOutputStream(inFromResult);
      Runnable copyOutput = () -> { try { out.writeTo(outFromResult);outFromResult.close(); } catch (IOException ioe) { throw new RuntimeException(ioe);} };
      copier = new Thread(copyOutput);
      console = System.out;
    }

    @Test
    public void resolveDuplicate_yields_best_quality () throws IOException {
        RMDupper rmdup = new RMDupper(in, out);
        rmdup.readSAMFile();
        rmdup.finish();
        copier.start();
        Set<String> observedReadNames = Utils.getReadNamesFromSAM(inFromResult).stream().collect(Collectors.toSet());
        observedReadNames.forEach(System.out::println);
        assertEquals(observedReadNames, Data.RMDupperTest__resolveDuplicate_yields_best_quality_expectedReadNames);
    }
}
