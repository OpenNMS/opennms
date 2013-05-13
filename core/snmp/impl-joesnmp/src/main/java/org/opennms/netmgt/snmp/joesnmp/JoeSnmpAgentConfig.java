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

package org.opennms.netmgt.snmp.joesnmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.protocols.snmp.SnmpSMI;

public class JoeSnmpAgentConfig {
    
    private SnmpAgentConfig m_config;
    
    public JoeSnmpAgentConfig(SnmpAgentConfig config) {
        m_config = config;
    }

    public InetAddress getAddress() {
        return m_config.getAddress();
    }

    public String getAuthPassPhrase() {
        return m_config.getAuthPassPhrase();
    }

    public String getAuthProtocol() {
        return m_config.getAuthProtocol();
    }

    public int getMaxRequestSize() {
        return m_config.getMaxRequestSize();
    }

    public int getMaxVarsPerPdu() {
        return m_config.getMaxVarsPerPdu();
    }
    
    public int getMaxRepetitions() {
        return m_config.getMaxRepetitions();
    }
    
    public int getPort() {
        return m_config.getPort();
    }

    public String getPrivPassPhrase() {
        return m_config.getPrivPassPhrase();
    }

    public String getPrivProtocol() {
        return m_config.getPrivProtocol();
    }

    public String getReadCommunity() {
        return m_config.getReadCommunity();
    }

    public int getRetries() {
        return m_config.getRetries();
    }

    public int getSecurityLevel() {
        return m_config.getSecurityLevel();
    }

    public String getSecurityName() {
        return m_config.getSecurityName();
    }

    public int getTimeout() {
        return m_config.getTimeout();
    }

    public int getVersion() {
        return convertVersion(m_config.getVersion());
    }

    public String getWriteCommunity() {
        return m_config.getWriteCommunity();
    }

    @Override
    public int hashCode() {
        return m_config.hashCode();
    }

    public void setAddress(InetAddress address) {
        m_config.setAddress(address);
    }

    public void setAuthPassPhrase(String authPassPhrase) {
        m_config.setAuthPassPhrase(authPassPhrase);
    }

    public void setAuthProtocol(String authProtocol) {
        m_config.setAuthProtocol(authProtocol);
    }

    public void setMaxRequestSize(int maxRequestSize) {
        m_config.setMaxRequestSize(maxRequestSize);
    }

    public void setMaxVarsPerPdu(int maxVarsPerPdu) {
        m_config.setMaxVarsPerPdu(maxVarsPerPdu);
    }
    
    public void setMaxRepetitions(int maxRepetitions) {
        m_config.setMaxRepetitions(maxRepetitions);
    }

    public void setPort(int port) {
        m_config.setPort(port);
    }

    public void setPrivPassPhrase(String privPassPhrase) {
        m_config.setPrivPassPhrase(privPassPhrase);
    }

    public void setPrivProtocol(String authPrivProtocol) {
        m_config.setPrivProtocol(authPrivProtocol);
    }

    public void setReadCommunity(String community) {
        m_config.setReadCommunity(community);
    }

    public void setRetries(int retries) {
        m_config.setRetries(retries);
    }

    public void setSecurityLevel(int securityLevel) {
        m_config.setSecurityLevel(securityLevel);
    }

    public void setSecurityName(String securityName) {
        m_config.setSecurityName(securityName);
    }

    public void setTimeout(int timeout) {
        m_config.setTimeout(timeout);
    }

    public void setVersion(int version) {
        m_config.setVersion(version);
    }

    public void setWriteCommunity(String community) {
        m_config.setWriteCommunity(community);
    }

    @Override
    public String toString() {
        return m_config.toString();
    }

    public static int convertVersion(int version) {
        switch (version) {
        case SnmpAgentConfig.VERSION2C :
            return SnmpSMI.SNMPV2;
        default :
            return SnmpSMI.SNMPV1;
        }
    }

}
