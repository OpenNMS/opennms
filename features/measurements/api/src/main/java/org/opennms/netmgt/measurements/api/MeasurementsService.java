/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.measurements.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.RowSortedTable;

import org.opennms.netmgt.measurements.api.exceptions.FetchException;
import org.opennms.netmgt.measurements.api.exceptions.MeasurementException;
import org.opennms.netmgt.measurements.api.exceptions.ResourceNotFoundException;
import org.opennms.netmgt.measurements.api.exceptions.ValidationException;
import org.opennms.netmgt.measurements.model.Expression;
import org.opennms.netmgt.measurements.model.FilterDefinition;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("measurementsService")
public class MeasurementsService {

    private final MeasurementFetchStrategy fetchStrategy;
    private final ExpressionEngine expressionEngine;
    private final FilterEngine filterEngine;

    @Autowired
    public MeasurementsService(MeasurementFetchStrategy fetchStrategy, ExpressionEngine expressionEngine, FilterEngine filterEngine) {
        Preconditions.checkNotNull(fetchStrategy);
        Preconditions.checkNotNull(expressionEngine);
        Preconditions.checkNotNull(filterEngine);
        this.fetchStrategy = fetchStrategy;
        this.expressionEngine = expressionEngine;
        this.filterEngine = filterEngine;
    }

    public QueryResponse query(QueryRequest request) throws MeasurementException {
        validate(request);

        // Fetch the measurements
        FetchResults results;
        try {
            results = fetchStrategy.fetch(
                    request.getStart(),
                    request.getEnd(),
                    request.getStep(),
                    request.getMaxRows(),
                    request.getSources());
        } catch (Exception e) {
            throw new FetchException(e, "Fetch failed");
        }
        if (results == null) {
            throw new ResourceNotFoundException(request);
        }

        // Apply the expression to the fetch results
        expressionEngine.applyExpressions(request, results);

        // Apply the filters
        if (!request.getFilters().isEmpty()) {
            RowSortedTable<Long, String, Double> table = results.asRowSortedTable();
            filterEngine.filter(request.getFilters(), table);
            results = new FetchResults(table, results.getStep(), results.getConstants());
        }

        // Remove any transient values belonging to sources
        final Map<String, double[]> columns = results.getColumns();
        for (final Source source : request.getSources()) {
            if (source.getTransient()) {
                columns.remove(source.getLabel());
            }
        }

        // Build the response
        final QueryResponse response = new QueryResponse();
        response.setStart(request.getStart());
        response.setEnd(request.getEnd());
        response.setStep(results.getStep());
        response.setTimestamps(results.getTimestamps());
        response.setColumns(results.getColumns());
        return response;
    }

    private void validate(QueryRequest request) throws ValidationException {
        if (request.getEnd() < 0) {
            throw new ValidationException("Query end must be >= 0: {}", request.getEnd());
        }
        if (request.getStep() <= 0) {
            throw new ValidationException("Query step must be > 0: {}", request.getStep());
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
                throw new ValidationException("Query expression label '" + expression.getLabel() + "' conflict: " + type + " with that label is already defined.");
            } else {
                labels.put(expression.getLabel(), "expression");
            }
        }
        List<FilterDefinition> filters = request.getFilters();
        if (filters.size() > 0) {
            for (FilterDefinition filter : filters) {
                if (filter.getName() == null) {
                    throw new ValidationException("Filter name must be set: {}", filter);
                }
            }
        }
    }
}
