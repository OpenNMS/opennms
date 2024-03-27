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

import org.opennms.netmgt.model.ReadOnlyEntity;

import com.google.common.base.MoreObjects;

@ReadOnlyEntity
public class OspfLinkTopologyEntity {
    private final Integer id;
    private final Integer nodeId;
    private final InetAddress ospfIpAddr;
    private final InetAddress ospfIpMask;
    private final InetAddress ospfRemIpAddr;
    private final Integer ospfIfIndex;
    private final InetAddress ospfIfAreaId;

    public OspfLinkTopologyEntity(Integer id, Integer nodeId, InetAddress ospfIpAddr, InetAddress ospfIpMask, InetAddress ospfRemIpAddr, Integer ospfIfIndex, InetAddress ospfIfAreaId) {
        this.id = id;
        this.nodeId = nodeId;
        this.ospfIpAddr = ospfIpAddr;
        this.ospfIpMask = ospfIpMask;
        this.ospfRemIpAddr = ospfRemIpAddr;
        this.ospfIfIndex = ospfIfIndex;
        this.ospfIfAreaId = ospfIfAreaId;
    }

    public static OspfLinkTopologyEntity create (OspfLink link) {
        return new OspfLinkTopologyEntity(link.getId()
                , link.getNode().getId()
                , link.getOspfIpAddr()
                , link.getOspfIpMask()
                , link.getOspfRemIpAddr()
                , link.getOspfIfIndex()
                , link.getOspfIfAreaId());
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

    public InetAddress getOspfIpAddr() {
        return ospfIpAddr;
    }

    public InetAddress getOspfRemIpAddr() {
        return ospfRemIpAddr;
    }

    public Integer getOspfIfIndex() {
        return ospfIfIndex;
    }

    public InetAddress getOspfIfAreaId() {
        return ospfIfAreaId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("ospfIpAddr", ospfIpAddr)
                .add("ospfRemIpAddr", ospfRemIpAddr)
                .add("ospfIfIndex", ospfIfIndex)
                .add("ospfIfAreaId", ospfIfAreaId)
                .toString();
    }

    public InetAddress getOspfIpMask() {
        return ospfIpMask;
    }
}
