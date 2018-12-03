/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.distributed.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
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
                    node.children.put(path.get(path.size() - 1), new Node(Optional.of(e.getValue())));
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
