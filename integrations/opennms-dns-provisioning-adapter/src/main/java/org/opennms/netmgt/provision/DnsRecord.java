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
package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

class DnsRecord {
    private static final Logger LOG = LoggerFactory.getLogger(DnsRecord.class);
    private InetAddress m_ip;
    private String m_hostname;
    private String m_zone;
    
    DnsRecord(OnmsNode node, int level) {

        LOG.debug("Constructor: set level: {}", level);

        OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        
        
        if (primaryInterface == null) {
            LOG.debug("Constructor: no primary interface found for nodeid: {}", node.getNodeId());
            Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
            for (OnmsIpInterface onmsIpInterface : ipInterfaces) {
                m_ip = onmsIpInterface.getIpAddress();
                break;
            }
        } else {
            LOG.debug("Constructor: primary interface found for nodeid: {}", node.getNodeId());
            m_ip = primaryInterface.getIpAddress();
        }
        LOG.debug("Constructor: set ip address: {}", m_ip);
        m_hostname = node.getLabel() + ".";
        LOG.debug("Constructor: set hostname: {}", m_hostname);

        String[] singlestat = m_hostname.split("\\.");
        if ( level == 0 || level >= singlestat.length){
            m_zone = m_hostname.substring(m_hostname.indexOf('.') + 1);
        } else {
        	String domain="";
        	for (int i=singlestat.length-level;i < singlestat.length;i++ ) {
        		domain+=singlestat[i];
        		domain+=".";
        	}
        	m_zone=domain;
        }
        LOG.debug("Constructor: set zone: {}", m_zone);

    }

    /**
     * <p>getIp</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getIp() {
        return m_ip;
    }

    /**
     * <p>getZone</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getZone() {
        return m_zone;
    }

    /**
     * <p>getHostname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostname() {
        return m_hostname;
    }
        
}
