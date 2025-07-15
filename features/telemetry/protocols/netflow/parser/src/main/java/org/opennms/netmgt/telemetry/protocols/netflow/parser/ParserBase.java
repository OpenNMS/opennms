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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.RateLimiter;
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
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.TransportValueVisitor;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.swrve.ratelimitedlogger.RateLimitedLog;

public abstract class ParserBase implements Parser {
    private static final Logger LOG = LoggerFactory.getLogger(ParserBase.class);

    private final RateLimitedLog SEQUENCE_ERRORS_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private static final long DEFAULT_CLOCK_SKEW_EVENT_RATE_SECONDS = TimeUnit.HOURS.toSeconds(1);

    private static final long DEFAULT_ILLEGAL_FLOW_EVENT_RATE_SECONDS = TimeUnit.HOURS.toSeconds(1);

    public static final String CLOCK_SKEW_EVENT_UEI = "uei.opennms.org/internal/telemetry/clockSkewDetected";

    public static final String ILLEGAL_FLOW_EVENT_UEI = "uei.opennms.org/internal/telemetry/illegalFlowDetected";

    private final ThreadLocal<Boolean> isParserThread = new ThreadLocal<>();

    private final Protocol protocol;

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    private final EventForwarder eventForwarder;

    private final Identity identity;

    private final DnsResolver dnsResolver;

    private final Meter recordsReceived;

    private final Meter recordsScheduled;

    private final Meter recordsDispatched;

    private final Meter recordsCompleted;

    private final Counter recordEnrichmentErrors;

    private final Counter recordDispatchErrors;

    private final Meter invalidFlows;

    private final Timer recordEnrichmentTimer;

    private final Counter sequenceErrors;

    private int threads = DEFAULT_NUM_THREADS;

    private long maxClockSkew = 0;

    private long clockSkewEventRate = 0;

    private long illegalFlowEventRate = 0;

    private int sequenceNumberPatience = 32;

    private boolean dnsLookupsEnabled = true;

    private LoadingCache<InetAddress, RateLimiter> clockSkewEventLimiters;

    private LoadingCache<InetAddress, RateLimiter> illegalFlowEventLimiters;

    private ExecutorService executor;

    private boolean includeRawMessage = false;

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

        recordsReceived = metricRegistry.meter(MetricRegistry.name("parsers", name, "recordsReceived"));
        recordsDispatched = metricRegistry.meter(MetricRegistry.name("parsers", name, "recordsDispatched"));
        recordEnrichmentTimer = metricRegistry.timer(MetricRegistry.name("parsers", name, "recordEnrichment"));
        recordEnrichmentErrors = metricRegistry.counter(MetricRegistry.name("parsers", name, "recordEnrichmentErrors"));
        invalidFlows = metricRegistry.meter(MetricRegistry.name("parsers", name, "invalidFlows"));
        recordsScheduled = metricRegistry.meter(MetricRegistry.name("parsers", name, "recordsScheduled"));
        recordsCompleted = metricRegistry.meter(MetricRegistry.name("parsers", name, "recordsCompleted"));
        recordDispatchErrors = metricRegistry.counter(MetricRegistry.name("parsers", name, "recordDispatchErrors"));
        sequenceErrors = metricRegistry.counter(MetricRegistry.name("parsers", name, "sequenceErrors"));

