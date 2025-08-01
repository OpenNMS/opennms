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

import java.io.Serializable;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ReadOnlyEntity;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

import com.google.common.base.MoreObjects;

@ReadOnlyEntity
public class NodeTopologyEntity implements Serializable {

    private final Integer id;
    private final OnmsNode.NodeType type;
    private final String sysObjectId;
    private final String label;
    private final String location;

    public NodeTopologyEntity(Integer nodeid, OnmsNode.NodeType nodetype, String nodesysoid, String nodelabel, String location){
        this.id = nodeid;
        this.type = nodetype;
        this.sysObjectId = nodesysoid;
        this.label = nodelabel;
        this.location = location;
    }

    public NodeTopologyEntity(Integer id, OnmsNode.NodeType type, String sysObjectId, String label, OnmsMonitoringLocation location){
        this(id, type, sysObjectId, label, location.getLocationName());
    }

    public static NodeTopologyEntity toNodeTopologyInfo(OnmsNode node){
        return new NodeTopologyEntity(node.getId(), node.getType(), node.getSysObjectId(), node.getLabel(), node.getLocation().getLocationName());
    }

    public Integer getId() {
        return id;
    }

    public OnmsNode.NodeType getType() {
        return type;
    }


    public String getSysObjectId() {
        return sysObjectId;
    }


    public String getLabel() {
        return label;
    }


    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("type", type)
                .add("sysObjectId", sysObjectId)
                .add("label", label)
                .add("location", location)
                .toString();
    }
}