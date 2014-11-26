/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

import java.util.Collections;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;

/**
 * <p>LatencyParameters class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class LatencyParameters {

    /**
     * Default thresholding interval (in milliseconds).
     */
    static final int DEFAULT_INTERVAL = 300000; // 300s or 5m

    /**
     * Default age before which a data point is considered "out of date"
     */
    static final int DEFAULT_RANGE = 0;

	private Map<?,?> m_parameters;
	private String m_svcName;

	/**
	 * <p>Constructor for LatencyParameters.</p>
	 *
	 * @param parameters a {@link java.util.Map} object.
	 * @param svcName a {@link java.lang.String} object.
	 */
	public LatencyParameters(Map<?,?> parameters, String svcName) {

		m_parameters = parameters;
		m_svcName = svcName;
	}

	/**
	 * <p>getParameters</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<?,?> getParameters() {
		return Collections.unmodifiableMap(m_parameters);
	}


	/**
	 * <p>getServiceName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getServiceName() {
		return m_svcName;
	}

	int getInterval() {
		Map<?,?> parameters = getParameters();
		return ParameterMap.getKeyedInteger(parameters, "interval", DEFAULT_INTERVAL);
	}

	String getGroupName() {
		Map<?,?> parameters = getParameters();
		return ParameterMap.getKeyedString(parameters, "thresholding-group", "default");
	}

	int getRange() {
		Map<?,?> parameters = getParameters();
		return ParameterMap.getKeyedInteger(parameters, "range", DEFAULT_RANGE);
	}
}
