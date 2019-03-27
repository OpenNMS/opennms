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

package org.opennms.netmgt.graph.api.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.GraphContainer;
import org.opennms.netmgt.graph.api.info.GraphInfo;

// TODO MVR we must rework the generic and simple objects a bit more... that is kinda weird how it is implemented. The graph could work as an example but is also not fully defined yet
public class GenericGraphContainer implements GraphContainer<GenericVertex, GenericEdge, GenericGraph> {

    private List<GenericGraph> graphs = new ArrayList<>();
    private Map<String, Object> properties = new HashMap<>();

    @Override
    public List<GenericGraph> getGraphs() {
        return new ArrayList<>(graphs);
    }

    @Override
    public GenericGraph getGraph(String namespace) {
        return graphs.stream().filter(g -> g.getNamespace().equals(namespace)).findAny().orElse(null);
    }

    @Override
    public void addGraph(GenericGraph graph) {
        graphs.add(graph);
    }

    @Override
    public void removeGraph(String namespace) {
        graphs.stream().filter(g -> g.getNamespace().equals(namespace)).findAny().ifPresent(g -> graphs.remove(g));
    }

    @Override
    public GenericGraphContainer asGenericGraphContainer() {
        return this;
    }

    @Override
    public String getId() {
        return (String) properties.get(GenericProperties.ID);
    }

    public void setId(String id) {
        properties.put(GenericProperties.ID, id);
    }

    @Override
    public List<String> getNamespaces() {
        return graphs.stream().map(GenericGraph::getNamespace).collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return (String) properties.get(GenericProperties.DESCRIPTION);
    }

    public void setDescription(String description) {
        properties.put(GenericProperties.DESCRIPTION, description);
    }

    @Override
    public String getLabel() {
        return (String) properties.get(GenericProperties.LABEL);
    }

    public void setLabel(String label) {
        properties.put(GenericProperties.LABEL, label);
    }

    @Override
    public GraphInfo getGraphInfo(String namespace) {
        Objects.requireNonNull(namespace);
        return graphs.stream().filter(g -> g.getNamespace().equalsIgnoreCase(namespace)).findAny().orElse(null);
    }

    @Override
    public GraphInfo getPrimaryGraphInfo() {
        return graphs.get(0);
    }

    @Override
    public List<GraphInfo> getGraphInfos() {
        return new ArrayList<>(graphs);
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericGraphContainer that = (GenericGraphContainer) o;
        return Objects.equals(graphs, that.graphs) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(graphs, properties);
    }
}
