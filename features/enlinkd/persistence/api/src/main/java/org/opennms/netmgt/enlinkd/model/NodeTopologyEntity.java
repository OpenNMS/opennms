/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.model;

import java.io.Serializable;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/**
 * This is NOT a Hibernate/JPA entity but rather a lightweight model without less attributes than OnmsNode and no lazy
 * loading. We use it to retrieve node information from the database fast.
 */
public class NodeTopologyEntity implements Serializable {

    private Integer id;
    private OnmsNode.NodeType type;
    private String sysObjectId;
    private String label;
    private String location;

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

    public static NodeTopologyEntity toVertexInfo(OnmsNode node){
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
        return "NodeTopologyEntity{" +
                "id=" + id +
                ", type=" + type +
                ", sysObjectId='" + sysObjectId + '\'' +
                ", label='" + label + '\'' +
                ", location=" + location +
                '}';
    }
}
