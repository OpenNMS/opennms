/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.nb;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.SnmpInterfaceBuilder;
import org.opennms.netmgt.model.OnmsNode.NodeType;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * 
 */
public abstract class NmsNetworkBuilder {
    //NMS6802
    public final static String CISCOISIS_ROOT= "CISCOISIS";
    public static final String CISCOISIS_IP = "10.100.68.2";
    public static final String CISCOISIS_NAME = "cisco-ios-xr";
    public static final String CISCOISIS_SYSNAME = "cisco-ios-xr";
    public static final String CISCOISIS_SNMP_RESOURCE = "classpath:linkd/nms6802/"+CISCOISIS_NAME+"-walk.txt";
    public static final String CISCOISIS_SYSOID = ".1.3.6.1.4.1.9.1.2090";
    public static final String CISCOISIS_ISIS_SYS_ID = "093176090107";
    
    public static final Map<InetAddress,Integer> CISCOISIS_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      CISCOISIS_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      CISCOISIS_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      CISCOISIS_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      CISCOISIS_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCOISIS_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    
    
    //NMS7563
    public final static String HOMESERVER_ROOT= "HOMESERVER";
    public static final String HOMESERVER_IP = "192.168.88.1";
    public static final String HOMESERVER_NAME = "homeserver";
    public static final String HOMESERVER_SYSNAME = "server";
    public static final String HOMESERVER_SNMP_RESOURCE = "classpath:linkd/nms7563/"+HOMESERVER_NAME+"-walk.txt";
    public static final String HOMESERVER_SYSOID = ".1.3.6.1.4.1.8072.3.2.10";
    public static final String HOMESERVER_LLDP_CHASSID_ID="001ff207994f";
    public static final String HOMESERVER_LLDP_SYSNAME="server.home.schwartzkopff.org";
    public static final String HOMESERVER_CDP_GLOBAL_DEVICE_ID="001ff207994f";
    
    public static final Map<InetAddress,Integer> HOMESERVER_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      HOMESERVER_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      HOMESERVER_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      HOMESERVER_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      HOMESERVER_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> HOMESERVER_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String CISCO01_ROOT= "CISCO01";
    public static final String CISCO01_IP = "192.168.88.240";
    public static final String CISCO01_NAME = "cisco01";
    public static final String CISCO01_SYSNAME = "cisco01";
    public static final String CISCO01_SNMP_RESOURCE = "classpath:linkd/nms7563/"+CISCO01_NAME+"-walk.txt";
    public static final String CISCO01_SYSOID = ".1.3.6.1.4.1.9.1.798";
    public static final String CISCO01_LLDP_CHASSID_ID="aca016bf0200";
    public static final String CISCO01_LLDP_SYSNAME="cisco01";
    public static final String CISCO01_CDP_GLOBAL_DEVICE_ID="cisco01";

    public static final Map<InetAddress,Integer> CISCO01_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      CISCO01_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      CISCO01_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      CISCO01_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      CISCO01_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCO01_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String SWITCH02_ROOT= "SWITCH02";
    public static final String SWITCH02_IP = "192.168.88.241";
    public static final String SWITCH02_NAME = "switch02";
    public static final String SWITCH02_SYSNAME = "ProCurve Switch 2510B-24";
    public static final String SWITCH02_SNMP_RESOURCE = "classpath:linkd/nms7563/"+SWITCH02_NAME+"-walk.txt";
    public static final String SWITCH02_SYSOID = ".1.3.6.1.4.1.11.2.3.7.11.90";
    public static final String SWITCH02_LLDP_CHASSID_ID="001db3c50960";
    public static final String SWITCH02_LLDP_SYSNAME="ProCurve Switch 2510B-24";
    public static final String SWITCH02_CDP_GLOBAL_DEVICE_ID="ProCurve Switch 2510B-24(001db3-c50960)";

    public static final Map<InetAddress,Integer> SWITCH02_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      SWITCH02_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      SWITCH02_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      SWITCH02_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      SWITCH02_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> SWITCH02_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    //NMS0123
    public final static String ITPN0111_ROOT= "ITPN0111";
    public static final String ITPN0111_IP = "1.1.1.1";
    public static final String ITPN0111_NAME = "itpn0111";
    public static final String ITPN0111_SNMP_RESOURCE = "classpath:linkd/nms0123/"+ITPN0111_NAME+".txt";
    public static final String ITPN0111_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0111_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      ITPN0111_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0111_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0111_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0111_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> ITPN0111_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String ITPN0112_ROOT= "ITPN0112";
    public static final String ITPN0112_IP = "2.2.2.2";
    public static final String ITPN0112_NAME = "itpn0112";
    public static final String ITPN0112_SNMP_RESOURCE = "classpath:linkd/nms0123/"+ITPN0112_NAME+".txt";
    public static final String ITPN0112_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0112_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      ITPN0112_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0112_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0112_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0112_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> ITPN0112_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String ITPN0113_ROOT= "ITPN0113";
    public static final String ITPN0113_IP = "3.3.3.3";
    public static final String ITPN0113_NAME = "itpn0113";
    public static final String ITPN0113_SNMP_RESOURCE = "classpath:linkd/nms0123/"+ITPN0113_NAME+".txt";
    public static final String ITPN0113_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0113_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      ITPN0113_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0113_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0113_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0113_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> ITPN0113_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String ITPN0114_ROOT= "ITPN0114";
    public static final String ITPN0114_IP = "5.5.5.5";
    public static final String ITPN0114_NAME = "itpn0114";
    public static final String ITPN0114_SNMP_RESOURCE = "classpath:linkd/nms0123/"+ITPN0114_NAME+".txt";
    public static final String ITPN0114_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0114_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      ITPN0114_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0114_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0114_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0114_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> ITPN0114_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String ITPN0121_ROOT= "ITPN0121";
    public static final String ITPN0121_IP = "6.6.6.6";
    public static final String ITPN0121_NAME = "itpn0121";
    public static final String ITPN0121_SNMP_RESOURCE = "classpath:linkd/nms0123/"+ITPN0121_NAME+".txt";
    public static final String ITPN0121_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0121_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      ITPN0121_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0121_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0121_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0121_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> ITPN0121_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String ITPN0123_ROOT= "ITPN0123";
    public static final String ITPN0123_IP = "8.8.8.8";
    public static final String ITPN0123_NAME = "itpn0123";
    public static final String ITPN0123_SNMP_RESOURCE = "classpath:linkd/nms0123/"+ITPN0123_NAME+".txt";
    public static final String ITPN0123_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0123_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      ITPN0123_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0123_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0123_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0123_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> ITPN0123_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String ITPN0201_ROOT= "ITPN0201";
    public static final String ITPN0201_IP = "4.4.4.4";
    public static final String ITPN0201_NAME = "itpn0201";
    public static final String ITPN0201_SNMP_RESOURCE = "classpath:linkd/nms0123/"+ITPN0201_NAME+".txt";
    public static final String ITPN0201_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0201_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      ITPN0201_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0201_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0201_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0201_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> ITPN0201_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    public final static String ITPN0202_ROOT= "ITPN0202";
    public static final String ITPN0202_IP = "9.9.9.9";
    public static final String ITPN0202_NAME = "itpn0202";
    public static final String ITPN0202_SNMP_RESOURCE = "classpath:linkd/nms0123/"+ITPN0202_NAME+".txt";
    public static final String ITPN0202_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0202_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String>      ITPN0202_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0202_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0202_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String>      ITPN0202_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> ITPN0202_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();    

