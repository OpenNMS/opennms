/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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
    private String m_engineId;
    private String m_contextEngineId;
    private String m_contextName;
    private String m_enterpriseId;
    
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
            setEnterpriseId(config.getEnterpriseId());
            setContextName(config.getContextName());
            setContextEngineId(config.getContextEngineId());
            setEngineId(config.getEngineId());            
        }
    }

    public final int getPort() {
        return m_port;
    }

    public final void setPort(int port) {
        m_port = port;
    }

    public final int getTimeout() {
        return m_timeout;
    }


    public final void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public final int getVersion() {
        return m_version;
    }
    
    public final void setVersion(int version) {
        m_version = version;
    }

    public final String getVersionAsString() {
        return versionToString(getVersion());
    }
    
    public final void setVersionAsString(String version) {
        setVersion(stringToVersion(version));
    }

    public final int getRetries() {
        return m_retries;
    }

    public final void setRetries(int retries) {
        m_retries = retries;
    }

    public final int getSecurityLevel() {
        return m_securityLevel;
    }

    public final void setSecurityLevel(int securityLevel) {
        m_securityLevel = securityLevel;
    }

    public final String getSecurityName() {
        return m_securityName;
    }

    public final void setSecurityName(String securityName) {
        m_securityName = securityName;
    }

    public final void setReadCommunity(String community) {
        m_readCommunity = community;
    }

    public final int getMaxRequestSize() {
        return m_maxRequestSize;
    }

    public final void setMaxRequestSize(int maxRequestSize) {
        m_maxRequestSize = maxRequestSize;
    }

    public final String getReadCommunity() {
        return m_readCommunity;
    }

    public final int getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    public final void setMaxVarsPerPdu(int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    public final int getMaxRepetitions() {
        return m_maxRepetitions;
    }

    public final void setMaxRepetitions(int maxRepetitions) {
        m_maxRepetitions = maxRepetitions;
    }

    public final String getWriteCommunity() {
        return m_writeCommunity;
    }

    public final void setWriteCommunity(String community) {
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

    public final String getAuthPassPhrase() {
        return m_authPassPhrase;
    }

    public final void setAuthPassPhrase(String authPassPhrase) {
        m_authPassPhrase = authPassPhrase;
    }

    public final String getPrivProtocol() {
        return m_privProtocol;
    }

    public final void setPrivProtocol(String authPrivProtocol) {
        m_privProtocol = authPrivProtocol;
    }

    public final String getAuthProtocol() {
        return m_authProtocol;
    }

    public final void setAuthProtocol(String authProtocol) {
        m_authProtocol = authProtocol;
    }

    public final String getPrivPassPhrase() {
        return m_privPassPhrase;
    }

    public final void setPrivPassPhrase(String privPassPhrase) {
        m_privPassPhrase = privPassPhrase;
    }
    
    public final String getEngineId() {
    	return m_engineId;
    }
    
    public final void setEngineId(final String engineId) {
    	m_engineId = engineId;
    }
    
    public final String getContextEngineId() {
    	return m_contextEngineId;
    }
    
    public final void setContextEngineId(final String contextEngineId) {
    	m_contextEngineId = contextEngineId;
    }
    
    public final String getContextName() {
    	return m_contextName;
    }
    
    public void setContextName(final String contextName) {
    	m_contextName = contextName;
    }
    
    public final String getEnterpriseId() {
    	return m_enterpriseId;
    }
    
    public void setEnterpriseId(final String enterpriseId) {
    	m_enterpriseId = enterpriseId;
    }
    
    public boolean isVersion3() {
    	return getVersion() == VERSION3;
    }
}
