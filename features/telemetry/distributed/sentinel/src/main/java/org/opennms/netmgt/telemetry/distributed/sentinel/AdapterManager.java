/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.distributed.sentinel;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.features.telemetry.adapters.registry.api.TelemetryAdapterRegistry;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.config.api.Adapter;
import org.opennms.netmgt.telemetry.config.api.Protocol;
import org.opennms.netmgt.telemetry.daemon.TelemetryMessageConsumer;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedAdapterDef;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedListenerDef;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedProtocolDef;
import org.opennms.netmgt.telemetry.distributed.common.MapUtils;
import org.opennms.netmgt.telemetry.ipc.TelemetrySinkModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

// TODO MVR ...
/**
 * This {@link ManagedServiceFactory} for service pids that contain
 * telemetry listener definitions and manages their lifecycle by starting/updating
 * and stopping them accordingly.
 *
 * See {@link MapBasedListenerDef} for a list of supported properties.
 *
 * @author jwhite
 */
public class AdapterManager implements ManagedServiceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AdapterManager.class);

    private DistPollerDao distPollerDao;

    private Map<String, TelemetryMessageConsumer> consumersById = new LinkedHashMap<>();
    private Map<String, ServiceRegistration<HealthCheck>> healthChecksById = new LinkedHashMap<>();

    private TelemetryAdapterRegistry telemetryAdapterRegistry;

    private MessageConsumerManager messageConsumerManager;

    private BundleContext bundleContext;

    @Override
    public String getName() {
        return "Manages telemetry listener lifecycle.";
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

        // Convert the dictionary to a map
        final Map<String, String> parameters = MapUtils.fromDict(properties);

        // Build the protocol and listener definitions
        final Protocol protocolDef = new MapBasedProtocolDef(parameters);
        final Adapter adapterDef = new MapBasedAdapterDef(parameters);

        // Register health check
        final AdapterHealthCheck healthCheck = new AdapterHealthCheck(adapterDef);
        final ServiceRegistration<HealthCheck> serviceRegistration = bundleContext.registerService(HealthCheck.class, healthCheck, null);
        healthChecksById.put(pid, serviceRegistration);

        try {
            // Create the Module
            final TelemetrySinkModule sinkModule = new TelemetrySinkModule(protocolDef);
            sinkModule.setDistPollerDao(distPollerDao);

            // Create the consumer
            final TelemetryMessageConsumer consumer = new TelemetryMessageConsumer(protocolDef, Lists.newArrayList(adapterDef), sinkModule);
            consumer.setAdapterRegistry(telemetryAdapterRegistry);
            consumer.init();
            messageConsumerManager.registerConsumer(consumer);
            consumersById.put(pid, consumer);

            // At this point the consumer should be up and running, so we mark the underlying health check as success
            healthCheck.markSucess();
        } catch (Exception e) {
            // In case of error, we mark the health check as failure as well
            healthCheck.markError(e);
            LOG.error("Failed to create {}", TelemetryMessageConsumer.class, e);
        }
    }

    @Override
    public void deleted(String pid) {
        healthChecksById.get(pid).unregister();
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

    public void setTelemetryAdapterRegistry(TelemetryAdapterRegistry telemetryAdapterRegistry) {
        this.telemetryAdapterRegistry = telemetryAdapterRegistry;
    }

    public void setMessageConsumerManager(MessageConsumerManager messageConsumerManager) {
        this.messageConsumerManager = messageConsumerManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
