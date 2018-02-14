/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;

import com.google.common.collect.Iterables;

/**
 * Given a list of managed IP addresses, this filter will match IP addresses not in that
 * list, hence it is an unmanaged IP address filter.
 */
public class UnmanagedInterfaceFilter implements IpAddressFilter {

    private final InterfaceToNodeCache interfaceToNodeCache;

    public UnmanagedInterfaceFilter(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = Objects.requireNonNull(interfaceToNodeCache);
    }

    @Override
    public boolean matches(String location, InetAddress address) {
        return Iterables.isEmpty(interfaceToNodeCache.getNodeId(location, address));
    }

    @Override
    public boolean matches(String location, String address) {
        return matches(location, InetAddressUtils.addr(address));
    }
}
