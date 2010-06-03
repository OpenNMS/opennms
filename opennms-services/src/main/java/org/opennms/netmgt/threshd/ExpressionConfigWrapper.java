/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 14, 2007
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.threshd;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.threshd.Expression;


/**
 * 
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public class ExpressionConfigWrapper extends BaseThresholdDefConfigWrapper {

	/**
	 * This class is used to sniff all of the variable names that a script tries
	 * to use out of the ScriptContext during a call to eval(). This will allow
	 * us to construct a list of required parameters for the script expression.
	 */
	private static class BindingsSniffer extends HashMap<String,Object> implements Bindings {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5595028572061424206L;
		private final Set<String> m_sniffedKeys = new HashSet<String>();
		private final String[] ignoreTheseKeys = new String[] { "math" };

		@Override
		public Object get(Object key) {
			LogUtils.tracef(this, "Bindings.get(%s)", key);
			m_sniffedKeys.add((String)key);
			return super.get(key);
		}

		@Override
		public boolean containsKey(Object key) {
			LogUtils.tracef(this, "Bindings.containsKey(%s)", key);
			m_sniffedKeys.add((String)key);
			return super.containsKey(key);
		}

		public Set<String> getSniffedKeys() {
			LogUtils.tracef(this, "Bindings.getSniffedKeys(%s)");
			m_sniffedKeys.removeAll(Arrays.asList(ignoreTheseKeys));
			return Collections.unmodifiableSet(m_sniffedKeys);
		}
	}

	private final Expression m_expression;
	private final Collection<String> m_datasources;
	private final ScriptEngine m_parser;
	public ExpressionConfigWrapper(Expression expression) throws ThresholdExpressionException {
		super(expression);
		m_expression = expression;

		// Fetch an instance of the JEXL script engine
		ScriptEngineManager mgr = new ScriptEngineManager();
		m_parser = mgr.getEngineByName("jexl");

		BindingsSniffer sniffer = new BindingsSniffer();
		sniffer.put("math", new MathBinding());

		// Test parsing of the expression and collect the variable names by using
		// a Bindings instance that sniffs all of the variable names
		try {
			m_parser.eval(m_expression.getExpression(), sniffer);
		} catch (Throwable e) {
			throw new ThresholdExpressionException("Could not parse threshold expression:" + e.getMessage(), e);
		}

		m_datasources = sniffer.getSniffedKeys();
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
		// public float abs(float a) { return Math.abs(a); }
		// public int abs(int a) { return Math.abs(a); }
		// public long abs(long a) { return Math.abs(a); }
		public double acos(double a) { return Math.acos(a); }
		public double asin(double a) { return Math.asin(a); }
		public double atan(double a) { return Math.atan(a); }
		public double atan2(double a, double b) { return Math.atan2(a, b); }
		public double cbrt(double a) { return Math.cbrt(a); }
		public double ceil(double a) { return Math.ceil(a); }
		public double copySign(double magnitude, double sign) { return Math.copySign(magnitude, sign); }
		// public float copySign(float magnitude, float sign) { return Math.copySign(magnitude, sign); }
		public double cos(double a) { return Math.cos(a); }
		public double cosh(double a) { return Math.cosh(a); }
		public double exp(double a) { return Math.exp(a); }
		public double expm1(double a) { return Math.expm1(a); }
		public double floor(double a) { return Math.floor(a); }
		public int getExponent(double a) { return Math.getExponent(a); }
		// public int getExponent(float a) { return Math.getExponent(a); }
		public double hypot(double a, double b) { return Math.hypot(a, b); }
		public double IEEEremainder(double a, double b) { return Math.IEEEremainder(a, b); }
		public double log(double a) { return Math.log(a); }
		public double log10(double a) { return Math.log10(a); }
		public double log1p(double a) { return Math.log1p(a); }
		public double max(double a, double b) { return Math.max(a, b); }
		// public float max(float a, float b) { return Math.max(a, b); }
		// public int max(int a, int b) { return Math.max(a, b); }
		// public long max(long a, long b) { return Math.max(a, b); }
		public double min(double a, double b) { return Math.min(a, b); }
		// public float min(float a, float b) { return Math.min(a, b); }
		// public int min(int a, int b) { return Math.min(a, b); }
		// public long min(long a, long b) { return Math.min(a, b); }
		public double nextAfter(double a, double direction) { return Math.nextAfter(a, direction); }
		// public float nextAfter(float a, double direction) { return Math.nextAfter(a, direction); }
		public double nextUp(double a) { return Math.nextUp(a); }
		// public float nextUp(float a) { return Math.nextUp(a); }
		public double pow(double a, double b) { return Math.pow(a, b); }
		public double random() { return Math.random(); }
		public double rint(double a) { return Math.rint(a); }
		public long round(double a) { return Math.round(a); }
		// public int round(float a) { return Math.round(a); }
		public double scalb(double a, int scaleFactor) { return Math.scalb(a, scaleFactor); }
		// public float scalb(float a, int scaleFactor) { return Math.scalb(a, scaleFactor); }
		public double signum(double a) { return Math.signum(a); }
		// public float signum(float a) { return Math.signum(a); }
		public double sin(double a) { return Math.sin(a); }
		public double sinh(double a) { return Math.sinh(a); }
		public double sqrt(double a) { return Math.sqrt(a); }
		public double tan(double a) { return Math.tan(a); }
		public double tanh(double a) { return Math.tanh(a); }
		public double toDegrees(double a) { return Math.toDegrees(a); }
		public double toRadians(double a) { return Math.toRadians(a); }
		public double ulp(double a) { return Math.ulp(a); }
		// public float ulp(float a) { return Math.ulp(a); }
	}

	@Override
	public double evaluate(Map<String, Double> values) throws ThresholdExpressionException {
		// Add all of the variable values to the script context
		m_parser.getBindings(ScriptContext.ENGINE_SCOPE).putAll(values);
		m_parser.getBindings(ScriptContext.ENGINE_SCOPE).put("math", new MathBinding());
		double result = Double.NaN;
		try {
			// Evaluate the script expression
			result = (Double)m_parser.eval(m_expression.getExpression());
		} catch (Throwable e) {
			throw new ThresholdExpressionException("Error while evaluating expression "+m_expression.getExpression()+": " + e.getMessage(), e);
		}
		return result;
	}
}
