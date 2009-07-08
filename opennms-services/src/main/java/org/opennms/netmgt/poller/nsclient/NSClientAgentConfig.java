/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.poller.nsclient;

import java.net.InetAddress;

/**
 * 
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public class NSClientAgentConfig {
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_PORT = 1248;
    public static final int DEFAULT_RETRIES = 1;
    public static final String DEFAULT_PASSWORD = "None";
    
    private InetAddress m_address;
    private int m_timeout;
    private int m_retries;
    private int m_port;
    private String m_password;

    
    public NSClientAgentConfig() {
        setDefaults();
    }
    
    public NSClientAgentConfig(InetAddress agentAddress) {
        m_address = agentAddress;
        setDefaults();
    }

    private void setDefaults() {
        m_timeout = DEFAULT_TIMEOUT;
        m_retries = DEFAULT_RETRIES;
        m_port = DEFAULT_PORT;
        m_password = DEFAULT_PASSWORD;
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer("AgentConfig[");
        buff.append("Address: "+m_address);
        buff.append(", Port: "+m_port);
        buff.append(", Password: "+String.valueOf(m_password)); //use valueOf to handle null values of m_password
        buff.append(", Timeout: "+m_timeout);
        buff.append(", Retries: "+m_retries);
        buff.append("]");
        return buff.toString();
    }


    public InetAddress getAddress() {
        return m_address;
    }

    public void setAddress(InetAddress address) {
        m_address = address;
    }

    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public int getRetries() {
        return m_retries;
    }

    public void setRetries(int retries) {
        m_retries = retries;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public String getPassword() {
        return m_password;
    }
}
