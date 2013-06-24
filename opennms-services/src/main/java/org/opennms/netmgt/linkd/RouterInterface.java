/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.net.InetAddress;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.InetAddressUtils;

public class RouterInterface {
	
    private int m_ifIndex;
    private final int m_nextHopNodeId;
    private final int m_nextHopIfIndex;
    private final InetAddress m_nextHopNetmask;
    private InetAddress m_nextHop;
    

	RouterInterface(final int nextHopNodeId, final int nextHopIfIndex, final InetAddress nextHopNetmask) {
		m_nextHopNodeId = nextHopNodeId;
		m_nextHopIfIndex = nextHopIfIndex;
		m_nextHopNetmask = nextHopNetmask;
	}

	RouterInterface(final int nextHopNodeId, final int nextHopIfIndex) {
		m_nextHopNodeId = nextHopNodeId;
		m_nextHopIfIndex = nextHopIfIndex;
		m_nextHopNetmask = InetAddressUtils.TWO_FIFTY_FIVES;
	}

	/**
	 * <p>Getter for the field <code>ifindex</code>.</p>
	 *
	 * @return Returns the ifindex.
	 */
	public int getIfindex() {
		return m_ifIndex;
	}
		
	/**
	 * <p>getNetmask</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getNextHopNetmask() {
		return m_nextHopNetmask;
	}
	/**
	 * <p>getNextHopNodeid</p>
	 *
	 * @return a int.
	 */
	public int getNextHopNodeid() {
		return m_nextHopNodeId;
	}
	/**
	 * <p>Getter for the field <code>nextHopIfindex</code>.</p>
	 *
	 * @return a int.
	 */
	public int getNextHopIfindex() {
		return m_nextHopIfIndex;
	}
	/**
	 * <p>Setter for the field <code>ifindex</code>.</p>
	 *
	 * @param ifindex a int.
	 */
	public void setIfindex(final int ifindex) {
		m_ifIndex = ifindex;
	}
	
	public InetAddress getNextHop() {
		return m_nextHop;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
    @Override
	public String toString() {
	    return new ToStringBuilder(this)
	    .append("ifIndex", m_ifIndex)
	    .append("nextHop", m_nextHop)
	    .append("nextHopNodeId", m_nextHopNodeId)
	    .append("nextHopIfIndex", m_nextHopIfIndex)
	    .append("nextHopNetmask", m_nextHopNetmask)
	    .toString();
	}
	
	public void setNextHop(InetAddress nexthop) {
		m_nextHop = nexthop;
	}
}

