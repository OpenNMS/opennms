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

import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Object to reference a node.
 * This can either be done by setting the <code>nodeId</code> or <code>foreignSource</code> AND <code>foreignId</code> fields.
 * If created from an OnmsNode all fields may be populated.
 *
 * Note: This should allow for easier node referencing,
 *       however it is no guarantee that the node referenced actually exists.
 *
 * @author mvrueden
 */
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

    public boolean matches(OnmsNode node) {
        Objects.requireNonNull(node);
        boolean match = Objects.equals(node.getId(), nodeId)
            || Objects.equals(node.getForeignSource(), foreignSource)
                && Objects.equals(node.getForeignId(), foreignId);
        return match;
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

    /**
     * If nodeId, foreignSource and foreignId are set,
     * this returns multiple variants: nodeId and foreignSource/foreignId node refs
     */
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

    /**
     * Creates a new {@link NodeRef} object from a string.
     * This can be a nodeId, but also a fs:fid string.
     *
     * @param nodeRefCriteria the criteria
     * @return the reference to a node
     */
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

    /**
     * Explicitly use foreignSource and foreignId to create a {@link NodeRef} object.
     */
    public static NodeRef from(String foreignSource, String foreignId) {
        return new NodeRef(foreignSource, foreignId);
    }

    /**
     * Creates a {@link NodeRef} with all fields populated
     */
    public static NodeRef from(int nodeId, String foreignSource, String foreignId) {
        return new NodeRef(nodeId, foreignSource, foreignId);
    }

    /**
     * Creates a {@link NodeRef} with only the <code>nodeId</code> field set.
     */
    public static NodeRef from(int nodeId) {
        return new NodeRef(nodeId);
    }

    /**
     * Creates {@link NodeRef}s from a {@link GenericVertex}.
     * If multiple properties are set, they are returned in the following order:
     *
     *  - reference defined by node id
     *  - reference defined by foreignSource/foreignId
     *  - reference defined by node criteria string
     */
    public static List<NodeRef> from(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        final Object nodeId = vertex.getProperty(GenericProperties.NODE_ID);
        final String foreignSource = vertex.getProperty(GenericProperties.FOREIGN_SOURCE);
        final String foreignId = vertex.getProperty(GenericProperties.FOREIGN_ID);
        final String nodeRef = vertex.getProperty(GenericProperties.NODE_CRITERIA);
        final List<NodeRef> nodeRefs = Lists.newArrayList();
        if (nodeId != null) {
            if (nodeId instanceof String) {
                nodeRefs.add(NodeRef.from((String) nodeId));
            } else if (nodeId instanceof Integer) {
                nodeRefs.add(NodeRef.from((Integer) nodeId));
            }
        }
        if (!Strings.isNullOrEmpty(foreignSource) && !Strings.isNullOrEmpty(foreignId)) {
            nodeRefs.add(NodeRef.from(foreignSource, foreignId));
        }
        if (!Strings.isNullOrEmpty(nodeRef)) {
            nodeRefs.add(NodeRef.from(nodeRef));
        }
        return nodeRefs;
    }
}
