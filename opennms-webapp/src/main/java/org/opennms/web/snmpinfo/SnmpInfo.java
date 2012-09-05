/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.snmpinfo;

import java.net.UnknownHostException;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * <p>SnmpInfo class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@XmlRootElement(name="snmp-info")
public class SnmpInfo {

    private String m_community;
    private String m_version;
    private int m_port;
    private int m_retries;
    private int m_timeout;
    
    /**
     * <p>Constructor for SnmpInfo.</p>
     */
    public SnmpInfo() {
        
    }

    /**
     * <p>Constructor for SnmpInfo.</p>
     *
     * @param config a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public SnmpInfo(SnmpAgentConfig config) {
        m_community = config.getReadCommunity();
        m_port = config.getPort();
        m_timeout = config.getTimeout();
        m_retries = config.getRetries();
        m_version = config.getVersionAsString();
    }

    /**
     * <p>getCommunity</p>
     *
     * @return the community
     */
    public String getCommunity() {
        return m_community;
    }

    /**
     * <p>setCommunity</p>
     *
     * @param community the community to set
     */
    public void setCommunity(String community) {
        m_community = community;
    }

    /**
     * <p>getVersion</p>
     *
     * @return the version
     */
    public String getVersion() {
        return m_version;
    }

    /**
     * <p>setVersion</p>
     *
     * @param version the version to set
     */
    public void setVersion(String version) {
        m_version = version;
    }

    /**
     * <p>getPort</p>
     *
     * @return the port
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>setPort</p>
     *
     * @param port the port to set
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * <p>getRetries</p>
     *
     * @return the retries
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries the retries to set
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>createEventInfo</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.SnmpEventInfo} object.
     * @throws java.net.UnknownHostException if any.
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

