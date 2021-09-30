/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.mock.snmp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author ranger
 */
public class AgentConfigData {
    public URL m_moFile;
    public InetAddress m_listenAddr;
    public int m_listenPort;

    public AgentConfigData() {
    }
    
    protected AgentConfigData(String moFileSpec, String listenAddr, int listenPort) throws UnknownHostException, MalformedURLException {
        if (moFileSpec.contains("://") || moFileSpec.startsWith("file:")) {
            m_moFile = new URL(moFileSpec);
        } else {
            m_moFile = new URL("file:" + moFileSpec);
        }
        m_listenAddr = InetAddress.getByName(listenAddr);
        m_listenPort = listenPort;
    }

    public URL getMoFile() {
        return m_moFile;
    }

    public void setMoFile(URL moFile) {
        m_moFile = moFile;
    }

    public InetAddress getListenAddr() {
        return m_listenAddr;
    }

    public void setListenAddr(InetAddress listenAddr) {
        m_listenAddr = listenAddr;
    }

    public long getListenPort() {
        return m_listenPort;
    }

    public void setListenPort(int listenPort) {
        m_listenPort = listenPort;
    }
}
