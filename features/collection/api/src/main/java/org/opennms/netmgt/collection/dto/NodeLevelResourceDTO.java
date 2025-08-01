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
