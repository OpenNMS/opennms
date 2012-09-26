package org.opennms.netmgt.provision;

import java.net.InetAddress;

import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class ReverseDnsRecord {

    String m_hostname;
    String m_zone;
    InetAddress m_ip;
    
    public ReverseDnsRecord(OnmsIpInterface ipInterface) {

        OnmsSnmpInterface snmpInterface = ipInterface.getSnmpInterface();

        if (snmpInterface == null) {
            log().debug("Constructor: no SnmpInterface found");
            m_hostname=ipInterface.getNode().getLabel()+".";
        } else if ( snmpInterface.getIfName() != null ) {
            log().debug("Constructor: SnmpInterface found: ifname: " + snmpInterface.getIfName());
            m_hostname=AlphaNumeric.parseAndReplace(snmpInterface.getIfName(),'-')+"-"+ipInterface.getNode().getLabel()+".";
        } else if (snmpInterface.getIfDescr() != null ) {
            log().debug("Constructor: SnmpInterface found: ifdescr: " + snmpInterface.getIfDescr());
            m_hostname=AlphaNumeric.parseAndReplace(snmpInterface.getIfDescr(),'-')+"-"+ipInterface.getNode().getLabel()+".";
        } else {
            log().debug("Constructor: SnmpInterface found: ifindex: " + snmpInterface.getIfDescr());
            m_hostname="ifindex-"+snmpInterface.getIfIndex()+"-"+ipInterface.getNode().getLabel()+".";            
        }
        log().debug("Constructor: set hostname: " + m_hostname);

        m_ip = ipInterface.getIpAddress();
        log().debug("Constructor: set ip address: " + m_ip);

        m_zone=ReverseDnsRecord.thirdLevelZonefromInet4Address(m_ip.getAddress());
        log().debug("Constructor: set zone: " + m_zone);
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
    
    private static ThreadCategory log() {
        return ThreadCategory.getInstance(ReverseDnsRecord.class);
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
}
