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
