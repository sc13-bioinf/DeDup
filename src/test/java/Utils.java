package test.java;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import htsjdk.samtools.*;

public class Utils {
    public static List<String> getReadNamesFromSAM (InputStream in) {
        List<String> result = new ArrayList<String> ();
        SamReader inputSam = SamReaderFactory.make().validationStringency(ValidationStringency.LENIENT).open(SamInputResource.of(in));
        Iterator<SAMRecord> it = inputSam.iterator();
        it.forEachRemaining(samRecord -> result.add(samRecord.getReadName()));
        return result;
    }
}
