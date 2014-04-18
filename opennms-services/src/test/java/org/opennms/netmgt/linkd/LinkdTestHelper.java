/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.dao.api.AtInterfaceDao;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpRouteInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.StpInterfaceDao;
import org.opennms.netmgt.dao.api.StpNodeDao;
import org.opennms.netmgt.dao.api.VlanDao;
import org.opennms.netmgt.linkd.CdpInterface;
import org.opennms.netmgt.linkd.Linkd;
import org.opennms.netmgt.linkd.RouterInterface;
import org.opennms.netmgt.linkd.snmp.CdpCacheTableEntry;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author <a href="mailto:antonio@opennme.it">Antonio Russo</a>
 * 
 */

public abstract class LinkdTestHelper implements InitializingBean {

    //NMS0001
    final static String FROH_ROOT= "FROH";
    protected static final String FROH_IP = "192.168.239.51";
    protected static final String FROH_NAME = "froh";
    static final String FROH_SNMP_RESOURCE = "classpath:linkd/nms0001/" + FROH_NAME + "-"+FROH_IP + "-walk.txt";
    static final String FROH_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.29";
    
    protected static final Map<InetAddress,Integer> FROH_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FROH_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FROH_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FROH_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FROH_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FROH_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String OEDIPUS_ROOT= "OEDIPUS";
    protected static final String OEDIPUS_IP = "192.168.239.62";
    protected static final String OEDIPUS_NAME = "oedipus";
    static final String OEDIPUS_SNMP_RESOURCE = "classpath:linkd/nms0001/" + OEDIPUS_NAME + "-"+OEDIPUS_IP + "-walk.txt";
    static final String OEDIPUS_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.11";
   
    protected static final Map<InetAddress,Integer> OEDIPUS_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> OEDIPUS_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> OEDIPUS_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> OEDIPUS_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> OEDIPUS_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> OEDIPUS_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
        
    final static String SIEGFRIE_ROOT= "SIEGFRIE";
    protected static final String SIEGFRIE_IP = "192.168.239.54";
    protected static final String SIEGFRIE_NAME = "siegfrie";
    static final String SIEGFRIE_SNMP_RESOURCE = "classpath:linkd/nms0001/" + SIEGFRIE_NAME + "-"+SIEGFRIE_IP + "-walk.txt";
    static final String SIEGFRIE_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.11";
   
    protected static final Map<InetAddress,Integer> SIEGFRIE_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> SIEGFRIE_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SIEGFRIE_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SIEGFRIE_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SIEGFRIE_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> SIEGFRIE_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FROH_ISIS_SYS_ID     = "000110088500";
    protected static final String OEDIPUS_ISIS_SYS_ID  = "000110255062";
    protected static final String SIEGFRIE_ISIS_SYS_ID = "000110255054";

    //NMS0001
    final static String Rluck001_ROOT= "Rluck001";
    protected static final String Rluck001_IP = "10.4.79.250";
    protected static final String Rluck001_NAME = "r-de-juet-luck-001";
    static final String Rluck001_SNMP_RESOURCE = "classpath:linkd/nms0002ciscojuniper/" + Rluck001_NAME +".txt";
    static final String Rluck001_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.39";
    protected static final Map<InetAddress,Integer> Rluck001_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> Rluck001_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> Rluck001_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> Rluck001_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> Rluck001_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> Rluck001_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String Sluck001_ROOT= "Sluck001";
    protected static final String Sluck001_IP = "10.4.68.215";
    protected static final String Sluck001_NAME = "s-de-juet-luck-001";
    static final String Sluck001_SNMP_RESOURCE = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".txt";
    static final String Sluck001_SNMP_RESOURCE_VLAN100 = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".vlan100.txt";
    static final String Sluck001_SNMP_RESOURCE_VLAN950 = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".vlan950.txt";
    static final String Sluck001_SYSOID = ".1.3.6.1.4.1.9.1.564";
   
    protected static final Map<InetAddress,Integer> Sluck001_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> Sluck001_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> Sluck001_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> Sluck001_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> Sluck001_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> Sluck001_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
        
    final static String RPict001_ROOT= "RPict001";
    protected static final String RPict001_IP = "10.140.252.57";
    protected static final String RPict001_NAME = "r-ro-suce-pict-001";
    static final String RPict001_SNMP_RESOURCE = "classpath:linkd/nms0002UkRoFakeLink/" + RPict001_NAME+".txt";
    static final String RPict001_SYSOID = ".1.3.6.1.4.1.9.1.571";
   
