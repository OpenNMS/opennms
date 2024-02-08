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

import org.opennms.core.utils.AlphaNumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class ReverseDnsRecord {
    private static final Logger LOG = LoggerFactory.getLogger(ReverseDnsRecord.class);

    String m_hostname;
    String m_zone;
    InetAddress m_ip;
    
    public ReverseDnsRecord(OnmsIpInterface ipInterface, int level) {

        OnmsSnmpInterface snmpInterface = ipInterface.getSnmpInterface();

        if (snmpInterface == null) {
            LOG.debug("Constructor: no SnmpInterface found");
            m_hostname=ipInterface.getNode().getLabel()+".";
        } else if ( snmpInterface.getIfName() != null ) {
            LOG.debug("Constructor: SnmpInterface found: ifname: {}", snmpInterface.getIfName());
            m_hostname=AlphaNumeric.parseAndReplace(snmpInterface.getIfName(),'-')+"-"+ipInterface.getNode().getLabel()+".";
        } else if (snmpInterface.getIfDescr() != null ) {
            LOG.debug("Constructor: SnmpInterface found: ifdescr: {}", snmpInterface.getIfDescr());
            m_hostname=AlphaNumeric.parseAndReplace(snmpInterface.getIfDescr(),'-')+"-"+ipInterface.getNode().getLabel()+".";
        } else {
            LOG.debug("Constructor: SnmpInterface found: ifindex: {}", snmpInterface.getIfDescr());
            m_hostname="ifindex-"+snmpInterface.getIfIndex()+"-"+ipInterface.getNode().getLabel()+".";            
        }
        LOG.debug("Constructor: set hostname: {}", m_hostname);

        m_ip = ipInterface.getIpAddress();
        LOG.debug("Constructor: set ip address: {}", m_ip);

        if (level == 1)
        	m_zone = ReverseDnsRecord.firstLevelZonefromInet4Address(m_ip.getAddress());
        else if (level == 2)
        	m_zone = ReverseDnsRecord.secondLevelZonefromInet4Address(m_ip.getAddress());
    	else
    		m_zone=ReverseDnsRecord.thirdLevelZonefromInet4Address(m_ip.getAddress());
        LOG.debug("Constructor: set zone: {}", m_zone);
    }

    public String getHostname() {
        return m_hostname;
    }

    public String getZone() {
        return m_zone;
    }

    public InetAddress getIp() {
        return m_ip;
    }
    
    public static String thirdLevelZonefromInet4Address(byte[] addr ) {
        if (addr.length != 4 && addr.length != 16)
            throw new IllegalArgumentException("array must contain " +
                                         "4 or 16 elements");
              
      final StringBuilder sb = new StringBuilder();
      if (addr.length == 4) {
          for (int i = addr.length - 2; i >= 0; i--) {
              sb.append(addr[i] & 0xFF);
              if (i > 0)
                  sb.append(".");
              }
          }
      sb.append(".in-addr.arpa.");
      return sb.toString();
    }
    
    public static String secondLevelZonefromInet4Address(byte[] addr ) {
        if (addr.length != 4 && addr.length != 16)
            throw new IllegalArgumentException("array must contain " +
                                         "4 or 16 elements");
              
      final StringBuilder sb = new StringBuilder();
      if (addr.length == 4) {
          for (int i = addr.length - 3; i >= 0; i--) {
              sb.append(addr[i] & 0xFF);
              if (i > 0)
                  sb.append(".");
              }
          }
      sb.append(".in-addr.arpa.");
      return sb.toString();
    }
    
    public static String firstLevelZonefromInet4Address(byte[] addr ) {
        if (addr.length != 4 && addr.length != 16)
            throw new IllegalArgumentException("array must contain " +
                                         "4 or 16 elements");
              
      final StringBuilder sb = new StringBuilder();
      if (addr.length == 4) {
          for (int i = addr.length - 4; i >= 0; i--) {
              sb.append(addr[i] & 0xFF);
              if (i > 0)
                  sb.append(".");
              }
          }
      sb.append(".in-addr.arpa.");
      return sb.toString();
    }


}
