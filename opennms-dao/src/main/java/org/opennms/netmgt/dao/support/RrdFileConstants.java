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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.opennms.netmgt.rrd.RrdUtils;

/**
 * A convenience class containing RRD file and directory related constants.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @version $Id: $
 */
public class RrdFileConstants extends Object {

    /** The longest an RRD filename can be, currently 1024 characters. */
    public static final int MAX_RRD_FILENAME_LENGTH = 1024;

    /** Convenience filter that matches only RRD files. */
    public static final FilenameFilter RRD_FILENAME_FILTER = new FilenameFilter() {
        public boolean accept(File file, String name) {
            return name.endsWith(getRrdSuffix());
        }
    };

    /** Convenience filter that matches directories with RRD files in them. */
    public static final FileFilter INTERFACE_DIRECTORY_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return isValidRRDInterfaceDir(file);
        }
    };

    /**
     * Convenience filter that matches integer-named directories that either
     * contain RRD files or directories that contain RRD files.
     */
    public static final FileFilter NODE_DIRECTORY_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return isValidRRDNodeDir(file);
        }
    };

    /**
     * <p>isValidRRDNodeDir</p>
     *
     * @param file a {@link java.io.File} object.
     * @return a boolean.
     */
    public static final boolean isValidRRDNodeDir(File file) {
        if (!file.isDirectory()) {
            return false;
        }

        try {
            // if the directory name is an integer
            Integer.parseInt(file.getName());
        } catch (Exception e) {
            return false;
        }

        // if the node dir contains RRDs, then it is queryable
        File[] nodeRRDs = file.listFiles(RRD_FILENAME_FILTER);
        if (nodeRRDs != null && nodeRRDs.length > 0) {
            return true;
        }

        // if the node dir contains queryable interface directories, then
        // it is queryable
        File[] intfDirs = file.listFiles(INTERFACE_DIRECTORY_FILTER);
        if (intfDirs != null && intfDirs.length > 0) {
            return true;
        }

        return false;
    }

    /**
     * Convenience filter that matches non-integer-named directories that
     * contain directories that contain RRD files.
     */
    public static final FileFilter DOMAIN_DIRECTORY_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return isValidRRDDomainDir(file);
        }
    };

    /**
     * <p>isValidRRDDomainDir</p>
     *
     * @param file a {@link java.io.File} object.
     * @return a boolean.
     */
    public static final boolean isValidRRDDomainDir(File file) {
        if (!file.isDirectory()) {
            return false;
        }

        try {
            // if the directory name is an integer
            Integer.parseInt(file.getName());
        } catch (Exception e) {
        
            // if the domain dir contains queryable interface directories, then
            // it is queryable
            File[] intfDirs = file.listFiles(INTERFACE_DIRECTORY_FILTER);
            if (intfDirs != null && intfDirs.length > 0) {
                return true;
            }

        } 
        return false;
    }

    /**
     * <p>isValidRRDInterfaceDir</p>
     *
     * @param file a {@link java.io.File} object.
     * @return a boolean.
     */
    public static final boolean isValidRRDInterfaceDir(File file) {
        if (!file.isDirectory()) {
            return false;
        }

        File[] intfRRDs = file.listFiles(RRD_FILENAME_FILTER);

        if (intfRRDs != null && intfRRDs.length > 0) {
            return true;
        }

        return false;
    }

    /**
     * Determines if the provided File object represents a valid RRD latency
     * directory.
     *
     * @param file a {@link java.io.File} object.
     * @return a boolean.
     */
    public static final boolean isValidRRDLatencyDir(File file) {
        if (!file.isDirectory()) {
            return false;
        }

        // if the directory contains RRDs, then it is queryable
        File[] nodeRRDs = file.listFiles(RRD_FILENAME_FILTER);
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
    public static boolean isValidRRDName(String rrd) {
        if (rrd == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int length = rrd.length();

        if (length > MAX_RRD_FILENAME_LENGTH) {
            return false;
        }

        // cannot contain references to higher directories for security's sake
        if (rrd.indexOf("..") >= 0) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            char c = rrd.charAt(i);

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
    public static String convertToValidRrdName(String rrd) {
        if (rrd == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer buffer = new StringBuffer(rrd);

        // truncate after the max length
        if (rrd.length() > MAX_RRD_FILENAME_LENGTH) {
            buffer.setLength(MAX_RRD_FILENAME_LENGTH - 1);
        }

        int length = buffer.length();

        for (int i = 0; i < length; i++) {
            char c = buffer.charAt(i);

            if (!(('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9') || (c == '_') || (c == '.') || (c == '-') || (c == '/'))) {
                buffer.setCharAt(i, '_');
            }
        }

        return buffer.toString();
    }

    /**
     * <p>getRrdSuffix</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getRrdSuffix() {
        return RrdUtils.getExtension();
    }
}
