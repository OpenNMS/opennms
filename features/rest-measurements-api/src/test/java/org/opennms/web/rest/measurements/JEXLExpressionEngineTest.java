/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.measurements;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.easymock.EasyMock;
import org.junit.Test;
import org.opennms.web.rest.measurements.fetch.FetchResults;
import org.opennms.web.rest.measurements.model.Expression;
import org.opennms.web.rest.measurements.model.Measurement;
import org.opennms.web.rest.measurements.model.QueryRequest;
import org.opennms.web.rest.measurements.model.Source;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JEXLExpressionEngineTest {

    private final ExpressionEngine jexlExpressionEngine = new JEXLExpressionEngine();

    @Test(expected=ExpressionException.class)
    public void failsWhenExpressionHasInvalidSyntax() throws ExpressionException {
        QueryRequest request = new QueryRequest();

        Expression invalid = new Expression();
        invalid.setLabel("expressionWithInvalidSyntax");
        invalid.setExpression("/");
        request.setExpressions(Lists.newArrayList(invalid));

        FetchResults results = EasyMock.createNiceMock(FetchResults.class);

        jexlExpressionEngine.getMeasurements(request, results);
    }

    @Test(expected=ExpressionException.class)
    public void failsWhenExpressionDoesNotReturnADouble() throws ExpressionException {
        QueryRequest request = new QueryRequest();

        Source constant = new Source();
        constant.setLabel("one");
        request.setSources(Lists.newArrayList(constant));

        Expression invalid = new Expression();
        invalid.setLabel("expressionWithInvalidReturnType");
        invalid.setExpression("!(!true)");
        request.setExpressions(Lists.newArrayList(invalid));

        FetchResults results = buildResults("one", 1, 1);

        jexlExpressionEngine.getMeasurements(request, results);
    }

    @Test
    public void canPerformLinearCombination() throws ExpressionException {
        QueryRequest request = new QueryRequest();

        Source constant = new Source();
        constant.setLabel("x");
        request.setSources(Lists.newArrayList(constant));

        Expression linearCombination = new Expression();
        linearCombination.setLabel("y");
        linearCombination.setExpression("x * 5 + 7");
        request.setExpressions(Lists.newArrayList(linearCombination));

        FetchResults results = buildResults("x", 1, 1);

        List<Measurement> measurements = jexlExpressionEngine.getMeasurements(request, results);

        assertEquals(12, measurements.get(0).getValues().get("y"), 0.0001);
    }

    private static FetchResults buildResults(String column, long length, double value) {
        SortedMap<Long, Map<String, Double>> rows = Maps.newTreeMap();
        for (long i = 0; i < length; i++) {
            Map<String, Double> row = Maps.newHashMap();
            row.put(column, value);
            rows.put(i, row);
        }

        Map<String, Object> constants = Maps.newHashMap();
        return new FetchResults(rows, 1, constants);
    }
}
