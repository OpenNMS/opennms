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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.validation.GraphContainerIdValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class GenericGraphContainer implements ImmutableGraphContainer<GenericGraph> {

    private final List<GenericGraph> graphs;
    private final Map<String, Object> properties;

    private GenericGraphContainer(GenericGraphContainerBuilder builder) {
        this.properties = ImmutableMap.copyOf(builder.properties);
        this.graphs = ImmutableList.copyOf(builder.graphs.values().stream().sorted(Comparator.comparing(GenericGraph::getNamespace)).collect(Collectors.toList()));
        new GraphContainerIdValidator().validate(getId());
    }
    
    @Override
    public List<GenericGraph> getGraphs() {
        return new ArrayList<>(graphs);
    }

    @Override
    public GenericGraph getGraph(String namespace) {
        return graphs.stream().filter(g -> g.getNamespace().equals(namespace)).findAny().orElse(null);
    }

    @Override
    public GenericGraphContainer asGenericGraphContainer() {
        return this;
    }

    @Override
    public String getId() {
        return (String) properties.get(GenericProperties.ID);
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

    @Override
    public GraphInfo getGraphInfo(String namespace) {
        Objects.requireNonNull(namespace);
        return graphs.stream().filter(g -> g.getNamespace().equals(namespace)).findAny().orElse(null);
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
        return properties;
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
    
    public static GenericGraphContainerBuilder builder() {
        return new GenericGraphContainerBuilder();
    }
    
    public static class GenericGraphContainerBuilder {

        private final static Logger LOG = LoggerFactory.getLogger(GenericGraphContainerBuilder.class);

        // allow graphs to be replaced in builder : use a Map
        private final Map<String, GenericGraph> graphs = new HashMap<>();
        private final Map<String, Object> properties = new HashMap<>();
        
        private GenericGraphContainerBuilder() {}
        
        public GenericGraphContainerBuilder id(String id) {
            property(GenericProperties.ID, id);
            return this;
        }
        
        public GenericGraphContainerBuilder label(String label){
            property(GenericProperties.LABEL, label);
            return this;
        }
        
        public GenericGraphContainerBuilder description(String description) {
            property(GenericProperties.DESCRIPTION, description);
            return this;
        }

        public GenericGraphContainerBuilder property(String name, Object value){
            if(name == null || value == null) {
                LOG.debug("Property name ({}) or value ({}) is null => ignoring it.", name, value);
                return this;
            }
            if (GenericProperties.ID.equals(name)) {
                new GraphContainerIdValidator().validate((String) value);
            }
            properties.put(name, value);
            return this;
        }
        
        public GenericGraphContainerBuilder properties(Map<String, Object> properties){
            Objects.requireNonNull(properties, "properties cannot be null");
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                property(entry.getKey(), entry.getValue());
            }
            return this;
        }
        
        public GenericGraphContainerBuilder applyContainerInfo(GraphContainerInfo containerInfo) {
            this.id(containerInfo.getId());
            this.label(containerInfo.getLabel());
            this.description(containerInfo.getDescription());
            return this;
        }
        
        public GenericGraphContainerBuilder addGraph(GenericGraph graph) {
            Objects.requireNonNull(graph, "Graph cannot be null");
            graphs.put(graph.getNamespace(), graph);
            return this;
        }
        
        public GenericGraphContainer build() {
            return new GenericGraphContainer(this);
        }
    }
}
