/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.web.enlinkd;

public class LldpElementNode {

	private String m_lldpChassisIdString;
	private String m_lldpSysName;
	private String m_lldpCreateTime;
	private String m_lldpLastPollTime;
	
	
	public String getLldpChassisIdString() {
		return m_lldpChassisIdString;
	}
	public void setLldpChassisIdString(String lldpSysIdString) {
		m_lldpChassisIdString = lldpSysIdString;
	}
	public String getLldpSysName() {
		return m_lldpSysName;
	}
	public void setLldpSysName(String lldpSysName) {
		m_lldpSysName = lldpSysName;
	}
	public String getLldpCreateTime() {
		return m_lldpCreateTime;
	}
	public void setLldpCreateTime(String lldpCreateTime) {
		m_lldpCreateTime = lldpCreateTime;
	}
	public String getLldpLastPollTime() {
		return m_lldpLastPollTime;
	}
	public void setLldpLastPollTime(String lldpLastPollTime) {
		m_lldpLastPollTime = lldpLastPollTime;
	}
	
}
