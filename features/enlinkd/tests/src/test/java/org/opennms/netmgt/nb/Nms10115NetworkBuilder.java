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
import java.util.Map;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public class Nms10115NetworkBuilder extends NmsNetworkBuilder {
    //NMSALBENGA
    public static final String MS01_IP = "10.115.180.1";
    public static final String MS01_NAME = "SW_D6_01_M";
    public static final String MS01_SNMP_RESOURCE = "classpath:/linkd/nms10115/" + MS01_IP +"-walk.txt";
    public static final String MS01_SYSOID = ".1.3.6.1.4.1.3181.10.6";
    public static final String MS01_LLDP_CHASSIS_ID = "0060a70a804e";

    public static final Map<InetAddress,Integer> MS01_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS01_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS01_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS01_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS01_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS01_IP_MK_MAP = new HashMap<>();

    public static final String MS02_IP = "10.115.180.2";
    public static final String MS02_NAME = "SW_D6_02_M";
    public static final String MS02_SNMP_RESOURCE = "classpath:/linkd/nms10115/" + MS02_IP +"-walk.txt";
    public static final String MS02_SYSOID = ".1.3.6.1.4.1.3181.10.6";
    public static final String MS02_LLDP_CHASSIS_ID = "0060a70c27fd";

    public static final Map<InetAddress,Integer> MS02_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS02_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS02_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS02_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS02_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS02_IP_MK_MAP = new HashMap<>();

    public static final String MS03_IP = "10.115.180.3";
    public static final String MS03_NAME = "SW_D6_03_M";
    public static final String MS03_SNMP_RESOURCE = "classpath:/linkd/nms10115/" + MS03_IP +"-walk.txt";
    public static final String MS03_SYSOID = ".1.3.6.1.4.1.3181.10.6";
    public static final String MS03_LLDP_CHASSIS_ID = "0060a70a805e";

    public static final Map<InetAddress,Integer> MS03_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS03_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS03_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS03_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS03_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS03_IP_MK_MAP = new HashMap<>();

    public static final String MS04_IP = "10.115.180.4";
    public static final String MS04_NAME = "SW_D6_04_M";
    public static final String MS04_SNMP_RESOURCE = "classpath:/linkd/nms10115/" + MS04_IP +"-walk.txt";
    public static final String MS04_SYSOID = ".1.3.6.1.4.1.3181.10.6";
    public static final String MS04_LLDP_CHASSIS_ID = "0060a70a8113";

    public static final Map<InetAddress,Integer> MS04_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS04_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS04_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS04_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS04_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS04_IP_MK_MAP = new HashMap<>();
    
    public static final String MS08_IP = "10.115.180.8";
    public static final String MS08_NAME = "SW_D6_08_M";
    public static final String MS08_SNMP_RESOURCE = "classpath:/linkd/nms10115/" + MS08_IP +"-walk.txt";
    public static final String MS08_SYSOID = ".1.3.6.1.4.1.3181.10.6";
    public static final String MS08_LLDP_CHASSIS_ID = "0060a70a7f16";

    public static final Map<InetAddress,Integer> MS08_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS08_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS08_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS08_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS08_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS08_IP_MK_MAP = new HashMap<>();

    public static final String MS09_IP = "10.115.180.9";
    public static final String MS09_NAME = "SW_D6_09_M";
    public static final String MS09_SNMP_RESOURCE = "classpath:/linkd/nms10115/" + MS09_IP +"-walk.txt";
    public static final String MS09_SYSOID = ".1.3.6.1.4.1.3181.10.6";
    public static final String MS09_LLDP_CHASSIS_ID = "0060a70d9e15";

    public static final Map<InetAddress,Integer> MS09_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS09_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS09_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS09_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS09_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS09_IP_MK_MAP = new HashMap<>();
    
    public static final String QFX_IP = "10.59.228.201";
    public static final String QFX_NAME = "qfx";
    public static final String QFX_SNMP_RESOURCE = "classpath:/linkd/nms10115/" + QFX_IP +"-walk.txt";
    public static final String QFX_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.4.82.17";
    public static final String QFX_LLDP_CHASSIS_ID = "b8f015f9de60";
    public static final String QFX_SYSNAME = "E0281L-ScALBENGA2-QFX";

    public static final Map<InetAddress,Integer> QFX_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> QFX_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> QFX_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> QFX_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> QFX_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> QFX_IP_MK_MAP = new HashMap<>();


    static {
        MS01_IP_IF_MAP.put(InetAddressUtils.addr(MS01_IP),1);
        MS02_IP_IF_MAP.put(InetAddressUtils.addr(MS02_IP),1);
        MS03_IP_IF_MAP.put(InetAddressUtils.addr(MS03_IP),1);
        MS04_IP_IF_MAP.put(InetAddressUtils.addr(MS04_IP),1);
        MS08_IP_IF_MAP.put(InetAddressUtils.addr(MS08_IP),1);
        MS09_IP_IF_MAP.put(InetAddressUtils.addr(MS09_IP),1);
        QFX_IP_IF_MAP.put(InetAddressUtils.addr(QFX_IP),1);
    }

    public OnmsNode getMs01() {
        return getNode(MS01_NAME,MS01_SYSOID,MS01_IP,
                MS01_IP_IF_MAP,MS01_IF_IFNAME_MAP,MS01_IF_MAC_MAP,
                MS01_IF_IFDESCR_MAP,MS01_IF_IFALIAS_MAP, MS01_IP_MK_MAP);
    }

    public OnmsNode getMs02() {
        return getNode(MS02_NAME,MS02_SYSOID,MS02_IP,
                MS02_IP_IF_MAP,MS02_IF_IFNAME_MAP,MS02_IF_MAC_MAP,
                MS02_IF_IFDESCR_MAP,MS02_IF_IFALIAS_MAP, MS02_IP_MK_MAP);
    }

    public OnmsNode getMs03() {
        return getNode(MS03_NAME,MS03_SYSOID,MS03_IP,
                MS03_IP_IF_MAP,MS03_IF_IFNAME_MAP,MS03_IF_MAC_MAP,
                MS03_IF_IFDESCR_MAP,MS03_IF_IFALIAS_MAP, MS03_IP_MK_MAP);
    }

    public OnmsNode getMs04() {
        return getNode(MS04_NAME,MS04_SYSOID,MS04_IP,
                MS04_IP_IF_MAP,MS04_IF_IFNAME_MAP,MS04_IF_MAC_MAP,
                MS04_IF_IFDESCR_MAP,MS04_IF_IFALIAS_MAP, MS04_IP_MK_MAP);
    }


    public OnmsNode getMs08() {
        return getNode(MS08_NAME,MS08_SYSOID,MS08_IP,MS08_IP_IF_MAP,
                MS08_IF_IFNAME_MAP,MS08_IF_MAC_MAP,MS08_IF_IFDESCR_MAP,
                MS08_IF_IFALIAS_MAP, MS08_IP_MK_MAP);
    }

    public OnmsNode getMs09() {
        return getNode(MS09_NAME,MS09_SYSOID,MS09_IP,
                MS09_IP_IF_MAP,MS09_IF_IFNAME_MAP,MS09_IF_MAC_MAP,
                MS09_IF_IFDESCR_MAP,MS09_IF_IFALIAS_MAP, MS09_IP_MK_MAP);
    }


    public OnmsNode getQFX() {
        return getNode(QFX_SYSNAME,QFX_SYSOID,QFX_IP,QFX_IP_IF_MAP,
                QFX_IF_IFNAME_MAP,QFX_IF_MAC_MAP,QFX_IF_IFDESCR_MAP,
                QFX_IF_IFALIAS_MAP, QFX_IP_MK_MAP);
    }

}
