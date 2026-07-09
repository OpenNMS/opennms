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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Prototype {@link FlowRepository} that persists enriched flows to PostgreSQL (hot columns + jsonb).
 *
 * <p>Targets the internal "opennms" datasource by default; set {@code dataSourceName} to a named
 * datasource (defined in {@code opennms-datasources.xml}) to persist to an external PostgreSQL for
 * scale. Writes are batched with a drop-newest backpressure policy (see {@link BatchingFlowWriter}).
 */
public class PostgresFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresFlowRepository.class);

    private static final String CHANGELOG = "org/opennms/netmgt/flows/postgres/changelog.xml";

    private static final String INSERT_SQL =
            "INSERT INTO flow (flow_ts, delta_switched, last_switched, first_switched, bytes, packets, " +
            "sampling_interval, direction, application, convo_key, src_addr, dst_addr, protocol, dscp, " +
            "exporter_node_id, input_snmp, output_snmp, location, document) VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::inet, ?::inet, ?, ?, ?, ?, ?, ?, ?::jsonb)";

    private final MetricRegistry metrics;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FlowRowMapper rowMapper = new FlowRowMapper(objectMapper);

    private int batchSize = 1000;
    private long flushIntervalMs = 500;
    private int queueCapacity = 100_000;
    private boolean runSchemaChangelog = true;

    private FlowDataSourceProvider dataSourceProvider;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private BatchingFlowWriter<FlowRow> writer;

    public PostgresFlowRepository(final MetricRegistry metrics) {
        this.metrics = Objects.requireNonNull(metrics);
    }

    public void start() throws Exception {
        if (this.dataSource == null && this.dataSourceProvider != null) {
            this.dataSource = this.dataSourceProvider.getDataSource();
        }
        if (this.dataSource == null) {
            // No flow DataSource was configured (see FlowDataSourceProvider). Stay inert rather than
            // failing the blueprint container: the feature loads, persist() becomes a no-op.
            LOG.error("PostgresFlowRepository not started: no flow DataSource is configured. Flows will NOT be "
                    + "persisted to PostgreSQL until datasource.url is set on the "
                    + "org.opennms.features.flows.persistence.postgres pid.");
            return;
        }
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        if (runSchemaChangelog) {
            installSchema();
        }
        this.writer = new BatchingFlowWriter<>("postgresFlowRepository", queueCapacity, batchSize,
                flushIntervalMs, this::flush, metrics);
        this.writer.start();
        LOG.info("PostgresFlowRepository started (batchSize={}, flushIntervalMs={}, queueCapacity={}).",
                batchSize, flushIntervalMs, queueCapacity);
    }

    public void stop() {
        if (writer != null) {
            writer.close();
        }
        LOG.info("PostgresFlowRepository stopped.");
    }

    @Override
    public void persist(final Collection<? extends Flow> flows) throws FlowException {
        final BatchingFlowWriter<FlowRow> w = this.writer;
        if (w == null || flows == null || flows.isEmpty()) {
            // Inert (no DataSource configured) or nothing to do.
            return;
        }
        for (final Flow flow : flows) {
            w.add(rowMapper.toRow(flow));
        }
    }

    private void flush(final List<FlowRow> rows) {
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                final FlowRow r = rows.get(i);
                int c = 1;
                ps.setTimestamp(c++, r.flowTs);
                setLong(ps, c++, r.deltaSwitched);
                setLong(ps, c++, r.lastSwitched);
                setLong(ps, c++, r.firstSwitched);
                setLong(ps, c++, r.bytes);
                setLong(ps, c++, r.packets);
                if (r.samplingInterval != null) ps.setDouble(c++, r.samplingInterval); else ps.setNull(c++, Types.DOUBLE);
                ps.setString(c++, r.direction);
                ps.setString(c++, r.application);
                ps.setString(c++, r.convoKey);
                ps.setString(c++, r.srcAddr);
                ps.setString(c++, r.dstAddr);
                setInt(ps, c++, r.protocol);
                setInt(ps, c++, r.dscp);
                setInt(ps, c++, r.exporterNodeId);
                setInt(ps, c++, r.inputSnmp);
                setInt(ps, c++, r.outputSnmp);
                ps.setString(c++, r.location);
                ps.setString(c, r.documentJson);
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    private void installSchema() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection()) {
            final liquibase.database.Database db = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));
            final Liquibase liquibase = new Liquibase(CHANGELOG,
                    new ClassLoaderResourceAccessor(getClass().getClassLoader()), db);
            liquibase.update("");
        }
        LOG.info("PostgresFlowRepository schema ensured.");
    }

    private static void setLong(final PreparedStatement ps, final int idx, final Long v) throws SQLException {
        if (v != null) ps.setLong(idx, v); else ps.setNull(idx, Types.BIGINT);
    }

    private static void setInt(final PreparedStatement ps, final int idx, final Integer v) throws SQLException {
        if (v != null) ps.setInt(idx, v); else ps.setNull(idx, Types.INTEGER);
    }

    // --- config setters (blueprint) ---
    /** Blueprint wiring: supplies the flow DataSource in start() (may be inert, i.e. return null). */
    public void setDataSourceProvider(final FlowDataSourceProvider dataSourceProvider) { this.dataSourceProvider = dataSourceProvider; }
    /** Test/embedding hook: use this DataSource directly instead of resolving one from the provider. */
    public void setDataSource(final DataSource dataSource) { this.dataSource = dataSource; }
    public void setBatchSize(final int batchSize) { this.batchSize = batchSize; }
    public void setFlushIntervalMs(final long flushIntervalMs) { this.flushIntervalMs = flushIntervalMs; }
    public void setQueueCapacity(final int queueCapacity) { this.queueCapacity = queueCapacity; }
    public void setRunSchemaChangelog(final boolean runSchemaChangelog) { this.runSchemaChangelog = runSchemaChangelog; }
}