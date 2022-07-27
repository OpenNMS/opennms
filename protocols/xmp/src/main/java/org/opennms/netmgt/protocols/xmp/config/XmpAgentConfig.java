/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.protocols.xmp.config;

/**
 * <p>XmpAgentConfig class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class XmpAgentConfig {
    /**
     * The TCP port on which the agent communicates
     */
    private int m_port;
    
    /**
     * The username used for authenticating to the agent
     */
    private String m_authenUser;
    
    /**
     * The timeout used when communicating with the agent
     */
    private long m_timeout;
    
    /**
     * The number of retries permitted when timeout expires
     */
    private int m_retry;

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * <p>getAuthenUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAuthenUser() {
        return m_authenUser;
    }

    /**
     * <p>setAuthenUser</p>
     *
     * @param authenUser a {@link java.lang.String} object.
     */
    public void setAuthenUser(String authenUser) {
        m_authenUser = authenUser;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return a long.
     */
    public long getTimeout() {
        return m_timeout;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout a long.
     */
    public void setTimeout(long timeout) {
        m_timeout = timeout;
    }

    /**
     * <p>getRetry</p>
     *
     * @return a int.
     */
    public int getRetry() {
        return m_retry;
    }

    /**
     * <p>setRetry</p>
     *
     * @param retries a int.
     */
    public void setRetry(int retries) {
        m_retry = retries;
    }
    
}
