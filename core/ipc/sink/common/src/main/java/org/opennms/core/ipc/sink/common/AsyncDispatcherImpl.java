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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.swrve.ratelimitedlogger.RateLimitedLog;

public class AsyncDispatcherImpl<W, S extends Message, T extends Message> implements AsyncDispatcher<S>  {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncDispatcherImpl.class);

    private final SyncDispatcher<S> syncDispatcher;

    final RateLimitedLog rateLimittedLogger = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.standardSeconds(30))
            .build();

    final LinkedBlockingQueue<Runnable> queue;
    final ExecutorService executor;

    public AsyncDispatcherImpl(DispatcherState<W,S,T> state, AsyncPolicy asyncPolicy, SyncDispatcher<S> syncDispatcher) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(asyncPolicy);
        this.syncDispatcher = Objects.requireNonNull(syncDispatcher);

        final RejectedExecutionHandler rejectedExecutionHandler;
        if (asyncPolicy.isBlockWhenFull()) {
            // This queue ensures that calling thread is blocked when the queue is full
            // See the implementation of OfferBlockingQueue for details
            queue = new OfferBlockingQueue<>(asyncPolicy.getQueueSize());
            rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
        } else {
            queue = new LinkedBlockingQueue<Runnable>(asyncPolicy.getQueueSize());
            // Reject and increase the dropped counter when the queue is full
            final Counter droppedCounter = state.getMetrics().counter(MetricRegistry.name(state.getModule().getId(), "dropped"));
            rejectedExecutionHandler = new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    droppedCounter.inc();
                    throw new RejectedExecutionException("Task " + r.toString() +
                            " rejected from " +
                            e.toString());
                }
            };
        }

        state.getMetrics().register(MetricRegistry.name(state.getModule().getId(), "queue-size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return queue.size();
            }
        });

        executor = new ThreadPoolExecutor(
                asyncPolicy.getNumThreads(),
                asyncPolicy.getNumThreads(),
                1000L,
                TimeUnit.MILLISECONDS,
                queue,
                new LogPreservingThreadFactory(SystemInfoUtils.DEFAULT_INSTANCE_ID + ".Sink.AsyncDispatcher." + state.getModule().getId(), Integer.MAX_VALUE),
                rejectedExecutionHandler
            );
    }

    /**
     * When used in a ThreadPoolExecutor, this queue will block calls to
     * {@link ThreadPoolExecutor#execute(Runnable)} when the queue is full.
     * This is done by overriding calls to {@link LinkedBlockingQueue#offer(Object)}
     * with calls to {@link LinkedBlockingQueue#put(Object)}, but comes with the caveat
     * that executor must be built with <code>corePoolSize == maxPoolSize</code>.
     * In the context of the {@link AsyncDispatcherImpl}, this is an acceptable caveat,
     * since we enforce the matching pool sizes.
     *
     * There are alternative ways of solving this problem, for example we could use a
     * {@link RejectedExecutionHandler} to achieve similar behavior, and allow
     * for <code>corePoolSize < maxPoolSize</code>, but not for <code>corePoolSize==0</code>.
     *
     * For further discussions on this topic see:
     *   http://stackoverflow.com/a/3518588
     *   http://stackoverflow.com/a/32123535
     *
     * If the implementation is changed, make sure that that executor is built accordingly.
     */
    private static class OfferBlockingQueue<E> extends LinkedBlockingQueue<E> {
        private static final long serialVersionUID = 1L;

        public OfferBlockingQueue(int capacity) {
            super(capacity);
        }

        @Override
        public boolean offer(E e) {
            try {
                put(e);
                return true;
            } catch(InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    @Override
    public CompletableFuture<S> send(S message) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                syncDispatcher.send(message);
                return message;
            }, executor);
        } catch (RejectedExecutionException ree) {
            final CompletableFuture<S> future = new CompletableFuture<>();
            future.completeExceptionally(ree);
            return future;
        }
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public void close() throws Exception {
        syncDispatcher.close();
        executor.shutdown();
    }
}
