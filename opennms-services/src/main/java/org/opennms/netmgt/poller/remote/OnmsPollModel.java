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

import java.io.Serializable;
import java.util.Date;


/**
 * <p>OnmsPollModel class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class OnmsPollModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private long m_pollInterval;
    
    /**
     * <p>Constructor for OnmsPollModel.</p>
     */
    public OnmsPollModel() {
        m_pollInterval = -1L;
    }
    
    /**
     * <p>Constructor for OnmsPollModel.</p>
     *
     * @param pollInterval a long.
     */
    public OnmsPollModel(long pollInterval) {
        m_pollInterval = pollInterval;
    }
	
	/**
	 * <p>getPollInterval</p>
	 *
	 * @return a long.
	 */
	public long getPollInterval() {
		return m_pollInterval;
	}

	/**
	 * <p>setPollInterval</p>
	 *
	 * @param pollInterval a long.
	 */
	public void setPollInterval(long pollInterval) {
		m_pollInterval = pollInterval;
	}

    /**
     * <p>getNextPollTime</p>
     *
     * @param lastPollTime a {@link java.util.Date} object.
     * @return a {@link java.util.Date} object.
     */
    public Date getNextPollTime(Date lastPollTime) {
        return new Date(lastPollTime.getTime()+m_pollInterval);
    }

    /**
     * <p>getPreviousPollTime</p>
     *
     * @param initialPollTime a {@link java.util.Date} object.
     * @return a {@link java.util.Date} object.
     */
    public Date getPreviousPollTime(Date initialPollTime) {
        return new Date(initialPollTime.getTime()-m_pollInterval);
    }
	
	
	
}
