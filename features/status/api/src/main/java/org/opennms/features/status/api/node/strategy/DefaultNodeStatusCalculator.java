/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.status.api.node.strategy;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.features.status.api.node.NodeStatusCalculator;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.utils.QueryParameters;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;

public class DefaultNodeStatusCalculator implements NodeStatusCalculator {

    @Autowired
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Override
    public Status calculateStatus(NodeStatusCalculatorConfig query) {
        final Map<String, Object> parameterMap = new HashMap<>();

        // Build query
        final StringBuilder sql = new StringBuilder();
        sql.append(String.format("SELECT node_status.nodeid, %s as severity, alarm_count, alarm_count_unack ", getSeverityColumn(query)));
        sql.append("FROM node_status ");
        sql.append("JOIN node on node_status.nodeid = node.nodeid ") ;

        applyRestrictions(sql, query, parameterMap);

        // Apply ordering
        if (query.getOrder() != null && !Strings.isNullOrEmpty(query.getOrder().getColumn())) {
            final QueryParameters.Order order = query.getOrder();
            sql.append(String.format("ORDER BY %s %s ",  order.getColumn(), order.isDesc() ? "desc" : "asc"));
        }

        applyLimitAndOffset(sql, query);

        // execute query
        final List<Object[]> rows = genericPersistenceAccessor.executeNativeQuery(sql.toString(), parameterMap);
        final Status status = new Status();
        for(Object[] row : rows) {
            status.add(
                    (int) row[0], // nodeId
                    OnmsSeverity.get((int) row[1]), // node status
                    row[2] != null ? ((BigInteger) row[2]).longValue() : 0, // alarm count
                    row[3] != null ? ((BigInteger) row[3]).longValue() : 0 // unacknowledged count
            );
        }
        return status;
    }

    public int countStatus(NodeStatusCalculatorConfig query) {
        final Map<String, Object> parameterMap = new HashMap<>();
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(*) ");
        sql.append("FROM node_status ");
        sql.append("JOIN node on node_status.nodeid = node.nodeid ") ;

        applyRestrictions(sql, query, parameterMap);

        final List<Object> rows = genericPersistenceAccessor.executeNativeQuery(sql.toString(), parameterMap);
        if (!rows.isEmpty()) {
            if (rows.get(0) != null) {
                return ((BigInteger) rows.get(0)).intValue();
            }
        }
        return 0;
    }

    public Map<OnmsSeverity, Long> calculateStatusOverview(NodeStatusCalculatorConfig query) {
        final Map<String, Object> parameterMap = new HashMap<>();
        final StringBuilder sql = new StringBuilder();
        sql.append(String.format("SELECT count(*), %s as severity ", getSeverityColumn(query)));
        sql.append("FROM node_status ");
        sql.append("JOIN node on node_status.nodeid = node.nodeid ") ;

        applyRestrictions(sql, query, parameterMap);

        sql.append("GROUP BY severity ");

        applyLimitAndOffset(sql, query);

        final List<Object[]> rows = genericPersistenceAccessor.executeNativeQuery(sql.toString(), parameterMap);
        final Map<OnmsSeverity, Long> statusMap = new HashMap<>();
        for(Object[] columns : rows) {
            statusMap.put(
                    OnmsSeverity.get(((Integer) columns[1]).intValue()),
                    ((BigInteger) columns[0]).longValue()
            );
        }
        return statusMap;
    }

    private static void applyLimitAndOffset(final StringBuilder sql, final NodeStatusCalculatorConfig query) {
        if (query.getLimit() != null) {
            sql.append(String.format(" LIMIT %d", query.getLimit()));
        }
        if (query.getOffset() != null) {
            sql.append(String.format(" OFFSET %d", query.getOffset()));
        }
    }

    private static String getSeverityColumn(NodeStatusCalculatorConfig query) {
        switch(query.getCalculationStrategy()) {
            case Alarms:
                if (query.isIncludeAcknowledgedAlarms()) {
                    return "max_alarm_severity";
                }
                return "max_alarm_severity_unack";
            case Outages:
                return "max_outage_severity";
            default:
                throw new IllegalStateException("CalculationStrategy not valid/found.");
        }
    }

    private static void applyRestrictions(StringBuilder sql, NodeStatusCalculatorConfig query, Map<String, Object> parameterMap) {
        sql.append("WHERE 1=1 ");
        if (!query.getNodeIds().isEmpty()) {
            sql.append("AND node_status.nodeid IN (:nodeIds) ");
            parameterMap.put("nodeIds", query.getNodeIds());
        }
        if(query.getLocation() != null) {
            sql.append("AND node.location = :nodeLocation ");
            parameterMap.put("nodeLocation", query.getLocation());
        }
        if (query.getSeverities() != null && !query.getSeverities().isEmpty()) {
            sql.append(String.format("AND %s IN (:severities) ", getSeverityColumn(query)));
            parameterMap.put("severities", query.getSeverities().stream().map(s -> s.getId()).collect(Collectors.toList()));
        }
    }
}
