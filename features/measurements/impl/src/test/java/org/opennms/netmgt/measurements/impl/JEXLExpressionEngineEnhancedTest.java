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

import org.apache.commons.jexl2.JexlContext;
import org.junit.Test;
import org.opennms.netmgt.measurements.api.ExpressionEngine;
import org.opennms.netmgt.measurements.api.exceptions.ExpressionException;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.Expression;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JEXLExpressionEngineEnhancedTest {

	private final ExpressionEngine jexlExpressionEngine = new JEXLExpressionEngine();

	@Test
	public void checkSamplePrior5NaN(){
		final String expression = "prior:zStartNaN(\"x\", 5, __context)";
		boolean success = true;

		StringBuffer sb = new StringBuffer("");
		try{

			double[] result = performExpression(expression);

			for (double value: result){
				sb.append(value+",");
			}
			success = (result[0] == Double.NaN);
			success = (result[1] == Double.NaN);
			success = (result[2] == Double.NaN);
			success = (result[3] == Double.NaN);
			success = (result[4] == Double.NaN);
			success = (result[5] == 5);
			success = (result[6] == 6);


		} catch (Exception e){
			e.printStackTrace();
			success = false;
		}


		System.out.println("JEXLExpressionEngineEnhancedTest: expression:"+expression +"\n    result "+sb.toString());
		assertTrue(success);
	}

	@Test
	public void checkSamplePrior5Zero(){
		final String expression = "prior:zStartZero(\"x\", 5, __context)";
		boolean success = true;

		StringBuffer sb = new StringBuffer("");
		try{

			double[] result = performExpression(expression);

			for (double value: result){
				sb.append(value+",");
			}

			success = (result[0] == 0);
			success = (result[1] == 0);
			success = (result[2] == 0);
			success = (result[3] == 0);
			success = (result[4] == 0);
			success = (result[5] == 5);
			success = (result[6] == 6);


		} catch (Exception e){
			e.printStackTrace();
			success = false;
		}


		System.out.println("JEXLExpressionEngineEnhancedTest: expression:"+expression +"\n    result "+sb.toString());
		assertTrue(success);
	}

	@Test
	public void checkSamplePrior5First(){
		final String expression = "prior:zStartFirst(\"x\", 5, __context)";

		boolean success = true;

		StringBuffer sb = new StringBuffer("");
		try{

			double[] result = performExpression(expression);

			for (double value: result){
				sb.append(value+",");
			}
			success = (result[0] == 5);
			success = (result[1] == 5);
			success = (result[2] == 5);
			success = (result[3] == 5);
			success = (result[4] == 5);
			success = (result[5] == 5);
			success = (result[6] == 6);


		} catch (Exception e){
			e.printStackTrace();
			success = false;
		}


		System.out.println("JEXLExpressionEngineEnhancedTest: expression:"+expression +"\n    result "+sb.toString());
		assertTrue(success);

	}


	// y = m * x +c
	@Test
	public void checkConstantBasedContextExpression(){
		final String expression = "__jexl.createExpression(__formula).evaluate(__context)";
		final String formula="m*x+c";

		final Map<String,Object> constants = Maps.newHashMap();

		constants.put("__formula", formula);
		constants.put("m", 10);
		constants.put("c", 1.5);

		boolean success = true;

		StringBuffer sb = new StringBuffer("");
		try{

			double[] result = performExpression(expression,constants);

			for (double value: result){
				sb.append(value+",");
			}

			success = (result[0] == 51.5);
			success = (result[1] == 61.5);
			success = (result[2] == 71.5);
			success = (result[3] == 81.5);
			success = (result[4] == 91.5);
			success = (result[5] == 101.5);

		} catch (Exception e){
			e.printStackTrace();
			success = false;
		}

		System.out.println("JEXLExpressionEngineEnhancedTest:\n   expression:"+expression +"\n   formula:"+formula+ "\n   result "+sb.toString());
		assertTrue(success);
	}

	// y = a * f(n) + b * f(n-1) + c * f(n-2)
	@Test
	public void checkTimeSeriesContextExpression(){
		final String expression = "__jexl.createExpression(__formula).evaluate(__context)";
		final String formula = "a * x + b * prior:zStartNaN(\"x\", 1, __context) + c * prior:zStartNaN(\"x\", 2, __context)";

		final Map<String,Object> constants = Maps.newHashMap();

		constants.put("__formula", formula);

		constants.put("a", 2);
		constants.put("b", 0.5);
		constants.put("c", 0.25);

		boolean success = true;

		StringBuffer sb = new StringBuffer("");
		try{

			double[] result = performExpression(expression,constants);

			for (double value: result){
				sb.append(value+",");
			}

			success = (result[0] == Double.NaN);
			success = (result[1] == Double.NaN);
			success = (result[2] == 18.25);
			success = (result[3] == 21.0);
			success = (result[4] == 23.75);
			success = (result[5] == 26.5);

		} catch (Exception e){
			e.printStackTrace();
			success = false;
		}

		System.out.println("JEXLExpressionEngineEnhancedTest:\n   expression:"+expression +"\n   formula:"+formula+ "\n   result "+sb.toString());
		assertTrue(success);
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
			xValues[i] = Double.valueOf(i+5);
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
