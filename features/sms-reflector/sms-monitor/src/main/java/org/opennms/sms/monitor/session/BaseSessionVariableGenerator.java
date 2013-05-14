/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.monitor.session;

import java.util.Map;

/**
 * <p>BaseSessionVariableGenerator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BaseSessionVariableGenerator implements SessionVariableGenerator {
	private Map<String, String> m_parameters;
	
	/**
	 * <p>Constructor for BaseSessionVariableGenerator.</p>
	 */
	public BaseSessionVariableGenerator() {
		
	}

	/**
	 * <p>Constructor for BaseSessionVariableGenerator.</p>
	 *
	 * @param parameters a {@link java.util.Map} object.
	 */
	public BaseSessionVariableGenerator(Map<String, String> parameters) {
		setParameters(parameters);
	}

	/** {@inheritDoc} */
        @Override
	public void checkIn(String variable) {
		throw new UnsupportedOperationException("You must implement checkIn() in your class!");
	}

	/**
	 * <p>checkOut</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String checkOut() {
		throw new UnsupportedOperationException("You must implement checkOut() in your class!");
	}

	/**
	 * <p>getParameters</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<String, String> getParameters() {
		return m_parameters;
	}
	
	/** {@inheritDoc} */
        @Override
	public void setParameters(Map<String, String> parameters) {
		m_parameters = parameters;
	}
}
