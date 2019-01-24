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

package org.opennms.features.graph.api.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.graph.api.AbstractGraph;
import org.opennms.features.graph.api.Graph;
import org.opennms.features.graph.api.aware.LocationAware;
import org.opennms.features.graph.api.aware.NodeAware;
import org.opennms.features.graph.api.info.GraphInfo;
import org.opennms.features.graph.api.info.NodeInfo;

// TODO MVR enforce namespace
public class GenericGraph extends AbstractGraph<GenericVertex, GenericEdge> implements Graph<GenericVertex, GenericEdge>, NodeAware, LocationAware {

    private class GenericGraphInfo implements GraphInfo<GenericVertex> {

        @Override
        public String getNamespace() {
            return (String) properties.get(GenericProperties.NAMESPACE);
        }

        @Override
        public String getDescription() {
            return (String) properties.get(GenericProperties.DESCRIPTION);
        }

        @Override
        public String getLabel() {
            return (String) properties.get(GenericProperties.LABEL);
        }

        @Override
        public Class<GenericVertex> getVertexType() {
            return GenericVertex.class;
        }
    }

    private Map<String, Object> properties;

    public GenericGraph() {
        this(new HashMap<>());
    }

    public GenericGraph(Map<String, Object> properties) {
        this.properties = Objects.requireNonNull(properties);
        this.graphInfo = new GenericGraphInfo();
    }

    //    @Override
//    public Vertex getVertex(NodeRef nodeRef) {
//        return nodeRefMap.get(nodeRef);
//    }


    // TODO MVR implement me
//    @Override
//    public NodeRef getNodeRef() {
//        String nodeId = getProperty(GenericProperties.NODE_ID);
//        String foreignSource = getProperty(GenericProperties.FOREIGN_SOURCE);
//        String foreignId = getProperty(GenericProperties.FOREIGN_ID);
//        if (nodeId != null) {
//            return NodeRefs.from(nodeId);
//        } else if (foreignSource != null && foreignId != null) {
//            return NodeRefs.from(foreignSource + ":" + foreignId);
//        }
//        return null;
//    }

    @Override
    public NodeInfo getNodeInfo() {
        return null;
//        final Optional<Object> first = graphProperties.getProperties().values().stream().filter(v -> v instanceof NodeInfo).findFirst();
//        if (first.isPresent()) {
//            return (NodeInfo) first.get();
//        }
//        return (NodeInfo) graphProperties.getComputedProperties().values().stream().filter(v -> v instanceof NodeInfo).findFirst().orElse(null);
    }


    @Override
    public GenericGraph asGenericGraph() {
        return this;
    }


    @Override
    public String getLocation() {
        return null;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
}
