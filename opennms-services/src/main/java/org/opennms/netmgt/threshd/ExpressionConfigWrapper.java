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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

		private final Set<String> m_sniffedKeys = new HashSet<String>();

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

		// Test parsing of the expression and collect the variable names by using
		// a Bindings instance that sniffs all of the variable names
		try {
			m_parser.eval(m_expression.getExpression(), sniffer);
		} catch (Throwable e) {
			throw new ThresholdExpressionException("Could not parse threshold expression:" + e.getMessage());
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

	@Override
	public double evaluate(Map<String, Double> values) throws ThresholdExpressionException {
		// Add all of the variable values to the script context
		m_parser.getBindings(ScriptContext.ENGINE_SCOPE).putAll(values);
		double result = Double.NaN;
		try {
			// Evaluate the script expression
			result = (Double)m_parser.eval(m_expression.getExpression());
		} catch (Throwable e) {
			throw new ThresholdExpressionException("Error while evaluating expression "+m_expression.getExpression()+": " + e.getMessage());
		}
		return result;
	}
}
