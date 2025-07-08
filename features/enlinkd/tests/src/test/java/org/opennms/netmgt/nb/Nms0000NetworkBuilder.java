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

public class Nms0000NetworkBuilder extends NmsNetworkBuilder {
    //NMS0100
    public static final String MS01_IP = "10.114.20.1";
    public static final String MS01_NAME = "ms01";
    public static final String MS01_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS01_IP +".txt";
    public static final String MS01_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS01_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS01_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS01_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS01_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS01_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS01_IP_MK_MAP = new HashMap<>();

    public static final String MS02_IP = "10.114.20.2";
    public static final String MS02_NAME = "ms02";
    public static final String MS02_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS02_IP +".txt";
    public static final String MS02_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS02_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS02_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS02_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS02_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS02_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS02_IP_MK_MAP = new HashMap<>();

    public static final String MS03_IP = "10.114.20.3";
    public static final String MS03_NAME = "ms03";
    public static final String MS03_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS03_IP +".txt";
    public static final String MS03_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS03_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS03_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS03_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS03_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS03_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS03_IP_MK_MAP = new HashMap<>();

    public static final String MS04_IP = "10.114.20.4";
    public static final String MS04_NAME = "ms04";
    public static final String MS04_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS04_IP +".txt";
    public static final String MS04_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS04_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS04_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS04_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS04_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS04_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS04_IP_MK_MAP = new HashMap<>();

    public static final String MS05_IP = "10.114.20.5";
    public static final String MS05_NAME = "ms05";
    public static final String MS05_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS05_IP +".txt";
    public static final String MS05_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS05_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS05_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS05_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS05_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS05_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS05_IP_MK_MAP = new HashMap<>();

    public static final String MS06_IP = "10.114.20.6";
    public static final String MS06_NAME = "ms06";
    public static final String MS06_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS06_IP +".txt";
    public static final String MS06_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS06_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS06_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS06_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS06_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS06_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS06_IP_MK_MAP = new HashMap<>();

    public static final String MS07_IP = "10.114.20.7";
    public static final String MS07_NAME = "ms07";
    public static final String MS07_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS07_IP +".txt";
    public static final String MS07_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS07_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS07_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS07_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS07_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS07_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS07_IP_MK_MAP = new HashMap<>();

    public static final String MS08_IP = "10.114.20.8";
    public static final String MS08_NAME = "ms08";
    public static final String MS08_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS08_IP +".txt";
    public static final String MS08_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS08_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS08_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS08_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS08_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS08_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS08_IP_MK_MAP = new HashMap<>();

    public static final String MS09_IP = "10.114.20.9";
    public static final String MS09_NAME = "ms09";
    public static final String MS09_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS09_IP +".txt";
    public static final String MS09_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS09_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS09_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS09_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS09_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS09_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS09_IP_MK_MAP = new HashMap<>();

    public static final String MS10_IP = "10.114.20.10";
    public static final String MS10_NAME = "ms10";
    public static final String MS10_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS10_IP +".txt";
    public static final String MS10_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS10_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS10_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS10_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS10_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS10_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS10_IP_MK_MAP = new HashMap<>();

    public static final String MS11_IP = "10.114.20.11";
    public static final String MS11_NAME = "ms11";
    public static final String MS11_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS11_IP +".txt";
    public static final String MS11_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS11_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS11_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS11_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS11_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS11_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS11_IP_MK_MAP = new HashMap<>();

    public static final String MS12_IP = "10.114.20.12";
    public static final String MS12_NAME = "ms12";
    public static final String MS12_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS12_IP +".txt";
    public static final String MS12_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS12_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS12_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS12_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS12_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS12_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS12_IP_MK_MAP = new HashMap<>();

    public static final String MS14_IP = "10.114.20.14";
    public static final String MS14_NAME = "ms14";
    public static final String MS14_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS14_IP +".txt";
    public static final String MS14_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS14_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS14_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS14_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS14_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS14_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS14_IP_MK_MAP = new HashMap<>();

    public static final String MS15_IP = "10.114.20.15";
    public static final String MS15_NAME = "ms15";
    public static final String MS15_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS15_IP +".txt";
    public static final String MS15_SYSOID = ".1.3.6.1.4.1.164.6.1.10.100.1";

    public static final Map<InetAddress,Integer> MS15_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS15_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS15_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS15_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS15_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS15_IP_MK_MAP = new HashMap<>();

