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

import java.util.StringTokenizer;

/**
 * Contains utility functions for handling with <em>bundle lists</em>, that
 * is, a comma-delimited list of strings that are contained as one line in a
 * Java properties file. This class contains methods to parse and create these
 * bundle lists.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 */
public abstract class BundleLists {

    /**
     * Parses a string into an array of substrings, using a comma as a delimiter
     * and trimming whitespace.
     *
     * @param list
     *            The list formatted as a <code>delimeter</code> -delimited
     *            string.
     * @return The list formatted as an array of strings.
     */
    public static String[] parseBundleList(String list) {
        return parseBundleList(list, ",");
    }

    /**
     * Parses a string into an array of substrings, using the specified
     * delimeter and trimming whitespace.
     *
     * @param list
     *            The list formatted as a <code>delimeter</code> -delimited
     *            string.
     * @param delimiter
     *            The delimeter.
     * @return The list formatted as an array of strings.
     */
    public static String[] parseBundleList(String list, String delimiter) {
        if (list == null || delimiter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String[] strings = new String[0];

        StringTokenizer tokenizer = new StringTokenizer(list, delimiter, false);

        int stringCount = tokenizer.countTokens();
        strings = new String[stringCount];

        for (int i = 0; i < stringCount; i++) {
            strings[i] = tokenizer.nextToken().trim();
        }

        return (strings);
    }

    /**
     * Parses a Object array and puts them into a array of substrings, using a
     * comma as a delimiter
     *
     * @param objArray
     *            The object array to be formatted as a comma-delimited string.
     * @return The comma-delimited string.
     */
    public static String createBundleList(Object[] objArray) {
        return createBundleList(objArray, ", ");
    }

    /**
     * Parses a Object array and puts them into a array of substrings, using a
     * comma as a delimiter
     *
     * @param objArray
     *            The object array to be formatted as a comma-delimited string.
     * @return The comma-delimited string.
     * @param delimiter a {@link java.lang.String} object.
     */
    public static String createBundleList(Object[] objArray, String delimiter) {
        if (objArray == null || delimiter == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final StringBuilder strings = new StringBuilder();

        for (int i = 0; i < objArray.length; i++) {
            strings.append(objArray[i].toString());

            if (i < objArray.length - 1) {
                strings.append(delimiter);
            }
        }

        return (strings.toString());
    }
}
