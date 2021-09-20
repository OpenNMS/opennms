/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

/**
 * Utility functions for regular expression patterns.
 */
public class RegexUtils {
    private static final Pattern NAMED_CAPTURE_GROUPS_REGEX = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    /**
     * Extracts the names of the named capture groups found within a regex.
     *
     * This function may return false positives, so you must ensure to guard against
     * IllegalArgumentExceptions when calling {@link Matcher#group(String)}.
     *
     * Derived from https://stackoverflow.com/questions/15588903/get-group-names-in-java-regex
     *
     * The returned {@code Set} preserves the order of capture groups as found in the pattern.
     *
     * @param pattern the pattern from which to extract the named capture groups
     * @return an ordered list of named capture group candidates
     */
    public static Set<String> getNamedCaptureGroupsFromPattern(String pattern) {
        final Set<String> namedGroups = Sets.newLinkedHashSet();
        final Matcher m = NAMED_CAPTURE_GROUPS_REGEX.matcher(pattern);
        while (m.find()) {
            namedGroups.add(m.group(1));
        }
        return namedGroups;
    }

}
