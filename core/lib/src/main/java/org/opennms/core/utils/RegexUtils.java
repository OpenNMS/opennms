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
