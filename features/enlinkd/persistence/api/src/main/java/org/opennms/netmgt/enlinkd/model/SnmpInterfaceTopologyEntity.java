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

import java.util.Optional;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ReadOnlyEntity;

@ReadOnlyEntity
public class SnmpInterfaceTopologyEntity {

    private final Integer id;
    private final Integer ifIndex;
    private final String ifName;
    private final String ifAlias;
    private final Long ifSpeed;
    private final Integer nodeId;

    public SnmpInterfaceTopologyEntity(
            Integer id,
            Integer ifIndex,
            String ifName,
            String ifAlias,
            Long ifSpeed,
            Integer nodeId){
        this.id=id;
        this.ifIndex=ifIndex;
        this.ifName=ifName;
        this.ifAlias=ifAlias;
        this.ifSpeed = ifSpeed;
        this.nodeId= nodeId;
    }

    public static SnmpInterfaceTopologyEntity create(OnmsSnmpInterface snmpInterface) {
        return new SnmpInterfaceTopologyEntity(
                snmpInterface.getId(),
                snmpInterface.getIfIndex(),
                snmpInterface.getIfName(),
                snmpInterface.getIfAlias(),
                snmpInterface.getIfSpeed(),
                Optional.ofNullable(snmpInterface.getNode()).map(OnmsNode::getId).orElse(null)
        );
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

    public Integer getIfIndex() {
        return ifIndex;
    }

    public String getIfName() {
        return ifName;
    }

    public String getIfAlias() {
        return ifAlias;
    }

    public Long getIfSpeed() {
        return ifSpeed;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    @Override
    public String toString() {
        return "SnmpInterfaceTopologyEntity{" +
                "id=" + id +
                ", ifIndex=" + ifIndex +
                ", ifName='" + ifName + '\'' +
                ", ifSpeed='" + ifSpeed + '\'' +
                ", nodeId=" + nodeId +
                '}';
    }
}
