package main.java;/*
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

import main.java.datastructure.OccurenceCounterMerged;
import main.java.datastructure.DupStats;

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
    private static final String VERSION = "0.11.3";
    private static boolean piped = true;

    private final Boolean allReadsAsMerged;
    private final SamReader inputSam;
    private final SAMFileWriter outputSam;
    private final DupStats dupStats = new DupStats();
    private OccurenceCounterMerged oc = new OccurenceCounterMerged();

    public RMDupper(File inputFile, File outputFile, Boolean merged) {
        inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).open(inputFile);
        outputSam = new SAMFileWriterFactory().makeSAMOrBAMWriter(inputSam.getFileHeader(), false, outputFile);
        allReadsAsMerged = merged;
    }


    public RMDupper(InputStream in, OutputStream out, Boolean merged) {
        inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).open(SamInputResource.of(in));
        outputSam = new SAMFileWriterFactory().makeSAMWriter(inputSam.getFileHeader(), false, out);
        allReadsAsMerged = merged;
    }


    public static void main(String[] args) throws IOException {
        System.err.println("DeDup v" + VERSION);
        // the command line parameters
        Options helpOptions = new Options();
        helpOptions.addOption("h", "help", false, "show this help page");
        Options options = new Options();
        options.addOption("h", "help", false, "show this help page");
        options.addOption("i", "input", true, "the input file if this option is not specified,\nthe input is expected to be piped in");
        options.addOption("o", "output", true, "the output folder. Has to be specified if input is set.");
        options.addOption("m", "merged", false, "the input only contains merged reads.\n If this option is specified read names are not examined for prefixes.\n Both the start and end of the aligment are considered for all reads.");
        options.addOption("v", "version", false, "the version of DeDup.");
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

        String input = "";
        String outputpath = "";
        Boolean merged = Boolean.FALSE;
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption('i')) {
                input = cmd.getOptionValue('i');
                piped = false;
            }
            if (cmd.hasOption('o')) {
                outputpath = cmd.getOptionValue('o');
            }
            if (cmd.hasOption('m')) {
              merged = Boolean.TRUE;
            }
            if(cmd.hasOption('v')){
                System.out.println("DeDup v" + VERSION);
                System.exit(0);
            }
        } catch (ParseException e) {
            helpformatter.printHelp(CLASS_NAME, options);
            System.err.println(e.getMessage());
            System.exit(0);
        }
        DecimalFormat df = new DecimalFormat("##.##");

        if (piped) {
            RMDupper rmdup = new RMDupper(System.in, System.out, merged);
            rmdup.readSAMFile();

            System.err.println("We are in piping mode!");
            System.err.println("Total reads: " + rmdup.dupStats.total + "\n");
            System.err.println("Reverse removed: " + rmdup.dupStats.removed_reverse + "\n");
            System.err.println("Forward removed: " + rmdup.dupStats.removed_forward + "\n");
            System.err.println("Merged removed: " + rmdup.dupStats.removed_merged + "\n");
            System.err.println("Total removed: " + (rmdup.dupStats.removed_forward + rmdup.dupStats.removed_merged
                    + rmdup.dupStats.removed_reverse) + "\n");
            if (rmdup.dupStats.removed_merged + rmdup.dupStats.removed_forward + rmdup.dupStats.removed_reverse == 0) {
                System.err.println("Duplication Rate: " + df.format(0.00));
            } else {
                System.err.println("Duplication Rate: " + df.format((double) (rmdup.dupStats.removed_merged + rmdup.dupStats.removed_reverse + rmdup.dupStats.removed_forward) / (double) rmdup.dupStats.total));
            }


        } else {
            if (outputpath.length() == 0) {
                System.err.println("The output folder has to be specified");
                helpformatter.printHelp(CLASS_NAME, options);
                System.exit(0);
            }

            //Check whether we have a directory as output path, else produce error message and quit!

            File f = new File(outputpath);
            if(!f.isDirectory()) {
                System.err.println("The output folder should be a folder and not a file!");
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

                RMDupper rmdup = new RMDupper(inputFile, outputFile, merged);
                rmdup.readSAMFile();
                rmdup.inputSam.close();
                rmdup.outputSam.close();

                bfw.write("Total reads: " + rmdup.dupStats.total + "\n");
                bfw.write("Reverse removed: " + rmdup.dupStats.removed_reverse + "\n");
                bfw.write("Forward removed: " + rmdup.dupStats.removed_forward + "\n");
                bfw.write("Merged removed: " + rmdup.dupStats.removed_merged + "\n");
                bfw.write("Total removed: " + (rmdup.dupStats.removed_forward + rmdup.dupStats.removed_merged
                    + rmdup.dupStats.removed_reverse) + "\n");
                bfw.write("Duplication Rate: " + df.format((double) (rmdup.dupStats.removed_merged + rmdup.dupStats.removed_reverse + rmdup.dupStats.removed_forward) / (double) rmdup.dupStats.total));
                bfw.flush();
                bfw.close();

                histbfw.write(rmdup.oc.getHistogram());
                histbfw.flush();
                histbfw.close();


                System.out.println("Total reads: " + rmdup.dupStats.total + "\n");
                System.out.println("Unmerged removed: " + (rmdup.dupStats.removed_forward + rmdup.dupStats.removed_reverse) + "\n");
                System.out.println("Merged removed: " + rmdup.dupStats.removed_merged + "\n");
                System.out.println("Total removed: " + (rmdup.dupStats.removed_forward + rmdup.dupStats.removed_merged
                    + rmdup.dupStats.removed_reverse) + "\n");
                if (rmdup.dupStats.removed_merged + rmdup.dupStats.removed_forward + rmdup.dupStats.removed_reverse == 0) {
                    System.out.println("Duplication Rate: " + df.format(0.00));
                } else {
                    System.out.println("Duplication Rate: " + df.format((double) (rmdup.dupStats.removed_merged + rmdup.dupStats.removed_reverse + rmdup.dupStats.removed_forward) / (double) rmdup.dupStats.total));
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
    public void readSAMFile() throws IOException {
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
                    queueOrOutput (this.dupStats, this.oc, this.outputSam, this.allReadsAsMerged, recordBuffer, discardSet, curr);
                } else {
                    flushQueue (this.dupStats, this.oc, this.outputSam, this.allReadsAsMerged, recordBuffer, discardSet);
                    queueOrOutput (this.dupStats, this.oc, this.outputSam, this.allReadsAsMerged, recordBuffer, discardSet, curr);
                    referenceName = curr.getReferenceName();
                }
            }

            this.dupStats.total++;
            if(this.dupStats.total % 100000 == 0){
                if(!piped) {
                    System.err.println("Reads treated: " + this.dupStats.total);
                }
            }
        }
        flushQueue(this.dupStats, this.oc, this.outputSam, this.allReadsAsMerged, recordBuffer, discardSet);
        finish();
    }

    public static void queueOrOutput (DupStats dupStats, OccurenceCounterMerged occurenceCounterMerged, SAMFileWriter outputSam, Boolean allReadsAsMerged, ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer, Set<String> discardSet, SAMRecord curr) {
        //Don't do anything with unmapped reads, just write them into the output!
        if (curr.getReadUnmappedFlag() || curr.getMappingQuality() == 0) {
          outputSam.addAlignment(curr);
        } else {
            if ( recordBuffer.size() > 0 && recordBuffer.peekFirst().middle < curr.getAlignmentStart() ) {
                checkForDuplication(dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, discardSet);
            }
            recordBuffer.add (new ImmutableTriple<Integer, Integer, SAMRecord>(curr.getAlignmentStart(), curr.getAlignmentEnd(), curr));
        }
    }

    public static void flushQueue (DupStats dupStats, OccurenceCounterMerged occurenceCounterMerged, SAMFileWriter outputSam, Boolean allReadsAsMerged, ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer, Set<String> discardSet) {
        while ( !recordBuffer.isEmpty() ) {
            checkForDuplication (dupStats, occurenceCounterMerged, outputSam, allReadsAsMerged, recordBuffer, discardSet);
        }
        discardSet.clear();
    }

    public static void checkForDuplication (DupStats dupStats, OccurenceCounterMerged occurenceCounterMerged, SAMFileWriter outputSam, Boolean allReadsAsMerged, ArrayDeque<ImmutableTriple<Integer, Integer, SAMRecord>> recordBuffer, Set<String> discardSet) {
        // At this point recordBuffer contains all alignments that overlap with its first entry
        // Therefore the task here is to de-duplicate for the first entry in recordBuffer
        Comparator<SAMRecord> samRecordComparator;
        if ( allReadsAsMerged ) {
          samRecordComparator = new SAMRecordQualityComparator();
        } else {
          samRecordComparator = new SAMRecordQualityComparatorPreferMerged();
        }

        PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>> duplicateBuffer = new PriorityQueue<ImmutableTriple<Integer, Integer, SAMRecord>>(1000, Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getRight, samRecordComparator.reversed()));

        Iterator<ImmutableTriple<Integer, Integer, SAMRecord>> it = recordBuffer.iterator();
        while (it.hasNext()) {
          ImmutableTriple<Integer, Integer, SAMRecord> maybeDuplicate = it.next();
          boolean duplicateIsShorterOrEqual = maybeDuplicate.middle - maybeDuplicate.left <= recordBuffer.peekFirst().middle - recordBuffer.peekFirst().left;
          boolean duplicateIsLongerOrEqual = recordBuffer.peekFirst().middle - recordBuffer.peekFirst().left <= maybeDuplicate.middle - maybeDuplicate.left;

          if ( allReadsAsMerged ) {
            if ( recordBuffer.peekFirst().left.equals(maybeDuplicate.left)  &&
                 recordBuffer.peekFirst().middle.equals(maybeDuplicate.middle) ) {
                 //System.out.println("* add");
                 duplicateBuffer.add(maybeDuplicate);
            }
          } else {
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
    System.out.println("dbe: "+currTriple+" "+main.java.SAMRecordQualityComparator.getQualityScore(currTriple.right.getBaseQualityString()));
}

// Sort again with priority queue order
sortedDuplicateBuffer.sort(Comparator.comparing(ImmutableTriple<Integer, Integer, SAMRecord>::getRight, samRecordComparator.reversed()));
for ( ImmutableTriple<Integer, Integer, SAMRecord> currTriple : sortedDuplicateBuffer ) {
    System.out.println("sdbe: "+currTriple+" "+main.java.SAMRecordQualityComparator.getQualityScore(currTriple.right.getBaseQualityString()));
}

END DEBUG */
       if ( !duplicateBuffer.isEmpty() && !discardSet.contains(duplicateBuffer.peek().right.getReadName()) ) {
         //System.out.println("WRITE "+duplicateBuffer.peek());
         decrementDuplicateStats(dupStats, allReadsAsMerged, duplicateBuffer.peek().right.getReadName());
         occurenceCounterMerged.putValue(Long.valueOf(duplicateBuffer.stream().filter(d -> allReadsAsMerged || d.right.getReadName().startsWith("M_")).count()).intValue() - 1);
         outputSam.addAlignment(duplicateBuffer.peek().right);
       }
       while ( !duplicateBuffer.isEmpty() ) {
         discardSet.add(duplicateBuffer.poll().right.getReadName());
       }
       // Maintain the invariant that the first item in recordBuffer may have duplicates
       while ( !recordBuffer.isEmpty() && discardSet.contains(recordBuffer.peekFirst().right.getReadName()) ) {
         String duplicateReadName = recordBuffer.poll().right.getReadName();
         incrementDuplicateStats(dupStats, allReadsAsMerged, duplicateReadName);
         discardSet.remove(duplicateReadName);
       }
    }

    public void finish () throws IOException {
        this.inputSam.close();
        this.outputSam.close();
    }

    public static void incrementDuplicateStats (DupStats dupStats, Boolean allReadsAsMerged, String readName) {
      if ( allReadsAsMerged || readName.startsWith("M_") ) {
        dupStats.removed_merged++;
      } else if ( readName.startsWith("F_") ) {
        dupStats.removed_forward++;
      } else if ( readName.startsWith("R_") ) {
        dupStats.removed_reverse++;
      }
    }

    public static void decrementDuplicateStats (DupStats dupStats, Boolean allReadsAsMerged, String readName) {
      if ( allReadsAsMerged || readName.startsWith("M_") ) {
        dupStats.removed_merged--;
      } else if ( readName.startsWith("F_") ) {
        dupStats.removed_forward--;
      } else if ( readName.startsWith("R_") ) {
        dupStats.removed_reverse--;
      }
    }
}