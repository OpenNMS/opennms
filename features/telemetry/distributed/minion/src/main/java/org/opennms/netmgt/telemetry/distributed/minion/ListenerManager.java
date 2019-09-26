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

package org.opennms.netmgt.telemetry.distributed.minion;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.common.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.distributed.common.MapBasedListenerDef;
import org.opennms.netmgt.telemetry.distributed.common.PropertyTree;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link ManagedServiceFactory} for service pids that contain
 * telemetry listener definitions and manages their lifecycle by starting/updating
 * and stopping them accordingly.
 *
 * See {@link MapBasedListenerDef} for a list of supported properties.
 *
 * @author jwhite
 */
public class ListenerManager implements ManagedServiceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ListenerManager.class);

    private MessageDispatcherFactory messageDispatcherFactory;
    private DistPollerDao distPollerDao;
    private TelemetryRegistry telemetryRegistry;

    private static class Entity {
        private Listener listener;
        private Set<String> queueNames = new HashSet<>();
        private ServiceRegistration<HealthCheck> healthCheck;
    }

    private Map<String, Entity> entities = new LinkedHashMap<>();

    private BundleContext bundleContext;

    @Override
    public String getName() {
        return "Manages telemetry listener lifecycle.";
    }

    @Override
    public void updated(String pid, Dictionary<String, ?> properties) {
        if (this.entities.containsKey(pid)) {
            LOG.info("Updating existing listener/dispatcher for pid: {}", pid);
            deleted(pid);
        } else {
            LOG.info("Creating new listener/dispatcher for pid: {}", pid);
        }
        final PropertyTree definition = PropertyTree.from(properties);
        final MapBasedListenerDef listenerDef = new MapBasedListenerDef(definition);
        final ListenerHealthCheck healthCheck = new ListenerHealthCheck(listenerDef);

        final Entity entity = new Entity();
        entity.healthCheck = bundleContext.registerService(HealthCheck.class, healthCheck, null);

        try {
            // Create sink modules for all defined queues
            listenerDef.getParsers().stream()
                    .forEach(parserDef -> {
                        // Ensure that the queues have not yet been created
                        if (telemetryRegistry.getDispatcher(parserDef.getQueueName()) != null) {
                            throw new IllegalArgumentException("A queue with name " + parserDef.getQueueName() + " is already defined. Bailing.");
                        }

                        // Create sink module
                        final TelemetrySinkModule sinkModule = new TelemetrySinkModule(parserDef);
                        sinkModule.setDistPollerDao(distPollerDao);

                        // Create dispatcher
                        final AsyncDispatcher<TelemetryMessage> dispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
                        final String queueName = Objects.requireNonNull(parserDef.getQueueName());
                        telemetryRegistry.registerDispatcher(queueName, dispatcher);

                        // Remember queue name
                        entity.queueNames.add(parserDef.getQueueName());
                    });

            // Start listener
            entity.listener = telemetryRegistry.getListener(listenerDef);
            entity.listener.start();

            // At this point the listener should be up and running,
            // so we mark the underlying health check as success
            healthCheck.markSucess();

            this.entities.put(pid, entity);
        } catch (Exception e) {
            LOG.error("Failed to build listener.", e);

            // In case of error, we mark the health check as failure as well
            healthCheck.markError(e);

            // Close all already started dispatcher
            stopQueues(entity.queueNames);
        }
        LOG.info("Successfully started listener/dispatcher for pid: {}", pid);
    }

    @Override
    public void deleted(String pid) {
        final Entity entity = this.entities.remove(pid);
        if (entity.healthCheck != null) {
            entity.healthCheck.unregister();
        }
        if (entity.listener != null) {
            LOG.info("Stopping listener for pid: {}", pid);
            try {
                entity.listener.stop();
            } catch (InterruptedException e) {
                LOG.error("Error occurred while stopping listener for pid: {}", pid, e);
            }
        }
        if (entity.queueNames != null) {
            stopQueues(entity.queueNames);
        }
    }

    public void init() {
        LOG.info("ListenerManager started.");
    }

    public void destroy() {
        new ArrayList<>(this.entities.keySet()).forEach(pid -> deleted(pid));
        LOG.info("ListenerManager stopped.");
    }

    public MessageDispatcherFactory getMessageDispatcherFactory() {
        return messageDispatcherFactory;
    }

    public void setMessageDispatcherFactory(MessageDispatcherFactory messageDispatcherFactory) {
        this.messageDispatcherFactory = messageDispatcherFactory;
    }

    public DistPollerDao getDistPollerDao() {
        return distPollerDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        this.distPollerDao = distPollerDao;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setTelemetryRegistry(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = telemetryRegistry;
    }

    private void stopQueues(Set<String> queueNames) {
        Objects.requireNonNull(queueNames);
        for (String queueName : queueNames) {
            try {
                final AsyncDispatcher<TelemetryMessage> dispatcher = telemetryRegistry.getDispatcher(queueName);
                dispatcher.close();
            } catch (Exception ex) {
                LOG.error("Failed to close dispatcher.", ex);
            } finally {
                telemetryRegistry.removeDispatcher(queueName);
            }
        }
    }

}
