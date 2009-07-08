/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.util.Map;

import org.opennms.core.utils.ParameterMap;

public class LatencyParameters {

	private Map m_parameters;
	private String m_svcName;

	public LatencyParameters(Map parameters, String svcName) {

		m_parameters = parameters;
		m_svcName = svcName;
	}

	public Map getParameters() {
		return m_parameters;
	}
	

	public String getServiceName() {
		return m_svcName;
	}

	int getInterval() {
		Map parameters = getParameters();
	    int interval = ParameterMap.getKeyedInteger(parameters, "interval", LatencyThresholder.DEFAULT_INTERVAL);
	    return interval;
	}

	String getGroupName() {
		Map parameters = getParameters();
	    String groupName = ParameterMap.getKeyedString(parameters, "thresholding-group", "default");
	    return groupName;
	}
	
	int getRange() {
		Map parameters = getParameters();
	    int range = ParameterMap.getKeyedInteger(parameters, "range", LatencyThresholder.DEFAULT_RANGE);
	    return range;
	}
	

}
