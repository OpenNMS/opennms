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
package org.opennms.netmgt.enlinkd.model;

import java.net.InetAddress;
import java.util.Optional;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.ReadOnlyEntity;

@ReadOnlyEntity
public class IpInterfaceTopologyEntity {
    private final Integer id;
    private final InetAddress ipAddress;
    private final InetAddress netMask;
    private final String isManaged;
    private final PrimaryType isSnmpPrimary;
    private final Integer nodeId;
    private final Integer snmpInterfaceId;

    public IpInterfaceTopologyEntity(Integer id,
            InetAddress ipAddress, InetAddress netMask, String isManaged, PrimaryType isSnmpPrimary, Integer nodeId,
            Integer snmpInterfaceId){
        this.id = id;
        this.ipAddress = ipAddress;
        this.netMask = netMask;
        this.isManaged = isManaged;
        this.isSnmpPrimary = isSnmpPrimary;
        this.nodeId = nodeId;
        this.snmpInterfaceId = snmpInterfaceId;
    }

    public IpInterfaceTopologyEntity(Integer id,
            InetAddress ipAddress, InetAddress netMask, String isManaged, String snmpPrimary, Integer nodeId,
            Integer snmpInterfaceId){
        this(id, ipAddress, netMask, isManaged, PrimaryType.get(snmpPrimary), nodeId, snmpInterfaceId);
    }

    public static IpInterfaceTopologyEntity create(OnmsIpInterface ipInterface) {
        return new IpInterfaceTopologyEntity(
                ipInterface.getId(),
                ipInterface.getIpAddress(),
                ipInterface.getNetMask(),
                ipInterface.getIsManaged(),
                ipInterface.getIsSnmpPrimary(),
                Optional.ofNullable(ipInterface.getNode()).map(OnmsNode::getId).orElse(null),
                Optional.ofNullable(ipInterface.getSnmpInterface()).map(OnmsSnmpInterface::getId).orElse(null));
    }

    public Integer getId() {
        return id;
    }

    public String getNodeIdAsString() {
        if (getNodeId() != null) {
            return getNodeId().toString();
        }
        return null;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public InetAddress getNetMask() {
        return netMask;
    }

    public String getIsManaged() {
        return isManaged;
    }

    public boolean isManaged() {
        return "M".equals(getIsManaged());
    }

    public char snmpPrimary() {
        return isSnmpPrimary.getCharCode();
    }

    public PrimaryType getIsSnmpPrimary() {
        return isSnmpPrimary;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Integer getSnmpInterfaceId() {
        return snmpInterfaceId;
    }

    @Override
    public String toString() {
        return "IpInterfaceTopologyEntity{" +
                "id=" + id +
                ", ipAddress=" + ipAddress +
                ", netMask=" + netMask +
                ", isManaged='" + isManaged + '\'' +
                ", isSnmpPrimary=" + isSnmpPrimary +
                ", nodeId=" + nodeId +
                ", snmpInterfaceId=" + snmpInterfaceId +
                '}';
    }
}
