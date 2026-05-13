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

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class Configuration implements Serializable {
    private static final long serialVersionUID = 6018795999027969844L;

    protected static final String MASKED_PASSWORD = "******";

    /**
     * The proxy host to use when communicating with this agent
     */
    @JsonProperty("proxyHost")
    @XmlAttribute(name="proxy-host")
    private String proxyHost;

    /**
     * Number of variables to send per SNMP request.
     */
    @JsonProperty("maxVarsPerPdu")
    @XmlAttribute(name="max-vars-per-pdu")
    private Integer maxVarsPerPdu;

    /**
     * Number of repetitions to send per get-bulk request.
     */
    @JsonProperty("maxRepetitions")
    @XmlAttribute(name="max-repetitions")
    private Integer maxRepetitions;

    /**
     * (SNMP4J specific) Specifies the maximum number of bytes that may be
     * encoded into an individual SNMP PDU request by Collectd. Provides a
     * means to limit the size of outgoing PDU requests. Default is 65535,
     * must be at least 484.
     */
    @JsonProperty("maxRequestSize")
    @XmlAttribute(name="max-request-size")
    private Integer maxRequestSize;

    /**
     * SNMPv3
     */
    @JsonProperty("securityName")
    @XmlAttribute(name="security-name")
    private String securityName;

    /**
     * SNMPv3
     */
    @JsonProperty("securityLevel")
    @XmlAttribute(name="security-level")
    private Integer securityLevel;

    /**
     * SNMPv3
     */
    @JsonProperty("authPassphrase")
    @XmlAttribute(name="auth-passphrase")
    private String authPassphrase;

    /**
     * SNMPv3
     */
    @JsonProperty("authProtocol")
    @XmlAttribute(name="auth-protocol")
    private String authProtocol;

    /**
     * SNMPv3
     */
    @JsonProperty("engineId")
    @XmlAttribute(name="engine-id")
    private String engineId;

    /**
     * SNMPv3
     */
    @JsonProperty("contextEngineId")
    @XmlAttribute(name="context-engine-id")
    private String contextEngineId;

    /**
     * SNMPv3
     */
    @JsonProperty("contextName")
    @XmlAttribute(name="context-name")
    private String contextName;

    /**
     * SNMPv3
     */
    @JsonProperty("privacyPassphrase")
    @XmlAttribute(name="privacy-passphrase")
    private String privacyPassphrase;

    /**
     * SNMPv3
     */
    @JsonProperty("privacyProtocol")
    @XmlAttribute(name="privacy-protocol")
    private String privacyProtocol;

    /**
     * SNMPv3
     */
    @JsonProperty("enterpriseId")
    @XmlAttribute(name="enterprise-id")
    private String enterpriseId;

    /**
     * If set, forces SNMP data collection to the specified version.
     */
    @JsonProperty("version")
    @XmlAttribute(name="version")
    private String version;

    /**
     * Default write community string
     */
    @JsonProperty("writeCommunity")
    @XmlAttribute(name="write-community")
    private String writeCommunity;

    /**
     * Default read community string
     */
    @JsonProperty("readCommunity")
    @XmlAttribute(name="read-community")
    private String readCommunity;

    /**
     * Default timeout (in milliseconds)
     */
    @JsonProperty("timeout")
    @XmlAttribute(name="timeout")
    private Integer timeout;

    /**
     * Default number of retries
     */
    @JsonProperty("retry")
    @XmlAttribute(name="retry")
    private Integer retry;

    /**
     * If set, overrides UDP port 161 as the port where SNMP GET/GETNEXT/GETBULK
     * requests are sent.
     */
    @JsonProperty("port")
    @XmlAttribute(name="port")
    private Integer port;


    @JsonProperty("ttl")
    @XmlAttribute(name="ttl")
    private Long ttl;


    @JsonProperty("encrypted")
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
     * the following description: SNMPv3.
     * Note, this will return null if field is null.
     * Valid values are 1-3, so we do not want to return 0 if the field is null.
     * Calling code should check 'hasSecurityLevel()' and substitute a default integer value if desired.
     *
     * @return the value of field 'SecurityLevel'.
     */
    public final Integer getSecurityLevel() {
        return securityLevel;
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
     * This can be set to null or else a valid value from 1-3.
     * 
     * @param securityLevel
     *            the value of field 'securityLevel'.
     */
    public final void setSecurityLevel(final Integer securityLevel) {
        if (securityLevel == null) {
            this.securityLevel = null;
        } else {
            this.securityLevel = (securityLevel >= 1 && securityLevel <= 3) ? securityLevel : 1;
        }
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
        return Objects.hash(authPassphrase, authProtocol, contextEngineId, contextName, engineId,
                enterpriseId, maxRepetitions, maxRequestSize, maxVarsPerPdu, port, privacyPassphrase,
                privacyProtocol, proxyHost, readCommunity, retry, securityLevel, securityName, timeout,
                version, writeCommunity, ttl, encrypted);
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

        return Objects.equals(authPassphrase, other.authPassphrase)
                && Objects.equals(authProtocol, other.authProtocol)
                && Objects.equals(contextEngineId, other.contextEngineId)
                && Objects.equals(contextName, other.contextName)
                && Objects.equals(engineId, other.engineId)
                && Objects.equals(enterpriseId, other.enterpriseId)
                && Objects.equals(maxRepetitions, other.maxRepetitions)
                && Objects.equals(maxRequestSize, other.maxRequestSize)
                && Objects.equals(maxVarsPerPdu, other.maxVarsPerPdu)
                && Objects.equals(port, other.port)
                && Objects.equals(privacyPassphrase, other.privacyPassphrase)
                && Objects.equals(privacyProtocol, other.privacyProtocol)
                && Objects.equals(proxyHost, other.proxyHost)
                && Objects.equals(readCommunity, other.readCommunity)
                && Objects.equals(retry, other.retry)
                && Objects.equals(securityLevel, other.securityLevel)
                && Objects.equals(securityName, other.securityName)
                && Objects.equals(timeout, other.timeout)
                && Objects.equals(version, other.version)
                && Objects.equals(writeCommunity, other.writeCommunity)
                && Objects.equals(ttl, other.ttl)
                && Objects.equals(encrypted, other.encrypted);
    }

    @Override
    public String toString() {
        return "Configuration [" +
                "proxyHost=" + proxyHost +
                ", maxVarsPerPdu=" + maxVarsPerPdu +
                ", maxRepetitions=" + maxRepetitions +
                ", maxRequestSize=" + maxRequestSize +
                ", securityName=" + securityName +
                ", securityLevel=" + securityLevel +
                ", authPassphrase=" + MASKED_PASSWORD +
                ", authProtocol=" + authProtocol +
                ", engineId=" + engineId +
                ", contextEngineId=" + contextEngineId +
                ", contextName=" + contextName +
                ", privacyPassphrase=" + MASKED_PASSWORD +
                ", privacyProtocol=" + privacyProtocol +
                ", enterpriseId=" + enterpriseId +
                ", version=" + version +
                ", writeCommunity=" + MASKED_PASSWORD +
                ", readCommunity=" + MASKED_PASSWORD +
                ", timeout=" + timeout +
                ", retry=" + retry +
                ", port=" + port +
                ", ttl=" + ttl +
                ", encrypted=" + encrypted +
                "]";
    }

    /**
     * Security level needs to be between 1-3. See org.opennms.netmgt.snmp.SnmpConfiguration for valid values.
     * This sets it to null if it is an invalid value such as 0.
     */
    public void fixSecurityLevel() {
        if (securityLevel != null && (securityLevel < 1 || securityLevel > 3)) {
            securityLevel = null;
        }
    }
}
