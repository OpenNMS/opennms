/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
    private String proxyHost;

    /**
     * Number of variables to send per SNMP request.
     */
    @XmlAttribute(name="max-vars-per-pdu")
    private Integer maxVarsPerPdu;

    /**
     * Number of repetitions to send per get-bulk request.
     */
    @XmlAttribute(name="max-repetitions")
    private Integer maxRepetitions;

    /**
     * (SNMP4J specific) Specifies the maximum number of bytes that may be
     * encoded into an individual SNMP PDU request by Collectd. Provides a
     * means to limit the size of outgoing PDU requests. Default is 65535,
     * must be at least 484.
     */
    @XmlAttribute(name="max-request-size")
    private Integer maxRequestSize;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="security-name")
    private String securityName;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="security-level")
    private Integer securityLevel;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="auth-passphrase")
    private String authPassphrase;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="auth-protocol")
    private String authProtocol;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="engine-id")
    private String engineId;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="context-engine-id")
    private String contextEngineId;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="context-name")
    private String contextName;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="privacy-passphrase")
    private String privacyPassphrase;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="privacy-protocol")
    private String privacyProtocol;

    /**
     * SNMPv3
     */
    @XmlAttribute(name="enterprise-id")
    private String enterpriseId;

    /**
     * If set, forces SNMP data collection to the specified version.
     */
    @XmlAttribute(name="version")
    private String version;

    /**
     * Default write community string
     */
    @XmlAttribute(name="write-community")
    private String writeCommunity;

    /**
     * Default read community string
     */
    @XmlAttribute(name="read-community")
    private String readCommunity;

    /**
     * Default timeout (in milliseconds)
     */
    @XmlAttribute(name="timeout")
    private Integer timeout;

    /**
     * Default number of retries
     */
    @XmlAttribute(name="retry")
    private Integer retry;

    /**
     * If set, overrides UDP port 161 as the port where SNMP GET/GETNEXT/GETBULK
     * requests are sent.
     */
    @XmlAttribute(name="port")
    private Integer port;


    @XmlAttribute(name="ttl")
    private Long ttl;


    @XmlAttribute(name = "encrypted")
    private Boolean encrypted;

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
        maxRepetitions = null;
    }

    public void deleteMaxRequestSize() {
        maxRequestSize = null;
    }

    public void deleteMaxVarsPerPdu() {
        maxVarsPerPdu = null;
    }

    public void deletePort() {
        port = null;
    }

    public void deleteRetry() {
        retry = null;
    }

    public void deleteSecurityLevel() {
        securityLevel = null;
    }

    public void deleteTimeout() {
        timeout = null;
    }


    /**
     * Returns the value of field 'authPassphrase'. The field 'authPassphrase'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'AuthPassphrase'.
     */
    public final String getAuthPassphrase() {
        return authPassphrase;
    }

    /**
     * Returns the value of field 'authProtocol'. The field 'authProtocol' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'AuthProtocol'.
     */
    public final String getAuthProtocol() {
        return authProtocol;
    }

    /**
     * Returns the value of field 'contextEngineId'. The field 'contextEngineId'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'ContextEngineId'.
     */
    public final String getContextEngineId() {
        return contextEngineId;
    }

    /**
     * Returns the value of field 'contextName'. The field 'contextName' has the
     * following description: SNMPv3
     * 
     * @return the value of field 'ContextName'.
     */
    public final String getContextName() {
        return contextName;
    }

    /**
     * Returns the value of field 'engineId'. The field 'engineId' has the
     * following description: SNMPv3
     * 
     * @return the value of field 'EngineId'.
     */
    public final String getEngineId() {
        return engineId;
    }

    /**
     * Returns the value of field 'enterpriseId'. The field 'enterpriseId' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'EnterpriseId'.
     */
    public final String getEnterpriseId() {
        return enterpriseId;
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
        return maxRepetitions == null? 2 : maxRepetitions;
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
        return maxRequestSize == null? 65535 : maxRequestSize;
    }

    /**
     * Returns the value of field 'maxVarsPerPdu'. The field 'maxVarsPerPdu' has
     * the following description: Number of variables to send per SNMP request.
     * 
     * 
     * @return the value of field 'MaxVarsPerPdu'.
     */
    public final Integer getMaxVarsPerPdu() {
        return maxVarsPerPdu == null? 10 : maxVarsPerPdu;
    }

    /**
     * Returns the value of field 'port'. The field 'port' has the following
     * description: If set, overrides UDP port 161 as the port where SNMP
     * GET/GETNEXT/GETBULK requests are sent.
     * 
     * @return the value of field 'Port'.
     */
    public final Integer getPort() {
        return port == null? 161 : port;
    }

    /**
     * Returns the value of field 'privacyPassphrase'. The field
     * 'privacyPassphrase' has the following description: SNMPv3
     * 
     * @return the value of field 'PrivacyPassphrase'.
     */
    public final String getPrivacyPassphrase() {
        return privacyPassphrase;
    }

    /**
     * Returns the value of field 'privacyProtocol'. The field 'privacyProtocol'
     * has the following description: SNMPv3
     * 
     * @return the value of field 'PrivacyProtocol'.
     */
    public final String getPrivacyProtocol() {
        return privacyProtocol;
    }

    /**
     * Returns the value of field 'proxyHost'. The field 'proxyHost' has the
     * following description: The proxy host to use when communiciating with
     * this agent
     * 
     * @return the value of field 'ProxyHost'.
     */
    public final String getProxyHost() {
        return proxyHost;
    }

    /**
     * Returns the value of field 'readCommunity'. The field 'readCommunity' has
     * the following description: Default read community string
     * 
     * @return the value of field 'ReadCommunity'.
     */
    public final String getReadCommunity() {
        return readCommunity;
    }

    /**
     * Returns the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries
     * 
     * @return the value of field 'Retry'.
     */
    public final Integer getRetry() {
        return retry == null? 0 : retry;
    }

    /**
     * Returns the value of field 'securityLevel'. The field 'securityLevel' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'SecurityLevel'.
     */
    public final Integer getSecurityLevel() {
        return securityLevel == null? 0 : securityLevel;
    }

    /**
     * Returns the value of field 'securityName'. The field 'securityName' has
     * the following description: SNMPv3
     * 
     * @return the value of field 'SecurityName'.
     */
    public final String getSecurityName() {
        return securityName;
    }

    /**
     * Returns the value of field 'timeout'. The field 'timeout' has the
     * following description: Default timeout (in milliseconds)
     * 
     * @return the value of field 'Timeout'.
     */
    public final Integer getTimeout() {
        return timeout == null? 0 : timeout;
    }

    /**
     * Returns the value of field 'version'. The field 'version' has the
     * following description: If set, forces SNMP data collection to the
     * specified version.
     * 
     * @return the value of field 'Version'.
     */
    public final String getVersion() {
        return version;
    }

    /**
     * Returns the value of field 'writeCommunity'. The field 'writeCommunity'
     * has the following description: Default write community string
     * 
     * @return the value of field 'WriteCommunity'.
     */
    public final String getWriteCommunity() {
        return writeCommunity;
    }

    /**
     * Method hasMaxRepetitions.
     * 
     * @return true if at least one MaxRepetitions has been added
     */
    public boolean hasMaxRepetitions() {
        return maxRepetitions != null;
    }

    /**
     * Method hasMaxRequestSize.
     * 
     * @return true if at least one MaxRequestSize has been added
     */
    public boolean hasMaxRequestSize() {
        return maxRequestSize != null;
    }

    /**
     * Method hasMaxVarsPerPdu.
     * 
     * @return true if at least one MaxVarsPerPdu has been added
     */
    public boolean hasMaxVarsPerPdu() {
        return maxVarsPerPdu != null;
    }

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return port != null;
    }

    /**
     * Method hasRetry.
     * 
     * @return true if at least one Retry has been added
     */
    public boolean hasRetry() {
        return retry != null;
    }

    /**
     * Method hasSecurityLevel.
     * 
     * @return true if at least one SecurityLevel has been added
     */
    public boolean hasSecurityLevel() {
        return securityLevel != null;
    }

    /**
     * Method hasTimeout.
     * 
     * @return true if at least one Timeout has been added
     */
    public boolean hasTimeout() {
        return timeout != null;
    }


    /**
     *
     * @return true if there is ttl defined
     */
    public boolean hasTTL() {
        return ttl != null;
    }

    /**
     * Sets the value of field 'authPassphrase'. The field 'authPassphrase' has
     * the following description: SNMPv3
     * 
     * @param authPassphrase
     *            the value of field 'authPassphrase'.
     */
    public final void setAuthPassphrase(final String authPassphrase) {
        this.authPassphrase = authPassphrase == null? null : authPassphrase.intern();
    }

    /**
     * Sets the value of field 'authProtocol'. The field 'authProtocol' has the
     * following description: SNMPv3
     * 
     * @param authProtocol
     *            the value of field 'authProtocol'.
     */
    public final void setAuthProtocol(final String authProtocol) {
        this.authProtocol = authProtocol == null? null : authProtocol.intern();
    }

    /**
     * Sets the value of field 'contextEngineId'. The field 'contextEngineId'
     * has the following description: SNMPv3
     * 
     * @param contextEngineId
     *            the value of field 'contextEngineId'.
     */
    public final void setContextEngineId(final String contextEngineId) {
        this.contextEngineId = contextEngineId == null? null : contextEngineId.intern();
    }

    /**
     * Sets the value of field 'contextName'. The field 'contextName' has the
     * following description: SNMPv3
     * 
     * @param contextName
     *            the value of field 'contextName'.
     */
    public final void setContextName(final String contextName) {
        this.contextName = contextName == null? null : contextName.intern();
    }

    /**
     * Sets the value of field 'engineId'. The field 'engineId' has the
     * following description: SNMPv3
     * 
     * @param engineId
     *            the value of field 'engineId'.
     */
    public final void setEngineId(final String engineId) {
        this.engineId = engineId == null? null : engineId.intern();
    }

    /**
     * Sets the value of field 'enterpriseId'. The field 'enterpriseId' has the
     * following description: SNMPv3
     * 
     * @param enterpriseId
     *            the value of field 'enterpriseId'.
     */
    public final void setEnterpriseId(final String enterpriseId) {
        this.enterpriseId = enterpriseId == null? null : enterpriseId.intern();
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
        this.maxRepetitions = maxRepetitions;
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
        this.maxRequestSize = maxRequestSize;
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
        this.maxVarsPerPdu = maxVarsPerPdu;
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
        this.port = port;
    }

    /**
     * Sets the value of field 'privacyPassphrase'. The field
     * 'privacyPassphrase' has the following description: SNMPv3
     * 
     * @param privacyPassphrase
     *            the value of field 'privacyPassphrase'.
     */
    public final void setPrivacyPassphrase(final String privacyPassphrase) {
        this.privacyPassphrase = privacyPassphrase == null? null : privacyPassphrase.intern();
    }

    /**
     * Sets the value of field 'privacyProtocol'. The field 'privacyProtocol'
     * has the following description: SNMPv3
     * 
     * @param privacyProtocol
     *            the value of field 'privacyProtocol'.
     */
    public final void setPrivacyProtocol(final String privacyProtocol) {
        this.privacyProtocol = privacyProtocol == null? null : privacyProtocol.intern();
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
        this.proxyHost = proxyHost == null? null : proxyHost.intern();
    }

    /**
     * Sets the value of field 'readCommunity'. The field 'readCommunity' has
     * the following description: Default read community string
     * 
     * @param readCommunity
     *            the value of field 'readCommunity'.
     */
    public final void setReadCommunity(final String readCommunity) {
        this.readCommunity = readCommunity == null? null : readCommunity.intern();
    }

    /**
     * Sets the value of field 'retry'. The field 'retry' has the following
     * description: Default number of retries
     * 
     * @param retry
     *            the value of field 'retry'.
     */
    public final void setRetry(final Integer retry) {
        this.retry = retry;
    }

    /**
     * Sets the value of field 'securityLevel'. The field 'securityLevel' has
     * the following description: SNMPv3
     * 
     * @param securityLevel
     *            the value of field 'securityLevel'.
     */
    public final void setSecurityLevel(final Integer securityLevel) {
        this.securityLevel = securityLevel;
    }

    /**
     * Sets the value of field 'securityName'. The field 'securityName' has the
     * following description: SNMPv3
     * 
     * @param securityName
     *            the value of field 'securityName'.
     */
    public final void setSecurityName(final String securityName) {
        this.securityName = securityName == null? null : securityName.intern();
    }

    /**
     * Sets the value of field 'timeout'. The field 'timeout' has the following
     * description: Default timeout (in milliseconds)
     * 
     * @param timeout
     *            the value of field 'timeout'.
     */
    public final void setTimeout(final Integer timeout) {
        this.timeout = timeout;
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
        this.version = version == null? null : version.intern();
    }

    /**
     * Sets the value of field 'writeCommunity'. The field 'writeCommunity' has
     * the following description: Default write community string
     * 
     * @param writeCommunity
     *            the value of field 'writeCommunity'.
     */
    public final void setWriteCommunity(final String writeCommunity) {
        this.writeCommunity = writeCommunity == null? null : writeCommunity.intern();
    }

    public Long getTTL() {
        return ttl;
    }

    public void setTTL(Long ttl) {
        this.ttl = ttl;
    }


    public Boolean getEncrypted() {
        return encrypted != null ? encrypted : false;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((authPassphrase == null) ? 0 : authPassphrase.hashCode());
        result = prime * result + ((authProtocol == null) ? 0 : authProtocol.hashCode());
        result = prime * result + ((contextEngineId == null) ? 0 : contextEngineId.hashCode());
        result = prime * result + ((contextName == null) ? 0 : contextName.hashCode());
        result = prime * result + ((engineId == null) ? 0 : engineId.hashCode());
        result = prime * result + ((enterpriseId == null) ? 0 : enterpriseId.hashCode());
        result = prime * result + ((maxRepetitions == null) ? 0 : maxRepetitions.hashCode());
        result = prime * result + ((maxRequestSize == null) ? 0 : maxRequestSize.hashCode());
        result = prime * result + ((maxVarsPerPdu == null) ? 0 : maxVarsPerPdu.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result + ((privacyPassphrase == null) ? 0 : privacyPassphrase.hashCode());
        result = prime * result + ((privacyProtocol == null) ? 0 : privacyProtocol.hashCode());
        result = prime * result + ((proxyHost == null) ? 0 : proxyHost.hashCode());
        result = prime * result + ((readCommunity == null) ? 0 : readCommunity.hashCode());
        result = prime * result + ((retry == null) ? 0 : retry.hashCode());
        result = prime * result + ((securityLevel == null) ? 0 : securityLevel.hashCode());
        result = prime * result + ((securityName == null) ? 0 : securityName.hashCode());
        result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((writeCommunity == null) ? 0 : writeCommunity.hashCode());
        result = prime * result + ((ttl == null) ? 0 : ttl.hashCode());
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
        if (authPassphrase == null) {
            if (other.authPassphrase != null) {
                return false;
            }
        } else if (!authPassphrase.equals(other.authPassphrase)) {
            return false;
        }
        if (authProtocol == null) {
            if (other.authProtocol != null) {
                return false;
            }
        } else if (!authProtocol.equals(other.authProtocol)) {
            return false;
        }
        if (contextEngineId == null) {
            if (other.contextEngineId != null) {
                return false;
            }
        } else if (!contextEngineId.equals(other.contextEngineId)) {
            return false;
        }
        if (contextName == null) {
            if (other.contextName != null) {
                return false;
            }
        } else if (!contextName.equals(other.contextName)) {
            return false;
        }
        if (engineId == null) {
            if (other.engineId != null) {
                return false;
            }
        } else if (!engineId.equals(other.engineId)) {
            return false;
        }
        if (enterpriseId == null) {
            if (other.enterpriseId != null) {
                return false;
            }
        } else if (!enterpriseId.equals(other.enterpriseId)) {
            return false;
        }
        if (maxRepetitions == null) {
            if (other.maxRepetitions != null) {
                return false;
            }
        } else if (!maxRepetitions.equals(other.maxRepetitions)) {
            return false;
        }
        if (maxRequestSize == null) {
            if (other.maxRequestSize != null) {
                return false;
            }
        } else if (!maxRequestSize.equals(other.maxRequestSize)) {
            return false;
        }
        if (maxVarsPerPdu == null) {
            if (other.maxVarsPerPdu != null) {
                return false;
            }
        } else if (!maxVarsPerPdu.equals(other.maxVarsPerPdu)) {
            return false;
        }
        if (port == null) {
            if (other.port != null) {
                return false;
            }
        } else if (!port.equals(other.port)) {
            return false;
        }
        if (privacyPassphrase == null) {
            if (other.privacyPassphrase != null) {
                return false;
            }
        } else if (!privacyPassphrase.equals(other.privacyPassphrase)) {
            return false;
        }
        if (privacyProtocol == null) {
            if (other.privacyProtocol != null) {
                return false;
            }
        } else if (!privacyProtocol.equals(other.privacyProtocol)) {
            return false;
        }
        if (proxyHost == null) {
            if (other.proxyHost != null) {
                return false;
            }
        } else if (!proxyHost.equals(other.proxyHost)) {
            return false;
        }
        if (readCommunity == null) {
            if (other.readCommunity != null) {
                return false;
            }
        } else if (!readCommunity.equals(other.readCommunity)) {
            return false;
        }
        if (retry == null) {
            if (other.retry != null) {
                return false;
            }
        } else if (!retry.equals(other.retry)) {
            return false;
        }
        if (securityLevel == null) {
            if (other.securityLevel != null) {
                return false;
            }
        } else if (!securityLevel.equals(other.securityLevel)) {
            return false;
        }
        if (securityName == null) {
            if (other.securityName != null) {
                return false;
            }
        } else if (!securityName.equals(other.securityName)) {
            return false;
        }
        if (timeout == null) {
            if (other.timeout != null) {
                return false;
            }
        } else if (!timeout.equals(other.timeout)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (writeCommunity == null) {
            if (other.writeCommunity != null) {
                return false;
            }
        } else if (!writeCommunity.equals(other.writeCommunity)) {
            return false;
        }
        if (ttl == null) {
            if (other.ttl != null) {
                return false;
            }
        } else if (!ttl.equals(other.ttl)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Configuration [proxyHost=" + proxyHost + ", maxVarsPerPdu=" + maxVarsPerPdu + ", maxRepetitions=" + maxRepetitions + ", maxRequestSize=" + maxRequestSize + ", securityName="
                + securityName + ", securityLevel=" + securityLevel + ", authPassphrase=" + authPassphrase + ", authProtocol=" + authProtocol + ", engineId=" + engineId
                + ", contextEngineId=" + contextEngineId + ", contextName=" + contextName + ", privacyPassphrase=" + privacyPassphrase + ", privacyProtocol=" + privacyProtocol
                + ", enterpriseId=" + enterpriseId + ", version=" + version + ", writeCommunity=" + writeCommunity + ", readCommunity=" + readCommunity + ", timeout=" + timeout
                + ", retry=" + retry + ", port=" + port + ", ttl=" + ttl + ", encrypted=" + encrypted + "]";
    }

}
