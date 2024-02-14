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
package org.opennms.netmgt.telemetry.distributed.sentinel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.api.TelemetryManager;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.common.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.QueueDefinition;
import org.opennms.netmgt.telemetry.daemon.TelemetryMessageConsumer;
import org.opennms.netmgt.telemetry.distributed.common.AdapterDefinitionParser;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedAdapterDef;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedQueueDef;
import org.opennms.netmgt.telemetry.distributed.common.PropertyTree;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This {@link ManagedServiceFactory} for service pids that contain
 * telemetry adapter definitions and manages their lifecycle by starting/updating
 * and stopping them accordingly.
 *
 * See {@link MapBasedAdapterDef} for a list of supported properties.
 *
 * @author mvrueden
 */
public class AdapterManager implements ManagedServiceFactory, TelemetryManager {
    private static final Logger LOG = LoggerFactory.getLogger(AdapterManager.class);

    private DistPollerDao distPollerDao;

    private Map<String, TelemetryMessageConsumer> consumersById = new LinkedHashMap<>();
    private Map<String, List<ServiceRegistration<HealthCheck>>> healthChecksById = new LinkedHashMap<>();

    private TelemetryRegistry telemetryRegistry;

    private MessageConsumerManager messageConsumerManager;

    private BundleContext bundleContext;

    @Override
    public String getName() {
        return "Manages telemetry adapter lifecycle.";
    }

    @Override
    public void updated(String pid, Dictionary<String, ?> properties) {
        final TelemetryMessageConsumer existingConsumer = consumersById.get(pid);
        if (existingConsumer != null) {
            LOG.info("Updating existing consumer for pid: {}", pid);
            deleted(pid);
        } else {
            LOG.info("Creating new consumer for pid: {}", pid);
        }

        // Build the queue and adapter definitions
        final PropertyTree propertyTree = PropertyTree.from(properties);
        final QueueDefinition queueDefinition = new MapBasedQueueDef(propertyTree);
        final List<AdapterDefinition> adapterDefinitions = new AdapterDefinitionParser().parse(queueDefinition.getName(), propertyTree);

        // Register health checks
        healthChecksById.putIfAbsent(pid, new ArrayList<>());
        final List<AdapterHealthCheck> healthChecks = new ArrayList<>(); // we need this temporarily, to mark the health check as success or failed afterwards
        for (AdapterDefinition eachAdapter : adapterDefinitions) {
            final AdapterHealthCheck healthCheck = new AdapterHealthCheck(eachAdapter);
            healthChecks.add(healthCheck);

            final ServiceRegistration<HealthCheck> serviceRegistration = bundleContext.registerService(HealthCheck.class, healthCheck, null);
            healthChecksById.get(pid).add(serviceRegistration);
        }

        try {
            // Create the Module
            final TelemetrySinkModule sinkModule = new TelemetrySinkModule(queueDefinition);
            sinkModule.setDistPollerDao(distPollerDao);

            // Create the consumer
            final TelemetryMessageConsumer consumer = new TelemetryMessageConsumer(queueDefinition, adapterDefinitions, sinkModule);
            consumer.setRegistry(telemetryRegistry);
            consumer.init();
            messageConsumerManager.registerConsumer(consumer);
            consumersById.put(pid, consumer);

            // At this point the consumer should be up and running, so we mark the underlying health checks as success
            healthChecks.forEach(AdapterHealthCheck::markSucess);
        } catch (Exception e) {
            // In case of error, we mark the health checks as failure as well
            healthChecks.forEach(healthCheck -> healthCheck.markError(e));
            LOG.error("Failed to create {}", TelemetryMessageConsumer.class, e);
        }
    }

    @Override
    public void deleted(String pid) {
        healthChecksById.get(pid).forEach(ServiceRegistration::unregister);
        final TelemetryMessageConsumer existingConsumer = consumersById.remove(pid);
        if (existingConsumer != null) {
            try {
                LOG.info("Stopping consumer for pid: {}", pid);
                existingConsumer.destroy();
            } catch (Exception e) {
                LOG.error("Error occurred while stopping consumer for pid: {}", pid, e);
            }
            try {
                LOG.info("Unregistering consumer for pid: {}", pid);
                messageConsumerManager.unregisterConsumer(existingConsumer);
            } catch (Exception e) {
                LOG.error("Error occurred while unregisterung consumer for pid: {}", pid, e);
            }
        }
    }

    public void init() {
        LOG.info("{} started.", getClass().getSimpleName());
    }

    public void destroy() {
        new ArrayList<>(consumersById.keySet()).forEach(pid -> deleted(pid));
        LOG.info("{} stopped.", getClass().getSimpleName());
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        this.distPollerDao = distPollerDao;
    }

    public void setTelemetryRegistry(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = telemetryRegistry;
    }

    public void setMessageConsumerManager(MessageConsumerManager messageConsumerManager) {
        this.messageConsumerManager = messageConsumerManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public List<Listener> getListeners() {
        return Collections.emptyList();
    }

    @Override
    public List<Adapter> getAdapters() {
        return this.consumersById.values().stream()
                .flatMap(consumer -> consumer.getAdapters().stream())
                .collect(Collectors.toList());
    }
}
