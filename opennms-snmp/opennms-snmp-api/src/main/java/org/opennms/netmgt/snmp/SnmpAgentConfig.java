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
 * <p>SnmpAgentConfig class.</p>
 *
 * @author (various previous authors not documented)
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class SnmpAgentConfig {
    
    /** Constant <code>DEFAULT_TIMEOUT=3000</code> */
    public static final int DEFAULT_TIMEOUT = 3000;
    /** Constant <code>DEFAULT_PORT=161</code> */
    public static final int DEFAULT_PORT = 161;
    /** Constant <code>VERSION1=1</code> */
    public static final int VERSION1 = 1;
    /** Constant <code>VERSION2C=2</code> */
    public static final int VERSION2C = 2;
    /** Constant <code>VERSION3=3</code> */
    public static final int VERSION3 = 3;
    /** Constant <code>DEFAULT_VERSION=VERSION1</code> */
    public static final int DEFAULT_VERSION = VERSION1;
    /** Constant <code>DEFAULT_RETRIES=1</code> */
    public static final int DEFAULT_RETRIES = 1;
    /** Constant <code>DEFAULT_MAX_REQUEST_SIZE=65535</code> */
    public static final int DEFAULT_MAX_REQUEST_SIZE = 65535;
    /** Constant <code>NOAUTH_NOPRIV=1</code> */
    public static final int NOAUTH_NOPRIV = 1;
    /** Constant <code>AUTH_NOPRIV=2</code> */
    public static final int AUTH_NOPRIV = 2;
    /** Constant <code>AUTH_PRIV=3</code> */
    public static final int AUTH_PRIV = 3;
    /** Constant <code>DEFAULT_READ_COMMUNITY="public"</code> */
    public static final String DEFAULT_READ_COMMUNITY = "public";
    /** Constant <code>DEFAULT_MAX_VARS_PER_PDU=10</code> */
    public static final int DEFAULT_MAX_VARS_PER_PDU = 10;
    /** Constant <code>DEFAULT_MAX_REPETITIONS=2</code> */
    public static final int DEFAULT_MAX_REPETITIONS = 2;
    /** Constant <code>DEFAULT_WRITE_COMMUNITY="private"</code> */
    public static final String DEFAULT_WRITE_COMMUNITY = "private";
    /** Constant <code>DEFAULT_SECURITY_LEVEL=NOAUTH_NOPRIV</code> */
    public static final int DEFAULT_SECURITY_LEVEL = NOAUTH_NOPRIV;
    /** Constant <code>DEFAULT_SECURITY_NAME="opennmsUser"</code> */
    public static final String DEFAULT_SECURITY_NAME = "opennmsUser";
    /** Constant <code>DEFAULT_AUTH_PASS_PHRASE="0p3nNMSv3"</code> */
    public static final String DEFAULT_AUTH_PASS_PHRASE = "0p3nNMSv3";
    /** Constant <code>DEFAULT_AUTH_PROTOCOL="MD5"</code> */
    public static final String DEFAULT_AUTH_PROTOCOL = "MD5";
    /** Constant <code>DEFAULT_PRIV_PROTOCOL="DES"</code> */
    public static final String DEFAULT_PRIV_PROTOCOL = "DES";
    /** Constant <code>DEFAULT_PRIV_PASS_PHRASE="DEFAULT_AUTH_PASS_PHRASE"</code> */
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
    private int m_maxRepetitions;
    private String m_writeCommunity;
    private String m_authPassPhrase;
    private String m_authProtocol;
    private String m_PrivProtocol;
    private String m_privPassPhrase;
    private InetAddress m_proxyFor;
    
    /**
     * <p>Constructor for SnmpAgentConfig.</p>
     */
    public SnmpAgentConfig() {
        setDefaults();
    }
    
    /**
     * <p>Constructor for SnmpAgentConfig.</p>
     *
     * @param agentAddress a {@link java.net.InetAddress} object.
     */
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
        m_maxRepetitions = DEFAULT_MAX_REPETITIONS;
        m_writeCommunity = DEFAULT_WRITE_COMMUNITY;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer("AgentConfig[");
        buff.append("Address: "+m_address);
        buff.append(", Port: "+m_port);
        buff.append(", Community: "+m_readCommunity);
        buff.append(", Timeout: "+m_timeout);
        buff.append(", Retries: "+m_retries);
        buff.append(", MaxVarsPerPdu: "+m_maxVarsPerPdu);
        buff.append(", MaxRepititions: "+m_maxRepetitions);
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


    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_address;
    }

    /**
     * <p>setAddress</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    public void setAddress(InetAddress address) {
        m_address = address;
    }

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a int.
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout a int.
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>getVersion</p>
     *
     * @return a int.
     */
    public int getVersion() {
        return m_version;
    }

    /**
     * <p>setVersion</p>
     *
     * @param version a int.
     */
    public void setVersion(int version) {
        m_version = version;
    }

    /**
     * <p>getRetries</p>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries a int.
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * <p>getSecurityLevel</p>
     *
     * @return a int.
     */
    public int getSecurityLevel() {
        return m_securityLevel;
    }

    /**
     * <p>setSecurityLevel</p>
     *
     * @param securityLevel a int.
     */
    public void setSecurityLevel(int securityLevel) {
        m_securityLevel = securityLevel;
    }

    /**
     * <p>getSecurityName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSecurityName() {
        return m_securityName;
    }

    /**
     * <p>setSecurityName</p>
     *
     * @param securityName a {@link java.lang.String} object.
     */
    public void setSecurityName(String securityName) {
        m_securityName = securityName;
    }

    /**
     * <p>setReadCommunity</p>
     *
     * @param community a {@link java.lang.String} object.
     */
    public void setReadCommunity(String community) {
        m_readCommunity = community;
    }

    /**
     * <p>getMaxRequestSize</p>
     *
     * @return a int.
     */
    public int getMaxRequestSize() {
        return m_maxRequestSize;
    }

    /**
     * <p>setMaxRequestSize</p>
     *
     * @param maxRequestSize a int.
     */
    public void setMaxRequestSize(int maxRequestSize) {
        m_maxRequestSize = maxRequestSize;
    }

    /**
     * <p>getReadCommunity</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReadCommunity() {
        return m_readCommunity;
    }

    /**
     * <p>getMaxVarsPerPdu</p>
     *
     * @return a int.
     */
    public int getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    /**
     * <p>setMaxVarsPerPdu</p>
     *
     * @param maxVarsPerPdu a int.
     */
    public void setMaxVarsPerPdu(int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }
    
    /**
     * <p>getMaxRepetitions</p>
     *
     * @return a int.
     */
    public int getMaxRepetitions() {
        return m_maxRepetitions;
    }

    /**
     * <p>setMaxRepetitions</p>
     *
     * @param maxRepetitions a int.
     */
    public void setMaxRepetitions(int maxRepetitions) {
        m_maxRepetitions = maxRepetitions;
    }

    /**
     * <p>getWriteCommunity</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWriteCommunity() {
        return m_writeCommunity;
    }
    
    /**
     * <p>setWriteCommunity</p>
     *
     * @param community a {@link java.lang.String} object.
     */
    public void setWriteCommunity(String community) {
        m_writeCommunity = community;
    }
    
    /**
     * <p>versionToString</p>
     *
     * @param version a int.
     * @return a {@link java.lang.String} object.
     */
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

    /**
     * <p>getAuthPassPhrase</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAuthPassPhrase() {
        return m_authPassPhrase;
    }

    /**
     * <p>setAuthPassPhrase</p>
     *
     * @param authPassPhrase a {@link java.lang.String} object.
     */
    public void setAuthPassPhrase(String authPassPhrase) {
        m_authPassPhrase = authPassPhrase;
    }

    /**
     * <p>getPrivProtocol</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPrivProtocol() {
        return m_PrivProtocol;
    }

    /**
     * <p>setPrivProtocol</p>
     *
     * @param authPrivProtocol a {@link java.lang.String} object.
     */
    public void setPrivProtocol(String authPrivProtocol) {
        m_PrivProtocol = authPrivProtocol;
    }

    /**
     * <p>getAuthProtocol</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAuthProtocol() {
        return m_authProtocol;
    }

    /**
     * <p>setAuthProtocol</p>
     *
     * @param authProtocol a {@link java.lang.String} object.
     */
    public void setAuthProtocol(String authProtocol) {
        m_authProtocol = authProtocol;
    }
    
    /**
     * <p>getPrivPassPhrase</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPrivPassPhrase() {
        return m_privPassPhrase;
    }

    /**
     * <p>setPrivPassPhrase</p>
     *
     * @param privPassPhrase a {@link java.lang.String} object.
     */
    public void setPrivPassPhrase(String privPassPhrase) {
        m_privPassPhrase = privPassPhrase;
    }

    /**
     * <p>getProxyFor</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getProxyFor() {
        return m_proxyFor;
    }
    
    /**
     * <p>setProxyFor</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    public void setProxyFor(InetAddress address) {
        m_proxyFor = address;
    }
    
}
