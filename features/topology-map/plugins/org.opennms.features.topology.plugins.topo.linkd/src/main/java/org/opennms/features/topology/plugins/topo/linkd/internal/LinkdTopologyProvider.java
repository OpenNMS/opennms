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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;

public class LinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    private static Logger LOG = LoggerFactory.getLogger(LinkdTopologyProvider.class);

    private final OnmsTopologyDao m_onmsTopologyDao;

    private final Timer m_loadFullTimer;
    private final Timer m_loadVerticesTimer;
    private final Timer m_loadEdgesTimer;
    private final List<String> m_protocols;
    private final SelectionAware selectionAwareDelegate;

    public LinkdTopologyProvider(String registryValue, MetricRegistry registry, List<String> protocols, OnmsTopologyDao onmsTopologyDao) {
        super(OnmsTopology.TOPOLOGY_NAMESPACE_LINKD);
        Objects.requireNonNull(registryValue);
        Objects.requireNonNull(registry);
        Objects.requireNonNull(protocols);
        Objects.requireNonNull(onmsTopologyDao);
        m_onmsTopologyDao = onmsTopologyDao;
        selectionAwareDelegate = new LinkdSelectionAware(OnmsTopology.TOPOLOGY_NAMESPACE_LINKD);
        m_protocols=protocols;
        m_loadFullTimer = registry.timer(MetricRegistry.name(registryValue, "load", "full"));
        m_loadVerticesTimer = registry.timer(MetricRegistry.name(registryValue, "load", "vertices", "none"));
        m_loadEdgesTimer = registry.timer(MetricRegistry.name(registryValue, "load", "edges", "none"));
    }

    public LinkdTopologyProvider(MetricRegistry registry, String protocol, OnmsTopologyDao onmsTopologyDao ) {
        super(protocol);
        Objects.requireNonNull(registry);
        Objects.requireNonNull(protocol);
        Objects.requireNonNull(onmsTopologyDao);
        m_onmsTopologyDao = onmsTopologyDao;
        selectionAwareDelegate = new LinkdSelectionAware(protocol);
        m_protocols=new ArrayList<>();
        m_protocols.add(protocol);
        m_loadFullTimer = registry.timer(MetricRegistry.name(protocol, "load", "full"));
        m_loadVerticesTimer = registry.timer(MetricRegistry.name(protocol, "load", "vertices", "none"));
        m_loadEdgesTimer = registry.timer(MetricRegistry.name(protocol, "load", "edges", "none"));
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return selectionAwareDelegate.getSelection(selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return selectionAwareDelegate.contributesTo(type);
    }
    
    private void loadEdges() {
        for (String protocol: m_protocols) {
            try{
                loadTopology(protocol);
                LOG.info("loadEdges: {}, protocol: {} loaded",getNamespace(), protocol);
            } catch (Exception e){
                LOG.error("loadEdges: {}, protocol {} failed",getNamespace(),protocol, e);
            }
        }
    }

    private void loadTopology(String protocol) {
        OnmsTopology topology =   m_onmsTopologyDao.getTopology(protocol);
        
        final Map<String, LinkdVertex> vmap = new HashMap<>();
        topology.getVertices().stream().forEach(tvertex -> {
            LinkdVertex vertex = (LinkdVertex) getVertex(getNamespace(), tvertex.getId());
            if (vertex == null) {
                vertex = LinkdVertex.create(tvertex,getNamespace());
                addVertices(vertex);
            } 
            vertex.getProtocolSupported().add(protocol);
            vmap.put(vertex.getId(), vertex);
        });
        
        topology.getEdges().stream().forEach(tedge -> addEdges(
                                       LinkdEdge.create(getNamespace(),
                                                tedge.getId(), 
                                                LinkdPort.create(tedge.getSource(), vmap.get(tedge.getSource().getVertex().getId())),
                                                LinkdPort.create(tedge.getTarget(), vmap.get(tedge.getTarget().getVertex().getId())),
                                                protocol)
                                       )
                                             );
    }
        
    public Vertex getDefaultVertex() {
        OnmsTopologyVertex node = null;
        try {
            node = m_onmsTopologyDao.getTopology("NODES").getDefaultVertex();
        } catch (Exception e) {
            LOG.error("getDefaultVertex: {} no default node found",getNamespace(), e);
            return null;
        }

        if (node == null) {
            LOG.info("getDefaultVertex: {} no default node found!",getNamespace());
            return null;
        }
        LOG.info("getDefaultVertex: {} default node found: [{}]:{}", getNamespace(),node.getId(), node.getLabel());
        return getVertex(getNamespace(), node.getId());
    }

    @Override
    public Defaults getDefaults() {
        return new Defaults()
                .withSemanticZoomLevel(Defaults.DEFAULT_SEMANTIC_ZOOM_LEVEL)
                .withPreferredLayout("D3 Layout") // D3 Layout
                .withCriteria(() -> { 
                    final Vertex defaultVertex = getDefaultVertex();
                    if (defaultVertex != null) {
                        LOG.info("getDefaults: {} default vertex found: [{}]:{}",getNamespace(), defaultVertex.getId(), defaultVertex.getLabel());
                        return Lists.newArrayList(LinkdHopCriteria.createCriteria(getNamespace(),defaultVertex.getId(), defaultVertex.getLabel()));
                    }
                    LOG.info("getDefaults: {} default vertex not found",getNamespace());
                    return Lists.newArrayList();
        });
    }
    
    private void doRefresh() {        
        Timer.Context vcontext = m_loadVerticesTimer.time();
        try {
            for (OnmsTopologyVertex tvertex : m_onmsTopologyDao.getTopology("NODES").getVertices()) {
                addVertices(LinkdVertex.create(tvertex,getNamespace()));
            }
            LOG.info("refresh: Loaded Vertices");
        } catch (Exception e){
            LOG.error("Exception Loading Vertices", e);
        } finally {
            vcontext.stop();
        }
        
        vcontext = m_loadEdgesTimer.time();
        try {
            loadEdges();
            LOG.info("refresh: Loaded Edges");
        } catch (Exception e){
            LOG.error("Exception Loading Edges", e);
        } finally {
            vcontext.stop();
        }
    }

    @Override
    public void refresh() {
        final Timer.Context context = m_loadFullTimer.time();
        try {
            resetContainer();
            doRefresh();
        } finally {
            context.stop();
        }
        
        LOG.info("refresh: Found {} groups", getGroups().size());
        LOG.info("refresh: Found {} vertices", getVerticesWithoutGroups().size());
        LOG.info("refresh: Found {} edges", getEdges().size());
    }
 
    public List<String> getProtocols() {
        return m_protocols;
    }
}