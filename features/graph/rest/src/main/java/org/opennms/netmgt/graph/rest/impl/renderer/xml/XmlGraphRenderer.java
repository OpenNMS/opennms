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

package org.opennms.netmgt.graph.rest.impl.renderer.xml;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXB;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLEdge;
import org.opennms.features.graphml.model.GraphMLElement;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.graphml.model.GraphMLWriter;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.netmgt.graph.api.GraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.rest.impl.renderer.GraphRenderer;

// TODO MVR graphml node id => namespace + id or only id
// TODO MVR graphml
public class XmlGraphRenderer implements GraphRenderer {

    @Override
    public String render(List<GraphContainerInfo> containerInfos) {
        final JaxbListWrapper<GraphContainerInfoDTO> list = new GraphContainerInfoDTOListWrapper();
        containerInfos.forEach(containerInfo -> {
            GraphContainerInfoDTO containerInfoDTO = new GraphContainerInfoDTO();
            containerInfoDTO.setId(containerInfo.getId());
            containerInfoDTO.setLabel(containerInfo.getLabel());
            containerInfoDTO.setDescription(containerInfo.getDescription());

            for (GraphInfo eachGraphInfo : containerInfo.getGraphInfos()) {
                GraphInfoDTO graphInfoDTO = new GraphInfoDTO();
                graphInfoDTO.setNamespace(eachGraphInfo.getNamespace());
                graphInfoDTO.setLabel(eachGraphInfo.getLabel());
                graphInfoDTO.setDescription(eachGraphInfo.getDescription());
                containerInfoDTO.getGraphs().add(graphInfoDTO);
            }
            list.add(containerInfoDTO);
        });

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JAXB.marshal(list, outputStream);
        return new String(outputStream.toByteArray());
    }

    // TODO MVR collection properties are not convertable to graphml :-/
    // TODO MVR verify id of all elements (see docs for this). vertex id (namespace + id) or just the id?
    @Override
    public String render(GraphContainer graphContainer) {
        // Container
        final GenericGraphContainer genericGraphContainer = graphContainer.asGenericGraphContainer();
        final GraphML graphML = new GraphML();
        graphML.setId(genericGraphContainer.getId());
        addProperties(genericGraphContainer.getProperties(), graphML);

        // Populate Graph and nodes
        genericGraphContainer.getGraphs().forEach(graph -> {
            final GenericGraph genericGraph = graph.asGenericGraph();
            final GraphMLGraph graphMLGraph = new GraphMLGraph();
            graphMLGraph.setId(genericGraph.getNamespace());
            addProperties(genericGraph.getProperties(), graphMLGraph);

            // Vertices
            genericGraph.getVertices().forEach(v -> {
                final GraphMLNode graphMLNode = new GraphMLNode();
                final GenericVertex genericVertex = v.asGenericVertex();
                graphMLNode.setId(v.getId());
                addProperties(genericVertex.getProperties(), graphMLNode);
                graphMLGraph.addNode(graphMLNode);
            });
            graphML.addGraph(graphMLGraph);
        });

        // Iterate again over all graphs and populate edges
        // It is done in separate steps, as edges may reference nodes from other graphs
        // To support this, all edges must be present.
        genericGraphContainer.getGraphs().forEach(graph -> {
            final GraphMLGraph graphMLGraph = getGraphByNamespace(graph.getNamespace(), graphML);
            final GenericGraph genericGraph = graph.asGenericGraph();
            genericGraph.getEdges().forEach(e -> {
                final GraphMLEdge graphMLEdge = new GraphMLEdge();
                final GenericEdge genericEdge = e.asGenericEdge();
                graphMLEdge.setId(e.getId());
                graphMLEdge.setSource(getNode(genericEdge.getSource().getId(), graphML));
                graphMLEdge.setTarget(getNode(genericEdge.getTarget().getId(), graphML));
                addProperties(genericEdge.getProperties(), graphMLEdge);
                graphMLGraph.addEdge(graphMLEdge);
            });
        });

        // Now convert to String
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            GraphMLWriter.write(graphML, outputStream);
        } catch (InvalidGraphException e) {
            throw new RuntimeException(e);
        }
        return new String(outputStream.toByteArray());

    }

    private static void addProperties(Map<String, Object> properties, GraphMLElement graphMLElement) {
        properties.keySet().forEach(eachKey -> {
            if (properties.get(eachKey) != null) {
                graphMLElement.setProperty(eachKey, properties.get(eachKey));
            }
        });
    }

    private static GraphMLGraph getGraphByNamespace(String namespace, GraphML graphML) {
        return graphML.getGraphs().stream()
                .filter(g -> namespace.equalsIgnoreCase(g.getProperty("namespace")))
                .findFirst().orElse(null);
    }

    private static GraphMLNode getNode(String id, GraphML graphMLContainer) {
        final Optional<GraphMLGraph> graphMLGraph = graphMLContainer.getGraphs().stream()
                .filter(g -> g.getNodeById(id) != null)
                .findFirst();
        if (graphMLGraph.isPresent()) {
            return graphMLGraph.get().getNodeById(id);
        }
        return null;
    }
}
