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

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="resource")
public class QueryResource {
    @XmlAttribute @XmlID private String id;
    @XmlAttribute(name="parent-id") private String parentId;
    @XmlAttribute private String label;
    @XmlAttribute private String name;

    /** implemented below in getNodeId() because Jackson JSON doesn't honor @XmlIDREF */
    private QueryNode node;

    public QueryResource() {
        this.id = null;
        this.parentId = null;
        this.label = null;
        this.name = null;
        this.node = null;
    }

    public QueryResource(final String id, final String parentId, final String label, final String name, final QueryNode node) {
        this.id = id;
        this.parentId = parentId;
        this.label = label;
        this.name = name;
        this.node = node;
    }

    public String getId() {
        return this.id;
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getLabel() {
        return this.label;
    }

    public String getName() {
        return this.name;
    }

    public QueryNode getNode() {
        return this.node;
    }

    @XmlAttribute(name="node-id")
    public Integer getNodeId() {
        return this.node == null? null : this.node.getId();
    }

    public void setNodeId(final Integer id) {
        this.node = new QueryNode(id, null, null, null);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryResource other = (QueryResource) obj;

        return Objects.equals(this.id, other.id)
                && Objects.equals(this.parentId, other.parentId)
                && Objects.equals(this.label, other.label)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.node, other.node);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.parentId, this.label, this.name, this.node);
    }
    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("parentId", this.parentId)
                .add("label", this.label)
                .add("name", this.name)
                .add("node", this.node)
                .toString();
    }
}
