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

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

/**
 * @author (various previous authors not documented)
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@XmlRootElement(name = "snmpAgentConfig")
public class SnmpAgentConfig extends SnmpConfiguration implements Serializable {
    private static final long serialVersionUID = 1456963719970029200L;
    private static final transient Logger LOG = LoggerFactory.getLogger(SnmpAgentConfig.class);

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
        agentConfig.setAddress(InetAddrUtils.addr(protocolConfig.optString("address")));
        agentConfig.setProxyFor(InetAddrUtils.addr(protocolConfig.optString("proxyFor")));
        agentConfig.setPort(protocolConfig.optInt("port"));
        agentConfig.setTimeout(protocolConfig.optInt("timeout"));
        agentConfig.setRetries(protocolConfig.optInt("retries"));
        agentConfig.setMaxVarsPerPdu(protocolConfig.optInt("max-vars-per-pdu"));
        agentConfig.setMaxRepetitions(protocolConfig.optInt("max-repetitions"));
        agentConfig.setMaxRequestSize(protocolConfig.optInt("max-request-size"));
        agentConfig.setVersion(protocolConfig.optInt("version"));
        agentConfig.setSecurityLevel(protocolConfig.optInt("security-level"));
        agentConfig.setSecurityName(protocolConfig.optString("security-name"));
        agentConfig.setAuthPassPhrase(protocolConfig.optString("auth-passphrase"));
        agentConfig.setAuthProtocol(protocolConfig.optString("auth-protocol"));
        agentConfig.setPrivPassPhrase(protocolConfig.optString("priv-passphrase"));
        agentConfig.setPrivProtocol(protocolConfig.optString("priv-protocol"));
        agentConfig.setEngineId(protocolConfig.optString("engine-id"));
        agentConfig.setContextEngineId(protocolConfig.optString("context-engine-id"));
        agentConfig.setContextName(protocolConfig.optString("context-name"));
        agentConfig.setEnterpriseId(protocolConfig.optString("enterprise-id"));
        agentConfig.setReadCommunity(protocolConfig.optString("read-community"));
        agentConfig.setWriteCommunity(protocolConfig.optString("write-community"));

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
            buff.append(", EngineId: " + getEngineId());
            buff.append(", ContextEngineId: " + getContextEngineId());
            buff.append(", ContextName: " + getContextName());
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
                getMaxRepetitions(),
                getMaxRequestSize(),
                getMaxVarsPerPdu(),
                getVersion(),
                getSecurityLevel(),
                getSecurityName(),
                getAuthPassPhrase(),
                getAuthProtocol(),
                getPrivPassPhrase(),
                getPrivProtocol(),
                getEngineId(),
                getContextEngineId(),
                getEnterpriseId(),
                getReadCommunity(),
                getWriteCommunity());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == getClass()) {
            SnmpAgentConfig other = (SnmpAgentConfig) obj;

            boolean equals = Objects.equals(getAddress(), other.getAddress())
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
            return equals;
        }
        return false;
    }
}
