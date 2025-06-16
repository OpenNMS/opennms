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

import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;

@XmlRootElement(name = "generic-type-resource")
@XmlAccessorType(XmlAccessType.NONE)
public class GenericTypeResourceDTO {

    @XmlElement(name = "node-level-resource")
    private NodeLevelResourceDTO parent;

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "fallback")
    private String fallback;

    @XmlAttribute(name = "instance")
    private String instance;

    @XmlAttribute(name = "timestamp")
    private Date timestamp;

    public GenericTypeResourceDTO() { }

    public GenericTypeResourceDTO(DeferredGenericTypeResource resource) {
        parent = new NodeLevelResourceDTO(resource.getParent());
        name = resource.getTypeName();
        fallback = resource.getFallbackTypeName();
        instance = resource.getInstance();
        timestamp = resource.getTimestamp();
    }

    @Override
    public String toString() {
        return String.format("GenericTypeResourceDTO[parent=%s, name=%s, fallback=%s, instance=%s]",
                parent, name, fallback, instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, name, fallback, instance, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof GenericTypeResourceDTO)) {
            return false;
        }
        GenericTypeResourceDTO other = (GenericTypeResourceDTO) obj;
        return Objects.equals(this.parent, other.parent)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.fallback, other.fallback)
                && Objects.equals(this.instance, other.instance)
                && Objects.equals(this.timestamp, other.timestamp);
    }

    public GenericTypeResource toResource() {
        final ResourceType resourceType = ResourceTypeMapper.getInstance().getResourceTypeWithFallback(name, fallback);
        if (resourceType == null) {
            throw new IllegalArgumentException(String.format("No resource type found with name '%s'!", name));
        }
        final GenericTypeResource resource = new GenericTypeResource(parent.toResource(), resourceType, instance);
        resource.setTimestamp(timestamp);
        return resource;
    }
}
