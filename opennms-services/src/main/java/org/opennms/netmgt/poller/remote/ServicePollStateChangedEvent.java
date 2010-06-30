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

import java.util.EventObject;

/**
 * <p>ServicePollStateChangedEvent class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class ServicePollStateChangedEvent extends EventObject {
	
	private static final long serialVersionUID = 5224040562319082465L;

	private int m_index;

	/**
	 * <p>Constructor for ServicePollStateChangedEvent.</p>
	 *
	 * @param polledService a {@link org.opennms.netmgt.poller.remote.PolledService} object.
	 * @param index a int.
	 */
	public ServicePollStateChangedEvent(PolledService polledService, int index) {
		super(polledService);
		m_index = index;
	}
	
	/**
	 * <p>getPolledService</p>
	 *
	 * @return a {@link org.opennms.netmgt.poller.remote.PolledService} object.
	 */
	public PolledService getPolledService() {
		return (PolledService)getSource();
	}
    
	
	/**
	 * <p>getIndex</p>
	 *
	 * @return a int.
	 */
	public int getIndex() {
		return m_index;
	}

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getName() + 
        "[" +
        "source=" + getSource() +
        ", index=" + m_index +
        "]";
    }
    
    

}
