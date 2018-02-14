/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class NodeACLVertexProvider implements GraphProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NodeACLVertexProvider.class);

    private final GraphProvider m_delegate;
    private final NodeDao m_nodeDao;
    private final boolean m_aclsEnabled;

    public NodeACLVertexProvider(GraphProvider delegate, NodeDao nodeDao){
        m_delegate = delegate;
        m_nodeDao = nodeDao;
        String aclsProp = System.getProperty("org.opennms.web.aclsEnabled");
        m_aclsEnabled = aclsProp != null ? aclsProp.equals("true") : false;
    }

    @Override
    public void refresh() {
        m_delegate.refresh();
    }

    @Override
    public void resetContainer() {
        m_delegate.resetContainer();
    }

    @Override
    public void addVertices(Vertex... vertices) {
        m_delegate.addVertices(vertices);
    }

    @Override
    public void removeVertex(VertexRef... vertexId) {
        m_delegate.removeVertex(vertexId);
    }

    @Override
    public Vertex addVertex(int x, int y) {
        return m_delegate.addVertex(x, y);
    }

    @Override
    public Vertex addGroup(String label, String iconKey) {
        return m_delegate.addGroup(label, iconKey);
    }

    @Override
    public EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
        return m_delegate.getEdgeIdsForVertex(vertex);
    }

    @Override
    public Map<VertexRef, Set<EdgeRef>> getEdgeIdsForVertices(VertexRef... vertex) {
        return m_delegate.getEdgeIdsForVertices(vertex);
    }

    @Override
    public void addEdges(Edge... edges) {
        m_delegate.addEdges(edges);
    }

    @Override
    public void removeEdges(EdgeRef... edges) {
        m_delegate.removeEdges(edges);
    }

    @Override
    public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
        return m_delegate.connectVertices(sourceVertextId, targetVertextId);
    }

    @Override
    public Defaults getDefaults() {
        return m_delegate.getDefaults();
    }

    @Override
    public Edge getEdge(String namespace, String id) {
        //TODO: Filter through ACL list ??
        return m_delegate.getEdge(namespace, id);
    }

    @Override
    public Edge getEdge(EdgeRef reference) {
        //TODO: Filter through ACL list ??
        return m_delegate.getEdge(reference);
    }

    @Override
    public List<Edge> getEdges(Criteria... criteria) {
        //TODO: Filter through ACL list
        return m_delegate.getEdges(criteria);
    }

    @Override
    public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
        //TODO: Filter through ACL list
        return m_delegate.getEdges(references);
    }

    @Override
    public void addEdgeListener(EdgeListener listener) {
        m_delegate.addEdgeListener(listener);
    }

    @Override
    public void removeEdgeListener(EdgeListener listener) {
        m_delegate.removeEdgeListener(listener);
    }

    @Override
    public void clearEdges() {
        m_delegate.clearEdges();
    }

    @Override
    public String getNamespace() {
        return m_delegate.getNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return m_delegate.contributesTo(namespace);
    }

    @Override
    public boolean containsVertexId(String id) {
        //TODO: Filter through ACL list
        return m_delegate.containsVertexId(id);
    }

    @Override
    public boolean containsVertexId(VertexRef id, Criteria... criteria) {
        //TODO: Filter through ACL list
        return m_delegate.containsVertexId(id, criteria);
    }

    @Override
    public Vertex getVertex(String namespace, String id) {
        //TODO: Filter through ACL list
        return m_delegate.getVertex(namespace, id);
    }

    @Override
    public Vertex getVertex(VertexRef reference, Criteria... criteria) {
        //TODO: Filter through ACL list
        return m_delegate.getVertex(reference, criteria);
    }

    @Override
    public int getSemanticZoomLevel(VertexRef vertex) {
        return m_delegate.getSemanticZoomLevel(vertex);
    }

    @Override
    public List<Vertex> getVertices(Criteria... criteria) {
        //Filter out vertices not in ACLs
        return filterVertices(m_delegate.getVertices(criteria));
    }

    private List<Vertex> filterVertices(List<Vertex> vertices) {
        if(m_aclsEnabled){
            //Get All nodes when called should filter with ACL
            List<OnmsNode> onmsNodes = m_nodeDao.findAll();

            //Transform the onmsNodes list to a list of Ids
            final Set<Integer> nodes = new HashSet<Integer>(Lists.transform(onmsNodes, new Function<OnmsNode, Integer>() {
                @Override
                public Integer apply(OnmsNode node) {
                    return node.getId();
                }
            }));

            //Filter out the nodes that are not viewable by the user.
            return Lists.newArrayList(Collections2.filter(vertices, new Predicate<Vertex>() {
                @Override
                public boolean apply(Vertex vertex) {
                    return vertex.getNamespace().toLowerCase().equals("nodes") ? nodes.contains(vertex.getNodeID()) : true;
                }
            }));
        }else{
            return vertices;
        }
    }

    @Override
    public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
        return filterVertices(m_delegate.getVertices(references, criteria));
    }

    @Override
    public List<Vertex> getRootGroup() {
        //TODO: Filter getting the root group
        return m_delegate.getRootGroup();
    }

    @Override
    public boolean hasChildren(VertexRef group) {
        //TODO: Filter out the ACLs
        return m_delegate.hasChildren(group);
    }

    @Override
    public Vertex getParent(VertexRef vertex) {
        //TODO: Filter through ACLs
        return m_delegate.getParent(vertex);
    }

    @Override
    public boolean setParent(VertexRef child, VertexRef parent) {
        return m_delegate.setParent(child, parent);
    }

    @Override
    public List<Vertex> getChildren(VertexRef group, Criteria... criteria) {
        return filterVertices(m_delegate.getChildren(group, criteria));
    }

    @Override
    public void addVertexListener(VertexListener vertexListener) {
        m_delegate.addVertexListener(vertexListener);
    }

    @Override
    public void removeVertexListener(VertexListener vertexListener) {
        m_delegate.removeVertexListener(vertexListener);
    }

    @Override
    public void clearVertices() {
        m_delegate.clearVertices();
    }

    @Override
    public int getVertexTotalCount() {
        return m_delegate.getVertexTotalCount();
    }

    @Override
    public int getEdgeTotalCount() {
        return m_delegate.getEdgeTotalCount();
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        return m_delegate.getSelection(selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return m_delegate.contributesTo(type);
    }

    public TopologyProviderInfo getTopologyProviderInfo() {
        return m_delegate.getTopologyProviderInfo();
    }
}
