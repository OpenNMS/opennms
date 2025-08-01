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
