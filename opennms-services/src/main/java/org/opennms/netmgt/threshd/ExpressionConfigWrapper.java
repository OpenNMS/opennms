/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.ExpressionImpl;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.opennms.netmgt.config.threshd.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public class ExpressionConfigWrapper extends BaseThresholdDefConfigWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(ExpressionConfigWrapper.class);

    private final Expression m_expression;
    private final Collection<String> m_datasources;
    public ExpressionConfigWrapper(Expression expression) throws ThresholdExpressionException {
        super(expression);
        m_expression = expression;

        JexlEngine expressionParser = new JexlEngine();
        m_datasources = new ArrayList<>();
        try {
            ExpressionImpl e = (ExpressionImpl) expressionParser.createExpression(m_expression.getExpression());
            LOG.trace("List of Variables on the Expression: {}", e.getVariables());
            for (List<String> list : e.getVariables()) { // Requires JEXL 2.1.x
                if (list.get(0).equalsIgnoreCase("math")) {
                    continue;
                }
                if (list.get(0).equalsIgnoreCase("datasources")) {
                    // Include the internal parameter. See NMS-5019
                    m_datasources.add(list.get(1).intern());
                } else {
                    // Include the first element, because datasources and math are the only composite elements
                    m_datasources.add(list.get(0).intern());
                }
            }
        } catch (Throwable e) {
            throw new ThresholdExpressionException("Could not parse threshold expression:" + e.getMessage(), e);
        }
        LOG.trace("Threshold Variables: {}", m_datasources);
    }

    @Override
    public String getDatasourceExpression() {
        return m_expression.getExpression();
    }
    @Override
    public Collection<String> getRequiredDatasources() {
        return m_datasources;
    }

    /**
     * This class provides an instance that gives access to the {@link java.lang.Math} functions.
     * You can access this variable in your expressions by using the <code>math</code> variable 
     * (ie. <code>math.abs(-1)</code>).
     */
    public static class MathBinding {
        public double abs(double a) { return Math.abs(a); }
        public float abs(float a) { return Math.abs(a); }
        public int abs(int a) { return Math.abs(a); }
        public long abs(long a) { return Math.abs(a); }
        public double acos(double a) { return Math.acos(a); }
        public double asin(double a) { return Math.asin(a); }
        public double atan(double a) { return Math.atan(a); }
        public double atan2(double a, double b) { return Math.atan2(a, b); }
        public double cbrt(double a) { return Math.cbrt(a); }
        public double ceil(double a) { return Math.ceil(a); }
        public double cos(double a) { return Math.cos(a); }
        public double cosh(double a) { return Math.cosh(a); }
        public double exp(double a) { return Math.exp(a); }
        public double expm1(double a) { return Math.expm1(a); }
        public double floor(double a) { return Math.floor(a); }
        public double hypot(double a, double b) { return Math.hypot(a, b); }
        public double IEEEremainder(double a, double b) { return Math.IEEEremainder(a, b); }
        public double log(double a) { return Math.log(a); }
        public double log10(double a) { return Math.log10(a); }
        public double log1p(double a) { return Math.log1p(a); }
        public double max(double a, double b) { return Math.max(a, b); }
        public float max(float a, float b) { return Math.max(a, b); }
        public int max(int a, int b) { return Math.max(a, b); }
        public long max(long a, long b) { return Math.max(a, b); }
        public double min(double a, double b) { return Math.min(a, b); }
        public float min(float a, float b) { return Math.min(a, b); }
        public int min(int a, int b) { return Math.min(a, b); }
        public long min(long a, long b) { return Math.min(a, b); }
        public double pow(double a, double b) { return Math.pow(a, b); }
        public double random() { return Math.random(); }
        public double rint(double a) { return Math.rint(a); }
        public long round(double a) { return Math.round(a); }
        public int round(float a) { return Math.round(a); }
        public double signum(double a) { return Math.signum(a); }
        public float signum(float a) { return Math.signum(a); }
        public double sin(double a) { return Math.sin(a); }
        public double sinh(double a) { return Math.sinh(a); }
        public double sqrt(double a) { return Math.sqrt(a); }
        public double tan(double a) { return Math.tan(a); }
        public double tanh(double a) { return Math.tanh(a); }
        public double toDegrees(double a) { return Math.toDegrees(a); }
        public double toRadians(double a) { return Math.toRadians(a); }
        public double ulp(double a) { return Math.ulp(a); }
        public float ulp(float a) { return Math.ulp(a); }
    }

    @Override
    public double evaluate(Map<String, Double> values) throws ThresholdExpressionException {
        // Add all of the variable values to the script context
        Map<String,Object> context = new HashMap<String,Object>();
        context.putAll(values);
        context.put("datasources", new HashMap<String, Double>(values)); // To workaround NMS-5019
        context.put("math", new MathBinding());
        double result = Double.NaN;
        try {
            // Fetch an instance of the JEXL script engine to evaluate the script expression
            Object resultObject = new JexlEngine().createExpression(m_expression.getExpression()).evaluate(new MapContext(context));
            result = Double.parseDouble(resultObject.toString());
        } catch (Throwable e) {
            throw new ThresholdExpressionException("Error while evaluating expression " + m_expression.getExpression() + ": " + e.getMessage(), e);
        }
        return result;
    }
}
