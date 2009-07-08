/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.poller.remote;

import java.util.Collection;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
class DemoPollService implements PollService {
	
	private int m_repetitions;
	private int m_pollCount;
	private PollStatus m_currentStatus;

	public DemoPollService(int repetitions, PollStatus initialStatus) {
		m_repetitions = repetitions;
		m_currentStatus = initialStatus;
	}
	
	public DemoPollService(int repetitions) {
		this(repetitions, PollStatus.up());
	}
	
	public DemoPollService() {
		this(2);
	}

	public PollStatus poll(PolledService polledService) {
        PollStatus status = m_currentStatus;
        
        m_pollCount++;
        if (m_pollCount % m_repetitions == 0) {
        	m_currentStatus = (m_currentStatus.isDown() ? PollStatus.up(100.0+m_pollCount) : PollStatus.down("pollCount is "+m_pollCount));
        }
        
        return status;
    }

    public void initialize(PolledService polledService) {
        // TODO Auto-generated method stub
        
    }

    public void release(PolledService polledService) {
        // TODO Auto-generated method stub
        
    }

    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators) {
        // TODO Auto-generated method stub
        
    }
	
}