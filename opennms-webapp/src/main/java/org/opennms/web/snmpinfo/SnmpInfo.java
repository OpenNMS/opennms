/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.snmpinfo;

import java.net.UnknownHostException;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

@XmlRootElement(name="snmp-info")
public class SnmpInfo {

    private String m_community;
    private String m_version;
    private int m_port;
    private int m_retries;
    private int m_timeout;
    
    public SnmpInfo() {
        
    }

    /**
     * @param config
     */
    public SnmpInfo(SnmpAgentConfig config) {
        m_community = config.getReadCommunity();
        m_port = config.getPort();
        m_timeout = config.getTimeout();
        m_retries = config.getRetries();
        m_version = config.getVersionAsString();
    }

    /**
     * @return the community
     */
    public String getCommunity() {
        return m_community;
    }

    /**
     * @param community the community to set
     */
    public void setCommunity(String community) {
        m_community = community;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return m_version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        m_version = version;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return m_port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * @return the retries
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * @param retries the retries to set
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    /**
     * @return
     */
    public SnmpEventInfo createEventInfo(String ipAddr) throws UnknownHostException {
        SnmpEventInfo eventInfo = new SnmpEventInfo();
        eventInfo.setCommunityString(m_community);
        eventInfo.setVersion(m_version);
        eventInfo.setPort(m_port);
        eventInfo.setTimeout(m_timeout);
        eventInfo.setRetryCount(m_retries);
        eventInfo.setFirstIPAddress(ipAddr);
        return eventInfo;
    }
    
    
}

