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
