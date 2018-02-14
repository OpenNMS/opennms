/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
