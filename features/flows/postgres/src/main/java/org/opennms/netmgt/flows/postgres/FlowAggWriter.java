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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.opennms.netmgt.flows.aggregation.AggregatedFlow;
import org.opennms.netmgt.flows.aggregation.AggregatedFlowSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The {@link org.opennms.netmgt.flows.aggregation.FlowAggregator} sink that persists closed-window
 * {@link AggregatedFlow} rows to the {@code flow_agg} table. Each row carries this writer's
 * {@code writerId}; when several writers feed the same window+key (e.g. multiple Sentinels), each writes
 * its own partial row and readers SUM them. A single Horizon-core writer therefore produces
 * already-final rows.
 */
public class FlowAggWriter implements AggregatedFlowSink {

    private static final Logger LOG = LoggerFactory.getLogger(FlowAggWriter.class);

    private static final String INSERT_SQL =
            "INSERT INTO flow_agg (window_start, window_end, exporter_node_id, if_index, dscp, dimension, " +
            "grouped_by_key, bytes_in, bytes_out, congestion_encountered, non_ecn_capable_transport, " +
            "hostname, writer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final String writerId;

    public FlowAggWriter(final DataSource dataSource, final String writerId) {
        this.jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(dataSource));
        this.writerId = Objects.requireNonNull(writerId);
    }

    @Override
    public void accept(final List<AggregatedFlow> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        try {
            jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                    final AggregatedFlow r = rows.get(i);
                    int c = 1;
                    // timestamptz columns bound as UTC OffsetDateTime so the stored instant is unambiguous
                    ps.setObject(c++, Instant.ofEpochMilli(r.windowStartMs).atOffset(ZoneOffset.UTC));
                    ps.setObject(c++, Instant.ofEpochMilli(r.windowEndMs).atOffset(ZoneOffset.UTC));
                    ps.setInt(c++, r.exporterNodeId);
                    ps.setInt(c++, r.ifIndex);
                    if (r.dscp != null) {
                        ps.setInt(c++, r.dscp);
                    } else {
                        ps.setNull(c++, java.sql.Types.SMALLINT);
                    }
                    ps.setString(c++, r.dimension.name());
                    ps.setString(c++, r.groupedByKey);
                    ps.setLong(c++, r.bytesIn);
                    ps.setLong(c++, r.bytesOut);
                    ps.setBoolean(c++, r.congestionEncountered);
                    ps.setBoolean(c++, r.nonEcnCapableTransport);
                    ps.setString(c++, r.hostname);
                    ps.setString(c, writerId);
                }

                @Override
                public int getBatchSize() {
                    return rows.size();
                }
            });
        } catch (final Exception e) {
            LOG.warn("Failed to persist a batch of {} aggregated flow rows; the batch is dropped.", rows.size(), e);
        }
    }
}
