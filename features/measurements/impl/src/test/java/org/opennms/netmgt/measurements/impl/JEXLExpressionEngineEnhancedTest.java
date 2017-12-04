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

import static org.junit.Assert.*;

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

public class JEXLExpressionEngineEnhancedTest {
	private static final double DELTA = 1e-15;

	private final ExpressionEngine jexlExpressionEngine = new JEXLExpressionEngine();

	@Test
	public void checkSamplefn5NaN() throws ExpressionException {
		final String expression = "fn:arrayNaN(\"x\", 5)";

		StringBuffer sb = new StringBuffer("");

		double[] result = performExpression(expression);

		for (double value: result){
			sb.append(value+",");
		}
		System.out.println("JEXLExpressionEngineEnhancedTest: expression="+expression +"\n    result "+sb.toString());

		assertEquals(Double.NaN, result[0], DELTA);
		assertEquals(Double.NaN, result[1], DELTA);
		assertEquals(Double.NaN, result[2], DELTA);
		assertEquals(Double.NaN, result[3], DELTA);
		assertEquals(Double.NaN, result[4], DELTA);
		assertEquals(5, result[5], DELTA);
		assertEquals(6, result[6], DELTA);


	}

	@Test
	public void checkSamplefn5Zero() throws ExpressionException{
		final String expression = "fn:arrayZero(\"x\", 5)";

		StringBuffer sb = new StringBuffer("");

		double[] result = performExpression(expression);

		for (double value: result){
			sb.append(value+",");
		}
		System.out.println("JEXLExpressionEngineEnhancedTest: expression:"+expression +"\n    result "+sb.toString());

		assertEquals(Double.valueOf(0), result[0], DELTA);
		assertEquals(Double.valueOf(0), result[1], DELTA);
		assertEquals(Double.valueOf(0), result[2], DELTA);
		assertEquals(Double.valueOf(0), result[3], DELTA);
		assertEquals(Double.valueOf(0), result[4], DELTA);
		assertEquals(Double.valueOf(5), result[5], DELTA);
		assertEquals(Double.valueOf(6), result[6], DELTA);

	}

	@Test
	public void checkSamplefn5First() throws ExpressionException{
		final String expression = "fn:arrayFirst(\"x\", 5)";


		StringBuffer sb = new StringBuffer("");

		double[] result = performExpression(expression);

		for (double value: result){
			sb.append(value+",");
		}
		System.out.println("JEXLExpressionEngineEnhancedTest: expression:"+expression +"\n    result "+sb.toString());

		assertEquals(Double.valueOf(5), result[0], DELTA);
		assertEquals(Double.valueOf(5), result[1], DELTA);
		assertEquals(Double.valueOf(5), result[2], DELTA);
		assertEquals(Double.valueOf(5), result[3], DELTA);
		assertEquals(Double.valueOf(5), result[4], DELTA);
		assertEquals(Double.valueOf(5), result[5], DELTA);
		assertEquals(Double.valueOf(6), result[6], DELTA);

	}

	@Test
	public void checkSamplefn5Start() throws ExpressionException{
		final String expression = "fn:arrayStart(\"x\", 5, 10)";

		StringBuffer sb = new StringBuffer("");

		double[] result = performExpression(expression);

		for (double value: result){
			sb.append(value+",");
		}
		System.out.println("JEXLExpressionEngineEnhancedTest: expression:"+expression +"\n    result "+sb.toString());

		assertEquals(Double.valueOf(10), result[0], DELTA);
		assertEquals(Double.valueOf(10), result[1], DELTA);
		assertEquals(Double.valueOf(10), result[2], DELTA);
		assertEquals(Double.valueOf(10), result[3], DELTA);
		assertEquals(Double.valueOf(10), result[4], DELTA);
		assertEquals(Double.valueOf(5), result[5], DELTA);
		assertEquals(Double.valueOf(6), result[6], DELTA);

	}


