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
package org.opennms.netmgt.measurements.impl;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Functions to use JexlEngine to evaluate a given formula string against the current context
 * @author cgallen
 *
 */
public class JexlEvaluateFunctions {
	private static final Logger LOG = LoggerFactory.getLogger(JexlEvaluateFunctions.class);

	private JexlEngine m_jexl ;

	private JexlContext m_context =null;

	public JexlEvaluateFunctions(JexlContext context, JexlEngine jexl){
		m_context = Preconditions.checkNotNull(context, "JexlContext context");
		m_jexl = Preconditions.checkNotNull(jexl, "JexlEngine jexl");
	}

	/**
	 * Uses the JexlEngine to evaluate a string constant as a Jexl expression
	 * @param formula string expression to evaluate
	 * @return double value or NaN if formula cannot be evaluated
	 */
	public Double evaluate(String formula){
		try {
			return (Double) m_jexl.createExpression(formula).evaluate(m_context);
		} catch (Exception ex){
			LOG.error("jexl:evaluate problem evaluating string formula '"+formula+"'",ex);
			return Double.NaN;
		}
	}

}
