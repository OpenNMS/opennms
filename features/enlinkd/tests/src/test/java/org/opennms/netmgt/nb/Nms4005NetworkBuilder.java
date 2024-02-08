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

public class Nms4005NetworkBuilder extends NmsNetworkBuilder {
    //nms4005 cisco router OSPF network 4 routers
    public static final String R1_IP = "10.1.1.2";
    public static final String R1_NAME = "R1";
    public static final String R1_SYSOID = ".1.3.6.1.4.1.9.1.122";

    public static final Map<InetAddress,Integer> R1_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> R1_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> R1_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> R1_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> R1_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> R1_IP_MK_MAP = new HashMap<>();

    public static final String R2_IP = "10.1.2.2";
    public static final String R2_NAME = "R2";
    public static final String R2_SYSOID = ".1.3.6.1.4.1.9.1.122";

    public static final Map<InetAddress,Integer> R2_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> R2_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> R2_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> R2_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> R2_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> R2_IP_MK_MAP = new HashMap<>();

    public static final String R3_IP = "10.1.3.2";
    public static final String R3_NAME = "R3";
    public static final String R3_SYSOID = ".1.3.6.1.4.1.9.1.122";

    public static final Map<InetAddress,Integer> R3_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> R3_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> R3_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> R3_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> R3_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> R3_IP_MK_MAP = new HashMap<>();

    public static final String R4_IP = "10.1.4.2";
    public static final String R4_NAME = "R4";
    public static final String R4_SYSOID = ".1.3.6.1.4.1.9.1.122";

    public static final Map<InetAddress,Integer> R4_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> R4_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> R4_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> R4_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> R4_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> R4_IP_MK_MAP = new HashMap<>();

    static {
        R1_IP_IF_MAP.put(InetAddressUtils.addr("10.1.2.1"), 1);
        R1_IP_IF_MAP.put(InetAddressUtils.addr("10.1.3.1"), 2);
        R1_IP_IF_MAP.put(InetAddressUtils.addr("10.1.1.2"), 3);
        R1_IP_MK_MAP.put(InetAddressUtils.addr("10.1.2.1"), InetAddressUtils.addr("255.255.255.0"));
        R1_IP_MK_MAP.put(InetAddressUtils.addr("10.1.3.1"), InetAddressUtils.addr("255.255.255.0"));
        R1_IP_MK_MAP.put(InetAddressUtils.addr("10.1.1.2"), InetAddressUtils.addr("255.255.255.0"));
        R2_IP_IF_MAP.put(InetAddressUtils.addr("10.1.2.2"), 1);
        R2_IP_IF_MAP.put(InetAddressUtils.addr("10.1.5.1"), 2);
        R2_IP_MK_MAP.put(InetAddressUtils.addr("10.1.2.2"), InetAddressUtils.addr("255.255.255.0"));
        R2_IP_MK_MAP.put(InetAddressUtils.addr("10.1.5.1"), InetAddressUtils.addr("255.255.255.0"));
        R3_IP_IF_MAP.put(InetAddressUtils.addr("10.1.3.2"), 1);
        R3_IP_IF_MAP.put(InetAddressUtils.addr("10.1.4.1"), 2);
        R3_IP_IF_MAP.put(InetAddressUtils.addr("10.1.5.2"), 3);
        R3_IP_MK_MAP.put(InetAddressUtils.addr("10.1.3.1"), InetAddressUtils.addr("255.255.255.0"));
        R3_IP_MK_MAP.put(InetAddressUtils.addr("10.1.4.1"), InetAddressUtils.addr("255.255.255.0"));
        R3_IP_MK_MAP.put(InetAddressUtils.addr("10.1.5.2"), InetAddressUtils.addr("255.255.255.0"));
        R4_IP_IF_MAP.put(InetAddressUtils.addr("10.1.4.2"), 1);
        R4_IP_MK_MAP.put(InetAddressUtils.addr("10.1.4.2"), InetAddressUtils.addr("255.255.255.0"));

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

    public OnmsNode getR1() {
        return getNode(R1_NAME,R1_SYSOID,R1_IP,R1_IP_IF_MAP,R1_IF_IFNAME_MAP,R1_IF_MAC_MAP,R1_IF_IFDESCR_MAP,R1_IF_IFALIAS_MAP,R1_IP_MK_MAP);
    }    

    public OnmsNode getR2() {
        return getNode(R2_NAME,R2_SYSOID,R2_IP,R2_IP_IF_MAP,R2_IF_IFNAME_MAP,R2_IF_MAC_MAP,R2_IF_IFDESCR_MAP,R2_IF_IFALIAS_MAP,R2_IP_MK_MAP);
    }    

    public OnmsNode getR3() {
        return getNode(R3_NAME,R3_SYSOID,R3_IP,R3_IP_IF_MAP,R3_IF_IFNAME_MAP,R3_IF_MAC_MAP,R3_IF_IFDESCR_MAP,R3_IF_IFALIAS_MAP,R3_IP_MK_MAP);
    }    

    public OnmsNode getR4() {
        return getNode(R4_NAME,R4_SYSOID,R4_IP,R4_IP_IF_MAP,R4_IF_IFNAME_MAP,R4_IF_MAC_MAP,R4_IF_IFDESCR_MAP,R4_IF_IFALIAS_MAP,R4_IP_MK_MAP);
    }    

}
