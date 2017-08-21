/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.wmi;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;
import org.opennms.core.utils.InetAddressUtils;

@XmlRootElement(name = "wmi-agent-config")
@XmlAccessorType(XmlAccessType.NONE)

public class WmiAgentConfig {
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int DEFAULT_RETRIES = 1;
    public static final String DEFAULT_PASSWORD = "";
    public static final String DEFAULT_USERNAME="Administrator";
    public static final String DEFAULT_DOMAIN="WORKGROUP";

    @XmlAttribute(name = "address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress m_Address;

    @XmlAttribute(name = "timeout")
    private int m_Timeout;

    @XmlAttribute(name = "retries")
    private int m_Retries;

    @XmlAttribute(name = "username")
    private String m_Username;

    @XmlAttribute(name = "domain")
    private String m_Domain;

    @XmlAttribute(name = "password")
    private String m_Password;

    /**
     * <p>Constructor for WmiAgentConfig.</p>
     */
    public WmiAgentConfig() {
        setDefaults();
    }
    
    /**
     * <p>Constructor for WmiAgentConfig.</p>
     *
     * @param agentAddress a {@link java.net.InetAddress} object.
     */
    public WmiAgentConfig(InetAddress agentAddress) {
        m_Address = agentAddress;
        setDefaults();
    }

    private void setDefaults() {
        m_Timeout = DEFAULT_TIMEOUT;
        m_Retries = DEFAULT_RETRIES;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder("AgentConfig[");
        buff.append("Address: "+m_Address);
        buff.append(", Password: "+String.valueOf(m_Password)); //use valueOf to handle null values of m_password
        buff.append(", Timeout: "+m_Timeout);
        buff.append(", Retries: "+m_Retries);
        buff.append("]");
        return buff.toString();
    }


    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_Address;
    }

    /**
     * <p>setAddress</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    public void setAddress(InetAddress address) {
        m_Address = address;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a int.
     */
    public int getTimeout() {
        return m_Timeout;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout a int.
     */
    public void setTimeout(int timeout) {
        m_Timeout = timeout;
    }

    /**
     * <p>getRetries</p>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_Retries;
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries a int.
     */
    public void setRetries(int retries) {
        m_Retries = retries;
    }

    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(String password) {
        m_Password = password;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPassword() {
        return m_Password;
    }


    /**
     * <p>getUsername</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUsername() {
        return m_Username;
    }
    
    /**
     * <p>setUsername</p>
     *
     * @param username a {@link java.lang.String} object.
     */
    public void setUsername(String username) {
    	m_Username = username;
    }

    /**
     * <p>Getter for the field <code>domain</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDomain() {
        return m_Domain;
    }
    
    /**
     * <p>Setter for the field <code>domain</code>.</p>
     *
     * @param domain a {@link java.lang.String} object.
     */
    public void setDomain(String domain) {
    	m_Domain = domain;
    }

    public Map<String, String> toMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("address", InetAddressUtils.str(m_Address));
        map.put("domain", m_Domain);
        map.put("password", m_Password);
        map.put("retries", Integer.toString(m_Retries));
        map.put("timeout", Integer.toString(m_Timeout));
        map.put("username", m_Username);
        return map;
    }

    public static WmiAgentConfig fromMap(Map<String, String> map) {
        final WmiAgentConfig agentConfig = new WmiAgentConfig();
        agentConfig.setAddress(InetAddressUtils.addr(map.get("address")));
        agentConfig.setDomain(map.get("domain"));
        agentConfig.setPassword(map.get("password"));
        agentConfig.setRetries(Integer.parseInt(map.get("retries")));
        agentConfig.setTimeout(Integer.parseInt(map.get("timeout")));
        agentConfig.setUsername(map.get("username"));
        return agentConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_Address, m_Domain, m_Password, m_Retries, m_Timeout, m_Username);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WmiAgentConfig other = (WmiAgentConfig) obj;
        return Objects.equals(this.m_Address, other.m_Address)
                && Objects.equals(this.m_Domain, other.m_Domain)
                && Objects.equals(this.m_Password, other.m_Password)
                && Objects.equals(this.m_Retries, other.m_Retries)
                && Objects.equals(this.m_Timeout, other.m_Timeout)
                && Objects.equals(this.m_Username, other.m_Username);
    }
}
