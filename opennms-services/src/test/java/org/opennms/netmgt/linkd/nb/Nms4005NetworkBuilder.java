package org.opennms.netmgt.linkd.nb;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

public class Nms4005NetworkBuilder extends LinkdNetworkBuilder {

    protected static final String R1_IP = "10.1.1.2";
    protected static final String R1_NAME = "R1";
    protected static final String R1_SYSOID = ".1.3.6.1.4.1.9.1.122";
    protected static final String R1_LLDP_CHASSISID = "";
    
    protected static final Map<InetAddress,Integer> R1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> R1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R1_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R1_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,InetAddress> R1_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String R2_IP = "10.1.2.2";
    protected static final String R2_NAME = "R2";
    protected static final String R2_SYSOID = ".1.3.6.1.4.1.9.1.122";
    protected static final String R2_LLDP_CHASSISID = "";
    
    protected static final Map<InetAddress,Integer> R2_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> R2_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R2_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R2_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R2_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,InetAddress> R2_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String R3_IP = "10.1.3.2";
    protected static final String R3_NAME = "R3";
    protected static final String R3_SYSOID = ".1.3.6.1.4.1.9.1.122";
    protected static final String R3_LLDP_CHASSISID = "";
    
    protected static final Map<InetAddress,Integer> R3_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> R3_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R3_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R3_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R3_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,InetAddress> R3_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String R4_IP = "10.1.4.2";
    protected static final String R4_NAME = "R4";
    protected static final String R4_SYSOID = ".1.3.6.1.4.1.9.1.122";
    protected static final String R4_LLDP_CHASSISID = "";
    
    protected static final Map<InetAddress,Integer> R4_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> R4_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R4_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R4_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> R4_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,InetAddress> R4_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    static {
        R1_IP_IF_MAP.put(InetAddressUtils.addr("10.1.2.1"), 1);
        R1_IP_IF_MAP.put(InetAddressUtils.addr("10.1.3.1"), 2);
        R1_IP_IF_MAP.put(InetAddressUtils.addr("10.1.1.2"), 3);
        R1_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));
        R1_IF_NETMASK_MAP.put(2, InetAddressUtils.addr("255.255.255.0"));
        R1_IF_NETMASK_MAP.put(3, InetAddressUtils.addr("255.255.255.0"));
        R2_IP_IF_MAP.put(InetAddressUtils.addr("10.1.2.2"), 1);
        R2_IP_IF_MAP.put(InetAddressUtils.addr("10.1.5.1"), 2);
        R2_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));
        R2_IF_NETMASK_MAP.put(2, InetAddressUtils.addr("255.255.255.0"));
        R3_IP_IF_MAP.put(InetAddressUtils.addr("10.1.3.2"), 1);
        R3_IP_IF_MAP.put(InetAddressUtils.addr("10.1.4.1"), 2);
        R3_IP_IF_MAP.put(InetAddressUtils.addr("10.1.5.2"), 3);
        R3_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));
        R3_IF_NETMASK_MAP.put(2, InetAddressUtils.addr("255.255.255.0"));
        R3_IF_NETMASK_MAP.put(3, InetAddressUtils.addr("255.255.255.0"));
        R4_IP_IF_MAP.put(InetAddressUtils.addr("10.1.4.2"), 1);
        R4_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));

    	R1_IF_IFNAME_MAP.put(1, "Fa0/0");
    	R1_IF_IFDESCR_MAP.put(1, "FastEthernet0/0");
    	R1_IF_IFNAME_MAP.put(2, "Fa0/1");
    	R1_IF_IFDESCR_MAP.put(2, "FastEthernet0/1");
    	R1_IF_IFNAME_MAP.put(3, "Fa1/0");
    	R1_IF_IFDESCR_MAP.put(3, "FastEthernet1/0");
    	R1_IF_IFNAME_MAP.put(4, "Nu0");
    	R1_IF_IFDESCR_MAP.put(4, "Null0");
    	R2_IF_IFNAME_MAP.put(1, "Fa0/0");
    	R2_IF_IFNAME_MAP.put(2, "Fa0/1");
    	R2_IF_IFDESCR_MAP.put(2, "FastEthernet0/1");
    	R2_IF_IFDESCR_MAP.put(1, "FastEthernet0/0");
    	R2_IF_IFNAME_MAP.put(3, "Nu0");
    	R2_IF_IFDESCR_MAP.put(3, "Null0");
    	R3_IF_IFNAME_MAP.put(1, "Fa0/0");
    	R3_IF_IFDESCR_MAP.put(1, "FastEthernet0/0");
    	R3_IF_IFNAME_MAP.put(2, "Fa0/1");
    	R3_IF_IFDESCR_MAP.put(2, "FastEthernet0/1");
    	R3_IF_IFNAME_MAP.put(3, "Fa1/0");
    	R3_IF_IFDESCR_MAP.put(3, "FastEthernet1/0");
    	R3_IF_IFNAME_MAP.put(4, "Nu0");
    	R3_IF_IFDESCR_MAP.put(4, "Null0");
    	R4_IF_IFNAME_MAP.put(1, "Fa0/0");
    	R4_IF_IFDESCR_MAP.put(1, "FastEthernet0/0");
    	R4_IF_IFNAME_MAP.put(2, "Fa0/1");
    	R4_IF_IFDESCR_MAP.put(2, "FastEthernet0/1");
    	R4_IF_IFNAME_MAP.put(3, "Nu0");
    	R4_IF_IFDESCR_MAP.put(3, "Null0");
    }

    protected OnmsNode getR1() {
        return getNode(R1_NAME,R1_SYSOID,R1_IP,R1_IP_IF_MAP,R1_IF_IFNAME_MAP,R1_IF_MAC_MAP,R1_IF_IFDESCR_MAP,R1_IF_IFALIAS_MAP,R1_IF_NETMASK_MAP);
    }    

    protected OnmsNode getR2() {
        return getNode(R2_NAME,R1_SYSOID,R2_IP,R2_IP_IF_MAP,R2_IF_IFNAME_MAP,R2_IF_MAC_MAP,R2_IF_IFDESCR_MAP,R2_IF_IFALIAS_MAP,R2_IF_NETMASK_MAP);
    }    

    protected OnmsNode getR3() {
        return getNode(R3_NAME,R3_SYSOID,R3_IP,R3_IP_IF_MAP,R3_IF_IFNAME_MAP,R3_IF_MAC_MAP,R3_IF_IFDESCR_MAP,R3_IF_IFALIAS_MAP,R3_IF_NETMASK_MAP);
    }    

    protected OnmsNode getR4() {
        return getNode(R4_NAME,R4_SYSOID,R4_IP,R4_IP_IF_MAP,R4_IF_IFNAME_MAP,R4_IF_MAC_MAP,R4_IF_IFDESCR_MAP,R4_IF_IFALIAS_MAP,R4_IF_NETMASK_MAP);
    }    

}
