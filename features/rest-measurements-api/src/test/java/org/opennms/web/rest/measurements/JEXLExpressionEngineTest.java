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

import java.util.Map;

import org.junit.Test;
import org.opennms.web.rest.measurements.fetch.FetchResults;
import org.opennms.web.rest.measurements.model.Expression;
import org.opennms.web.rest.measurements.model.QueryRequest;
import org.opennms.web.rest.measurements.model.Source;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JEXLExpressionEngineTest {

    private final ExpressionEngine jexlExpressionEngine = new JEXLExpressionEngine();

    @Test(expected=ExpressionException.class)
    public void failsWhenExpressionHasInvalidSyntax() throws ExpressionException {
        peformExpression("/");
    }

    @Test(expected=ExpressionException.class)
    public void failsWhenExpressionDoesNotReturnADouble() throws ExpressionException {
        peformExpression("!(!true)");
    }

    @Test
    public void canPerformLinearCombination() throws ExpressionException {
        double results[] = peformExpression("x * 5 + 7");
        assertEquals(12, results[1], 0.0001);
    }

    @Test
    public void canPerformSin() throws ExpressionException {
        double results[] = peformExpression("math:sin(x)");
        assertEquals(Math.sin(1.0d), results[1], 0.0001);
    }

    @Test
    public void canReferenceTimestamp() throws ExpressionException {
        double results[] = peformExpression("timestamp / 125.0d");
        assertEquals(400.0d, results[50], 0.0001);
    }

    @Test
    public void canReferenceConstant() throws ExpressionException {
        Map<String, Object> constants = Maps.newHashMap();
        constants.put("speed", 65);

        double results[] = peformExpression("speed / 0.62137", constants);
        assertEquals(104.607560713, results[0], 0.0001);
    }

    private double[] peformExpression(String expression) throws ExpressionException {
        Map<String, Object> constants = Maps.newHashMap();
        return peformExpression(expression, constants);
    }

    private double[] peformExpression(String expression, Map<String, Object> constants) throws ExpressionException {
        // Build a simple request with the given expression
        QueryRequest request = new QueryRequest();

        Source constant = new Source();
        constant.setLabel("x");
        request.setSources(Lists.newArrayList(constant));

        Expression exp = new Expression();
        exp.setLabel("y");
        exp.setExpression(expression);
        request.setExpressions(Lists.newArrayList(exp));

        // Build the fetch results with known values
        final int N = 100;
        long timestamps[] = new long[N];
        double xValues[] = new double[N];
        for (int i = 0; i < N; i++) {
            timestamps[i] = i * 1000;
            xValues[i] = Double.valueOf(i);
        }
        Map<String, double[]> values = Maps.newHashMap();
        values.put("x", xValues);
        FetchResults results = new FetchResults(timestamps, values, 1, constants);

        // Use the engine to evaluate the expression
        jexlExpressionEngine.applyExpressions(request, results);

        // Retrieve the results
        return results.getColumns().get("y");
    }
}
