/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="metadata")
public class QueryMetadata {
    @XmlElementWrapper(name="resources")
    @XmlElement(name="resource")
    private final List<QueryResource> resources;

    @XmlElementWrapper(name="nodes")
    @XmlElement(name="node")
    private final Set<QueryNode> nodes;

    public QueryMetadata() {
        this.resources = null;
        this.nodes = null;
    }

    public QueryMetadata(final List<QueryResource> resources) {
        this.resources = resources;
        if (resources != null) {
            final Set<QueryNode> nodes = Sets.newTreeSet();
            for (final QueryResource resource : resources) {
                final QueryNode node = resource.getNode();
                if (node != null) {
                    nodes.add(node);
                }
            }
            this.nodes = nodes;
        } else {
            this.nodes = null;
        }
    }

    public List<QueryResource> getResources() {
        return this.resources == null? new ArrayList<QueryResource>() : this.resources;
    }

    public Set<QueryNode> getNodes() {
        return this.nodes == null? new HashSet<QueryNode>() : this.nodes;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryMetadata other = (QueryMetadata) obj;

        return Objects.equals(this.resources, other.resources);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.resources);
    }
    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("resources", this.resources)
                .add("nodes", this.nodes)
                .toString();
    }
}
