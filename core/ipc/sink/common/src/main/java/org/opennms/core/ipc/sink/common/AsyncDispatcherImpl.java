/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.common;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.DispatchQueueFactory;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.opennms.core.ipc.sink.offheap.DispatchQueueServiceLoader;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import com.swrve.ratelimitedlogger.RateLimitedLog;

public class AsyncDispatcherImpl<W, S extends Message, T extends Message> implements AsyncDispatcher<S> {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncDispatcherImpl.class);
    private final SyncDispatcher<S> syncDispatcher;
    private final AsyncPolicy asyncPolicy;
    private final Counter droppedCounter;

    private final Map<String, CompletableFuture<DispatchStatus>> futureMap = new ConcurrentHashMap<>();
    private final AtomicResultQueue<S> atomicResultQueue;
    private final AtomicLong missedFutures = new AtomicLong(0);
    private final AtomicInteger activeDispatchers = new AtomicInteger(0);
    
    private final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private final ExecutorService executor;

    public AsyncDispatcherImpl(DispatcherState<W, S, T> state, AsyncPolicy asyncPolicy,
                               SyncDispatcher<S> syncDispatcher) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(asyncPolicy);
        Objects.requireNonNull(syncDispatcher);
        this.syncDispatcher = syncDispatcher;
        this.asyncPolicy = asyncPolicy;
        SinkModule<S, T> sinkModule = state.getModule();
        Optional<DispatchQueueFactory> factory = DispatchQueueServiceLoader.getDispatchQueueFactory();

        DispatchQueue<S> dispatchQueue;
        if (factory.isPresent()) {
            LOG.debug("Using queue from factory");
            dispatchQueue = factory.get().getQueue(asyncPolicy, sinkModule.getId(),
                    sinkModule::marshalSingleMessage, sinkModule::unmarshalSingleMessage);
        } else {
            int size = asyncPolicy.getQueueSize();
            LOG.debug("Using default in memory queue of size {}", size);
            dispatchQueue = new DefaultQueue<>(size);
        }
        atomicResultQueue = new AtomicResultQueue<>(dispatchQueue);

        state.getMetrics().register(MetricRegistry.name(state.getModule().getId(), "queue-size"),
                (Gauge<Integer>) activeDispatchers::get);

        droppedCounter = state.getMetrics().counter(MetricRegistry.name(state.getModule().getId(), "dropped"));

        executor = Executors.newFixedThreadPool(asyncPolicy.getNumThreads(),
                new LogPreservingThreadFactory(SystemInfoUtils.DEFAULT_INSTANCE_ID + ".Sink.AsyncDispatcher." +
                        state.getModule().getId(), Integer.MAX_VALUE));
        startDrainingQueue();
    }

    private void dispatchFromQueue() {
        while (true) {
            try {
                LOG.trace("Asking dispatch queue for the next entry...");
                Map.Entry<String, S> messageEntry = atomicResultQueue.dequeue();
                LOG.trace("Received message entry from dispatch queue {}", messageEntry);
                activeDispatchers.incrementAndGet();
                LOG.trace("Sending message {} via sync dispatcher", messageEntry);
                syncDispatcher.send(messageEntry.getValue());
                LOG.trace("Successfully sent message {}", messageEntry);

                if (messageEntry.getKey() != null) {
                    LOG.trace("Attempting to complete future for message {}", messageEntry);
                    CompletableFuture<DispatchStatus> messageFuture = futureMap.remove(messageEntry.getKey());

                    if (messageFuture != null) {
                        messageFuture.complete(DispatchStatus.DISPATCHED);
                        LOG.trace("Completed future for message {}", messageEntry);
                    } else {
                        RATE_LIMITED_LOGGER.warn("No future found for message {}", messageEntry);
                        missedFutures.incrementAndGet();
                    }
                } else {
                    LOG.trace("Dequeued an entry with a null key");
                }

                activeDispatchers.decrementAndGet();
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                RATE_LIMITED_LOGGER.warn("Encountered exception while taking from dispatch queue", e);
            }
        }
    }

    private void startDrainingQueue() {
        for (int i = 0; i < asyncPolicy.getNumThreads(); i++) {
            executor.execute(this::dispatchFromQueue);
        }
    }

    @Override
    public CompletableFuture<DispatchStatus> send(S message) {
        CompletableFuture<DispatchStatus> sendFuture = new CompletableFuture<>();

        if (!asyncPolicy.isBlockWhenFull() && atomicResultQueue.isFull()) {
            droppedCounter.inc();
            sendFuture.completeExceptionally(new RuntimeException("Dispatch queue full"));
            return sendFuture;
        }

        try {
            String newId = UUID.randomUUID().toString();
            futureMap.put(newId, sendFuture);
            atomicResultQueue.enqueue(message, newId, result -> {
                LOG.trace("Result of enqueueing for Id {} was {}", newId, result);

                if (result == DispatchQueue.EnqueueResult.DEFERRED) {
                    futureMap.remove(newId);
                    sendFuture.complete(DispatchStatus.QUEUED);
                }
            });
        } catch (WriteFailedException e) {
            sendFuture.completeExceptionally(e);
        }

        return sendFuture;
    }

    @VisibleForTesting
    public long getMissedFutures() {
        return missedFutures.get();
    }

    /**
     * This class serves to ensure operations of enqueueing a message and acting on the result of that enqueue are done
     * atomically from the point of view of any thread calling dequeue.
     */
    private final static class AtomicResultQueue<T> {
        private final Map<String, CountDownLatch> resultRecordedMap = new ConcurrentHashMap<>();
        private final DispatchQueue<T> dispatchQueue;

        public AtomicResultQueue(DispatchQueue<T> dispatchQueue) {
            this.dispatchQueue = Objects.requireNonNull(dispatchQueue);
        }

        void enqueue(T message, String key, Consumer<DispatchQueue.EnqueueResult> onEnqueue) throws WriteFailedException {
            CountDownLatch resultRecorded = new CountDownLatch(1);
            resultRecordedMap.put(key, resultRecorded);
            DispatchQueue.EnqueueResult result = dispatchQueue.enqueue(message, key);

            // When the result is DEFERRED we should not track the future so remove it from the map
            if (result == DispatchQueue.EnqueueResult.DEFERRED) {
                resultRecordedMap.remove(key);
            }

            onEnqueue.accept(result);
            resultRecorded.countDown();
        }

        Map.Entry<String, T> dequeue() throws InterruptedException {
            Map.Entry<String, T> messageEntry = dispatchQueue.dequeue();

            // If the key is null, we weren't tracking it so we don't need to synchronize
            if (messageEntry.getKey() == null) {
                return messageEntry;
            }
            CountDownLatch resultRecorded = resultRecordedMap.remove(messageEntry.getKey());
            if(resultRecorded != null) {
                resultRecorded.await();
            }

            return messageEntry;
        }

        boolean isFull() {
            return dispatchQueue.isFull();
        }

        int getSize() {
            return dispatchQueue.getSize();
        }
    }
    
    @Override
    public int getQueueSize() {
        return atomicResultQueue.getSize();
    }

    @Override
    public void close() throws Exception {
        syncDispatcher.close();
        executor.shutdown();
    }

    /**
     * This class is intended to be used only when a suitable implementation could not be found at runtime. This should
     * only occur in testing.
     */
    private static class DefaultQueue<T> implements DispatchQueue<T> {
        private final BlockingQueue<Map.Entry<String, T>> queue;

        DefaultQueue(int size) {
            queue = new LinkedBlockingQueue<>(size);
        }

        @Override
        public EnqueueResult enqueue(T message, String key) throws WriteFailedException {
            try {
                queue.put(new AbstractMap.SimpleImmutableEntry<>(key, message));
                return EnqueueResult.IMMEDIATE;
            } catch (InterruptedException e) {
                throw new WriteFailedException(e);
            }
        }

        @Override
        public Map.Entry<String, T> dequeue() throws InterruptedException {
            return queue.take();
        }

        @Override
        public boolean isFull() {
            return queue.remainingCapacity() <= 0;
        }

        @Override
        public int getSize() {
            return queue.size();
        }
    }

}
