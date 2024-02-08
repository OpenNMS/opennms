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
            results = new FetchResults(table, results.getStep(), results.getConstants(), results.getMetadata());
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
        response.setMetadata(results.getMetadata());
        return response;
    }

    private void validate(QueryRequest request) throws ValidationException {
        queryRequestValidator.validate(request);
    }
}
