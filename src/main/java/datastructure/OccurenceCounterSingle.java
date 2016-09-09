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

package main.java.datastructure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by peltzer on 16/09/15.
 */
public class OccurenceCounterSingle {

    private int currPosition = 0;
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

    public OccurenceCounterSingle() {
        this.occurencyHashMap = new HashMap<>();
    }

    public void putValue(int key) {
        if (occurencyHashMap.containsKey(key)) {
            occurencyHashMap.put(key, occurencyHashMap.get(key) + 1);
        } else {
            occurencyHashMap.put(key, 1);
        }
    }


    public String getHistogram() {
        String buffer = "";
        //Key == number of occurences, Value = occurences
        Map<Integer, Integer> results = new TreeMap<>();
        Iterator iter = occurencyHashMap.values().iterator();
        while ((iter.hasNext())) {
            int x = (Integer) iter.next();
            if (results.containsKey(x)) {
                results.put(x, results.get(x) + 1);
            } else {
                results.put(x, 1);
            }
        }


        Iterator iter2 = results.values().iterator();
        int count = 1;
        while ((iter2.hasNext())) {
            buffer = buffer + count + "\t" + iter2.next() + "\n";
            count++;
        }
        return buffer;
    }


}
