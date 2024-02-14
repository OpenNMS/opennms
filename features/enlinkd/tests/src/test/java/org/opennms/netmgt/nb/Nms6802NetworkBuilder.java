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

public class Nms6802NetworkBuilder extends NmsNetworkBuilder {


    //NMS6802 cisco ISIS enabled device
    public static final String CISCOISIS_IP = "10.100.68.2";
    public static final String CISCOISIS_NAME = "cisco-ios-xr";
    public static final String CISCOISIS_SNMP_RESOURCE = "classpath:/linkd/nms6802/"+CISCOISIS_NAME+"-walk.txt";
    public static final String CISCOISIS_SYSOID = ".1.3.6.1.4.1.9.1.2090";
    public static final String CISCOISIS_ISIS_SYS_ID = "093176090107";

    public static final Map<InetAddress,Integer> CISCOISIS_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      CISCOISIS_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      CISCOISIS_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      CISCOISIS_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      CISCOISIS_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> CISCOISIS_IP_MK_MAP = new HashMap<>();

    static {
        try {
            CISCOISIS_IP_IF_MAP.put(InetAddressUtils.addr("10.100.68.2"), 1);
            CISCOISIS_IF_IFNAME_MAP.put(1, "eth0/1");
            CISCOISIS_IF_IFDESCR_MAP.put(1, "ethernet0/1");
            CISCOISIS_IP_MK_MAP.put(InetAddressUtils.addr("10.100.68.2"), InetAddressUtils.addr("255.255.255.0"));
            CISCOISIS_IF_MAC_MAP.put(1, "00d1590e4310");

        } catch (Exception e) {
        
        }
    }
    
    public OnmsNode getCiscoIosXrRouter() {
        return getNode(CISCOISIS_NAME,CISCOISIS_SYSOID,CISCOISIS_IP,CISCOISIS_IP_IF_MAP,CISCOISIS_IF_IFNAME_MAP,CISCOISIS_IF_MAC_MAP,CISCOISIS_IF_IFDESCR_MAP,CISCOISIS_IF_IFALIAS_MAP, CISCOISIS_IP_MK_MAP);
    }    
}
