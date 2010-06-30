//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.snmp.joesnmp;


import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.protocols.snmp.SnmpSMI;

/**
 * <p>JoeSnmpAgentConfig class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JoeSnmpAgentConfig {
    
    private SnmpAgentConfig m_config;
    
    /**
     * <p>Constructor for JoeSnmpAgentConfig.</p>
     *
     * @param config a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public JoeSnmpAgentConfig(SnmpAgentConfig config) {
        m_config = config;
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_config.getAddress();
    }

    /**
     * <p>getAuthPassPhrase</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAuthPassPhrase() {
        return m_config.getAuthPassPhrase();
    }

    /**
     * <p>getAuthProtocol</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAuthProtocol() {
        return m_config.getAuthProtocol();
    }

    /**
     * <p>getMaxRequestSize</p>
     *
     * @return a int.
     */
    public int getMaxRequestSize() {
        return m_config.getMaxRequestSize();
    }

    /**
     * <p>getMaxVarsPerPdu</p>
     *
     * @return a int.
     */
    public int getMaxVarsPerPdu() {
        return m_config.getMaxVarsPerPdu();
    }
    
    /**
     * <p>getMaxRepetitions</p>
     *
     * @return a int.
     */
    public int getMaxRepetitions() {
        return m_config.getMaxRepetitions();
    }
    
    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_config.getPort();
    }

    /**
     * <p>getPrivPassPhrase</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPrivPassPhrase() {
        return m_config.getPrivPassPhrase();
    }

    /**
     * <p>getPrivProtocol</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPrivProtocol() {
        return m_config.getPrivProtocol();
    }

    /**
     * <p>getReadCommunity</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReadCommunity() {
        return m_config.getReadCommunity();
    }

    /**
     * <p>getRetries</p>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_config.getRetries();
    }

    /**
     * <p>getSecurityLevel</p>
     *
     * @return a int.
     */
    public int getSecurityLevel() {
        return m_config.getSecurityLevel();
    }

    /**
     * <p>getSecurityName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSecurityName() {
        return m_config.getSecurityName();
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a int.
     */
    public int getTimeout() {
        return m_config.getTimeout();
    }

    /**
     * <p>getVersion</p>
     *
     * @return a int.
     */
    public int getVersion() {
        return convertVersion(m_config.getVersion());
    }

    /**
     * <p>getWriteCommunity</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWriteCommunity() {
        return m_config.getWriteCommunity();
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return m_config.hashCode();
    }

    /**
     * <p>setAddress</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    public void setAddress(InetAddress address) {
        m_config.setAddress(address);
    }

    /**
     * <p>setAuthPassPhrase</p>
     *
     * @param authPassPhrase a {@link java.lang.String} object.
     */
    public void setAuthPassPhrase(String authPassPhrase) {
        m_config.setAuthPassPhrase(authPassPhrase);
    }

    /**
     * <p>setAuthProtocol</p>
     *
     * @param authProtocol a {@link java.lang.String} object.
     */
    public void setAuthProtocol(String authProtocol) {
        m_config.setAuthProtocol(authProtocol);
    }

    /**
     * <p>setMaxRequestSize</p>
     *
     * @param maxRequestSize a int.
     */
    public void setMaxRequestSize(int maxRequestSize) {
        m_config.setMaxRequestSize(maxRequestSize);
    }

    /**
     * <p>setMaxVarsPerPdu</p>
     *
     * @param maxVarsPerPdu a int.
     */
    public void setMaxVarsPerPdu(int maxVarsPerPdu) {
        m_config.setMaxVarsPerPdu(maxVarsPerPdu);
    }
    
    /**
     * <p>setMaxRepetitions</p>
     *
     * @param maxRepetitions a int.
     */
    public void setMaxRepetitions(int maxRepetitions) {
        m_config.setMaxRepetitions(maxRepetitions);
    }

    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        m_config.setPort(port);
    }

    /**
     * <p>setPrivPassPhrase</p>
     *
     * @param privPassPhrase a {@link java.lang.String} object.
     */
    public void setPrivPassPhrase(String privPassPhrase) {
        m_config.setPrivPassPhrase(privPassPhrase);
    }

    /**
     * <p>setPrivProtocol</p>
     *
     * @param authPrivProtocol a {@link java.lang.String} object.
     */
    public void setPrivProtocol(String authPrivProtocol) {
        m_config.setPrivProtocol(authPrivProtocol);
    }

    /**
     * <p>setReadCommunity</p>
     *
     * @param community a {@link java.lang.String} object.
     */
    public void setReadCommunity(String community) {
        m_config.setReadCommunity(community);
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries a int.
     */
    public void setRetries(int retries) {
        m_config.setRetries(retries);
    }

    /**
     * <p>setSecurityLevel</p>
     *
     * @param securityLevel a int.
     */
    public void setSecurityLevel(int securityLevel) {
        m_config.setSecurityLevel(securityLevel);
    }

    /**
     * <p>setSecurityName</p>
     *
     * @param securityName a {@link java.lang.String} object.
     */
    public void setSecurityName(String securityName) {
        m_config.setSecurityName(securityName);
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout a int.
     */
    public void setTimeout(int timeout) {
        m_config.setTimeout(timeout);
    }

    /**
     * <p>setVersion</p>
     *
     * @param version a int.
     */
    public void setVersion(int version) {
        m_config.setVersion(version);
    }

    /**
     * <p>setWriteCommunity</p>
     *
     * @param community a {@link java.lang.String} object.
     */
    public void setWriteCommunity(String community) {
        m_config.setWriteCommunity(community);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return m_config.toString();
    }

    /**
     * <p>convertVersion</p>
     *
     * @param version a int.
     * @return a int.
     */
    public static int convertVersion(int version) {
        switch (version) {
        case SnmpAgentConfig.VERSION2C :
            return SnmpSMI.SNMPV2;
        default :
            return SnmpSMI.SNMPV1;
        }
    }

}
