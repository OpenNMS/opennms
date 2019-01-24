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

package org.opennsm.features.graphs.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.graph.api.Graph;
import org.opennms.features.graph.api.GraphContainer;
import org.opennms.features.graph.api.generic.GenericGraphContainer;
import org.opennms.features.graph.api.info.GraphInfo;

// TODO MVR probably implement ContainerGraphInfo instead of a property
// TODO MVR make more type safe
public class SimpleGraphContainer implements GraphContainer {

    private final String id;
    private String description;
    private String label;
    private List<Graph<?,?>> graphs = new ArrayList<>();

    public SimpleGraphContainer(String containerId) {
        this.id = Objects.requireNonNull(containerId);
    }

    @Override
    public List<Graph<?, ?>> getGraphs() {
        return graphs;
    }

    @Override
    public Graph<?, ?> getGraph(String namespace) {
        return graphs.stream().filter(g -> g.getNamespace().equalsIgnoreCase(namespace)).findFirst().orElse(null);
    }

    @Override
    public void addGraph(Graph graph) {
        this.graphs.add(graph);
    }

    @Override
    public void removeGraph(String namespace) {
        graphs.stream()
                .filter(g -> g.getNamespace().equalsIgnoreCase(namespace))
                .findAny()
                .ifPresent(graph -> this.graphs.remove(graph));
    }

    @Override
    public List<String> getNamespaces() {
        return graphs.stream().map(graph -> graph.getNamespace()).collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public GraphInfo getGraphInfo(String namespace) {
        Objects.requireNonNull(namespace);
        return graphs.stream().filter(gi -> namespace.equalsIgnoreCase(gi.getNamespace())).findFirst().orElse(null);
    }

    @Override
    public GraphInfo getPrimaryGraphInfo() {
        return graphs.get(0);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    // TODO MVR do we really want to use these methods?
    public SimpleGraph g(String namespace) {
        final SimpleGraph simpleGraph = new SimpleGraph(namespace);
        addGraph(simpleGraph);
        return simpleGraph;
    }

    @Override
    public GenericGraphContainer asGenericGraphContainer() {
        final GenericGraphContainer genericGraphContainer = new GenericGraphContainer();
        genericGraphContainer.setId(id);
        genericGraphContainer.setDescription(description);
        genericGraphContainer.setLabel(label);
        getGraphs().forEach(g -> genericGraphContainer.addGraph(g.asGenericGraph()));
        return genericGraphContainer;
    }
}