    //NMSDRAGONWAVE
    public final static String DW_ROOT= "DW";
    public static final String DW_IP = "10.103.1.1";
    public static final String DW_NAME = "dw";
    public static final String DW_SNMP_RESOURCE = "classpath:linkd/nms7777dw/"+DW_NAME+"-walk.txt";
    public static final String DW_SYSOID = ".1.3.6.1.4.1.7262.2.4";

    public static final Map<InetAddress,Integer> DW_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> DW_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> DW_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> DW_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> DW_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> DW_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    //NMS4943
    public static final String DLINK1_IP = "10.1.1.2";
    public static final String DLINK1_NAME = "dlink1";
    public static final String DLINK1_SNMP_RESOURCE = "classpath:linkd/nms4930/dlink_DES-3026.properties";

    public static final String DLINK2_IP = "10.1.2.2";
    public static final String DLINK2_NAME = "dlink2";
    public static final String DLINK2_SNMP_RESOURCE = "classpath:linkd/nms4930/dlink_DGS-3612G.properties";

    //NMS0001
    public final static String FROH_ROOT= "FROH";
    public static final String FROH_IP = "192.168.239.51";
    public static final String FROH_NAME = "froh";
    public static final String FROH_SNMP_RESOURCE = "classpath:linkd/nms0001/" + FROH_NAME + "-"+FROH_IP + "-walk.txt";
    public static final String FROH_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.29";
    
    public static final Map<InetAddress,Integer> FROH_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> FROH_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> FROH_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> FROH_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> FROH_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> FROH_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public final static String OEDIPUS_ROOT= "OEDIPUS";
    public static final String OEDIPUS_IP = "192.168.239.62";
    public static final String OEDIPUS_NAME = "oedipus";
    public static  final String OEDIPUS_SNMP_RESOURCE = "classpath:linkd/nms0001/" + OEDIPUS_NAME + "-"+OEDIPUS_IP + "-walk.txt";
    public static  final String OEDIPUS_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.11";
   
    public static final Map<InetAddress,Integer> OEDIPUS_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> OEDIPUS_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> OEDIPUS_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> OEDIPUS_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> OEDIPUS_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> OEDIPUS_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
        
    public static final String SIEGFRIE_ROOT= "SIEGFRIE";
    public static final String SIEGFRIE_IP = "192.168.239.54";
    public static final String SIEGFRIE_NAME = "siegfrie";
    public static  final String SIEGFRIE_SNMP_RESOURCE = "classpath:linkd/nms0001/" + SIEGFRIE_NAME + "-"+SIEGFRIE_IP + "-walk.txt";
    public static  final String SIEGFRIE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.11";
   
    public static final Map<InetAddress,Integer> SIEGFRIE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SIEGFRIE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SIEGFRIE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SIEGFRIE_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SIEGFRIE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SIEGFRIE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FROH_ISIS_SYS_ID     = "000110088500";
    public static final String OEDIPUS_ISIS_SYS_ID  = "000110255062";
    public static final String SIEGFRIE_ISIS_SYS_ID = "000110255054";

    //NMS0001
    public static final String Rluck001_ROOT= "Rluck001";
    public static final String Rluck001_IP = "10.4.79.250";
    public static final String Rluck001_NAME = "r-de-juet-luck-001";
    public static  final String Rluck001_SNMP_RESOURCE = "classpath:linkd/nms0002ciscojuniper/" + Rluck001_NAME +".txt";
    public static  final String Rluck001_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.39";
    public static final Map<InetAddress,Integer> Rluck001_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> Rluck001_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> Rluck001_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> Rluck001_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> Rluck001_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> Rluck001_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String Sluck001_ROOT= "Sluck001";
    public static final String Sluck001_IP = "10.4.68.215";
    public static final String Sluck001_NAME = "s-de-juet-luck-001";
    public static  final String Sluck001_SNMP_RESOURCE = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".txt";
    public static  final String Sluck001_SNMP_RESOURCE_VLAN100 = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".vlan100.txt";
    public static  final String Sluck001_SNMP_RESOURCE_VLAN950 = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".vlan950.txt";
    public static  final String Sluck001_SYSOID = ".1.3.6.1.4.1.9.1.564";
   
    public static final Map<InetAddress,Integer> Sluck001_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> Sluck001_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> Sluck001_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> Sluck001_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> Sluck001_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> Sluck001_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
        
    public static final String RPict001_ROOT= "RPict001";
    public static final String RPict001_IP = "10.140.252.57";
    public static final String RPict001_NAME = "r-ro-suce-pict-001";
    public static  final String RPict001_SNMP_RESOURCE = "classpath:linkd/nms0002UkRoFakeLink/" + RPict001_NAME+".txt";
    public static  final String RPict001_SYSOID = ".1.3.6.1.4.1.9.1.571";
   
    public static final Map<InetAddress,Integer> RPict001_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> RPict001_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RPict001_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RPict001_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RPict001_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> RPict001_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
    
    public static final String RNewt103_ROOT= "RNewt103";
    public static final String RNewt103_IP = "10.239.9.22";
    public static final String RNewt103_NAME = "r-uk-nott-newt-103";
    public static  final String RNewt103_SNMP_RESOURCE = "classpath:linkd/nms0002UkRoFakeLink/" + RNewt103_NAME+".txt";
    public static  final String RNewt103_SYSOID = ".1.3.6.1.4.1.9.1.571";
   
    public static final Map<InetAddress,Integer> RNewt103_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> RNewt103_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RNewt103_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RNewt103_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RNewt103_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> RNewt103_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String RDeEssnBrue_ROOT= "RDeEssnBrue";
    public static final String RDeEssnBrue_IP = "10.167.254.40";
    public static final String RDeEssnBrue_NAME = "r-de-essn-brue-001";
    public static  final String RDeEssnBrue_SNMP_RESOURCE = "classpath:linkd/nms0002ciscoalcatel2/" + RDeEssnBrue_NAME+ ".txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_400 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan400.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_450 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan450.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_451 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan451.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_452 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan452.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_453 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan453.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_500 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan500.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_501 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan501.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_502 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan502.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_503 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan503.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_504 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan504.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_505 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan505.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_506 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan506.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_507 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan507.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_508 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan508.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_509 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan509.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_510 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan510.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_511 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan511.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_512 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan512.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_513 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan513.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_514 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan514.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_515 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan515.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_516 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan516.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_517 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan517.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_518 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan518.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_519 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan519.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_520 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan520.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_750 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan750.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_751 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan751.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_752 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan752.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_753 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan753.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_754 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan754.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_755 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan755.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_756 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan756.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_757 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan757.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_758 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan758.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_760 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan760.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_800 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan800.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_801 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan801.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_850 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan850.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_851 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan851.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_852 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan852.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_900 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan900.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_950 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan950.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_951 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan951.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_952 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan952.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_953 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan953.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_954 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan954.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_955 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan955.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_956 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan956.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_957 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan957.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_958 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan958.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_959 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan959.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_960 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan960.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_961 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan961.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_962 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan962.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_963 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan963.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_964 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan964.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_965 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan965.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_966 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan966.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_967 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan967.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_968 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan968.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_969 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan969.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_970 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan970.txt";
    public static  final String RDeEssnBrue_SNMP_RESOURCE_VLAN_979 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan979.txt";
    public static  final String RDeEssnBrue_SYSOID = ".1.3.6.1.4.1.9.1.896";
   
