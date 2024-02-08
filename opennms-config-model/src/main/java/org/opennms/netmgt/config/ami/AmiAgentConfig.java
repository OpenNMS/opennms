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
package org.opennms.netmgt.config.ami;

import java.net.InetAddress;
import java.util.Optional;

/**
 * <p>AmiAgentConfig class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AmiAgentConfig {
    /** Constant <code>DEFAULT_TIMEOUT=3000</code> */
    public static final int DEFAULT_TIMEOUT = 3000;
    /** Constant <code>DEFAULT_RETRIES=1</code> */
    public static final int DEFAULT_RETRIES = 1;
    /** Constant <code>DEFAULT_PASSWORD=""</code> */
    public static final String DEFAULT_PASSWORD = "";
    /** Constant <code>DEFAULT_USERNAME="opennms"</code> */
    public static final String DEFAULT_USERNAME="opennms";
    /** Constant <code>DEFAULT_PORT=5038</code> */
    public static final int DEFAULT_PORT = 5038;
    /** Constant <code>DEFAULT_TLS_PORT=5039</code> */
    public static final int DEFAULT_TLS_PORT = 5039;
    /** Constant <code>DEFAULT_USE_TLS=false</code> */
    public static final boolean DEFAULT_USE_TLS = false;
    
    private InetAddress m_address;
    private Integer m_timeout;
    private Integer m_retries;
    private String m_username;
    private String m_password;
    private Integer m_port;
    private Boolean m_useTls;
    
    String user = "";
	String pass = "";
	String matchType = "all";
    /**
     * <p>Constructor for AmiAgentConfig.</p>
     */
    public AmiAgentConfig() {
        setDefaults();
    }
    
    /**
     * <p>Constructor for AmiAgentConfig.</p>
     *
     * @param agentAddress a {@link java.net.InetAddress} object.
     */
    public AmiAgentConfig(InetAddress agentAddress) {
        m_address = agentAddress;
        setDefaults();
    }

    private void setDefaults() {
        m_timeout = DEFAULT_TIMEOUT;
        m_retries = DEFAULT_RETRIES;
        m_port = DEFAULT_PORT;
        m_useTls = DEFAULT_USE_TLS;
        m_username = DEFAULT_USERNAME;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder("AgentConfig[");
        buff.append("Address: "+m_address);
        buff.append(", Port: " +m_port);
        buff.append(", TLS: "+m_useTls);
        buff.append(", Username: "+String.valueOf(m_username)); //use valueOf to handle null values of m_username
        buff.append(", Password: "+String.valueOf(m_password)); //use valueOf to handle null values of m_password
        buff.append(", Timeout: "+m_timeout);
        buff.append(", Retries: "+m_retries);
        buff.append("]");
        return buff.toString();
    }


    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public Optional<InetAddress> getAddress() {
        return Optional.ofNullable(m_address);
    }

    /**
     * <p>setAddress</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    public void setAddress(InetAddress address) {
        m_address = address;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return an Integer.
     */
    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(m_timeout);
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout an Integer.
     */
    public void setTimeout(final Integer timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>getRetries</p>
     *
     * @return an Integer.
     */
    public Optional<Integer> getRetries() {
        return Optional.ofNullable(m_retries);
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries an Integer.
     */
    public void setRetries(final Integer retries) {
        m_retries = retries;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public Optional<String> getPassword() {
        return Optional.ofNullable(m_password);
    }


    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(final String password) {
        m_password = password;
    }

    /**
     * <p>getUsername</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public Optional<String> getUsername() {
        return Optional.ofNullable(m_username);
    }
    
    /**
     * <p>setUsername</p>
     *
     * @param username a {@link java.lang.String} object.
     */
    public void setUsername(final String username) {
    	m_username = username;
    }
    
    /**
     * <p>getPort</p>
     *
     * @return an Integer.
     */
    public Optional<Integer> getPort() {
        return Optional.ofNullable(m_port);
    }
    
    /**
     * <p>setPort</p>
     *
     * @param port an Integer.
     */
    public void setPort(final Integer port) {
        m_port = port;
    }
    
    /**
     * <p>getUseTls</p>
     *
     * @return a Boolean.
     */
    public Optional<Boolean> getUseTls() {
        return Optional.ofNullable(m_useTls);
    }

    /**
     * <p>setUseTls</p>
     *
     * @param useTls a boolean.
     */
    public void setUseTls(final Boolean useTls) {
        m_useTls = useTls;
    }
}
