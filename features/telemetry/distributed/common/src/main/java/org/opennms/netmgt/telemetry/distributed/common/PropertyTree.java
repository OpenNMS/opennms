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

// TODO fooker verify if this is used or if should be reverted to MapUtils instead. If this is kept, add javadoc and more error handling
public class PropertyTree {

    public static class Node {
        private final Optional<String> value;
        private final Map<String, Node> children;

        public Node(final Optional<String> value) {
            this.value = Objects.requireNonNull(value);
            this.children = Maps.newHashMap();
        }

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

    private PropertyTree(Optional<String> value) {
        this(new Node(value));
    }

    private PropertyTree() {
        this(Optional.empty());
    }

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

    public Optional<Node> find(final String... path) {
        return this.find(Arrays.asList(path));
    }

    public String getRequiredString(final String... path) {
        return this.find(path)
                .flatMap(Node::getValue)
                .orElseThrow(() -> new NoSuchElementException(String.format("%s must be set.", path)));
    }

    public Optional<Integer> getOptionalInteger(final String... path) {
        return this.find(path)
                .flatMap(Node::getValue)
                .map(Integer::parseInt);
    }

    public Map<String, String> getMap(final String... path) {
        return this.find(path)
                .map(node -> Maps.transformValues(node.children, c -> c.getValue().get()))
                .orElseGet(Collections::emptyMap);
    }

    public Map<String, PropertyTree> getSubTrees(final String... path) {
        return this.find(path)
                .map(node -> Maps.transformValues(node.children, PropertyTree::new))
                .orElseGet(Collections::emptyMap);
    }

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

    public static PropertyTree from(final Dictionary<String, ?> properties) {
        return from(Maps.toMap(Iterators.forEnumeration(properties.keys()),
                key -> (String)properties.get(key)));
    }

    private static Node ensure(Node node, final Iterable<String> path) {
        for (final String p : path) {
            node = node.children.computeIfAbsent(p, (key) -> new Node(Optional.empty()));
        }
        return node;
    }
}
