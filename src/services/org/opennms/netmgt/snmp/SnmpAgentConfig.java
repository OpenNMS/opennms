//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.snmp;

import java.net.InetAddress;

/**
 * @author david
 *
 */
public class SnmpAgentConfig {
    
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_PORT = 161;
    public static final int VERSION1 = 1;
    public static final int VERSION2C = 2;
    public static final int VERSION3 = 3;
    public static final int DEFAULT_VERSION = VERSION1;
    public static final int DEFAULT_RETRIES = 1;
    public static final int DEFAULT_MAX_REQUEST_SIZE = 65535;
    public static final int NOAUTH_NO_PRIV = 1;
    public static final int AUTH_NOPRIV = 2;
    public static final int AUTH_PRIV = 3;
    public static final int DEFAULT_SECURITY_LEVEL = NOAUTH_NO_PRIV;
    public static final String DEFAULT_SECURITY_NAME = "opennmsUser";
    public static final String DEFAULT_COMMUNITY = "public";
    public static final int DEFAULT_MAX_VARS_PER_PDU = 100;
    
    private InetAddress m_address;
    private int m_timeout;
    private int m_retries;
    private int m_port;
    private int m_version;
    private int m_maxRequestSize;
    private int m_securityLevel;
    private String m_securityName;
    private String m_readCommunity;
    private int m_maxVarsPerPdu;
    
    public SnmpAgentConfig() {
        setDefaults();
    }
    
    public SnmpAgentConfig(InetAddress agentAddress) {
        m_address = agentAddress;
        setDefaults();
    }

    private void setDefaults() {
        m_timeout = DEFAULT_TIMEOUT;
        m_retries = DEFAULT_RETRIES;
        m_port = DEFAULT_PORT;
        m_version = DEFAULT_VERSION;
        m_maxRequestSize = DEFAULT_MAX_REQUEST_SIZE;
        m_securityLevel = DEFAULT_SECURITY_LEVEL;
        m_securityName = DEFAULT_SECURITY_NAME;
        m_readCommunity = DEFAULT_COMMUNITY;
        m_maxVarsPerPdu = DEFAULT_MAX_VARS_PER_PDU;
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

    public int getVersion() {
        return m_version;
    }

    public void setVersion(int version) {
        m_version = version;
    }

    public int getRetries() {
        return m_retries;
    }

    public void setRetries(int retries) {
        m_retries = retries;
    }

    public int getSecurityLevel() {
        return m_securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        m_securityLevel = securityLevel;
    }

    public String getSecurityName() {
        return m_securityName;
    }

    public void setSecurityName(String securityName) {
        m_securityName = securityName;
    }

    public void setReadCommunity(String community) {
        m_readCommunity = community;
    }

    public int getMaxRequestSize() {
        return m_maxRequestSize;
    }

    public void setMaxRequestSize(int maxRequestSize) {
        m_maxRequestSize = maxRequestSize;
    }

    public String getReadCommunity() {
        return m_readCommunity;
    }

    public int getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    public void setMaxVarsPerPdu(int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

}
