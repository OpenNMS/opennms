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
import java.util.Set;

import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
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

    private static class Entity {
        private Listener listener = null;
        private Set<Parser> parsers = new HashSet<>();
        private Set<AsyncDispatcher<TelemetryMessage>> dispatchers = new HashSet<>();
        private ServiceRegistration<HealthCheck> healthCheck = null;
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

        final Entity entity = new Entity();

        // Convert the dictionary to a map
        final PropertyTree definition = PropertyTree.from(properties);

        // Build the protocol and listener definitions
        final MapBasedListenerDef listenerDef = new MapBasedListenerDef(definition);

        // Register health check
        final ListenerHealthCheck healthCheck = new ListenerHealthCheck(listenerDef);
        entity.healthCheck = bundleContext.registerService(HealthCheck.class, healthCheck, null);

        try {
            // TODO MVR
//            final Listener.Factory listenerFactory = Beans.createFactory(Listener.Factory.class, listenerDef.getClassName());
//
//            // Create Parsers
//            for (final MapBasedParserDef parserDef : listenerDef.getParsers()) {
//                final TelemetrySinkModule sinkModule = new TelemetrySinkModule(parserDef);
//                sinkModule.setDistPollerDao(distPollerDao);
//                final AsyncDispatcher<TelemetryMessage> dispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
//
//                entity.dispatchers.add(dispatcher);
//
//                final Parser parser = listenerFactory.parser(parserDef).create(dispatcher);
//                entity.parsers.add(parser);
//            }
//
//            entity.listener = listenerFactory.create(listenerDef.getName(), listenerDef.getParameterMap(), entity.parsers);
            entity.listener.start();

            // At this point the listener should be up and running,
            // so we mark the underlying health check as success
            healthCheck.markSucess();

        } catch (Exception e) {
            // In case of error, we mark the health check as failure as well
            healthCheck.markError(e);
            LOG.error("Failed to build listener.", e);
            try {
                for (final AsyncDispatcher<TelemetryMessage> dispatcher : entity.dispatchers) {
                    dispatcher.close();
                }
            } catch (Exception ee) {
                LOG.error("Failed to close dispatcher.", e);
            }
        }

        LOG.info("Successfully started listener/dispatcher for pid: {}", pid);
    }

    @Override
    public void deleted(String pid) {
        final Entity entity = this.entities.remove(pid);

        entity.healthCheck.unregister();

        if (entity.listener != null) {
            LOG.info("Stopping listener for pid: {}", pid);
            try {
                entity.listener.stop();
            } catch (InterruptedException e) {
                LOG.error("Error occurred while stopping listener for pid: {}", pid, e);
            }
        }

        for (final Parser parser : entity.parsers) {
            LOG.info("Closing parser for pid: {}", pid);
            try {
                parser.stop();
            } catch (Exception e) {
                LOG.error("Error occurred while closing parser for pid: {}", pid, e);
            }
        }

        for (final AsyncDispatcher<TelemetryMessage> dispatcher : entity.dispatchers) {
            LOG.info("Closing dispatcher for pid: {}", pid);
            try {
                dispatcher.close();
            } catch (Exception e) {
                LOG.error("Error occurred while closing dispatcher for pid: {}", pid, e);
            }
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
}
