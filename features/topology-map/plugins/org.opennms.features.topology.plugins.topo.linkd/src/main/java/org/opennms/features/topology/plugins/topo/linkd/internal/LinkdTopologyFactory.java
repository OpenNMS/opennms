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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;


public class LinkdTopologyFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LinkdTopologyFactory.class);

    protected static Set<ProtocolSupported> getProtocolSupportedSet(String... names) {
        Set<ProtocolSupported> protocolSupportedSet = new LinkedHashSet<>();
        for (String protocol: names) {
            if (protocol.equalsIgnoreCase(ProtocolSupported.NODES.name())) {
                protocolSupportedSet.add(ProtocolSupported.NODES);
            }
            if (protocol.equalsIgnoreCase(ProtocolSupported.CDP.name())) {
                protocolSupportedSet.add(ProtocolSupported.CDP);
            }
            if (protocol.equalsIgnoreCase(ProtocolSupported.NETWORKROUTER.name())) {
                protocolSupportedSet.add(ProtocolSupported.NETWORKROUTER);
            }
            if (protocol.equalsIgnoreCase(ProtocolSupported.LLDP.name())) {
                protocolSupportedSet.add(ProtocolSupported.LLDP);
            }
            if (protocol.equalsIgnoreCase(ProtocolSupported.BRIDGE.name())) {
                protocolSupportedSet.add(ProtocolSupported.BRIDGE);
            }
            if (protocol.equalsIgnoreCase(ProtocolSupported.OSPF.name())) {
                protocolSupportedSet.add(ProtocolSupported.OSPF);
            }
            if (protocol.equalsIgnoreCase(ProtocolSupported.OSPFAREA.name())) {
                protocolSupportedSet.add(ProtocolSupported.OSPFAREA);
            }
            if (protocol.equalsIgnoreCase(ProtocolSupported.ISIS.name())) {
                protocolSupportedSet.add(ProtocolSupported.ISIS);
            }
            if (protocol.equalsIgnoreCase(ProtocolSupported.USERDEFINED.name())) {
                protocolSupportedSet.add(ProtocolSupported.USERDEFINED);
            }
        }

        return protocolSupportedSet;
    }

    private LinkdTopologyProvider m_delegate;
    private final OnmsTopologyDao m_onmsTopologyDao;

    private final Timer m_loadFullTimer;

    private final SelectionAware selectionAwareDelegate;

    public LinkdTopologyFactory(MetricRegistry registry, OnmsTopologyDao onmsTopologyDao) {
        Objects.requireNonNull(registry);
        Objects.requireNonNull(onmsTopologyDao);
        m_onmsTopologyDao=onmsTopologyDao;
        selectionAwareDelegate = new LinkdSelectionAware(this);
        m_loadFullTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "full"));
    }
    
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return selectionAwareDelegate.getSelection(selectedVertices, type);
    }

    public boolean contributesTo(ContentType type) {
        return selectionAwareDelegate.contributesTo(type);
    }

    private void loadTopology(ProtocolSupported protocol, BackendGraph graph) {
        OnmsTopology topology;
        try {
             topology = m_onmsTopologyDao.getTopology(protocol.name());
        } catch (IllegalArgumentException e) {
            LOG.info("loadTopology: protocol not supported: {}", protocol);
            return;
        }
        final Map<String, LinkdVertex> vmap = new HashMap<>();
        topology.getVertices().forEach(tvertex -> {
            LinkdVertex vertex = (LinkdVertex) graph.getVertex(getActiveNamespace(), tvertex.getId());
            if (vertex == null) {
                vertex = LinkdVertex.create(tvertex,getActiveNamespace());
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
                                                protocol, getActiveNamespace())
                                       )
                                             );
    }

    protected Vertex getDefaultVertex(BackendGraph graph) {
        for (ProtocolSupported protocol: m_delegate.getProtocolSupported()) {
            OnmsTopology topology;

            try {
                topology = m_onmsTopologyDao.getTopology(protocol.name());
            } catch (Exception e) {
                LOG.error("getDefaultVertex: {}: {}: no topology found {}", graph.getNamespace(), protocol, e.getMessage());
                continue;
            }

            if (topology.getDefaultVertex() == null) {
                LOG.info("getDefaultVertex: {}: {}: no default vertex found!", graph.getNamespace(), protocol);
                continue;
            }

            LOG.info("getDefaultVertex: {}, default node found: [{}]:{}", graph.getNamespace(), topology.getDefaultVertex().getId(), topology.getDefaultVertex().getLabel());
            return graph.getVertex(graph.getNamespace(), topology.getDefaultVertex().getId());
        }
        LOG.info("getDefaultVertex: {}: no default vertex found", graph.getNamespace());
        return null;
    }

    protected Defaults getDefaults(BackendGraph graph) {
        return new Defaults()
                .withSemanticZoomLevel(Defaults.DEFAULT_SEMANTIC_ZOOM_LEVEL)
                .withPreferredLayout("D3 Layout") // D3 Layout
                .withCriteria(() -> {
                    final Vertex defaultVertex = getDefaultVertex(graph);
                    if (defaultVertex != null) {
                        LOG.info("getDefaults: default vertex found: [{}]:{}", defaultVertex.getId(), defaultVertex.getLabel());
                        return Lists.newArrayList(LinkdHopCriteria.createCriteria(defaultVertex.getId(), defaultVertex.getLabel(),this));
                    }
                    LOG.info("getDefaults: default vertex not found");
                    return Lists.newArrayList();
                });
    }

    protected void doRefresh(Set<ProtocolSupported> supportedProtocols, BackendGraph graph) {
        final Timer.Context context = m_loadFullTimer.time();
        try {
            for (ProtocolSupported protocol: supportedProtocols) {
                loadTopology(protocol, graph);
            }
        } finally {
            context.stop();
        }

    }

    public GraphProvider getDelegate() {
        return m_delegate;
    }

    public void setDelegate(LinkdTopologyProvider delegate) {
        this.m_delegate = delegate;
    }
    
    public String getActiveNamespace() {
        return m_delegate.getNamespace();
    }


}