    protected static final Map<InetAddress,Integer> RPict001_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> RPict001_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RPict001_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RPict001_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RPict001_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> RPict001_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();
    
    final static String RNewt103_ROOT= "RNewt103";
    protected static final String RNewt103_IP = "10.239.9.22";
    protected static final String RNewt103_NAME = "r-uk-nott-newt-103";
    static final String RNewt103_SNMP_RESOURCE = "classpath:linkd/nms0002UkRoFakeLink/" + RNewt103_NAME+".txt";
    static final String RNewt103_SYSOID = ".1.3.6.1.4.1.9.1.571";
   
    protected static final Map<InetAddress,Integer> RNewt103_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> RNewt103_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RNewt103_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RNewt103_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RNewt103_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> RNewt103_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String RDeEssnBrue_ROOT= "RDeEssnBrue";
    protected static final String RDeEssnBrue_IP = "10.167.254.40";
    protected static final String RDeEssnBrue_NAME = "r-de-essn-brue-001";
    static final String RDeEssnBrue_SNMP_RESOURCE = "classpath:linkd/nms0002ciscoalcatel2/" + RDeEssnBrue_NAME+ ".txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_400 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan400.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_450 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan450.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_451 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan451.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_452 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan452.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_453 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan453.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_500 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan500.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_501 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan501.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_502 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan502.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_503 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan503.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_504 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan504.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_505 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan505.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_506 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan506.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_507 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan507.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_508 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan508.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_509 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan509.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_510 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan510.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_511 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan511.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_512 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan512.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_513 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan513.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_514 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan514.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_515 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan515.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_516 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan516.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_517 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan517.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_518 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan518.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_519 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan519.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_520 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan520.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_750 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan750.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_751 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan751.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_752 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan752.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_753 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan753.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_754 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan754.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_755 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan755.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_756 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan756.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_757 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan757.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_758 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan758.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_760 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan760.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_800 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan800.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_801 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan801.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_850 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan850.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_851 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan851.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_852 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan852.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_900 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan900.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_950 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan950.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_951 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan951.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_952 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan952.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_953 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan953.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_954 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan954.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_955 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan955.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_956 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan956.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_957 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan957.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_958 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan958.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_959 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan959.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_960 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan960.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_961 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan961.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_962 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan962.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_963 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan963.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_964 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan964.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_965 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan965.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_966 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan966.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_967 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan967.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_968 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan968.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_969 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan969.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_970 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan970.txt";
    static final String RDeEssnBrue_SNMP_RESOURCE_VLAN_979 = "classpath:linkd/nms0002ciscoalcatel2/r-de-essn-brue-001.vlan979.txt";
    static final String RDeEssnBrue_SYSOID = ".1.3.6.1.4.1.9.1.896";
   
    protected static final Map<InetAddress,Integer> RDeEssnBrue_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> RDeEssnBrue_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RDeEssnBrue_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RDeEssnBrue_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RDeEssnBrue_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> RDeEssnBrue_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String SDeEssnBrue081_ROOT= "SDeEssnBrue081";
    protected static final String SDeEssnBrue081_IP = "10.165.62.91";
    protected static final String SDeEssnBrue081_NAME = "s-de-essn-brue-081";
    static final String SDeEssnBrue081_SNMP_RESOURCE ="classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue081_NAME+ ".txt";
    static final String SDeEssnBrue081_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";
   
    protected static final Map<InetAddress,Integer> SDeEssnBrue081_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> SDeEssnBrue081_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue081_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue081_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue081_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> SDeEssnBrue081_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String SDeEssnBrue121_ROOT= "SDeEssnBrue121";
    protected static final String SDeEssnBrue121_IP = "10.165.62.131";
    protected static final String SDeEssnBrue121_NAME = "s-de-essn-brue-121";
    static final String SDeEssnBrue121_SNMP_RESOURCE ="classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue121_NAME+ ".txt";
    static final String SDeEssnBrue121_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";
   