	// y = m * x +c
	@Test
	public void checkConstantBasedContextExpression() throws ExpressionException{
		final String expression = "jexl:evaluate(__formula)";
		final String formula="m*x+c";

		final Map<String,Object> constants = Maps.newHashMap();

		constants.put("__formula", formula);
		constants.put("m", 10);
		constants.put("c", 1.5);


		StringBuffer sb = new StringBuffer("");

		double[] result = performExpression(expression,constants);

		for (double value: result){
			sb.append(value+",");
		}

		System.out.println("JEXLExpressionEngineEnhancedTest:\n   expression:"+expression +"\n   formula:"+formula+ "\n   result "+sb.toString());

		assertEquals(Double.valueOf(51.5), result[0], DELTA);
		assertEquals(Double.valueOf(61.5), result[1], DELTA);
		assertEquals(Double.valueOf(71.5), result[2], DELTA);
		assertEquals(Double.valueOf(81.5), result[3], DELTA);
		assertEquals(Double.valueOf(91.5), result[4], DELTA);
		assertEquals(Double.valueOf(101.5), result[5], DELTA);

	}

	// y = a * f(n) + b * f(n-1) + c * f(n-2)
	@Test
	public void checkTimeSeriesConstantBasedContextExpression() throws ExpressionException{
		final String expression = "jexl:evaluate(__formula)";

		final String formula = "a * x + b * fn:arrayNaN(\"x\", 1) + c * fn:arrayNaN(\"x\", 2)";

		final Map<String,Object> constants = Maps.newHashMap();

		constants.put("__formula", formula);

		constants.put("a", 2);
		constants.put("b", 0.5);
		constants.put("c", 0.25);


		StringBuffer sb = new StringBuffer("");

		double[] result = performExpression(expression,constants);

		for (double value: result){
			sb.append(value+",");
		}
		System.out.println("JEXLExpressionEngineEnhancedTest:\n   expression:"+expression +"\n   formula:"+formula+ "\n   result "+sb.toString());

		assertEquals(Double.NaN, result[0], DELTA);
		assertEquals(Double.NaN, result[1], DELTA);
		assertEquals(Double.valueOf(18.25), result[2], DELTA);
		assertEquals(Double.valueOf(21.0), result[3], DELTA);
		assertEquals(Double.valueOf(23.75), result[4], DELTA);
		assertEquals(Double.valueOf(26.5), result[5], DELTA);

	}


	@Test
	public void checkEconstant() throws ExpressionException{
		final String expression = "jexl:evaluate(__formula)";

		final String formula = "__E";

		final Map<String,Object> constants = Maps.newHashMap();

		constants.put("__formula", formula);

		StringBuffer sb = new StringBuffer("");

		double[] result = performExpression(expression,constants);

		for (double value: result){
			sb.append(value+",");
		}
		System.out.println("JEXLExpressionEngineEnhancedTest:\n   expression:"+expression +"\n   formula:"+formula+ "\n   result "+sb.toString());

		assertEquals(java.lang.Math.E, result[0], DELTA);
		assertEquals(java.lang.Math.E, result[1], DELTA);

	}

	@Test
	public void checkPIconstant() throws ExpressionException{
		final String expression = "jexl:evaluate(__formula)";

		final String formula = "__PI";

		final Map<String,Object> constants = Maps.newHashMap();

		constants.put("__formula", formula);

		StringBuffer sb = new StringBuffer("");

		double[] result = performExpression(expression,constants);

		for (double value: result){
			sb.append(value+",");
		}
		System.out.println("JEXLExpressionEngineEnhancedTest:\n   expression:"+expression +"\n   formula:"+formula+ "\n   result "+sb.toString());

		assertEquals(java.lang.Math.PI, result[0], DELTA);
		assertEquals(java.lang.Math.PI, result[1], DELTA);

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
			xValues[i] = Double.valueOf(i+5); // note for tests values start from 5
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
