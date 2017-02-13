/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.util.Map;

import org.opennms.netmgt.measurements.api.exceptions.FetchException;
import org.opennms.netmgt.measurements.api.exceptions.MeasurementException;
import org.opennms.netmgt.measurements.api.exceptions.ResourceNotFoundException;
import org.opennms.netmgt.measurements.api.exceptions.ValidationException;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.RowSortedTable;

@Component("measurementsService")
public class DefaultMeasurementsService implements MeasurementsService {

    private final MeasurementFetchStrategy fetchStrategy;
    private final ExpressionEngine expressionEngine;
    private final FilterEngine filterEngine;
    private final QueryRequestValidator queryRequestValidator = new QueryRequestValidator();

    @Autowired
    public DefaultMeasurementsService(MeasurementFetchStrategy fetchStrategy, ExpressionEngine expressionEngine, FilterEngine filterEngine) {
        this.fetchStrategy = Preconditions.checkNotNull(fetchStrategy);
        this.expressionEngine = Preconditions.checkNotNull(expressionEngine);
        this.filterEngine = Preconditions.checkNotNull(filterEngine);
    }

    @Override
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
                    request.getHeartbeat(),
                    request.getInterval(),
                    request.getSources(),
                    request.isRelaxed());
        } catch (Exception e) {
            throw new FetchException(e, "Fetch failed: {}", e.getMessage());
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
        response.setConstants(results.getConstants());
        return response;
    }

    private void validate(QueryRequest request) throws ValidationException {
        queryRequestValidator.validate(request);
    }
}
