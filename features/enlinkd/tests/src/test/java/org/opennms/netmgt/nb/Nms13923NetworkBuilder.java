/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
