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
package org.opennms.core.ipc.sink.common;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.logging.Logging;
import org.opennms.core.logging.Logging.MDCCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public abstract class AbstractMessageConsumerManager implements MessageConsumerManager {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMessageConsumerManager.class);

    public static final String SINK_INITIAL_SLEEP_TIME = "org.opennms.core.ipc.sink.initialSleepTime";

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("consumer-starter-%d")
            .build();

    protected final ExecutorService startupExecutor = Executors.newCachedThreadPool(threadFactory);

    private final Multimap<SinkModule<?, Message>, MessageConsumer<?, Message>> consumersByModule = LinkedListMultimap.create();

    protected abstract void startConsumingForModule(SinkModule<?, Message> module) throws Exception;

    protected abstract void stopConsumingForModule(SinkModule<?, Message> module) throws Exception;

    public final CompletableFuture<Void> waitForStartup;

    protected AbstractMessageConsumerManager() {
        // By default, do not introduce any delay on startup
        CompletableFuture<Void> startupFuture = CompletableFuture.completedFuture(null);
        String initialSleepString = System.getProperty(SINK_INITIAL_SLEEP_TIME, "0");
        try {
            int initialSleep = Integer.parseInt(initialSleepString);
            if (initialSleep > 0) {
                startupFuture = CompletableFuture.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(initialSleep);
                        } catch (InterruptedException e) {
                            LOG.warn(e.getMessage(), e);
                        }
                    }
                }, startupExecutor);
            }
        } catch (NumberFormatException e) {
            LOG.warn("Invalid value for system property {}: {}", SINK_INITIAL_SLEEP_TIME, initialSleepString);
        }
        waitForStartup = startupFuture;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S,T> module, T message) {
        consumersByModule.get((SinkModule<?,Message>)module)
            .forEach(c -> c.handleMessage(message));
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <S extends Message, T extends Message> void registerConsumer(MessageConsumer<S, T> consumer)
            throws Exception {
        if (consumer == null) {
            return;
        }

        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            LOG.info("Registering consumer: {}", consumer);
            final SinkModule<?, Message> module = (SinkModule<?, Message>)consumer.getModule();
            final int numConsumersBefore = consumersByModule.get(module).size();
            consumersByModule.put(module, (MessageConsumer<?, Message>)consumer);
            if (numConsumersBefore < 1) {
                waitForStartup.thenRunAsync(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LOG.info("Starting to consume messages for module: {}", module.getId());
                            startConsumingForModule(module);
                        } catch (Exception e) {
                            LOG.error("Unexpected exception while trying to start consumer for module: {}", module.getId(), e);
                        }
                    }
                }, startupExecutor);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <S extends Message, T extends Message> void unregisterConsumer(MessageConsumer<S, T> consumer)
            throws Exception {
        if (consumer == null) {
            return;
        }

        try (MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            LOG.info("Unregistering consumer: {}", consumer);
            final SinkModule<?, Message> module = (SinkModule<?, Message>)consumer.getModule();
            consumersByModule.remove(module, (MessageConsumer<?, Message>)consumer);
            if (consumersByModule.get(module).size() < 1) {
                waitForStartup.thenRunAsync(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LOG.info("Stopping consumption of messages for module: {}", module.getId());
                            stopConsumingForModule(module);
                        } catch (Exception e) {
                            LOG.error("Unexpected exception while trying to stop consumer for module: {}", module.getId(), e);
                        }
                    }
                });
            }
        }
    }

    public synchronized void unregisterAllConsumers() throws Exception {
        // Copy the list of consumers before we iterate to avoid concurrent modification exceptions
        final List<MessageConsumer<?, Message>> consumers = Lists.newArrayList(consumersByModule.values());
        for (MessageConsumer<?, Message> consumer : consumers) {
            unregisterConsumer(consumer);
        }
    }

    public static int getNumConsumerThreads(SinkModule<?, ?> module) {
        Objects.requireNonNull(module);
        final int defaultValue = Runtime.getRuntime().availableProcessors() * 2;
        final int configured = module.getNumConsumerThreads();
        if (configured <= 0) {
            LOG.warn("Number of consumer threads for module {} was {}. Value must be > 0. Falling back to {}", module.getId(), configured, defaultValue);
            return defaultValue;
        }
        return configured;
    }

    public ExecutorService getStartupExecutor() {
        return startupExecutor;
    }
}