    protected static final Map<InetAddress,Integer> SDeEssnBrue121_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> SDeEssnBrue121_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue121_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue121_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue121_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> SDeEssnBrue121_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String SDeEssnBrue142_ROOT= "SDeEssnBrue142";
    protected static final String SDeEssnBrue142_IP = "10.165.62.152";
    protected static final String SDeEssnBrue142_NAME = "s-de-essn-brue-142";
    static final String SDeEssnBrue142_SNMP_RESOURCE ="classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue142_NAME+ ".txt";
    static final String SDeEssnBrue142_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";
   
    protected static final Map<InetAddress,Integer> SDeEssnBrue142_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> SDeEssnBrue142_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue142_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue142_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue142_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> SDeEssnBrue142_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String SDeEssnBrue165_ROOT= "SDeEssnBrue165";
    protected static final String SDeEssnBrue165_IP = "10.165.62.175";
    protected static final String SDeEssnBrue165_NAME = "s-de-essn-brue-165";
    static final String SDeEssnBrue165_SNMP_RESOURCE ="classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue165_NAME+ ".txt";
    static final String SDeEssnBrue165_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";
   
    protected static final Map<InetAddress,Integer> SDeEssnBrue165_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> SDeEssnBrue165_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue165_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue165_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SDeEssnBrue165_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> SDeEssnBrue165_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String RSeMalmNobe_ROOT= "RSeMalmNobe";
    protected static final String RSeMalmNobe_IP = "10.111.253.9";
    protected static final String RSeMalmNobe_NAME = "r-se-malm-nobe-013";
    final static String RSeMalmNobe_SNMP_RESOURCE = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1002 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1002.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1003 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1003.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1004 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1004.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_1005 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.1005.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_3 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.3.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_357 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.357.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_360 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.360.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_389 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.389.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_399 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.399.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_450 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.450.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_451 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.451.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_452 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.452.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_453 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.453.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_454 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.454.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_500 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.500.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_501 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.501.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_502 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.502.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_503 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.503.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_504 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.504.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_505 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.505.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_506 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.506.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_507 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.507.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_508 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.508.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_600 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.600.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_601 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.601.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_602 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.602.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_603 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.603.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_604 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.604.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_605 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.605.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_606 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.606.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_750 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.750.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_800 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.800.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_801 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.801.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_835 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.835.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_836 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.836.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_850 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.850.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_851 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.851.txt";
    static final String RSeMalmNobe_SNMP_RESOURCE_VLAN_950 = "classpath:linkd/nms0002ciscoalcatel/r-se-malm-nobe-013.vlan.950.txt";
    static final String RSeMalmNobe_SYSOID = ".1.3.6.1.4.1.9.1.516";
   
    protected static final Map<InetAddress,Integer> RSeMalmNobe_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> RSeMalmNobe_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RSeMalmNobe_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RSeMalmNobe_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> RSeMalmNobe_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> RSeMalmNobe_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    final static String SSeMalmNobe_ROOT= "SSeMalmNobe";
    protected static final String SSeMalmNobe_IP = "10.108.191.171";
    protected static final String SSeMalmNobe_NAME = "s-se-malm-nobe-561";
    final static String SSeMalmNobe_SNMP_RESOURCE = "classpath:linkd/nms0002ciscoalcatel/s-se-malm-nobe-561.txt";
    static final String SSeMalmNobe_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.11.2.2";

    protected static final Map<InetAddress,Integer> SSeMalmNobe_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> SSeMalmNobe_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SSeMalmNobe_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SSeMalmNobe_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> SSeMalmNobe_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> SSeMalmNobe_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    //nms003
    final static String SWITCH1_ROOT = "SWITCH1";
    final static String SWITCH1_SNMP_RESOURCE = "classpath:linkd/nms003/switch1-walk.txt";
    protected static final String SWITCH1_IP = "172.16.10.1";
    protected static final String SWITCH1_NAME = "Switch1";
    protected static final String SWITCH1_SYSOID = ".1.3.6.1.4.1.9.1.614";
    
    protected static final Map<InetAddress,Integer> SWITCH1_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> SWITCH1_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH1_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH1_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH1_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    final static String SWITCH2_ROOT = "SWITCH2";
    final static String SWITCH2_SNMP_RESOURCE = "classpath:linkd/nms003/switch2-walk.txt";
    protected static final String SWITCH2_IP = "172.16.10.2";
    protected static final String SWITCH2_NAME = "Switch2";
    protected static final String SWITCH2_SYSOID = ".1.3.6.1.4.1.9.1.696";
    