        // Call setters since these also perform additional handling
        setClockSkewEventRate(DEFAULT_CLOCK_SKEW_EVENT_RATE_SECONDS);
        setIllegalFlowEventRate(DEFAULT_ILLEGAL_FLOW_EVENT_RATE_SECONDS);
        setThreads(DEFAULT_NUM_THREADS);
    }

    protected abstract MessageBuilder getMessageBuilder();

    @Override
    public void start(ScheduledExecutorService executorService) {
        executor = new ThreadPoolExecutor(
                // corePoolSize must be > 0 since we use the RejectedExecutionHandler to block when the queue is full
                1, threads,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new LogPreservingThreadFactory("Telemetryd-" + protocol + "-" + name, Integer.MAX_VALUE),
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

    @Override
    public String getDescription() {
        return this.protocol.description;
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

        this.clockSkewEventLimiters = CacheBuilder.newBuilder()
                .expireAfterWrite(this.clockSkewEventRate, TimeUnit.SECONDS)
                .build(CacheLoader.from(() -> RateLimiter.create(1.0 / this.clockSkewEventRate)));
    }

    public void setIllegalFlowEventRate(final long illegalFlowEventRate) {
        this.illegalFlowEventRate = illegalFlowEventRate;

        this.illegalFlowEventLimiters = CacheBuilder.newBuilder()
                .expireAfterWrite(this.illegalFlowEventRate, TimeUnit.SECONDS)
                .build(CacheLoader.from(() -> RateLimiter.create(1.0 / this.clockSkewEventRate)));
    }

    public long getIllegalFlowEventRate() {
        return illegalFlowEventRate;
    }

    public int getSequenceNumberPatience() {
        return this.sequenceNumberPatience;
    }

    public void setSequenceNumberPatience(final int sequenceNumberPatience) {
        this.sequenceNumberPatience = sequenceNumberPatience;
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

    protected CompletableFuture<?> transmit(final RecordProvider packet, final Session session, final InetSocketAddress remoteAddress) {
        // Verify that flows sequences are in order
        if (!session.verifySequenceNumber(packet.getObservationDomainId(), packet.getSequenceNumber())) {
            SEQUENCE_ERRORS_LOGGER.warn("Error in flow sequence detected: from {}", session.getRemoteAddress());
            this.sequenceErrors.inc();
        }

        final RecordEnricher recordEnricher = new RecordEnricher(dnsResolver, getDnsLookupsEnabled());

        // The packets are coming in hot - performance here is critical
        //   LOG.trace("Got packet: {}", packet);
        // Perform the record enrichment and serialization in a thread pool allowing these to be parallelized
        final var futures = packet.getRecords().map(record -> {
            this.recordsReceived.mark();

            final Timer.Context timerContext = recordEnrichmentTimer.time();

            // Trigger record enrichment (performing DNS reverse lookups for example)
            return recordEnricher.enrich(record)
                    .whenComplete((enrichment, ex) -> {
                        timerContext.close();

                        if (ex != null) {
                            this.recordEnrichmentErrors.inc();
                        }
                    })
                    .thenApplyAsync(enrichment -> {
                        this.recordsScheduled.mark();

                        // Let's serialize
                        final FlowMessage.Builder flowMessage;
                        try {
                            flowMessage = this.getMessageBuilder().buildMessage(record, enrichment);
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }

                        if (includeRawMessage) {
                            for (final Value<?> value : record) {
                                final TransportValueVisitor transportValueVisitor = new TransportValueVisitor();
                                value.visit(transportValueVisitor);
                                flowMessage.addRawMessage(transportValueVisitor.build());
                            }
                        }

                        // Check if the flow is valid (and maybe correct it)
                        final List<String> corrections = this.correctFlow(flowMessage);
                        if (!corrections.isEmpty()) {
                            this.invalidFlows.mark();

                            if (illegalFlowEventLimiters.getUnchecked(session.getRemoteAddress()).tryAcquire()) {
                                eventForwarder.sendNow(new EventBuilder()
                                        .setUei(ILLEGAL_FLOW_EVENT_UEI)
                                        .setTime(new Date())
                                        .setSource(getName())
                                        .setInterface(session.getRemoteAddress())
                                        .setDistPoller(identity.getId())
                                        .addParam("monitoringSystemId", identity.getId())
                                        .addParam("monitoringSystemLocation", identity.getLocation())
                                        .setParam("cause", Joiner.on('\n').join(corrections))
                                        .setParam("protocol", protocol.name())
                                        .setParam("illegalFlowEventRate", (int) getIllegalFlowEventRate())
                                        .getEvent());

                                for (final String correction : corrections) {
                                    LOG.warn("Illegal flow detected from exporter {}: \n{}", session.getRemoteAddress().getAddress(), correction);
                                }
                            }
                        }

                        // Build the message to dispatch
                        return new TelemetryMessage(remoteAddress, ByteBuffer.wrap(flowMessage.build().toByteArray()));

                    }, executor)
                    .thenCompose(msg -> {
                        // Dispatch
                        recordsDispatched.mark();
                        return dispatcher.send(msg).whenComplete((b, exx) -> {
                            if (exx != null) {
                                this.recordDispatchErrors.inc();
                            } else {
                                this.recordsCompleted.mark();
                            }
                        });
                    });
        }).toArray(CompletableFuture[]::new);

        // Return a future which is completed when all records are finished dispatching (i.e. written to Kafka)
        return CompletableFuture.allOf(futures).whenComplete((any, exx) -> {
            if (exx != null) {
                LOG.warn("One or more of the records were not successfully dispatched.", exx);
            }
        });
    }

    protected void detectClockSkew(final long packetTimestampMs, final InetAddress remoteAddress) {
        if (getMaxClockSkew() > 0) {
            long deltaMs = Math.abs(packetTimestampMs - System.currentTimeMillis());
            if (deltaMs > getMaxClockSkew() * 1000L) {
                if (clockSkewEventLimiters.getUnchecked(remoteAddress).tryAcquire()) {
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

    private List<String> correctFlow(final FlowMessage.Builder flow) {
        final List<String> corrections = Lists.newArrayList();

        if (flow.getFirstSwitched().getValue() > flow.getLastSwitched().getValue()) {
            corrections.add(String.format("Malformed flow: lastSwitched must be greater than firstSwitched: srcAddress=%s, dstAddress=%s, firstSwitched=%d, lastSwitched=%d, duration=%d",
                    flow.getSrcAddress(),
                    flow.getDstAddress(),
                    flow.getFirstSwitched().getValue(),
                    flow.getLastSwitched().getValue(),
                    flow.getLastSwitched().getValue() - flow.getFirstSwitched().getValue()));

            // Re-calculate a (somewhat) valid timout from the flow timestamps
            final long timeout = (flow.hasDeltaSwitched() && flow.getDeltaSwitched().getValue() != flow.getFirstSwitched().getValue())
                    ? (flow.getLastSwitched().getValue() - flow.getDeltaSwitched().getValue())
                    : 0L;

            flow.getLastSwitchedBuilder().setValue(flow.getTimestamp());
            flow.getFirstSwitchedBuilder().setValue(flow.getTimestamp() - timeout);
            flow.getDeltaSwitchedBuilder().setValue(flow.getTimestamp() - timeout);
        }

        return corrections;
    }

    protected SequenceNumberTracker sequenceNumberTracker() {
        return new SequenceNumberTracker(this.sequenceNumberPatience);
    }

    public boolean isIncludeRawMessage() {
        return includeRawMessage;
    }

    public void setIncludeRawMessage(boolean includeRawMessage) {
        this.includeRawMessage = includeRawMessage;
    }
}
