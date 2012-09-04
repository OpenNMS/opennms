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

import java.net.InetAddress;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.bind.InetAddressXmlAdapter;

/**
 * @author (various previous authors not documented)
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@XmlRootElement(name="snmpAgentConfig")
public class SnmpAgentConfig extends SnmpConfiguration {
    
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

    public String toString() {
        StringBuffer buff = new StringBuffer("AgentConfig[");
        buff.append("Address: "+InetAddressUtils.str(m_address));
        buff.append(", ProxyForAddress: "+InetAddressUtils.str(m_proxyFor));
        buff.append(", Port: "+getPort());
        buff.append(", Community: "+getReadCommunity());
        buff.append(", Timeout: "+getTimeout());
        buff.append(", Retries: "+getRetries());
        buff.append(", MaxVarsPerPdu: "+getMaxVarsPerPdu());
        buff.append(", MaxRepetitions: "+getMaxRepetitions());
        buff.append(", Max request size: "+getMaxRequestSize());
        buff.append(", Version: "+versionToString(getVersion()));
        if (getVersion() == VERSION3) {
            buff.append(", Security level: "+getSecurityLevel());
            buff.append(", Security name: "+getSecurityName());
            buff.append(", auth-passphrase: "+getAuthPassPhrase());
            buff.append(", auth-protocol: "+getAuthProtocol());
            buff.append(", priv-passprhase: "+getPrivPassPhrase());
            buff.append(", priv-protocol: "+getPrivProtocol());
        }
        buff.append("]");
        return buff.toString();
    }


    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getAddress() {
        return m_address;
    }

    public void setAddress(InetAddress address) {
        m_address = address;
    }

    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
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

}
