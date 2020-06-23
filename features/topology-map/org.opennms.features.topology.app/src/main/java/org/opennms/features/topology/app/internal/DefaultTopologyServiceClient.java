/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.TopologyService;
import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;

public class DefaultTopologyServiceClient implements TopologyServiceClient {

    private String namespace;
    private String metaTopologyId;
    private final TopologyService topologyService;

    public DefaultTopologyServiceClient(TopologyService topologyService) {
        this.topologyService = Objects.requireNonNull(topologyService);
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List< VertexRef > selectedVertices, ContentType type) {
        if (namespace == null) {
            return SelectionChangedListener.Selection.NONE;
        }
        return topologyService.getGraphProvider(metaTopologyId, namespace).getSelection(selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        if (namespace == null) {
            return false;
        }
        return topologyService.getGraphProvider(metaTopologyId, namespace).contributesTo(type);
    }

    @Override
    public Vertex getVertex(VertexRef target, Criteria... criteria) {
        return topologyService.getGraphProvider(metaTopologyId, namespace).getVertex(target, criteria);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public Vertex getVertex(String namespace, String vertexId) {
        return topologyService.getGraphProvider(metaTopologyId, namespace).getVertex(namespace, vertexId);
    }

    @Override
    public int getVertexTotalCount() {
        return topologyService.getGraphProvider(metaTopologyId, namespace).getVertexTotalCount();
    }

    @Override
    public int getEdgeTotalCount() {
        return topologyService.getGraphProvider(metaTopologyId, namespace).getEdgeTotalCount();
    }

    @Override
    public TopologyProviderInfo getInfo() {
        return topologyService.getGraphProvider(metaTopologyId, namespace).getTopologyProviderInfo();
    }

    @Override
    public Defaults getDefaults() {
        return topologyService.getGraphProvider(metaTopologyId, namespace).getDefaults();
    }

    @Override
    public List<Vertex> getChildren(VertexRef vertexId, Criteria[] criteria) {
        return topologyService.getGraphProvider(metaTopologyId, namespace).getChildren(vertexId, criteria);
    }

    @Override
    public Collection<GraphProvider> getGraphProviders() {
        return topologyService.getMetaTopologyProvider(metaTopologyId).getGraphProviders();
    }

    @Override
    public Collection<VertexRef> getOppositeVertices(VertexRef vertexRef) {
        return topologyService.getMetaTopologyProvider(metaTopologyId).getOppositeVertices(vertexRef);
    }

    @Override
    public GraphProvider getGraphProviderBy(String namespace) {
        return topologyService.getMetaTopologyProvider(metaTopologyId).getGraphProviderBy(namespace);
    }

    @Override
    public VertexProvider getDefaultGraphProvider() {
        return topologyService.getMetaTopologyProvider(metaTopologyId).getDefaultGraphProvider();
    }

    @Override
    public LayoutAlgorithm getPreferredLayoutAlgorithm() {
        return topologyService.getPreferredLayoutAlgorithm(metaTopologyId, namespace);
    }

    @Override
    public BreadcrumbStrategy getBreadcrumbStrategy() {
        return topologyService.getMetaTopologyProvider(metaTopologyId).getBreadcrumbStrategy();
    }

    @Override
    public String getMetaTopologyId() {
        return metaTopologyId;
    }

    @Override
    public void setMetaTopologyId(String metaTopologyId) {
        this.metaTopologyId = metaTopologyId;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public Graph getGraph(Criteria[] criteria, int semanticZoomLevel) {
        return topologyService.getGraph(getMetaTopologyId(), getNamespace(), criteria, semanticZoomLevel);
    }
}
