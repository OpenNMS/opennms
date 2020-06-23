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

package org.opennms.netmgt.poller.remote;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.Collections;
import java.util.Map;

import org.opennms.netmgt.model.OnmsMonitoredService;

/**
 * <p>PollConfiguration class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PollConfiguration {
	
	private OnmsMonitoredService m_monitoredService;
	private OnmsPollModel m_pollModel;
	private Map<String,Object> m_monitorConfiguration;
	
	/**
	 * <p>Constructor for PollConfiguration.</p>
	 *
	 * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 * @param monitorConfiguration a {@link java.util.Map} object.
	 * @param pollInterval a long.
	 */
	public PollConfiguration(OnmsMonitoredService monitoredService, Map<String,Object> monitorConfiguration, long pollInterval) {
		m_monitoredService = monitoredService;
		m_monitorConfiguration = monitorConfiguration;
		m_pollModel = new OnmsPollModel();
		m_pollModel.setPollInterval(pollInterval);
	}

	/**
	 * <p>getMonitoredService</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 */
	public OnmsMonitoredService getMonitoredService() {
		return m_monitoredService;
	}

	/**
	 * <p>setMonitoredService</p>
	 *
	 * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 */
	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}
	
	/**
	 * <p>getMonitorConfiguration</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,Object> getMonitorConfiguration() {
		return Collections.unmodifiableMap(m_monitorConfiguration);
	}

	/**
	 * <p>getPollModel</p>
	 *
	 * @return a {@link org.opennms.netmgt.poller.remote.OnmsPollModel} object.
	 */
	public OnmsPollModel getPollModel() {
		return m_pollModel;
	}

	/**
	 * <p>setPollModel</p>
	 *
	 * @param pollModel a {@link org.opennms.netmgt.poller.remote.OnmsPollModel} object.
	 */
	public void setPollModel(OnmsPollModel pollModel) {
		m_pollModel = pollModel;
	}

	/**
	 * <p>getId</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() {
		return m_monitoredService.getNodeId()+":"+str(m_monitoredService.getIpAddress())+":"+m_monitoredService.getServiceName();
	}

}
