
import org.apache.commons.lang3.tuple.ImmutableTriple;
import htsjdk.samtools.SAMRecord;
import java.util.Comparator;

public class SAMRecordQualityComparator implements Comparator<SAMRecord> {
       @Override
       public int compare(SAMRecord a, SAMRecord b) {
           int sa = getQualityScore(a.getBaseQualityString());
           int sb = getQualityScore(b.getBaseQualityString());
           return sa < sb ? -1 : ( sa == sb ? 0 : 1);
       }
       private static int getQualityScore(String s) {
           int result = 0;
           for (Character c : s.toCharArray()) {
               result += (int) c;
           }
           return result;
       }
}