    protected static final Map<InetAddress,Integer> SWITCH2_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> SWITCH2_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH2_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH2_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH2_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    final static String SWITCH3_ROOT = "SWITCH3";
    final static String SWITCH3_SNMP_RESOURCE = "classpath:linkd/nms003/switch3-walk.txt";
    protected static final String SWITCH3_IP = "172.16.10.3";
    protected static final String SWITCH3_NAME = "Switch3";
    protected static final String SWITCH3_SYSOID = ".1.3.6.1.4.1.9.1.716";
    
    protected static final Map<InetAddress,Integer> SWITCH3_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    protected static final Map<Integer,String> SWITCH3_IF_IFNAME_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH3_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH3_IF_MAC_MAP = new HashMap<Integer, String>();
    protected static final Map<Integer,String> SWITCH3_IF_IFALIAS_MAP = new HashMap<Integer, String>();

    //Nms007
    protected static final String FireFly170_IP = "192.168.168.170";
    protected static final String FireFly170_NAME = "FireFly_170";
    static final String FireFly170_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly170_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly170_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly170_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly170_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly170_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly170_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FireFly171_IP = "192.168.168.171";
    protected static final String FireFly171_NAME = "FireFly_171";
    static final String FireFly171_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly171_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly171_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly171_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly171_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly171_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly171_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FireFly172_IP = "192.168.168.172";
    protected static final String FireFly172_NAME = "FireFly_172";
    static final String FireFly172_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly172_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly172_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly172_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly172_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly172_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly172_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FireFly173_IP = "192.168.168.173";
    protected static final String FireFly173_NAME = "FireFly_173";
    static final String FireFly173_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly173_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly173_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly173_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly173_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly173_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly173_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FireFly174_IP = "192.168.168.174";
    protected static final String FireFly174_NAME = "FireFly_174";
    static final String FireFly174_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly174_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly174_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly174_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly174_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly174_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly174_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FireFly175_IP = "192.168.168.175";
    protected static final String FireFly175_NAME = "FireFly_175";
    static final String FireFly175_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly175_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly175_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly175_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly175_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly175_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly175_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FireFly176_IP = "192.168.168.176";
    protected static final String FireFly176_NAME = "FireFly_176";
    static final String FireFly176_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly176_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly176_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly176_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly176_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly176_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly176_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FireFly177_IP = "192.168.168.177";
    protected static final String FireFly177_NAME = "FireFly_177";
    static final String FireFly177_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly177_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly177_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly177_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly177_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly177_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly177_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();

    protected static final String FireFly189_IP = "192.168.168.189";
    protected static final String FireFly189_NAME = "FireFly_189";
    static final String FireFly189_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.96";
    protected static final Map<InetAddress,Integer> FireFly189_IP_IF_MAP =  new HashMap<InetAddress,Integer>();
    static final Map<Integer,String> FireFly189_IF_IFNAME_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly189_IF_IFDESCR_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly189_IF_MAC_MAP = new HashMap<Integer, String>();
    static final Map<Integer,String> FireFly189_IF_IFALIAS_MAP = new HashMap<Integer, String>();
    static final Map<Integer,InetAddress> FireFly189_IF_NETMASK_MAP = new HashMap<Integer, InetAddress>();


    @Autowired
    protected NodeDao m_nodeDao;
    
    @Autowired
    protected SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    protected IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    protected DataLinkInterfaceDao m_dataLinkInterfaceDao;
        
    @Autowired
    protected StpNodeDao m_stpNodeDao;
    
    @Autowired
    protected StpInterfaceDao m_stpInterfaceDao;
    
    @Autowired
    protected IpRouteInterfaceDao m_ipRouteInterfaceDao;
    
    @Autowired
    protected AtInterfaceDao m_atInterfaceDao;

    @Autowired
    protected VlanDao m_vlanDao;

    protected void printRouteInterface(int nodeid, RouterInterface route) {
        System.err.println("-----------------------------------------------------------");
        System.err.println("Local Route nodeid: "+nodeid);
        System.err.println("Local Route ifIndex: "+route.getIfindex());
        System.err.println("Next Hop Address: " +route.getNextHop());
        System.err.println("Next Hop Network: " +Linkd.getNetwork(route.getNextHop(), route.getNextHopNetmask()));
        System.err.println("Next Hop Netmask: " +route.getNextHopNetmask());
        System.err.println("Next Hop nodeid: "+route.getNextHopIfindex());
        System.err.println("Next Hop ifIndex: "+route.getNextHopIfindex());
        System.err.println("-----------------------------------------------------------");
        System.err.println("");        
    }

