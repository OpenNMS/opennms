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

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Objects;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;


public class LinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    public static final String TOPOLOGY_NAMESPACE_LINKD = "nodes";
    private static final Logger LOG = LoggerFactory.getLogger(LinkdTopologyProvider.class);
    private final LinkdTopologyFactory linkdTopologyFactory;
    private final Set<ProtocolSupported> supportedSet;

    private static Set<ProtocolSupported> getProtocolSupportedSet(String... names) {
        Set<ProtocolSupported> protocolSupportedSet = new HashSet<>();
        protocolSupportedSet.add(ProtocolSupported.NODES);
        for (String namespace: names) {
            if (namespace.equalsIgnoreCase(ProtocolSupported.CDP.name())) {
                protocolSupportedSet.add(ProtocolSupported.CDP);
            }
            if (namespace.equalsIgnoreCase(ProtocolSupported.LLDP.name())) {
                protocolSupportedSet.add(ProtocolSupported.LLDP);
            }
            if (namespace.equalsIgnoreCase(ProtocolSupported.BRIDGE.name())) {
                protocolSupportedSet.add(ProtocolSupported.BRIDGE);
            }
            if (namespace.equalsIgnoreCase(ProtocolSupported.OSPF.name())) {
                protocolSupportedSet.add(ProtocolSupported.OSPF);
            }
            if (namespace.equalsIgnoreCase(ProtocolSupported.ISIS.name())) {
                protocolSupportedSet.add(ProtocolSupported.ISIS);
            }
            if (namespace.equalsIgnoreCase(ProtocolSupported.USERDEFINED.name())) {
                protocolSupportedSet.add(ProtocolSupported.USERDEFINED);
            }
        }

        return protocolSupportedSet;
    }

    public LinkdTopologyProvider(LinkdTopologyFactory linkdTopologyFactory) {
        super(TOPOLOGY_NAMESPACE_LINKD);
        LOG.debug("Called constructor 1 args");
        this.linkdTopologyFactory= Objects.requireNonNull(linkdTopologyFactory);
        supportedSet= EnumSet.allOf(ProtocolSupported.class);
        LOG.info("Created instance namespace {}, protocols {}", TOPOLOGY_NAMESPACE_LINKD,supportedSet);
    }

    public LinkdTopologyProvider(String name, LinkdTopologyFactory linkdTopologyFactory) {
        super(TOPOLOGY_NAMESPACE_LINKD+":"+name);
        LOG.debug("Called constructor 2 args");
        this.linkdTopologyFactory= Objects.requireNonNull(linkdTopologyFactory);
        supportedSet= getProtocolSupportedSet(name);
        LOG.info("Created instance namespace {}, protocols {}", name,supportedSet);
    }


    public LinkdTopologyProvider(String name, LinkdTopologyFactory linkdTopologyFactory, String... protocols) {
        super(TOPOLOGY_NAMESPACE_LINKD+":"+name);
        LOG.debug("Called constructor {} args protocols {}", 2+ protocols.length,protocols);
        this.linkdTopologyFactory= Objects.requireNonNull(linkdTopologyFactory);
        supportedSet=getProtocolSupportedSet(protocols);
        LOG.info("Created instance namespace {}, protocols {}", name,supportedSet);
    }
    
    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return linkdTopologyFactory.getSelection(selectedVertices,type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return linkdTopologyFactory.contributesTo(type);
    }

    @Override
    public Defaults getDefaults() {
        return linkdTopologyFactory.getDefaults(graph);
    }

    @Override
    public void refresh() {
        linkdTopologyFactory.setActiveNamespace(getNamespace());
        graph.resetContainer();
        linkdTopologyFactory.doRefresh(supportedSet,graph);
        LOG.info("refresh: {}: Found {} vertices",getNamespace(), graph.getVertices().size());
        LOG.info("refresh: {}: Found {} edges", getNamespace(),graph.getEdges().size());
    }

}