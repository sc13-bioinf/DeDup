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

package datastructure;

import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

/**
 * Created by peltzer on 16/09/15.
 */
public class OccurenceCounterMerged {
    /**
     * Occurence should follow a simple distribution in general
     * |
     * |*
     * |* *
     * |* * *
     * |* * * *
     * |* * * * *
     * |* * * * * *
     * |* * * * * *
     * |*_*_*_*_*_*___________________________
     * ---> Number of occurences (n_i), this would be a good sample, for non-good ones, for any n_i+1 it would be that
     * n_i+1 > n_i.
     */

    private HashMap<Integer, Integer> occurencyHashMap;

    public OccurenceCounterMerged() {
        this.occurencyHashMap = new HashMap<Integer, Integer>();
    }

    public void putValue(Integer numberOfDuplicates) {
        if (occurencyHashMap.containsKey(numberOfDuplicates)) {
            occurencyHashMap.put(numberOfDuplicates, occurencyHashMap.get(numberOfDuplicates) + 1);
        } else {
            occurencyHashMap.put(numberOfDuplicates, 1);
        }
    }


    public String getHistogram() {
        StringBuilder buffer = new StringBuilder();
        Integer[] sortedOccurences = this.occurencyHashMap.keySet().toArray(new Integer[this.occurencyHashMap.size()]);
        Arrays.sort(sortedOccurences);
        //List<Integer> sortedOccurences = this.occurencyHashMap.keySet().toArray(new Integer[this.occurencyHashMap.size()]);
        if ( !this.occurencyHashMap.isEmpty() ) {
          for (int count = 0; count <= sortedOccurences[sortedOccurences.length -1];count++) {
              buffer.append("" + (count + 1) + "\t" + (this.occurencyHashMap.containsKey(count) ? this.occurencyHashMap.get(count) : 0) + "\n");
          }
        }
        return buffer.toString();
    }


}
