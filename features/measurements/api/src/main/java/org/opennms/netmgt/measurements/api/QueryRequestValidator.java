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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.opennms.netmgt.measurements.api.exceptions.ValidationException;
import org.opennms.netmgt.measurements.model.Expression;
import org.opennms.netmgt.measurements.model.FilterDef;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.Source;

public class QueryRequestValidator {

    private final static String VALID_ATTRIBUTE_NAME_PATTERN = "[a-zA-Z_$]+[0-9a-zA-Z_$]*";
    private final Pattern validAttributeName = Pattern.compile(VALID_ATTRIBUTE_NAME_PATTERN);

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

        checkIfInvalidVariablesAreUsedInExpressions(labels.keySet(), request.getExpressions());

        List<FilterDef> filters = request.getFilters();
        if (filters.size() > 0) {
            for (FilterDef filter : filters) {
                if (filter.getName() == null) {
                    throw new ValidationException("Filter name must be set: {}", filter);
                }
            }
        }
    }

    void checkIfInvalidVariablesAreUsedInExpressions(final Collection<String> variables, final Collection<Expression> expressions) throws ValidationException {
        Objects.requireNonNull(variables);
        Objects.requireNonNull(expressions);

        for(String variable : variables) {
            if(isValidAttributeName(variable)) {
                continue;
            }
            for(Expression expression : expressions) {
                if(expression.getExpression().contains(variable)) {
                    throw new ValidationException("Invalid label used in expression. Label='{}', Expression='{}'. Allowed characters: '{}' ",
                            variable, expression, VALID_ATTRIBUTE_NAME_PATTERN);
                }
            }
        }
    }

    boolean isValidAttributeName(final String s) {
        return this.validAttributeName.matcher(s).matches();
    }
}
