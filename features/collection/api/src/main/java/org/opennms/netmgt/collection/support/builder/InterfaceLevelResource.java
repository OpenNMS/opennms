/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.collection.adapters.InterfaceLevelResourceAdapter;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.model.ResourcePath;

@XmlJavaTypeAdapter(InterfaceLevelResourceAdapter.class)
public class InterfaceLevelResource extends AbstractResource {

    private NodeLevelResource m_node;
    private String m_ifName;

    public InterfaceLevelResource(NodeLevelResource node, String ifName) {
        m_node = node;
        m_ifName = ifName;
    }

    public String getIfName() {
        return m_ifName;
    }

    @Override
    public NodeLevelResource getParent() {
        return m_node;
    }

    @Override
    public String getInstance() {
        return m_ifName;
    }

    @Override
    public String getLabel(CollectionResource resource) {
        return m_ifName;
    }

    @Override
    public ResourcePath getPath(CollectionResource resource) {
        return ResourcePath.get(getIfName());
    }

    @Override
    public String getTypeName() {
        return CollectionResource.RESOURCE_TYPE_IF;
    }

    @Override
    public String toString() {
        return String.format("InterfaceLevelResource[node=%s, ifName=%s]", m_node, m_ifName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_node, m_ifName, getTimestamp());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof InterfaceLevelResource)) {
            return false;
        }
        InterfaceLevelResource other = (InterfaceLevelResource) obj;
        return Objects.equals(this.m_node, other.m_node)
                && Objects.equals(this.m_ifName, other.m_ifName)
                && Objects.equals(this.getTimestamp(), other.getTimestamp());
    }
}
