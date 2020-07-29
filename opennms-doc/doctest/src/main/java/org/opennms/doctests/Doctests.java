/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.doctests;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.opennms.doctests.model.Component;
import org.opennms.doctests.model.Listing;
import org.opennms.doctests.model.Sequence;
import org.opennms.doctests.model.Snippet;
import org.opennms.doctests.model.executions.KarafExecution;
import org.opennms.doctests.model.executions.ShellExecution;
import org.opennms.doctests.model.preparations.FilePreparation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;

public class Doctests {
    public static final Logger LOG = LoggerFactory.getLogger(Doctests.class);

    private static final Pattern TARGET_FILE_PATTERN = Pattern.compile("file:(?<path>.+)");
    private static final Pattern TARGET_KARAF_PATTERN = Pattern.compile("karaf:(?<system>.+)");
    private static final Pattern TARGET_SHELL_PATTERN = Pattern.compile("shell:(?<system>.+)");

    private final Set<Sequence> sequences;

    private Doctests(final Set<Sequence> sequences) {
        this.sequences = Objects.requireNonNull(sequences);
    }

    public static Doctests load(final String path) throws IOException {
        return Doctests.load(FileSystems.getDefault().getPath(path));
    }

    public static Doctests load(final Path path) throws IOException {
        final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        // Load the whole document as AST
        final Document document = asciidoctor.loadFile(path.toFile(),
                                                       OptionsBuilder.options()
                                                                     .safe(SafeMode.SAFE)
                                                                     .sourcemap(true)
                                                                     .asMap());

        // Find all listing sections and grouping together listings with shared IDs
        final Map<String, List<Listing>> listings = document.findBy(ImmutableMap.builder()
                                                                                .put("context", ":listing")
                                                                                .build()).stream().flatMap(node -> {
            // Listings are assumed to be block
            final Block block;
            if (node instanceof Block) {
                block = (Block) node;
            } else {
                LOG.debug("Ignoring listing which is no block ({})", node.getClass());
                return Stream.empty();
            }

            // Target must be specified or listing will be ignored
            return getNodeAttr(block, "doctest-target")
                    .map(target -> {
                        // Read details from listing
                        // TODO: Capture title?
                        return Listing.builder()
                                      .withFile(block.getSourceLocation().getPath())
                                      .withLine(block.getSourceLocation().getLineNumber())
                                      .withContent(block.getSource())
                                      .withTarget(target)
                                      .withId(getNodeAttr(block, "doctest-id")
                                                      .orElseGet(() -> generateId(block)))
                                      .withDependencies(getNodeAttr(block, "doctest-deps")
                                                                .map(deps -> Splitter.on(",").trimResults().splitToList(deps))
                                                                .orElseGet(Collections::emptyList))
                                      .withComponents(getNodeAttr(block, "doctest-components")
                                                              .map(deps -> Splitter.on(",").trimResults().splitToList(deps))
                                                              .orElseGet(Collections::emptyList))
                                      .build();
                    })
                    .map(Stream::of)
                    .orElseGet(Stream::empty);
        }).collect(Collectors.groupingBy(Listing::getId));

        // Create dependency graph
        final MutableGraph<String> dependencies = GraphBuilder.directed()
                                                              .allowsSelfLoops(false)
                                                              .build();
        for (final Map.Entry<String, List<Listing>> group : listings.entrySet()) {
            dependencies.addNode(group.getKey());
            for (final Listing listing : group.getValue()) {
                for (final String dependency : listing.getDependencies()) {
                    dependencies.putEdge(group.getKey(), dependency);
                }
            }
        }

        // Build testing sequences by finding all leafs in the dependency graph and walk down to the sub-graphs root
        final Stream<String> leafs = dependencies.nodes().stream()
                                                 .filter(key -> dependencies.predecessors(key).isEmpty());

        final Set<Sequence> sequences = leafs.map(key -> {
            final Sequence.Builder sequence = Sequence.builder(key);

            for (final String s : Traverser.forGraph(dependencies).breadthFirst(key)) {
                final List<Listing> group = listings.get(s);
                for (final Listing listing : group) {
                    sequence.withSnippet(buildSnippet(listing));
                }
            }

            return sequence.build();
        }).collect(Collectors.toSet());

        return new Doctests(sequences);
    }

    private static Optional<String> getNodeAttr(final ContentNode node, final String key) {
        if (node.hasAttribute(key)) {
            final Object value = node.getAttribute(key);
            return Optional.ofNullable(value).map(Object::toString);
        } else {
            return Optional.empty();
        }
    }

    private static String generateId(final StructuralNode node) {
        return String.format("%s_%s", node.getSourceLocation().getPath().replaceAll("\\/", "_"), node.getSourceLocation().getLineNumber());
    }

    private static Snippet buildSnippet(final Listing listing) {
        final Snippet.Builder<?> builder;

        Matcher matcher;
        if ((matcher = TARGET_FILE_PATTERN.matcher(listing.getTarget())).matches()) {
            builder = FilePreparation.builder()
                                     .withPath(FileSystems.getDefault().getPath(matcher.group("path")));
        } else if ((matcher = TARGET_KARAF_PATTERN.matcher(listing.getTarget())).matches()) {
            builder = KarafExecution.builder()
                                    .withSystem(matcher.group("system"));
        } else if ((matcher = TARGET_SHELL_PATTERN.matcher(listing.getTarget())).matches()) {
            builder = ShellExecution.builder()
                                    .withSystem(matcher.group("system"));
        } else {
            throw new IllegalArgumentException("Invalid target: " + listing.getTarget());
        }

        // Replace occurrences of `${OPENNMS_HOME}` in content
        // TODO: Is there code already doing this? More things to replace?
        final String content = listing.getContent()
                                      .replaceAll("\\$\\{OPENNMS_HOME}", "/opt/opennms");

        final List<Component> components = listing.getComponents().stream()
                                                  .map(component -> Component.valueOf(component.toUpperCase()))
                                                  .collect(Collectors.toList());

        return builder
                .withFile(FileSystems.getDefault().getPath(listing.getFile()))
                .withLine(listing.getLine())
                .withContent(content)
                .withId(listing.getId())
                .withComponents(components)
                .build();

    }

    public void execute() throws Exception {
        for (final Sequence sequence : this.sequences) {
            sequence.execute();
        }
    }
}
