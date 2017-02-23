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

package org.opennms.netmgt.measurements.impl;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.measurements.api.ExpressionEngine;
import org.opennms.netmgt.measurements.api.exceptions.ExpressionException;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.Expression;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.Source;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JEXLExpressionEngineTest {

    private static final double DELTA = 0.0001;
    private final ExpressionEngine jexlExpressionEngine = new JEXLExpressionEngine();

    @Test(expected=ExpressionException.class)
    public void failsWhenExpressionHasInvalidSyntax() throws ExpressionException {
        performExpression("/");
    }

    @Test(expected=ExpressionException.class)
    public void failsWhenExpressionDoesNotReturnADouble() throws ExpressionException {
        performExpression("!(!true)");
    }

    @Test
    public void canPerformLinearCombination() throws ExpressionException {
        double results[] = performExpression("x * 5 + 7");
        assertEquals(12, results[1], DELTA);
    }

    @Test
    public void canPerformSin() throws ExpressionException {
        double results[] = performExpression("math:sin(x)");
        assertEquals(Math.sin(1.0d), results[1], DELTA);
    }

    /*
     * LIMIT
     * 
     * Pops two elements from the stack and uses them to define a range.
     * Then it pops another element and if it falls inside the range, it is
     * pushed back. If not, an unknown is pushed. The range defined includes
     * the two boundaries (so: a number equal to one of the boundaries will be
     * pushed back). If any of the three numbers involved is either unknown or
     * infinite this function will always return an unknown
     * 
     * Example: CDEF:a=alpha,0,100,LIMIT will return unknown if alpha is
     * lower than 0 or if it is higher than 100.
     */
    @Test
    public void canPerformLimit() throws ExpressionException {
        final String limitExpression = "( ( (A == __inf) || (A == __neg_inf) || (B == __inf) || (B == __neg_inf) || (C == __inf) || (C == __neg_inf) || (C < A) || (C > B) ) ? NaN : C )";

        // a = min, b = max, c = value
        final double a = 0.0;
        final double b = 0.14290626;
        final double withinRange = 0.1;
        final Map<String,Object> entries = Maps.newHashMap();

        // value is within the range (value)
        entries.put("A", a);
        entries.put("B", b);
        entries.put("C", 0.01);
        double results[] = performExpression(limitExpression, entries);
        assertEquals(0.01, results[0], DELTA);

        // value is equal to minimum (value)
        entries.put("A", a);
        entries.put("B", b);
        entries.put("C", a);
        results = performExpression(limitExpression, entries);
        assertEquals(a, results[0], DELTA);

        // value is less than minimum (unknown)
        entries.put("A", a);
        entries.put("B", b);
        entries.put("C", a - 1.0);
        results = performExpression(limitExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);

        // value is equal to maximum (value)
        entries.put("A", a);
        entries.put("B", b);
        entries.put("C", b);
        results = performExpression(limitExpression, entries);
        assertEquals(b, results[0], DELTA);

        // value is greater than maximum (unknown)
        entries.put("A", a);
        entries.put("B", b);
        entries.put("C", b + 1.0);
        results = performExpression(limitExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);

        // value is -infinity (unknown)
        entries.put("A", a);
        entries.put("B", b);
        entries.put("C", Double.NEGATIVE_INFINITY);
        results = performExpression(limitExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);

        // minimum is -infinity (unknown)
        entries.put("A", Double.NEGATIVE_INFINITY);
        entries.put("B", b);
        entries.put("C", withinRange);
        results = performExpression(limitExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);

        // minimum is infinity (unknown)
        entries.put("A", Double.POSITIVE_INFINITY);
        entries.put("B", b);
        entries.put("C", withinRange);
        results = performExpression(limitExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);

        // maximum is -infinity (unknown)
        entries.put("A", a);
        entries.put("B", Double.NEGATIVE_INFINITY);
        entries.put("C", withinRange);
        results = performExpression(limitExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);

        // maximum is infinity (unknown)
        entries.put("A", a);
        entries.put("B", Double.POSITIVE_INFINITY);
        entries.put("C", withinRange);
        results = performExpression(limitExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);
    }

    @Test
    public void testMinMax() throws ExpressionException {
        final String minExpression = "math:min(A,B)";
        final String maxExpression = "math:max(A,B)";

        // a = min, b = max, c = value
        final double small = 1.0;
        final double large = 100.0;
        final Map<String,Object> entries = Maps.newHashMap();

        // min: small is smaller than large
        entries.put("A", small);
        entries.put("B", large);
        double results[] = performExpression(minExpression, entries);
        assertEquals(small, results[0], DELTA);

        // min: small is smaller than large
        entries.put("A", large);
        entries.put("B", small);
        results = performExpression(minExpression, entries);
        assertEquals(small, results[0], DELTA);

        // min: small is same as small
        entries.put("A", small);
        entries.put("B", small);
        results = performExpression(minExpression, entries);
        assertEquals(small, results[0], DELTA);

        // min: unknown is unknown
        entries.put("A", small);
        entries.put("B", Double.NaN);
        results = performExpression(minExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);

        // max: large is larger than small
        entries.put("A", small);
        entries.put("B", large);
        results = performExpression(maxExpression, entries);
        assertEquals(large, results[0], DELTA);

        // max: large is larger than small
        entries.put("A", large);
        entries.put("B", small);
        results = performExpression(maxExpression, entries);
        assertEquals(large, results[0], DELTA);

        // max: large is same as large
        entries.put("A", large);
        entries.put("B", large);
        results = performExpression(maxExpression, entries);
        assertEquals(large, results[0], DELTA);

        // max: unknown is unknown
        entries.put("A", small);
        entries.put("B", Double.NaN);
        results = performExpression(maxExpression, entries);
        assertEquals(Double.NaN, results[0], DELTA);
    }

    @Test
    public void testMinMaxNaN() throws ExpressionException {
        final String minExpression = "( ( A == NaN ) ? B : ( ( B == NaN ) ? A : math:min(A,B) ) )";
        final String maxExpression = "( ( A == NaN ) ? B : ( ( B == NaN ) ? A : math:max(A,B) ) )";

        // a = min, b = max, c = value
        final double small = 1.0;
        final double large = 100.0;
        final Map<String,Object> entries = Maps.newHashMap();

        // min: small is smaller than large
        entries.put("A", small);
        entries.put("B", large);
        double results[] = performExpression(minExpression, entries);
        assertEquals(small, results[0], DELTA);

        // min: small is smaller than large
        entries.put("A", large);
        entries.put("B", small);
        results = performExpression(minExpression, entries);
        assertEquals(small, results[0], DELTA);

        // min: small is same as small
        entries.put("A", small);
        entries.put("B", small);
        results = performExpression(minExpression, entries);
        assertEquals(small, results[0], DELTA);

        // min: value and unknown is value
        entries.put("A", small);
        entries.put("B", Double.NaN);
        results = performExpression(minExpression, entries);
        assertEquals(small, results[0], DELTA);

        // min: unknown and value is value
        entries.put("A", Double.NaN);
        entries.put("B", small);
        results = performExpression(minExpression, entries);
        assertEquals(small, results[0], DELTA);

        // max: large is larger than small
        entries.put("A", small);
        entries.put("B", large);
        results = performExpression(maxExpression, entries);
        assertEquals(large, results[0], DELTA);

        // max: large is larger than small
        entries.put("A", large);
        entries.put("B", small);
        results = performExpression(maxExpression, entries);
        assertEquals(large, results[0], DELTA);

        // max: large is same as large
        entries.put("A", large);
        entries.put("B", large);
        results = performExpression(maxExpression, entries);
        assertEquals(large, results[0], DELTA);

        // max: value and unknown is value
        entries.put("A", small);
        entries.put("B", Double.NaN);
        results = performExpression(maxExpression, entries);
        assertEquals(small, results[0], DELTA);

        // max: unknown and value is value
        entries.put("A", Double.NaN);
        entries.put("B", small);
        results = performExpression(maxExpression, entries);
        assertEquals(small, results[0], DELTA);
    }

    @Test
    public void testAddNan() throws Exception {
        final String expression = "( ( ( A == NaN ) && ( B == NaN ) ) ? NaN : ( ( A == NaN ) ? B : ( ( B == NaN ) ? A : ( A + B ) ) ) )";

        // a = min, b = max, c = value
        final double a = 1.0;
        final double b = 100.0;
        final Map<String,Object> entries = Maps.newHashMap();

        // a + b = sum
        entries.put("A", a);
        entries.put("B", b);
        double results[] = performExpression(expression, entries);
        assertEquals(a+b, results[0], DELTA);

        // a + NaN = a
        entries.put("A", a);
        entries.put("B", Double.NaN);
        results = performExpression(expression, entries);
        assertEquals(a, results[0], DELTA);

        // NaN + b = b
        entries.put("A", Double.NaN);
        entries.put("B", b);
        results = performExpression(expression, entries);
        assertEquals(b, results[0], DELTA);

        // NaN + NaN = NaN
        entries.put("A", Double.NaN);
        entries.put("B", Double.NaN);
        results = performExpression(expression, entries);
        assertEquals(Double.NaN, results[0], DELTA);
    }

    @Test
    public void testMath() throws Exception {
        final Map<String,Object> entries = Maps.newHashMap();

        entries.put("A", 1);
        double results[] = performExpression("math:sin(A)", entries);
        assertEquals(0.841470, results[0], DELTA);

        entries.put("A", 1);
        results = performExpression("math:cos(A)", entries);
        assertEquals(0.540302, results[0], DELTA);

        entries.put("A", Math.E);
        results = performExpression("math:log(A)", entries);
        assertEquals(1, results[0], DELTA);

        entries.put("A", 1);
        results = performExpression("math:exp(A)", entries);
        assertEquals(Math.E, results[0], DELTA);

        entries.put("A", 4);
        results = performExpression("math:sqrt(A)", entries);
        assertEquals(2, results[0], DELTA);

        entries.put("A", 1);
        results = performExpression("math:atan(A)", entries);
        assertEquals(0.7853981633974483, results[0], DELTA);

        entries.put("Y", 90);
        entries.put("X", 15);
        results = performExpression("math:atan2(Y,X)", entries);
        assertEquals(1.4056476493802699, results[0], DELTA);
    }

    @Test
    public void canReferenceTimestamp() throws ExpressionException {
        double results[] = performExpression("timestamp / 125.0d");
        assertEquals(400.0d, results[50], 0.0001);
    }

    @Test
    public void canReferenceConstant() throws ExpressionException {
        Map<String, Object> constants = Maps.newHashMap();
        constants.put("speed", 65);

        double results[] = performExpression("speed / 0.62137", constants);
        assertEquals(104.607560713, results[0], 0.0001);
    }

    @Test
    public void canReferenceDiffTime() throws ExpressionException {
        // The __diff_time attribute is used by Backshift
        double results[] = performExpression("1 * __diff_time");
        assertEquals(99000.0, results[0], 0.0001);
    }

    private double[] performExpression(String expression) throws ExpressionException {
        Map<String, Object> constants = Maps.newHashMap();
        return performExpression(expression, constants);
    }

    private double[] performExpression(String expression, Map<String, Object> constants) throws ExpressionException {
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
