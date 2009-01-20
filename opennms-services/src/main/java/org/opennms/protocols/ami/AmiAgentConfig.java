//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// Modifications:
// 2009 17 Jan: Created file - jeffg@opennms.org
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.protocols.ami;

import java.net.InetAddress;

public class AmiAgentConfig {
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_RETRIES = 1;
    public static final String DEFAULT_PASSWORD = "";
    public static final String DEFAULT_USERNAME="opennms";
    public static final int DEFAULT_PORT = 5038;
    public static final int DEFAULT_TLS_PORT = 5039;
    public static final boolean DEFAULT_USE_TLS = false;
    
    private InetAddress m_address;
    private int m_timeout;
    private int m_retries;
    private String m_username;
    private String m_password;
    private int m_port;
    private boolean m_useTls;
    
    String user = "";
	String pass = "";
	String matchType = "all";
    public AmiAgentConfig() {
        setDefaults();
    }
    
    public AmiAgentConfig(InetAddress agentAddress) {
        m_address = agentAddress;
        setDefaults();
    }

    private void setDefaults() {
        m_timeout = DEFAULT_TIMEOUT;
        m_retries = DEFAULT_RETRIES;
        m_port = DEFAULT_PORT;
        m_useTls = DEFAULT_USE_TLS;
        m_username = DEFAULT_USERNAME;
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer("AgentConfig[");
        buff.append("Address: "+m_address);
        buff.append(", Port: " +m_port);
        buff.append(", TLS: "+m_useTls);
        buff.append(", Username: "+String.valueOf(m_username)); //use valueOf to handle null values of m_username
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


    public String getUsername() {
        return m_username;
    }
    
    public void setUsername(String username) {
    	m_username = username;
    }
    
    public int getPort() {
        return m_port;
    }
    
    public void setPort(int port) {
        m_port = port;
    }
    
    public boolean getUseTls() {
        return m_useTls;
    }

    public void setUseTls(boolean useTls) {
        m_useTls = useTls;
    }
}