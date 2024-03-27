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

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public class Nms7777DWNetworkBuilder extends NmsNetworkBuilder {

    //NMS7777DRAGONWAVE
    public static final String DW_IP = "10.103.1.1";
    public static final String DW_NAME = "dw";
    public static final String DW_SNMP_RESOURCE = "classpath:/linkd/nms7777dw/"+DW_NAME+"-walk.txt";
    public static final String DW_SYSOID = ".1.3.6.1.4.1.7262.2.4";

    public static final Map<InetAddress,Integer> DW_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String> DW_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String> DW_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String> DW_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String> DW_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> DW_IP_MK_MAP = new HashMap<>();

    static {
    try {
    DW_IP_IF_MAP.put(InetAddressUtils.addr("10.103.1.1"), 1);
    DW_IF_IFNAME_MAP.put(1, "dw-1/1/1");
    DW_IF_IFDESCR_MAP.put(1, "dragon-wave-1/1/1");
    DW_IP_MK_MAP.put(InetAddressUtils.addr("10.103.1.1"), InetAddressUtils.addr("255.255.255.0"));
    DW_IF_MAC_MAP.put(1, "00d1590e43e9");

    } catch (Exception e) {
        
    }
    }
    
    public OnmsNode getDragonWaveRouter() {
        return getNode(DW_NAME,DW_SYSOID,DW_IP,DW_IP_IF_MAP,DW_IF_IFNAME_MAP,DW_IF_MAC_MAP,DW_IF_IFDESCR_MAP,DW_IF_IFALIAS_MAP, DW_IP_MK_MAP);
    }    
}
