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
package org.opennms.netmgt.flows.aggregation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.opennms.distributed.core.api.Identity;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

/**
 * Backend-agnostic {@link FlowRepository} that feeds the write-time {@link FlowAggregator} instead of
 * storing raw flows. It is meant to be registered as a <em>forwarder</em> (see the blueprint
 * {@code flows.repository.forwarder=true}) so the pipeline always delivers flows to it, alongside
 * whichever primary store is active — it never competes for the highest-ranked-store tier.
 *
 * <p>Concrete backends supply the storage {@link AggregatedFlowSink} via {@link #createSink(String)};
 * everything else — the aggregator lifecycle, windowing configuration, {@code writerId} resolution, and
 * staying inert (a no-op {@link #persist}) when aggregation is disabled or the backend is unconfigured —
 * lives here. On Horizon core this is the single aggregating writer; on Sentinel each instance sets its
 * own {@code writerId} and emits partial rows that readers sum.
 */
public abstract class AbstractAggregatingFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAggregatingFlowRepository.class);

    private final MetricRegistry metrics;

    private boolean enabled = false;
    private long windowSizeMs = 60_000L;
    private long allowedLatenessMs = 300_000L;
    private long flushIntervalMs = 5_000L;
    private long idleFlushMs = 0L; // 0 = auto (windowSize + lateness + 60s)
    private int topK = 10;
    private String writerId = ""; // blank -> auto-default to the node Identity (see resolveWriterId)

    private Identity identity;
    private FlowAggregator aggregator;

    protected AbstractAggregatingFlowRepository(final MetricRegistry metrics) {
        this.metrics = Objects.requireNonNull(metrics);
    }

    /**
     * Build the backend sink that persists closed-window rows, tagged with the given (already-resolved)
     * {@code writerId}, or return {@code null} to stay inert when the backend is not configured (e.g. no
     * DataSource). Called once from {@link #start()} when aggregation is enabled.
     */
    protected abstract Consumer<List<AggregatedFlow>> createSink(String writerId);

    public void start() {
        if (!enabled) {
            LOG.info("Write-time flow aggregation is disabled (set aggregation.enabled=true to enable it).");
            return;
        }
        final String effectiveWriterId = resolveWriterId();
        final Consumer<List<AggregatedFlow>> sink = createSink(effectiveWriterId);
        if (sink == null) {
            // The backend reported it is not configured; stay inert rather than failing to load. The
            // concrete repository is expected to have logged the backend-specific reason and remedy.
            LOG.error("Write-time flow aggregation not started: no aggregation sink is available.");
            return;
        }
        this.aggregator = new FlowAggregator(windowSizeMs, allowedLatenessMs, flushIntervalMs, topK, idleFlushMs, sink, metrics);
        this.aggregator.start();
        LOG.info("Write-time flow aggregation started (writerId={}, windowSizeMs={}, allowedLatenessMs={}, "
                + "flushIntervalMs={}, topK={}, idleFlushMs={}).", effectiveWriterId, windowSizeMs, allowedLatenessMs,
                flushIntervalMs, topK, idleFlushMs);
    }

    public void stop() {
        if (aggregator != null) {
            aggregator.close();
        }
        LOG.info("Write-time flow aggregation stopped.");
    }

    @Override
    public void persist(final Collection<? extends Flow> flows) throws FlowException {
        final FlowAggregator agg = this.aggregator;
        if (agg == null || flows == null || flows.isEmpty()) {
            // Disabled, inert (no sink), or nothing to do.
            return;
        }
        for (final Flow flow : flows) {
            agg.add(flow);
        }
    }

    /**
     * The writer id to tag this instance's rows with: the explicitly configured {@code writerId} if set,
     * otherwise the node's {@link Identity} id (so each Sentinel gets a distinct id automatically), falling
     * back to {@code "core"} when no Identity is available.
     */
    public String resolveWriterId() {
        if (writerId != null && !writerId.trim().isEmpty()) {
            return writerId;
        }
        if (identity != null && identity.getId() != null && !identity.getId().trim().isEmpty()) {
            return identity.getId();
        }
        return "core";
    }

    // --- config setters (blueprint) ---
    /** Node identity used to auto-default {@code writerId} when it is left blank. */
    public void setIdentity(final Identity identity) { this.identity = identity; }
    public void setEnabled(final boolean enabled) { this.enabled = enabled; }
    public void setWindowSizeMs(final long windowSizeMs) { this.windowSizeMs = windowSizeMs; }
    public void setAllowedLatenessMs(final long allowedLatenessMs) { this.allowedLatenessMs = allowedLatenessMs; }
    public void setFlushIntervalMs(final long flushIntervalMs) { this.flushIntervalMs = flushIntervalMs; }
    /** Wall-clock age after which an open window is flushed even if the watermark stalls; &lt;= 0 auto-derives. */
    public void setIdleFlushMs(final long idleFlushMs) { this.idleFlushMs = idleFlushMs; }
    /** Per (exporter, interface, dimension) cap for application/conversation/host; &lt;= 0 disables capping. */
    public void setTopK(final int topK) { this.topK = topK; }
    /** Identifies this writer's partial rows; give each Sentinel a distinct value so readers sum correctly. */
    public void setWriterId(final String writerId) { this.writerId = writerId; }
}