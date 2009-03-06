/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.snmp;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a base class for SnmpConfiguration of agents, ranges and defaults
 *
 * @author brozow
 */
@XmlRootElement(name="snmpConfiguration")
public class SnmpConfiguration {
    
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
    public static final int DEFAULT_MAX_REPETITIONS = 2;
    public static final String DEFAULT_WRITE_COMMUNITY = "private";
    public static final int DEFAULT_SECURITY_LEVEL = NOAUTH_NOPRIV;
    public static final String DEFAULT_SECURITY_NAME = "opennmsUser";
    public static final String DEFAULT_AUTH_PASS_PHRASE = "0p3nNMSv3";
    public static final String DEFAULT_AUTH_PROTOCOL = "MD5";
    public static final String DEFAULT_PRIV_PROTOCOL = "DES";
    public static final String DEFAULT_PRIV_PASS_PHRASE = DEFAULT_AUTH_PASS_PHRASE;

    public static final SnmpConfiguration DEFAULTS;
    
    static {
        DEFAULTS = new SnmpConfiguration(null);
        DEFAULTS.setTimeout(DEFAULT_TIMEOUT);
        DEFAULTS.setRetries(DEFAULT_RETRIES);
        DEFAULTS.setPort(DEFAULT_PORT);
        DEFAULTS.setVersion(DEFAULT_VERSION);
        DEFAULTS.setMaxRequestSize(DEFAULT_MAX_REQUEST_SIZE);
        DEFAULTS.setSecurityLevel(DEFAULT_SECURITY_LEVEL);
        DEFAULTS.setSecurityName(DEFAULT_SECURITY_NAME);
        DEFAULTS.setReadCommunity(DEFAULT_READ_COMMUNITY);
        DEFAULTS.setMaxVarsPerPdu(DEFAULT_MAX_VARS_PER_PDU);
        DEFAULTS.setMaxRepetitions(DEFAULT_MAX_REPETITIONS);
        DEFAULTS.setWriteCommunity(DEFAULT_WRITE_COMMUNITY);
        DEFAULTS.setAuthPassPhrase(DEFAULT_AUTH_PASS_PHRASE);
        DEFAULTS.setAuthProtocol(DEFAULT_AUTH_PROTOCOL);
        DEFAULTS.setPrivProtocol(DEFAULT_PRIV_PROTOCOL);
        DEFAULTS.setPrivPassPhrase(DEFAULT_PRIV_PASS_PHRASE);
    }

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
    private String m_privProtocol;
    private String m_privPassPhrase;
    
    public SnmpConfiguration() {
        this(DEFAULTS);
    }
    
    public SnmpConfiguration(SnmpConfiguration config) {
        if (config != null) {
            setAuthPassPhrase(config.getAuthPassPhrase());
            setAuthProtocol(config.getAuthProtocol());
            setMaxRepetitions(config.getMaxRepetitions());
            setMaxRequestSize(config.getMaxRequestSize());
            setMaxVarsPerPdu(config.getMaxVarsPerPdu());
            setPort(config.getPort());
            setPrivPassPhrase(config.getPrivPassPhrase());
            setPrivProtocol(config.getPrivProtocol());
            setReadCommunity(config.getReadCommunity());
            setSecurityLevel(config.getSecurityLevel());
            setSecurityName(config.getSecurityName());
            setTimeout(config.getTimeout());
            setVersion(config.getVersion());
            setWriteCommunity(config.getWriteCommunity());
        }
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

    public String getVersionAsString() {
        return versionToString(getVersion());
    }
    
    public void setVersionAsString(String version) {
        setVersion(stringToVersion(version));
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

    public int getMaxRepetitions() {
        return m_maxRepetitions;
    }

    public void setMaxRepetitions(int maxRepetitions) {
        m_maxRepetitions = maxRepetitions;
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
    
    public static int stringToVersion(String version) {
        if ("v1".equalsIgnoreCase(version)) {
            return VERSION1;
        }
        if ("v2c".equalsIgnoreCase(version)) {
            return VERSION2C;
        }
        if ("v3".equalsIgnoreCase(version)) {
            return VERSION3;
        }
        return VERSION1;
    }

    public String getAuthPassPhrase() {
        return m_authPassPhrase;
    }

    public void setAuthPassPhrase(String authPassPhrase) {
        m_authPassPhrase = authPassPhrase;
    }

    public String getPrivProtocol() {
        return m_privProtocol;
    }

    public void setPrivProtocol(String authPrivProtocol) {
        m_privProtocol = authPrivProtocol;
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

}
