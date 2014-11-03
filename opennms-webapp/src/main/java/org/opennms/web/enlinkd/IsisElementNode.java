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

public class IsisElementNode {

    private String m_isisSysID;
    private String m_isisSysAdminState;
	private String m_isisCreateTime;
	private String m_isisLastPollTime;
	
	public String getIsisSysID() {
		return m_isisSysID;
	}
	public void setIsisSysID(String isisSysID) {
		m_isisSysID = isisSysID;
	}
	public String getIsisSysAdminState() {
		return m_isisSysAdminState;
	}
	public void setIsisSysAdminState(String isisSysAdminState) {
		m_isisSysAdminState = isisSysAdminState;
	}
	public String getIsisCreateTime() {
		return m_isisCreateTime;
	}
	public void setIsisCreateTime(String isisCreateTime) {
		m_isisCreateTime = isisCreateTime;
	}
	public String getIsisLastPollTime() {
		return m_isisLastPollTime;
	}
	public void setIsisLastPollTime(String isisLastPollTime) {
		m_isisLastPollTime = isisLastPollTime;
	}

}
