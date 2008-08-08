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

/**
 * Represents a base class for SnmpConfiguration of agents, ranges and defaults
 *
 * @author brozow
 */
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

    private Integer m_timeout;
    private Integer m_retries;
    private Integer m_port;
    private Integer m_version;
    private Integer m_maxRequestSize;
    private Integer m_securityLevel;
    private String m_securityName;
    private String m_readCommunity;
    private Integer m_maxVarsPerPdu;
    private Integer m_maxRepetitions;
    private String m_writeCommunity;
    private String m_authPassPhrase;
    private String m_authProtocol;
    private String m_privProtocol;
    private String m_privPassPhrase;
    
    private SnmpConfiguration m_defaults;
    
    public SnmpConfiguration() {
        this(DEFAULTS);
    }
    
    public SnmpConfiguration(SnmpConfiguration defaults) {
        m_defaults = defaults;
    }

    protected void setDefaults() {
        m_timeout = DEFAULT_TIMEOUT;
        m_retries = DEFAULT_RETRIES;
        m_port = DEFAULT_PORT;
        m_version = DEFAULT_VERSION;
        m_maxRequestSize = DEFAULT_MAX_REQUEST_SIZE;
        m_securityLevel = DEFAULT_SECURITY_LEVEL;
        m_securityName = DEFAULT_SECURITY_NAME;
        m_authPassPhrase = DEFAULT_AUTH_PASS_PHRASE;
        m_authProtocol = DEFAULT_AUTH_PROTOCOL;
        m_privProtocol = DEFAULT_PRIV_PROTOCOL;
        m_privPassPhrase = DEFAULT_PRIV_PASS_PHRASE;
        m_readCommunity = DEFAULT_READ_COMMUNITY;
        m_maxVarsPerPdu = DEFAULT_MAX_VARS_PER_PDU;
        m_maxRepetitions = DEFAULT_MAX_REPETITIONS;
        m_writeCommunity = DEFAULT_WRITE_COMMUNITY;
    }

    public int getPort() {
        return hasPort() ? m_port : m_defaults.getPort();
    }

    public boolean hasPort() {
        return m_port != null;
    }

    public void setPort(Integer port) {
        m_port = port;
    }

    public int getTimeout() {
        return hasTimeout() ? m_timeout : m_defaults.getTimeout();
    }

    public boolean hasTimeout() {
        return m_timeout != null;
    }

    public void setTimeout(Integer timeout) {
        m_timeout = timeout;
    }

    public int getVersion() {
        return hasVersion() ? m_version : m_defaults.getVersion();
    }
    
    public String getVersionAsString() {
        return versionToString(getVersion());
    }

    public boolean hasVersion() {
        return m_version != null;
    }

    public void setVersion(Integer version) {
        m_version = version;
    }

    public int getRetries() {
        return hasRetries() ? m_retries : m_defaults.getRetries();
    }

    public boolean hasRetries() {
        return m_retries != null;
    }

    public void setRetries(Integer retries) {
        m_retries = retries;
    }

    public int getSecurityLevel() {
        return hasSecurityLevel() ? m_securityLevel : m_defaults.getSecurityLevel();
    }

    public boolean hasSecurityLevel() {
        return m_securityLevel != null;
    }

    public void setSecurityLevel(Integer securityLevel) {
        m_securityLevel = securityLevel;
    }

    public String getSecurityName() {
        return hasSecurityName() ? m_securityName : m_defaults.getSecurityName();
    }

    public boolean hasSecurityName() {
        return m_securityName != null;
    }

    public void setSecurityName(String securityName) {
        m_securityName = securityName;
    }

    public void setReadCommunity(String community) {
        m_readCommunity = community;
    }

    public int getMaxRequestSize() {
        return hasMaxRequestSize() ? m_maxRequestSize : m_defaults.getMaxRequestSize();
    }

    public boolean hasMaxRequestSize() {
        return m_maxRequestSize != null;
    }

    public void setMaxRequestSize(Integer maxRequestSize) {
        m_maxRequestSize = maxRequestSize;
    }

    public String getReadCommunity() {
        return hasReadCommunity() ? m_readCommunity : m_defaults.getReadCommunity();
    }

    public boolean hasReadCommunity() {
        return m_readCommunity != null;
    }

    public int getMaxVarsPerPdu() {
        return hasMaxVarsPerPdu() ? m_maxVarsPerPdu : m_defaults.getMaxVarsPerPdu();
    }

    public boolean hasMaxVarsPerPdu() {
        return m_maxVarsPerPdu != null;
    }

    public void setMaxVarsPerPdu(Integer maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    public int getMaxRepetitions() {
        return hasMaxRepetitions() ? m_maxRepetitions : m_defaults.getMaxRepetitions();
    }

    public boolean hasMaxRepetitions() {
        return m_maxRepetitions != null;
    }

    public void setMaxRepetitions(Integer maxRepetitions) {
        m_maxRepetitions = maxRepetitions;
    }

    public String getWriteCommunity() {
        return hasWriteCommunity() ? m_writeCommunity : m_defaults.getWriteCommunity();
    }

    public boolean hasWriteCommunity() {
        return m_writeCommunity != null;
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
        return hasAuthPassPhrase() ? m_authPassPhrase : m_defaults.getAuthPassPhrase();
    }

    public boolean hasAuthPassPhrase() {
        return m_authPassPhrase != null;
    }

    public void setAuthPassPhrase(String authPassPhrase) {
        m_authPassPhrase = authPassPhrase;
    }

    public String getPrivProtocol() {
        return hasPrivProtocol() ? m_privProtocol : m_defaults.getPrivProtocol();
    }

    public boolean hasPrivProtocol() {
        return m_privProtocol != null;
    }

    public void setPrivProtocol(String authPrivProtocol) {
        m_privProtocol = authPrivProtocol;
    }

    public String getAuthProtocol() {
        return hasAuthProtocol() ? m_authProtocol : m_defaults.getAuthProtocol();
    }

    public boolean hasAuthProtocol() {
        return m_authProtocol != null;
    }

    public void setAuthProtocol(String authProtocol) {
        m_authProtocol = authProtocol;
    }

    public String getPrivPassPhrase() {
        return hasPrivPassPhrase() ? m_privPassPhrase : m_defaults.getPrivPassPhrase();
    }

    public boolean hasPrivPassPhrase() {
        return m_privPassPhrase != null;
    }

    public void setPrivPassPhrase(String privPassPhrase) {
        m_privPassPhrase = privPassPhrase;
    }

}
