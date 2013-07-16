/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Comparator;

/**
 * <p>This comparator will sort {@link InetAddress} instances in the following order:</p>
 * 
 * <ul>
 * <li><code>Inet4Address</code> instances</li>
 * <li><code>Inet6Address</code> instances that are routable with scopeId == 0</li>
 * <li><code>Inet6Address</code> instances that are link-local ordered by scopeId</li>
 * </ul>
 */
public class InetAddressComparator implements Comparator<InetAddress> {

    @Override
    public int compare(InetAddress addr1, InetAddress addr2) {
        if (addr1 == null) {
            if (addr2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (addr2 == null) {
                return 1;
            } else {
                if (addr1 instanceof Inet4Address) {
                    if (addr2 instanceof Inet4Address) {
                        // Two Inet4Address instances
                        return new ByteArrayComparator().compare(addr1.getAddress(), addr2.getAddress());
                    } else {
                        return -1;
                    }
                } else {
                    if (addr2 instanceof Inet4Address) {
                        return 1;
                    } else {
                        // Two Inet6Address instances
                        int scopeComparison = Integer.valueOf(((Inet6Address)addr1).getScopeId()).compareTo(((Inet6Address)addr2).getScopeId());
                        if (scopeComparison == 0) {
                            // If the scope IDs are identical, then compare the addresses
                            return new ByteArrayComparator().compare(addr1.getAddress(), addr2.getAddress());
                        } else {
                           return scopeComparison;
                        }
                    }
                }
            }
        }
    }
}
