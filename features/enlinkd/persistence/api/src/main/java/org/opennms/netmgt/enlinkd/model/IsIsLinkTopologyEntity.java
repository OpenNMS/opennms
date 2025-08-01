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
public class IsIsLinkTopologyEntity {
    private final Integer id;
    private final Integer nodeId;
    private final Integer isisISAdjIndex;
    private final Integer isisCircIfIndex;
    private final String isisISAdjNeighSysID;
    private final String isisISAdjNeighSNPAAddress;


    public IsIsLinkTopologyEntity(Integer id, Integer nodeId, Integer isisISAdjIndex, Integer isisCircIfIndex, String isisISAdjNeighSysID,
                                  String isisISAdjNeighSNPAAddress){
        this.id = id;
        this.nodeId = nodeId;
        this.isisISAdjIndex = isisISAdjIndex;
        this.isisCircIfIndex = isisCircIfIndex;
        this.isisISAdjNeighSysID = isisISAdjNeighSysID;
        this.isisISAdjNeighSNPAAddress = isisISAdjNeighSNPAAddress;
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

    public Integer getIsisISAdjIndex() {
        return isisISAdjIndex;
    }

    public Integer getIsisCircIfIndex() {
        return isisCircIfIndex;
    }

    public String getIsisISAdjNeighSysID() {
        return isisISAdjNeighSysID;
    }

    public String getIsisISAdjNeighSNPAAddress() {
        return isisISAdjNeighSNPAAddress;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("isisISAdjIndex", isisISAdjIndex)
                .add("isisCircIfIndex", isisCircIfIndex)
                .add("isisISAdjNeighSysID", isisISAdjNeighSysID)
                .add("isisISAdjNeighSNPAAddress", isisISAdjNeighSNPAAddress)
                .toString();
    }
}
