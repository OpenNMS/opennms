/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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
              
      StringBuffer sb = new StringBuffer();
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
              
      StringBuffer sb = new StringBuffer();
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
              
      StringBuffer sb = new StringBuffer();
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
