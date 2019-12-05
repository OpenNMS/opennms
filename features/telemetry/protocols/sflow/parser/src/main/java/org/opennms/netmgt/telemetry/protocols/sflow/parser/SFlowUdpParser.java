/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.sflow.parser;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bson.BsonBinaryWriter;
import org.bson.io.BasicOutputBuffer;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.telemetry.listeners.Dispatchable;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpParser;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.DatagramVersion;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleDatagram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

public class SFlowUdpParser implements UdpParser, Dispatchable {

    private static final Logger LOG = LoggerFactory.getLogger(SFlowUdpParser.class);

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private final ThreadLocal<Boolean> isParserThread = new ThreadLocal<>();

    private final ThreadFactory threadFactory;

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    private final SampleDatagramEnricher enricher;

    private int threads = DEFAULT_NUM_THREADS;
	
    private boolean dnsLookupsEnabled = true;
	
    private ExecutorService executor;

    public SFlowUdpParser(final String name,
                          final AsyncDispatcher<TelemetryMessage> dispatcher,
                          final DnsResolver dnsResolver) {
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);

        // Create a thread factory that sets a thread local variable when the thread is created
        // This variable is used to identify the thread as one that belongs to this class
        final LogPreservingThreadFactory logPreservingThreadFactory = new LogPreservingThreadFactory("Telemetryd-sFlow-" + name, Integer.MAX_VALUE);
        threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return logPreservingThreadFactory.newThread(() -> {
                    isParserThread.set(true);
                    r.run();
                });
            }
        };

        enricher = new SampleDatagramEnricher(dnsResolver, getDnsLookupsEnabled());
    }

    @Override
    public boolean handles(final ByteBuf buffer) {
        return uint32(buffer) == DatagramVersion.VERSION5.value;
    }

    @Override
    public CompletableFuture<?> parse(final ByteBuf buffer,
                                      final InetSocketAddress remoteAddress,
                                      final InetSocketAddress localAddress) throws Exception {
        final SampleDatagram packet = new SampleDatagram(buffer);

        LOG.trace("Got packet: {}", packet);

        final CompletableFuture<TelemetryMessage> future = new CompletableFuture<>();
        executor.execute(() -> {
            enricher.enrich(packet).whenComplete((enrichment,ex) -> {
                if (ex != null) {
                    // Enrichment failed
                    future.completeExceptionally(ex);
                    return;
                }
                // Enrichment was successful

                // We're currently in the callback thread from the enrichment process
                // We want the remainder of the serialization and dispatching to be performed
                // from one of our executor threads so that we can put back-pressure on the listener
                // if we can't keep up
                final Runnable dispatch = () -> {
                    // Serialize
                    final BasicOutputBuffer output = new BasicOutputBuffer();
                    try (final BsonBinaryWriter bsonWriter = new BsonBinaryWriter(output)) {
                        bsonWriter.writeStartDocument();

                        bsonWriter.writeName("time");
                        bsonWriter.writeInt64(System.currentTimeMillis());

                        bsonWriter.writeName("data");
                        packet.version.datagram.writeBson(bsonWriter, enrichment);

                        bsonWriter.writeEndDocument();
                    }

                    // Build the message to be sent
                    final TelemetryMessage msg = new TelemetryMessage(remoteAddress, output.getByteBuffers().get(0).asNIO());
                    dispatcher.send(msg).whenComplete((any, exx) -> {
                        if (exx != null) {
                            // Dispatching failed
                            future.completeExceptionally(exx);
                        }
                        future.complete(any);
                    });
                };

                // It's possible that the callback thread is already a thread from the pool, if that's the case
                // execute within the current thread. This helps avoid deadlocks.
                if (Boolean.TRUE.equals(isParserThread.get())) {
                    dispatch.run();
                } else {
                    // We're not in one of the parsers threads, execute the dispatch in the pool
                    executor.execute(dispatch);
                }
            });
        });
        return future;
    }

    public boolean getDnsLookupsEnabled() {
        return dnsLookupsEnabled;
    }

    public void setDnsLookupsEnabled(boolean dnsLookupsEnabled) {
        this.dnsLookupsEnabled = dnsLookupsEnabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start(final ScheduledExecutorService executorService) {
        executor = new ThreadPoolExecutor(
                // corePoolSize must be > 0 since we use the RejectedExecutionHandler to block when the queue is full
                1, threads,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(true),
                threadFactory,
                (r, executor) -> {
                    // We enter this block when the queue is full and the caller is attempting to submit additional tasks
                    try {
                        // If we're not shutdown, then block until there's room in the queue
                        if (!executor.isShutdown()) {
                            executor.getQueue().put(r);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RejectedExecutionException("Executor interrupted while waiting for capacity in the work queue.", e);
                    }
                });
    }

    @Override
    public void stop() {
        executor.shutdown();
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("Threads must be >= 1");
        }
        this.threads = threads;
    }

}
