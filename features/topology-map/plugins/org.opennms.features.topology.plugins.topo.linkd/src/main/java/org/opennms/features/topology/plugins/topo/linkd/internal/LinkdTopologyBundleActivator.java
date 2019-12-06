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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.DefaultTopologyProviderInfo;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.SimpleMetaTopologyProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class LinkdTopologyBundleActivator {

    private static Logger LOG = LoggerFactory.getLogger(LinkdTopologyBundleActivator.class);
    private List<ServiceRegistration<MetaTopologyProvider>> registrations = new ArrayList<ServiceRegistration<MetaTopologyProvider>>();

    private final OnmsTopologyDao onmsTopologyDao;
    private final MetricRegistry metricRegistry;
    private final BundleContext context;

    public LinkdTopologyBundleActivator(
            OnmsTopologyDao onmsTopologyDao, MetricRegistry metricRegistry,
            BundleContext context) {
        this.onmsTopologyDao = onmsTopologyDao;
        this.metricRegistry = metricRegistry;
        this.context = context;
    }

    public void start() throws Exception {
        for (OnmsTopologyProtocol onmsTopologyProtocol: onmsTopologyDao.getSupportedProtocols()) {
            TopologyProviderInfo info = 
                    new DefaultTopologyProviderInfo(
                                    onmsTopologyProtocol.getName() + " Topology Provider", 
                                    "This Topology Provider displays the "+ onmsTopologyProtocol.getId() + " topology information discovered by: " + onmsTopologyProtocol.getSource(), 
                                    false, 
                                    true);
            LinkdTopologyProvider topologyProvider = new LinkdTopologyProvider(metricRegistry, onmsTopologyProtocol, onmsTopologyDao);
            topologyProvider.setTopologyProviderInfo(info);
            VertexHopGraphProvider hop = new VertexHopGraphProvider(topologyProvider);
            SimpleMetaTopologyProvider meta = new SimpleMetaTopologyProvider(hop);
            LOG.info("start: registering service: {}", onmsTopologyProtocol.getId());
            final Dictionary<String,String> props = new Hashtable<>();
            props.put("label", onmsTopologyProtocol.getName());
            registrations.add(context.registerService(MetaTopologyProvider.class, meta,props));
        }
        
    }

    public void stop() throws Exception { 
        for (ServiceRegistration<MetaTopologyProvider> registration:registrations) {
            LOG.info("stop: unregistering service: {}", registration);
            registration.unregister();
        }
        
    }

    public List<ServiceRegistration<MetaTopologyProvider>> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(
            List<ServiceRegistration<MetaTopologyProvider>> registrations) {
        this.registrations = registrations;
    }
}
