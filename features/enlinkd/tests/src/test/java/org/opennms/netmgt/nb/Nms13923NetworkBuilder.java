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
import java.util.Locale;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public class Nms13923NetworkBuilder extends NmsNetworkBuilder {

    public static final String srv005_NAME="srv005";
    public static final String srv005_IP="10.119.77.21";
    public static final String srv005_SYSOID=".1.3.6.1.4.1.6527.1.6.4";
    public static final String srv005_LLDP_ID="00 16 4D DD D5 5B".replaceAll("\\s+","").toLowerCase(Locale.ROOT);
    public static final String srv005_RESOURCE="classpath:/linkd/nms13923/srv005.txt";
    public static final Map<InetAddress,Integer> srv005_IP_IF_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> srv005_IP_MK_MAP = new HashMap<>();
    public static final Map<Integer,String>      srv005_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      srv005_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      srv005_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      srv005_IF_IFALIAS_MAP = new HashMap<>();


    static {
    try {
        srv005_IP_IF_MAP.put(InetAddressUtils.addr(srv005_IP), 1);
        srv005_IP_MK_MAP.put(InetAddressUtils.addr(srv005_IP),InetAddressUtils.addr("255.255.255.0"));
    srv005_IF_IFDESCR_MAP.put(1, "system, Loopback IP interface");
        srv005_IF_IFNAME_MAP.put(1, "lo0");
        srv005_IF_IFALIAS_MAP.put(1, "");

    } catch (Exception ignored) {
        
    }
    }
    
    public OnmsNode getSrv005() {
        return getNode(srv005_NAME,srv005_SYSOID,srv005_IP,srv005_IP_IF_MAP,srv005_IF_IFNAME_MAP,
                srv005_IF_MAC_MAP,srv005_IF_IFDESCR_MAP,srv005_IF_IFALIAS_MAP, srv005_IP_MK_MAP);
    }

}
