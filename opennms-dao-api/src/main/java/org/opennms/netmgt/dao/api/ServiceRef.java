/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.OnmsIpInterface;

import com.google.common.base.MoreObjects;

/**
 * A immutable reference to an IP-service
 */
public class ServiceRef {
    private final int nodeId;
    private final InetAddress ipAddress;
    private final String serviceName;

    public ServiceRef(final int nodeId,
                   final InetAddress ipAddress,
                   final String serviceName) {
        this.nodeId = nodeId;
        this.ipAddress = Objects.requireNonNull(ipAddress);
        this.serviceName = Objects.requireNonNull(serviceName);
    }

    public static ServiceRef fromEvent(final IEvent event) {
        final int nodeId = event.getNodeid().intValue();
        final String ipAddress = event.getInterface();
        final String serviceName = event.getService();

        return new ServiceRef(nodeId, InetAddressUtils.addr(ipAddress), serviceName);
    }

    public static ServiceRef fromIpInterface(OnmsIpInterface ipInterface, String serviceName) {
        return new ServiceRef(ipInterface.getNodeId(), ipInterface.getIpAddress(), serviceName);
    }

    public int getNodeId() {
        return nodeId;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceRef)) {
            return false;
        }

        final ServiceRef service = (ServiceRef) o;
        return Objects.equals(this.nodeId, service.nodeId) &&
                Objects.equals(this.ipAddress, service.ipAddress) &&
                Objects.equals(this.serviceName, service.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nodeId, this.ipAddress, this.serviceName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nodeId", this.nodeId)
                .add("address", this.ipAddress)
                .add("serviceName", this.serviceName)
                .toString();
    }
}
