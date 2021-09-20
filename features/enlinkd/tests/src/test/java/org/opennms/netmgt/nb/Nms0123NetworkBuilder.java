/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsNode;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */

public class Nms0123NetworkBuilder extends NmsNetworkBuilder {


    static {
    try {
        ITPN0111_IP_IF_MAP.put(InetAddressUtils.addr("1.1.1.1"), 16);
        ITPN0111_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0111_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0111_IF_NETMASK_MAP.put(16, InetAddressUtils.addr("255.255.255.255"));
        ITPN0111_IF_MAC_MAP.put(16, "");
        
        ITPN0112_IP_IF_MAP.put(InetAddressUtils.addr("2.2.2.2"), 16);
        ITPN0112_IF_IFNAME_MAP.put(16, "lo0.0");
        ITPN0112_IF_IFDESCR_MAP.put(16, "lo0.0");
        ITPN0112_IF_NETMASK_MAP.put(16, InetAddressUtils.addr("255.255.255.255"));
        ITPN0112_IF_MAC_MAP.put(16, "");
    } catch (Exception e) {
        
    }
    }
    
    public OnmsNode getItpn0111() {
        return getNode(ITPN0111_NAME,ITPN0111_SYSOID,ITPN0111_IP,ITPN0111_IP_IF_MAP,ITPN0111_IF_IFNAME_MAP,ITPN0111_IF_MAC_MAP,ITPN0111_IF_IFDESCR_MAP,ITPN0111_IF_IFALIAS_MAP);
    }    
    
    public OnmsNode getItpn0112() {
        return getNode(ITPN0112_NAME,ITPN0112_SYSOID,ITPN0112_IP,ITPN0112_IP_IF_MAP,ITPN0112_IF_IFNAME_MAP,ITPN0112_IF_MAC_MAP,ITPN0112_IF_IFDESCR_MAP,ITPN0112_IF_IFALIAS_MAP);
    }    
    
    public OnmsNode getItpn0113() {
        return getNode(ITPN0113_NAME,ITPN0113_SYSOID,ITPN0113_IP,ITPN0113_IP_IF_MAP,ITPN0113_IF_IFNAME_MAP,ITPN0113_IF_MAC_MAP,ITPN0113_IF_IFDESCR_MAP,ITPN0113_IF_IFALIAS_MAP);
    }    

    public OnmsNode getItpn0114() {
        return getNode(ITPN0114_NAME,ITPN0114_SYSOID,ITPN0114_IP,ITPN0114_IP_IF_MAP,ITPN0114_IF_IFNAME_MAP,ITPN0114_IF_MAC_MAP,ITPN0114_IF_IFDESCR_MAP,ITPN0114_IF_IFALIAS_MAP);
    }    

    public OnmsNode getItpn0121() {
        return getNode(ITPN0121_NAME,ITPN0121_SYSOID,ITPN0121_IP,ITPN0121_IP_IF_MAP,ITPN0121_IF_IFNAME_MAP,ITPN0121_IF_MAC_MAP,ITPN0121_IF_IFDESCR_MAP,ITPN0121_IF_IFALIAS_MAP);
    }    

    public OnmsNode getItpn0123() {
        return getNode(ITPN0123_NAME,ITPN0123_SYSOID,ITPN0123_IP,ITPN0123_IP_IF_MAP,ITPN0123_IF_IFNAME_MAP,ITPN0123_IF_MAC_MAP,ITPN0123_IF_IFDESCR_MAP,ITPN0123_IF_IFALIAS_MAP);
    }    

    public OnmsNode getItpn0201() {
        return getNode(ITPN0201_NAME,ITPN0201_SYSOID,ITPN0201_IP,ITPN0201_IP_IF_MAP,ITPN0201_IF_IFNAME_MAP,ITPN0201_IF_MAC_MAP,ITPN0201_IF_IFDESCR_MAP,ITPN0201_IF_IFALIAS_MAP);
    }    

    public OnmsNode getItpn0202() {
        return getNode(ITPN0202_NAME,ITPN0202_SYSOID,ITPN0202_IP,ITPN0202_IP_IF_MAP,ITPN0202_IF_IFNAME_MAP,ITPN0202_IF_MAC_MAP,ITPN0202_IF_IFDESCR_MAP,ITPN0202_IF_IFALIAS_MAP);
    }    
}
