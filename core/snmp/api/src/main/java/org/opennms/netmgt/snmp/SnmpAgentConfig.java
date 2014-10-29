/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

/**
 * @author (various previous authors not documented)
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@XmlRootElement(name = "snmpAgentConfig")
public class SnmpAgentConfig extends SnmpConfiguration implements Serializable {
    private static final long serialVersionUID = -6646744513933866811L;

    private InetAddress m_address;
    private InetAddress m_proxyFor;

    public SnmpAgentConfig() {
        this(null);
    }

    public SnmpAgentConfig(InetAddress agentAddress) {
        this(agentAddress, SnmpConfiguration.DEFAULTS);
    }

    public SnmpAgentConfig(InetAddress agentAddress, SnmpConfiguration defaults) {
        super(defaults);
        m_address = agentAddress;
    }

    public static SnmpAgentConfig parseProtocolConfigurationString(String protocolConfigString) {
        if (protocolConfigString == null) {
            throw new IllegalArgumentException("Protocol configuration string for SnmpAgentConfig must not be null.");
        }

        final JSONObject protocolConfig = new JSONObject(new JSONTokener(protocolConfigString)).optJSONObject("snmp");

        if (protocolConfig == null) {
            throw new IllegalArgumentException("Invalid protocol configuration string for SnmpAgentConfig: Expected it to start with snmp object" + protocolConfigString);
        }

        final SnmpAgentConfig agentConfig = new SnmpAgentConfig();
        if (!protocolConfig.isNull("address")) agentConfig.setAddress(InetAddrUtils.addr(protocolConfig.optString("address")));
        if (!protocolConfig.isNull("proxyFor")) agentConfig.setProxyFor(InetAddrUtils.addr(protocolConfig.optString("proxyFor")));
        if (!protocolConfig.isNull("port")) agentConfig.setPort(protocolConfig.optInt("port"));
        if (!protocolConfig.isNull("timeout")) agentConfig.setTimeout(protocolConfig.optInt("timeout"));
        if (!protocolConfig.isNull("retries")) agentConfig.setRetries(protocolConfig.optInt("retries"));
        if (!protocolConfig.isNull("max-vars-per-pdu")) agentConfig.setMaxVarsPerPdu(protocolConfig.optInt("max-vars-per-pdu"));
        if (!protocolConfig.isNull("max-repetitions")) agentConfig.setMaxRepetitions(protocolConfig.optInt("max-repetitions"));
        if (!protocolConfig.isNull("max-request-size")) agentConfig.setMaxRequestSize(protocolConfig.optInt("max-request-size"));
        if (!protocolConfig.isNull("version")) agentConfig.setVersion(protocolConfig.optInt("version"));
        if (!protocolConfig.isNull("security-level")) agentConfig.setSecurityLevel(protocolConfig.optInt("security-level"));
        if (!protocolConfig.isNull("security-name")) agentConfig.setSecurityName(protocolConfig.optString("security-name"));
        if (!protocolConfig.isNull("auth-passphrase")) agentConfig.setAuthPassPhrase(protocolConfig.optString("auth-passphrase"));
        if (!protocolConfig.isNull("auth-protocol")) agentConfig.setAuthProtocol(protocolConfig.optString("auth-protocol"));
        if (!protocolConfig.isNull("priv-passphrase")) agentConfig.setPrivPassPhrase(protocolConfig.optString("priv-passphrase"));
        if (!protocolConfig.isNull("priv-protocol")) agentConfig.setPrivProtocol(protocolConfig.optString("priv-protocol"));
        if (!protocolConfig.isNull("context-name")) agentConfig.setContextName(protocolConfig.optString("context-name"));
        if (!protocolConfig.isNull("engine-id")) agentConfig.setEngineId(protocolConfig.optString("engine-id"));
        if (!protocolConfig.isNull("context-engine-id")) agentConfig.setContextEngineId(protocolConfig.optString("context-engine-id"));
        if (!protocolConfig.isNull("enterprise-id")) agentConfig.setEnterpriseId(protocolConfig.optString("enterprise-id"));
        if (!protocolConfig.isNull("read-community")) agentConfig.setReadCommunity(protocolConfig.optString("read-community"));
        if (!protocolConfig.isNull("write-community")) agentConfig.setWriteCommunity(protocolConfig.optString("write-community"));

        return agentConfig;
    }

    public String toProtocolConfigString() {
        return new JSONStringer()
        .object()
        .key("snmp")
        .object()
        .key("address").value((m_address == null)
                              ? null
                                  : InetAddrUtils.str(m_address))
                                  .key("proxyFor").value((m_proxyFor == null)
                                                         ? null
                                                             : InetAddrUtils.str(m_proxyFor))
                                                             .key("port").value(getPort())
                                                             .key("timeout").value(getTimeout())
                                                             .key("retries").value(getRetries())
                                                             .key("max-vars-per-pdu").value(getMaxVarsPerPdu())
                                                             .key("max-repetitions").value(getMaxRepetitions())
                                                             .key("max-request-size").value(getMaxRequestSize())
                                                             .key("version").value(getVersion())
                                                             .key("security-level").value(getSecurityLevel())
                                                             .key("security-name").value(getSecurityName())
                                                             .key("auth-passphrase").value(getAuthPassPhrase())
                                                             .key("auth-protocol").value(getAuthProtocol())
                                                             .key("priv-passphrase").value(getPrivPassPhrase())
                                                             .key("priv-protocol").value(getPrivProtocol())
                                                             .key("context-name").value(getContextName())
                                                             .key("engine-id").value(getEngineId())
                                                             .key("context-engine-id").value(getContextEngineId())
                                                             .key("enterprise-id").value(getEnterpriseId())
                                                             .key("read-community").value(getReadCommunity())
                                                             .key("write-community").value(getWriteCommunity())
                                                             .endObject()
                                                             .endObject()
                                                             .toString();
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer("SnmpAgentConfig[");
        buff.append("Address: " + InetAddrUtils.str(m_address));
        buff.append(", ProxyForAddress: " + InetAddrUtils.str(m_proxyFor));
        buff.append(", Port: " + getPort());
        buff.append(", Timeout: " + getTimeout());
        buff.append(", Retries: " + getRetries());
        buff.append(", MaxVarsPerPdu: " + getMaxVarsPerPdu());
        buff.append(", MaxRepetitions: " + getMaxRepetitions());
        buff.append(", MaxRequestSize: " + getMaxRequestSize());
        buff.append(", Version: " + versionToString(getVersion()));
        if (isVersion3()) {
            buff.append(", SecurityLevel: " + getSecurityLevel());
            buff.append(", SecurityName: " + getSecurityName());
            buff.append(", AuthPassPhrase: " + getAuthPassPhrase());
            buff.append(", AuthProtocol: " + getAuthProtocol());
            buff.append(", PrivPassphrase: " + getPrivPassPhrase());
            buff.append(", PrivProtocol: " + getPrivProtocol());
            buff.append(", ContextName: " + getContextName());
            buff.append(", EngineId: " + getEngineId());
            buff.append(", ContextEngineId: " + getContextEngineId());
            buff.append(", EnterpriseId:" + getEnterpriseId());
        } else {
            buff.append(", ReadCommunity: " + getReadCommunity());
            buff.append(", WriteCommunity: " + getWriteCommunity());
        }
        buff.append("]");
        return buff.toString();
    }


    @XmlJavaTypeAdapter(InetAddrXmlAdapter.class)
    public InetAddress getAddress() {
        return m_address;
    }

    public void setAddress(InetAddress address) {
        m_address = address;
    }

    @XmlJavaTypeAdapter(InetAddrXmlAdapter.class)
    public InetAddress getProxyFor() {
        return m_proxyFor;
    }

    public void setProxyFor(InetAddress address) {
        m_proxyFor = address;
    }

    @XmlTransient
    public InetAddress getEffectiveAddress() {
        if (m_proxyFor == null) return m_address;
        return m_proxyFor;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(getAddress(),
                                getProxyFor(),
                                getPort(),
                                getTimeout(),
                                getRetries(),
                                getMaxVarsPerPdu(),
                                getMaxRepetitions(),
                                getMaxRequestSize(),
                                getVersion(),
                                getSecurityLevel(),
                                getSecurityName(),
                                getAuthPassPhrase(),
                                getAuthProtocol(),
                                getPrivPassPhrase(),
                                getPrivProtocol(),
                                getContextName(),
                                getEngineId(),
                                getContextEngineId(),
                                getEnterpriseId(),
                                getReadCommunity(),
                                getWriteCommunity());
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            System.err.println("obj = null!");
            return false;
        }
        if (obj instanceof SnmpAgentConfig) {
            final SnmpAgentConfig other = (SnmpAgentConfig) obj;

            return Objects.equals(getAddress(), other.getAddress())
                    && Objects.equals(getProxyFor(), other.getProxyFor())
                    && Objects.equals(getPort(), other.getPort())
                    && Objects.equals(getTimeout(), other.getTimeout())
                    && Objects.equals(getRetries(), other.getRetries())
                    && Objects.equals(getMaxRepetitions(), other.getMaxRepetitions())
                    && Objects.equals(getMaxRequestSize(), other.getMaxRequestSize())
                    && Objects.equals(getMaxVarsPerPdu(), other.getMaxVarsPerPdu())
                    && Objects.equals(getVersion(), other.getVersion())
                    && Objects.equals(getSecurityLevel(), other.getSecurityLevel())
                    && Objects.equals(getSecurityName(), other.getSecurityName())
                    && Objects.equals(getAuthPassPhrase(), other.getAuthPassPhrase())
                    && Objects.equals(getAuthProtocol(), other.getAuthProtocol())
                    && Objects.equals(getPrivPassPhrase(), other.getPrivPassPhrase())
                    && Objects.equals(getPrivProtocol(), other.getPrivProtocol())
                    && Objects.equals(getEngineId(), other.getEngineId())
                    && Objects.equals(getContextEngineId(), other.getContextEngineId())
                    && Objects.equals(getEnterpriseId(), other.getEnterpriseId())
                    && Objects.equals(getReadCommunity(), other.getReadCommunity())
                    && Objects.equals(getWriteCommunity(), other.getWriteCommunity());
        }
        return false;
    }
}
