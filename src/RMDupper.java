/*
 * Copyright (c) 2016. DeDup Alexander Peltzer
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.google.common.io.Files;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import htsjdk.samtools.*;
import java.util.*;
import java.io.File;

import datastructure.DedupStore;
import datastructure.OccurenceCounterMerged;
import datastructure.OccurenceCounterSingle;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutableTriple;

/**
 * DeDup Tool for Duplicate Removal of short read duplicates in BAM/SAM Files.
 *
 * @author Alexander Peltzer
 * @version 0.9.10
 * @Date: 09/17/15
 */
public class RMDupper{
    private static final String CLASS_NAME = "dedup";
    private static final String VERSION = "0.10.0";

    private final SamReader inputSam;
    private final SAMFileWriter outputSam;
    private int total = 0;
    private int removed_reverse = 0;
    private int removed_forward = 0;
    private int removed_merged = 0;
    private OccurenceCounterMerged oc = new OccurenceCounterMerged();
    private OccurenceCounterSingle ocunmerged = new OccurenceCounterSingle();




    public RMDupper(File inputFile, File outputFile) {
        inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).open(inputFile);
        outputSam = new SAMFileWriterFactory().makeSAMOrBAMWriter(inputSam.getFileHeader(), false, outputFile);
    }


    public RMDupper(InputStream in, OutputStream out) {
        inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).open(SamInputResource.of(in));
        outputSam = new SAMFileWriterFactory().makeSAMWriter(inputSam.getFileHeader(), false, out);
    }


    public static void main(String[] args) throws IOException {
        System.out.println("DeDup v" + VERSION);
        // the command line parameters
        Options helpOptions = new Options();
        helpOptions.addOption("h", "help", false, "show this help page");
        Options options = new Options();
        options.addOption("h", "help", false, "show this help page");
        options.addOption("i", "input", true, "the input file if this option is not specified,\nthe input is expected to be piped in");
        options.addOption("o", "output", true, "the output folder. Has to be specified if input is set.");
        HelpFormatter helpformatter = new HelpFormatter();
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(helpOptions, args);
            if (cmd.hasOption('h')) {
                helpformatter.printHelp(CLASS_NAME, options);
                System.exit(0);
            }
        } catch (ParseException e1) {
        }

        boolean pipe = true;
        String input = "";
        String outputpath = "";
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption('i')) {
                input = cmd.getOptionValue('i');
                pipe = false;
            }
            if (cmd.hasOption('o')) {
                outputpath = cmd.getOptionValue('o');
            }
        } catch (ParseException e) {
            helpformatter.printHelp(CLASS_NAME, options);
            System.err.println(e.getMessage());
            System.exit(0);
        }
        DecimalFormat df = new DecimalFormat("##.##");

        if (pipe) {
            RMDupper rmdup = new RMDupper(System.in, System.out);
            rmdup.readSAMFile();
            System.out.println("Total reads: " + rmdup.total + "\n");
            System.out.println("Reverse removed: " + rmdup.removed_reverse + "\n");
            System.out.println("Forward removed: " + rmdup.removed_forward + "\n");
            System.out.println("Merged removed: " + rmdup.removed_merged + "\n");
            System.out.println("Total removed: " + (rmdup.removed_forward + rmdup.removed_merged
                    + rmdup.removed_reverse) + "\n");
            if (rmdup.removed_merged + rmdup.removed_forward + rmdup.removed_reverse == 0) {
                System.out.println("Duplication Rate: " + df.format(0.00));
            } else {
                System.out.println("Duplication Rate: " + df.format((double) (rmdup.removed_merged + rmdup.removed_reverse + rmdup.removed_forward) / (double) rmdup.total));
            }
        } else {
            if (outputpath.length() == 0) {
                System.err.println("The output folder has to be specified");
                helpformatter.printHelp(CLASS_NAME, options);
                System.exit(0);
            }

            File inputFile = new File(input);
            File outputFile = new File(outputpath + "/" + Files.getNameWithoutExtension(inputFile.getAbsolutePath()) + "_rmdup.bam");
            File outputlog = new File(outputpath + "/" + Files.getNameWithoutExtension(inputFile.getAbsolutePath()) + ".log");
            File outputhist = new File(outputpath + "/" + Files.getNameWithoutExtension(inputFile.getAbsolutePath()) + ".hist");






            try {
                FileWriter fw = new FileWriter(outputlog);
                FileWriter histfw = new FileWriter(outputhist);
                BufferedWriter bfw = new BufferedWriter(fw);
                BufferedWriter histbfw = new BufferedWriter(histfw);

                RMDupper rmdup = new RMDupper(inputFile, outputFile);
                rmdup.readSAMFile();
                rmdup.inputSam.close();
                rmdup.outputSam.close();

                bfw.write("Total reads: " + rmdup.total + "\n");
                bfw.write("Reverse removed: " + rmdup.removed_reverse + "\n");
                bfw.write("Forward removed: " + rmdup.removed_forward + "\n");
                bfw.write("Merged removed: " + rmdup.removed_merged + "\n");
                bfw.write("Total removed: " + (rmdup.removed_forward + rmdup.removed_merged
                    + rmdup.removed_reverse) + "\n");
                bfw.write("Duplication Rate: " + df.format((double) (rmdup.removed_merged + rmdup.removed_reverse + rmdup.removed_forward) / (double) rmdup.total));
                bfw.flush();
                bfw.close();

                histbfw.write(rmdup.oc.getHistogram());
                histbfw.flush();
                histbfw.close();


                System.out.println("Total reads: " + rmdup.total + "\n");
                System.out.println("Unmerged removed: " + rmdup.removed_forward + "\n");
                System.out.println("Merged removed: " + rmdup.removed_merged + "\n");
                System.out.println("Total removed: " + (rmdup.removed_forward + rmdup.removed_merged
                    + rmdup.removed_reverse) + "\n");
                if (rmdup.removed_merged + rmdup.removed_forward + rmdup.removed_reverse == 0) {
                    System.out.println("Duplication Rate: " + df.format(0.00));
                } else {
                    System.out.println("Duplication Rate: " + df.format((double) (rmdup.removed_merged + rmdup.removed_reverse + rmdup.removed_forward) / (double) rmdup.total));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This Method reads a SAM File and parses the input
     * Currently, only merged Reads with the "M" Flag in front are checked for Duplicates.
     * R/F Flags are simply written into output File, also other "non-flagged" ones.
     */
    public void readSAMFile() {
        ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer = new ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>>(1000);
        Set<String> discardSet = new HashSet<String>(1000);
        String referenceName = SAMRecord.NO_ALIGNMENT_REFERENCE_NAME;
        Iterator it = inputSam.iterator();
        while (it.hasNext()) {
            SAMRecord curr = (SAMRecord) it.next();
            if ( curr.getReferenceName() == SAMRecord.NO_ALIGNMENT_REFERENCE_NAME ) {
                this.outputSam.addAlignment(curr);
            } else {
                if ( referenceName == curr.getReferenceName() ) {
                    queueOrOutput (this.outputSam, recordBuffer, discardSet, curr);
                } else {
                    flushQueue (this.outputSam, recordBuffer, discardSet);
                    queueOrOutput (this.outputSam, recordBuffer, discardSet, curr);
                    referenceName = curr.getReferenceName();
                }
            }

            total++;
            if(total % 100000 == 0){
                System.err.println("Reads treated: " + total);
            }
        }
        flushQueue(this.outputSam, recordBuffer, discardSet);
    }

    public static void queueOrOutput (SAMFileWriter outputSam, ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer, Set<String> discardSet, SAMRecord curr) {
        //Don't do anything with unmapped reads, just write them into the output!
        if (curr.getReadUnmappedFlag() || curr.getMappingQuality() == 0) {
          outputSam.addAlignment(curr);
        } else {
            if ( recordBuffer.size() > 0 && recordBuffer.peekFirst().middle < curr.getAlignmentStart() ) {
                checkForDuplication(outputSam, recordBuffer, discardSet);
            }
            recordBuffer.add (new ImmutableTriple<Integer, Integer, SAMRecord>(curr.getAlignmentStart(), curr.getAlignmentEnd(), curr));
        }
    }

    public static void flushQueue (SAMFileWriter outputSam, ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer, Set<String> discardSet) {
        while ( !recordBuffer.isEmpty() ) {
            checkForDuplication (outputSam, recordBuffer, discardSet);
        }
        discardSet.clear();
    }

    public static void checkForDuplication (SAMFileWriter outputSam, ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer, Set<String> discardSet) {
        // At this point recordBuffer contains all alignments that overlap with its first entry
        // Therefore the task here is to de-duplicate for the first entry in recordBuffer

        PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>> duplicateBuffer = new PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>>(1000, Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getRight, new SAMRecordQualityComparator().reversed()));
        Iterator<ImmutableTriple<Integer, Integer, SAMRecord>> it = recordBuffer.iterator();
        while (it.hasNext()) {
          ImmutableTriple<Integer, Integer, SAMRecord> maybeDuplicate = it.next();
          boolean duplicateIsShorterOrEqual = maybeDuplicate.middle - maybeDuplicate.left <= recordBuffer.peekFirst().middle - recordBuffer.peekFirst().left;
          boolean duplicateIsLongerOrEqual = recordBuffer.peekFirst().middle - recordBuffer.peekFirst().left <= maybeDuplicate.middle - maybeDuplicate.left;

          if ( recordBuffer.peekFirst().right.getReadName().startsWith("M_") &&
               ( ( maybeDuplicate.right.getReadName().startsWith("M_") &&
                   recordBuffer.peekFirst().left.equals(maybeDuplicate.left)  &&
                   recordBuffer.peekFirst().middle.equals(maybeDuplicate.middle) ) ||
                 ( maybeDuplicate.right.getReadName().startsWith("F_") &&
                   recordBuffer.peekFirst().left.equals(maybeDuplicate.left) &&
                   duplicateIsShorterOrEqual ) ||
                 ( maybeDuplicate.right.getReadName().startsWith("R_") &&
                   recordBuffer.peekFirst().middle.equals(maybeDuplicate.middle) &&
                   duplicateIsShorterOrEqual ) ) ) {
                   //System.out.println("M_ add");
               duplicateBuffer.add(maybeDuplicate);
          } else if ( recordBuffer.peekFirst().right.getReadName().startsWith("F_") &&
                      ( ( maybeDuplicate.right.getReadName().startsWith("M_") &&
                          recordBuffer.peekFirst().left.equals(maybeDuplicate.left) &&
                          duplicateIsLongerOrEqual ) ||
                        ( maybeDuplicate.right.getReadName().startsWith("F_") &&
                          recordBuffer.peekFirst().left.equals(maybeDuplicate.left) ) ||
                        ( maybeDuplicate.right.getReadName().startsWith("R_") &&
                          recordBuffer.peekFirst().middle.equals(maybeDuplicate.middle) &&
                          duplicateIsShorterOrEqual ) ) ) {
                      //System.out.println("F_ add");
             duplicateBuffer.add(maybeDuplicate);
          } else if ( recordBuffer.peekFirst().right.getReadName().startsWith("R_") &&
                      ( ( maybeDuplicate.right.getReadName().startsWith("M_") &&
                          recordBuffer.peekFirst().middle.equals(maybeDuplicate.middle) &&
                          duplicateIsLongerOrEqual ) ||
                        ( maybeDuplicate.right.getReadName().startsWith("F_") &&
                          recordBuffer.peekFirst().left.equals(maybeDuplicate.left) &&
                          recordBuffer.peekFirst().middle.equals(maybeDuplicate.middle) ) ||
                        ( maybeDuplicate.right.getReadName().startsWith("R_") &&
                          recordBuffer.peekFirst().middle.equals(maybeDuplicate.middle) ) ) ) {
            //System.out.println("R_ add");
             duplicateBuffer.add(maybeDuplicate);
          }
        }
        /* DEBUG
System.out.println ("duplicateBuffer");
ArrayList<ImmutableTriple<Integer, Integer, SAMRecord>> sortedDuplicateBuffer = new ArrayList<ImmutableTriple<Integer, Integer, SAMRecord>>(duplicateBuffer.size());
Iterator<ImmutableTriple<Integer, Integer, SAMRecord>> dit = duplicateBuffer.iterator();
while (dit.hasNext()) {
    sortedDuplicateBuffer.add(dit.next());
}
sortedDuplicateBuffer.sort(Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getMiddle));

for ( ImmutableTriple<Integer, Integer, SAMRecord> currTriple : sortedDuplicateBuffer ) {
    System.out.println("dbe: "+currTriple+" "+SAMRecordQualityComparator.getQualityScore(currTriple.right.getBaseQualityString()));
}

// Sort again with priority queue order
sortedDuplicateBuffer.sort(Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getRight, new SAMRecordQualityComparator().reversed()));
for ( ImmutableTriple<Integer, Integer, SAMRecord> currTriple : sortedDuplicateBuffer ) {
    System.out.println("sdbe: "+currTriple+" "+SAMRecordQualityComparator.getQualityScore(currTriple.right.getBaseQualityString()));
}

END DEBUG */
       //discardSet.add(duplicateBuffer.peek().right.getReadName());
       if ( !duplicateBuffer.isEmpty() && !discardSet.contains(duplicateBuffer.peek().right.getReadName()) ) {
         //System.out.println("WRITE "+duplicateBuffer.peek());
         outputSam.addAlignment(duplicateBuffer.peek().right);
       }
       while ( !duplicateBuffer.isEmpty() ) {
         discardSet.add(duplicateBuffer.poll().right.getReadName());
       }
       // Maintain the invariant that the first item in recordBuffer may have duplicates
       while ( !recordBuffer.isEmpty() && discardSet.contains(recordBuffer.peekFirst().right.getReadName()) ) {
         discardSet.remove(recordBuffer.poll().right.getReadName());
       }
    }

    public void finish () throws IOException {
        this.inputSam.close();
        this.outputSam.close();
    }
}
