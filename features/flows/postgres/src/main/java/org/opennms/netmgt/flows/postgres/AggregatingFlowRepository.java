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
package org.opennms.netmgt.flows.postgres;

import java.util.Collection;
import java.util.Objects;

import javax.sql.DataSource;

import org.opennms.distributed.core.api.Identity;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

/**
 * A {@link FlowRepository} that feeds the write-time {@link FlowAggregator} instead of storing raw flows.
 * Registered as a <em>forwarder</em> (see the blueprint {@code flows.repository.forwarder=true}) so the
 * pipeline always delivers flows to it, alongside whichever primary store is active — it never competes
 * with {@link PostgresFlowRepository} for the highest-ranked-store tier.
 *
 * <p>Lifecycle mirrors {@link PostgresFlowRepository}: the DataSource is resolved from the shared
 * {@link FlowDataSourceProvider} in {@link #start()}, and the repository stays inert (a no-op
 * {@link #persist}) rather than failing to load when aggregation is disabled or no DataSource is
 * configured. On Horizon core this is the single aggregating writer; on Sentinel each instance sets its
 * own {@code writerId} and emits partial rows that readers sum.
 */
public class AggregatingFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatingFlowRepository.class);

    private final MetricRegistry metrics;

    private boolean enabled = false;
    private long windowSizeMs = 60_000L;
    private long allowedLatenessMs = 300_000L;
    private long flushIntervalMs = 5_000L;
    private long idleFlushMs = 0L; // 0 = auto (windowSize + lateness + 60s)
    private int topK = 10;
    private String writerId = ""; // blank -> auto-default to the node Identity (see resolveWriterId)

    private Identity identity;
    private FlowDataSourceProvider dataSourceProvider;
    private DataSource dataSource;
    private FlowAggregator aggregator;

    public AggregatingFlowRepository(final MetricRegistry metrics) {
        this.metrics = Objects.requireNonNull(metrics);
    }

    public void start() {
        if (!enabled) {
            LOG.info("Write-time flow aggregation is disabled (set aggregation.enabled=true on the "
                    + "org.opennms.features.flows.persistence.postgres pid to enable it).");
            return;
        }
        if (this.dataSource == null && this.dataSourceProvider != null) {
            this.dataSource = this.dataSourceProvider.getDataSource();
        }
        if (this.dataSource == null) {
            LOG.error("Write-time flow aggregation not started: no flow DataSource is configured. Flows will "
                    + "NOT be aggregated until datasource.url is set on the "
                    + "org.opennms.features.flows.persistence.postgres pid.");
            return;
        }
        final String effectiveWriterId = resolveWriterId();
        final FlowAggWriter writer = new FlowAggWriter(this.dataSource, effectiveWriterId);
        this.aggregator = new FlowAggregator(windowSizeMs, allowedLatenessMs, flushIntervalMs, topK, idleFlushMs, writer, metrics);
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
            // Disabled, inert (no DataSource), or nothing to do.
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
    String resolveWriterId() {
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
    /** Blueprint wiring: supplies the flow DataSource in start() (may be inert, i.e. return null). */
    public void setDataSourceProvider(final FlowDataSourceProvider dataSourceProvider) { this.dataSourceProvider = dataSourceProvider; }
    /** Test/embedding hook: use this DataSource directly instead of resolving one from the provider. */
    public void setDataSource(final DataSource dataSource) { this.dataSource = dataSource; }
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
