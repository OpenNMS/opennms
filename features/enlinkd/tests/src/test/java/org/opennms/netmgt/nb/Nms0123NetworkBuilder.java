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

public class Nms0123NetworkBuilder extends NmsNetworkBuilder {
    //NMS0123
    public static final String ITPN0111_IP = "10.1.1.1";
    public static final String ITPN0111_NAME = "ITPN0111";
    public static final String ITPN0111_SNMP_RESOURCE = "classpath:/linkd/nms0123/"+ITPN0111_NAME+".txt";
    public static final String ITPN0111_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0111_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      ITPN0111_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0111_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0111_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0111_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> ITPN0111_IP_MK_MAP = new HashMap<>();

    public static final String ITPN0112_IP = "10.1.1.2";
    public static final String ITPN0112_NAME = "ITPN0112";
    public static final String ITPN0112_SNMP_RESOURCE = "classpath:/linkd/nms0123/"+ITPN0112_NAME+".txt";
    public static final String ITPN0112_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0112_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      ITPN0112_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0112_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0112_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0112_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> ITPN0112_IP_MK_MAP = new HashMap<>();

    public static final String ITPN0113_IP = "10.1.1.3";
    public static final String ITPN0113_NAME = "ITPN0113";
    public static final String ITPN0113_SNMP_RESOURCE = "classpath:/linkd/nms0123/"+ITPN0113_NAME+".txt";
    public static final String ITPN0113_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0113_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      ITPN0113_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0113_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0113_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0113_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> ITPN0113_IP_MK_MAP = new HashMap<>();

    public static final String ITPN0114_IP = "10.1.1.4";
    public static final String ITPN0114_NAME = "ITPN0114";
    public static final String ITPN0114_SNMP_RESOURCE = "classpath:/linkd/nms0123/"+ITPN0114_NAME+".txt";
    public static final String ITPN0114_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0114_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      ITPN0114_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0114_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0114_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0114_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> ITPN0114_IP_MK_MAP = new HashMap<>();

    public static final String ITPN0121_IP = "10.1.1.5";
    public static final String ITPN0121_NAME = "ITPN0121";
    public static final String ITPN0121_SNMP_RESOURCE = "classpath:/linkd/nms0123/"+ITPN0121_NAME+".txt";
    public static final String ITPN0121_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0121_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      ITPN0121_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0121_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0121_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0121_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> ITPN0121_IP_MK_MAP = new HashMap<>();

    public static final String ITPN0123_IP = "10.1.1.6";
    public static final String ITPN0123_NAME = "ITPN0123";
    public static final String ITPN0123_SNMP_RESOURCE = "classpath:/linkd/nms0123/"+ITPN0123_NAME+".txt";
    public static final String ITPN0123_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0123_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      ITPN0123_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0123_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0123_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0123_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> ITPN0123_IP_MK_MAP = new HashMap<>();

    public static final String ITPN0201_IP = "10.1.1.7";
    public static final String ITPN0201_NAME = "ITPN0201";
    public static final String ITPN0201_SNMP_RESOURCE = "classpath:/linkd/nms0123/"+ITPN0201_NAME+".txt";
    public static final String ITPN0201_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0201_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      ITPN0201_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0201_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0201_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0201_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> ITPN0201_IP_MK_MAP = new HashMap<>();

    public static final String ITPN0202_IP = "10.1.1.8";
    public static final String ITPN0202_NAME = "ITPN0202";
    public static final String ITPN0202_SNMP_RESOURCE = "classpath:/linkd/nms0123/"+ITPN0202_NAME+".txt";
    public static final String ITPN0202_SYSOID = ".1.3.6.1.4.1.2636.1.1.1.2.31";

    public static final Map<InetAddress,Integer> ITPN0202_IP_IF_MAP =  new HashMap<>();
    public static final Map<Integer,String>      ITPN0202_IF_IFNAME_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0202_IF_IFDESCR_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0202_IF_MAC_MAP = new HashMap<>();
    public static final Map<Integer,String>      ITPN0202_IF_IFALIAS_MAP = new HashMap<>();
    public static final Map<InetAddress,InetAddress> ITPN0202_IP_MK_MAP = new HashMap<>();


