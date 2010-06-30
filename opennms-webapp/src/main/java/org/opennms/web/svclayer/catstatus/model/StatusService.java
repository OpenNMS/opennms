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

/**
 * <p>StatusService class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class StatusService {

	private String m_name;
	private Boolean m_outagestatus;
	private long m_outagetime;
	
	
	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	/**
	 * <p>setName</p>
	 *
	 * @param m_name a {@link java.lang.String} object.
	 */
	public void setName(String m_name) {
		this.m_name = m_name;
	}
	/**
	 * <p>getOutageStatus</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getOutageStatus() {
		return m_outagestatus;
	}
	/**
	 * <p>setOutageStatus</p>
	 *
	 * @param m_outagestatus a {@link java.lang.Boolean} object.
	 */
	public void setOutageStatus(Boolean m_outagestatus) {
		this.m_outagestatus = m_outagestatus;
	}
	/**
	 * <p>getOutageTime</p>
	 *
	 * @return a long.
	 */
	public long getOutageTime() {
		return m_outagetime;
	}
	/**
	 * <p>setOutageTime</p>
	 *
	 * @param m_outagetime a long.
	 */
	public void setOutageTime(long m_outagetime) {
		this.m_outagetime = m_outagetime;
	}
	
}
