package test.java;


import org.junit.Test;
import org.junit.Before;

import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamInputResource;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator;
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
  protected Thread copier = null;
  protected PrintStream console = null;

  protected SamReader inputSam;
  protected SAMFileWriter outputSam;
  protected PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer;
  protected PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>> duplicateBuffer;
  protected Boolean allReadsAsMerged;

  @Before
  public abstract void setUp () throws IOException;

  protected void universalSetup() throws IOException {
    if ( in == null ) { throw new RuntimeException("Input bam not found for test");}
    out = new ByteArrayOutputStream();
    inFromResult = new PipedInputStream();
    outFromResult = new PipedOutputStream(inFromResult);
    Runnable copyOutput = () -> { try { out.writeTo(outFromResult);outFromResult.close(); } catch (IOException ioe) { throw new RuntimeException(ioe);} };
    copier = new Thread(copyOutput);
    console = System.out;
  }

  protected void inputSAMoutputSAMrecordBufferSetup() {
    inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).open(SamInputResource.of(in));
    outputSam = new SAMFileWriterFactory().makeSAMWriter(inputSam.getFileHeader(), false, out);

    Comparator<SAMRecord> samRecordComparatorForRecordBuffer = new SAMRecordPositionAndQualityComparator();
    recordBuffer = new PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>>(1000, Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getRight, samRecordComparatorForRecordBuffer));

    Comparator<SAMRecord> samRecordComparatorForDuplicateBuffer;

    if ( this.allReadsAsMerged ) {
      samRecordComparatorForDuplicateBuffer = new SAMRecordQualityComparator();
    } else {
      samRecordComparatorForDuplicateBuffer = new SAMRecordQualityComparatorPreferMerged();
    }

    duplicateBuffer = new PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>>(1000, Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getRight, samRecordComparatorForDuplicateBuffer.reversed()));
  }
}
