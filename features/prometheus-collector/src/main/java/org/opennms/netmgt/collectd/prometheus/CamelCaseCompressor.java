/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
