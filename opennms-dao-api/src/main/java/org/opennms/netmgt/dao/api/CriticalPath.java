/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;

public class CriticalPath {

    private final String locationName;
    private final InetAddress ipAddress;
    private final String serviceName;

    public CriticalPath(String locationName, InetAddress ipAddress, String serviceName) {
        this.locationName = locationName;
        this.ipAddress = ipAddress;
        this.serviceName = serviceName;
    }

    public String getLocationName() {
        return locationName;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String toString() {
        return String.format("CriticalPath[locationName=%s, ipAddress=%s, serviceName=%s]",
                locationName, InetAddressUtils.str(ipAddress), serviceName);
    }
}
