//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Organize imports, Java 5 generics, create log() method.
//              - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.core.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;


/**
 * Convenience class for generating a list of IP addresses from a file URL.
 */
public class IpListFromUrl extends Object {
    /**
     * The string indicating the start of the comments in a line containing the
     * IP address in a file URL
     */
    private final static String COMMENT_STR = " #";

    /**
     * This character at the start of a line indicates a comment line in a URL
     * file
     */
    private final static char COMMENT_CHAR = '#';

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
     * 
     * @return list of IPs in the file
     */
    public static List<String> parse(String url) {
        List<String> iplist = new ArrayList<String>();

        try {
            // open the file indicated by the url
            URL fileURL = new URL(url);

            InputStream file = fileURL.openStream();

            // check to see if the file exists
            if (file != null) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(file, "UTF-8"));

                String ipLine = null;
                String specIP = null;

                // get each line of the file and turn it into a specific range
                while ((ipLine = buffer.readLine()) != null) {
                    ipLine = ipLine.trim();
                    if (ipLine.length() == 0 || ipLine.charAt(0) == COMMENT_CHAR) {
                        // blank line or skip comment
                        continue;
                    }

                    // check for comments after IP
                    int comIndex = ipLine.indexOf(COMMENT_STR);
                    if (comIndex == -1) {
                        specIP = ipLine;
                    } else {
                        specIP = ipLine.substring(0, comIndex);
                        ipLine = ipLine.trim();
                    }

                    iplist.add(specIP);
                }

                buffer.close();
            } else {
                // log something
                log().warn("URL does not exist: " + url.toString());
            }
        } catch (MalformedURLException e) {
            log().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
        } catch (FileNotFoundException e) {
            log().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
        } catch (IOException e) {
            log().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
        }

        return iplist;
    }

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(IpListFromUrl.class);
    }
}
