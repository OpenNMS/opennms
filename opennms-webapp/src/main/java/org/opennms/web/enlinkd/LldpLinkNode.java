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

	private String   m_lldpPortString;
	private String   m_lldpPortDescr;
	private String   m_lldpPortUrl;
	private String   m_lldpRemChassisIdString;
	private String   m_lldpRemSysName;
	private String   m_lldpRemChassisIdUrl;
	private String   m_lldpRemPortString;
	private String   m_lldpRemPortDescr;
	private String   m_lldpRemPortUrl;
	private String   m_lldpCreateTime;
	private String   m_lldpLastPollTime;

	public String getLldpPortString() {
		return m_lldpPortString;
	}
	public void setLldpPortString(String lldpPortString) {
		m_lldpPortString = lldpPortString;
	}
	public String getLldpPortDescr() {
		return m_lldpPortDescr;
	}
	public void setLldpPortDescr(String lldpPortDescr) {
		m_lldpPortDescr = lldpPortDescr;
	}
	public String getLldpPortUrl() {
		return m_lldpPortUrl;
	}
	public void setLldpPortUrl(String lldpPortUrl) {
		m_lldpPortUrl = lldpPortUrl;
	}
	public String getLldpRemChassisIdString() {
		return m_lldpRemChassisIdString;
	}
	public void setLldpRemChassisIdString(String lldpRemChassisIdString) {
		m_lldpRemChassisIdString = lldpRemChassisIdString;
	}
	public String getLldpRemSysName() {
		return m_lldpRemSysName;
	}
	public void setLldpRemSysName(String lldpRemSysName) {
		m_lldpRemSysName = lldpRemSysName;
	}
	public String getLldpRemChassisIdUrl() {
		return m_lldpRemChassisIdUrl;
	}
	public void setLldpRemChassisIdUrl(String lldpRemChassisIdUrl) {
		m_lldpRemChassisIdUrl = lldpRemChassisIdUrl;
	}
	public String getLldpRemPortString() {
		return m_lldpRemPortString;
	}
	public void setLldpRemPortString(String lldpRemPortString) {
		m_lldpRemPortString = lldpRemPortString;
	}
	public String getLldpRemPortDescr() {
		return m_lldpRemPortDescr;
	}
	public void setLldpRemPortDescr(String lldpRemPortDescr) {
		m_lldpRemPortDescr = lldpRemPortDescr;
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
        return m_lldpPortString.compareTo(o.m_lldpPortString);
    }
	
}
