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

public class Nms7777DWNetworkBuilder extends NmsNetworkBuilder {


    static {
    try {
    DW_IP_IF_MAP.put(InetAddressUtils.addr("10.103.1.1"), 1);
    DW_IF_IFNAME_MAP.put(1, "dw-1/1/1");
    DW_IF_IFDESCR_MAP.put(1, "dragon-wave-1/1/1");
    DW_IF_NETMASK_MAP.put(1, InetAddressUtils.addr("255.255.255.0"));
    DW_IF_MAC_MAP.put(1, "00d1590e43e9");

    } catch (Exception e) {
        
    }
    }
    
    public OnmsNode getDragonWaveRouter() {
        return getNode(DW_NAME,DW_SYSOID,DW_IP,DW_IP_IF_MAP,DW_IF_IFNAME_MAP,DW_IF_MAC_MAP,DW_IF_IFDESCR_MAP,DW_IF_IFALIAS_MAP);
    }    
}
