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
package org.opennms.netmgt.nb;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

public class Nms102NetworkBuilder extends NmsNetworkBuilder {
    //nms102 my net at home some time ago two macs a router and a printer
    // to be considered to support wifi links
    public static final String MIKROTIK_IP = "192.168.0.1";
    public static final String MIKROTIK_NAME = "mikrotik";
    public static final String MIKROTIK_SYSOID = ".1.3.6.1.4.1.14988.1";

    public static final Map<InetAddress,Integer> MIKROTIK_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MIKROTIK_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MIKROTIK_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MIKROTIK_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MIKROTIK_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MIKROTIK_IP_MK_MAP = new HashMap<>();

    public static final String SAMSUNG_IP = "192.168.0.14";
    public static final String SAMSUNG_NAME = "samsung";
    public static final String SAMSUNG_SYSOID = ".1.3.6.1.4.1.236.11.5.1";

    public static final Map<InetAddress,Integer> SAMSUNG_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> SAMSUNG_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> SAMSUNG_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> SAMSUNG_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> SAMSUNG_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> SAMSUNG_IP_MK_MAP = new HashMap<>();

    public static final String MAC1_IP = "192.168.0.16";
    public static final String MAC1_NAME = "mac1";
    public static final String MAC1_SYSOID = ".1.3.6.1.4.1.8072.3.2.255";

    public static final Map<InetAddress,Integer> MAC1_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MAC1_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MAC1_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MAC1_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MAC1_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MAC1_IP_MK_MAP = new HashMap<>();

    public static final String MAC2_IP = "192.168.0.17";
    public static final String MAC2_NAME = "mac2";
    public static final String MAC2_SYSOID = ".1.3.6.1.4.1.8072.3.2.255";

    public static final Map<InetAddress,Integer> MAC2_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MAC2_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MAC2_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MAC2_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MAC2_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MAC2_IP_MK_MAP = new HashMap<>();

    public static final String MIKROTIK_SNMP_RESOURCE = "classpath:/linkd/nms102/"+MIKROTIK_NAME+"-"+MIKROTIK_IP+"-walk.txt";
    public static final String SAMSUNG_SNMP_RESOURCE = "classpath:/linkd/nms102/"+SAMSUNG_NAME+"-"+SAMSUNG_IP+"-walk.txt";
    public static final String MAC1_SNMP_RESOURCE = "classpath:/linkd/nms102/"+"mac-"+MAC1_IP+"-walk.txt";
    public static final String MAC2_SNMP_RESOURCE = "classpath:/linkd/nms102/"+"mac-"+MAC2_IP+"-walk.txt";

    static {
        MIKROTIK_IP_IF_MAP.put(InetAddressUtils.addr("192.168.0.1"), 2);
        MIKROTIK_IP_MK_MAP.put(InetAddressUtils.addr("192.168.0.1"), InetAddressUtils.addr("255.255.255.0"));
        MIKROTIK_IP_IF_MAP.put(InetAddressUtils.addr("10.129.16.165"), 1);
        MIKROTIK_IP_MK_MAP.put(InetAddressUtils.addr("10.129.16.165"), InetAddressUtils.addr("255.255.248.0"));
        MIKROTIK_IF_IFNAME_MAP.put(2, "wlan1");
        MIKROTIK_IF_IFDESCR_MAP.put(2, "wlan1");
        MIKROTIK_IF_IFNAME_MAP.put(1, "ether1");
        MIKROTIK_IF_IFDESCR_MAP.put(1, "ether1");
        MIKROTIK_IF_IFALIAS_MAP.put(1, "salvatore");

        SAMSUNG_IP_IF_MAP.put(InetAddressUtils.addr("192.168.0.14"), 1);
        SAMSUNG_IP_MK_MAP.put(InetAddressUtils.addr("192.168.0.14"), InetAddressUtils.addr("255.255.255.0"));
        SAMSUNG_IF_IFDESCR_MAP.put(1, "Embedded Ethernet Controller, 10/100 Mbps, v1.0, UTP RJ-45, connector A1, 10 half duplex");
        SAMSUNG_IF_IFNAME_MAP.put(1, "ethernet0");
        SAMSUNG_IF_IFDESCR_MAP.put(2, "Loopback Interface");
        SAMSUNG_IF_IFNAME_MAP.put(2, "loopback0");

        MAC1_IP_IF_MAP.put(InetAddressUtils.addr("192.168.0.16"), 5);
        MAC1_IP_MK_MAP.put(InetAddressUtils.addr("192.168.0.16"), InetAddressUtils.addr("255.255.255.0"));
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
        MAC1_IF_IFNAME_MAP.put(6, "fw0");
        MAC1_IF_IFDESCR_MAP.put(6, "fw0");
        
        MAC2_IP_IF_MAP.put(InetAddressUtils.addr("192.168.0.17"), 6);
        MAC2_IP_MK_MAP.put(InetAddressUtils.addr("192.168.0.17"), InetAddressUtils.addr("255.255.255.0"));
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
    }
    
    public OnmsNode getMikrotik() {
        return getNode(MIKROTIK_NAME,MIKROTIK_SYSOID,MIKROTIK_IP,MIKROTIK_IP_IF_MAP,MIKROTIK_IF_IFNAME_MAP,MIKROTIK_IF_MAC_MAP,MIKROTIK_IF_IFDESCR_MAP,MIKROTIK_IF_IFALIAS_MAP,MIKROTIK_IP_MK_MAP);
    }

    public OnmsNode getSamsung() {
        return getNode(SAMSUNG_NAME,SAMSUNG_SYSOID,SAMSUNG_IP,SAMSUNG_IP_IF_MAP,SAMSUNG_IF_IFNAME_MAP,SAMSUNG_IF_MAC_MAP,SAMSUNG_IF_IFDESCR_MAP,SAMSUNG_IF_IFALIAS_MAP,SAMSUNG_IP_MK_MAP);
    }

    public OnmsNode getMac1() {
        return getNode(MAC1_NAME,MAC1_SYSOID,MAC1_IP,MAC1_IP_IF_MAP,MAC1_IF_IFNAME_MAP,MAC1_IF_MAC_MAP,MAC1_IF_IFDESCR_MAP,MAC1_IF_IFALIAS_MAP,MAC1_IP_MK_MAP);
    }

    public OnmsNode getMac2() {
        return getNode(MAC2_NAME,MAC2_SYSOID,MAC2_IP,MAC2_IP_IF_MAP,MAC2_IF_IFNAME_MAP,MAC2_IF_MAC_MAP,MAC2_IF_IFDESCR_MAP,MAC2_IF_IFALIAS_MAP,MAC2_IP_MK_MAP);
    }

}
