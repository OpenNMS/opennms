/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
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

        queue = new LinkedBlockingQueue<Runnable>(asyncPolicy.getQueueSize());
        state.getMetrics().register(MetricRegistry.name(state.getModule().getId(), "queue-size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return queue.size();
            }
        });
        final Counter droppedCounter = state.getMetrics().counter(MetricRegistry.name(state.getModule().getId(), "dropped"));

        executor = new ThreadPoolExecutor(
                asyncPolicy.getNumThreads(),
                asyncPolicy.getNumThreads(),
                1000L,
                TimeUnit.MILLISECONDS,
                queue,
                new LogPreservingThreadFactory("OpenNMS.Sink.AsyncDispatcher." + state.getModule().getId(), Integer.MAX_VALUE),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        rateLimittedLogger.warn("Task was rejected. Dropping message for {}.", state.getModule().getId());
                        droppedCounter.inc();
                    }
                }
            );
    }

    @Override
    public CompletableFuture<S> send(S message) {
        return CompletableFuture.supplyAsync(() -> {
            syncDispatcher.send(message);
            return message;
        }, executor);
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
