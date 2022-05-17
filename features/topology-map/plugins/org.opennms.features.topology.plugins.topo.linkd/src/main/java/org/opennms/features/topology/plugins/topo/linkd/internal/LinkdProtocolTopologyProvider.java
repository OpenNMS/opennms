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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LinkdProtocolTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LinkdProtocolTopologyProvider.class);
    private final LinkdTopologyProvider linkdTopologyProvider;

    private static Set<ProtocolSupported> getProtocolSupportedSet(String namespace) {
        Set<ProtocolSupported> protocolSupportedSet = new HashSet<>();
        protocolSupportedSet.add(ProtocolSupported.NODES);
        if (namespace.equalsIgnoreCase(ProtocolSupported.CDP.name())) {
            protocolSupportedSet.add(ProtocolSupported.CDP);
            return protocolSupportedSet;
        }
        if (namespace.equalsIgnoreCase(ProtocolSupported.LLDP.name())) {
            protocolSupportedSet.add(ProtocolSupported.LLDP);
            return protocolSupportedSet;
        }
        if (namespace.equalsIgnoreCase(ProtocolSupported.BRIDGE.name())) {
            protocolSupportedSet.add(ProtocolSupported.BRIDGE);
            return protocolSupportedSet;
        }
        if (namespace.equalsIgnoreCase(ProtocolSupported.OSPF.name())) {
            protocolSupportedSet.add(ProtocolSupported.OSPF);
            return protocolSupportedSet;
        }
        if (namespace.equalsIgnoreCase(ProtocolSupported.ISIS.name())) {
            protocolSupportedSet.add(ProtocolSupported.ISIS);
            return protocolSupportedSet;
        }
        if (namespace.equalsIgnoreCase(ProtocolSupported.USERDEFINED.name())) {
            protocolSupportedSet.add(ProtocolSupported.USERDEFINED);
            return protocolSupportedSet;
        }
        if (namespace.equalsIgnoreCase("layer2")) {
            protocolSupportedSet.add(ProtocolSupported.LLDP);
            protocolSupportedSet.add(ProtocolSupported.CDP);
            return protocolSupportedSet;
        }
        if (namespace.equalsIgnoreCase("layer3")) {
            protocolSupportedSet.add(ProtocolSupported.OSPF);
            protocolSupportedSet.add(ProtocolSupported.ISIS);
            return protocolSupportedSet;
        }


        return protocolSupportedSet;
    }

    private final Set<ProtocolSupported> supportedSet;
    public LinkdProtocolTopologyProvider(LinkdTopologyProvider linkdTopologyProvider, String namespace) {
        super(Objects.requireNonNull(namespace));
        this.linkdTopologyProvider=Objects.requireNonNull(linkdTopologyProvider);
        this.supportedSet=getProtocolSupportedSet(namespace);
    }
    
    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return linkdTopologyProvider.getSelection(selectedVertices,type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return linkdTopologyProvider.contributesTo(type);
    }

    @Override
    public Defaults getDefaults() {
        return linkdTopologyProvider.getDefaults(graph);
    }

    @Override
    public void refresh() {
        graph.resetContainer();
        linkdTopologyProvider.doRefresh(supportedSet,graph);
        LOG.info("refresh: Found {} vertices", graph.getVertices().size());
        LOG.info("refresh: Found {} edges", graph.getEdges().size());
    }

}