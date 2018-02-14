/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.graphml.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.graphdrawing.graphml.DataType;
import org.graphdrawing.graphml.EdgeType;
import org.graphdrawing.graphml.GraphType;
import org.graphdrawing.graphml.GraphmlType;
import org.graphdrawing.graphml.HyperedgeType;
import org.graphdrawing.graphml.KeyForType;
import org.graphdrawing.graphml.KeyType;
import org.graphdrawing.graphml.KeyTypeType;
import org.graphdrawing.graphml.NodeType;
import org.graphdrawing.graphml.PortType;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Reads GraphML files.
 *
 * @see
 */
public class GraphMLReader {

    private static final Logger LOG = LoggerFactory.getLogger(GraphMLReader.class);

    public static GraphML read(InputStream input) throws InvalidGraphException {
        return convert(JaxbUtils.unmarshal(GraphmlType.class, input));
    }

    public static GraphML convert(GraphmlType input) throws InvalidGraphException {
        final Map<String, GraphMLGraph> nodeIdToGraphMapping = new HashMap<>();
        final Map<String, KeyType> keyIdToTypeMapping = getKeyIdToTypeMapping(input.getKey());
        final List<GraphType> graphs = filter(input.getGraphOrData(), GraphType.class);

        final GraphML convertedGraphML = new GraphML();
        addProperties(convertedGraphML, null, keyIdToTypeMapping, filter(input.getGraphOrData(), DataType.class));

        for (GraphType eachGraph : graphs) {
            GraphMLGraph convertedGraph = new GraphMLGraph();
            addProperties(convertedGraph, eachGraph.getId(), keyIdToTypeMapping, filter(eachGraph.getDataOrNodeOrEdge(), DataType.class));

            // Nodes
            List<NodeType> nodes = filter(eachGraph.getDataOrNodeOrEdge(), NodeType.class);
            for (NodeType eachNode : nodes) {
                GraphMLNode convertedNode = new GraphMLNode();
                nodeIdToGraphMapping.put(eachNode.getId(), convertedGraph);
                addProperties(convertedNode, eachNode.getId(), keyIdToTypeMapping, filter(eachNode.getDataOrPort(), DataType.class));

                List<PortType> ports = filter(eachNode.getDataOrPort(), PortType.class);
                if (!ports.isEmpty()) {
                    LOG.warn("Ports are defined for node with id {} but ports are not supported. Ignoring {} defined ports", eachNode.getId(), ports.size());
                }
                convertedGraph.addNode(convertedNode);
            }
            convertedGraphML.addGraph(convertedGraph);
        }

        // Add Edges Last, as they may connect between graphs, and we just now know all graphs
        for (GraphType eachGraphType : graphs) {
            // Edges
            List<EdgeType> edges = filter(eachGraphType.getDataOrNodeOrEdge(), EdgeType.class);
            for (EdgeType eachEdge : edges) {
                GraphMLEdge convertedEdge = new GraphMLEdge();

                GraphMLGraph sourceGraph = nodeIdToGraphMapping.get(eachEdge.getSource());
                GraphMLGraph targetGraph = nodeIdToGraphMapping.get(eachEdge.getTarget());

                if (sourceGraph == null) {
                    throw new InvalidGraphException("No graph found for edge with id " + eachEdge.getSource());
                }
                if (targetGraph == null) {
                    throw new InvalidGraphException("No graph found for edge with id " + eachEdge.getTarget());
                }

                GraphMLNode sourceNode = sourceGraph.getNodeById(eachEdge.getSource());
                GraphMLNode targetNode = targetGraph.getNodeById(eachEdge.getTarget());

                convertedEdge.setSource(sourceNode);
                convertedEdge.setTarget(targetNode);

                addProperties(convertedEdge, eachEdge.getId(), keyIdToTypeMapping, filter(eachEdge.getData(), DataType.class));
                sourceGraph.addEdge(convertedEdge);
            }

            // Hyper Edges
            List<HyperedgeType> hyperEdges = filter(eachGraphType.getDataOrNodeOrEdge(), HyperedgeType.class);
            if (!hyperEdges.isEmpty()) {
                LOG.warn("Hyper Edges are defined for graph with id {} but are not supported. Ignoring {} defined hyper edges", eachGraphType.getId(), hyperEdges.size());
            }
        }
        validate(convertedGraphML);
        return convertedGraphML;
    }

    private static Map<String, KeyType> getKeyIdToTypeMapping(List<KeyType> key) throws InvalidGraphException {
        // Filter by id
        Map<String, List<KeyType>> keyTypeIdMap = new HashMap<>();
        for (KeyType eachKeyType : key) {
            keyTypeIdMap.putIfAbsent(eachKeyType.getId(), new ArrayList<>());
            keyTypeIdMap.get(eachKeyType.getId()).add(eachKeyType);
        }

        // Verify that all types are the same
        for (List<KeyType> eachList : keyTypeIdMap.values()) {
            if (eachList.stream().map(KeyType::getAttrType).collect(Collectors.toSet()).size() > 1) {
                throw new InvalidGraphException("Attribute Type of key with id " + eachList.get(0).getId() + " varies.");
            }
        }

        // Unify
        Map<String, KeyType> keyIdToTypeMapping = keyTypeIdMap.values().stream().map(list -> list.get(0)).collect(Collectors.toMap(KeyType::getId, Function.identity()));
        if (keyIdToTypeMapping.keySet().stream().filter(keyId -> keyId.equals("id")).findFirst().isPresent()) {
            throw new InvalidGraphException("Property with id cannot be defined");
        }
        return keyIdToTypeMapping;
    }

