
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

public abstract class AbstractTest {
  protected InputStream in = null;
  protected ByteArrayOutputStream out = null;
  private PipedOutputStream outFromResult = null;
  protected PipedInputStream inFromResult = null;
  protected Thread copier;
  protected PrintStream console = null;

  @Before
  public abstract void setUp () throws IOException;

  protected void universalSetUp() throws IOException {
    if ( in == null ) { throw new RuntimeException("Input bam not found for test");}
    out = new ByteArrayOutputStream();
    inFromResult = new PipedInputStream();
    outFromResult = new PipedOutputStream(inFromResult);
    Runnable copyOutput = () -> { try { out.writeTo(outFromResult);outFromResult.close(); } catch (IOException ioe) { throw new RuntimeException(ioe);} };
    copier = new Thread(copyOutput);
    console = System.out;
  }
}
