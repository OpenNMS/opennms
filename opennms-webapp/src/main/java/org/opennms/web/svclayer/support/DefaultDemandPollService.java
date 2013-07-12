/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.dao.api.DemandPollDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.services.PollerService;
import org.opennms.web.svclayer.DemandPollService;

/**
 * <p>DefaultDemandPollService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultDemandPollService implements DemandPollService {
	
	private PollerService m_pollerService;
	private DemandPollDao m_demandPollDao;
	private MonitoredServiceDao m_monitoredServiceDao;
	
	/**
	 * <p>setDemandPollDao</p>
	 *
	 * @param demandPollDao a {@link org.opennms.netmgt.dao.api.DemandPollDao} object.
	 */
	public void setDemandPollDao(final DemandPollDao demandPollDao) {
		m_demandPollDao = demandPollDao;
	}
	
	/**
	 * <p>setPollerAPI</p>
	 *
	 * @param pollerAPI a {@link org.opennms.web.services.PollerService} object.
	 */
	public void setPollerAPI(final PollerService pollerAPI) {
		m_pollerService = pollerAPI;
	}
	
	/**
	 * <p>setMonitoredServiceDao</p>
	 *
	 * @param monitoredServiceDao a {@link org.opennms.netmgt.dao.api.MonitoredServiceDao} object.
	 */
	public void setMonitoredServiceDao(final MonitoredServiceDao monitoredServiceDao) {
		m_monitoredServiceDao = monitoredServiceDao;
	}

        @Override
	public DemandPoll pollMonitoredService(final int nodeId, final InetAddress ipAddr, final int ifIndex, final int serviceId) {
	    final DemandPoll demandPoll = new DemandPoll();
		demandPoll.setRequestTime(new Date());
		
		m_demandPollDao.save(demandPoll);
		
		final OnmsMonitoredService monSvc = m_monitoredServiceDao.get(nodeId, ipAddr, ifIndex, serviceId);
		
		if (monSvc == null) {
			throw new RuntimeException("Service doesn't exist: "+monSvc);
		}
		m_pollerService.poll(monSvc, demandPoll.getId());
		return demandPoll;
	}

	/** {@inheritDoc} */
        @Override
	public DemandPoll getUpdatedResults(final int pollId) {
		return m_demandPollDao.get(pollId);
	}

}
