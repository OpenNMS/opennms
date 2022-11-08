/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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