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

public class OspfLinkNode implements Comparable<OspfLinkNode>{

    private String m_ospfLocalPort;
    private String m_ospfLocalPortUrl;

    private String m_ospfRemRouterId;
    private String m_ospfRemRouterUrl;

    private String m_ospfRemPort;
    private String m_ospfRemPortUrl;
    
    private String m_ospfLinkInfo;

    private String m_ospfLinkCreateTime;
    private String m_ospfLinkLastPollTime;

    public String getOspfLocalPort() {
        return m_ospfLocalPort;
    }

    public void setOspfLocalPort(String ospfLocalPort) {
        m_ospfLocalPort = ospfLocalPort;
    }

    public String getOspfLocalPortUrl() {
        return m_ospfLocalPortUrl;
    }

    public void setOspfLocalPortUrl(String ospfLocalPortUrl) {
        m_ospfLocalPortUrl = ospfLocalPortUrl;
    }

    public String getOspfRemRouterId() {
        return m_ospfRemRouterId;
    }

    public void setOspfRemRouterId(String ospfRemRouterId) {
        m_ospfRemRouterId = ospfRemRouterId;
    }

    public String getOspfRemRouterUrl() {
        return m_ospfRemRouterUrl;
    }

    public void setOspfRemRouterUrl(String ospfRemRouterUrl) {
        m_ospfRemRouterUrl = ospfRemRouterUrl;
    }

    public String getOspfRemPort() {
        return m_ospfRemPort;
    }

    public void setOspfRemPort(String ospfRemPort) {
        m_ospfRemPort = ospfRemPort;
    }

    public String getOspfRemPortUrl() {
        return m_ospfRemPortUrl;
    }

    public void setOspfRemPortUrl(String ospfRemPortUrl) {
        m_ospfRemPortUrl = ospfRemPortUrl;
    }

    public String getOspfLinkInfo() {
        return m_ospfLinkInfo;
    }

    public void setOspfLinkInfo(String ospfLinkInfo) {
        m_ospfLinkInfo = ospfLinkInfo;
    }

    public String getOspfLinkCreateTime() {
        return m_ospfLinkCreateTime;
    }

    public void setOspfLinkCreateTime(String ospfLinkCreateTime) {
        m_ospfLinkCreateTime = ospfLinkCreateTime;
    }

    public String getOspfLinkLastPollTime() {
        return m_ospfLinkLastPollTime;
    }

    public void setOspfLinkLastPollTime(String ospfLinkLastPollTime) {
        m_ospfLinkLastPollTime = ospfLinkLastPollTime;
    }

    @Override
    public int compareTo(OspfLinkNode o) {
        return getOspfLocalPort().compareTo(o.getOspfLocalPort());
    }

}
