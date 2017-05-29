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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.utils.QueryParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Service
public class OutageNodeStatusCalculator implements NodeStatusCalculator {

    @Autowired
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Override
    public Status calculateStatus(NodeStatusCalculatorConfig query) {
        final List<String> parameterNames = Lists.newArrayList();
        final List<Object> parameterValues = Lists.newArrayList();

        // Build Query
        final StringBuilder hql = new StringBuilder();
        hql.append("SELECT node.id, max(case when event.eventSeverity is null then 3 else event.eventSeverity end) as severity ");
        hql.append("FROM OnmsOutage as outage ");
        hql.append("RIGHT JOIN outage.monitoredService as service ");
        hql.append("RIGHT JOIN service.ipInterface as iface ");
        hql.append("RIGHT JOIN iface.node as node ");
        hql.append("LEFT JOIN outage.serviceLostEvent as event ");

        applyRestrictions(hql, query, parameterNames, parameterValues);

        hql.append("GROUP BY node.id ");

        // Apply order
        if (query.getOrder() != null && !Strings.isNullOrEmpty(query.getOrder().getColumn())) {
            final QueryParameters.Order order = query.getOrder();
            hql.append(String.format("ORDER BY %s %s ",  order.getColumn(), order.isDesc() ? "desc" : "asc"));
        }

        // Execute query
        final List<Object[]> rows = genericPersistenceAccessor.findUsingNamedParameters(
                hql.toString(),
                parameterNames.toArray(new String[parameterNames.size()]),
                parameterValues.toArray(),
                query.getOffset(),
                query.getLimit());
        final Status status = new Status();
        for (Object[] eachRow : rows) {
            status.add((int) eachRow[0], OnmsSeverity.get((int) eachRow[1]), 0, 0);
        }
        return status;
    }

    @Override
    public int countStatus(NodeStatusCalculatorConfig query) {
        final List<String> parameterNames = Lists.newArrayList();
        final List<Object> parameterValues = Lists.newArrayList();

        final StringBuilder hql = new StringBuilder();
        hql.append("SELECT count(distinct node) ");
        hql.append("FROM OnmsOutage as outage ");
        hql.append("RIGHT JOIN outage.monitoredService as service ");
        hql.append("RIGHT JOIN service.ipInterface as iface ");
        hql.append("RIGHT JOIN iface.node as node ");
        hql.append("LEFT JOIN outage.serviceLostEvent as event ");

        applyRestrictions(hql, query, parameterNames, parameterValues);

        final List<Object> rows = genericPersistenceAccessor.findUsingNamedParameters(
                hql.toString(),
                parameterNames.toArray(new String[parameterNames.size()]),
                parameterValues.toArray());
        if (!rows.isEmpty()) {
            if (rows.get(0) != null && rows.get(0) != null) {
                return ((Long) rows.get(0)).intValue();
            }
        }
        return 0;
    }

    @Override
    public Map<OnmsSeverity, Long> calculateStatusOverview(NodeStatusCalculatorConfig query) {
        final List<String> parameterNames = Lists.newArrayList();
        final List<Object> parameterValues = Lists.newArrayList();

        final StringBuilder hql = new StringBuilder();
        hql.append("SELECT count(outage), max(outage.serviceLostEvent.eventSeverity) as severity ");
        hql.append("FROM OnmsOutage as outage ");
        hql.append("JOIN outage.monitoredService.ipInterface.node as node ");
        hql.append("JOIN outage.serviceLostEvent as event ");

        applyRestrictions(hql, query, parameterNames, parameterValues);

        hql.append("GROUP BY event.eventSeverity");

        final List<Object[]> rows = genericPersistenceAccessor.findUsingNamedParameters(
                hql.toString(),
                parameterNames.toArray(new String[parameterNames.size()]),
                parameterValues.toArray(),
                query.getOffset(),
                query.getLimit());
        final Map<OnmsSeverity, Long> severityStatusMap = new HashMap<>();
        for (Object[] columns : rows) {
            severityStatusMap.put(OnmsSeverity.get((int) columns[1]), (long) columns[0]);
        }
        return severityStatusMap;
    }

    private static void applyRestrictions(StringBuilder hql, NodeStatusCalculatorConfig query, List<String> parameterNames, List<Object> parameterValues) {
        hql.append("WHERE outage.serviceRegainedEvent is null ");
        if (!query.getNodeIds().isEmpty()) {
            hql.append("AND node.id in (:nodeIds) ");
            parameterNames.add("nodeIds");
            parameterValues.add(query.getNodeIds());
        }
        if (query.getLocation() != null) {
            hql.append("AND node.location.locationName = :locationName ");
            parameterNames.add("locationName");
            parameterValues.add(query.getLocation());
        }
        if (!query.getSeverities().isEmpty()) {
            hql.append("AND (");
            hql.append("event.eventSeverity in (:severities) ");
            parameterNames.add("severities");
            parameterValues.add(query.getSeverities().stream().map(s -> s.getId()).collect(Collectors.toList()));

            // If normal, we must include null values
            if (query.getSeverities().contains(OnmsSeverity.NORMAL)) {
                hql.append(" OR event.eventSeverity is null");
            }
            hql.append(") ");
        }
    }
}
