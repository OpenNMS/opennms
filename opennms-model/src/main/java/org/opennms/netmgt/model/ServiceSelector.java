/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.util.List;

/**
 * <p>ServiceSelector class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jason.aras@fastsearch.com">Jason Ayers</a>
 */
public class ServiceSelector {
	private String m_filterRule;
	private List<String> m_serviceNames;
	
	
	/**
	 * <p>Constructor for ServiceSelector.</p>
	 *
	 * @param filterRule a {@link java.lang.String} object.
	 * @param serviceNames a {@link java.util.List} object.
	 */
	public ServiceSelector(String filterRule, List<String> serviceNames) {
		m_filterRule = filterRule;
		m_serviceNames = serviceNames;
	}
	
	/**
	 * <p>getFilterRule</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilterRule() {
		return m_filterRule;
	}
	
	/**
	 * <p>getServiceNames</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getServiceNames() {
		return m_serviceNames;
	}
}
