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
package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A convenience class containing RRD file and directory related constants.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 */
public class RrdFileConstants {
    private static final Pattern GRAPHING_ESCAPE_PATTERN;
    static {
        // IPv6: ':' and '%'
        if (File.separatorChar == '\\') {
            // If Windows, escape '\' as well
            GRAPHING_ESCAPE_PATTERN = Pattern.compile("([\\:\\%\\\\])");
        } else {
            GRAPHING_ESCAPE_PATTERN = Pattern.compile("([\\:\\%])");
        }
    }

	/** The longest an RRD filename can be, currently 1024 characters. */
    public static final int MAX_RRD_FILENAME_LENGTH = 1024;

    /**
     * Determines if the provided File object represents a valid RRD latency
     * directory.
     *
     * @param file a {@link java.io.File} object.
     * @return a boolean.
     */
    public static final boolean isValidRRDLatencyDir(final File file, final String suffix) {
        if (!file.isDirectory()) {
            return false;
        }

        FilenameFilter rrdFilenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File file, final String name) {
                return name.endsWith(suffix);
            }
        };

        // if the directory contains RRDs, then it is queryable
        final File[] nodeRRDs = file.listFiles(rrdFilenameFilter);
        if (nodeRRDs != null && nodeRRDs.length > 0) {
            return true;
        }

        return false;
    }

    /**
     * Checks an RRD filename to make sure it is of the proper length and does
     * not contain any unexpected charaters.
     *
     * The maximum length is specified by the
     * {@link #MAX_RRD_FILENAME_LENGTH MAX_RRD_FILENAME_LENGTH}constant. The
     * only valid characters are letters (A-Z and a-z), numbers (0-9), dashes
     * (-), dots (.), and underscores (_). These precautions are necessary since
     * the RRD filename is used on the commandline and specified in the graph
     * URL.
     *
     * @param rrd a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isValidRRDName(final String rrd) {
        if (rrd == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final int length = rrd.length();

        if (length > MAX_RRD_FILENAME_LENGTH) {
            return false;
        }

        // cannot contain references to higher directories for security's sake
        if (rrd.indexOf("..") >= 0) {
            return false;
        }

        for (int i = 0; i < length; i++) {
        	final char c = rrd.charAt(i);

            if (!(('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9') || (c == '_') || (c == '.') || (c == '-') || (c == '/'))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Note this method will <strong>not </strong> handle references to higher
     * directories ("..").
     *
     * @param rrd a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String convertToValidRrdName(final String rrd) {
        if (rrd == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final StringBuilder buffer = new StringBuilder(rrd);

        // truncate after the max length
        if (rrd.length() > MAX_RRD_FILENAME_LENGTH) {
            buffer.setLength(MAX_RRD_FILENAME_LENGTH - 1);
        }

        final int length = buffer.length();

        for (int i = 0; i < length; i++) {
            char c = buffer.charAt(i);

            if (!(('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9') || (c == '_') || (c == '.') || (c == '-') || (c == '/'))) {
                buffer.setCharAt(i, '_');
            }
        }

        return buffer.toString();
    }

    public static String escapeForGraphing(final String path) {
    	final Matcher matcher = GRAPHING_ESCAPE_PATTERN.matcher(path);
    	return '"' + matcher.replaceAll("\\\\$1") + '"'; // To avoid NMS-6331, put double quotes around the DS path.
    }

}