    public static final Map<InetAddress,Integer> RDeEssnBrue_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> RDeEssnBrue_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RDeEssnBrue_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RDeEssnBrue_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RDeEssnBrue_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> RDeEssnBrue_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SDeEssnBrue081_ROOT= "SDeEssnBrue081";
    public static final String SDeEssnBrue081_IP = "10.165.62.91";
    public static final String SDeEssnBrue081_NAME = "s-de-essn-brue-081";
    public static  final String SDeEssnBrue081_SNMP_RESOURCE ="classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue081_NAME+ ".txt";
    public static  final String SDeEssnBrue081_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";
   
    public static final Map<InetAddress,Integer> SDeEssnBrue081_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SDeEssnBrue081_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue081_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue081_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue081_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SDeEssnBrue081_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SDeEssnBrue121_ROOT= "SDeEssnBrue121";
    public static final String SDeEssnBrue121_IP = "10.165.62.131";
    public static final String SDeEssnBrue121_NAME = "s-de-essn-brue-121";
    public static  final String SDeEssnBrue121_SNMP_RESOURCE ="classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue121_NAME+ ".txt";
    public static  final String SDeEssnBrue121_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";
   
    public static final Map<InetAddress,Integer> SDeEssnBrue121_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SDeEssnBrue121_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue121_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue121_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue121_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SDeEssnBrue121_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SDeEssnBrue142_ROOT= "SDeEssnBrue142";
    public static final String SDeEssnBrue142_IP = "10.165.62.152";
    public static final String SDeEssnBrue142_NAME = "s-de-essn-brue-142";
    public static  final String SDeEssnBrue142_SNMP_RESOURCE ="classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue142_NAME+ ".txt";
    public static  final String SDeEssnBrue142_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";
   
    public static final Map<InetAddress,Integer> SDeEssnBrue142_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SDeEssnBrue142_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue142_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue142_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue142_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SDeEssnBrue142_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SDeEssnBrue165_ROOT= "SDeEssnBrue165";
    public static final String SDeEssnBrue165_IP = "10.165.62.175";
    public static final String SDeEssnBrue165_NAME = "s-de-essn-brue-165";
    public static  final String SDeEssnBrue165_SNMP_RESOURCE ="classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue165_NAME+ ".txt";
    public static  final String SDeEssnBrue165_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";
   
    public static final Map<InetAddress,Integer> SDeEssnBrue165_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SDeEssnBrue165_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue165_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue165_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SDeEssnBrue165_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SDeEssnBrue165_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String RSeMalmNobe_ROOT= "RSeMalmNobe";
    public static final String RSeMalmNobe_IP = "10.111.253.9";
    public static final String RSeMalmNobe_NAME = "r-se-malm-nobe-013";
    public static final String RSeMalmNobe_SNMP_RESOURCE = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1002 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1002.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1003 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1003.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1004 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1004.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1005 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1005.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_3 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.3.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_357 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.357.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_360 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.360.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_389 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.389.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_399 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.399.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_450 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.450.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_451 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.451.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_452 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.452.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_453 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.453.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_454 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.454.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_500 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.500.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_501 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.501.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_502 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.502.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_503 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.503.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_504 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.504.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_505 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.505.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_506 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.506.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_507 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.507.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_508 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.508.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_600 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.600.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_601 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.601.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_602 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.602.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_603 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.603.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_604 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.604.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_605 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.605.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_606 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.606.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_750 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.750.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_800 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.800.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_801 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.801.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_835 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.835.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_836 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.836.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_850 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.850.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_851 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.851.txt";
    public static  final String RSeMalmNobe_SNMP_RESOURCE_VLAN_950 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.950.txt";
    public static  final String RSeMalmNobe_SYSOID = ".1.3.6.1.4.1.9.1.516";
   
    public static final Map<InetAddress,Integer> RSeMalmNobe_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> RSeMalmNobe_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RSeMalmNobe_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RSeMalmNobe_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> RSeMalmNobe_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> RSeMalmNobe_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SSeMalmNobe_ROOT= "SSeMalmNobe";
    public static final String SSeMalmNobe_IP = "10.108.191.171";
    public static final String SSeMalmNobe_NAME = "s-se-malm-nobe-561";
    public static final String SSeMalmNobe_SNMP_RESOURCE = "classpath:linkd/nms0002ciscoalcatel/s-se-malm-nobe-561.txt";
    public static  final String SSeMalmNobe_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";

    public static final Map<InetAddress,Integer> SSeMalmNobe_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SSeMalmNobe_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SSeMalmNobe_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SSeMalmNobe_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SSeMalmNobe_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SSeMalmNobe_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    //nms003 and nms17216
    public static final String ROUTER1_IP = "192.168.100.245";
    public static final String ROUTER1_NAME = "Router1";
    public static final String ROUTER1_SYSOID = ".1.3.6.1.4.1.9.1.576";
    public static final String ROUTER1_LLDP_CHASSISID = "";
    
    public static final Map<InetAddress,Integer> ROUTER1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> ROUTER1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER1_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER1_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String ROUTER2_IP = "192.168.100.241";
    public static final String ROUTER2_NAME = "Router2";
    public static final String ROUTER2_SYSOID = ".1.3.6.1.4.1.9.1.1045";
    public static final String ROUTER2_LLDP_CHASSISID = "";
    
    public static final Map<InetAddress,Integer> ROUTER2_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> ROUTER2_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER2_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER2_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER2_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String ROUTER3_IP = "172.16.50.1";
    public static final String ROUTER3_NAME = "Router3";
    public static final String ROUTER3_SYSOID = ".1.3.6.1.4.1.9.1.1045";
    public static final String ROUTER3_LLDP_CHASSISID = "";
    
    public static final Map<InetAddress,Integer> ROUTER3_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> ROUTER3_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER3_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER3_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER3_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String ROUTER4_IP = "10.10.10.1";
    public static final String ROUTER4_NAME = "Router4";
    public static final String ROUTER4_SYSOID = ".1.3.6.1.4.1.9.1.1045";
    public static final String ROUTER4_LLDP_CHASSISID = "";
    
    public static final Map<InetAddress,Integer> ROUTER4_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> ROUTER4_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER4_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER4_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> ROUTER4_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String SWITCH1_IP = "172.16.10.1";
    public static final String SWITCH1_NAME = "Switch1";
    public static final String SWITCH1_SYSOID = ".1.3.6.1.4.1.9.1.614";
    public static final String SWITCH1_LLDP_CHASSISID = "0016c8bd4d80";
    
    public static final Map<InetAddress,Integer> SWITCH1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> SWITCH1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH1_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH1_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String SWITCH2_IP = "172.16.10.2";
    public static final String SWITCH2_NAME = "Switch2";
    public static final String SWITCH2_SYSOID = ".1.3.6.1.4.1.9.1.696";
    public static final String SWITCH2_LLDP_CHASSISID = "0016c894aa80";
    
