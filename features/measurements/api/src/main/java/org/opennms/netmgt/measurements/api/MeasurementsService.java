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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.RowSortedTable;

import org.opennms.netmgt.measurements.api.exceptions.FetchException;
import org.opennms.netmgt.measurements.api.exceptions.MeasurementException;
import org.opennms.netmgt.measurements.api.exceptions.ResourceNotFoundException;
import org.opennms.netmgt.measurements.api.exceptions.ValidationException;
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
    private final QueryRequestValidator queryRequestValidator = new QueryRequestValidator();

    @Autowired
    public MeasurementsService(MeasurementFetchStrategy fetchStrategy, ExpressionEngine expressionEngine, FilterEngine filterEngine) {
        this.fetchStrategy = Preconditions.checkNotNull(fetchStrategy);
        this.expressionEngine = Preconditions.checkNotNull(expressionEngine);
        this.filterEngine = Preconditions.checkNotNull(filterEngine);
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

        // Build the list of column names ordered as defined by sources but skipping transient sources
        final String[] labels = FluentIterable.from(request.getSources())
                                              .filter(new Predicate<Source>() {
                                                  @Override
                                                  public boolean apply(final Source source) {
                                                      return !source.getTransient();
                                                  }
                                              })
                                              .transform(new Function<Source, String>() {
                                                  @Override
                                                  public String apply(final Source source) {
                                                      return source.getLabel();
                                                  }
                                              })
                                              .toArray(String.class);

        // Build the list of columns ordered as defined by the labels
        final QueryResponse.WrappedPrimitive[] columns = new QueryResponse.WrappedPrimitive[labels.length];
        for (int i = 0; i < labels.length; i++) {
            columns[i] = new QueryResponse.WrappedPrimitive(results.getColumns().get(labels[i]));
        }

        // Build the response
        final QueryResponse response = new QueryResponse();
        response.setStart(request.getStart());
        response.setEnd(request.getEnd());
        response.setStep(results.getStep());
        response.setTimestamps(results.getTimestamps());
        response.setLabels(labels);
        response.setColumns(columns);
        response.setConstants(results.getConstants());
        return response;
    }

    private void validate(QueryRequest request) throws ValidationException {
        queryRequestValidator.validate(request);
    }
}
