/*
 * Copyright 2017, Solutech RMS
 * Licensed under the Apache License, Version 2.0, "Solutech Limited".
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev4vin.promise.util;

import dev4vin.promise.model.Category;
import dev4vin.promise.model.List;

/**
 * Created on 6/8/18 by yoctopus.
 */
public class ArrayUtil {
    public static List<Category<Double, Double>> classify(List<Double> list, int chunks) {
        return list.sorted((o1, o2) -> o1 > o2 ? 1 : o1 < o2 ? -1 : 0).groupBy(aDouble -> aDouble);
    }

    public static int sum(List<Integer> items) {
        int sum = 0;
        for (int item: items) sum += item;
        return sum;
    }

    public static int sum(Integer... items) {
        return sum(List.fromArray(items));
    }

    public static double average(List<Integer> items) {
        if (items.isEmpty()) return 0;
        return sum(items) / items.size();
    }

    public static double average(Integer... items) {
        return average(List.fromArray(items));
    }
}
