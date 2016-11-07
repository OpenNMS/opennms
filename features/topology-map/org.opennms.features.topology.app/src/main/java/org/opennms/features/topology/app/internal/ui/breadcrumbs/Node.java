/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.ui.breadcrumbs;

import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

class Node {

    private final List<Node> children = Lists.newArrayList();

    private VertexRef vertexRef;

    private Node parent;

    public Node addChild(VertexRef vertexRef) {
        Objects.requireNonNull(vertexRef);
        // First see, if we already have that node
        for (Node eachChild : children) {
            if (eachChild.getVertexRef().equals(vertexRef)) {
                return eachChild;
            }
        }
        // We don't have the node, add it
        Node node = new Node();
        node.setVertexRef(vertexRef);
        node.setParent(this);
        children.add(node);
        return node;
    }

    public VertexRef getVertexRef() {
        return vertexRef;
    }

    public void setVertexRef(VertexRef vertexRef) {
        this.vertexRef = vertexRef;
    }

    public boolean isLeaf() {
        return !isRoot() && children.isEmpty();
    }

    boolean isRoot() {
        return parent == null;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
