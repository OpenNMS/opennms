/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 16, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.poller.remote;

import java.util.Map;

import org.opennms.netmgt.model.OnmsMonitoredService;

/**
 * <p>PollConfiguration class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PollConfiguration {
	
	private OnmsMonitoredService m_monitoredService;
	private OnmsPollModel m_pollModel;
	private Map m_monitorConfiguration;
	
	/**
	 * <p>Constructor for PollConfiguration.</p>
	 *
	 * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 * @param monitorConfiguration a {@link java.util.Map} object.
	 * @param pollInterval a long.
	 */
	public PollConfiguration(OnmsMonitoredService monitoredService, Map monitorConfiguration, long pollInterval) {
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
	public Map getMonitorConfiguration() {
		return m_monitorConfiguration;
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
		return m_monitoredService.getNodeId()+":"+m_monitoredService.getIpAddress()+":"+m_monitoredService.getServiceName();
	}

}