    public static final Map<InetAddress,Integer> SWITCH2_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> SWITCH2_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH2_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH2_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH2_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String SWITCH3_IP = "172.16.10.3";
    public static final String SWITCH3_NAME = "Switch3";
    public static final String SWITCH3_SYSOID = ".1.3.6.1.4.1.9.1.716";
    public static final String SWITCH3_LLDP_CHASSISID = "f4ea67ebdc00";
    
    public static final Map<InetAddress,Integer> SWITCH3_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> SWITCH3_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH3_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH3_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH3_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String SWITCH4_IP = "172.16.50.2";
    public static final String SWITCH4_NAME = "Switch4";
    public static final String SWITCH4_SYSOID = ".1.3.6.1.4.1.9.1.716";
    public static final String SWITCH4_LLDP_CHASSISID = "a4187504e400";
    
    public static final Map<InetAddress,Integer> SWITCH4_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> SWITCH4_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH4_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH4_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH4_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String SWITCH5_IP = "172.16.10.4";
    public static final String SWITCH5_NAME = "Switch5";
    public static final String SWITCH5_SYSOID = ".1.3.6.1.4.1.9.1.716";
    public static final String SWITCH5_LLDP_CHASSISID = "f4ea67f82980";
    
    public static final Map<InetAddress,Integer> SWITCH5_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> SWITCH5_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH5_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH5_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SWITCH5_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    public static final String ROUTER1_ROOT = "ROUTER1";
    public static final String ROUTER2_ROOT = "ROUTER2";
    public static final String ROUTER3_ROOT = "ROUTER3";
    public static final String ROUTER4_ROOT = "ROUTER4";
    
    public static final String SWITCH1_ROOT = "SWITCH1";
    public static final String SWITCH2_ROOT = "SWITCH2";
    public static final String SWITCH3_ROOT = "SWITCH3";
    public static final String SWITCH4_ROOT = "SWITCH4";
    public static final String SWITCH5_ROOT = "SWITCH5";

    public static final String ROUTER1_SNMP_RESOURCE = "classpath:linkd/nms17216/router1-walk.txt";
    public static final String ROUTER2_SNMP_RESOURCE = "classpath:linkd/nms17216/router2-walk.txt";
    public static final String ROUTER3_SNMP_RESOURCE = "classpath:linkd/nms17216/router3-walk.txt";
    public static final String ROUTER4_SNMP_RESOURCE = "classpath:linkd/nms17216/router4-walk.txt";

    public static final String SWITCH1_SNMP_RESOURCE = "classpath:linkd/nms17216/switch1-walk.txt";
    public static final String SWITCH2_SNMP_RESOURCE = "classpath:linkd/nms17216/switch2-walk.txt";
    public static final String SWITCH3_SNMP_RESOURCE = "classpath:linkd/nms17216/switch3-walk.txt";
    public static final String SWITCH4_SNMP_RESOURCE = "classpath:linkd/nms17216/switch4-walk.txt";
    public static final String SWITCH5_SNMP_RESOURCE = "classpath:linkd/nms17216/switch5-walk.txt";

    public static final String SWITCH1_SNMP_RESOURCE_003 = "classpath:linkd/nms003/switch1-walk.txt";
    public static final String SWITCH2_SNMP_RESOURCE_003 = "classpath:linkd/nms003/switch2-walk.txt";
    public static final String SWITCH3_SNMP_RESOURCE_003 = "classpath:linkd/nms003/switch3-walk.txt";

    //Nms007
    public static final String FireFly170_IP = "192.168.168.170";
    public static final String FireFly170_ROOT = "FireFly170";
    public static final String FireFly170_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly170_IP + ".txt";
    public static final String FireFly170_NAME = "FireFly_170";
    public static  final String FireFly170_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly170_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly170_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly170_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly170_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly170_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly170_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FireFly171_IP = "192.168.168.171";
    public static final String FireFly171_ROOT = "FireFly171";
    public static final String FireFly171_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly171_IP + ".txt";
    public static final String FireFly171_NAME = "FireFly_171";
    public static  final String FireFly171_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly171_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly171_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly171_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly171_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly171_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly171_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FireFly172_IP = "192.168.168.172";
    public static final String FireFly172_ROOT = "FireFly172";
    public static final String FireFly172_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly172_IP + ".txt";
    public static final String FireFly172_NAME = "FireFly_172";
    public static  final String FireFly172_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly172_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly172_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly172_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly172_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly172_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly172_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FireFly173_IP = "192.168.168.173";
    public static final String FireFly173_ROOT = "FireFly173";
    public static final String FireFly173_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly173_IP + ".txt";
    public static final String FireFly173_NAME = "FireFly_173";
    public static  final String FireFly173_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly173_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly173_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly173_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly173_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly173_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly173_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FireFly174_IP = "192.168.168.174";
    public static final String FireFly174_ROOT = "FireFly174";
    public static final String FireFly174_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly174_IP + ".txt";
    public static final String FireFly174_NAME = "FireFly_174";
    public static  final String FireFly174_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly174_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly174_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly174_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly174_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly174_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly174_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FireFly175_IP = "192.168.168.175";
    public static final String FireFly175_ROOT = "FireFly175";
    public static final String FireFly175_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly175_IP + ".txt";
    public static final String FireFly175_NAME = "FireFly_175";
    public static  final String FireFly175_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly175_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly175_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly175_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly175_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly175_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly175_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FireFly176_IP = "192.168.168.176";
    public static final String FireFly176_ROOT = "FireFly176";
    public static final String FireFly176_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly176_IP + ".txt";
    public static final String FireFly176_NAME = "FireFly_176";
    public static  final String FireFly176_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly176_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly176_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly176_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly176_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly176_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly176_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FireFly177_IP = "192.168.168.177";
    public static final String FireFly177_ROOT = "FireFly177";
    public static final String FireFly177_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly177_IP + ".txt";
    public static final String FireFly177_NAME = "FireFly_177";
    public static  final String FireFly177_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly177_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly177_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly177_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly177_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly177_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly177_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String FireFly189_IP = "192.168.168.189";
    public static final String FireFly189_ROOT = "FireFly189";
    public static final String FireFly189_SNMP_RESOURCE = "classpath:linkd/nms007/mib2_"+FireFly189_IP + ".txt";
    public static final String FireFly189_NAME = "FireFly_189";
    public static  final String FireFly189_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    public static final Map<InetAddress,Integer> FireFly189_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> FireFly189_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly189_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly189_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> FireFly189_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> FireFly189_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();


    //Nms101

    public static final String LAPTOP_ROOT = "LAPTOP";
    public static final String LAPTOP_SNMP_RESOURCE = "classpath:linkd/nms101/laptop.properties";
    public static final String LAPTOP_IP = "10.1.1.2";
    public static final String LAPTOP_NAME = "laptop";
    public static final String LAPTOP_SYSOID = ".1.3.6.1.4.1.8072.3.2.255";
    
    public static final Map<InetAddress,Integer> LAPTOP_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> LAPTOP_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> LAPTOP_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> LAPTOP_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> LAPTOP_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> LAPTOP_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String CISCO7200A_ROOT = "CISCO7200A";
    public static final String CISCO7200A_SNMP_RESOURCE = "classpath:linkd/nms101/cisco7200a.properties";
    public static final String CISCO7200A_IP = "10.1.1.1";
    public static final String CISCO7200A_NAME = "cisco7200ATM.befunk.com";
    public static final String CISCO7200A_SYSOID = ".1.3.6.1.4.1.9.1.222";
    
