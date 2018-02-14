/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.snmp;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class Configuration implements Serializable {
    private static final long serialVersionUID = 6018795999027969844L;

    /**
     * The proxy host to use when communicating with this agent
     */
    @XmlAttribute(name="proxy-host")
    private String m_proxyHost;

    /**
     * Number of variables to send per SNMP request.
     */
    @XmlAttribute(name="max-vars-per-pdu")
    private Integer m_maxVarsPerPdu;

    /**
     * Number of repetitions to send per get-bulk request.
     */
    @XmlAttribute(name="max-repetitions")
    private Integer m_maxRepetitions;

    /**
     * (SNMP4J specific) Specifies the maximum number of bytes that may be
     * encoded into an individual SNMP PDU request by Collectd. Provides a
     * means to limit the size of outgoing PDU requests. Default is 65535,
     * must be at least 484.
     */
    @XmlAttribute(name="max-request-size")
    private Integer m_maxRequestSize;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="security-name")
    private String m_securityName;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="security-level")
    private Integer m_securityLevel;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="auth-passphrase")
    private String m_authPassphrase;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="auth-protocol")
    private String m_authProtocol;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="engine-id")
    private String m_engineId;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="context-engine-id")
    private String m_contextEngineId;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="context-name")
    private String m_contextName;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="privacy-passphrase")
    private String m_privacyPassphrase;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="privacy-protocol")
    private String m_privacyProtocol;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="enterprise-id")
    private String m_enterpriseId;

    /**
     * If set, forces SNMP data collection to the specified version.
     */
    @XmlAttribute(name="version")
    private String m_version;

    /**
     * Default write community string
     */
    @XmlAttribute(name="write-community")
    private String m_writeCommunity;

    /**
     * Default read community string
     */
    @XmlAttribute(name="read-community")
    private String m_readCommunity;

    /**
     * Default timeout (in milliseconds)
     */
    @XmlAttribute(name="timeout")
    private Integer m_timeout;

    /**
     * Default number of retries
     */
    @XmlAttribute(name="retry")
    private Integer m_retry;

    /**
     * If set, overrides UDP port 161 as the port where SNMP GET/GETNEXT/GETBULK
     * requests are sent.
     */
    @XmlAttribute(name="port")
    private Integer m_port;

    public Configuration() {
        super();
    }

    public Configuration(
            final Integer port,
            final Integer retry,
            final Integer timeout,
            final String readCommunity,
            final String writeCommunity,
            final String proxyHost,
            final String version,
            final Integer maxVarsPerPdu,
            final Integer maxRepetitions,
            final Integer maxRequestSize,
            final String securityName,
            final Integer securityLevel,
            final String authPassphrase,
            final String authProtocol,
            final String engineId,
            final String contextEngineId,
            final String contextName,
            final String privacyPassphrase,
            final String privacyProtocol,
            final String enterpriseId
            ) {
        setPort(port);
        setRetry(retry);
        setTimeout(timeout);
        setReadCommunity(readCommunity);
        setWriteCommunity(writeCommunity);
        setProxyHost(proxyHost);
        setVersion(version);
        setMaxVarsPerPdu(maxVarsPerPdu);
        setMaxRepetitions(maxRepetitions);
        setMaxRequestSize(maxRequestSize);
        setSecurityName(securityName);
        setSecurityLevel(securityLevel);
        setAuthPassphrase(authPassphrase);
        setAuthProtocol(authProtocol);
        setEngineId(engineId);
        setContextEngineId(contextEngineId);
        setContextName(contextName);
        setPrivacyPassphrase(privacyPassphrase);
        setPrivacyProtocol(privacyProtocol);
        setEnterpriseId(enterpriseId);
    }

    public void deleteMaxRepetitions() {
        m_maxRepetitions = null;
    }

    public void deleteMaxRequestSize() {
        m_maxRequestSize = null;
    }

    public void deleteMaxVarsPerPdu() {
        m_maxVarsPerPdu = null;
    }

    public void deletePort() {
        m_port = null;
    }

    public void deleteRetry() {
        m_retry = null;
    }

    public void deleteSecurityLevel() {
        m_securityLevel = null;
    }

    public void deleteTimeout() {
        m_timeout = null;
    }


    /**
     * Returns the value of field 'authPassphrase'. The field 'authPassphrase'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'AuthPassphrase'.
     */
    public final String getAuthPassphrase() {
        return m_authPassphrase;
    }

    /**
     * Returns the value of field 'authProtocol'. The field 'authProtocol' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'AuthProtocol'.
     */
    public final String getAuthProtocol() {
        return m_authProtocol;
    }

    /**
     * Returns the value of field 'contextEngineId'. The field 'contextEngineId'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'ContextEngineId'.
     */
    public final String getContextEngineId() {
        return m_contextEngineId;
    }

    /**
     * Returns the value of field 'contextName'. The field 'contextName' has the
     * following description: SNMPv3
     * 
     * @return the value of field 'ContextName'.
     */
    public final String getContextName() {
        return m_contextName;
    }

    /**
     * Returns the value of field 'engineId'. The field 'engineId' has the
     * following description: SNMPv3
     * 
     * @return the value of field 'EngineId'.
     */
    public final String getEngineId() {
        return m_engineId;
    }

    /**
     * Returns the value of field 'enterpriseId'. The field 'enterpriseId' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'EnterpriseId'.
     */
    public final String getEnterpriseId() {
        return m_enterpriseId;
    }

    /**
     * Returns the value of field 'maxRepetitions'. The field 'maxRepetitions'
     * has the following description: Number of repetitions to send per get-bulk
     * request.
     * 
     * 
     * @return the value of field 'MaxRepetitions'.
     */
    public final Integer getMaxRepetitions() {
        return m_maxRepetitions == null? 2 : m_maxRepetitions;
    }

    /**
     * Returns the value of field 'maxRequestSize'. The field 'maxRequestSize'
     * has the following description: (SNMP4J specific) Specifies the maximum
     * number of bytes that may be encoded into an individual SNMP PDU request
     * by Collectd. Provides a means to limit the size of outgoing PDU requests.
     * Default is 65535, must be at least 484.
     * 
     * @return the value of field 'MaxRequestSize'.
     */
    public final Integer getMaxRequestSize() {
        return m_maxRequestSize == null? 65535 : m_maxRequestSize;
    }

    /**
     * Returns the value of field 'maxVarsPerPdu'. The field 'maxVarsPerPdu' has
     * the following description: Number of variables to send per SNMP request.
     * 
     * 
     * @return the value of field 'MaxVarsPerPdu'.
     */
    public final Integer getMaxVarsPerPdu() {
        return m_maxVarsPerPdu == null? 10 : m_maxVarsPerPdu;
    }

    /**
     * Returns the value of field 'port'. The field 'port' has the following
     * description: If set, overrides UDP port 161 as the port where SNMP
     * GET/GETNEXT/GETBULK requests are sent.
     * 
     * @return the value of field 'Port'.
     */
    public final Integer getPort() {
        return m_port == null? 0 : m_port;
    }

    /**
     * Returns the value of field 'privacyPassphrase'. The field
     * 'privacyPassphrase' has the following description: SNMPv3
     * 
     * @return the value of field 'PrivacyPassphrase'.
     */
    public final String getPrivacyPassphrase() {
        return m_privacyPassphrase;
    }

    /**
     * Returns the value of field 'privacyProtocol'. The field 'privacyProtocol'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'PrivacyProtocol'.
     */
    public final String getPrivacyProtocol() {
        return m_privacyProtocol;
    }

    /**
     * Returns the value of field 'proxyHost'. The field 'proxyHost' has the
     * following description: The proxy host to use when communiciating with
     * this agent
     * 
     * @return the value of field 'ProxyHost'.
     */
    public final String getProxyHost() {
        return m_proxyHost;
    }

    /**
     * Returns the value of field 'readCommunity'. The field 'readCommunity' has
     * the following description: Default read community string
     * 
     * @return the value of field 'ReadCommunity'.
     */
    public final String getReadCommunity() {
        return m_readCommunity;
    }

    /**
     * Returns the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries
     * 
     * @return the value of field 'Retry'.
     */
    public final Integer getRetry() {
        return m_retry == null? 0 : m_retry;
    }

    /**
     * Returns the value of field 'securityLevel'. The field 'securityLevel' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'SecurityLevel'.
     */
    public final Integer getSecurityLevel() {
        return m_securityLevel == null? 0 : m_securityLevel;
    }

    /**
     * Returns the value of field 'securityName'. The field 'securityName' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'SecurityName'.
     */
    public final String getSecurityName() {
        return m_securityName;
    }

    /**
     * Returns the value of field 'timeout'. The field 'timeout' has the
     * following description: Default timeout (in milliseconds)
     * 
     * @return the value of field 'Timeout'.
     */
    public final Integer getTimeout() {
        return m_timeout == null? 0 : m_timeout;
    }

    /**
     * Returns the value of field 'version'. The field 'version' has the
     * following description: If set, forces SNMP data collection to the
     * specified version.
     * 
     * @return the value of field 'Version'.
     */
    public final String getVersion() {
        return m_version;
    }

    /**
     * Returns the value of field 'writeCommunity'. The field 'writeCommunity'
     * has the following description: Default write community string
     * 
     * @return the value of field 'WriteCommunity'.
     */
    public final String getWriteCommunity() {
        return m_writeCommunity;
    }

    /**
     * Method hasMaxRepetitions.
     * 
     * @return true if at least one MaxRepetitions has been added
     */
    public boolean hasMaxRepetitions() {
        return m_maxRepetitions != null;
    }

    /**
     * Method hasMaxRequestSize.
     * 
     * @return true if at least one MaxRequestSize has been added
     */
    public boolean hasMaxRequestSize() {
        return m_maxRequestSize != null;
    }

    /**
     * Method hasMaxVarsPerPdu.
     * 
     * @return true if at least one MaxVarsPerPdu has been added
     */
    public boolean hasMaxVarsPerPdu() {
        return m_maxVarsPerPdu != null;
    }

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return m_port != null;
    }

    /**
     * Method hasRetry.
     * 
     * @return true if at least one Retry has been added
     */
    public boolean hasRetry() {
        return m_retry != null;
    }

    /**
     * Method hasSecurityLevel.
     * 
     * @return true if at least one SecurityLevel has been added
     */
    public boolean hasSecurityLevel() {
        return m_securityLevel != null;
    }

    /**
     * Method hasTimeout.
     * 
     * @return true if at least one Timeout has been added
     */
    public boolean hasTimeout() {
        return m_timeout != null;
    }

    /**
     * Sets the value of field 'authPassphrase'. The field 'authPassphrase' has
     * the following description: SNMPv3
     * 
     * @param authPassphrase
     *            the value of field 'authPassphrase'.
     */
    public final void setAuthPassphrase(final String authPassphrase) {
        m_authPassphrase = authPassphrase == null? null : authPassphrase.intern();
    }

    /**
     * Sets the value of field 'authProtocol'. The field 'authProtocol' has the
     * following description: SNMPv3
     * 
     * @param authProtocol
     *            the value of field 'authProtocol'.
     */
    public final void setAuthProtocol(final String authProtocol) {
        m_authProtocol = authProtocol == null? null : authProtocol.intern();
    }

    /**
     * Sets the value of field 'contextEngineId'. The field 'contextEngineId'
     * has the following description: SNMPv3
     * 
     * @param contextEngineId
     *            the value of field 'contextEngineId'.
     */
    public final void setContextEngineId(final String contextEngineId) {
        m_contextEngineId = contextEngineId == null? null : contextEngineId.intern();
    }

    /**
     * Sets the value of field 'contextName'. The field 'contextName' has the
     * following description: SNMPv3
     * 
     * @param contextName
     *            the value of field 'contextName'.
     */
    public final void setContextName(final String contextName) {
        m_contextName = contextName == null? null : contextName.intern();
    }

    /**
     * Sets the value of field 'engineId'. The field 'engineId' has the
     * following description: SNMPv3
     * 
     * @param engineId
     *            the value of field 'engineId'.
     */
    public final void setEngineId(final String engineId) {
        m_engineId = engineId == null? null : engineId.intern();
    }

    /**
     * Sets the value of field 'enterpriseId'. The field 'enterpriseId' has the
     * following description: SNMPv3
     * 
     * @param enterpriseId
     *            the value of field 'enterpriseId'.
     */
    public final void setEnterpriseId(final String enterpriseId) {
        m_enterpriseId = enterpriseId == null? null : enterpriseId.intern();
    }

    /**
     * Sets the value of field 'maxRepetitions'. The field 'maxRepetitions' has
     * the following description: Number of repetitions to send per get-bulk
     * request.
     * 
     * 
     * @param maxRepetitions
     *            the value of field 'maxRepetitions'.
     */
    public final void setMaxRepetitions(final Integer maxRepetitions) {
        m_maxRepetitions = maxRepetitions;
    }

    /**
     * Sets the value of field 'maxRequestSize'. The field 'maxRequestSize' has
     * the following description: (SNMP4J specific) Specifies the maximum number
     * of bytes that may be encoded into an individual SNMP PDU request by
     * Collectd. Provides a means to limit the size of outgoing PDU requests.
     * Default is 65535, must be at least 484.
     * 
     * @param maxRequestSize
     *            the value of field 'maxRequestSize'.
     */
    public final void setMaxRequestSize(final Integer maxRequestSize) {
        m_maxRequestSize = maxRequestSize;
    }

    /**
     * Sets the value of field 'maxVarsPerPdu'. The field 'maxVarsPerPdu' has
     * the following description: Number of variables to send per SNMP request.
     * 
     * 
     * @param maxVarsPerPdu
     *            the value of field 'maxVarsPerPdu'.
     */
    public final void setMaxVarsPerPdu(final Integer maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    /**
     * Sets the value of field 'port'. The field 'port' has the following
     * description: If set, overrides UDP port 161 as the port where SNMP
     * GET/GETNEXT/GETBULK requests are sent.
     * 
     * @param port
     *            the value of field 'port'.
     */
    public final void setPort(final Integer port) {
        m_port = port;
    }

    /**
     * Sets the value of field 'privacyPassphrase'. The field
     * 'privacyPassphrase' has the following description: SNMPv3
     * 
     * @param privacyPassphrase
     *            the value of field 'privacyPassphrase'.
     */
    public final void setPrivacyPassphrase(final String privacyPassphrase) {
        m_privacyPassphrase = privacyPassphrase == null? null : privacyPassphrase.intern();
    }

    /**
     * Sets the value of field 'privacyProtocol'. The field 'privacyProtocol'
     * has the following description: SNMPv3
     * 
     * @param privacyProtocol
     *            the value of field 'privacyProtocol'.
     */
    public final void setPrivacyProtocol(final String privacyProtocol) {
        m_privacyProtocol = privacyProtocol == null? null : privacyProtocol.intern();
    }

    /**
     * Sets the value of field 'proxyHost'. The field 'proxyHost' has the
     * following description: The proxy host to use when communiciating with
     * this agent
     * 
     * @param proxyHost
     *            the value of field 'proxyHost'.
     */
    public final void setProxyHost(final String proxyHost) {
        m_proxyHost = proxyHost == null? null : proxyHost.intern();
    }

    /**
     * Sets the value of field 'readCommunity'. The field 'readCommunity' has
     * the following description: Default read community string
     * 
     * @param readCommunity
     *            the value of field 'readCommunity'.
     */
    public final void setReadCommunity(final String readCommunity) {
        m_readCommunity = readCommunity == null? null : readCommunity.intern();
    }

    /**
     * Sets the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries
     * 
     * @param retry
     *            the value of field 'retry'.
     */
    public final void setRetry(final Integer retry) {
        m_retry = retry;
    }

    /**
     * Sets the value of field 'securityLevel'. The field 'securityLevel' has
     * the following description: SNMPv3
     * 
     * @param securityLevel
     *            the value of field 'securityLevel'.
     */
    public final void setSecurityLevel(final Integer securityLevel) {
        m_securityLevel = securityLevel;
    }

    /**
     * Sets the value of field 'securityName'. The field 'securityName' has the
     * following description: SNMPv3
     * 
     * @param securityName
     *            the value of field 'securityName'.
     */
    public final void setSecurityName(final String securityName) {
        m_securityName = securityName == null? null : securityName.intern();
    }

    /**
     * Sets the value of field 'timeout'. The field 'timeout' has the following
     * description: Default timeout (in milliseconds)
     * 
     * @param timeout
     *            the value of field 'timeout'.
     */
    public final void setTimeout(final Integer timeout) {
        m_timeout = timeout;
    }

    /**
     * Sets the value of field 'version'. The field 'version' has the following
     * description: If set, forces SNMP data collection to the specified
     * version.
     * 
     * @param version
     *            the value of field 'version'.
     */
    public final void setVersion(final String version) {
        m_version = version == null? null : version.intern();
    }

    /**
     * Sets the value of field 'writeCommunity'. The field 'writeCommunity' has
     * the following description: Default write community string
     * 
     * @param writeCommunity
     *            the value of field 'writeCommunity'.
     */
    public final void setWriteCommunity(final String writeCommunity) {
        m_writeCommunity = writeCommunity == null? null : writeCommunity.intern();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_authPassphrase == null) ? 0 : m_authPassphrase.hashCode());
        result = prime * result + ((m_authProtocol == null) ? 0 : m_authProtocol.hashCode());
        result = prime * result + ((m_contextEngineId == null) ? 0 : m_contextEngineId.hashCode());
        result = prime * result + ((m_contextName == null) ? 0 : m_contextName.hashCode());
        result = prime * result + ((m_engineId == null) ? 0 : m_engineId.hashCode());
        result = prime * result + ((m_enterpriseId == null) ? 0 : m_enterpriseId.hashCode());
        result = prime * result + ((m_maxRepetitions == null) ? 0 : m_maxRepetitions.hashCode());
        result = prime * result + ((m_maxRequestSize == null) ? 0 : m_maxRequestSize.hashCode());
        result = prime * result + ((m_maxVarsPerPdu == null) ? 0 : m_maxVarsPerPdu.hashCode());
        result = prime * result + ((m_port == null) ? 0 : m_port.hashCode());
        result = prime * result + ((m_privacyPassphrase == null) ? 0 : m_privacyPassphrase.hashCode());
        result = prime * result + ((m_privacyProtocol == null) ? 0 : m_privacyProtocol.hashCode());
        result = prime * result + ((m_proxyHost == null) ? 0 : m_proxyHost.hashCode());
        result = prime * result + ((m_readCommunity == null) ? 0 : m_readCommunity.hashCode());
        result = prime * result + ((m_retry == null) ? 0 : m_retry.hashCode());
        result = prime * result + ((m_securityLevel == null) ? 0 : m_securityLevel.hashCode());
        result = prime * result + ((m_securityName == null) ? 0 : m_securityName.hashCode());
        result = prime * result + ((m_timeout == null) ? 0 : m_timeout.hashCode());
        result = prime * result + ((m_version == null) ? 0 : m_version.hashCode());
        result = prime * result + ((m_writeCommunity == null) ? 0 : m_writeCommunity.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Configuration)) {
            return false;
        }
        final Configuration other = (Configuration) obj;
        if (m_authPassphrase == null) {
            if (other.m_authPassphrase != null) {
                return false;
            }
        } else if (!m_authPassphrase.equals(other.m_authPassphrase)) {
            return false;
        }
        if (m_authProtocol == null) {
            if (other.m_authProtocol != null) {
                return false;
            }
        } else if (!m_authProtocol.equals(other.m_authProtocol)) {
            return false;
        }
        if (m_contextEngineId == null) {
            if (other.m_contextEngineId != null) {
                return false;
            }
        } else if (!m_contextEngineId.equals(other.m_contextEngineId)) {
            return false;
        }
        if (m_contextName == null) {
            if (other.m_contextName != null) {
                return false;
            }
        } else if (!m_contextName.equals(other.m_contextName)) {
            return false;
        }
        if (m_engineId == null) {
            if (other.m_engineId != null) {
                return false;
            }
        } else if (!m_engineId.equals(other.m_engineId)) {
            return false;
        }
        if (m_enterpriseId == null) {
            if (other.m_enterpriseId != null) {
                return false;
            }
        } else if (!m_enterpriseId.equals(other.m_enterpriseId)) {
            return false;
        }
        if (m_maxRepetitions == null) {
            if (other.m_maxRepetitions != null) {
                return false;
            }
        } else if (!m_maxRepetitions.equals(other.m_maxRepetitions)) {
            return false;
        }
        if (m_maxRequestSize == null) {
            if (other.m_maxRequestSize != null) {
                return false;
            }
        } else if (!m_maxRequestSize.equals(other.m_maxRequestSize)) {
            return false;
        }
        if (m_maxVarsPerPdu == null) {
            if (other.m_maxVarsPerPdu != null) {
                return false;
            }
        } else if (!m_maxVarsPerPdu.equals(other.m_maxVarsPerPdu)) {
            return false;
        }
        if (m_port == null) {
            if (other.m_port != null) {
                return false;
            }
        } else if (!m_port.equals(other.m_port)) {
            return false;
        }
        if (m_privacyPassphrase == null) {
            if (other.m_privacyPassphrase != null) {
                return false;
            }
        } else if (!m_privacyPassphrase.equals(other.m_privacyPassphrase)) {
            return false;
        }
        if (m_privacyProtocol == null) {
            if (other.m_privacyProtocol != null) {
                return false;
            }
        } else if (!m_privacyProtocol.equals(other.m_privacyProtocol)) {
            return false;
        }
        if (m_proxyHost == null) {
            if (other.m_proxyHost != null) {
                return false;
            }
        } else if (!m_proxyHost.equals(other.m_proxyHost)) {
            return false;
        }
        if (m_readCommunity == null) {
            if (other.m_readCommunity != null) {
                return false;
            }
        } else if (!m_readCommunity.equals(other.m_readCommunity)) {
            return false;
        }
        if (m_retry == null) {
            if (other.m_retry != null) {
                return false;
            }
        } else if (!m_retry.equals(other.m_retry)) {
            return false;
        }
        if (m_securityLevel == null) {
            if (other.m_securityLevel != null) {
                return false;
            }
        } else if (!m_securityLevel.equals(other.m_securityLevel)) {
            return false;
        }
        if (m_securityName == null) {
            if (other.m_securityName != null) {
                return false;
            }
        } else if (!m_securityName.equals(other.m_securityName)) {
            return false;
        }
        if (m_timeout == null) {
            if (other.m_timeout != null) {
                return false;
            }
        } else if (!m_timeout.equals(other.m_timeout)) {
            return false;
        }
        if (m_version == null) {
            if (other.m_version != null) {
                return false;
            }
        } else if (!m_version.equals(other.m_version)) {
            return false;
        }
        if (m_writeCommunity == null) {
            if (other.m_writeCommunity != null) {
                return false;
            }
        } else if (!m_writeCommunity.equals(other.m_writeCommunity)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Configuration [proxyHost=" + m_proxyHost + ", maxVarsPerPdu=" + m_maxVarsPerPdu + ", maxRepetitions=" + m_maxRepetitions + ", maxRequestSize=" + m_maxRequestSize + ", securityName="
                + m_securityName + ", securityLevel=" + m_securityLevel + ", authPassphrase=" + m_authPassphrase + ", authProtocol=" + m_authProtocol + ", engineId=" + m_engineId
                + ", contextEngineId=" + m_contextEngineId + ", contextName=" + m_contextName + ", privacyPassphrase=" + m_privacyPassphrase + ", privacyProtocol=" + m_privacyProtocol
                + ", enterpriseId=" + m_enterpriseId + ", version=" + m_version + ", writeCommunity=" + m_writeCommunity + ", readCommunity=" + m_readCommunity + ", timeout=" + m_timeout
                + ", retry=" + m_retry + ", port=" + m_port + "]";
    }

}
