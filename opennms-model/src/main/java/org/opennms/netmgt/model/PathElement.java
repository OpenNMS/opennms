/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Embeddable
public class PathElement implements Serializable {
    private static final long serialVersionUID = 2437052160139305371L;
    
    private String m_ipAddress;
	private String m_serviceName;
	
	public PathElement() {
		
	}

	public PathElement(String ipAddress, String serviceName) {
		m_ipAddress = ipAddress;
		m_serviceName = serviceName;
	}

	@Column(name="criticalPathIp")
	public String getIpAddress() {
		return m_ipAddress;
	}
	
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}
	
	@Column(name="criticalPathServiceName")
	public String getServiceName() {
		return m_serviceName;
	}
	
	public void setServiceName(String serviceName) {
		m_serviceName = serviceName;
	}


}
