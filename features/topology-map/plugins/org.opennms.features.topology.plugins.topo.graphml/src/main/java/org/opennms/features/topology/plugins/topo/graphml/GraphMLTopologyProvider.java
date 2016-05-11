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

import java.net.MalformedURLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultTopologyProviderInfo;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLGraph;
import org.opennms.features.topology.plugins.topo.graphml.model.GraphMLNode;

public class GraphMLTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    protected static final String DEFAULT_DESCRIPTION = "This Topology Provider visualizes a predefined GraphML graph.";

    private static TopologyProviderInfo createTopologyProviderInfo(GraphMLGraph graph) {
        String name = graph.getProperty(GraphMLProperties.LABEL, graph.getId());
        String description = graph.getProperty(GraphMLProperties.DESCRIPTION, DEFAULT_DESCRIPTION);
        return new DefaultTopologyProviderInfo(name, description);
    }

    public GraphMLTopologyProvider(GraphMLGraph graph) {
        super(graph.getProperty(GraphMLProperties.NAMESPACE));
        for (GraphMLNode graphMLNode : graph.getNodes()) {
            GraphMLVertex newVertex = new GraphMLVertex(this.getVertexNamespace(), graphMLNode);
            addVertices(newVertex);
        }
        for (org.opennms.features.topology.plugins.topo.graphml.model.GraphMLEdge eachEdge : graph.getEdges()) {
            GraphMLVertex sourceVertex = (GraphMLVertex) getVertex(getVertexNamespace(), eachEdge.getSource().getId());
            GraphMLVertex targetVertex = (GraphMLVertex) getVertex(getVertexNamespace(), eachEdge.getTarget().getId());
            if (sourceVertex == null || targetVertex == null) {
                // Skip edges where either the source of target vertices are outside of this graph
                continue;
            }
            GraphMLEdge newEdge = new GraphMLEdge(eachEdge, sourceVertex, targetVertex);
            addEdges(newEdge);
        }
        setTopologyProviderInfo(createTopologyProviderInfo(graph));
    }

    @Override
    public void refresh() {
        // TODO: How to handle refresh()?
    }

    @Override
    public void load(final String filename) throws MalformedURLException, JAXBException {
        refresh();
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
