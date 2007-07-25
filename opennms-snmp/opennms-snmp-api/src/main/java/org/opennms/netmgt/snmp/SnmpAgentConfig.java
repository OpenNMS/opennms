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
 * @author (various previous authors not documented)
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
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
    public static final int NOAUTH_NOPRIV = 1;
    public static final int AUTH_NOPRIV = 2;
    public static final int AUTH_PRIV = 3;
    public static final String DEFAULT_READ_COMMUNITY = "public";
    public static final int DEFAULT_MAX_VARS_PER_PDU = 10;
    public static final int DEFAULT_MAX_REPITITIONS = 2;
    public static final String DEFAULT_WRITE_COMMUNITY = "private";
    public static final int DEFAULT_SECURITY_LEVEL = NOAUTH_NOPRIV;
    public static final String DEFAULT_SECURITY_NAME = "opennmsUser";
    public static final String DEFAULT_AUTH_PASS_PHRASE = "0p3nNMSv3";
    public static final String DEFAULT_AUTH_PROTOCOL = "MD5";
    public static final String DEFAULT_PRIV_PROTOCOL = "DES";
    public static final String DEFAULT_PRIV_PASS_PHRASE = DEFAULT_AUTH_PASS_PHRASE;
    
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
    private int m_maxRepititions;
    private String m_writeCommunity;
    private String m_authPassPhrase;
    private String m_authProtocol;
    private String m_PrivProtocol;
    private String m_privPassPhrase;
    private InetAddress m_proxyFor;
    
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
        m_authPassPhrase = DEFAULT_AUTH_PASS_PHRASE;
        m_authProtocol = DEFAULT_AUTH_PROTOCOL;
        m_PrivProtocol = DEFAULT_PRIV_PROTOCOL;
        m_privPassPhrase = DEFAULT_PRIV_PASS_PHRASE;
        m_readCommunity = DEFAULT_READ_COMMUNITY;
        m_maxVarsPerPdu = DEFAULT_MAX_VARS_PER_PDU;
        m_maxRepititions = DEFAULT_MAX_REPITITIONS;
        m_writeCommunity = DEFAULT_WRITE_COMMUNITY;
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer("AgentConfig[");
        buff.append("Address: "+m_address);
        buff.append(", Port: "+m_port);
        buff.append(", Community: "+m_readCommunity);
        buff.append(", Timeout: "+m_timeout);
        buff.append(", Retries: "+m_retries);
        buff.append(", MaxVarsPerPdu: "+m_maxVarsPerPdu);
        buff.append(", MaxRepititions: "+m_maxRepititions);
        buff.append(", Max request size: "+m_maxRequestSize);
        buff.append(", Version: "+m_version);
        buff.append(", ProxyForAddress: "+m_proxyFor);
        if (m_version == VERSION3) {
            buff.append(", Security level: "+m_securityLevel);
            buff.append(", Security name: "+m_securityName);
            buff.append(", auth-passphrase: "+m_authPassPhrase);
            buff.append(", auth-protocol: "+m_authProtocol);
            buff.append(", priv-passprhase: "+m_privPassPhrase);
            buff.append(", priv-protocol: "+m_PrivProtocol);
        }
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
    
    public int getMaxRepititions() {
        return m_maxRepititions;
    }

    public void setMaxRepititions(int maxRepititions) {
        m_maxRepititions = maxRepititions;
    }

    public String getWriteCommunity() {
        return m_writeCommunity;
    }
    
    public void setWriteCommunity(String community) {
        m_writeCommunity = community;
    }
    
    public static String versionToString(int version) {
        switch (version) {
        case VERSION1 :
            return "v1";
        case VERSION2C :
            return "v2c";
        case VERSION3 :
            return "v3";
        default :
            return "unknown";
        }
    }

    public String getAuthPassPhrase() {
        return m_authPassPhrase;
    }

    public void setAuthPassPhrase(String authPassPhrase) {
        m_authPassPhrase = authPassPhrase;
    }

    public String getPrivProtocol() {
        return m_PrivProtocol;
    }

    public void setPrivProtocol(String authPrivProtocol) {
        m_PrivProtocol = authPrivProtocol;
    }

    public String getAuthProtocol() {
        return m_authProtocol;
    }

    public void setAuthProtocol(String authProtocol) {
        m_authProtocol = authProtocol;
    }
    
    public String getPrivPassPhrase() {
        return m_privPassPhrase;
    }

    public void setPrivPassPhrase(String privPassPhrase) {
        m_privPassPhrase = privPassPhrase;
    }

    public InetAddress getProxyFor() {
        return m_proxyFor;
    }
    
    public void setProxyFor(InetAddress address) {
        m_proxyFor = address;
    }
    
}
