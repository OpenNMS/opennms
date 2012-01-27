/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


package org.opennms.mock.snmp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * <p>AgentConfigData class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AgentConfigData {
    public URL m_moFile;
    public InetAddress m_listenAddr;
    public int m_listenPort;

    /**
     * <p>Constructor for AgentConfigData.</p>
     */
    public AgentConfigData() {
    }
    
    /**
     * <p>Constructor for AgentConfigData.</p>
     *
     * @param moFileSpec a {@link java.lang.String} object.
     * @param listenAddr a {@link java.lang.String} object.
     * @param listenPort a long.
     * @throws java.net.UnknownHostException if any.
     * @throws java.net.MalformedURLException if any.
     */
    protected AgentConfigData(String moFileSpec, String listenAddr, int listenPort) throws UnknownHostException, MalformedURLException {
        if (moFileSpec.contains("://") || moFileSpec.startsWith("file:")) {
            m_moFile = new URL(moFileSpec);
        } else {
            m_moFile = new URL("file:" + moFileSpec);
        }
        m_listenAddr = InetAddress.getByName(listenAddr);
        m_listenPort = listenPort;
    }

    /**
     * <p>getMoFile</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public URL getMoFile() {
        return m_moFile;
    }

    /**
     * <p>setMoFile</p>
     *
     * @param moFile a {@link org.springframework.core.io.Resource} object.
     */
    public void setMoFile(URL moFile) {
        m_moFile = moFile;
    }

    /**
     * <p>getListenAddr</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getListenAddr() {
        return m_listenAddr;
    }

    /**
     * <p>setListenAddr</p>
     *
     * @param listenAddr a {@link java.net.InetAddress} object.
     */
    public void setListenAddr(InetAddress listenAddr) {
        m_listenAddr = listenAddr;
    }

    /**
     * <p>getListenPort</p>
     *
     * @return a long.
     */
    public long getListenPort() {
        return m_listenPort;
    }

    /**
     * <p>setListenPort</p>
     *
     * @param listenPort a long.
     */
    public void setListenPort(int listenPort) {
        m_listenPort = listenPort;
    }
}