    public static final String MS16_IP = "10.114.20.16";
    public static final String MS16_NAME = "ms16";
    public static final String MS16_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS16_IP +".txt";
    public static final String MS16_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS16_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS16_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS16_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS16_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS16_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS16_IP_MK_MAP = new HashMap<>();

    public static final String MS17_IP = "10.114.20.17";
    public static final String MS17_NAME = "ms17";
    public static final String MS17_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS17_IP +".txt";
    public static final String MS17_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS17_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS17_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS17_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS17_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS17_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS17_IP_MK_MAP = new HashMap<>();

    public static final String MS18_IP = "10.114.20.18";
    public static final String MS18_NAME = "ms18";
    public static final String MS18_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS18_IP +".txt";
    public static final String MS18_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS18_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS18_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS18_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS18_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS18_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS18_IP_MK_MAP = new HashMap<>();

    public static final String MS19_IP = "10.114.20.19";
    public static final String MS19_NAME = "ms19";
    public static final String MS19_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + MS19_IP +".txt";
    public static final String MS19_SYSOID = ".1.3.6.1.4.1.3181.10.6";

    public static final Map<InetAddress,Integer> MS19_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> MS19_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> MS19_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> MS19_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> MS19_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> MS19_IP_MK_MAP = new HashMap<>();

    public static final String PLANET_IP = "10.114.108.8";
    public static final String PLANET_NAME = "planet";
    public static final String PLANET_SNMP_RESOURCE = "classpath:/linkd/nms0000/" + PLANET_NAME +".txt";
    public static final String PLANET_SYSOID = ".1.3.6.1.4.1.10456.9.40";

    public static final Map<InetAddress,Integer> PLANET_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> PLANET_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> PLANET_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> PLANET_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> PLANET_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> PLANET_IP_MK_MAP = new HashMap<>();


