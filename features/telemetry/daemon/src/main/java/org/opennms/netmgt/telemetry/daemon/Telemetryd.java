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

package org.opennms.netmgt.telemetry.daemon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.daemon.DaemonTools;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.common.ipc.TelemetrySinkModule;
import org.opennms.netmgt.telemetry.config.dao.TelemetrydConfigDao;
import org.opennms.netmgt.telemetry.config.model.AdapterConfig;
import org.opennms.netmgt.telemetry.config.model.ListenerConfig;
import org.opennms.netmgt.telemetry.config.model.QueueConfig;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfig;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * telemetryd is responsible for managing the life cycle of
 * {@link Listener}s and {@link Adapter}s as well as connecting
 * both of these to the Sink API.
 *
 * @author jwhite
 */
@EventListener(name=Telemetryd.NAME, logPrefix=Telemetryd.LOG_PREFIX)
public class Telemetryd implements SpringServiceDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(Telemetryd.class);

    public static final String NAME = "Telemetryd";

    public static final String LOG_PREFIX = "telemetryd";

    @Autowired
    private TelemetrydConfigDao telemetrydConfigDao;

    @Autowired
    private MessageDispatcherFactory messageDispatcherFactory;

    @Autowired
    private MessageConsumerManager messageConsumerManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TelemetryRegistry telemetryRegistry;

    private List<TelemetryMessageConsumer> consumers = new ArrayList<>();
    private List<Listener> listeners = new ArrayList<>();


    @Override
    public synchronized void start() throws Exception {
        if (consumers.size() > 0) {
            throw new IllegalStateException(NAME + " is already started.");
        }
        LOG.info("{} is starting.", NAME);
        final TelemetrydConfig config = telemetrydConfigDao.getContainer().getObject();
        final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();

        // First we create the queues as parsers may reference them
        for (final QueueConfig queueConfig : config.getQueues()) {
            // Create a Sink module using the queue definition.
            // This allows for queue to have their respective queues and thread
            // related settings to help limit the impact of one adapter on another.
            final TelemetrySinkModule sinkModule = new TelemetrySinkModule(queueConfig);
            beanFactory.autowireBean(sinkModule);
            beanFactory.initializeBean(sinkModule, "sinkModule");

            // Create the consumer of there are any adapters, but don't start it yet
            final List<AdapterConfig> enabledAdapters = queueConfig.getAdapters().stream().filter(AdapterConfig::isEnabled).collect(Collectors.toList());
            if (!enabledAdapters.isEmpty()) {
                final TelemetryMessageConsumer consumer = new TelemetryMessageConsumer(queueConfig, enabledAdapters, sinkModule);
                beanFactory.autowireBean(consumer);
                beanFactory.initializeBean(consumer, "consumer");
                consumers.add(consumer);
            } else {
                LOG.debug("Skipping consumer for queue: {} (no adapters enabled/defined)", queueConfig.getName());
            }

            // Build the dispatcher
            final AsyncDispatcher<TelemetryMessage> dispatcher = messageDispatcherFactory.createAsyncDispatcher(sinkModule);
            telemetryRegistry.registerDispatcher(queueConfig.getName(), dispatcher);
        }

        // Create listeners AND parsers
        for (final ListenerConfig listenerConfig : config.getListeners()) {
            if (!listenerConfig.isEnabled()) {
                LOG.debug("Skipping disabled listener: {}", listenerConfig.getName());
                continue;
            }
            if (listenerConfig.getParsers().isEmpty()) {
                LOG.debug("Skipping listener with no parsers: {}", listenerConfig.getName());
                continue;
            }
            final Listener listener = telemetryRegistry.getListener(listenerConfig);
            listeners.add(listener);
        }

        // Start the consumers
        for (TelemetryMessageConsumer consumer : consumers) {
            LOG.info("Starting consumer for {} adapter.", consumer.getQueue().getName());
            messageConsumerManager.registerConsumer(consumer);
        }

        // Start the listeners
        for (Listener listener : listeners) {
            LOG.info("Starting {} listener.", listener.getName());
            listener.start();
        }

        LOG.info("{} is started.", NAME);
    }

    @Override
    public synchronized void destroy() {
        LOG.info("{} is stopping.", NAME);

        // Stop the listeners
        for (Listener listener : listeners) {
            try {
                LOG.info("Stopping {} listener.", listener.getName());
                listener.stop();
            } catch (InterruptedException e) {
                LOG.warn("Error while stopping listener.", e);
            }
        }
        listeners.clear();

        // Stop the dispatchers
        for (AsyncDispatcher<?> dispatcher : telemetryRegistry.getDispatchers()) {
            try {
                LOG.info("Closing dispatcher.", dispatcher);
                dispatcher.close();
            } catch (Exception e) {
                LOG.warn("Error while closing dispatcher.", e);
            }
        }
        telemetryRegistry.clearDispatchers();

        final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        // Stop the consumers
        for (TelemetryMessageConsumer consumer : consumers) {
            try {
                LOG.info("Stopping consumer for {} protocol.", consumer.getQueue().getName());
                messageConsumerManager.unregisterConsumer(consumer);
            } catch (Exception e) {
                LOG.error("Error while stopping consumer.", e);
            }
            beanFactory.destroyBean(consumer);
        }
        consumers.clear();

        LOG.info("{} is stopped.", NAME);
    }

    @Override
    public void afterPropertiesSet() {
        // pass
    }

    private synchronized void handleConfigurationChanged() {
        destroy();
        try {
            start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadEvent(Event e) {
        DaemonTools.handleReloadEvent(e, Telemetryd.NAME, (event) -> handleConfigurationChanged());
    }

}