    static {
    try {
        ITPN0111_IP_IF_MAP.put(InetAddressUtils.addr(ITPN0111_IP), 16);
        ITPN0111_IP_MK_MAP.put(InetAddressUtils.addr(ITPN0111_IP), InetAddressUtils.addr("255.255.255.0"));
        ITPN0111_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0111_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0111_IF_MAC_MAP.put(16, "");
        
        ITPN0112_IP_IF_MAP.put(InetAddressUtils.addr(ITPN0112_IP), 16);
        ITPN0112_IP_MK_MAP.put(InetAddressUtils.addr(ITPN0112_IP), InetAddressUtils.addr("255.255.255.0"));
        ITPN0112_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0112_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0112_IF_MAC_MAP.put(16, "");

        ITPN0113_IP_IF_MAP.put(InetAddressUtils.addr(ITPN0113_IP), 16);
        ITPN0113_IP_MK_MAP.put(InetAddressUtils.addr(ITPN0113_IP), InetAddressUtils.addr("255.255.255.0"));
        ITPN0113_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0113_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0113_IF_MAC_MAP.put(16, "");

        ITPN0114_IP_IF_MAP.put(InetAddressUtils.addr(ITPN0114_IP), 16);
        ITPN0114_IP_MK_MAP.put(InetAddressUtils.addr(ITPN0114_IP), InetAddressUtils.addr("255.255.255.0"));
        ITPN0114_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0114_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0114_IF_MAC_MAP.put(16, "");

        ITPN0121_IP_IF_MAP.put(InetAddressUtils.addr(ITPN0121_IP), 16);
        ITPN0121_IP_MK_MAP.put(InetAddressUtils.addr(ITPN0121_IP), InetAddressUtils.addr("255.255.255.0"));
        ITPN0121_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0121_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0121_IF_MAC_MAP.put(16, "");

        ITPN0123_IP_IF_MAP.put(InetAddressUtils.addr(ITPN0123_IP), 16);
        ITPN0123_IP_MK_MAP.put(InetAddressUtils.addr(ITPN0123_IP), InetAddressUtils.addr("255.255.255.0"));
        ITPN0123_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0123_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0123_IF_MAC_MAP.put(16, "");

        ITPN0201_IP_IF_MAP.put(InetAddressUtils.addr(ITPN0201_IP), 16);
        ITPN0201_IP_MK_MAP.put(InetAddressUtils.addr(ITPN0201_IP), InetAddressUtils.addr("255.255.255.0"));
        ITPN0201_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0201_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0201_IF_MAC_MAP.put(16, "");

        ITPN0202_IP_IF_MAP.put(InetAddressUtils.addr(ITPN0202_IP), 16);
        ITPN0202_IP_MK_MAP.put(InetAddressUtils.addr(ITPN0202_IP), InetAddressUtils.addr("255.255.255.0"));
        ITPN0202_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0202_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0202_IF_MAC_MAP.put(16, "");


    } catch (Exception ignored) {
        
    }
    }
    
    public OnmsNode getItpn0111() {
        return getNode(ITPN0111_NAME,ITPN0111_SYSOID,ITPN0111_IP,ITPN0111_IP_IF_MAP,ITPN0111_IF_IFNAME_MAP,ITPN0111_IF_MAC_MAP,ITPN0111_IF_IFDESCR_MAP,ITPN0111_IF_IFALIAS_MAP, ITPN0111_IP_MK_MAP);
    }    
    
    public OnmsNode getItpn0112() {
        return getNode(ITPN0112_NAME,ITPN0112_SYSOID,ITPN0112_IP,ITPN0112_IP_IF_MAP,ITPN0112_IF_IFNAME_MAP,ITPN0112_IF_MAC_MAP,ITPN0112_IF_IFDESCR_MAP,ITPN0112_IF_IFALIAS_MAP, ITPN0112_IP_MK_MAP);
    }    
    
    public OnmsNode getItpn0113() {
        return getNode(ITPN0113_NAME,ITPN0113_SYSOID,ITPN0113_IP,ITPN0113_IP_IF_MAP,ITPN0113_IF_IFNAME_MAP,ITPN0113_IF_MAC_MAP,ITPN0113_IF_IFDESCR_MAP,ITPN0113_IF_IFALIAS_MAP, ITPN0113_IP_MK_MAP);
    }    

    public OnmsNode getItpn0114() {
        return getNode(ITPN0114_NAME,ITPN0114_SYSOID,ITPN0114_IP,ITPN0114_IP_IF_MAP,ITPN0114_IF_IFNAME_MAP,ITPN0114_IF_MAC_MAP,ITPN0114_IF_IFDESCR_MAP,ITPN0114_IF_IFALIAS_MAP, ITPN0114_IP_MK_MAP);
    }    

    public OnmsNode getItpn0121() {
        return getNode(ITPN0121_NAME,ITPN0121_SYSOID,ITPN0121_IP,ITPN0121_IP_IF_MAP,ITPN0121_IF_IFNAME_MAP,ITPN0121_IF_MAC_MAP,ITPN0121_IF_IFDESCR_MAP,ITPN0121_IF_IFALIAS_MAP, ITPN0121_IP_MK_MAP);
    }    

    public OnmsNode getItpn0123() {
        return getNode(ITPN0123_NAME,ITPN0123_SYSOID,ITPN0123_IP,ITPN0123_IP_IF_MAP,ITPN0123_IF_IFNAME_MAP,ITPN0123_IF_MAC_MAP,ITPN0123_IF_IFDESCR_MAP,ITPN0123_IF_IFALIAS_MAP,ITPN0123_IP_MK_MAP);
    }    

    public OnmsNode getItpn0201() {
        return getNode(ITPN0201_NAME,ITPN0201_SYSOID,ITPN0201_IP,ITPN0201_IP_IF_MAP,ITPN0201_IF_IFNAME_MAP,ITPN0201_IF_MAC_MAP,ITPN0201_IF_IFDESCR_MAP,ITPN0201_IF_IFALIAS_MAP, ITPN0201_IP_MK_MAP);
    }    

    public OnmsNode getItpn0202() {
        return getNode(ITPN0202_NAME,ITPN0202_SYSOID,ITPN0202_IP,ITPN0202_IP_IF_MAP,ITPN0202_IF_IFNAME_MAP,ITPN0202_IF_MAC_MAP,ITPN0202_IF_IFDESCR_MAP,ITPN0202_IF_IFALIAS_MAP, ITPN0202_IP_MK_MAP);
    }    
}
