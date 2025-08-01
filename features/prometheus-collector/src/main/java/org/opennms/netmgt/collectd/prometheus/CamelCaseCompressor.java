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
package org.opennms.netmgt.collectd.prometheus;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Method for reducing the length of camelFormattedStrings
 * while attempting to preserve the structure and meaning.
 *
 * @author jwhite
 */
public class CamelCaseCompressor {

    public static String compress(String input, int maxLen) {
        if (input == null) {
            return null;
        }

        int len = input.length();
        if (len <= maxLen) {
            return input;
        }

        // Split the string into components
        final String[] components = StringUtils.splitByCharacterTypeCamelCase(input);

        // Determine the number of characters we need to remove
        int numCharactersToRemove = Arrays.stream(components).mapToInt(String::length).sum() - maxLen;

        // Build a priority queue of the components, larger elements have higher priority
        PriorityQueue<Component> q = new PriorityQueue<>();
        int i = 0;
        for (String component : components) {
            q.add(new Component(i++, component));
        }

        // Remove a character from the largest string until we've removed enough characters
        while (numCharactersToRemove > 0) {
            Component component = q.remove();
            if (!component.removeCharacter()) {
                continue;
            }
            q.add(component);
            numCharactersToRemove--;
        }

        // Rebuild the string
        return q.stream().sorted(Comparator.comparing(c -> c.index))
                .map(c -> c.value).collect(Collectors.joining());
    }

    private static class Component implements Comparable<Component> {
        int index;
        String value;

        public Component(int index, String value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(Component o) {
            return o.value.length() - value.length();
        }

        public boolean removeCharacter() {
            if (value == null || value.length() < 1) {
                return false;
            }
            value = value.substring(0, value.length() -1);
            return true;
        }
    }
}
