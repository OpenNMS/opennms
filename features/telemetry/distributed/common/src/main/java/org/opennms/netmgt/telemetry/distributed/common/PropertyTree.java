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
package org.opennms.netmgt.telemetry.distributed.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

/**
 * A tree representation for dot-separated properties.
 * The tree consists of nodes, where each node can have a value.
 * Each node can have an arbitrary number of child nodes which are addressed by the node name.
 */
public class PropertyTree {

    /**
     * The node of the {@link PropertyTree}.
     */
    public static class Node {
        private final Optional<String> value;
        private final Map<String, Node> children;

        private Node(final Optional<String> value) {
            this.value = Objects.requireNonNull(value);
            this.children = Maps.newHashMap();
        }

        /**
         * Returns the value of this node.
         * @return the value or {@link Optional#empty()} if the node has no value associated
         */
        public Optional<String> getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("value", value)
                    .add("children", children)
                    .toString();
        }
    }

    private final Node root;

    private PropertyTree(final Node root) {
        this.root = root;
    }

    /**
     * Finds the node at the given path.
     *
     * @param path the path of the node to return split into its elements
     * @return the {@link Node} if there is a node on the given path or an {@link Optional#empty()} value if one of the nodes in the path does not exist
     */
    public Optional<Node> find(final Iterable<String> path) {
        Node node = this.root;
        for (final String p : path) {
            node = node.children.get(p);
            if (node == null) {
                return Optional.empty();
            }
        }

        return Optional.of(node);
    }

    /**
     * @see PropertyTree#find(Iterable)
     */
    public Optional<Node> find(final String... path) {
        return this.find(Arrays.asList(path));
    }

    /**
     * Get the value at the given path. The node is required to exist.
     *
     * @param path the path of the node to return split into its elements
     * @return the value of the node at the given path
     * @throws NoSuchElementException if there is no such node or the node has no value
     */
    public String getRequiredString(final String... path) {
        return this.find(path)
                .flatMap(Node::getValue)
                .orElseThrow(() -> new NoSuchElementException(String.format("%s must be set.", path)));
    }

    /**
     * Get the value at the given path parsed as an {@link Integer}.
     *
     * @param path the path of the node to return split into its elements
     * @return the {@link Integer} value of the node at the given path or {@link Optional#empty()} if one of the nodes in the path does not exist
     */
    public Optional<Integer> getOptionalInteger(final String... path) {
        return this.find(path)
                .flatMap(Node::getValue)
                .map(Integer::parseInt);
    }

    /**
     * Get the value at the given path parsed as an {@link Boolean}.
     *
     * @param path the path of the node to return split into its elements
     * @return the {@link Integer} value of the node at the given path or {@link Optional#empty()} if one of the nodes in the path does not exist
     */
    public Optional<Boolean> getOptionalBoolean(final String... path) {
        return this.find(path)
                .flatMap(Node::getValue)
                .map(Boolean::parseBoolean);
    }

    /**
     * Get the values of all children nodes at the given path. The values are mapped by the children node names.
     *
     * @param path the path of the node to return split into its elements
     * @return the {@link Map} from the children node names to its values. If one of the nodes in the path does not exist this will return an empty {@link Map}
     */
    public Map<String, String> getMap(final String... path) {
        return this.find(path)
                .map(node -> Maps.transformValues(node.children, c -> c.getValue().get()))
                .orElseGet(Collections::emptyMap);
    }

    /**
     * Get the values of all child nodes at the given path, enumerating any subtrees and converting their
     * paths into dot-separated property names. This is intended to handle parameter keys containing dots -
     * see NMS-12738.
     *
     * @param path the path of the node to return split into its elements
     * @return the {@link Map} from the children node names to its values. If one of the nodes in the path does not exist this will return an empty {@link Map}
     */

    public Map<String, String> getFlatMap(final String... path) {
        Map<String,String> outmap = new HashMap<>();
        this.find(path).ifPresent(n -> {
            for(Map.Entry<String, Node> e: n.children.entrySet()) {
                buildKeysRecursive(e.getValue(), e.getKey(), outmap);
            }
        });
        return outmap;
    }

    private static void buildKeysRecursive(Node n, String prefix, Map<String,String> m) {
        n.value.ifPresent(v -> {
            m.put(prefix, v);
        });

        // recurse to the leaves
        for(Map.Entry<String, Node> e: n.children.entrySet()) {
            buildKeysRecursive(e.getValue(), prefix + "." + e.getKey(), m);
        }
    }


    /**
     * Get the children nodes at the given path represented as sub-trees. The sub-trees are mapped by the children node names.
     * The resulting {@link Map} will contain an entry for each children of the addressed node. The name of the children will be used as key.
     * The value will be new {@link PropertyTree} instance in which the children node is used as root node.
     *
     * @param path the path of the node to return split into its elements
     * @return the {@link Map} from the children node names to its representations as sub-trees. If one of the nodes in the path does not exist this will return an empty {@link Map}
     */
    public Map<String, PropertyTree> getSubTrees(final String... path) {
        return this.find(path)
                .map(node -> Maps.transformValues(node.children, PropertyTree::new))
                .orElseGet(Collections::emptyMap);
    }

    /**
     * Create a new {@link PropertyTree} from the given {@link Map}.
     * The keys of the map are handled as full-qualified dot-separated paths.
     *
     * @param map the path / value mapping used to create the property tree
     * @return a new {@link PropertyTree} instance
     */
    public static PropertyTree from(final Map<String, String> map) {
        final Node root = new Node(Optional.empty());

        // We sort here so that a comes before a.a and the node with the value is created before it's child nodes
        map.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEachOrdered(e -> {
                    final List<String> path = Splitter.on('.').splitToList(e.getKey());
                    final Node node = ensure(root, path.subList(0, path.size() - 1));
                    node.children.put(path.get(path.size() - 1), new Node(Optional.of(e.getValue() == null ? null : e.getValue().trim())));
                });

        return new PropertyTree(root);
    }

    /**
     * Create a new {@link PropertyTree} from the given {@link Dictionary}.
     * The keys of the properties are handled as full-qualified dot-separated paths.
     *
     * @param properties the path / value mapping used to create the property tree
     * @return a new {@link PropertyTree} instance
     */
    public static PropertyTree from(final Dictionary<String, ?> properties) {
        return from(Maps.toMap(Iterators.forEnumeration(properties.keys()),
                key -> (String) properties.get(key)));
    }

    private static Node ensure(Node node, final Iterable<String> path) {
        for (final String p : path) {
            node = node.children.computeIfAbsent(p, (key) -> new Node(Optional.empty()));
        }
        return node;
    }
}