    public static final Map<InetAddress,Integer> CISCO7200A_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> CISCO7200A_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO7200A_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO7200A_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO7200A_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCO7200A_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String CISCO7200B_ROOT = "CISCO7200B";
    public static final String CISCO7200B_SNMP_RESOURCE = "classpath:linkd/nms101/cisco7200b.properties";
    public static final String CISCO7200B_IP = "10.1.2.2";
    public static final String CISCO7200B_NAME = "cisco7200";
    public static final String CISCO7200B_SYSOID = ".1.3.6.1.4.1.9.1.222";
    
    public static final Map<InetAddress,Integer> CISCO7200B_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> CISCO7200B_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO7200B_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO7200B_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO7200B_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCO7200B_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String CISCO3700_ROOT = "CISCO3700";
    public static final String CISCO3700_SNMP_RESOURCE = "classpath:linkd/nms101/cisco3700.properties";
    public static final String CISCO3700_IP = "10.1.3.2";
    public static final String CISCO3700_NAME = "cisco3700";
    public static final String CISCO3700_SYSOID = ".1.3.6.1.4.1.9.1.122";
    
    public static final Map<InetAddress,Integer> CISCO3700_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> CISCO3700_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO3700_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO3700_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO3700_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCO3700_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String CISCO2691_ROOT = "CISCO2691";
    public static final String CISCO2691_SNMP_RESOURCE = "classpath:linkd/nms101/cisco2691.properties";
    public static final String CISCO2691_IP = "10.1.4.2";
    public static final String CISCO2691_NAME = "cisco2691";
    public static final String CISCO2691_SYSOID = ".1.3.6.1.4.1.9.1.122";
    
    public static final Map<InetAddress,Integer> CISCO2691_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> CISCO2691_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO2691_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO2691_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO2691_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCO2691_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String CISCO1700B_ROOT = "CISCO1700B";
    public static final String CISCO1700B_SNMP_RESOURCE = "classpath:linkd/nms101/cisco1700b.properties";
    public static final String CISCO1700B_IP = "10.1.5.1";
    public static final String CISCO1700B_NAME = "cisco1700b";
    public static final String CISCO1700B_SYSOID = ".1.3.6.1.4.1.9.1.200";
    
    public static final Map<InetAddress,Integer> CISCO1700B_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> CISCO1700B_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO1700B_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO1700B_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO1700B_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCO1700B_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String CISCO3600_ROOT = "CISCO3600";
    public static final String CISCO3600_SNMP_RESOURCE = "classpath:linkd/nms101/cisco3600.properties";
    public static final String CISCO3600_IP = "10.1.6.2";
    public static final String CISCO3600_NAME = "cisco3600";
    public static final String CISCO3600_SYSOID = ".1.3.6.1.4.1.9.1.122";
    
    public static final Map<InetAddress,Integer> CISCO3600_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> CISCO3600_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO3600_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO3600_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO3600_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCO3600_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String CISCO1700_ROOT = "CISCO1700";
    public static final String CISCO1700_SNMP_RESOURCE = "classpath:linkd/nms101/cisco1700.properties";
    public static final String CISCO1700_IP = "10.1.5.2";
    public static final String CISCO1700_NAME = "cisco1700";
    public static final String CISCO1700_SYSOID = ".1.3.6.1.4.1.9.1.200";
    
    public static final Map<InetAddress,Integer> CISCO1700_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> CISCO1700_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO1700_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO1700_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO1700_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> CISCO1700_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String EXAMPLECOM_IP = "192.168.1.10";
    public static final String EXAMPLECOM_NAME = "test.example.com";
    public static final String EXAMPLECOM_SYSOID = ".1.3.6.1.4.1.1724.81";

    public static final Map<InetAddress,Integer> EXAMPLECOM_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> EXAMPLECOM_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> EXAMPLECOM_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> EXAMPLECOM_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> EXAMPLECOM_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> EXAMPLECOM_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    //Nms10205
    public static InetAddress MUMBAI_OSPF_ID;
    public static InetAddress DELHI_OSPF_ID;
    public static InetAddress BANGALORE_OSPF_ID;
    public static InetAddress MYSORE_OSPF_ID;
    public static InetAddress BAGMANE_OSPF_ID;
    public static InetAddress SPACE_EX_SW1_OSPF_ID;
    public static InetAddress SPACE_EX_SW2_OSPF_ID;
    public static InetAddress J6350_42_OSPF_ID;
    public static InetAddress SRX_100_OSPF_ID;
    static {
        MUMBAI_OSPF_ID  = InetAddressUtils.addr("192.168.5.1");
        DELHI_OSPF_ID  = InetAddressUtils.addr("192.168.7.1");
        BANGALORE_OSPF_ID  = InetAddressUtils.addr("192.168.9.1");
        MYSORE_OSPF_ID = InetAddressUtils.addr("192.168.22.1");
        BAGMANE_OSPF_ID = InetAddressUtils.addr("192.168.20.1");
        SPACE_EX_SW1_OSPF_ID = InetAddressUtils.addr("10.205.56.1");
        SPACE_EX_SW2_OSPF_ID = InetAddressUtils.addr("10.205.56.2");
        J6350_42_OSPF_ID = InetAddressUtils.addr("10.205.56.42");
        SRX_100_OSPF_ID = InetAddressUtils.addr("0.0.0.0");
    }

    public static final String MUMBAI_IP = "10.205.56.5";
    public static final String MUMBAI_NAME = "Mumbai";
    public static final String MUMBAI_ROOT = "MUMBAI";
    public static final String MUMBAI_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +MUMBAI_NAME+"_"+MUMBAI_IP+".txt";
    public static final String MUMBAI_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +MUMBAI_NAME+"_"+MUMBAI_IP+".txt";
    public static  final String MUMBAI_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.9";

    public static  final Map<InetAddress,Integer> MUMBAI_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> MUMBAI_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> MUMBAI_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> MUMBAI_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> MUMBAI_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> MUMBAI_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String CHENNAI_IP = "10.205.56.6";
    public static final String CHENNAI_NAME = "Chennai";
    public static final String CHENNAI_ROOT = "CHENNAI";
    public static final String CHENNAI_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +CHENNAI_NAME+"_"+CHENNAI_IP+".txt";
    public static final String CHENNAI_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +CHENNAI_NAME+"_"+CHENNAI_IP+".txt";
    public static  final String CHENNAI_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.25";

    public static  final Map<InetAddress,Integer> CHENNAI_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> CHENNAI_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> CHENNAI_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> CHENNAI_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> CHENNAI_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> CHENNAI_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String DELHI_IP = "10.205.56.7";
    public static final String DELHI_NAME = "Delhi";
    public static final String DELHI_ROOT = "DELHI";
    public static final String DELHI_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +DELHI_NAME+"_"+DELHI_IP+".txt";
    public static final String DELHI_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +DELHI_NAME+"_"+DELHI_IP+".txt";
    public static  final String DELHI_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.29";

