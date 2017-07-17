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

package org.opennms.features.status.api.node.strategy.query;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.status.api.node.strategy.NodeStatusCalculatorConfig;
import org.opennms.features.status.api.node.strategy.Status;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.utils.QueryParameters;

import com.google.common.base.Strings;

public abstract class Query {

    protected final Map<String, Object> parameterMap = new HashMap<>();

    protected StringBuilder sql = new StringBuilder();

    protected final NodeStatusCalculatorConfig config;

    private final GenericPersistenceAccessor genericPersistenceAccessor;

    public Query(GenericPersistenceAccessor genericPersistenceAccessor, NodeStatusCalculatorConfig config) {
        this.config = Objects.requireNonNull(config);
        this.genericPersistenceAccessor = Objects.requireNonNull(genericPersistenceAccessor);
    }

    public int count() {
        sql = new StringBuilder();
        sql.append("SELECT count(*) ");
        sql.append("FROM ").append(getViewName()).append(" ");
        sql.append("JOIN node ON ").append(getViewName()).append(".nodeid = node.nodeid ") ;

        applyRestrictions();

        final int count = executeQuerySingleResult(column -> ((BigInteger) column).intValue());
        return count;
    }

    public Map<OnmsSeverity, Long> overview() {
        sql = new StringBuilder();
        sql.append(String.format("SELECT count(*), %s as severity ", getSeverityColumn()));
        sql.append("FROM ").append(getViewName()).append(" ");
        sql.append("JOIN node on ").append(getViewName()).append(".nodeid = node.nodeid ") ;

        applyRestrictions();

        sql.append("GROUP BY severity ");

        applyLimitAndOffset();

        final Map<OnmsSeverity, Long> statusMap = new HashMap<>();
        executeQuery((RowHandler<Object[]>) columns -> statusMap.put(
                OnmsSeverity.get(((Integer) columns[1]).intValue()),
                ((BigInteger) columns[0]).longValue()
        ));
        return statusMap;
    }

    protected void applyLimitAndOffset() {
        if (config.getLimit() != null) {
            sql.append(String.format(" LIMIT %d", config.getLimit()));
        }
        if (config.getOffset() != null) {
            sql.append(String.format(" OFFSET %d", config.getOffset()));
        }
    }

    protected void applyOrder() {
        // Apply ordering
        if (config.getOrder() != null && !Strings.isNullOrEmpty(config.getOrder().getColumn())) {
            final QueryParameters.Order order = config.getOrder();
            sql.append(String.format("ORDER BY %s %s ",  order.getColumn(), order.isDesc() ? "desc" : "asc"));
        }
    }

    protected void applyRestrictions() {
        sql.append("WHERE 1=1 ");
        if (!config.getNodeIds().isEmpty()) {
            sql.append(String.format("AND %s.nodeid IN (:nodeIds) ", getViewName()));
            parameterMap.put("nodeIds", config.getNodeIds());
        }
        if(config.getLocation() != null) {
            sql.append("AND node.location = :nodeLocation ");
            parameterMap.put("nodeLocation", config.getLocation());
        }
        if (config.getSeverities() != null && !config.getSeverities().isEmpty()) {
            sql.append(String.format("AND %s IN (:severities) ", getSeverityColumn()));
            parameterMap.put("severities", config.getSeverities().stream().map(s -> s.getId()).collect(Collectors.toList()));
        }
    }

    protected NodeStatusCalculatorConfig getConfig() {
        return config;
    }

    // executes query
    protected <T> void executeQuery(RowHandler<T> rowHandler) {
        final List<T> rows = genericPersistenceAccessor.executeNativeQuery(sql.toString(), parameterMap);
        if (rows != null && !rows.isEmpty()) {
            for (T eachRow : rows) {
                rowHandler.handle(eachRow);
            }
        }
    }

    protected <T, X> X executeQuerySingleResult(Function<T, X> function) {
        final List<T> rows = genericPersistenceAccessor.executeNativeQuery(sql.toString(), parameterMap);
        if (rows == null || rows.size() != 1) {
            throw new IllegalStateException("Query returned multiple rows, but only 1 expected");
        }
        T column = rows.get(0);
        return function.apply(column);
    }

    public abstract Status status();

    protected abstract String getSeverityColumn();

    protected abstract String getViewName();
}
