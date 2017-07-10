/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.dto;

import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.collection.support.builder.NodeLevelResource;

@XmlRootElement(name = "node-level-resource")
@XmlAccessorType(XmlAccessType.NONE)
public class NodeLevelResourceDTO {

    @XmlAttribute(name = "node-id")
    private int nodeId;

    @XmlAttribute(name = "path")
    private String path;

    @XmlAttribute(name = "timestamp")
    private Date timestamp;

    public NodeLevelResourceDTO() { }

    public NodeLevelResourceDTO(NodeLevelResource resource) {
        nodeId = resource.getNodeId();
        timestamp = resource.getTimestamp();
        path = resource.getPath();
    }

    @Override
    public String toString() {
        return String.format("NodeLevelResourceDTO[nodeId=%d, path=%s]", nodeId, path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, path, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof NodeLevelResourceDTO)) {
            return false;
        }
        NodeLevelResourceDTO other = (NodeLevelResourceDTO) obj;
        return Objects.equals(this.nodeId, other.nodeId)
                && Objects.equals(this.path, other.path)
                && Objects.equals(this.timestamp, other.timestamp);
    }

    public NodeLevelResource toResource() {
        final NodeLevelResource resource = new NodeLevelResource(nodeId, path);
        resource.setTimestamp(timestamp);
        return resource;
    }
}