    protected void printCdpInterface(int nodeid, CdpInterface cdp) {
        System.err.println("-----------------------------------------------------------");
        System.err.println("Local cdp nodeid: "+nodeid);
        System.err.println("Local cdp ifindex: "+cdp.getCdpIfIndex());
        System.err.println("Local cdp port: "+cdp.getCdpIfName());
        System.err.println("Target cdp deviceId: "+cdp.getCdpTargetDeviceId());
        System.err.println("Target cdp nodeid: "+cdp.getCdpTargetNodeId());
        System.err.println("Target cdp ifname: "+cdp.getCdpTargetIfName());
        System.err.println("-----------------------------------------------------------");
        System.err.println("");        
    	
    }

    protected void printCdpRow(CdpCacheTableEntry cdpCacheTableEntry) {
        System.err.println("-----------------------------------------------------------");    
        System.err.println("getCdpCacheIfIndex: "+cdpCacheTableEntry.getCdpCacheIfIndex());
        System.err.println("getCdpCacheDeviceIndex: "+cdpCacheTableEntry.getCdpCacheDeviceIndex());
        System.err.println("getCdpCacheAddressType: "+cdpCacheTableEntry.getCdpCacheAddressType());
        System.err.println("getCdpCacheAddress: "+cdpCacheTableEntry.getCdpCacheAddress());
        if (cdpCacheTableEntry.getCdpCacheIpv4Address() != null )
            System.err.println("getCdpCacheIpv4Address: "+cdpCacheTableEntry.getCdpCacheIpv4Address().getHostName());
        System.err.println("getCdpCacheVersion: "+cdpCacheTableEntry.getCdpCacheVersion());
        System.err.println("getCdpCacheDeviceId: "+cdpCacheTableEntry.getCdpCacheDeviceId());
        System.err.println("getCdpCacheDevicePort: "+cdpCacheTableEntry.getCdpCacheDevicePort());
        System.err.println("-----------------------------------------------------------");
        System.err.println("");        
        
    }

    protected void printLldpRemRow(Integer lldpRemLocalPortNum, String lldpRemSysname, 
            String lldpRemChassiid,Integer lldpRemChassisidSubtype,String lldpRemPortid, Integer lldpRemPortidSubtype) {
        System.err.println("-----------------------------------------------------------");    
        System.err.println("getLldpRemLocalPortNum: "+lldpRemLocalPortNum);
        System.err.println("getLldpRemSysname: "+lldpRemSysname);
        System.err.println("getLldpRemChassiid: "+lldpRemChassiid);
        System.err.println("getLldpRemChassisidSubtype: "+lldpRemChassisidSubtype);
        System.err.println("getLldpRemPortid: "+lldpRemPortid);
        System.err.println("getLldpRemPortidSubtype: "+lldpRemPortidSubtype);
        System.err.println("-----------------------------------------------------------");
        System.err.println("");        
    }
    
    protected void printLldpLocRow(Integer lldpLocPortNum,
            Integer lldpLocPortidSubtype, String lldpLocPortid) {
        System.err.println("-----------------------------------------------------------");    
        System.err.println("getLldpLocPortNum: "+lldpLocPortNum);
        System.err.println("getLldpLocPortid: "+lldpLocPortid);
        System.err.println("getLldpRemPortidSubtype: "+lldpLocPortidSubtype);
        System.err.println("-----------------------------------------------------------");
        System.err.println("");
      
    }

