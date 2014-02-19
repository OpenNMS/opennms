/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.discovery;

import java.net.InetAddress;

public class IPAddress extends org.opennms.core.network.IPAddress {

    public IPAddress(final IPAddress addr) {
        super(addr);
    }
    
    public IPAddress(final String dottedNotation) {
        super(dottedNotation);
    }
    
    public IPAddress(final InetAddress inetAddress) {
        super(inetAddress);
    }
    
    public IPAddress(final byte[] ipAddrOctets) {
        super(ipAddrOctets);
    }

    public static IPAddress min(final IPAddress a, final IPAddress b) {
        return (IPAddress) IPAddress.min(a, b);
    }

    public IPAddress incr() {
        return (IPAddress) super.incr();
    }

    public IPAddress decr() {
        return (IPAddress) super.decr();

    }

    public static IPAddress max(final IPAddress a, final IPAddress b) {
        return (IPAddress) IPAddress.max(a, b);
    }

}
