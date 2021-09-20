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
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.breadcrumbs.Breadcrumb;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

public class PathTree {

    private Node root = new Node();

    public PathTree() {
    }

    public void addPath(List<VertexRef> vertexRefs) {
        if (vertexRefs != null && !vertexRefs.isEmpty()) {
            Node parent = root;
            for (VertexRef eachRef : vertexRefs) {
                Node createdNode = parent.addChild(eachRef);
                parent = createdNode;
            }
        }
    }

    List<Node> getLeafs() {
        List<Node> leafs = Lists.newArrayList();
        collectLeafs(leafs, root);
        return leafs;
    }

    private static void collectLeafs(List<Node> leafs, Node node) {
        if (node.isLeaf()) {
            leafs.add(node);
        } else {
            for (Node eachChild : node.getChildren()) {
                collectLeafs(leafs, eachChild);
            }
        }
    }

    public void clear() {
        root = new Node();
    }

    public boolean isEmpty() {
        return root.getChildren().isEmpty();
    }

    public int getNumberOfPaths() {
        List<Node> leafs = getLeafs();
        return leafs.stream().map(Node::getParent).collect(Collectors.toSet()).size();
    }

    public List<Breadcrumb> toBreadcrumbs() {
        final List<Breadcrumb> breadcrumbList = Lists.newArrayList();

        // Build from bottom to top (including root)
        List<Node> work = getLeafs();
        while (!work.isEmpty() && !work.get(0).isRoot()) {
            // All working nodes must be on the same layer (resulting in the same namespace)
            String targetNamespace = work.get(0).getVertexRef().getNamespace();
            Set<Node> parentNodes = work.stream().map(Node::getParent).filter(Objects::nonNull).collect(Collectors.toSet());
            breadcrumbList.add(0, new Breadcrumb(targetNamespace, parentNodes.stream().map(Node::getVertexRef).filter(Objects::nonNull).collect(Collectors.toList())));
            work = Lists.newArrayList(parentNodes);
        }
        return breadcrumbList;
    }
}
