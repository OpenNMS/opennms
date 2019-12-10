/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import java.util.Collections;
import java.util.List;

import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.DefaultTopologyProviderInfo;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol.OnmsProtocolLayer;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocolComparator;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

public class LinkdMetaTopologyProvider implements MetaTopologyProvider {

    private final List<GraphProvider> providers = Lists.newArrayList();

    public LinkdMetaTopologyProvider(final OnmsTopologyDao onmsTopologyDao, final MetricRegistry metricRegistry) {
        List<OnmsTopologyProtocol> protocols = Lists.newArrayList();
        for (OnmsTopologyProtocol onmsTopologyProtocol: onmsTopologyDao.getSupportedProtocols()) {
            if (onmsTopologyProtocol.getLayer() == OnmsProtocolLayer.NoLayer) {
                continue;
            }
            protocols.add(onmsTopologyProtocol);
        }
        Collections.sort(protocols,new OnmsTopologyProtocolComparator());
        for (OnmsTopologyProtocol onmsTopologyProtocol: protocols) {
            final TopologyProviderInfo info =
                    new DefaultTopologyProviderInfo(
                            onmsTopologyProtocol.getId(),
                            "This Topology Provider displays the "+ onmsTopologyProtocol.getLayer()+ " " +onmsTopologyProtocol.getId() + " topology information discovered by: " + onmsTopologyProtocol.getSource(),
                            false,
                            true);
            final LinkdTopologyProvider topologyProvider = new LinkdTopologyProvider(metricRegistry, onmsTopologyProtocol, onmsTopologyDao);
            topologyProvider.setTopologyProviderInfo(info);

            final VertexHopGraphProvider hop = new VertexHopGraphProvider(topologyProvider);
            providers.add(hop);
        }
    }

    @Override
    public GraphProvider getDefaultGraphProvider() {
        return providers.get(0);
    }

    @Override
    public Collection<GraphProvider> getGraphProviders() {
        return Collections.unmodifiableCollection(providers);
    }

    @Override
    public Collection<VertexRef> getOppositeVertices(VertexRef vertexRef) {
        return Collections.emptyList();
    }

    @Override
    public GraphProvider getGraphProviderBy(String namespace) {
        return getGraphProviders()
                .stream()
                .filter(provider -> provider.getNamespace().equals(namespace))
                .findFirst().orElse(null);
    }

    @Override
    public BreadcrumbStrategy getBreadcrumbStrategy() {
        return BreadcrumbStrategy.NONE;
    }

    @Override
    public String getId() {
        return OnmsTopology.TOPOLOGY_NAMESPACE_LINKD_PREFIX+"meta";
    }
}
