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
