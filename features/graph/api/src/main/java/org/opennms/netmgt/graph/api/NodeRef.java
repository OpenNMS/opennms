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

package org.opennms.netmgt.graph.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NodeRef {

    private final Integer nodeId;
    private final String foreignSource;
    private final String foreignId;

    private NodeRef(int nodeId) {
        this.nodeId = nodeId;
        this.foreignSource = null;
        this.foreignId = null;
    }

    private NodeRef(String foreignSource, String foreignId) {
        this.nodeId = null;
        this.foreignSource = Objects.requireNonNull(foreignSource);
        this.foreignId = Objects.requireNonNull(foreignId);
    }

    private NodeRef(int nodeId, String foreignSource, String foreignId) {
        this.nodeId = nodeId;
        this.foreignSource = Objects.requireNonNull(foreignSource);
        this.foreignId = Objects.requireNonNull(foreignId);
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public String getForeignId() {
        return foreignId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final NodeRef nodeRef = (NodeRef) o;
        return Objects.equals(nodeId, nodeRef.nodeId)
                && Objects.equals(foreignSource, nodeRef.foreignSource)
                && Objects.equals(foreignId, nodeRef.foreignId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, foreignSource, foreignId);
    }

    // If nodeId, foreignSource and foreignId are set,
    // this returns multiple variants: nodeId and foreignSource/foreignId node refs
    public List<NodeRef> getVariants() {
        final List<NodeRef> variants = new ArrayList<>();
        if (getNodeId() != null) {
            variants.add(from(getNodeId()));
        }
        if (getForeignSource() != null && getForeignId() != null) {
            variants.add(from(getForeignSource(), foreignId));
        }
        return variants;
    }

    public static NodeRef from(String nodeRefCriteria) {
        if (nodeRefCriteria.contains(":")) {
            String[] criteria = nodeRefCriteria.split(":");
            return new NodeRef(criteria[0], criteria[1]);
        }
        try {
            final int nodeId = Integer.parseInt(nodeRefCriteria);
            return new NodeRef(nodeId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Provided criteria '" + nodeRefCriteria + "' is not valid. Cannot parse '" + nodeRefCriteria + "' to integer", ex);
        }
    }

    public static NodeRef from(String foreignSource, String foreignId) {
        return new NodeRef(foreignSource, foreignId);
    }

    public static NodeRef from(int nodeId, String foreignSource, String foreignId) {
        return new NodeRef(nodeId, foreignSource, foreignId);
    }

    public static NodeRef from(int nodeId) {
        return new NodeRef(nodeId);
    }
}
