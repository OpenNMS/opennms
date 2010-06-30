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
 * Created: July 27, 2006
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
package org.opennms.web.svclayer.catstatus.model;

import java.util.Collection;
import java.util.ArrayList;

/**
 * <p>StatusInterface class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusInterface {
	
	private String m_ipaddress;
	private String m_interfacename;
	private Collection<StatusService> m_services;
	
	/**
	 * <p>Constructor for StatusInterface.</p>
	 */
	public StatusInterface(){
		
		m_services = new ArrayList<StatusService>();
		
	}
	
	/**
	 * <p>addService</p>
	 *
	 * @param service a {@link org.opennms.web.svclayer.catstatus.model.StatusService} object.
	 */
	public void addService ( StatusService service ) {	
		m_services.add(service);
	}
	
	/**
	 * <p>getServices</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusService> getServices(){
		return m_services;
	}
	
	/**
	 * <p>getInterfacename</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getInterfacename() {
		return m_interfacename;
	}
	
	/**
	 * <p>setInterfacename</p>
	 *
	 * @param m_interfacename a {@link java.lang.String} object.
	 */
	public void setInterfacename(String m_interfacename) {
		this.m_interfacename = m_interfacename;
	}
	
	/**
	 * <p>getIpAddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getIpAddress() {
		return m_ipaddress;
	}
	
	/**
	 * <p>setIpAddress</p>
	 *
	 * @param m_ipaddress a {@link java.lang.String} object.
	 */
	public void setIpAddress(String m_ipaddress) {
		this.m_ipaddress = m_ipaddress;
	}
	
	

}
