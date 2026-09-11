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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.api.CategoryAvailabilityCalculator;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * Set-based implementation of {@link CategoryAvailabilityCalculator}.
 *
 * The whole computation is one SQL statement. The filter rule is rendered by
 * the {@link FilterDao} as a subquery yielding node IDs, so the member node set
 * never leaves the database. Outage durations are clamped to the window and
 * summed per node, and service counts are grouped per node, in the same
 * statement. The result is one row per member node regardless of how many
 * services or outages the node has, so the amount of work grows with the
 * number of nodes and outages in the window, not with round trips.
 */
public class JdbcCategoryAvailabilityCalculator implements CategoryAvailabilityCalculator, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcCategoryAvailabilityCalculator.class);

    /** Service names starting with this prefix are regular expressions. */
    private static final String REGEX_PREFIX = "~";

    private DataSource m_dataSource;
    private FilterDao m_filterDao;
    private JdbcTemplate m_jdbcTemplate;

    @Override
    public void afterPropertiesSet() {
        Assert.state(m_dataSource != null, "dataSource must be set");
        Assert.state(m_filterDao != null, "filterDao must be set");
        m_jdbcTemplate = new JdbcTemplate(m_dataSource);
    }

    @Override
    public CategoryAvailability calculate(final String label, final String filterRule, final Collection<String> serviceNames, final Date windowStart, final Date windowEnd) {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(windowStart, "windowStart");
        Objects.requireNonNull(windowEnd, "windowEnd");
        if (filterRule == null || filterRule.trim().isEmpty()) {
            throw new IllegalArgumentException("filterRule must not be blank");
        }
        if (!windowStart.before(windowEnd)) {
            throw new IllegalArgumentException("windowStart must be before windowEnd");
        }

        final String nodeIdSelect;
        try {
            nodeIdSelect = m_filterDao.getNodeIdSelectStatement(filterRule);
        } catch (final FilterParseException e) {
            throw new IllegalArgumentException("Cannot parse filter rule for category '" + label + "': " + filterRule, e);
        }

        final List<Object> params = new ArrayList<>();
        final String servicePredicate = buildServicePredicate(serviceNames, params);

        final Timestamp start = new Timestamp(windowStart.getTime());
        final Timestamp end = new Timestamp(windowEnd.getTime());
        // Parameter order must match the placeholders in buildSql().
        params.add(end);   // COALESCE(ifregainedservice, end)
        params.add(end);   // LEAST(..., end)
        params.add(start); // GREATEST(iflostservice, start)
        params.add(end);   // iflostservice <= end
        params.add(start); // ifregainedservice > start

        final String sql = buildSql(nodeIdSelect, servicePredicate);
        LOG.debug("Calculating availability for category '{}' with rule '{}' over {} .. {}", label, filterRule, windowStart, windowEnd);
        LOG.trace("Availability SQL for category '{}': {}", label, sql);

        final long windowMillis = windowEnd.getTime() - windowStart.getTime();
        final long began = System.nanoTime();
        final List<NodeAvailability> nodes = m_jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) ->
            new NodeAvailability(rs.getInt("nodeid"), rs.getLong("service_count"), rs.getLong("services_down"), rs.getLong("downtime_ms"), windowMillis));
        LOG.debug("Calculated availability for category '{}': {} nodes in {} ms", label, nodes.size(), (System.nanoTime() - began) / 1_000_000L);

        return new CategoryAvailability(label, windowStart, windowEnd, new Date(), nodes);
    }

    /**
     * Build the service name predicate (without a leading AND) and append its
     * bind values to params. Returns null when every service is covered.
     */
    static String buildServicePredicate(final Collection<String> serviceNames, final List<Object> params) {
        if (serviceNames == null) {
            return null;
        }
        final List<String> literals = new ArrayList<>();
        final List<String> patterns = new ArrayList<>();
        for (final String name : serviceNames) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            final String trimmed = name.trim();
            if (trimmed.startsWith(REGEX_PREFIX)) {
                patterns.add(trimmed.substring(REGEX_PREFIX.length()));
            } else {
                literals.add(trimmed);
            }
        }
        if (literals.isEmpty() && patterns.isEmpty()) {
            return null;
        }

        final List<String> clauses = new ArrayList<>();
        if (!literals.isEmpty()) {
            final StringBuilder in = new StringBuilder("s.servicename IN (");
            for (int i = 0; i < literals.size(); i++) {
                in.append(i == 0 ? "?" : ", ?");
                params.add(literals.get(i));
            }
            in.append(")");
            clauses.add(in.toString());
        }
        for (final String pattern : patterns) {
            clauses.add("s.servicename ~ ?");
            params.add(pattern);
        }
        return "(" + String.join(" OR ", clauses) + ")";
    }

    static String buildSql(final String nodeIdSelect, final String servicePredicate) {
        return "WITH member_nodes AS (\n"
             + "    SELECT DISTINCT f.nodeid FROM (" + nodeIdSelect + ") AS f(nodeid)\n"
             + "), member_services AS (\n"
             + "    SELECT ifsvc.id AS ifserviceid, ipif.nodeid\n"
             + "      FROM ifservices ifsvc\n"
             + "      JOIN ipinterface ipif ON ipif.id = ifsvc.ipinterfaceid\n"
             + "      JOIN service s ON s.serviceid = ifsvc.serviceid\n"
             + "     WHERE ipif.ismanaged = 'M'\n"
             + "       AND ipif.nodeid IN (SELECT nodeid FROM member_nodes)\n"
             + (servicePredicate == null ? "" : "       AND " + servicePredicate + "\n")
             + "), service_counts AS (\n"
             + "    SELECT nodeid, COUNT(*) AS service_count\n"
             + "      FROM member_services\n"
             + "     GROUP BY nodeid\n"
             + "), outage_sums AS (\n"
             + "    SELECT ms.nodeid,\n"
             + "           ROUND(SUM(EXTRACT(EPOCH FROM (LEAST(COALESCE(o.ifregainedservice, ?), ?) - GREATEST(o.iflostservice, ?)))) * 1000)::bigint AS downtime_ms,\n"
             + "           COUNT(*) FILTER (WHERE o.ifregainedservice IS NULL) AS services_down\n"
             + "      FROM outages o\n"
             + "      JOIN member_services ms ON ms.ifserviceid = o.ifserviceid\n"
             + "     WHERE o.perspective IS NULL\n"
             + "       AND o.iflostservice <= ?\n"
             + "       AND (o.ifregainedservice IS NULL OR o.ifregainedservice > ?)\n"
             + "     GROUP BY ms.nodeid\n"
             + ")\n"
             + "SELECT mn.nodeid,\n"
             + "       COALESCE(sc.service_count, 0) AS service_count,\n"
             + "       COALESCE(os.downtime_ms, 0) AS downtime_ms,\n"
             + "       COALESCE(os.services_down, 0) AS services_down\n"
             + "  FROM member_nodes mn\n"
             + "  LEFT JOIN service_counts sc ON sc.nodeid = mn.nodeid\n"
             + "  LEFT JOIN outage_sums os ON os.nodeid = mn.nodeid\n"
             + " ORDER BY mn.nodeid";
    }

    public void setDataSource(final DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public void setFilterDao(final FilterDao filterDao) {
        m_filterDao = filterDao;
    }
}
