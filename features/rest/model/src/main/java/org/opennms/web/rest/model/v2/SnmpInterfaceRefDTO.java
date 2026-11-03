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
package org.opennms.web.rest.model.v2;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

/** An SNMP interface by node and ifIndex. */
@XmlRootElement(name = "snmpInterfaceRef")
@Schema(description = "An SNMP interface identified by its node and ifIndex.")
public class SnmpInterfaceRefDTO {

    private Integer nodeId;
    private Integer ifIndex;

    public SnmpInterfaceRefDTO() {
    }

    public SnmpInterfaceRefDTO(final Integer nodeId, final Integer ifIndex) {
        this.nodeId = nodeId;
        this.ifIndex = ifIndex;
    }

    @XmlElement(name = "nodeId")
    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(final Integer nodeId) {
        this.nodeId = nodeId;
    }

    @XmlElement(name = "ifIndex")
    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(final Integer ifIndex) {
        this.ifIndex = ifIndex;
    }
}
