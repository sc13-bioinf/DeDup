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
import java.util.*;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import datastructure.DedupStore;
import datastructure.OccurenceCounterMerged;
import datastructure.OccurenceCounterSingle;
import htsjdk.samtools.*;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * DeDup Tool for Duplicate Removal of short read duplicates in BAM/SAM Files.
 *
 * @author Alexander Peltzer
 * @version 0.9.10
 * @Date: 09/17/15
 */
public class RMDupper{
    private static final String CLASS_NAME = "dedup";
    private static final String VERSION = "0.9.10";

    private final SamReader inputSam;
    private final SAMFileWriter outputSam;
    private FileWriter fw;
    private BufferedWriter bfw;
    private FileWriter histfw;
    private BufferedWriter histbfw;
    private LinkedHashMap<DedupStore, SAMRecord> workSet;
    private LinkedHashMap<Integer, SAMRecord> forwards;
    private int total = 0;
    private int removed_reverse = 0;
    private int removed_forward = 0;
    private int removed_merged = 0;
    private int position_merged = 0;
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
        workSet = new LinkedHashMap<DedupStore, SAMRecord>();
        forwards = new LinkedHashMap<Integer, SAMRecord>();
    }


    public RMDupper(InputStream in, OutputStream out) {
        inputSam = SamReaderFactory.make().enable(SamReaderFactory.Option.DONT_MEMORY_MAP_INDEX).validationStringency(ValidationStringency.LENIENT).open(SamInputResource.of(in));
        outputSam = new SAMFileWriterFactory().makeSAMWriter(inputSam.getFileHeader(), false, out);
        workSet = new LinkedHashMap<DedupStore, SAMRecord>();
        forwards = new LinkedHashMap<Integer, SAMRecord>();
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
            rmdup.resolveDuplicates(4);
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
            rmdup.resolveDuplicates(4);
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
        Iterator it = inputSam.iterator();
        while (it.hasNext()) {
            SAMRecord curr = (SAMRecord) it.next();
            //Don't do anything with unmapped reads, just write them into the output!
            if (curr.getReadUnmappedFlag() || curr.getMappingQuality() == 0) {
                outputSam.addAlignment(curr);
            } else {
                checkForDuplication(curr);
            }
            total++;
            if(total % 100000 == 0){
                System.err.println("Reads treated: " + total);
            }
        }
    }

    /**
     * Method that checks a provided SAMRecord for potential duplication, calling resolveduplicates(), as soon as done.
     *
     * @param rec SAMRecord
     */

    private void checkForDuplication(SAMRecord rec) {

        if (workSet.isEmpty() && rec.getReadName().startsWith("M_")){
            DedupStore dp = new DedupStore(rec.getAlignmentStart(), rec.getAlignmentEnd());
            workSet.put(dp, rec);
            oc.putValue(dp);
            this.position_merged = rec.getAlignmentStart();
        } else
        if(forwards.isEmpty()  && !rec.getReadName().startsWith("M_")){
            forwards.put(rec.getAlignmentStart(), rec);
            ocunmerged.putValue(rec.getAlignmentStart());
            this.position_merged = rec.getAlignmentStart();
        } else

        if (rec.getReadName().startsWith("M_")) {
                int start_outside = rec.getAlignmentStart();
                if (position_merged == start_outside) {
                    this.position_merged = start_outside;
                    DedupStore dp = new DedupStore(rec.getAlignmentStart(), rec.getAlignmentEnd());
                    oc.putValue(dp);

                    if (workSet.containsKey(dp)) { //Then we have a read inside, that starts and stops the same...
                        SAMRecord inside = workSet.get(dp);
                        //Check base qualities now!
                        if (getQualityScore(inside.getBaseQualityString()) >= getQualityScore(rec.getBaseQualityString())) {
                            removed_merged++; //if the BQ of our read in the workset is higher than the new read, we drop the new read!

                        } else {
                            workSet.put(dp, rec); //Same start, stop but better base quality on average!
                            removed_merged++;
                        }
                    } else {
                        this.position_merged = start_outside;
                        workSet.put(dp, rec); //in case we have a read with different stop but same start, we want to keep it in the set for now!
                    }
                } else {
                    // Unequal start, stop and flags.
                    resolveDuplicates(3);
                    DedupStore dp = new DedupStore(rec.getAlignmentStart(), rec.getAlignmentEnd());
                    oc.putValue(dp);
                    workSet.put(dp, rec);
                    this.position_merged = rec.getAlignmentStart();
                }
            } else

        {
                    int startPosForward = rec.getAlignmentStart();

                    if (forwards.containsKey(startPosForward)) {//Check which one's better
                        SAMRecord inside = forwards.get(startPosForward);
                        this.position_merged = startPosForward;
                        if (getQualityScore(inside.getBaseQualityString()) <= getQualityScore(rec.getBaseQualityString())) {
                            //Then we drop the old one and replace it by the new entry
                            forwards.put(rec.getAlignmentStart(), rec);
                            removed_forward++;

                        } else {
                            removed_forward++; //cause we just delete the entry in this case!
                        }
                    } else {
                        resolveDuplicates(2);
                        // We add a new alignment to forwards
                        forwards.put(rec.getAlignmentStart(), rec);
                        this.position_merged = rec.getAlignmentStart();
                    }
                }

            }



        /**
         * Method that resolved potential duplicates with the same starting position in our workingSet<merged/unmerged>
         */

    private void resolveDuplicates(int which) {
     if (which == 2) {
                for (SAMRecord fw : forwards.values()) {
                    outputSam.addAlignment(fw);
                }
                forwards.clear();


        } else if (which == 3) {
                for (SAMRecord merged : workSet.values()) {
                    outputSam.addAlignment(merged);
                }
                workSet.clear();



        } else if (which == 4) { //clear all case

            for (SAMRecord fw : forwards.values()) {
                outputSam.addAlignment(fw);
            }
            for (SAMRecord merged : workSet.values()) {
                outputSam.addAlignment(merged);
            }
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
