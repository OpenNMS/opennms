/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.measurements.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.measurements.api.exceptions.ValidationException;
import org.opennms.netmgt.measurements.model.Expression;
import org.opennms.netmgt.measurements.model.FilterDef;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.Source;

public class QueryRequestValidator {
    public void validate(QueryRequest request) throws ValidationException {
        if (request.getEnd() < 0) {
            throw new ValidationException("Query end must be >= 0: {}", request.getEnd());
        }
        if (request.getStep() <= 0) {
            throw new ValidationException("Query step must be > 0: {}", request.getStep());
        }
        if ((request.getHeartbeat() == null && request.getInterval() != null)
                || (request.getHeartbeat() != null && request.getInterval() == null)) {
            throw new ValidationException("If either the heartbeat or the interval are set, then both must be set.");
        }
        if (request.getHeartbeat() != null && request.getInterval() != null) {
            if (request.getHeartbeat() <= 0) {
                throw new ValidationException("Heartbeat must be positive: {}", request.getHeartbeat());
            }
            if (request.getInterval() <= 0) {
                throw new ValidationException("Interval must be positive: {}", request.getInterval());
            }

            if (request.getStep() % request.getInterval() != 0) {
                throw new ValidationException("Step must be a multiple of the interval. Step: {}, Interval: {}",
                        request.getStep(), request.getInterval());
            }

            if (request.getHeartbeat() % request.getInterval() != 0) {
                throw new ValidationException("Heartbeat must be a multiple of the interval. Interval: {} Heartbeat: {}",
                        request.getInterval(), request.getHeartbeat());
            }
        }

        final Map<String,String> labels = new HashMap<>();
        for (final Source source : request.getSources()) {
            if (source.getResourceId() == null
                    || source.getAttribute() == null
                    || source.getLabel() == null
                    || source.getAggregation() == null) {
                throw new ValidationException("Query source fields must be set: {}", source);
            }
            if (labels.containsKey(source.getLabel())) {
                throw new ValidationException("Query source label '{}' conflict: source with that label is already defined.", source.getLabel());
            } else {
                labels.put(source.getLabel(), "source");
            }
        }
        for (final Expression expression : request.getExpressions()) {
            if (expression.getExpression() == null
                    || expression.getLabel() == null) {
                throw new ValidationException("Query expression fields must be set: {}", expression);
            }
            if (labels.containsKey(expression.getLabel())) {
                final String type = labels.get(expression.getLabel());
                throw new ValidationException("Query expression label '{}' conflict: {} with that label is already defined.", expression.getLabel(), type);
            } else {
                labels.put(expression.getLabel(), "expression");
            }
        }
        List<FilterDef> filters = request.getFilters();
        if (filters.size() > 0) {
            for (FilterDef filter : filters) {
                if (filter.getName() == null) {
                    throw new ValidationException("Filter name must be set: {}", filter);
                }
            }
        }
    }
}
