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

package org.opennms.netmgt.config.wmi;

import java.net.InetAddress;

/**
 * <p>WmiAgentConfig class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiAgentConfig {
    /** Constant <code>DEFAULT_TIMEOUT=3000</code> */
    public static final int DEFAULT_TIMEOUT = 3000;
    /** Constant <code>DEFAULT_RETRIES=1</code> */
    public static final int DEFAULT_RETRIES = 1;
    /** Constant <code>DEFAULT_PASSWORD=""</code> */
    public static final String DEFAULT_PASSWORD = "";
    /** Constant <code>DEFAULT_USERNAME="Administrator"</code> */
    public static final String DEFAULT_USERNAME="Administrator";
    /** Constant <code>DEFAULT_DOMAIN="WORKGROUP"</code> */
    public static final String DEFAULT_DOMAIN="WORKGROUP";
    
    private InetAddress m_Address;
    private int m_Timeout;
    private int m_Retries;
    private String m_Username;
    private String m_Domain;
    private String m_Password;
    
    
	String user = "";
	String pass = "";
	String domain = "";
	String matchType = "all";
	String compVal = "";
	String compOp = "NOOP";
	String wmiClass = "";
	String wmiObject = "";
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
        StringBuffer buff = new StringBuffer("AgentConfig[");
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
    
}
