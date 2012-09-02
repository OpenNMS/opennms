/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.filter.FilterParseException;

public class MockFilterDao implements FilterDao {
    private final List<InetAddress> m_activeAddresses;
    
    public MockFilterDao() {
        m_activeAddresses = new ArrayList<InetAddress>();
        m_activeAddresses.add(InetAddressUtils.addr("192.168.1.10"));
        m_activeAddresses.add(InetAddressUtils.addr("192.168.160.250"));
        m_activeAddresses.add(InetAddressUtils.addr("192.168.160.251"));
        m_activeAddresses.add(InetAddressUtils.addr("192.168.160.253"));
    }

    @Override
    public SortedMap<Integer, String> getNodeMap(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Map<InetAddress, Set<String>> getIPAddressServiceMap(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<InetAddress> getActiveIPAddressList(final String rule) throws FilterParseException {
        return m_activeAddresses;
    }

    @Override
    public List<InetAddress> getIPAddressList(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isValid(final String addr, final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean isRuleMatching(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void validateRule(final String rule) throws FilterParseException {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
