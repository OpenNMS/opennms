/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.mock;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;

public abstract class MonitorTestUtils {

    public static MockMonitoredService getMonitoredService(int nodeId, InetAddress addr, String svcName) throws UnknownHostException {
        return new MockMonitoredService(nodeId, InetAddressUtils.str(addr), addr, svcName);
    }

    public static MockMonitoredService getMonitoredService(int nodeId, String hostname, String svcName) throws UnknownHostException {
        return getMonitoredService(nodeId, hostname, svcName, false);
    }

    public static MockMonitoredService getMonitoredService(int nodeId, String hostname, String svcName, boolean preferInet6Address) throws UnknownHostException {
        InetAddress myAddress = InetAddressUtils.resolveHostname(hostname, preferInet6Address);
        return new MockMonitoredService(nodeId, hostname, myAddress, svcName);
    }

}