    static {
        MS01_IP_IF_MAP.put(InetAddressUtils.addr(MS01_IP),1);
        MS02_IP_IF_MAP.put(InetAddressUtils.addr(MS02_IP),1);
        MS03_IP_IF_MAP.put(InetAddressUtils.addr(MS03_IP),1);
        MS04_IP_IF_MAP.put(InetAddressUtils.addr(MS04_IP),1);
        MS05_IP_IF_MAP.put(InetAddressUtils.addr(MS05_IP),1);
        MS06_IP_IF_MAP.put(InetAddressUtils.addr(MS06_IP),1);
        MS07_IP_IF_MAP.put(InetAddressUtils.addr(MS07_IP),1);
        MS08_IP_IF_MAP.put(InetAddressUtils.addr(MS08_IP),1);
        MS09_IP_IF_MAP.put(InetAddressUtils.addr(MS09_IP),1);
        MS10_IP_IF_MAP.put(InetAddressUtils.addr(MS10_IP),1);
        MS11_IP_IF_MAP.put(InetAddressUtils.addr(MS11_IP),1);
        MS12_IP_IF_MAP.put(InetAddressUtils.addr(MS12_IP),1);
        MS14_IP_IF_MAP.put(InetAddressUtils.addr(MS14_IP),1);
        MS15_IP_IF_MAP.put(InetAddressUtils.addr(MS15_IP),1);
        MS16_IP_IF_MAP.put(InetAddressUtils.addr(MS16_IP),1);
        MS17_IP_IF_MAP.put(InetAddressUtils.addr(MS17_IP),1);
        MS18_IP_IF_MAP.put(InetAddressUtils.addr(MS18_IP),1);
        MS19_IP_IF_MAP.put(InetAddressUtils.addr(MS19_IP),1);
        PLANET_IP_IF_MAP.put(InetAddressUtils.addr(PLANET_IP),1);
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

    public OnmsNode getMs05() {
        return getNode(MS05_NAME,MS05_SYSOID,MS05_IP,
                MS05_IP_IF_MAP,MS05_IF_IFNAME_MAP,MS05_IF_MAC_MAP,
                MS05_IF_IFDESCR_MAP,MS05_IF_IFALIAS_MAP, MS05_IP_MK_MAP);
    }

    public OnmsNode getMs06() {
        return getNode(MS06_NAME,MS06_SYSOID,MS06_IP,
                MS06_IP_IF_MAP,MS06_IF_IFNAME_MAP,MS06_IF_MAC_MAP,
                MS06_IF_IFDESCR_MAP,MS06_IF_IFALIAS_MAP, MS06_IP_MK_MAP);
    }

    public OnmsNode getMs07() {
        return getNode(MS07_NAME,MS07_SYSOID,MS07_IP,
                MS07_IP_IF_MAP,MS07_IF_IFNAME_MAP,MS07_IF_MAC_MAP,
                MS07_IF_IFDESCR_MAP,MS07_IF_IFALIAS_MAP, MS07_IP_MK_MAP);
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

    public OnmsNode getMs10() {
        return getNode(MS10_NAME,MS10_SYSOID,MS10_IP,
                MS10_IP_IF_MAP,MS10_IF_IFNAME_MAP,MS10_IF_MAC_MAP,
                MS10_IF_IFDESCR_MAP,MS10_IF_IFALIAS_MAP, MS10_IP_MK_MAP);
    }

    public OnmsNode getMs11() {
        return getNode(MS11_NAME,MS11_SYSOID,MS11_IP,
                MS11_IP_IF_MAP,MS11_IF_IFNAME_MAP,MS11_IF_MAC_MAP,
                MS11_IF_IFDESCR_MAP,MS11_IF_IFALIAS_MAP, MS11_IP_MK_MAP);
    }

    public OnmsNode getMs12() {
        return getNode(MS12_NAME,MS12_SYSOID,MS12_IP,
                MS12_IP_IF_MAP,MS12_IF_IFNAME_MAP,MS12_IF_MAC_MAP,
                MS12_IF_IFDESCR_MAP,MS12_IF_IFALIAS_MAP, MS12_IP_MK_MAP);
    }

    public OnmsNode getMs14() {
        return getNode(MS14_NAME,MS14_SYSOID,MS14_IP,
                MS14_IP_IF_MAP,MS14_IF_IFNAME_MAP,MS14_IF_MAC_MAP,
                MS14_IF_IFDESCR_MAP,MS14_IF_IFALIAS_MAP, MS14_IP_MK_MAP);
    }

    public OnmsNode getMs15() {
        return getNode(MS15_NAME,MS15_SYSOID,MS15_IP,
                MS15_IP_IF_MAP,MS15_IF_IFNAME_MAP,MS15_IF_MAC_MAP,
                MS15_IF_IFDESCR_MAP,MS15_IF_IFALIAS_MAP, MS15_IP_MK_MAP);
    }

    public OnmsNode getMs16() {
        return getNode(MS16_NAME,MS16_SYSOID,MS16_IP,
                MS16_IP_IF_MAP,MS16_IF_IFNAME_MAP,MS16_IF_MAC_MAP,
                MS16_IF_IFDESCR_MAP,MS16_IF_IFALIAS_MAP, MS16_IP_MK_MAP);
    }

    public OnmsNode getMs17() {
        return getNode(MS17_NAME,MS17_SYSOID,MS17_IP,
                MS17_IP_IF_MAP,MS17_IF_IFNAME_MAP,MS17_IF_MAC_MAP,
                MS17_IF_IFDESCR_MAP,MS17_IF_IFALIAS_MAP, MS17_IP_MK_MAP);
    }

    public OnmsNode getMs18() {
        return getNode(MS18_NAME,MS18_SYSOID,MS18_IP,
                MS18_IP_IF_MAP,MS18_IF_IFNAME_MAP,MS18_IF_MAC_MAP,
                MS18_IF_IFDESCR_MAP,MS18_IF_IFALIAS_MAP, MS18_IP_MK_MAP);
    }

    public OnmsNode getMs19() {
        return getNode(MS19_NAME,MS19_SYSOID,MS19_IP,
                MS19_IP_IF_MAP,MS19_IF_IFNAME_MAP,MS19_IF_MAC_MAP,
                MS19_IF_IFDESCR_MAP,MS19_IF_IFALIAS_MAP, MS19_IP_MK_MAP);
    }
    
    public OnmsNode getPlanet() {
        return getNode(PLANET_NAME,PLANET_SYSOID,PLANET_IP,PLANET_IP_IF_MAP,
                PLANET_IF_IFNAME_MAP,PLANET_IF_MAC_MAP,PLANET_IF_IFDESCR_MAP,
                PLANET_IF_IFALIAS_MAP, PLANET_IP_MK_MAP);
    }

}
