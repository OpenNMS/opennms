/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.search.api;

import java.util.List;
import java.util.Objects;

public abstract class QueryUtils {

    private QueryUtils() {}

    public static Object ilike(String input) {
        return String.format("%%%s%%", input);
    }

    public static boolean equals(Integer checkMe, String input) {
        if (checkMe == null) {
            return false;
        }
        return checkMe.toString().equals(input);
    }

    public static boolean matches(String checkMe, String input) {
        if (checkMe == null) {
            return false;
        }
        return checkMe.toLowerCase().contains(input.toLowerCase());
    }

    public static boolean matches(List<String> checkMe, String input) {
        if (checkMe != null) {
            return checkMe.stream().anyMatch(it -> matches(it, input));
        }
        return false;
    }

    public static <T> List<T> shrink(List<T> input, int maxResults) {
        Objects.requireNonNull(input);
        final List<T> subList = input.subList(0, Math.min(maxResults, input.size()));
        return subList;
    }

    public static String getFirstMatch(List<String> aliases, String input) {
        return aliases.stream().filter(alias -> matches(alias, input)).findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find any match"));
    }
}