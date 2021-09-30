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
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "query-request")
@XmlAccessorType(XmlAccessType.NONE)
public class QueryNode implements Comparable<QueryNode> {
    @XmlAttribute private final Integer id;
    @XmlAttribute(name="foreign-source") private final String foreignSource;
    @XmlAttribute(name="foreign-id") private final String foreignId;
    @XmlAttribute private final String label;

    public QueryNode() {
        this.id = null;
        this.foreignSource = null;
        this.foreignId = null;
        this.label = null;
    }

    public QueryNode(final Integer id, final String foreignSource, final String foreignId, final String label) {
        this.id = id;
        this.foreignSource = foreignSource;
        this.foreignId = foreignId;
        this.label = label;
    }

    public Integer getId() {
        return id;
    }
    public String getForeignSource() {
        return foreignSource;
    }
    public String getForeignId() {
        return foreignId;
    }
    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryNode other = (QueryNode) obj;

        return Objects.equals(this.id, other.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("foreignSource", this.foreignSource)
                .add("foreignId", this.foreignId)
                .add("label", this.label)
                .toString();
    }

    @Override
    public int compareTo(final QueryNode other) {
        return this.getId() - other.getId();
    }
}
