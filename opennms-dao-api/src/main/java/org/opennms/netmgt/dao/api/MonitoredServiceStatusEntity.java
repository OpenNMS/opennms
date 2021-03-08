/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import java.util.Date;

import org.opennms.netmgt.model.OnmsSeverity;

public class MonitoredServiceStatusEntity {
    private final int nodeId;
    private final Date lastEventTime;
    private final OnmsSeverity severity;
    private final long alarmCount;
    private final InetAddress ipAddress;
    private final int serviceTypeId;

    public MonitoredServiceStatusEntity(final int nodeId, final InetAddress ipAddress, final int serviceTypeId,
                                        final Date lastEventTime, final OnmsSeverity severity, final long alarmCount) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.serviceTypeId = serviceTypeId;
        this.lastEventTime = lastEventTime;
        this.severity = severity;
        this.alarmCount = alarmCount;
    }

    public OnmsSeverity getSeverity() {
        return severity;
    }

    public long getCount() {
        return alarmCount;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getServiceTypeId() {
        return serviceTypeId;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }
}
