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

import org.opennms.netmgt.model.ReadOnlyEntity;

import com.google.common.base.MoreObjects;

@ReadOnlyEntity
public class CdpLinkTopologyEntity {

    private final Integer id;
    private final Integer nodeId;
    private final Integer cdpCacheIfIndex;
    private final String cdpInterfaceName;
    private final String cdpCacheAddress;
    private final String cdpCacheDeviceId;
    private final String cdpCacheDevicePort;

    public CdpLinkTopologyEntity(Integer id, Integer nodeId, Integer cdpCacheIfIndex, String cdpInterfaceName, String cdpCacheAddress,
                                 String cdpCacheDeviceId, String cdpCacheDevicePort){
        this.id = id;
        this.nodeId = nodeId;
        this.cdpCacheIfIndex = cdpCacheIfIndex;
        this.cdpInterfaceName = cdpInterfaceName;
        this.cdpCacheAddress = cdpCacheAddress;
        this.cdpCacheDeviceId = cdpCacheDeviceId;
        this.cdpCacheDevicePort = cdpCacheDevicePort;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public String getNodeIdAsString() {
        if (getNodeId() != null) {
            return getNodeId().toString();
        }
        return null;
    }

    public Integer getCdpCacheIfIndex() {
        return cdpCacheIfIndex;
    }

    public String getCdpInterfaceName() {
        return cdpInterfaceName;
    }

    public String getCdpCacheAddress() {
        return cdpCacheAddress;
    }

    public String getCdpCacheDevicePort() {
        return cdpCacheDevicePort;
    }

    public String getCdpCacheDeviceId() {
        return cdpCacheDeviceId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("cdpCacheIfIndex", cdpCacheIfIndex)
                .add("cdpInterfaceName", cdpInterfaceName)
                .add("cdpCacheAddress", cdpCacheAddress)
                .add("cdpCacheDeviceId", cdpCacheDeviceId)
                .add("cdpCacheDevicePort", cdpCacheDevicePort)
                .toString();
    }
}
