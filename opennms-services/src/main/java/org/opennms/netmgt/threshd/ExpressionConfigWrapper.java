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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import org.opennms.netmgt.config.threshd.Expression;

/**
 * 
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public class ExpressionConfigWrapper extends BaseThresholdDefConfigWrapper {

	private static class BindingsSniffer extends TreeMap<String,Object> implements Bindings {

		private final Set<String> m_sniffedKeys = new HashSet<String>();

		@Override
		public Object get(Object key) {
			System.out.println(new Date().toString() + " Bindings.get(" + key + ")");
			m_sniffedKeys.add((String)key);
			return super.get(key);
		}

		@Override
		public boolean containsKey(Object key) {
			System.out.println(new Date().toString() + " Bindings.containsKey(" + key + ")");
			m_sniffedKeys.add((String)key);
			return super.containsKey(key);
		}

		public Set<String> getSniffedKeys() {
			System.out.println(new Date().toString() + " Bindings.getSniffedKeys()");
			return Collections.unmodifiableSet(m_sniffedKeys);
		}
	}

	/*
	private static class DelegateScriptContext extends SimpleScriptContext implements ScriptContext {

		private final Bindings m_bindingsSniffer = new BindingsSniffer();
		private final Set<String> m_sniffedAttributes = new HashSet<String>();

		public Object getAttribute(String name) {
			System.out.println(new Date().toString() + " ScriptContext.getAttribute(" + name + ")");
			m_sniffedAttributes.add(name);
			return m_bindingsSniffer.get(name);
		}

		public Object getAttribute(String name, int scope) {
			System.out.println(new Date().toString() + " ScriptContext.getAttribute(" + name + ")");
			m_sniffedAttributes.add(name);
			return m_bindingsSniffer.get(name);
		}

		public int getAttributesScope(String name) {
			throw new UnsupportedOperationException("ScriptContext.getAttributesScope()");
		}

		public Bindings getBindings(int scope) {
			return m_bindingsSniffer;
		}

		public List<Integer> getScopes() {
			throw new UnsupportedOperationException("ScriptContext.getAttributesScope()");
		}

		public Object removeAttribute(String name, int scope) {
			System.out.println(new Date().toString() + " ScriptContext.removeAttribute(" + name + ")");
			m_sniffedAttributes.add(name);
			return m_bindingsSniffer.remove(name);
		}

		public void setAttribute(String name, Object value, int scope) {
			System.out.println(new Date().toString() + " ScriptContext.setAttribute(" + name + ")");
			m_sniffedAttributes.add(name);
			m_bindingsSniffer.put(name, value);
		}

		public void setBindings(Bindings bindings, int scope) {
			throw new UnsupportedOperationException("ScriptContext.getAttributesScope()");
		}

		public Set<String> getSniffedKeys() {
			System.out.println(new Date().toString() + " ScriptContext.getSniffedKeys()");
			return Collections.unmodifiableSet(m_sniffedAttributes);
		}
	}
	*/

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
		// DelegateScriptContext context = new DelegateScriptContext();

		// Test parsing of the expression and collect the variable names by inserting
		// a ScriptContext that sniffs calls to getAttribute()
		try {
			// ScriptContext context = m_parser.getContext();
			// context.setBindings(sniffer, ScriptContext.ENGINE_SCOPE);
			// context.setBindings(sniffer, ScriptContext.GLOBAL_SCOPE);
			// m_parser.setBindings(sniffer, ScriptContext.ENGINE_SCOPE);
			// m_parser.setBindings(sniffer, ScriptContext.GLOBAL_SCOPE);

			// m_parser.setContext(context);
			m_parser.eval(m_expression.getExpression(), sniffer);
		} catch (Throwable e) {
			throw new ThresholdExpressionException("Could not parse threshold expression:" + e.getMessage());
		}

		//m_datasources.addAll(m_parser.getBindings(ScriptContext.ENGINE_SCOPE).keySet());
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
		m_parser.getBindings(ScriptContext.ENGINE_SCOPE).putAll(values);
		double result = Double.NaN;
		try {
			result = (Double)m_parser.eval(m_expression.getExpression());
		} catch (Throwable e) {
			throw new ThresholdExpressionException("Error while evaluating expression "+m_expression.getExpression()+": " + e.getMessage());
		}
		return result;
	}
}
