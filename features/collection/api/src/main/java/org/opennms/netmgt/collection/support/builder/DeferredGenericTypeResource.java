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

package org.opennms.netmgt.collection.support.builder;

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.collection.adapters.DeferredGenericTypeResourceAdapter;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.model.ResourcePath;

/**
 * A GenericTypeResource which defers the resource type definition
 * lookup until the collection resource is accessed.
 *
 * This allows generic resources to be constructed on the Minion,
 * by only referring to the type, without having to pass
 * the resource type definitions back and forth.
 *
 * @author jwhite
 */
@XmlJavaTypeAdapter(DeferredGenericTypeResourceAdapter.class)
public class DeferredGenericTypeResource extends AbstractResource {

    private final NodeLevelResource m_node;
    private final String m_resourceTypeName;
    private final String m_fallbackResourceTypeName;
    private final String m_instance;

    public DeferredGenericTypeResource(NodeLevelResource node, String resourceTypeName, String instance) {
        this(node, resourceTypeName, null, instance);
    }

    public DeferredGenericTypeResource(NodeLevelResource node, String resourceTypeName, String fallbackResourceTypeName, String instance) {
        m_node = Objects.requireNonNull(node, "node argument");
        m_resourceTypeName = Objects.requireNonNull(resourceTypeName, "resourceTypeName argument");
        m_fallbackResourceTypeName = fallbackResourceTypeName;
        m_instance = GenericTypeResource.sanitizeInstance(Objects.requireNonNull(instance, "instance argument"));
    }

    @Override
    public NodeLevelResource getParent() {
        return m_node;
    }

    @Override
    public String getTypeName() {
        return m_resourceTypeName;
    }

    public String getFallbackTypeName() {
        return m_fallbackResourceTypeName;
    }

    @Override
    public String getInstance() {
        return m_instance;
    }

    @Override
    public String getLabel(CollectionResource resource) {
        throw new UnsupportedOperationException("DeferredGenericTypeResource must be converted to GenericTypeResources before being used to retrieve the label.");
    }

    @Override
    public ResourcePath getPath(CollectionResource resource) {
        throw new UnsupportedOperationException("DeferredGenericTypeResource must be converted to GenericTypeResources before being used to build resource paths.");
    }

    @Override
    public Resource resolve() {
        final ResourceType resourceType = ResourceTypeMapper.getInstance().getResourceTypeWithFallback(m_resourceTypeName, m_fallbackResourceTypeName);
        if (resourceType == null) {
            throw new IllegalArgumentException(String.format("No resource type found with name '%s'!", m_resourceTypeName));
        }
        final GenericTypeResource resource = new GenericTypeResource(m_node, resourceType, m_instance);
        resource.setTimestamp(getTimestamp());
        return resource;
    }

    @Override
    public String toString() {
        return String.format("DeferredGenericTypeResource[node=%s, instance=%s, resourceTypeName=%s, fallbackResourceTypeName=%s]",
                m_node, m_instance, m_resourceTypeName, m_fallbackResourceTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_node, m_resourceTypeName, m_fallbackResourceTypeName, m_instance, getTimestamp());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof DeferredGenericTypeResource)) {
            return false;
        }
        DeferredGenericTypeResource other = (DeferredGenericTypeResource) obj;
        return Objects.equals(this.m_node, other.m_node)
                && Objects.equals(this.m_resourceTypeName, other.m_resourceTypeName)
                && Objects.equals(this.m_fallbackResourceTypeName, other.m_fallbackResourceTypeName)
                && Objects.equals(this.m_instance, other.m_instance)
                && Objects.equals(this.getTimestamp(), other.getTimestamp());
    }
}
