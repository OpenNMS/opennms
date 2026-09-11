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
package org.opennms.netmgt.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.api.CategoryAvailabilitySnapshotDao;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * Stores snapshots in the category_availability and
 * category_node_availability tables. A save deletes and reinserts the
 * category inside one transaction, so readers see either the old snapshot or
 * the new one, never a partial one.
 */
public class JdbcCategoryAvailabilitySnapshotDao implements CategoryAvailabilitySnapshotDao, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcCategoryAvailabilitySnapshotDao.class);

    private static final int BATCH_SIZE = 5000;

    private static final String SUMMARY_COLUMNS = "label, window_start, window_end, computed_at, node_count, service_count, services_down, downtime_ms";
    private static final String NODE_COLUMNS = "nodeid, service_count, services_down, downtime_ms";

    private DataSource m_dataSource;
    private PlatformTransactionManager m_transactionManager;
    private JdbcTemplate m_jdbc;
    private TransactionTemplate m_transaction;

    @Override
    public void afterPropertiesSet() {
        Assert.state(m_dataSource != null, "dataSource must be set");
        Assert.state(m_transactionManager != null, "transactionManager must be set");
        m_jdbc = new JdbcTemplate(m_dataSource);
        m_jdbc.setFetchSize(1000);
        m_transaction = new TransactionTemplate(m_transactionManager);
    }

    @Override
    public void save(final CategoryAvailability availability) {
        Objects.requireNonNull(availability, "availability");
        if (!availability.hasNodes()) {
            throw new IllegalArgumentException("Snapshot for '" + availability.getLabel() + "' must carry its node list");
        }
        final String label = availability.getLabel();
        final long began = System.nanoTime();
        m_transaction.executeWithoutResult(status -> {
            m_jdbc.update("DELETE FROM category_node_availability WHERE label = ?", label);
            m_jdbc.update("DELETE FROM category_availability WHERE label = ?", label);
            m_jdbc.update("INSERT INTO category_availability (" + SUMMARY_COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    label,
                    new Timestamp(availability.getWindowStart().getTime()),
                    new Timestamp(availability.getWindowEnd().getTime()),
                    new Timestamp(availability.getComputedAt().getTime()),
                    availability.getNodeCount(),
                    availability.getServiceCount(),
                    availability.getServicesDown(),
                    availability.getDowntimeMillis());

            final List<NodeAvailability> nodes = availability.getNodes();
            for (int from = 0; from < nodes.size(); from += BATCH_SIZE) {
                final List<NodeAvailability> chunk = nodes.subList(from, Math.min(nodes.size(), from + BATCH_SIZE));
                m_jdbc.batchUpdate("INSERT INTO category_node_availability (label, " + NODE_COLUMNS + ") VALUES (?, ?, ?, ?, ?)",
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                                final NodeAvailability node = chunk.get(i);
                                ps.setString(1, label);
                                ps.setInt(2, node.getNodeId());
                                ps.setLong(3, node.getServiceCount());
                                ps.setLong(4, node.getServicesDown());
                                ps.setLong(5, node.getDowntimeMillis());
                            }
                            @Override
                            public int getBatchSize() {
                                return chunk.size();
                            }
                        });
            }
        });
        LOG.debug("Saved availability snapshot for '{}' with {} nodes in {} ms", label, availability.getNodeCount(), (System.nanoTime() - began) / 1_000_000L);
    }

    @Override
    public Optional<CategoryAvailability> findSummary(final String label) {
        final List<CategoryAvailability> rows = m_jdbc.query("SELECT " + SUMMARY_COLUMNS + " FROM category_availability WHERE label = ?", summaryMapper(), label);
        return rows.stream().findFirst();
    }

    @Override
    public List<CategoryAvailability> findAllSummaries() {
        return m_jdbc.query("SELECT " + SUMMARY_COLUMNS + " FROM category_availability ORDER BY label", summaryMapper());
    }

    @Override
    public Optional<CategoryAvailability> findWithNodes(final String label) {
        return findSummary(label).map(summary -> new CategoryAvailability(
                summary.getLabel(), summary.getWindowStart(), summary.getWindowEnd(), summary.getComputedAt(),
                findNodes(label, 0, 0)));
    }

    @Override
    public List<NodeAvailability> findNodes(final String label, final int offset, final int limit) {
        final long windowMillis = windowMillis(label);
        final StringBuilder sql = new StringBuilder("SELECT " + NODE_COLUMNS + " FROM category_node_availability WHERE label = ? ORDER BY nodeid");
        final List<Object> params = new ArrayList<>();
        params.add(label);
        if (limit > 0) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }
        if (offset > 0) {
            sql.append(" OFFSET ?");
            params.add(offset);
        }
        return m_jdbc.query(sql.toString(), params.toArray(), nodeMapper(windowMillis));
    }

    @Override
    public Optional<NodeAvailability> findNode(final String label, final int nodeId) {
        final long windowMillis = windowMillis(label);
        return m_jdbc.query("SELECT " + NODE_COLUMNS + " FROM category_node_availability WHERE label = ? AND nodeid = ?", nodeMapper(windowMillis), label, nodeId)
                .stream().findFirst();
    }

    @Override
    public List<Integer> findNodeIds(final String label) {
        return m_jdbc.queryForList("SELECT nodeid FROM category_node_availability WHERE label = ? ORDER BY nodeid", Integer.class, label);
    }

    @Override
    public void deleteByLabel(final String label) {
        m_transaction.executeWithoutResult(status -> {
            m_jdbc.update("DELETE FROM category_node_availability WHERE label = ?", label);
            m_jdbc.update("DELETE FROM category_availability WHERE label = ?", label);
        });
    }

    @Override
    public void retainOnly(final Collection<String> labels) {
        final List<String> stale = m_jdbc.queryForList("SELECT label FROM category_availability", String.class);
        stale.removeAll(labels);
        for (final String label : stale) {
            LOG.info("Removing availability snapshot for category '{}', which is no longer defined", label);
            deleteByLabel(label);
        }
    }

    /** Window length of a stored snapshot; zero when the category has no snapshot. */
    private long windowMillis(final String label) {
        final List<Long> rows = m_jdbc.query("SELECT window_start, window_end FROM category_availability WHERE label = ?",
                (rs, n) -> rs.getTimestamp("window_end").getTime() - rs.getTimestamp("window_start").getTime(), label);
        return rows.isEmpty() ? 0L : rows.get(0);
    }

    private static RowMapper<CategoryAvailability> summaryMapper() {
        return (rs, n) -> CategoryAvailability.withTotals(
                rs.getString("label"),
                date(rs, "window_start"),
                date(rs, "window_end"),
                date(rs, "computed_at"),
                rs.getLong("node_count"),
                rs.getLong("service_count"),
                rs.getLong("services_down"),
                rs.getLong("downtime_ms"));
    }

    private static RowMapper<NodeAvailability> nodeMapper(final long windowMillis) {
        return (rs, n) -> new NodeAvailability(rs.getInt("nodeid"), rs.getLong("service_count"), rs.getLong("services_down"), rs.getLong("downtime_ms"), windowMillis);
    }

    private static Date date(final ResultSet rs, final String column) throws SQLException {
        final Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : new Date(ts.getTime());
    }

    public void setDataSource(final DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        m_transactionManager = transactionManager;
    }
}
