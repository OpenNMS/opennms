//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.protocols.wmi;

import java.net.InetAddress;

public class WmiAgentConfig {
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_RETRIES = 1;
    public static final String DEFAULT_PASSWORD = "";
    public static final String DEFAULT_USERNAME="Administrator";
    public static final String DEFAULT_DOMAIN="WORKGROUP";
    
    private InetAddress m_Address;
    private int m_Timeout;
    private int m_Retries;
    private String m_Username;
    private String m_Domain;
    private String m_Password;
    
    
	String user = "";
	String pass = "";
	String domain = "";
	String matchType = "all";
	String compVal = "";
	String compOp = "NOOP";
	String wmiClass = "";
	String wmiObject = "";
    public WmiAgentConfig() {
        setDefaults();
    }
    
    public WmiAgentConfig(InetAddress agentAddress) {
        m_Address = agentAddress;
        setDefaults();
    }

    private void setDefaults() {
        m_Timeout = DEFAULT_TIMEOUT;
        m_Retries = DEFAULT_RETRIES;
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer("AgentConfig[");
        buff.append("Address: "+m_Address);
        buff.append(", Password: "+String.valueOf(m_Password)); //use valueOf to handle null values of m_password
        buff.append(", Timeout: "+m_Timeout);
        buff.append(", Retries: "+m_Retries);
        buff.append("]");
        return buff.toString();
    }


    public InetAddress getAddress() {
        return m_Address;
    }

    public void setAddress(InetAddress address) {
        m_Address = address;
    }

    public int getTimeout() {
        return m_Timeout;
    }

    public void setTimeout(int timeout) {
        m_Timeout = timeout;
    }

    public int getRetries() {
        return m_Retries;
    }

    public void setRetries(int retries) {
        m_Retries = retries;
    }

    public void setPassword(String password) {
        m_Password = password;
    }

    public String getPassword() {
        return m_Password;
    }


    public String getUsername() {
        return m_Username;
    }
    
    public void setUsername(String username) {
    	m_Username = username;
    }

    public String getDomain() {
        return m_Domain;
    }
    
    public void setDomain(String domain) {
    	m_Domain = domain;
    }
    
}
