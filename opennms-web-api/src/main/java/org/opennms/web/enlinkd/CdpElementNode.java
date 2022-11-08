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

public class CdpElementNode {

    private String m_cdpGlobalRun;
    private String m_cdpGlobalDeviceId;
    private String m_cdpGlobalDeviceIdFormat;
    private String m_cdpCreateTime;
    private String m_cdpLastPollTime;

    public String getCdpCreateTime() {
        return m_cdpCreateTime;
    }

    public void setCdpCreateTime(String cdpCreateTime) {
        m_cdpCreateTime = cdpCreateTime;
    }

    public String getCdpLastPollTime() {
        return m_cdpLastPollTime;
    }

    public void setCdpLastPollTime(String cdpLastPollTime) {
        m_cdpLastPollTime = cdpLastPollTime;
    }

    public String getCdpGlobalRun() {
        return m_cdpGlobalRun;
    }

    public void setCdpGlobalRun(String cdpGlobalRun) {
        m_cdpGlobalRun = cdpGlobalRun;
    }

    public String getCdpGlobalDeviceId() {
        return m_cdpGlobalDeviceId;
    }

    public void setCdpGlobalDeviceId(String cdpGlobalDeviceId) {
        m_cdpGlobalDeviceId = cdpGlobalDeviceId;
    }

    public String getCdpGlobalDeviceIdFormat() {
        return m_cdpGlobalDeviceIdFormat;
    }

    public void setCdpGlobalDeviceIdFormat(String cdpGlobalDeviceIdFormat) {
        m_cdpGlobalDeviceIdFormat = cdpGlobalDeviceIdFormat;
    }

}
