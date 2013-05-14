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
        @Override
	public PollStatus poll(PolledService polledService) {
        PollStatus status = m_currentStatus;
        
        m_pollCount++;
        if (m_pollCount % m_repetitions == 0) {
        	m_currentStatus = (m_currentStatus.isDown() ? PollStatus.up(100.0+m_pollCount) : PollStatus.down("pollCount is "+m_pollCount));
        }
        
        return status;
    }

    /** {@inheritDoc} */
        @Override
    public void initialize(PolledService polledService) {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
        @Override
    public void release(PolledService polledService) {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
        @Override
    public void setServiceMonitorLocators(Collection<ServiceMonitorLocator> locators) {
        // TODO Auto-generated method stub
        
    }
	
}
