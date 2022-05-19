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

import java.util.*;

import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;


public class LinkdTopologyFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LinkdTopologyFactory.class);
    private String activeNamespace;

    private OnmsTopologyDao m_onmsTopologyDao;

    private final Timer m_loadFullTimer;
    private final Timer m_loadLldpLinksTimer;
    private final Timer m_loadOspfLinksTimer;
    private final Timer m_loadCdpLinksTimer;
    private final Timer m_loadIsisLinksTimer;
    private final Timer m_loadBridgeLinksTimer;
    private final Timer m_loadUserDefinedLinksTimer;
    private final Timer m_loadVerticesTimer;
    private final Timer m_loadEdgesTimer;

    private final SelectionAware selectionAwareDelegate;

    public LinkdTopologyFactory(MetricRegistry registry) {
        Objects.requireNonNull(registry);
        activeNamespace= LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD;
        selectionAwareDelegate = new LinkdSelectionAware(this);
        m_loadFullTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "full"));
        m_loadLldpLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "lldp"));
        m_loadOspfLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "ospf"));
        m_loadCdpLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "cdp"));
        m_loadIsisLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "isis"));
        m_loadBridgeLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "bridge"));
        m_loadUserDefinedLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "userdefined"));
        m_loadVerticesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "vertices", "none"));
        m_loadEdgesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "edges", "none"));
    }
    
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return selectionAwareDelegate.getSelection(selectedVertices, type);
    }

    public boolean contributesTo(ContentType type) {
        return selectionAwareDelegate.contributesTo(type);
    }
    
    protected void loadEdges(Set<ProtocolSupported> supportedProtocols, BackendGraph graph) {
        Timer.Context context = m_loadEdgesTimer.time();
        try {
            if (supportedProtocols.contains(ProtocolSupported.LLDP)) {
                loadTopology(ProtocolSupported.LLDP, m_loadLldpLinksTimer, graph);
            }
            if (supportedProtocols.contains(ProtocolSupported.OSPF)) {
                loadTopology(ProtocolSupported.OSPF,m_loadOspfLinksTimer, graph);
            }
            if (supportedProtocols.contains(ProtocolSupported.CDP)) {
                loadTopology(ProtocolSupported.CDP, m_loadCdpLinksTimer, graph);
            }
            if (supportedProtocols.contains(ProtocolSupported.ISIS)) {
                loadTopology(ProtocolSupported.ISIS, m_loadIsisLinksTimer, graph);
            }
            if (supportedProtocols.contains(ProtocolSupported.BRIDGE)) {
                loadTopology(ProtocolSupported.BRIDGE, m_loadBridgeLinksTimer, graph);
            }
            if (supportedProtocols.contains(ProtocolSupported.USERDEFINED)) {
                loadTopology(ProtocolSupported.USERDEFINED, m_loadUserDefinedLinksTimer, graph);
            }
            LOG.info("refresh: Loaded Edges");
        } catch (Exception e){
            LOG.error("Exception Loading Edges", e);
        } finally {
            context.stop();
        }
    }

    private void loadTopology(ProtocolSupported protocol, Timer timer, BackendGraph graph) {
        Timer.Context context = timer.time();
        try{
            loadTopology(protocol,graph);
            LOG.info("loadEdges: {}, loaded", protocol.name());
        } catch (Exception e){
            LOG.error("loadEdges: {}, failed", protocol.name(), e);
        } finally {
            context.stop();
        }
    }

    private void loadTopology(ProtocolSupported protocol, BackendGraph graph) {
        OnmsTopology topology =   m_onmsTopologyDao.getTopology(protocol.name());
        
        final Map<String, LinkdVertex> vmap = new HashMap<>();
        topology.getVertices().forEach(tvertex -> {
            LinkdVertex vertex = (LinkdVertex) graph.getVertex(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, tvertex.getId());
            if (vertex == null) {
                vertex = LinkdVertex.create(tvertex);
                graph.addVertices(vertex);
            } 
            vertex.getProtocolSupported().add(protocol);
            vmap.put(vertex.getId(), vertex);
        });
        
        topology.getEdges().forEach(tedge -> graph.addEdges(
                                       LinkdEdge.create(
                                                tedge.getId(), 
                                                LinkdPort.create(tedge.getSource(), vmap.get(tedge.getSource().getVertex().getId())),
                                                LinkdPort.create(tedge.getTarget(), vmap.get(tedge.getTarget().getVertex().getId())),
                                                protocol, graph.getNamespace())
                                       )
                                             );
    }

    protected Vertex getDefaultVertex(BackendGraph graph) {
        OnmsTopologyVertex node;
        try {
            node = m_onmsTopologyDao.getTopology(ProtocolSupported.NODES.name()).getDefaultVertex();
        } catch (Exception e) {
            LOG.error("getDefaultVertex: no default node found", e);
            return null;
        }

        if (node == null) {
            LOG.info("getDefaultVertex: no default node found!");
            return null;
        }
        LOG.info("getDefaultVertex: default node found: [{}]:{}", node.getId(), node.getLabel());
        return graph.getVertex(graph.getNamespace(), node.getId());
    }

    protected Defaults getDefaults(BackendGraph graph) {
        return new Defaults()
                .withSemanticZoomLevel(Defaults.DEFAULT_SEMANTIC_ZOOM_LEVEL)
                .withPreferredLayout("D3 Layout") // D3 Layout
                .withCriteria(() -> {
                    final Vertex defaultVertex = getDefaultVertex(graph);
                    if (defaultVertex != null) {
                        LOG.info("getDefaults: default vertex found: [{}]:{}", defaultVertex.getId(), defaultVertex.getLabel());
                        return Lists.newArrayList(LinkdHopCriteria.createCriteria(defaultVertex.getId(), defaultVertex.getLabel()));
                    }
                    LOG.info("getDefaults: default vertex not found");
                    return Lists.newArrayList();
                });
    }

    private void loadVertices(BackendGraph graph) {
        Timer.Context vcontext = m_loadVerticesTimer.time();
        try {
            for (OnmsTopologyVertex tvertex : m_onmsTopologyDao.getTopology(ProtocolSupported.NODES.name()).getVertices()) {
                graph.addVertices(LinkdVertex.create(tvertex));
            }
            LOG.info("refresh: Loaded Vertices");
        } catch (Exception e){
            LOG.error("Exception Loading Vertices", e);
        } finally {
            vcontext.stop();
        }
    }

    protected void doRefresh(Set<ProtocolSupported> supportedProtocols, BackendGraph graph) {
        final Timer.Context context = m_loadFullTimer.time();
        try {
            loadVertices(graph);
            loadEdges(supportedProtocols,graph);
        } finally {
            context.stop();
        }

    }

    public OnmsTopologyDao getOnmsTopologyDao() {
        return m_onmsTopologyDao;
    }

    public void setOnmsTopologyDao(OnmsTopologyDao onmsTopologyDao) {
        m_onmsTopologyDao = onmsTopologyDao;
    }

    public String getActiveNamespace() {
        return activeNamespace;
    }

    public void setActiveNamespace(String namespace) {
        this.activeNamespace=namespace;
    }

}