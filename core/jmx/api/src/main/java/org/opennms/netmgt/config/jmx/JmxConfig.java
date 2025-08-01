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
