/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.core.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.InetAddressUtils;

@Command(scope = "opennms", name = "ip-addr-range", description = "Returns a list of IP addresses in a given range. Useful with closures.")
@Service
public class IpAddressRangeCommand implements Action {

    @Argument(index = 0, name = "start-address", description = "First IP address in range", required = true, multiValued = false)
    private String m_startAddress;
    
    @Argument(index = 1, name = "end-address", description = "Last IP address in range", required = true, multiValued = false)
    private String m_endAddress;

    
    @Override
    public Object execute() {
        InetAddress startAddr = null;
        InetAddress endAddr = null;
        try {
            startAddr = InetAddress.getByName(m_startAddress);
            endAddr = InetAddress.getByName(m_endAddress);
        } catch (UnknownHostException uhe) {
            System.out.println(uhe.getMessage());
            return null;
        }
        
        if (startAddr.getClass() != endAddr.getClass()) {
            System.out.println("Start and end addresses must be of same class (IPv4 / IPv6)");
            return null;
        }
        
        if (InetAddressUtils.difference(endAddr, startAddr).longValue() < 1) {
            System.out.println("Start address must be lower than end address.");
            return null;
        }

        String thisAddr = InetAddressUtils.str(startAddr);
        List<String> addresses = new ArrayList<>();
        addresses.add(thisAddr);
        for (int i = 0; i < InetAddressUtils.difference(endAddr,  startAddr).intValue(); i++) {
            try {
                thisAddr = InetAddressUtils.incr(thisAddr);
                addresses.add(thisAddr);
            } catch (UnknownHostException e) {
                // gulp
            }
        }
            
        return addresses; 
    }

}
