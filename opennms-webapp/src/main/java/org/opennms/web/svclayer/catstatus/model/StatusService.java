/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.catstatus.model;

/**
 * <p>StatusService class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
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
