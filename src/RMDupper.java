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
    private FileWriter fw;
    private BufferedWriter bfw;
    private FileWriter histfw;
    private BufferedWriter histbfw;
    private int total = 0;
    private int removed_reverse = 0;
    private int removed_forward = 0;
    private int removed_merged = 0;
    private OccurenceCounterMerged oc = new OccurenceCounterMerged();
    private OccurenceCounterSingle ocunmerged = new OccurenceCounterSingle();




    public RMDupper(File f, String outputpath) {
        inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).open(f);

        File output = new File(outputpath + "/" + Files.getNameWithoutExtension(f.getAbsolutePath()) + "_rmdup.bam");
        File outputlog = new File(outputpath + "/" + Files.getNameWithoutExtension(f.getAbsolutePath()) + ".log");
        File outputhist = new File(outputpath + "/" + Files.getNameWithoutExtension(f.getAbsolutePath()) + ".hist");
        try {
            fw = new FileWriter(outputlog);
            histfw = new FileWriter(outputhist);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bfw = new BufferedWriter(fw);
        histbfw = new BufferedWriter(histfw);
        outputSam = new SAMFileWriterFactory().makeSAMOrBAMWriter(inputSam.getFileHeader(), false, output);
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
        String output = "";
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption('i')) {
                input = cmd.getOptionValue('i');
                pipe = false;
            }
            if (cmd.hasOption('o')) {
                output = cmd.getOptionValue('o');
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
            if (output.length() == 0) {
                System.err.println("The output folder has to be specified");
                helpformatter.printHelp(CLASS_NAME, options);
                System.exit(0);
            }
            RMDupper rmdup = new RMDupper(new File(input), output);
            rmdup.readSAMFile();
            rmdup.inputSam.close();
            rmdup.outputSam.close();

            rmdup.bfw.write("Total reads: " + rmdup.total + "\n");
            rmdup.bfw.write("Reverse removed: " + rmdup.removed_reverse + "\n");
            rmdup.bfw.write("Forward removed: " + rmdup.removed_forward + "\n");
            rmdup.bfw.write("Merged removed: " + rmdup.removed_merged + "\n");
            rmdup.bfw.write("Total removed: " + (rmdup.removed_forward + rmdup.removed_merged
                    + rmdup.removed_reverse) + "\n");
            rmdup.bfw.write("Duplication Rate: " + df.format((double) (rmdup.removed_merged + rmdup.removed_reverse + rmdup.removed_forward) / (double) rmdup.total));
            rmdup.bfw.flush();
            rmdup.bfw.close();

            rmdup.histbfw.write(rmdup.oc.getHistogram());
            rmdup.histbfw.flush();
            rmdup.histbfw.close();


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
        }
    }

    /**
     * This Method reads a SAM File and parses the input
     * Currently, only merged Reads with the "M" Flag in front are checked for Duplicates.
     * R/F Flags are simply written into output File, also other "non-flagged" ones.
     */
    private void readSAMFile() {
        ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer = new ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>>(1000);

        String referenceName = SAMRecord.NO_ALIGNMENT_REFERENCE_NAME;
        Iterator it = inputSam.iterator();
        while (it.hasNext()) {
            SAMRecord curr = (SAMRecord) it.next();
            if ( curr.getReferenceName() == SAMRecord.NO_ALIGNMENT_REFERENCE_NAME ) {
                outputSam.addAlignment(curr);
            } else {
                if ( referenceName == curr.getReferenceName() ) {
                    queueOrOutput (recordBuffer, curr);
                } else {
                    flushQueue (recordBuffer);
                    queueOrOutput (recordBuffer, curr);
                    referenceName = curr.getReferenceName();
                }
            }

            total++;
            if(total % 100000 == 0){
                System.err.println("Reads treated: " + total);
            }
        }
        flushQueue(recordBuffer);
    }

    private void queueOrOutput (ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer, SAMRecord curr) {
        //Don't do anything with unmapped reads, just write them into the output!
        if (curr.getReadUnmappedFlag() || curr.getMappingQuality() == 0) {
            this.outputSam.addAlignment(curr);
        } else {
            if ( recordBuffer.size() > 0 && recordBuffer.peekFirst().left < curr.getAlignmentStart() ) {
                checkForDuplication(recordBuffer);
            }
            recordBuffer.add (new ImmutableTriple(curr.getAlignmentStart(), curr.getAlignmentEnd(), curr));
        }
    }

    private void flushQueue (ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer) {
        checkForDuplication (recordBuffer);
    }

    private void checkForDuplication (ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer) {
        PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>> duplicateBuffer = new PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>>(1000, Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getMiddle));
        while ( recordBuffer.size() > 0 ) {
            // Fill duplicateBuffer with alignments from the queue that have the same start
            while ( recordBuffer.size() > 0 && (duplicateBuffer.isEmpty() || duplicateBuffer.peek().left.equals(recordBuffer.peekFirst().left) ) ) {
                duplicateBuffer.add(recordBuffer.poll());
            }
            // DEBUG
            System.out.println ("duplicateBuffer");
            ArrayList<ImmutableTriple<Integer, Integer, SAMRecord>> sortedDuplicateBuffer = new ArrayList<ImmutableTriple<Integer, Integer, SAMRecord>>(duplicateBuffer.size());
            Iterator<ImmutableTriple<Integer, Integer, SAMRecord>> it = duplicateBuffer.iterator();
            while (it.hasNext()) {
                sortedDuplicateBuffer.add(it.next());
            }
            sortedDuplicateBuffer.sort(Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getMiddle));

            for ( ImmutableTriple<Integer, Integer, SAMRecord> currTriple : sortedDuplicateBuffer ) {
                System.out.println("dbe: "+currTriple);
            }
            // END DEBUG

            Set<String> duplicateSet = new HashSet<String>();
            if ( duplicateBuffer.size() > 1 ) {
                resolveDuplicates(duplicateSet, duplicateBuffer);
            }
            //System.out.println("duplicateSet:");
            //System.out.println(duplicateSet);

            while ( duplicateBuffer.size() > 0 ) {
                ImmutableTriple<Integer, Integer, SAMRecord> pollTriple = duplicateBuffer.poll();
                if ( !duplicateSet.contains(pollTriple.right.getReadName()) ) {
                    System.out.println ("kept"+pollTriple);
                    this.outputSam.addAlignment(pollTriple.right);
                }
            }
            duplicateSet.clear();
        }
    }

    private void resolveDuplicates(Set<String> duplicateSet, PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>> duplicateBuffer) {
        System.out.println ("resolveDuplicates");
        ImmutableTriple<Integer, Integer, SAMRecord> bestTriple = duplicateBuffer.peek();

        ArrayList<ImmutableTriple<Integer, Integer, SAMRecord>> sortedDuplicateBuffer = new ArrayList<ImmutableTriple<Integer, Integer, SAMRecord>>(duplicateBuffer.size());
        Iterator<ImmutableTriple<Integer, Integer, SAMRecord>> it = duplicateBuffer.iterator();
        while (it.hasNext()) {
          sortedDuplicateBuffer.add(it.next());
        }
        sortedDuplicateBuffer.sort(Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getMiddle));

        for ( ImmutableTriple<Integer, Integer, SAMRecord> currTriple : sortedDuplicateBuffer ) {
            duplicateSet.add(currTriple.right.getReadName());
            if ( bestTriple.middle.equals(currTriple.middle) ) {
                bestTriple = resolveDuplicate(duplicateSet, bestTriple, currTriple);
            } else {
                duplicateSet.remove(bestTriple.right.getReadName());
                bestTriple = currTriple;
            }
        }
        duplicateSet.remove(bestTriple.right.getReadName());
    }

    private ImmutableTriple<Integer, Integer, SAMRecord> resolveDuplicate(Set<String> duplicateSet, ImmutableTriple<Integer, Integer, SAMRecord> bestTriple, ImmutableTriple<Integer, Integer, SAMRecord> currTriple) {
        if ( getQualityScore(currTriple.right.getBaseQualityString()) > getQualityScore(bestTriple.right.getBaseQualityString()) ) {
            return currTriple;
        }
        else if ( getQualityScore(currTriple.right.getBaseQualityString()) == getQualityScore(bestTriple.right.getBaseQualityString()) ) {
            if ( currTriple.right.getReadName().startsWith("M_") ) {
                return currTriple;
            } else {
                return bestTriple;
            }
        }
        else {
            return bestTriple;
        }
    }

    /**
     * Sums up the quality score of a given quality string in FastQ/SAM format
     *
     * @param s
     * @return the quality score of a string S
     */
    private int getQualityScore(String s) {
        int result = 0;
        for (Character c : s.toCharArray()) {
            result += (int) c;
        }
        return result;
    }
}
