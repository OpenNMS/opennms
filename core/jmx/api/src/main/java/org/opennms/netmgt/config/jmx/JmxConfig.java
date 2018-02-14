/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2017 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.config.jmx;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Jaxb root element for the jmx config.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
@XmlRootElement(name = "jmx-config")
@XmlAccessorType(XmlAccessType.NONE)
public class JmxConfig {
    private Set<MBeanServer> m_mBeanServer = new HashSet<>();

    @XmlElement(name = "mbean-server")
    public Set<MBeanServer> getMBeanServer() {
        return m_mBeanServer;
    }

    public void setMBeanServer(Set<MBeanServer> mBeanServer) {
        this.m_mBeanServer = mBeanServer;
    }

    public MBeanServer lookupMBeanServer(String ipAddress, int port) {
        for (MBeanServer mBeanServer : getMBeanServer()) {
            if (port == mBeanServer.getPort() && ipAddress.equals(mBeanServer.getIpAddress()))
                return mBeanServer;
        }
        return null;
    }

    public MBeanServer lookupMBeanServer(String ipAddress, String port) {
        return lookupMBeanServer(ipAddress, Integer.parseInt(port));
    }
}