    public static  final Map<InetAddress,Integer> DELHI_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> DELHI_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> DELHI_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> DELHI_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> DELHI_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> DELHI_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String BANGALORE_IP = "10.205.56.9";
    public static final String BANGALORE_NAME = "Bangalore";
    public static final String BANGALORE_ROOT = "BANGALORE";
    public static final String BANGALORE_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +BANGALORE_NAME+"_"+BANGALORE_IP+".txt";
    public static final String BANGALORE_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +BANGALORE_NAME+"_"+BANGALORE_IP+".txt";
    public static  final String BANGALORE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.10";

    public static  final Map<InetAddress,Integer> BANGALORE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> BANGALORE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> BANGALORE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> BANGALORE_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> BANGALORE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> BANGALORE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String MYSORE_IP = "10.205.56.22";
    public static final String MYSORE_NAME = "Mysore";
    public static final String MYSORE_ROOT = "MYSORE";
    public static final String MYSORE_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +MYSORE_NAME+"_"+MYSORE_IP+".txt";
    public static final String MYSORE_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +MYSORE_NAME+"_"+MYSORE_IP+".txt";
    public static  final String MYSORE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.11";

    public static  final Map<InetAddress,Integer> MYSORE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> MYSORE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> MYSORE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> MYSORE_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> MYSORE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> MYSORE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String BAGMANE_IP = "10.205.56.20";
    public static final String BAGMANE_NAME = "Bagmane";
    public static final String BAGMANE_ROOT = "BAGMANE";
    public static final String BAGMANE_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +BAGMANE_NAME+"_"+BAGMANE_IP+".txt";
    public static final String BAGMANE_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +BAGMANE_NAME+"_"+BAGMANE_IP+".txt";
    public static  final String BAGMANE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.57";

    public static  final Map<InetAddress,Integer> BAGMANE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> BAGMANE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> BAGMANE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> BAGMANE_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> BAGMANE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> BAGMANE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SPACE_EX_SW1_IP = "10.205.56.1";
    public static final String SPACE_EX_SW1_NAME = "Space-EX-SW1";
    public static final String SPACE_EX_SW1_ROOT = "SPACE_EX_SW1";
    public static final String SPACE_EX_SW1_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +SPACE_EX_SW1_NAME+"_"+SPACE_EX_SW1_IP+".txt";
    public static final String SPACE_EX_SW1_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +SPACE_EX_SW1_NAME+"_"+SPACE_EX_SW1_IP+".txt";
    public static  final String SPACE_EX_SW1_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.30";

    public static  final Map<InetAddress,Integer> SPACE_EX_SW1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SPACE_EX_SW1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SPACE_EX_SW1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SPACE_EX_SW1_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SPACE_EX_SW1_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SPACE_EX_SW1_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SPACE_EX_SW2_IP = "10.205.56.2";
    public static final String SPACE_EX_SW2_NAME = "Space-EX-SW2";
    public static final String SPACE_EX_SW2_ROOT = "SPACE_EX_SW2";
    public static final String SPACE_EX_SW2_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +SPACE_EX_SW2_NAME+"_"+SPACE_EX_SW2_IP+".txt";
    public static final String SPACE_EX_SW2_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +SPACE_EX_SW2_NAME+"_"+SPACE_EX_SW2_IP+".txt";
    public static  final String SPACE_EX_SW2_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static  final Map<InetAddress,Integer> SPACE_EX_SW2_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SPACE_EX_SW2_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SPACE_EX_SW2_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SPACE_EX_SW2_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SPACE_EX_SW2_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SPACE_EX_SW2_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String J6350_41_IP = "10.205.56.41";
    public static final String J6350_41_NAME = "J6350-41";
    public static final String J6350_41_ROOT = "J6350_41";
    public static final String J6350_41_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +J6350_41_NAME+"_"+J6350_41_IP+".txt";
    public static final String J6350_41_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +J6350_41_NAME+"_"+J6350_41_IP+".txt";
    public static  final String J6350_41_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.20";

    public static  final Map<InetAddress,Integer> J6350_41_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> J6350_41_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> J6350_41_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> J6350_41_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> J6350_41_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> J6350_41_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String J6350_42_IP = "10.205.56.42";
    public static final String J6350_42_NAME = "J6350-2";
    public static final String J6350_42_ROOT = "J6350_42";
    public static final String J6350_42_SNMP_RESOURCE   = "classpath:linkd/nms10205/" +"J6350-42_"+J6350_42_IP+".txt";
    public static final String J6350_42_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/"+"J6350-42_"+J6350_42_IP+".txt";
    public static  final String J6350_42_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.20";
    public static final String J6350_42_LLDP_CHASSISID = "2c6bf55dc100";
    
    public static  final Map<InetAddress,Integer> J6350_42_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> J6350_42_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> J6350_42_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> J6350_42_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> J6350_42_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> J6350_42_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SRX_100_IP = "10.205.56.23";
    public static final String SRX_100_NAME = "SRX_56.23";
    public static final String SRX_100_ROOT = "SRX_100";
    public static final String SRX_100_SNMP_RESOURCE   = "classpath:linkd/nms10205/" +"SRX-100_"+SRX_100_IP+".txt";
    public static final String SRX_100_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/"+"SRX-100_"+SRX_100_IP+".txt";
    public static  final String SRX_100_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.41";

    public static  final Map<InetAddress,Integer> SRX_100_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SRX_100_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SRX_100_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SRX_100_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SRX_100_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SRX_100_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SSG550_IP = "10.205.35.100";
    public static final String SSG550_NAME = "SSG550";
    public static final String SSG550_ROOT = "SSG550";
    public static final String SSG550_SNMP_RESOURCE   = "classpath:linkd/nms10205/"  +SSG550_NAME+"_"+SSG550_IP+".txt";
    public static final String SSG550_SNMP_RESOURCE_B = "classpath:linkd/nms10205b/" +SSG550_NAME+"_"+SSG550_IP+".txt";
    public static  final String SSG550_SYSOID = ".1.3.6.1.4.1.3224.1.51";

    public static  final Map<InetAddress,Integer> SSG550_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static  final Map<Integer,String> SSG550_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SSG550_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SSG550_IF_MAC_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,String> SSG550_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static  final Map<Integer,InetAddress> SSG550_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    //Nms1055
    public static final String PENROSE_IP = "10.155.69.16";
    public static final String PENROSE_NAME = "penrose-mx480";
    public static final String PENROSE_ROOT = "PENROSE";
    public static final String PENROSE_SNMP_RESOURCE   = "classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt";
    public static final String PENROSE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.25";
    public static final String PENROSE_LLDP_CHASSISID = "80711f8fafc0";

    public static final Map<InetAddress,Integer> PENROSE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> PENROSE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> PENROSE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> PENROSE_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> PENROSE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> PENROSE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String DELAWARE_IP = "10.155.69.17";
    public static final String DELAWARE_NAME = "delaware";
    public static final String DELAWARE_ROOT = "DELAWARE";
    public static final String DELAWARE_SNMP_RESOURCE   = "classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt";
    public static final String DELAWARE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.25";
    public static final String DELAWARE_LLDP_CHASSISID = "0022830957c0";

    public static final Map<InetAddress,Integer> DELAWARE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> DELAWARE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> DELAWARE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> DELAWARE_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> DELAWARE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> DELAWARE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String PHOENIX_IP = "10.155.69.42";
    public static final String PHOENIX_NAME = "phoenix-mx80";
    public static final String PHOENIX_ROOT = "PHOENIX";
    public static final String PHOENIX_SNMP_RESOURCE   = "classpath:linkd/nms1055/"+PHOENIX_NAME+"_"+PHOENIX_IP+".txt";
    public static final String PHOENIX_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.57";
    public static final String PHOENIX_LLDP_CHASSISID = "80711fc414c0";

