/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.snmp;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration implements Serializable {
    private static final long serialVersionUID = -8779120716708528358L;

    /**
     * The proxy host to use when communicating with this agent
     */
    @XmlAttribute(name="proxy-host")
    private String _proxyHost;

    /**
     * Number of variables to send per SNMP request.
     * 
     */
    @XmlAttribute(name="max-vars-per-pdu")
    private Integer _maxVarsPerPdu;

    /**
     * Number of repetitions to send per get-bulk request.
     * 
     */
    @XmlAttribute(name="max-repetitions")
    private Integer _maxRepetitions;

    /**
     * (SNMP4J specific) Specifies the maximum number of bytes that may be
     * encoded into an individual SNMP PDU request by Collectd. Provides a means
     * to limit the size of outgoing PDU requests. Default is 65535, must be at
     * least 484.
     */
    @XmlAttribute(name="max-request-size")
    private Integer _maxRequestSize;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="security-name")
    private String _securityName;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="security-level")
    private Integer _securityLevel;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="auth-passphrase")
    private String _authPassphrase;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="auth-protocol")
    private String _authProtocol;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="engine-id")
    private String _engineId;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="context-engine-id")
    private String _contextEngineId;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="context-name")
    private String _contextName;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="privacy-passphrase")
    private String _privacyPassphrase;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="privacy-protocol")
    private String _privacyProtocol;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="enterprise-id")
    private String _enterpriseId;

    /**
     * If set, forces SNMP data collection to the specified version.
     */
    @XmlAttribute(name="version")
    private String _version;

    /**
     * Default write community string
     */
    @XmlAttribute(name="write-community")
    private String _writeCommunity;

    /**
     * Default read community string
     */
    @XmlAttribute(name="read-community")
    private String _readCommunity;

    /**
     * Default timeout (in milliseconds)
     */
    @XmlAttribute(name="timeout")
    private Integer _timeout;

    /**
     * Default number of retries
     */
    @XmlAttribute(name="retry")
    private Integer _retry;

    /**
     * If set, overrides UDP port 161 as the port where SNMP GET/GETNEXT/GETBULK
     * requests are sent.
     */
    @XmlAttribute(name="port")
    private Integer _port;



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
        _maxRepetitions = null;
    }

    public void deleteMaxRequestSize() {
        _maxRequestSize = null;
    }

    public void deleteMaxVarsPerPdu() {
        _maxVarsPerPdu = null;
    }

    public void deletePort() {
        _port = null;
    }

    public void deleteRetry() {
        _retry = null;
    }

    public void deleteSecurityLevel() {
        _securityLevel = null;
    }

    public void deleteTimeout() {
        _timeout = null;
    }


    /**
     * Returns the value of field 'authPassphrase'. The field 'authPassphrase'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'AuthPassphrase'.
     */
    public final String getAuthPassphrase() {
        return _authPassphrase;
    }

    /**
     * Returns the value of field 'authProtocol'. The field 'authProtocol' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'AuthProtocol'.
     */
    public final String getAuthProtocol() {
        return _authProtocol;
    }

    /**
     * Returns the value of field 'contextEngineId'. The field 'contextEngineId'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'ContextEngineId'.
     */
    public final String getContextEngineId() {
        return _contextEngineId;
    }

    /**
     * Returns the value of field 'contextName'. The field 'contextName' has the
     * following description: SNMPv3
     * 
     * @return the value of field 'ContextName'.
     */
    public final String getContextName() {
        return _contextName;
    }

    /**
     * Returns the value of field 'engineId'. The field 'engineId' has the
     * following description: SNMPv3
     * 
     * @return the value of field 'EngineId'.
     */
    public final String getEngineId() {
        return _engineId;
    }

    /**
     * Returns the value of field 'enterpriseId'. The field 'enterpriseId' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'EnterpriseId'.
     */
    public final String getEnterpriseId() {
        return _enterpriseId;
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
        return _maxRepetitions == null? 2 : _maxRepetitions;
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
        return _maxRequestSize == null? 65535 : _maxRequestSize;
    }

    /**
     * Returns the value of field 'maxVarsPerPdu'. The field 'maxVarsPerPdu' has
     * the following description: Number of variables to send per SNMP request.
     * 
     * 
     * @return the value of field 'MaxVarsPerPdu'.
     */
    public final Integer getMaxVarsPerPdu() {
        return _maxVarsPerPdu == null? 10 : _maxVarsPerPdu;
    }

    /**
     * Returns the value of field 'port'. The field 'port' has the following
     * description: If set, overrides UDP port 161 as the port where SNMP
     * GET/GETNEXT/GETBULK requests are sent.
     * 
     * @return the value of field 'Port'.
     */
    public final Integer getPort() {
        return _port == null? 0 : _port;
    }

    /**
     * Returns the value of field 'privacyPassphrase'. The field
     * 'privacyPassphrase' has the following description: SNMPv3
     * 
     * @return the value of field 'PrivacyPassphrase'.
     */
    public final String getPrivacyPassphrase() {
        return _privacyPassphrase;
    }

    /**
     * Returns the value of field 'privacyProtocol'. The field 'privacyProtocol'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'PrivacyProtocol'.
     */
    public final String getPrivacyProtocol() {
        return _privacyProtocol;
    }

    /**
     * Returns the value of field 'proxyHost'. The field 'proxyHost' has the
     * following description: The proxy host to use when communiciating with
     * this agent
     * 
     * @return the value of field 'ProxyHost'.
     */
    public final String getProxyHost() {
        return _proxyHost;
    }

    /**
     * Returns the value of field 'readCommunity'. The field 'readCommunity' has
     * the following description: Default read community string
     * 
     * @return the value of field 'ReadCommunity'.
     */
    public final String getReadCommunity() {
        return _readCommunity;
    }

    /**
     * Returns the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries
     * 
     * @return the value of field 'Retry'.
     */
    public final Integer getRetry() {
        return _retry == null? 0 : _retry;
    }

    /**
     * Returns the value of field 'securityLevel'. The field 'securityLevel' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'SecurityLevel'.
     */
    public final Integer getSecurityLevel() {
        return _securityLevel == null? 0 : _securityLevel;
    }

    /**
     * Returns the value of field 'securityName'. The field 'securityName' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'SecurityName'.
     */
    public final String getSecurityName() {
        return _securityName;
    }

    /**
     * Returns the value of field 'timeout'. The field 'timeout' has the
     * following description: Default timeout (in milliseconds)
     * 
     * @return the value of field 'Timeout'.
     */
    public final Integer getTimeout() {
        return _timeout == null? 0 : _timeout;
    }

    /**
     * Returns the value of field 'version'. The field 'version' has the
     * following description: If set, forces SNMP data collection to the
     * specified version.
     * 
     * @return the value of field 'Version'.
     */
    public final String getVersion() {
        return _version;
    }

    /**
     * Returns the value of field 'writeCommunity'. The field 'writeCommunity'
     * has the following description: Default write community string
     * 
     * @return the value of field 'WriteCommunity'.
     */
    public final String getWriteCommunity() {
        return _writeCommunity;
    }

    /**
     * Method hasMaxRepetitions.
     * 
     * @return true if at least one MaxRepetitions has been added
     */
    public boolean hasMaxRepetitions() {
        return _maxRepetitions != null;
    }

    /**
     * Method hasMaxRequestSize.
     * 
     * @return true if at least one MaxRequestSize has been added
     */
    public boolean hasMaxRequestSize() {
        return _maxRequestSize != null;
    }

    /**
     * Method hasMaxVarsPerPdu.
     * 
     * @return true if at least one MaxVarsPerPdu has been added
     */
    public boolean hasMaxVarsPerPdu() {
        return _maxVarsPerPdu != null;
    }

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return _port != null;
    }

    /**
     * Method hasRetry.
     * 
     * @return true if at least one Retry has been added
     */
    public boolean hasRetry() {
        return _retry != null;
    }

    /**
     * Method hasSecurityLevel.
     * 
     * @return true if at least one SecurityLevel has been added
     */
    public boolean hasSecurityLevel() {
        return _securityLevel != null;
    }

    /**
     * Method hasTimeout.
     * 
     * @return true if at least one Timeout has been added
     */
    public boolean hasTimeout() {
        return _timeout != null;
    }

    /**
     * Sets the value of field 'authPassphrase'. The field 'authPassphrase' has
     * the following description: SNMPv3
     * 
     * @param authPassphrase
     *            the value of field 'authPassphrase'.
     */
    public final void setAuthPassphrase(final String authPassphrase) {
        _authPassphrase = authPassphrase;
    }

    /**
     * Sets the value of field 'authProtocol'. The field 'authProtocol' has the
     * following description: SNMPv3
     * 
     * @param authProtocol
     *            the value of field 'authProtocol'.
     */
    public final void setAuthProtocol(final String authProtocol) {
        _authProtocol = authProtocol;
    }

    /**
     * Sets the value of field 'contextEngineId'. The field 'contextEngineId'
     * has the following description: SNMPv3
     * 
     * @param contextEngineId
     *            the value of field 'contextEngineId'.
     */
    public final void setContextEngineId(final String contextEngineId) {
        _contextEngineId = contextEngineId;
    }

    /**
     * Sets the value of field 'contextName'. The field 'contextName' has the
     * following description: SNMPv3
     * 
     * @param contextName
     *            the value of field 'contextName'.
     */
    public final void setContextName(final String contextName) {
        _contextName = contextName;
    }

    /**
     * Sets the value of field 'engineId'. The field 'engineId' has the
     * following description: SNMPv3
     * 
     * @param engineId
     *            the value of field 'engineId'.
     */
    public final void setEngineId(final String engineId) {
        _engineId = engineId;
    }

    /**
     * Sets the value of field 'enterpriseId'. The field 'enterpriseId' has the
     * following description: SNMPv3
     * 
     * @param enterpriseId
     *            the value of field 'enterpriseId'.
     */
    public final void setEnterpriseId(final String enterpriseId) {
        _enterpriseId = enterpriseId;
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
        _maxRepetitions = maxRepetitions;
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
        _maxRequestSize = maxRequestSize;
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
        _maxVarsPerPdu = maxVarsPerPdu;
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
        _port = port;
    }

    /**
     * Sets the value of field 'privacyPassphrase'. The field
     * 'privacyPassphrase' has the following description: SNMPv3
     * 
     * @param privacyPassphrase
     *            the value of field 'privacyPassphrase'.
     */
    public final void setPrivacyPassphrase(final String privacyPassphrase) {
        _privacyPassphrase = privacyPassphrase;
    }

    /**
     * Sets the value of field 'privacyProtocol'. The field 'privacyProtocol'
     * has the following description: SNMPv3
     * 
     * @param privacyProtocol
     *            the value of field 'privacyProtocol'.
     */
    public final void setPrivacyProtocol(final String privacyProtocol) {
        _privacyProtocol = privacyProtocol;
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
        _proxyHost = proxyHost;
    }

    /**
     * Sets the value of field 'readCommunity'. The field 'readCommunity' has
     * the following description: Default read community string
     * 
     * @param readCommunity
     *            the value of field 'readCommunity'.
     */
    public final void setReadCommunity(final String readCommunity) {
        _readCommunity = readCommunity;
    }

    /**
     * Sets the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries
     * 
     * @param retry
     *            the value of field 'retry'.
     */
    public final void setRetry(final Integer retry) {
        _retry = retry;
    }

    /**
     * Sets the value of field 'securityLevel'. The field 'securityLevel' has
     * the following description: SNMPv3
     * 
     * @param securityLevel
     *            the value of field 'securityLevel'.
     */
    public final void setSecurityLevel(final Integer securityLevel) {
        _securityLevel = securityLevel;
    }

    /**
     * Sets the value of field 'securityName'. The field 'securityName' has the
     * following description: SNMPv3
     * 
     * @param securityName
     *            the value of field 'securityName'.
     */
    public final void setSecurityName(final String securityName) {
        _securityName = securityName;
    }

    /**
     * Sets the value of field 'timeout'. The field 'timeout' has the following
     * description: Default timeout (in milliseconds)
     * 
     * @param timeout
     *            the value of field 'timeout'.
     */
    public final void setTimeout(final Integer timeout) {
        _timeout = timeout;
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
        _version = version;
    }

    /**
     * Sets the value of field 'writeCommunity'. The field 'writeCommunity' has
     * the following description: Default write community string
     * 
     * @param writeCommunity
     *            the value of field 'writeCommunity'.
     */
    public final void setWriteCommunity(final String writeCommunity) {
        _writeCommunity = writeCommunity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_authPassphrase == null) ? 0 : _authPassphrase.hashCode());
        result = prime * result + ((_authProtocol == null) ? 0 : _authProtocol.hashCode());
        result = prime * result + ((_contextEngineId == null) ? 0 : _contextEngineId.hashCode());
        result = prime * result + ((_contextName == null) ? 0 : _contextName.hashCode());
        result = prime * result + ((_engineId == null) ? 0 : _engineId.hashCode());
        result = prime * result + ((_enterpriseId == null) ? 0 : _enterpriseId.hashCode());
        result = prime * result + ((_maxRepetitions == null) ? 0 : _maxRepetitions.hashCode());
        result = prime * result + ((_maxRequestSize == null) ? 0 : _maxRequestSize.hashCode());
        result = prime * result + ((_maxVarsPerPdu == null) ? 0 : _maxVarsPerPdu.hashCode());
        result = prime * result + ((_port == null) ? 0 : _port.hashCode());
        result = prime * result + ((_privacyPassphrase == null) ? 0 : _privacyPassphrase.hashCode());
        result = prime * result + ((_privacyProtocol == null) ? 0 : _privacyProtocol.hashCode());
        result = prime * result + ((_proxyHost == null) ? 0 : _proxyHost.hashCode());
        result = prime * result + ((_readCommunity == null) ? 0 : _readCommunity.hashCode());
        result = prime * result + ((_retry == null) ? 0 : _retry.hashCode());
        result = prime * result + ((_securityLevel == null) ? 0 : _securityLevel.hashCode());
        result = prime * result + ((_securityName == null) ? 0 : _securityName.hashCode());
        result = prime * result + ((_timeout == null) ? 0 : _timeout.hashCode());
        result = prime * result + ((_version == null) ? 0 : _version.hashCode());
        result = prime * result + ((_writeCommunity == null) ? 0 : _writeCommunity.hashCode());
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
        if (_authPassphrase == null) {
            if (other._authPassphrase != null) {
                return false;
            }
        } else if (!_authPassphrase.equals(other._authPassphrase)) {
            return false;
        }
        if (_authProtocol == null) {
            if (other._authProtocol != null) {
                return false;
            }
        } else if (!_authProtocol.equals(other._authProtocol)) {
            return false;
        }
        if (_contextEngineId == null) {
            if (other._contextEngineId != null) {
                return false;
            }
        } else if (!_contextEngineId.equals(other._contextEngineId)) {
            return false;
        }
        if (_contextName == null) {
            if (other._contextName != null) {
                return false;
            }
        } else if (!_contextName.equals(other._contextName)) {
            return false;
        }
        if (_engineId == null) {
            if (other._engineId != null) {
                return false;
            }
        } else if (!_engineId.equals(other._engineId)) {
            return false;
        }
        if (_enterpriseId == null) {
            if (other._enterpriseId != null) {
                return false;
            }
        } else if (!_enterpriseId.equals(other._enterpriseId)) {
            return false;
        }
        if (_maxRepetitions == null) {
            if (other._maxRepetitions != null) {
                return false;
            }
        } else if (!_maxRepetitions.equals(other._maxRepetitions)) {
            return false;
        }
        if (_maxRequestSize == null) {
            if (other._maxRequestSize != null) {
                return false;
            }
        } else if (!_maxRequestSize.equals(other._maxRequestSize)) {
            return false;
        }
        if (_maxVarsPerPdu == null) {
            if (other._maxVarsPerPdu != null) {
                return false;
            }
        } else if (!_maxVarsPerPdu.equals(other._maxVarsPerPdu)) {
            return false;
        }
        if (_port == null) {
            if (other._port != null) {
                return false;
            }
        } else if (!_port.equals(other._port)) {
            return false;
        }
        if (_privacyPassphrase == null) {
            if (other._privacyPassphrase != null) {
                return false;
            }
        } else if (!_privacyPassphrase.equals(other._privacyPassphrase)) {
            return false;
        }
        if (_privacyProtocol == null) {
            if (other._privacyProtocol != null) {
                return false;
            }
        } else if (!_privacyProtocol.equals(other._privacyProtocol)) {
            return false;
        }
        if (_proxyHost == null) {
            if (other._proxyHost != null) {
                return false;
            }
        } else if (!_proxyHost.equals(other._proxyHost)) {
            return false;
        }
        if (_readCommunity == null) {
            if (other._readCommunity != null) {
                return false;
            }
        } else if (!_readCommunity.equals(other._readCommunity)) {
            return false;
        }
        if (_retry == null) {
            if (other._retry != null) {
                return false;
            }
        } else if (!_retry.equals(other._retry)) {
            return false;
        }
        if (_securityLevel == null) {
            if (other._securityLevel != null) {
                return false;
            }
        } else if (!_securityLevel.equals(other._securityLevel)) {
            return false;
        }
        if (_securityName == null) {
            if (other._securityName != null) {
                return false;
            }
        } else if (!_securityName.equals(other._securityName)) {
            return false;
        }
        if (_timeout == null) {
            if (other._timeout != null) {
                return false;
            }
        } else if (!_timeout.equals(other._timeout)) {
            return false;
        }
        if (_version == null) {
            if (other._version != null) {
                return false;
            }
        } else if (!_version.equals(other._version)) {
            return false;
        }
        if (_writeCommunity == null) {
            if (other._writeCommunity != null) {
                return false;
            }
        } else if (!_writeCommunity.equals(other._writeCommunity)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Configuration [proxyHost=" + _proxyHost + ", maxVarsPerPdu=" + _maxVarsPerPdu + ", maxRepetitions=" + _maxRepetitions + ", maxRequestSize=" + _maxRequestSize + ", securityName="
                + _securityName + ", securityLevel=" + _securityLevel + ", authPassphrase=" + _authPassphrase + ", authProtocol=" + _authProtocol + ", engineId=" + _engineId
                + ", contextEngineId=" + _contextEngineId + ", contextName=" + _contextName + ", privacyPassphrase=" + _privacyPassphrase + ", privacyProtocol=" + _privacyProtocol
                + ", enterpriseId=" + _enterpriseId + ", version=" + _version + ", writeCommunity=" + _writeCommunity + ", readCommunity=" + _readCommunity + ", timeout=" + _timeout
                + ", retry=" + _retry + ", port=" + _port + "]";
    }

}
