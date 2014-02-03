/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;


import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;

public class LinkdBaseTest  {

    @Test
    public void testDiscoveryOspfGetSubNetAddress() throws Exception {
        DiscoveryLink discovery = new DiscoveryLink();
        OspfNbrInterface ospfinterface = new OspfNbrInterface(InetAddressUtils.addr("192.168.9.1"));
        ospfinterface.setOspfNbrIpAddr(InetAddressUtils.addr("192.168.15.45"));

        ospfinterface.setOspfNbrNetMask(InetAddressUtils.addr("255.255.255.0"));        
        assertEquals(InetAddressUtils.addr("192.168.15.0"), discovery.getSubnetAddress(ospfinterface));
        
        ospfinterface.setOspfNbrNetMask(InetAddressUtils.addr("255.255.0.0"));
        assertEquals(InetAddressUtils.addr("192.168.0.0"), discovery.getSubnetAddress(ospfinterface));

        ospfinterface.setOspfNbrNetMask(InetAddressUtils.addr("255.255.255.252"));
        assertEquals(InetAddressUtils.addr("192.168.15.44"), discovery.getSubnetAddress(ospfinterface));

        ospfinterface.setOspfNbrNetMask(InetAddressUtils.addr("255.255.255.240"));
        assertEquals(InetAddressUtils.addr("192.168.15.32"), discovery.getSubnetAddress(ospfinterface));

    }

    @Test
    public void testBridgePortFromDesignatedBridgePort() {
        assertEquals(5826, 8191 & Integer.parseInt("96c2",16));
        assertEquals(5781, 8191 & Integer.parseInt("9695",16));
        assertEquals(4230, 8191 & Integer.parseInt("9086",16));
        assertEquals(110, 8191 & Integer.parseInt("806e",16));
     }

}
