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

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Objects;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;


public class LinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE_LINKD = "nodes";
    private static final Logger LOG = LoggerFactory.getLogger(LinkdTopologyProvider.class);
    private final LinkdTopologyFactory m_linkdTopologyFactory;
    private final Set<ProtocolSupported> m_supportedSet;

    public LinkdTopologyProvider(LinkdTopologyFactory linkdTopologyFactory) {
        this(TOPOLOGY_NAMESPACE_LINKD, LinkdTopologyFactory.getProtocolSupportedSet(ProtocolSupported.NODES.name(),
                ProtocolSupported.LLDP.name(),
                ProtocolSupported.CDP.name(),
                ProtocolSupported.BRIDGE.name(),
                ProtocolSupported.OSPF.name(),
                ProtocolSupported.ISIS.name(),
                ProtocolSupported.USERDEFINED.name()), linkdTopologyFactory);
        linkdTopologyFactory.setDelegate(this);
        LOG.info("Created delegate instance namespace {}, protocols {}", TOPOLOGY_NAMESPACE_LINKD, m_supportedSet);
    }

    public LinkdTopologyProvider(String name, LinkdTopologyFactory linkdTopologyFactory) {
        this(TOPOLOGY_NAMESPACE_LINKD + ":" + name, LinkdTopologyFactory.getProtocolSupportedSet(name), linkdTopologyFactory);
    }


    public LinkdTopologyProvider(String name, LinkdTopologyFactory linkdTopologyFactory, List<String> protocols) {
        this(TOPOLOGY_NAMESPACE_LINKD+":" + name, LinkdTopologyFactory.getProtocolSupportedSet(protocols.toArray(new String[0])), linkdTopologyFactory);
    }

    private LinkdTopologyProvider(String namespace, Set<ProtocolSupported> protocolSupportedSet, LinkdTopologyFactory factory) {
        super(namespace);
        m_linkdTopologyFactory = Objects.requireNonNull(factory);
        m_supportedSet = protocolSupportedSet;
        LOG.info("Created instance namespace {}, protocols {}", namespace, m_supportedSet);
    }
    
    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return m_linkdTopologyFactory.getSelection(selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return m_linkdTopologyFactory.contributesTo(type);
    }

    @Override
    public Defaults getDefaults() {
        return m_linkdTopologyFactory.getDefaults(graph);
    }

    @Override
    public void refresh() {
        LOG.info("refresh: {}: protocolSupported: {}",getNamespace(), m_supportedSet);
        m_linkdTopologyFactory.setDelegate(this);
        graph.resetContainer();
        m_linkdTopologyFactory.doRefresh(m_supportedSet, graph);
        LOG.info("refresh: {}: Found {} vertices",getNamespace(), graph.getVertices().size());
        LOG.info("refresh: {}: Found {} edges", getNamespace(), graph.getEdges().size());
    }

    public Set<ProtocolSupported> getProtocolSupported() {
        return m_supportedSet;
    }

}