/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.util.Date;
import java.util.Objects;

import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;

public class ApplicationStatusEntity {

    /** Helper class to create a key object to map to Alarm. */
    public static class Key {
        private final String nodeId;
        private final String serviceId;
        private final String ipAddress;

        public Key(String nodeId, String serviceId, String ipAddress) {
            this.nodeId = nodeId;
            this.serviceId = serviceId;
            this.ipAddress = ipAddress;
        }

        public Key(Integer nodeId, OnmsServiceType serviceType, InetAddress ipAddress) {
            this (String.valueOf(Objects.requireNonNull(nodeId)), String.valueOf(Objects.requireNonNull(serviceType).getId()), Objects.requireNonNull(ipAddress).toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            Key other = (Key)obj;
            boolean equals = Objects.equals(nodeId, other.nodeId) && Objects.equals(serviceId, other.serviceId) && Objects.equals(ipAddress, other.ipAddress);
            return equals;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId, serviceId, ipAddress);
        }
    }

    private final int serviceTypeId;
    private final int nodeId;
    private final InetAddress ipAddress;
    private final Date lastEventTime;
    private final OnmsSeverity severity;
    private long alarmCount;

    public ApplicationStatusEntity(Integer nodeId, InetAddress ipAddress, Integer serviceTypeId, Date lastEventTime, OnmsSeverity severity, Long alarmCount) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.lastEventTime = lastEventTime;
        this.severity = severity;
        this.alarmCount = alarmCount;
        this.serviceTypeId = serviceTypeId;
    }

    public Key getKey() {
        return new Key(String.valueOf(nodeId), String.valueOf(serviceTypeId), ipAddress.toString());
    }

    public OnmsSeverity getSeverity() {
        return severity;
    }

    public int getCount() {
        return (int) alarmCount;
    }
}