    public static final Map<InetAddress,Integer> PHOENIX_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> PHOENIX_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> PHOENIX_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> PHOENIX_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> PHOENIX_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> PHOENIX_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();


    public static final String AUSTIN_IP = "10.155.69.43";
    public static final String AUSTIN_NAME = "Austin";
    public static final String AUSTIN_ROOT = "AUSTIN";
    public static final String AUSTIN_SNMP_RESOURCE   = "classpath:linkd/nms1055/"+AUSTIN_NAME+"_"+AUSTIN_IP+".txt";
    public static final String AUSTIN_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.57";
    public static final String AUSTIN_LLDP_CHASSISID = "80711fc413c0";

    public static final Map<InetAddress,Integer> AUSTIN_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> AUSTIN_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> AUSTIN_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> AUSTIN_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> AUSTIN_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> AUSTIN_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SANJOSE_IP = "10.155.69.12";
    public static final String SANJOSE_NAME = "sanjose-mx240";
    public static final String SANJOSE_ROOT = "SANJOSE";
    public static final String SANJOSE_SNMP_RESOURCE   = "classpath:linkd/nms1055/"+SANJOSE_NAME+"_"+SANJOSE_IP+".txt";
    public static final String SANJOSE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.29";
    public static final String SANJOSE_LLDP_CHASSISID = "002283d857c0";

    public static final Map<InetAddress,Integer> SANJOSE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> SANJOSE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SANJOSE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SANJOSE_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SANJOSE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> SANJOSE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String RIOVISTA_IP = "10.155.69.107";
    public static final String RIOVISTA_NAME = "Riovista-ce";
    public static final String RIOVISTA_ROOT = "RIOVISTA";
    public static final String RIOVISTA_SNMP_RESOURCE   = "classpath:linkd/nms1055/"+RIOVISTA_NAME+"_"+RIOVISTA_IP+".txt";
    public static final String RIOVISTA_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.10";
    public static final String RIOVISTA_LLDP_CHASSISID = "001f12373dc0";

    public static final Map<InetAddress,Integer> RIOVISTA_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> RIOVISTA_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> RIOVISTA_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> RIOVISTA_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> RIOVISTA_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> RIOVISTA_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    //nms102
    public static final String MIKROTIK_IP = "192.168.0.1";
    public static final String MIKROTIK_NAME = "mikrotik";
    public static final String MIKROTIK_SNMP_RESOURCE = "classpath:linkd/nms102/"+MIKROTIK_NAME+"-"+MIKROTIK_IP+"-walk.txt";
    public static final String MIKROTIK_SYSOID = ".1.3.6.1.4.1.14988.1";
    
    public static final Map<InetAddress,Integer> MIKROTIK_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> MIKROTIK_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MIKROTIK_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MIKROTIK_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MIKROTIK_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> MIKROTIK_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String SAMSUNG_IP = "192.168.0.14";
    public static final String SAMSUNG_NAME = "samsung";
    public static final String SAMSUNG_SNMP_RESOURCE = "classpath:linkd/nms102/"+SAMSUNG_NAME+"-"+SAMSUNG_IP+"-walk.txt";
    public static final String SAMSUNG_SYSOID = ".1.3.6.1.4.1.236.11.5.1";
    
    public static final Map<InetAddress,Integer> SAMSUNG_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> SAMSUNG_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SAMSUNG_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SAMSUNG_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> SAMSUNG_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> SAMSUNG_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String MAC1_IP = "192.168.0.16";
    public static final String MAC1_NAME = "mac1";
    public static final String MAC1_SNMP_RESOURCE = "classpath:linkd/nms102/"+"mac-"+MAC1_IP+"-walk.txt";
    public static final String MAC1_SYSOID = ".1.3.6.1.4.1.8072.3.2.255";
    
    public static final Map<InetAddress,Integer> MAC1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> MAC1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MAC1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MAC1_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MAC1_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> MAC1_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String MAC2_IP = "192.168.0.17";
    public static final String MAC2_NAME = "mac2";
    public static final String MAC2_SNMP_RESOURCE = "classpath:linkd/nms102/"+"mac-"+MAC2_IP+"-walk.txt";
    public static final String MAC2_SYSOID = ".1.3.6.1.4.1.8072.3.2.255";
    
    public static final Map<InetAddress,Integer> MAC2_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> MAC2_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MAC2_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MAC2_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> MAC2_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> MAC2_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    //nms4005
    public static final String R1_IP = "10.1.1.2";
    public static final String R1_NAME = "R1";
    public static final String R1_SYSOID = ".1.3.6.1.4.1.9.1.122";
    public static final String R1_LLDP_CHASSISID = "";
    
    public static final Map<InetAddress,Integer> R1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> R1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R1_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R1_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> R1_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String R2_IP = "10.1.2.2";
    public static final String R2_NAME = "R2";
    public static final String R2_SYSOID = ".1.3.6.1.4.1.9.1.122";
    public static final String R2_LLDP_CHASSISID = "";
    
    public static final Map<InetAddress,Integer> R2_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> R2_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R2_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R2_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R2_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> R2_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String R3_IP = "10.1.3.2";
    public static final String R3_NAME = "R3";
    public static final String R3_SYSOID = ".1.3.6.1.4.1.9.1.122";
    public static final String R3_LLDP_CHASSISID = "";
    
    public static final Map<InetAddress,Integer> R3_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> R3_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R3_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R3_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R3_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> R3_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    public static final String R4_IP = "10.1.4.2";
    public static final String R4_NAME = "R4";
    public static final String R4_SYSOID = ".1.3.6.1.4.1.9.1.122";
    public static final String R4_LLDP_CHASSISID = "";
    
    public static final Map<InetAddress,Integer> R4_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> R4_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R4_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R4_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> R4_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,InetAddress> R4_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
    
    //nms7467
    
	public static final String WORKSTATION_NAME         = "workstation";
	public static final String WORKSTATION_IP           = "172.20.1.101";
	public static final String WORKSTATION_MAC          = "60334b0817a8";

	public static final String ACCESSPOINT_NAME         = "mrgarrison.internal.opennms.com";
	public static final String ACCESSPOINT_IP           = "172.20.1.5";

    public static final String CISCO_C870_NAME          = "mrmakay.internal.opennms.com";
    public static final String CISCO_C870_IP_PRIMARY    = "10.255.255.2";
    public static final String CISCO_C870_IP            = "172.20.1.1";
    public static final String CISCO_C870_BRIDGEID      = "00000c83d9a8";
    public static final String CISCO_C870_SYSOID        = ".1.3.6.1.4.1.9.1.569";
    public static final String CISCO_C870_ROOT          = "CISCO_C870";
    public static final String CISCO_C870_SNMP_RESOURCE = "classpath:linkd/nms7467/"+CISCO_C870_IP+"-walk.txt";
    
    public static final Map<Integer, String> CISCO_C870_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    public static final Map<Integer, String> CISCO_C870_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer, String> CISCO_C870_IF_MAC_MAP = new HashMap<Integer, String>();
    public static final Map<InetAddress, Integer> CISCO_C870_IP_IF_MAP = new HashMap<InetAddress, Integer>();

