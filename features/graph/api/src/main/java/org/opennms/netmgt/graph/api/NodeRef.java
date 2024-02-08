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
