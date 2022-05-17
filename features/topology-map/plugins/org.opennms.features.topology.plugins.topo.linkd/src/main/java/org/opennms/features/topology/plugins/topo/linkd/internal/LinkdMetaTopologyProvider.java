/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.simple.SimpleMetaTopologyProvider;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LinkdMetaTopologyProvider extends SimpleMetaTopologyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LinkdMetaTopologyProvider.class);

    List<GraphProvider> graphProviders = new ArrayList<>();

    public LinkdMetaTopologyProvider(LinkdTopologyProvider linkdTopologyProvider) {
        super(Objects.requireNonNull(linkdTopologyProvider));
        LOG.info("Adding Protocol Provider for {}",linkdTopologyProvider.getNamespace() );
        graphProviders.add(linkdTopologyProvider);
        LOG.info("Adding Protocol Provider for {}",ProtocolSupported.CDP.name() );
        graphProviders.add(new LinkdProtocolTopologyProvider(linkdTopologyProvider, ProtocolSupported.CDP.name()));
        LOG.info("Adding Protocol Provider for {}",ProtocolSupported.LLDP.name() );
        graphProviders.add(new LinkdProtocolTopologyProvider(linkdTopologyProvider, ProtocolSupported.LLDP.name()));
        LOG.info("Adding Protocol Provider for {}",ProtocolSupported.BRIDGE.name() );
        graphProviders.add(new LinkdProtocolTopologyProvider(linkdTopologyProvider, ProtocolSupported.BRIDGE.name()));
        LOG.info("Adding Protocol Provider for {}",ProtocolSupported.OSPF.name() );
        graphProviders.add(new LinkdProtocolTopologyProvider(linkdTopologyProvider, ProtocolSupported.OSPF.name()));
        LOG.info("Adding Protocol Provider for {}",ProtocolSupported.ISIS.name() );
        graphProviders.add(new LinkdProtocolTopologyProvider(linkdTopologyProvider, ProtocolSupported.ISIS.name()));
        LOG.info("Adding Protocol Provider for {}",ProtocolSupported.USERDEFINED.name() );
        graphProviders.add(new LinkdProtocolTopologyProvider(linkdTopologyProvider, ProtocolSupported.USERDEFINED.name()));

        LOG.info("Adding Protocol Provider for {}","layer2 - CDP and LLDP" );
        graphProviders.add(new LinkdProtocolTopologyProvider(linkdTopologyProvider,"layer2"));
        LOG.info("Adding Protocol Provider for {}","layer3 - OSPF and ISIS" );
        graphProviders.add(new LinkdProtocolTopologyProvider(linkdTopologyProvider,"layer3"));
    }

    @Override
    public List<GraphProvider> getGraphProviders() {
        return graphProviders;
    }


}