    private static <T> List<T> filter(List<?> inputList, Class<T> type) {
        return inputList.stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    private static void addProperties(GraphMLElement graphElement, String elementId, Map<String, KeyType> keyIdToTypeMapping, List<DataType> elementData) throws InvalidGraphException {
        // add defined properties
        for (DataType eachDataElement : elementData) {
            KeyType keyType = keyIdToTypeMapping.get(eachDataElement.getKey());
            if (keyType == null) {
                throw new InvalidGraphException("Accessing not existing attribute with key " + eachDataElement.getKey());
            }
            if (keyType.getAttrType() == null) {
                throw new InvalidGraphException("Key with id='" + keyType.getId() + "' and " +
                        "attribute name '" + keyType.getAttrName() + "' is null. " +
                        "This is usually caused by an invalid attribute type value. " +
                        "The following values are supported: " + Arrays.stream(KeyTypeType.values()).map(KeyTypeType::value).collect(Collectors.joining(", ")));
            }
            Object value = typeCastValue(eachDataElement.getContent(), keyType.getAttrType());
            graphElement.setProperty(keyType.getAttrName(), value);
        }

        // add default values if not already defined
        keyIdToTypeMapping.values().stream()
                .filter(keyType -> keyType.getDefault() != null)
                .filter(keyType -> !Strings.isNullOrEmpty(keyType.getDefault().getContent()))
                .filter(keyType -> graphElement.accept(new GraphMLElement.GraphMLElementVisitor<Boolean>() {
                    @Override
                    public Boolean visit(GraphMLGraph graph) {
                        return Lists.newArrayList(
                                KeyForType.ALL,
                                KeyForType.GRAPH,
                                KeyForType.GRAPHML).contains(keyType.getFor());
                    }

                    @Override
                    public Boolean visit(GraphMLNode node) {
                        return Lists.newArrayList(
                                KeyForType.ALL,
                                KeyForType.GRAPH,
                                KeyForType.GRAPHML,
                                KeyForType.NODE).contains(keyType.getFor());
                    }

                    @Override
                    public Boolean visit(GraphMLEdge edge) {
                        return Lists.newArrayList(
                                KeyForType.ALL,
                                KeyForType.GRAPH,
                                KeyForType.GRAPHML,
                                KeyForType.EDGE).contains(keyType.getFor());
                    }

                    @Override
                    public Boolean visit(GraphML graphML) {
                        return Lists.newArrayList(KeyForType.ALL, KeyForType.GRAPHML).contains(keyType.getFor());
                    }
                }))
                .forEach(keyType -> {
                    if (graphElement.getProperty(keyType.getAttrName()) == null) {
                        graphElement.setProperty(keyType.getAttrName(), typeCastValue(keyType.getDefault().getContent(), keyType.getAttrType()));
                    }
                });

        // add id as property
        if (elementId != null) {
            graphElement.setProperty(GraphMLElement.ID, elementId);
        }
    }

    private static Object typeCastValue(String value, KeyTypeType keyType) {
        switch (keyType) {
            case BOOLEAN:
                return Boolean.valueOf(value);
            case DOUBLE:
                return Double.valueOf(value);
            case FLOAT:
                return Float.valueOf(value);
            case INT:
                return Integer.valueOf(value);
            case LONG:
                return Long.valueOf(value);
            default:
            case STRING:
                return value;
        }
    }

    private static void validate(GraphML graphML) throws InvalidGraphException {
        Set<String> graphIds = new HashSet<>();

        for (GraphMLGraph eachGraph : graphML.getGraphs()) {
            if (graphIds.contains(eachGraph.getId())) {
                throw new InvalidGraphException("There already exists a graph with id " + eachGraph.getId());
            }
            graphIds.add(eachGraph.getId());

            Set<String> nodeIds = new HashSet<>();
            Set<String> edgeIds = new HashSet<>();

            for (GraphMLNode eachNode : eachGraph.getNodes()) {
                if (nodeIds.contains(eachNode.getId())) {
                    throw new InvalidGraphException("There already exists a node with id " + eachNode.getId()
                        + " in graph with id " + eachGraph.getId());
                }
                nodeIds.add(eachNode.getId());
            }
            for (GraphMLEdge eachEdge : eachGraph.getEdges()) {
                if (edgeIds.contains(eachEdge.getId())) {
                    throw new InvalidGraphException("There already exists an edge with id " + eachEdge.getId()
                        + " in graph with id " + eachGraph.getId());
                }
                edgeIds.add(eachEdge.getId());
            }
        }
    }
}
