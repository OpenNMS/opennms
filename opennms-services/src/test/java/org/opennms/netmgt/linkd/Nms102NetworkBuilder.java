package org.opennms.netmgt.linkd;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

public class Nms102NetworkBuilder extends LinkdTestBuilder {

    protected static final String MIKROTIK_IP = "192.168.0.1";
    protected static final String MIKROTIK_NAME = "mikrotik";
    protected static final String MIKROTIK_SYSOID = ".1.3.6.1.4.1.14988.1";
    
    protected static final Map<InetAddress,Integer> MIKROTIK_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> MIKROTIK_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MIKROTIK_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MIKROTIK_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MIKROTIK_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,InetAddress> MIKROTIK_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String SAMSUNG_IP = "192.168.0.14";
    protected static final String SAMSUNG_NAME = "samsung";
    protected static final String SAMSUNG_SYSOID = ".1.3.6.1.4.1.236.11.5.1";
    
    protected static final Map<InetAddress,Integer> SAMSUNG_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> SAMSUNG_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SAMSUNG_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SAMSUNG_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SAMSUNG_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,InetAddress> SAMSUNG_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String MAC1_IP = "192.168.0.16";
    protected static final String MAC1_NAME = "mac1";
    protected static final String MAC1_SYSOID = ".1.3.6.1.4.1.8072.3.2.255";
    
    protected static final Map<InetAddress,Integer> MAC1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> MAC1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MAC1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MAC1_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MAC1_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,InetAddress> MAC1_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String MAC2_IP = "192.168.0.17";
    protected static final String MAC2_NAME = "mac2";
    protected static final String MAC2_SYSOID = ".1.3.6.1.4.1.8072.3.2.255";
    
    protected static final Map<InetAddress,Integer> MAC2_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> MAC2_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MAC2_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MAC2_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> MAC2_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,InetAddress> MAC2_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
    
    static {
        MIKROTIK_IP_IF_MAP.put(InetAddressUtils.addr("192.168.0.1"), 2);
        MIKROTIK_IP_IF_MAP.put(InetAddressUtils.addr("10.129.16.165"), 1);
        MIKROTIK_IF_IFNAME_MAP.put(2, "wlan1");
        MIKROTIK_IF_IFDESCR_MAP.put(2, "wlan1");
        MIKROTIK_IF_NETMASK_MAP.put(2, InetAddressUtils.addr("255.255.255.0"));
        MIKROTIK_IF_IFNAME_MAP.put(1, "ether1");
        MIKROTIK_IF_IFDESCR_MAP.put(1, "ether1");
        MIKROTIK_IF_IFALIAS_MAP.put(1, "salvatore");
        MIKROTIK_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.248.0"));
        
        SAMSUNG_IP_IF_MAP.put(InetAddressUtils.addr("192.168.0.14"), 1);
        SAMSUNG_IF_IFDESCR_MAP.put(1, "Embedded Ethernet Controller, 10/100 Mbps, v1.0, UTP RJ-45, connector A1, 10 half duplex");
        SAMSUNG_IF_IFNAME_MAP.put(1, "ethernet0");
        SAMSUNG_IF_IFDESCR_MAP.put(2, "Loopback Interface");
        SAMSUNG_IF_IFNAME_MAP.put(2, "loopback0");
        SAMSUNG_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));
        
        MAC1_IP_IF_MAP.put(InetAddressUtils.addr("192.168.0.16"), 5);
        MAC1_IF_IFNAME_MAP.put(4, "en0");
        MAC1_IF_IFDESCR_MAP.put(4, "en0");
        MAC1_IF_IFALIAS_MAP.put(4, "roberta");
        MAC1_IF_IFNAME_MAP.put(2, "gif0");
        MAC1_IF_IFDESCR_MAP.put(2, "gif0");
        MAC1_IF_IFNAME_MAP.put(3, "stf0");
        MAC1_IF_IFDESCR_MAP.put(3, "stf0");
        MAC1_IF_IFNAME_MAP.put(5, "en1");
        MAC1_IF_IFDESCR_MAP.put(5, "en1");
        MAC1_IF_IFALIAS_MAP.put(5, "salvatore is here");
        MAC1_IF_NETMASK_MAP.put(5, InetAddressUtils.addr("255.255.255.0"));
        MAC1_IF_IFNAME_MAP.put(6, "fw0");
        MAC1_IF_IFDESCR_MAP.put(6, "fw0");
        
        MAC2_IP_IF_MAP.put(InetAddressUtils.addr("192.168.0.17"), 6);
        MAC2_IF_IFDESCR_MAP.put(2, "gif0");
        MAC2_IF_IFDESCR_MAP.put(3, "stf0");
        MAC2_IF_IFDESCR_MAP.put(4, "en0");
        MAC2_IF_IFDESCR_MAP.put(5, "fw0");
        MAC2_IF_IFDESCR_MAP.put(6, "en1");
        MAC2_IF_IFNAME_MAP.put(2, "gif0");
        MAC2_IF_IFNAME_MAP.put(3, "stf0");
        MAC2_IF_IFNAME_MAP.put(4, "en0");
        MAC2_IF_IFNAME_MAP.put(5, "fw0");
        MAC2_IF_IFNAME_MAP.put(6, "en1");
        MAC2_IF_NETMASK_MAP.put(6, InetAddressUtils.addr("255.255.255.0"));
        
    }
    
    protected OnmsNode getMikrotik() {
        return getNode(MIKROTIK_NAME,MIKROTIK_SYSOID,MIKROTIK_IP,MIKROTIK_IP_IF_MAP,MIKROTIK_IF_IFNAME_MAP,MIKROTIK_IF_MAC_MAP,MIKROTIK_IF_IFDESCR_MAP,MIKROTIK_IF_IFALIAS_MAP,MIKROTIK_IF_NETMASK_MAP);        
    }

    protected OnmsNode getSamsung() {
        return getNode(SAMSUNG_NAME,SAMSUNG_SYSOID,SAMSUNG_IP,SAMSUNG_IP_IF_MAP,SAMSUNG_IF_IFNAME_MAP,SAMSUNG_IF_MAC_MAP,SAMSUNG_IF_IFDESCR_MAP,SAMSUNG_IF_IFALIAS_MAP,SAMSUNG_IF_NETMASK_MAP);        
    }

    protected OnmsNode getMac1() {
        return getNode(MAC1_NAME,MAC1_SYSOID,MAC1_IP,MAC1_IP_IF_MAP,MAC1_IF_IFNAME_MAP,MAC1_IF_MAC_MAP,MAC1_IF_IFDESCR_MAP,MAC1_IF_IFALIAS_MAP,MAC1_IF_NETMASK_MAP);        
    }

    protected OnmsNode getMac2() {
        return getNode(MAC2_NAME,MAC2_SYSOID,MAC2_IP,MAC2_IP_IF_MAP,MAC2_IF_IFNAME_MAP,MAC2_IF_MAC_MAP,MAC2_IF_IFDESCR_MAP,MAC2_IF_IFALIAS_MAP,MAC2_IF_NETMASK_MAP);        
    }

}
