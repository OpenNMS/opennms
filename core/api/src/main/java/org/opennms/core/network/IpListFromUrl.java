/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.core.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for generating a list of IP addresses from a file URL.
 */
public abstract class IpListFromUrl {
    private static final Logger LOG = LoggerFactory.getLogger(IpListFromUrl.class);

    protected IpListFromUrl() {
    }

    /**
     * The string indicating the start of the comments in a line containing the
     * IP address in a file URL
     */
    private static final String COMMENT_STR = " #";

    /**
     * This character at the start of a line indicates a comment line in a URL
     * file
     */
    private static final char COMMENT_CHAR = '#';

    /**
     * This method is used to read all interfaces from an URL file.
     *
     * <pre>
     * The file URL is read and each entry in this file checked. Each line
     *  in the URL file can be one of -
     *  &lt;IP&gt;&lt;space&gt;#&lt;comments&gt;
     *  or
     *  &lt;IP&gt;
     *  or
     *  #&lt;comments&gt;
     *
     *  Lines starting with a '#' are ignored and so are characters after
     *  a '&lt;space&gt;#' in a line.
     * </pre>
     *
     * @param url
     *            The url file to read
     * @return list of IPs in the file
     */
    @SuppressWarnings("java:S2139")
    public static List<String> fetch(final String url) throws IOException {
        final List<String> iplist = new ArrayList<>();

        URL u = null;
        try {
            u = new URL(url);
        } catch (final IOException e) {
            LOG.error("Error reading URL: {}: {}", url, e.getLocalizedMessage());
            throw e;
        }

        try (
                final InputStream stream = u.openStream();
                final InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
                final BufferedReader br = new BufferedReader(isr);
        ) {
            String ipLine = null;
            String specIP = null;

            // get each line of the file and turn it into a specific range
            while ((ipLine = br.readLine()) != null) {
                ipLine = ipLine.trim();
                if (ipLine.length() == 0 || ipLine.charAt(0) == COMMENT_CHAR) {
                    // blank line or skip comment
                    continue;
                }

                // check for comments after IP
                final int comIndex = ipLine.indexOf(COMMENT_STR);
                if (comIndex == -1) {
                    specIP = ipLine;
                } else {
                    specIP = ipLine.substring(0, comIndex);
                    ipLine = ipLine.trim();
                }

                iplist.add(specIP);
            }
        }

        return iplist;
    }
}
