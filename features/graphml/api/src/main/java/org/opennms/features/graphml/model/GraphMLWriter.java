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
package org.opennms.features.graphml.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.graphdrawing.graphml.DataType;
import org.graphdrawing.graphml.EdgeType;
import org.graphdrawing.graphml.GraphType;
import org.graphdrawing.graphml.GraphmlType;
import org.graphdrawing.graphml.KeyForType;
import org.graphdrawing.graphml.KeyType;
import org.graphdrawing.graphml.KeyTypeType;
import org.graphdrawing.graphml.NodeType;
import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists GraphML files.
 */
public class GraphMLWriter {
    private static final Logger LOG = LoggerFactory.getLogger(GraphMLWriter.class);

    public interface ProcessHook {
        void process(GraphML input, GraphmlType result);
    }

    private interface DataTypeAddCallback {
        void addData(DataType dataType);
    }

    public static void write(GraphML graphML, File file, ProcessHook... hooks) throws InvalidGraphException {
        GraphmlType graphmlType = convert(graphML);
        if (hooks != null) {
            for (ProcessHook eachHook : hooks) {
                eachHook.process(graphML, graphmlType);
            }
        }
        try {
            JaxbUtils.marshal(graphmlType, file);
        } catch (final IOException e) {
            LOG.error("Unable to write GraphML to {}", file, e);
            throw new InvalidGraphException(e);
        }
    }

    public static GraphmlType convert(GraphML graphML) throws InvalidGraphException {
        GraphmlType graphmlType = new GraphmlType();
        addProperties(graphmlType, KeyForType.GRAPHML, graphML, dataType -> graphmlType.getGraphOrData().add(dataType));

        for (GraphMLGraph eachGraph : graphML.getGraphs()) {
            GraphType graphType = new GraphType();
            graphType.setId(eachGraph.getId());
            addProperties(graphmlType, KeyForType.GRAPH, eachGraph, dataType -> graphType.getDataOrNodeOrEdge().add(dataType));

            for (GraphMLNode eachNode : eachGraph.getNodes()) {
                NodeType nodeType = new NodeType();
                nodeType.setId(eachNode.getId());
                graphType.getDataOrNodeOrEdge().add(nodeType);
                addProperties(graphmlType, KeyForType.NODE, eachNode, dataType -> nodeType.getDataOrPort().add(dataType));
            }

            for (GraphMLEdge eachEdge : eachGraph.getEdges()) {
                EdgeType edgeType = new EdgeType();
                edgeType.setId(eachEdge.getId());
                edgeType.setSource(eachEdge.getSource().getId());
                edgeType.setTarget(eachEdge.getTarget().getId());
                graphType.getDataOrNodeOrEdge().add(edgeType);
                addProperties(graphmlType, KeyForType.EDGE, eachEdge, dataType -> edgeType.getData().add(dataType));
            }

            graphmlType.getGraphOrData().add(graphType);
        }
        return graphmlType;
    }

    private static void addProperties(GraphmlType graphmlType, KeyForType keyForType, GraphMLElement element, DataTypeAddCallback callback) throws InvalidGraphException {
        for (Map.Entry<String, Object> eachEntry : element.getProperties().entrySet()) {
            if (eachEntry.getKey().equals(GraphMLElement.ID)) { // skip IDs
                continue;
            }
            List<KeyType> definedKeys = graphmlType.getKey().stream()
                    .filter(eachKey -> eachKey.getFor() == keyForType)
                    .filter(eachKey -> eachKey.getId().equals(eachEntry.getKey()))
                    .collect(Collectors.toList());
            if (definedKeys.isEmpty()) {
                KeyType keyType = new KeyType();
                keyType.setFor(keyForType);
                keyType.setId(eachEntry.getKey());
                keyType.setAttrName(eachEntry.getKey());
                keyType.setAttrType(parseType(eachEntry.getValue()));
                graphmlType.getKey().add(keyType);
            }
            if (definedKeys.size() > 1) {
                throw new InvalidGraphException("Duplicate key found for id " + eachEntry.getKey());
            }

            DataType dataType = new DataType();
            dataType.setKey(eachEntry.getKey());
            dataType.setContent(String.valueOf(eachEntry.getValue()));

            callback.addData(dataType);
        }
    }

    private static KeyTypeType parseType(Object input) throws InvalidGraphException {
        if (input instanceof Boolean) {
            return KeyTypeType.BOOLEAN;
        }
        if (input instanceof Double) {
            return KeyTypeType.STRING;
        }
        if (input instanceof Float) {
            return KeyTypeType.FLOAT;
        }
        if (input instanceof Integer) {
            return KeyTypeType.INT;
        }
        if (input instanceof Long) {
            return KeyTypeType.LONG;
        }
        if (input instanceof String) {
            return KeyTypeType.STRING;
        }
        throw new InvalidGraphException("Input '" + input + "'not parseable");
    }
}
