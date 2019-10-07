/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;
import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.ListValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ParserBase implements Parser {
    private static final Logger LOG = LoggerFactory.getLogger(ParserBase.class);

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private static final long DEFAULT_CLOCK_SKEW_EVENT_RATE_SECONDS = TimeUnit.HOURS.toSeconds(1);

    public static final String CLOCK_SKEW_EVENT_UEI = "uei.opennms.org/internal/telemetry/clockSkewDetected";

    private final ThreadLocal<Boolean> isParserThread = new ThreadLocal<>();

    private final Protocol protocol;

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    private final EventForwarder eventForwarder;

    private final Identity identity;

    private final DnsResolver dnsResolver;

    private final Meter recordsDispatched;

    private final Timer recordEnrichmentTimer;

    private final ThreadFactory threadFactory;

    private int threads = DEFAULT_NUM_THREADS;

    private long maxClockSkew = 0;

    private long clockSkewEventRate = 0;

    private boolean dnsLookupsEnabled = true;

    private LoadingCache<InetAddress, Optional<Instant>> eventCache;

    private ExecutorService executor;

    public ParserBase(final Protocol protocol,
                      final String name,
                      final AsyncDispatcher<TelemetryMessage> dispatcher,
                      final EventForwarder eventForwarder,
                      final Identity identity,
                      final DnsResolver dnsResolver,
                      final MetricRegistry metricRegistry) {
        this.protocol = Objects.requireNonNull(protocol);
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
        this.identity = Objects.requireNonNull(identity);
        this.dnsResolver = Objects.requireNonNull(dnsResolver);
        Objects.requireNonNull(metricRegistry);

        // Create a thread factory that sets a thread local variable when the thread is created
        // This variable is used to identify the thread as one that belongs to this class
        final LogPreservingThreadFactory logPreservingThreadFactory = new LogPreservingThreadFactory("Telemetryd-" + protocol + "-" + name, Integer.MAX_VALUE);
        threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return logPreservingThreadFactory.newThread(() -> {
                    isParserThread.set(true);
                    r.run();
                });
            }
        };

        recordsDispatched = metricRegistry.meter(MetricRegistry.name("parsers",  name, "recordsDispatched"));
        recordEnrichmentTimer = metricRegistry.timer(MetricRegistry.name("parsers",  name, "recordEnrichment"));

        // Call setters since these also perform additional handling
        setClockSkewEventRate(DEFAULT_CLOCK_SKEW_EVENT_RATE_SECONDS);
        setThreads(DEFAULT_NUM_THREADS);
    }

    @Override
    public void start(ScheduledExecutorService executorService) {
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

    @Override
    public String getName() {
        return this.name;
    }

    public void setMaxClockSkew(final long maxClockSkew) {
        this.maxClockSkew = maxClockSkew;
    }

    public long getMaxClockSkew() {
        return this.maxClockSkew;
    }

    public long getClockSkewEventRate() {
        return clockSkewEventRate;
    }

    public void setClockSkewEventRate(final long clockSkewEventRate) {
        this.clockSkewEventRate = clockSkewEventRate;

        this.eventCache = CacheBuilder.newBuilder().expireAfterWrite(this.clockSkewEventRate, TimeUnit.SECONDS).build(new CacheLoader<InetAddress, Optional<Instant>>() {
            @Override
            public Optional<Instant> load(InetAddress key) throws Exception {
                return Optional.empty();
            }
        });
    }

    public boolean getDnsLookupsEnabled() {
        return dnsLookupsEnabled;
    }

    public void setDnsLookupsEnabled(boolean dnsLookupsEnabled) {
        this.dnsLookupsEnabled = dnsLookupsEnabled;
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

    protected CompletableFuture<?> transmit(final RecordProvider packet, final InetSocketAddress remoteAddress) {
        LOG.trace("Got packet: {}", packet);

        // Perform the record enrichment and serialization in a thread pool allowing these to be parallelized
        final CompletableFuture<CompletableFuture[]> futureOfFutures = CompletableFuture.supplyAsync(()-> {
            return packet.getRecords().map(record -> {
                final CompletableFuture<TelemetryMessage> future = new CompletableFuture<>();
                final Timer.Context timerContext = recordEnrichmentTimer.time();
                // Trigger record enrichment (performing DNS reverse lookups for example)
                final RecordEnricher recordEnricher = new RecordEnricher(dnsResolver, getDnsLookupsEnabled());
                recordEnricher.enrich(record).whenComplete((enrichment, ex) -> {
                    timerContext.close();
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
                        // Let's serialize
                        final ByteBuffer buffer = serializeRecords(this.protocol, record, enrichment);

                        // Build the message to dispatch
                        final TelemetryMessage msg = new TelemetryMessage(remoteAddress, buffer);

                        // Dispatch
                        dispatcher.send(msg).whenComplete((b,exx) -> {
                            if (exx != null) {
                                future.completeExceptionally(exx);
                                return;
                            }
                            future.complete(b);
                        });

                        recordsDispatched.mark();
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
                return future;
            }).toArray(CompletableFuture[]::new);
        }, executor);

        // Return a future which is completed when all records are finished dispatching (i.e. written to Kafka)
        final CompletableFuture<Void> future = new CompletableFuture<>();
        futureOfFutures.whenComplete((futures,ex) -> {
            if (ex != null) {
                LOG.warn("Error preparing records for dispatch.", ex);
                future.completeExceptionally(ex);
                return;
            }
            // Dispatch was triggered for all the records, now wait for the dispatching to complete
            CompletableFuture.allOf(futures).whenComplete((any,exx) -> {
                if (exx != null) {
                    LOG.warn("One or more of the records were not successfully dispatched.", exx);
                    future.completeExceptionally(exx);
                    return;
                }
                // All of the records have been successfully dispatched
                future.complete(any);
            });
        });
        return future;
    }

    @VisibleForTesting
    public static ByteBuffer serialize(final Protocol protocol, final Iterable<Value<?>> record) {
        return serialize(protocol, record, new RecordEnrichment() {
            @Override
            public Optional<String> getHostnameFor(InetAddress srcAddress) {
                return Optional.empty();
            }
        });
    }

    private static ByteBuffer serialize(final Protocol protocol, final Iterable<Value<?>> record, final RecordEnrichment enrichment) {
        // Build BSON document from flow
        final BasicOutputBuffer output = new BasicOutputBuffer();
        try (final BsonBinaryWriter writer = new BsonBinaryWriter(output)) {
            writer.writeStartDocument();
            writer.writeInt32("@version", protocol.version);

            final FlowBuilderVisitor visitor = new FlowBuilderVisitor(writer, enrichment);
            for (final Value<?> value : record) {
                value.visit(visitor);
            }

            writer.writeEndDocument();
        }

        return output.getByteBuffers().get(0).asNIO();
    }

    private ByteBuffer serializeRecords(final Protocol protocol, final Iterable<Value<?>> record, final RecordEnrichment enrichment) {
        return serialize(protocol, record, enrichment);
    }

    protected void detectClockSkew(final long packetTimestampMs, final InetAddress remoteAddress) {
        if (getMaxClockSkew() > 0) {
            long deltaMs = Math.abs(packetTimestampMs - System.currentTimeMillis());
            if (deltaMs > getMaxClockSkew() * 1000L) {
                final Optional<Instant> instant = eventCache.getUnchecked(remoteAddress);

                if (!instant.isPresent() || Duration.between(instant.get(), Instant.now()).getSeconds() > getClockSkewEventRate()) {
                    eventCache.put(remoteAddress, Optional.of(Instant.now()));

                    eventForwarder.sendNow(new EventBuilder()
                            .setUei(CLOCK_SKEW_EVENT_UEI)
                            .setTime(new Date())
                            .setSource(getName())
                            .setInterface(remoteAddress)
                            .setDistPoller(identity.getId())
                            .addParam("monitoringSystemId", identity.getId())
                            .addParam("monitoringSystemLocation", identity.getLocation())
                            .setParam("delta", (int) deltaMs)
                            .setParam("clockSkewEventRate", (int) getClockSkewEventRate())
                            .setParam("maxClockSkew", (int) getMaxClockSkew())
                            .getEvent());
                }

            }
        }
    }

    private static class FlowBuilderVisitor implements Value.Visitor {
        // TODO: Really use ordinal for enums?

        private final BsonWriter writer;
        private final RecordEnrichment enrichment;

        public FlowBuilderVisitor(final BsonWriter writer, final RecordEnrichment enrichment) {
            this.writer = writer;
            this.enrichment = enrichment;
        }

        @Override
        public void accept(final NullValue value) {
            this.writer.writeNull(value.getName());
        }

        @Override
        public void accept(final BooleanValue value) {
            this.writer.writeBoolean(value.getName(), value.getValue());
        }

        @Override
        public void accept(final DateTimeValue value) {
            this.writer.writeStartDocument(value.getName());
            this.writer.writeInt64("epoch", value.getValue().getEpochSecond());
            if (value.getValue().getNano() != 0) {
                this.writer.writeInt64("nanos", value.getValue().getNano());
            }
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final FloatValue value) {
            this.writer.writeDouble(value.getName(), value.getValue());
        }

        @Override
        public void accept(final IPv4AddressValue value) {
            this.writer.writeStartDocument(value.getName());
            this.writer.writeString("address", value.getValue().getHostAddress());
            enrichment.getHostnameFor(value.getValue()).ifPresent((hostname) -> this.writer.writeString("hostname", hostname));
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final IPv6AddressValue value) {
            this.writer.writeStartDocument(value.getName());
            this.writer.writeString("address", value.getValue().getHostAddress());
            enrichment.getHostnameFor(value.getValue()).ifPresent((hostname) -> this.writer.writeString("hostname", hostname));
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final MacAddressValue value) {
            this.writer.writeStartDocument(value.getName());
            value.getSemantics().ifPresent(semantics -> {
                this.writer.writeInt32("s", semantics.ordinal());
            });
            this.writer.writeBinaryData("v", new BsonBinary(value.getValue()));
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final OctetArrayValue value) {
            this.writer.writeBinaryData(value.getName(), new BsonBinary(value.getValue()));
        }

        @Override
        public void accept(final SignedValue value) {
            this.writer.writeInt64(value.getName(), value.getValue());
        }

        @Override
        public void accept(final StringValue value) {
            this.writer.writeString(value.getName(), value.getValue());
        }

        @Override
        public void accept(final ListValue value) {
            this.writer.writeStartDocument(value.getName());
            this.writer.writeInt32("semantic", value.getSemantic().ordinal());
            this.writer.writeStartArray("values");
            for (int i = 0; i < value.getValue().size(); i++) {
                this.writer.writeStartDocument();
                for (int j = 0; j < value.getValue().get(i).size(); j++) {
                    value.getValue().get(i).get(j).visit(this);
                }
                this.writer.writeEndDocument();
            }
            this.writer.writeEndArray();
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final UnsignedValue value) {
            // TODO: Mark this as unsigned?
            this.writer.writeInt64(value.getName(), value.getValue().longValue());
        }

        @Override
        public void accept(final UndeclaredValue value) {
            this.writer.writeBinaryData(value.getName(), new BsonBinary(value.getValue()));
        }
    }
}
