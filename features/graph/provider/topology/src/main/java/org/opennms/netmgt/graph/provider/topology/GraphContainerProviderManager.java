/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.topology;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.topology.api.IconRepository;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.integration.api.v1.graph.Properties;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentService;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.api.service.osgi.GraphContainerProviderRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphContainerProviderManager {

    private final BundleContext bundleContext;
    private final Map<GraphContainerProvider, List<ServiceRegistration<?>>> serviceRegistrations = Maps.newHashMap();
    private final GraphService graphService;
    private final EnrichmentService enrichmentService;
    private final NodeDao nodeDao;
    private final AlarmDao alarmDao ;

    public GraphContainerProviderManager(final BundleContext bundlecontext, final GraphService graphService, final EnrichmentService enrichmentService,
                                         final NodeDao nodeDao, final AlarmDao alarmDao) {
        this.bundleContext = Objects.requireNonNull(bundlecontext);
        this.graphService = Objects.requireNonNull(graphService);
        this.enrichmentService = Objects.requireNonNull(enrichmentService);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.alarmDao = Objects.requireNonNull(alarmDao);
    }

    public void onBind(final GraphContainerProviderRegistration containerProviderRegistration, final Map<String, String> properties) {
        final GraphContainerProvider containerProvider = containerProviderRegistration.getDelegate();
        final LegacyTopologyConfigurationImpl configuration = new LegacyTopologyConfigurationImpl(properties);
        if (configuration.isExposeToTopology()) {
            serviceRegistrations.putIfAbsent(containerProvider, Lists.newArrayList());

            // Expose Meta Topology Provider
            final GraphContainerInfo containerInfo = containerProvider.getContainerInfo();
            final String containerId = containerInfo.getId();
            final Hashtable<String, String> serviceProperties = new Hashtable<>();
            serviceProperties.put("label", containerInfo.getLabel());

            final MetaTopologyProvider metaTopologyProvider = new LegacyMetaTopologyProvider(configuration, nodeDao, graphService, enrichmentService, containerId);
            final ServiceRegistration<MetaTopologyProvider> metaTopologyProviderServiceRegistration = bundleContext.registerService(MetaTopologyProvider.class, metaTopologyProvider, serviceProperties);

            // Register Search provider
            metaTopologyProvider.getGraphProviders().forEach(topologyProvider -> {
                final SearchProvider searchProvider = new LegacyTopologySearchProvider((LegacyTopologyProvider) topologyProvider);
                final ServiceRegistration<SearchProvider> registeredSearchProviderServiceRegistration = bundleContext.registerService(SearchProvider.class, searchProvider, new Hashtable<>());
                serviceRegistrations.get(containerProvider).add(registeredSearchProviderServiceRegistration);
            });

            // Register IconRepository, otherwise icons will not work
            metaTopologyProvider.getGraphProviders().forEach(topologyProvider -> {
                final ServiceRegistration<IconRepository> iconRepositoryServiceRegistration = bundleContext.registerService(IconRepository.class, new LegacyIconRepositoryAdapter(topologyProvider), new Hashtable<>());
                serviceRegistrations.get(containerProvider).add(iconRepositoryServiceRegistration);
            });

            // If configured, expose Status Provider
            if (configuration.isExposeStatusProvider()) {
                metaTopologyProvider.getGraphProviders().forEach(topologyProvider -> {
                    final LegacyStatusProvider statusProvider = new LegacyStatusProvider(topologyProvider.getNamespace(), alarmDao);
                    final ServiceRegistration<StatusProvider> statusProviderServiceRegistration = bundleContext.registerService(StatusProvider.class, statusProvider, new Hashtable<>());
                    final ServiceRegistration<EdgeStatusProvider> edgeStatusProviderServiceRegistration = bundleContext.registerService(EdgeStatusProvider.class, statusProvider, new Hashtable<>());
                    serviceRegistrations.get(containerProvider).add(statusProviderServiceRegistration);
                    serviceRegistrations.get(containerProvider).add(edgeStatusProviderServiceRegistration);
                });
            }

            // The MetaTopologyProvider is registered last, to avoid missing icons or missing edges.
            // This way everything is registered before anyone tries to access/select the meta topology
            serviceRegistrations.get(containerProvider).add(metaTopologyProviderServiceRegistration);
        }
    }

    public void onUnbind(final GraphContainerProviderRegistration containerProviderRegistration, final Map<String, String> properties) {
        if (containerProviderRegistration != null) {
            final List<ServiceRegistration<?>> removedServices = serviceRegistrations.remove(containerProviderRegistration.getDelegate());
            if (removedServices != null) {
                for (ServiceRegistration<?> removedService : removedServices) {
                    if (removedService != null) {
                        removedService.unregister();
                    }
                }
            }
        }
    }

    private static class LegacyTopologyConfigurationImpl implements LegacyTopologyConfiguration {
        private final Map<String, String> properties;

        public LegacyTopologyConfigurationImpl(final Map<String, String> properties) {
            this.properties = Objects.requireNonNull(properties);
        }

        public boolean isExposeToTopology() {
            return Boolean.valueOf(properties.get("expose-to-topology"));
        }

        @Override
        public boolean isExposeStatusProvider() {
            return Boolean.valueOf(properties.get("expose-status-provider"));
        }

        @Override
        public boolean isResolveNodeIds() {
            if(properties.get(Properties.Enrichment.RESOLVE_NODES) != null) {
                return Boolean.parseBoolean(properties.get(Properties.Enrichment.RESOLVE_NODES));
            }
            // Fallback: legacy property - this is deprecated and will be removed in future versions
            return Boolean.parseBoolean(properties.get("resolve-node-ids"));
        }
    }
}
