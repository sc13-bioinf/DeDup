
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class Data {
       public static final Set<String> RMDupperTest__resolveDuplicate_yields_best_quality_expectedReadNames = Stream.of(
            "M_NS500559:30:HNM25BGXX:4:23404:22495:5372",
            "M_NS500559:30:HNM25BGXX:3:22508:6628:10061",
            "M_NS500559:30:HNM25BGXX:3:12504:16048:7514",
            "M_NS500559:30:HNM25BGXX:4:23408:7534:13863",
            "M_NS500559:30:HNM25BGXX:4:11404:21353:16162",
            "M_NS500559:30:HNM25BGXX:1:23311:19533:3291",
            "M_NS500559:30:HNM25BGXX:3:23402:1367:1427",
            "M_NS500559:30:HNM25BGXX:4:22607:14180:16389",
            "M_NS500559:30:HNM25BGXX:4:11610:12733:6726",
            "M_NS500559:30:HNM25BGXX:4:23409:2505:18276",
            "M_NS500559:30:HNM25BGXX:2:12204:22389:14660",
            "M_NS500559:30:HNM25BGXX:4:22505:24153:2528",
            "M_NS500559:30:HNM25BGXX:4:22512:19255:1372",
            "M_NS500559:30:HNM25BGXX:3:22512:6947:7877",
            "M_NS500559:30:HNM25BGXX:3:13501:8198:1908",
            "F_NS500559:30:HNM25BGXX:2:21209:12082:12672",
            "M_NS500559:30:HNM25BGXX:3:22507:7192:5163",
            "M_NS500559:30:HNM25BGXX:3:12607:11825:10419",
            "M_NS500559:30:HNM25BGXX:1:23307:13826:3081",
            "M_NS500559:30:HNM25BGXX:3:12601:6521:5315",
            "M_NS500559:30:HNM25BGXX:4:22605:13798:13816",
            "M_NS500559:30:HNM25BGXX:3:21403:16465:9082",
            "F_NS500559:30:HNM25BGXX:4:22510:21196:11652",
            "R_NS500559:30:HNM25BGXX:1:21203:15540:16052").collect(Collectors.toSet());
        public static final Set<String> RMDupperTest__resolveDuplicate_forward_expectedReadNames = Stream.of(
        "F_NS500559:30:HNM25BGXX:1:13209:14750:12764",
        "F_NS500559:30:HNM25BGXX:2:22309:10359:11690").collect(Collectors.toSet());
        public static final Set<String> RMDupperTest__resolveDuplicate_reverse_expectedReadNames = Stream.of(
        "R_NS500559:30:HNM25BGXX:1:21203:15540:16052").collect(Collectors.toSet());
        public static final Set<String> RMDupperTest__resolveDuplicate_reverse_merged_expectedReadNames = Stream.of(
        "R_NS500559:30:HNM25BGXX:1:23312:9844:3571",
        "M_NS500559:30:HNM25BGXX:2:12204:22389:14660",
        "M_NS500559:30:HNM25BGXX:4:11506:2444:17383"
        ).collect(Collectors.toSet());
        public static final Set<String> RMDupperTest__queueOrOutput_nextAlignmentStartBeyondFirstEndTriggersCheckForDuplication_pre_expectedReadNames = Stream.of(
        "M_NS500559:30:HNM25BGXX:1:12108:16299:13403",
        "M_NS500559:30:HNM25BGXX:3:12601:6521:5315",
        "M_NS500559:30:HNM25BGXX:2:12204:22389:14659"
        ).collect(Collectors.toSet());
        public static final Set<String> RMDupperTest__queueOrOutput_nextAlignmentStartBeyondFirstEndTriggersCheckForDuplication_post_expectedReadNames = Stream.of(
        "M_NS500559:30:HNM25BGXX:3:12601:6521:5315",
        "M_NS500559:30:HNM25BGXX:2:12204:22389:14659",
        "M_NS500559:30:HNM25BGXX:2:12204:22389:14660"
        ).collect(Collectors.toSet());
}