    protected void printAtInterface(OnmsAtInterface at) {
        System.out.println("----------------net to media------------------");
        System.out.println("id: " + at.getId());
        System.out.println("nodeid: " + at.getNode().getId());
        System.out.println("nodelabel: " + m_nodeDao.get(at.getNode().getId()).getLabel());       
        System.out.println("ip: " + at.getIpAddress());
        System.out.println("mac: " + at.getMacAddress());
        System.out.println("ifindex: " + at.getIfIndex());
        System.out.println("source: " + at.getSourceNodeId());
        System.out.println("sourcenodelabel: " + m_nodeDao.get(at.getSourceNodeId()).getLabel());       
        System.out.println("--------------------------------------");
        System.out.println("");

    }
    protected void printLink(DataLinkInterface datalinkinterface) {
        System.out.println("----------------Link------------------");
        Integer nodeid = datalinkinterface.getNode().getId();
        System.out.println("linkid: " + datalinkinterface.getId());
        System.out.println("nodeid: " + nodeid);
        System.out.println("nodelabel: " + m_nodeDao.get(nodeid).getLabel());       
        Integer ifIndex = datalinkinterface.getIfIndex();
        System.out.println("ifindex: " + ifIndex);
        if (ifIndex > 0)
            System.out.println("ifname: " + m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeid,ifIndex).getIfName());
        Integer nodeparent = datalinkinterface.getNodeParentId();
        System.out.println("nodeparent: " + nodeparent);
        System.out.println("parentnodelabel: " + m_nodeDao.get(nodeparent).getLabel());
        Integer parentifindex = datalinkinterface.getParentIfIndex();
        System.out.println("parentifindex: " + parentifindex);        
        if (parentifindex > 0)
            System.out.println("parentifname: " + m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeparent,parentifindex).getIfName());
        System.out.println("source: " + datalinkinterface.getSource());        
        System.out.println("protocol: " + datalinkinterface.getProtocol());        
        System.out.println("--------------------------------------");
        System.out.println("");

    }
    
    protected void checkLink(OnmsNode node, OnmsNode nodeparent, int ifindex, int parentifindex, DataLinkInterface datalinkinterface) {
        printLink(datalinkinterface);
        printNode(node);
        printNode(nodeparent);
        assertEquals(node.getId(),datalinkinterface.getNode().getId());
        assertEquals(ifindex,datalinkinterface.getIfIndex().intValue());
        assertEquals(nodeparent.getId(), datalinkinterface.getNodeParentId());
        assertEquals(parentifindex,datalinkinterface.getParentIfIndex().intValue());
    }

    protected void printNode(OnmsNode node) {
        System.err.println("----------------Node------------------");
        System.err.println("nodeid: " + node.getId());
        System.err.println("nodelabel: " + node.getLabel());
        System.err.println("nodesysname: " + node.getSysName());
        System.err.println("nodesysoid: " + node.getSysObjectId());
        System.err.println("");
        
    }
    
    protected int getStartPoint(List<DataLinkInterface> links) {
        int start = 0;
        for (final DataLinkInterface link:links) {
            if (start==0 || link.getId().intValue() < start)
                start = link.getId().intValue();                
        }
        return start;
    }
    
    protected void printipInterface(String nodeStringId,OnmsIpInterface ipinterface) {
        System.out.println(nodeStringId+"_IP_IF_MAP.put(InetAddressUtils.addr(\""+ipinterface.getIpHostName()+"\"), "+ipinterface.getIfIndex()+");");
    }
    
    protected void printSnmpInterface(String nodeStringId,OnmsSnmpInterface snmpinterface) {
        if ( snmpinterface.getIfName() != null)
            System.out.println(nodeStringId+"_IF_IFNAME_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfName()+"\");");
        if (snmpinterface.getIfDescr() != null)
            System.out.println(nodeStringId+"_IF_IFDESCR_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfDescr()+"\");");
        if (snmpinterface.getPhysAddr() != null)
            System.out.println(nodeStringId+"_IF_MAC_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getPhysAddr()+"\");");            
        if (snmpinterface.getIfAlias() != null)
            System.out.println(nodeStringId+"_IF_IFALIAS_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfAlias()+"\");");            
        if (snmpinterface.getNetMask() != null && !snmpinterface.getNetMask().getHostAddress().equals("127.0.0.1"))
            System.out.println(nodeStringId+"_IF_NETMASK_MAP.put("+snmpinterface.getIfIndex()+", InetAddressUtils.addr(\""+snmpinterface.getNetMask().getHostAddress()+"\"));");
    }
    
    protected final void printNode(String ipAddr, String prefix) {

        List<OnmsIpInterface> ips = m_ipInterfaceDao.findByIpAddress(ipAddr);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        for (OnmsIpInterface ipinterface: ip.getNode().getIpInterfaces()) {
            if (ipinterface.getIfIndex() != null )
                printipInterface(prefix, ipinterface);
        }

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            printSnmpInterface(prefix, snmpinterface);
        }
    }
    
}
