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
