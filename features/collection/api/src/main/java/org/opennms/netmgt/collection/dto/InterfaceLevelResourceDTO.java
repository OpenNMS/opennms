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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;

@XmlRootElement(name = "interface-level-resource")
@XmlAccessorType(XmlAccessType.NONE)
public class InterfaceLevelResourceDTO {

    @XmlElement(name = "node-level-resource")
    private NodeLevelResourceDTO parent;

    @XmlAttribute(name = "if-name")
    private String ifName;

    @XmlAttribute(name = "timestamp")
    private Date timestamp;

    public InterfaceLevelResourceDTO() { }

    public InterfaceLevelResourceDTO(InterfaceLevelResource resource) {
        parent = new NodeLevelResourceDTO(resource.getParent());
        ifName = resource.getIfName();
        timestamp = resource.getTimestamp();
    }

    @Override
    public String toString() {
        return String.format("InterfaceLevelResourceDTO[parent=%s, ifName=%s]", parent, ifName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, ifName, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof InterfaceLevelResourceDTO)) {
            return false;
        }
        InterfaceLevelResourceDTO other = (InterfaceLevelResourceDTO) obj;
        return Objects.equals(this.parent, other.parent)
                && Objects.equals(this.ifName, other.ifName)
                && Objects.equals(this.timestamp, other.timestamp);
    }

    public InterfaceLevelResource toResource() {
        final InterfaceLevelResource resource = new InterfaceLevelResource(parent.toResource(), ifName);
        resource.setTimestamp(timestamp);
        return resource;
    }
}
