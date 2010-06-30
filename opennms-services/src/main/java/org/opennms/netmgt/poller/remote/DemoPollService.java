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
 * Created: August 17, 2006
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

	/**
	 * <p>Constructor for DemoPollService.</p>
	 *
	 * @param repetitions a int.
	 * @param initialStatus a {@link org.opennms.netmgt.model.PollStatus} object.
	 */
	public DemoPollService(int repetitions, PollStatus initialStatus) {
		m_repetitions = repetitions;
		m_currentStatus = initialStatus;
	}
	
	/**
	 * <p>Constructor for DemoPollService.</p>
	 *
	 * @param repetitions a int.
	 */
	public DemoPollService(int repetitions) {
		this(repetitions, PollStatus.up());
	}
	
	/**
	 * <p>Constructor for DemoPollService.</p>
	 */
	public DemoPollService() {
		this(2);
	}

	/** {@inheritDoc} */
	public PollStatus poll(PolledService polledService) {
        PollStatus status = m_currentStatus;
        
        m_pollCount++;
        if (m_pollCount % m_repetitions == 0) {
        	m_currentStatus = (m_currentStatus.isDown() ? PollStatus.up(100.0+m_pollCount) : PollStatus.down("pollCount is "+m_pollCount));
        }
        
        return status;
    }

    /** {@inheritDoc} */
    public void initialize(PolledService polledService) {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void release(PolledService polledService) {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators) {
        // TODO Auto-generated method stub
        
    }
	
}
