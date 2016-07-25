
import org.apache.commons.lang3.tuple.ImmutableTriple;
import htsjdk.samtools.SAMRecord;
import java.util.Comparator;

public class SAMRecordQualityComparatorPreferMerged implements Comparator<SAMRecord> {
       @Override
       public int compare(SAMRecord a, SAMRecord b) {
           int sa = getQualityScore(a.getBaseQualityString());
           int sb = getQualityScore(b.getBaseQualityString());
           return sa < sb ? -1 : ( sa == sb ?

           //0
           ( !a.getReadName().startsWith("M_") && b.getReadName().startsWith("M_") ? -1 : (
                a.getReadName().startsWith("M_") && b.getReadName().startsWith("M_") ? 0 : 1) )

           : 1);
       }
       /**
        * Sums up the quality score of a given quality string in FastQ/SAM format
        *
        * @param s
        * @return the quality score of a string S
        */
       public static int getQualityScore(String s) {
           int result = 0;
           for (Character c : s.toCharArray()) {
               result += (int) c;
           }
           return result;
       }
}