    public static final String CISCO_WS_C2948_NAME         = "ciscoswitch";
    public static final String CISCO_WS_C2948_IP       = "172.20.1.7";
    public static final String CISCO_WS_C2948_BRIDGEID      = "0002baaacc00";
    public static final String CISCO_WS_C2948_SYSOID        = ".1.3.6.1.4.1.9.5.42";
    public static final String CISCO_WS_C2948_ROOT          = "CISCO_WS_C2948";
    public static final String CISCO_WS_C2948_SNMP_RESOURCE = "classpath:linkd/nms7467/"+CISCO_WS_C2948_IP+"-walk.txt";

    public static final Map<InetAddress,Integer> CISCO_WS_C2948_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    public static final Map<Integer,String> CISCO_WS_C2948_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer,String> CISCO_WS_C2948_IF_MAC_MAP = new HashMap<Integer, String>();

    public static final String NETGEAR_SW_108_NAME          = "ng108switch";
    public static final String NETGEAR_SW_108_IP            = "172.20.1.8";
    public static final String NETGEAR_SW_108_BRIDGEID      = "00223ff00b7b";
    public static final String NETGEAR_SW_108_SYSOID        = ".1.3.6.1.4.1.4526.100.4.8";
    public static final String NETGEAR_SW_108_ROOT          = "NETGEAR_SW_108";
    public static final String NETGEAR_SW_108_SNMP_RESOURCE = "classpath:linkd/nms7467/"+NETGEAR_SW_108_IP+"-walk.txt";

    public static final Map<InetAddress,Integer> NETGEAR_SW_108_IP_IF_MAP = new HashMap<InetAddress, Integer>();
    public static final Map<Integer, String> NETGEAR_SW_108_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer, String> NETGEAR_SW_108_IF_MAC_MAP = new HashMap<Integer, String>();

    public static final String LINUX_UBUNTU_NAME          = "linuxubuntu";
    public static final String LINUX_UBUNTU_IP            = "172.20.1.14";
    public static final String LINUX_UBUNTU_SYSOID        = ".1.3.6.1.4.1.8072.3.2.10";
    public static final String LINUX_UBUNTU_ROOT          = "LINUX_UBUNTU";
    public static final String LINUX_UBUNTU_SNMP_RESOURCE = "classpath:linkd/nms7467/"+LINUX_UBUNTU_IP+"-walk.txt";
    
    public static final Map<InetAddress, Integer> LINUX_UBUNTU_IP_IF_MAP = new HashMap<InetAddress, Integer>();
    public static final Map<Integer, String> LINUX_UBUNTU_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer, String> LINUX_UBUNTU_IF_MAC_MAP = new HashMap<Integer, String>();

    public static final String DARWIN_10_8_NAME          = "mac";
    public static final String DARWIN_10_8_IP            = "172.20.1.28";
    public static final String DARWIN_10_8_SYSOID        = ".1.3.6.1.4.1.8072.3.2.255";
    public static final String DARWIN_10_8_ROOT          = "DARWIN_10_8";
    public static final String DARWIN_10_8_SNMP_RESOURCE = "classpath:linkd/nms7467/"+DARWIN_10_8_IP+"-walk.txt";
    
    public static final Map<InetAddress,Integer> DARWIN_10_8_IP_IF_MAP = new HashMap<InetAddress, Integer>();
    public static final Map<Integer, String> DARWIN_10_8_IF_IFNAME_MAP = new HashMap<Integer, String>();
    public static final Map<Integer, String> DARWIN_10_8_IF_MAC_MAP = new HashMap<Integer, String>();

    NetworkBuilder m_networkBuilder;

    NetworkBuilder getNetworkBuilder() {
        if ( m_networkBuilder == null )
            m_networkBuilder = new NetworkBuilder();
        return m_networkBuilder;
    }

    OnmsNode getNode(String name, String sysoid, String primaryip,
            Map<InetAddress, Integer> ipinterfacemap,
            Map<Integer,String> ifindextoifnamemap,
            Map<Integer,String> ifindextomacmap, 
            Map<Integer,String> ifindextoifdescrmap,
            Map<Integer,String> ifindextoifalias) {
        return getNode(name, sysoid, primaryip, ipinterfacemap, ifindextoifnamemap, ifindextomacmap, ifindextoifdescrmap, ifindextoifalias, new HashMap<Integer, InetAddress>());
    }
    
    OnmsNode getNode(String name, String sysoid, String primaryip,
            Map<InetAddress, Integer> ipinterfacemap,
            Map<Integer,String> ifindextoifnamemap,
            Map<Integer,String> ifindextomacmap, 
            Map<Integer,String> ifindextoifdescrmap,
            Map<Integer,String> ifindextoifalias, 
            Map<Integer,InetAddress>ifindextonetmaskmap)
    {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(name).setForeignSource("linkd").setForeignId(name).setSysObjectId(sysoid).setSysName(name).setType(NodeType.ACTIVE);
        final Map<Integer, SnmpInterfaceBuilder> ifindexsnmpbuildermap = new HashMap<Integer, SnmpInterfaceBuilder>();
        for (Integer ifIndex: ifindextoifnamemap.keySet()) {
            ifindexsnmpbuildermap.put(ifIndex, nb.addSnmpInterface(ifIndex).
                                      setIfType(6).
                                      setIfName(ifindextoifnamemap.get(ifIndex)).
                                      setIfAlias(getSuitableString(ifindextoifalias, ifIndex)).
                                      setIfSpeed(100000000).
                                      setNetMask(getMask(ifindextonetmaskmap,ifIndex)).
                                      setPhysAddr(getSuitableString(ifindextomacmap, ifIndex)).setIfDescr(getSuitableString(ifindextoifdescrmap,ifIndex)));
        }
        
        for (InetAddress ipaddr: ipinterfacemap.keySet()) { 
            String isSnmpPrimary="N";
            Integer ifIndex = ipinterfacemap.get(ipaddr);
            if (ipaddr.getHostAddress().equals(primaryip))
                isSnmpPrimary="P";
            if (ifIndex == null)
                nb.addInterface(ipaddr.getHostAddress()).setIsSnmpPrimary(isSnmpPrimary).setIsManaged("M");
            else {
                nb.addInterface(ipaddr.getHostAddress(), ifindexsnmpbuildermap.get(ifIndex).getSnmpInterface()).
                setIsSnmpPrimary(isSnmpPrimary).setIsManaged("M");            }
        }
            
        return nb.getCurrentNode();
    }
    
    private InetAddress getMask(
            Map<Integer, InetAddress> ifindextonetmaskmap, Integer ifIndex) {
        if (ifindextonetmaskmap.containsKey(ifIndex))
            return ifindextonetmaskmap.get(ifIndex);
        return null;
    }

    private String getSuitableString(Map<Integer,String> ifindextomacmap, Integer ifIndex) {
        String value = "";
        if (ifindextomacmap.containsKey(ifIndex))
            value = ifindextomacmap.get(ifIndex);
        return value;
    }
    
    
    public OnmsNode getNodeWithoutSnmp(String name, String ipaddr) {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(name).setForeignSource("linkd").setForeignId(name).setType(NodeType.ACTIVE);
        nb.addInterface(ipaddr).setIsSnmpPrimary("N").setIsManaged("M");
        return nb.getCurrentNode();
    }    
}
