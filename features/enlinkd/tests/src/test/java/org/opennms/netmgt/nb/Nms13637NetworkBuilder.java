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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public class Nms13637NetworkBuilder extends NmsNetworkBuilder {

    //NMS-13637
    public static final String MKTROUTER1_SYSOID=".1.3.6.1.4.1.14988.1";
    public static final String MKTROUTER1_NAME="router-1";
    public static final String MKTROUTER1_IP="192.168.178.73";
    public static final String MKTROUTER1_RESOURCE="classpath:/linkd/nms13637/router-1-walk.txt";
    public static final String MKTROUTER1_ETHER1_MAC =    "00 50 56 82 A1 05".replaceAll("\\s+","").toLowerCase(Locale.ROOT);

    public static final String MKTROUTER2_SYSOID=".1.3.6.1.4.1.14988.1";
    public static final String MKTROUTER2_NAME="router-2";
    public static final String MKTROUTER2_IP="192.168.178.74";
    public static final String MKTROUTER2_RESOURCE="classpath:/linkd/nms13637/router-2-walk.txt";
    public static final String MKTROUTER2_ETHER1_MAC =    "00 50 56 82 EF FC".replaceAll("\\s+","").toLowerCase(Locale.ROOT);

    public static final String MKTROUTER3_SYSOID=".1.3.6.1.4.1.14988.1";
    public static final String MKTROUTER3_NAME="router-3";
    public static final String MKTROUTER3_IP="192.168.180.73";
    public static final String MKTROUTER3_RESOURCE="classpath:/linkd/nms13637/router-3-walk.txt";
    public static final String MKTROUTER3_ETHER1_MAC =    "00 50 56 82 A1 35".replaceAll("\\s+","").toLowerCase(Locale.ROOT);
    public static final String MKTROUTER3_ETHER2_MAC =    "00 50 56 82 04 D6".replaceAll("\\s+","").toLowerCase(Locale.ROOT);
    public static final String MKTROUTER3_ETHER3_MAC =    "00 50 56 82 7D A0".replaceAll("\\s+","").toLowerCase(Locale.ROOT);

    public static final String CISCO_SW01_SYSOID =".1.3.6.1.4.1.9.6.1.95.10.3";
    public static final String CISCO_SW01_NAME ="sw01-office";
    public static final String CISCO_SW01_IP ="192.168.178.30";
    public static final String CISCO_SW01_RESOURCE ="classpath:/linkd/nms13637/sw01-office-walk.txt";
    public static final String CISCO_SW01_LLDP_ID =    "5C 71 0D 26 AC 3E".replaceAll("\\s+","").toLowerCase(Locale.ROOT);
    public static final String CISCO_SW01_GB05_MAC =    "5C 71 0D 26 AC 43".replaceAll("\\s+","").toLowerCase(Locale.ROOT);

    public static final String HOST3_LLDP_ID ="00 50 56 82 23 35".replaceAll("\\s+","").toLowerCase(Locale.ROOT);
    public static final String HOST4_LLDP_ID ="00 50 56 82 20 E2".replaceAll("\\s+","").toLowerCase(Locale.ROOT);
    public static final String HOST5_LLDP_ID ="00 50 56 82 62 AB".replaceAll("\\s+","").toLowerCase(Locale.ROOT);


    public static final Map<InetAddress,Integer> MKTROUTER1_IP_IF_MAP =  new HashMap<>();
    public static final Map<InetAddress,InetAddress> MKTROUTER1_IP_MK_MAP =  new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER1_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER1_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER1_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER1_IF_IFALIAS_MAP = new HashMap<>();

    public static final Map<InetAddress,Integer> MKTROUTER2_IP_IF_MAP =  new HashMap<>();
    public static final Map<InetAddress,InetAddress> MKTROUTER2_IP_MK_MAP =  new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER2_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER2_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER2_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER2_IF_IFALIAS_MAP = new HashMap<>();

    public static final Map<InetAddress,Integer> MKTROUTER3_IP_IF_MAP =  new HashMap<>();
    public static final Map<InetAddress,InetAddress> MKTROUTER3_IP_MK_MAP =  new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER3_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER3_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER3_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      MKTROUTER3_IF_IFALIAS_MAP = new HashMap<>();

    public static final Map<InetAddress,Integer> CISCO_SW01_IP_IF_MAP =  new HashMap<>();
    public static final Map<InetAddress,InetAddress> CISCO_SW01_IP_MK_MAP =  new HashMap<>();
    public static final Map<Integer,String>      CISCO_SW01_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      CISCO_SW01_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      CISCO_SW01_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      CISCO_SW01_IF_IFALIAS_MAP = new HashMap<>();


    static {
    try {
        MKTROUTER1_IP_IF_MAP.put(InetAddressUtils.addr(MKTROUTER1_IP), 1);
        MKTROUTER1_IP_MK_MAP.put(InetAddressUtils.addr("192.168.178.73"), InetAddressUtils.addr("255.255.255.0"));
        MKTROUTER1_IF_IFDESCR_MAP.put(1, "ether1");
        MKTROUTER1_IF_IFNAME_MAP.put(1, "ether1");
        MKTROUTER1_IF_MAC_MAP.put(1, MKTROUTER1_ETHER1_MAC);
        MKTROUTER1_IF_IFALIAS_MAP.put(1, "");

        MKTROUTER2_IP_IF_MAP.put(InetAddressUtils.addr(MKTROUTER2_IP), 1);
        MKTROUTER2_IP_MK_MAP.put(InetAddressUtils.addr("192.168.178.74"), InetAddressUtils.addr("255.255.255.0"));
        MKTROUTER2_IF_IFDESCR_MAP.put(1, "ether1");
        MKTROUTER2_IF_IFNAME_MAP.put(1, "ether1");
        MKTROUTER2_IF_MAC_MAP.put(1, MKTROUTER2_ETHER1_MAC);
        MKTROUTER2_IF_IFALIAS_MAP.put(1, "");

        MKTROUTER3_IP_IF_MAP.put(InetAddressUtils.addr(MKTROUTER3_IP), 1);
        MKTROUTER3_IP_MK_MAP.put(InetAddressUtils.addr("192.168.180.73"), InetAddressUtils.addr("255.255.255.0"));
        MKTROUTER3_IF_IFDESCR_MAP.put(1, "ether1");
        MKTROUTER3_IF_IFNAME_MAP.put(1, "ether1");
        MKTROUTER3_IF_MAC_MAP.put(1, MKTROUTER2_ETHER1_MAC);
        MKTROUTER3_IF_IFALIAS_MAP.put(1, "");

        CISCO_SW01_IP_IF_MAP.put(InetAddressUtils.addr(CISCO_SW01_IP),7000);
        CISCO_SW01_IF_IFDESCR_MAP.put(7000, "loopback1");
        CISCO_SW01_IF_MAC_MAP.put(7000, "5C 71 0D 26 AC 3E".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_IFNAME_MAP.put(7000, "loopback1");
        CISCO_SW01_IF_IFALIAS_MAP.put(7000, "");

        CISCO_SW01_IF_IFDESCR_MAP.put(1, "GigabitEthernet1");
        CISCO_SW01_IF_IFDESCR_MAP.put(2, "GigabitEthernet2");
        CISCO_SW01_IF_IFDESCR_MAP.put(3, "GigabitEthernet3");
        CISCO_SW01_IF_IFDESCR_MAP.put(4, "GigabitEthernet4");
        CISCO_SW01_IF_IFDESCR_MAP.put(5, "GigabitEthernet5");
        CISCO_SW01_IF_IFDESCR_MAP.put(6, "GigabitEthernet6");
        CISCO_SW01_IF_IFDESCR_MAP.put(7, "GigabitEthernet7");
        CISCO_SW01_IF_IFDESCR_MAP.put(8, "GigabitEthernet8");
        CISCO_SW01_IF_IFDESCR_MAP.put(9, "GigabitEthernet9");
        CISCO_SW01_IF_IFDESCR_MAP.put(10, "GigabitEthernet10");

        CISCO_SW01_IF_MAC_MAP.put(1, "5C 71 0D 26 AC 3F".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_MAC_MAP.put(2, "5C 71 0D 26 AC 40".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_MAC_MAP.put(3, "5C 71 0D 26 AC 41".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_MAC_MAP.put(4, "5C 71 0D 26 AC 42".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_MAC_MAP.put(5, CISCO_SW01_GB05_MAC);
        CISCO_SW01_IF_MAC_MAP.put(6, "5C 71 0D 26 AC 44".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_MAC_MAP.put(7, "5C 71 0D 26 AC 45".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_MAC_MAP.put(8, "5C 71 0D 26 AC 46".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_MAC_MAP.put(9, "5C 71 0D 26 AC 47".replaceAll("\\s+","").toLowerCase(Locale.ROOT));
        CISCO_SW01_IF_MAC_MAP.put(10, "5C 71 0D 26 AC 48".replaceAll("\\s+","").toLowerCase(Locale.ROOT));

        CISCO_SW01_IF_IFNAME_MAP.put(1, "gi1");
        CISCO_SW01_IF_IFNAME_MAP.put(2, "gi2");
        CISCO_SW01_IF_IFNAME_MAP.put(3, "gi3");
        CISCO_SW01_IF_IFNAME_MAP.put(4, "gi4");
        CISCO_SW01_IF_IFNAME_MAP.put(5, "gi5");
        CISCO_SW01_IF_IFNAME_MAP.put(6, "gi6");
        CISCO_SW01_IF_IFNAME_MAP.put(7, "gi7");
        CISCO_SW01_IF_IFNAME_MAP.put(8, "gi8");
        CISCO_SW01_IF_IFNAME_MAP.put(9, "gi9");
        CISCO_SW01_IF_IFNAME_MAP.put(10, "gi10");

        CISCO_SW01_IF_IFALIAS_MAP.put(1, "");
        CISCO_SW01_IF_IFALIAS_MAP.put(2, "Powerline Office");
        CISCO_SW01_IF_IFALIAS_MAP.put(3, "");
        CISCO_SW01_IF_IFALIAS_MAP.put(4, "Brother DCP-9022CDW");
        CISCO_SW01_IF_IFALIAS_MAP.put(5, "ESX Management");
        CISCO_SW01_IF_IFALIAS_MAP.put(6, "VMware ESX");
        CISCO_SW01_IF_IFALIAS_MAP.put(7, "dinky");
        CISCO_SW01_IF_IFALIAS_MAP.put(8, "fritz.box");
        CISCO_SW01_IF_IFALIAS_MAP.put(9, "");
        CISCO_SW01_IF_IFALIAS_MAP.put(10, "");


    } catch (Exception ignored) {
        
    }
    }
    
    public OnmsNode getRouter1() {
        return getNode(MKTROUTER1_NAME, MKTROUTER1_SYSOID, MKTROUTER1_IP, MKTROUTER1_IP_IF_MAP, MKTROUTER1_IF_IFNAME_MAP,
                MKTROUTER1_IF_MAC_MAP, MKTROUTER1_IF_IFDESCR_MAP, MKTROUTER1_IF_IFALIAS_MAP, MKTROUTER1_IP_MK_MAP);
    }

    public OnmsNode getRouter2() {
        return getNode(MKTROUTER2_NAME, MKTROUTER2_SYSOID, MKTROUTER2_IP, MKTROUTER2_IP_IF_MAP, MKTROUTER2_IF_IFNAME_MAP,
                MKTROUTER2_IF_MAC_MAP, MKTROUTER2_IF_IFDESCR_MAP, MKTROUTER2_IF_IFALIAS_MAP, MKTROUTER2_IP_MK_MAP);
    }

    public OnmsNode getRouter3() {
        return getNode(MKTROUTER3_NAME, MKTROUTER3_SYSOID, MKTROUTER3_IP, MKTROUTER3_IP_IF_MAP, MKTROUTER3_IF_IFNAME_MAP,
                MKTROUTER3_IF_MAC_MAP, MKTROUTER3_IF_IFDESCR_MAP, MKTROUTER3_IF_IFALIAS_MAP, MKTROUTER3_IP_MK_MAP);
    }

    public OnmsNode getCiscoHomeSw() {
        return getNode(CISCO_SW01_NAME, CISCO_SW01_SYSOID, CISCO_SW01_IP, CISCO_SW01_IP_IF_MAP, CISCO_SW01_IF_IFNAME_MAP,
                CISCO_SW01_IF_MAC_MAP,CISCO_SW01_IF_IFDESCR_MAP,CISCO_SW01_IF_IFALIAS_MAP, CISCO_SW01_IP_MK_MAP);
    }

}
