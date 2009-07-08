/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.daemon;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.ThreadCategory;

public class DaemonUtils {
    /**
     * No public constructor.  This has static methods only.
     */
    private DaemonUtils() {
    }

    public static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ThreadCategory.getInstance(DaemonUtils.class).warn("getLocalHostAddress: Could not lookup the host address for the local host machine, address set to 127.0.0.1: " + e, e);
            return "127.0.0.1";
        }
    }
    
    public static String getLocalHostName() {
        String localhost;
        try {
            localhost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            ThreadCategory.getInstance(DaemonUtils.class).warn("getLocalHostName: Could not lookup the host name for the local host machine, name set to 'localhost': " + e, e);
            localhost = "localhost";
        }
        return localhost;
    }


}
