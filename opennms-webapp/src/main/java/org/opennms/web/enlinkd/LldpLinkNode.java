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

public class LldpLinkNode implements Comparable<LldpLinkNode> {

	private String   m_lldpLocalPort;
	private String   m_lldpLocalPortUrl;
	private String   m_lldpRemChassisId;
        private String   m_lldpRemChassisIdUrl;
	private String   m_lldpRemInfo;
	private String   m_lldpRemPort;
	private String   m_lldpRemPortUrl;
	private String   m_lldpCreateTime;
	private String   m_lldpLastPollTime;

	
	public String getLldpLocalPort() {
        return m_lldpLocalPort;
    }
    public void setLldpLocalPort(String lldpLocalPort) {
        m_lldpLocalPort = lldpLocalPort;
    }
    public String getLldpLocalPortUrl() {
        return m_lldpLocalPortUrl;
    }
    public void setLldpLocalPortUrl(String lldpLocalPortUrl) {
        m_lldpLocalPortUrl = lldpLocalPortUrl;
    }
    public String getLldpRemChassisId() {
        return m_lldpRemChassisId;
    }
    public void setLldpRemChassisId(String lldpRemChassisId) {
        m_lldpRemChassisId = lldpRemChassisId;
    }
    public String getLldpRemChassisIdUrl() {
        return m_lldpRemChassisIdUrl;
    }
    public void setLldpRemChassisIdUrl(String lldpRemChassisIdUrl) {
        m_lldpRemChassisIdUrl = lldpRemChassisIdUrl;
    }
    public String getLldpRemInfo() {
        return m_lldpRemInfo;
    }
    public void setLldpRemInfo(String lldpRemInfo) {
        m_lldpRemInfo = lldpRemInfo;
    }
    public String getLldpRemPort() {
        return m_lldpRemPort;
    }
    public void setLldpRemPort(String lldpRemPort) {
        m_lldpRemPort = lldpRemPort;
    }
    public String getLldpRemPortUrl() {
        return m_lldpRemPortUrl;
    }
    public void setLldpRemPortUrl(String lldpRemPortUrl) {
        m_lldpRemPortUrl = lldpRemPortUrl;
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
    @Override
    public int compareTo(LldpLinkNode o) {
        return m_lldpLocalPort.compareTo(o.m_lldpLocalPort);
    }
	
}
