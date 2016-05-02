/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SimpleEdgeProvider;
import org.opennms.features.topology.api.topo.SimpleVertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphML;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLGraph;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLNode;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLReader;
import org.opennms.features.topology.plugins.topo.graphml.model.InvalidGraphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphMLTopologyProvider extends AbstractTopologyProvider implements GraphProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GraphMLTopologyProvider.class);

    private File graphMLFile;

    public GraphMLTopologyProvider(String namespace) {
        super(namespace);
    }

    @Override
    public void refresh() {
        resetContainer();
        if (graphMLFile == null) {
            LOG.warn("No graph defined");
            return;
        }
        if (!graphMLFile.exists()) {
            LOG.warn("No graph found at location " + graphMLFile.toString());
            return;
        }
        try (InputStream input = new FileInputStream(graphMLFile)) {
            GraphML graphML = GraphMLReader.read(input);
            final String namespace = graphML.getNamespace();
            if (!getVertexNamespace().equals(namespace)) {
                LoggerFactory.getLogger(this.getClass()).info("Creating new vertex provider with namespace {}", namespace);
                m_vertexProvider = new SimpleVertexProvider(namespace);
            }
            if (!getEdgeNamespace().equals(namespace)) {
                LoggerFactory.getLogger(this.getClass()).info("Creating new edge provider with namespace {}", namespace);
                m_edgeProvider = new SimpleEdgeProvider(namespace);
            }

            // Add all Nodes to container
            for (GraphMLGraph eachGraph : graphML.getGraphs()) {
                for (GraphMLNode vertex : eachGraph.getNodes()) {
                    GraphMLVertex newVertex = new GraphMLVertex(
                            vertex.getNamespace(),
                            vertex.getId(),
                            vertex.getLabel());
                    newVertex.setIconKey(vertex.getIconKey());
                    newVertex.setIpAddress(vertex.getIpAddr());
                    newVertex.setLabel(vertex.getLabel());
                    newVertex.setLocked(vertex.isLocked());
                    if (vertex.getNodeID() != null) newVertex.setNodeID(vertex.getNodeID());
                    newVertex.setSelected(vertex.isSelected());
                    newVertex.setStyleName(vertex.getStyleName());
                    newVertex.setTooltipText(vertex.getTooltipText());
                    newVertex.setProperties(vertex.getProperties());
                    addVertices(newVertex);
                }
            }

            // Add all Edges to container
            for (GraphMLGraph eachGraph : graphML.getGraphs()) {
                for (org.opennms.features.topology.plugins.topo.graphml.model.GraphMLEdge eachEdge : eachGraph.getEdges()) {
                    GraphMLEdge newEdge = createGraphMLEdge(eachEdge.getNamespace(), eachEdge.getId(), eachEdge.getSource(), eachEdge.getTarget());
                    newEdge.setProperties(eachEdge.getProperties());
                    newEdge.setLabel(eachEdge.getLabel());
                    newEdge.setTooltipText(eachEdge.getTooltipText());
                }
            }
        } catch (InvalidGraphException | IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setTopologyLocation(String filename) {
        this.graphMLFile = filename != null ? new File(filename) : null;
    }

    @Override
    public void load(final String filename) throws MalformedURLException, JAXBException {
        refresh();
    }

    private GraphMLEdge createGraphMLEdge(String id, String namespace, GraphMLNode source, GraphMLNode target) {
        GraphMLVertex sourceVertex = (GraphMLVertex) getVertex(source.getNamespace(), source.getId());
        GraphMLVertex targetVertex = (GraphMLVertex) getVertex(target.getNamespace(), target.getId());
        return new GraphMLEdge(namespace, id, sourceVertex, targetVertex);
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Criteria getDefaultCriteria() {
        if (!getVertices().isEmpty()) {
            return new VertexHopGraphProvider.DefaultVertexHopCriteria(getVertices().iterator().next());
        }
        return null;
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType contentType) {
        return SelectionChangedListener.Selection.NONE;
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return false;
    }

}
