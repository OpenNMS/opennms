/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.info;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public class DefaultGraphInfo implements GraphInfo {

    private String namespace;
    private String description;
    private String label;

    public DefaultGraphInfo(final String namespace) {
        this.namespace = Objects.requireNonNull(namespace);
    }

    /**
     * Constructor to change the vertex Type of the given GraphInfo.
     */
    public DefaultGraphInfo(final GraphInfo copy) {
        this(Objects.requireNonNull(copy).getNamespace());
        setLabel(copy.getLabel());
        setDescription(copy.getDescription());
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DefaultGraphInfo withLabel(String label) {
        setLabel(label);
        return this;
    }

    public DefaultGraphInfo withDescription(String description) {
        setDescription(description);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGraphInfo graphInfo = (DefaultGraphInfo) o;
        return Objects.equals(namespace, graphInfo.namespace)
                && Objects.equals(description, graphInfo.description)
                && Objects.equals(label, graphInfo.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, description, label);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("namespace", namespace)
                .add("label", label)
                .add("description", description)
                .toString();
    }
}
