/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * <p>PathElement class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
@Embeddable
public class PathElement implements Serializable {
    private static final long serialVersionUID = 2437052160139305371L;
    
    private String m_ipAddress;
	private String m_serviceName;
	
	/**
	 * <p>Constructor for PathElement.</p>
	 */
	public PathElement() {
		
	}

	/**
	 * <p>Constructor for PathElement.</p>
	 *
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param serviceName a {@link java.lang.String} object.
	 */
	public PathElement(String ipAddress, String serviceName) {
		m_ipAddress = ipAddress;
		m_serviceName = serviceName;
	}

	/**
	 * <p>getIpAddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="criticalPathIp")
	public String getIpAddress() {
		return m_ipAddress;
	}
	
	/**
	 * <p>setIpAddress</p>
	 *
	 * @param ipAddress a {@link java.lang.String} object.
	 */
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}
	
	/**
	 * <p>getServiceName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="criticalPathServiceName")
	public String getServiceName() {
		return m_serviceName;
	}
	
	/**
	 * <p>setServiceName</p>
	 *
	 * @param serviceName a {@link java.lang.String} object.
	 */
	public void setServiceName(String serviceName) {
		m_serviceName = serviceName;
	}


}
