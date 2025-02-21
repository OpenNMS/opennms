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
package org.opennms.netmgt.telemetry.daemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.telemetry.api.TelemetryManager;
import org.opennms.netmgt.telemetry.api.receiver.GracefulShutdownListener;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * telemetryd is responsible for managing the life cycle of
 * {@link Listener}s, {@link org.opennms.netmgt.telemetry.api.receiver.Connector}s and {@link Adapter}s
 * as well as connecting these to the Sink API.
 *
 * @author jwhite
 */
@EventListener(name=Telemetryd.NAME, logPrefix=Telemetryd.LOG_PREFIX)
public class Telemetryd implements SpringServiceDaemon, TelemetryManager {
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

    @Autowired
    private ConnectorManager connectorManager;

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
            if (listener == null) {
                throw new IllegalStateException("Failed to create listener from registry for listener named: " + listenerConfig.getName());
            }
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

        // Start the connectors
        if (!config.getConnectors().isEmpty()) {
            LOG.info("Starting connectors.");
            connectorManager.start(config);
        }

        LOG.info("{} is started.", NAME);
    }

    @Override
    public synchronized void destroy() {
        LOG.info("{} is stopping.", NAME);

        List<Future<?>> stopFutures = new ArrayList<>();

        // Stop the listeners
        for (Listener listener : listeners) {
            try {
                LOG.info("Stopping {} listener.", listener.getName());
                listener.stop();
                if (listener instanceof GracefulShutdownListener) {
                    Future<?> future = ((GracefulShutdownListener) listener).getShutdownFuture();
                    if (future == null) {
                        LOG.warn("Shutdown future is missing for {}.", listener.getName());
                    } else {
                        stopFutures.add(future);
                    }
                }
            } catch (InterruptedException e) {
                LOG.warn("Error while stopping listener.", e);
            }
        }
        listeners.clear();

        // wait for 60s (overridable by property)
        try {
            this.waitForStop(stopFutures, SystemProperties.getInteger("org.opennms.features.telemetry.shutdownTimeout", 60));
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Error while waiting stop future.", e);
        }

        // Stop the connectors
        LOG.info("Stopping connectors.");
        connectorManager.stop();

        // Stop the dispatchers
        for (AsyncDispatcher<?> dispatcher : telemetryRegistry.getDispatchers()) {
            try {
                LOG.info("Closing dispatcher: {}", dispatcher);
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

    /**
     * It will wait for the futures by maxTime (second)
     * @param futures shutdown future
     * @param maxTime
     */
    private void waitForStop(List<Future<?>> futures, int maxTime) throws ExecutionException, InterruptedException {
        Objects.requireNonNull(futures);
        Object lock = new Object();
        synchronized (lock) {
            int i = 0;
            while (i < maxTime) {
                boolean allDone = true;
                for (Future<?> future : futures) {
                    if (!future.isDone()) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) {
                    if (LOG.isInfoEnabled()) {
                        StringBuilder builder = new StringBuilder();
                        for (Future<?> future : futures) {
                            builder.append(future.get());
                            builder.append(" ");
                        }
                        LOG.info("Future done time: {}s output: {}", i, builder);
                    }
                    return;
                }
                lock.wait(1000L);
                i++;
            }
            LOG.warn("Fail to wait for stop future. Futures: {} maxTime: {}", futures, maxTime);
        }
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
    public void handleReloadEvent(IEvent e) {
        DaemonTools.handleReloadEvent(e, Telemetryd.NAME, (event) -> handleConfigurationChanged());
    }

    @Override
    public List<Listener> getListeners() {
        return this.listeners;
    }

    @Override
    public List<Adapter> getAdapters() {
        return this.consumers.stream()
                .flatMap(consumer -> consumer.getAdapters().stream())
                .collect(Collectors.toList());
    }
}
