/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 22, 2006
 *
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
package org.opennms.web.svclayer.support;

import java.util.Date;

import org.opennms.netmgt.dao.DemandPollDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
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
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class DefaultDemandPollService implements DemandPollService {
	
	private PollerService m_pollerService;
	private DemandPollDao m_demandPollDao;
	private MonitoredServiceDao m_monitoredServiceDao;
	
	/**
	 * <p>setDemandPollDao</p>
	 *
	 * @param demandPollDao a {@link org.opennms.netmgt.dao.DemandPollDao} object.
	 */
	public void setDemandPollDao(DemandPollDao demandPollDao) {
		m_demandPollDao = demandPollDao;
	}
	
	/**
	 * <p>setPollerAPI</p>
	 *
	 * @param pollerAPI a {@link org.opennms.web.services.PollerService} object.
	 */
	public void setPollerAPI(PollerService pollerAPI) {
		m_pollerService = pollerAPI;
	}
	
	/**
	 * <p>setMonitoredServiceDao</p>
	 *
	 * @param monitoredServiceDao a {@link org.opennms.netmgt.dao.MonitoredServiceDao} object.
	 */
	public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
		m_monitoredServiceDao = monitoredServiceDao;
	}
	
	/** {@inheritDoc} */
	public DemandPoll pollMonitoredService(int nodeId, String ipAddr, int ifIndex, int serviceId) {
		DemandPoll demandPoll = new DemandPoll();
		demandPoll.setRequestTime(new Date());
		
		m_demandPollDao.save(demandPoll);
		
		OnmsMonitoredService monSvc = m_monitoredServiceDao.get(nodeId, ipAddr, ifIndex, serviceId);
		
		if (monSvc == null) {
			throw new RuntimeException("Service doesn't exist: "+monSvc);
		}
		m_pollerService.poll(monSvc, demandPoll.getId());
		return demandPoll;
	}

	/** {@inheritDoc} */
	public DemandPoll getUpdatedResults(int pollId) {
		return m_demandPollDao.get(pollId);
	}

}
