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
    private int m_metric;
    private InetAddress m_routeDest;
    private InetAddress m_routeMask;
    private InetAddress m_nextHop;
    private final int m_nextHopNodeId;
    private final int m_nextHopIfIndex;
    private final InetAddress m_nextHopNetmask;
    private int m_snmpIfType; 
	
	RouterInterface(final int nextHopNodeId, final int nextHopIfIndex, final InetAddress nextHopNetmask) {
		m_nextHopNodeId = nextHopNodeId;
		m_nextHopIfIndex = nextHopIfIndex;
		m_nextHopNetmask = nextHopNetmask;
	}

	RouterInterface(final int nextHopNodeId, final int nextHopIfIndex) {
		m_nextHopNodeId = nextHopNodeId;
		m_nextHopIfIndex = nextHopIfIndex;
		m_nextHopNetmask = InetAddressUtils.getInetAddress("255.255.255.255");
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
	 * <p>Getter for the field <code>metric</code>.</p>
	 *
	 * @return Returns the metric.
	 */
	public int getMetric() {
		return m_metric;
	}
	/**
	 * <p>Setter for the field <code>metric</code>.</p>
	 *
	 * @param metric The metric to set.
	 */
	public void setMetric(final int metric) {
		m_metric = metric;
	}
	/**
	 * <p>Getter for the field <code>nextHop</code>.</p>
	 *
	 * @return Returns the nextHop.
	 */
	public InetAddress getNextHop() {
		return m_nextHop;
	}
	/**
	 * <p>Setter for the field <code>nextHop</code>.</p>
	 *
	 * @param nextHop The nextHop to set.
	 */
	public void setNextHop(final InetAddress nextHop) {
		m_nextHop = nextHop;
	}
	/**
	 * <p>Getter for the field <code>snmpiftype</code>.</p>
	 *
	 * @return Returns the snmpiftype.
	 */
	public int getSnmpiftype() {
		return m_snmpIfType;
	} 
	
	/**
	 * <p>Setter for the field <code>snmpiftype</code>.</p>
	 *
	 * @param snmpiftype The snmpiftype to set.
	 */
	public void setSnmpiftype(final int snmpiftype) {
		m_snmpIfType = snmpiftype;
	}
	
	/**
	 * <p>getNetmask</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getNetmask() {
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
	
	/**
	 * <p>getNextHopNet</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getNextHopNet() {
	    final byte[] ipAddress = m_nextHop.getAddress();
		final byte[] netMask = m_nextHopNetmask.getAddress();
		final byte[] netWork = new byte[4];

		for (int i=0;i< 4; i++) {
			netWork[i] = Integer.valueOf(ipAddress[i] & netMask[i]).byteValue();
			
		}
		return InetAddressUtils.getInetAddress(netWork);
	}

	/**
	 * <p>getRouteNet</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getRouteNet() {
	    final byte[] ipAddress = m_routeDest.getAddress();
		final byte[] netMask = m_routeMask.getAddress();
		final byte[] netWork = new byte[4];

		for (int i=0;i< 4; i++) {
			netWork[i] = Integer.valueOf(ipAddress[i] & netMask[i]).byteValue();
			
		}
		return InetAddressUtils.getInetAddress(netWork);
	}

	/**
	 * <p>getRouteDest</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getRouteDest() {
		return m_routeDest;
	}

	/**
	 * <p>setRouteDest</p>
	 *
	 * @param routedest a {@link java.net.InetAddress} object.
	 */
	public void setRouteDest(final InetAddress routedest) {
		m_routeDest = routedest;
	}

	/**
	 * <p>Getter for the field <code>routemask</code>.</p>
	 *
	 * @return a {@link java.net.InetAddress} object.
	 */
	public InetAddress getRoutemask() {
		return m_routeMask;
	}

	/**
	 * <p>Setter for the field <code>routemask</code>.</p>
	 *
	 * @param routemask a {@link java.net.InetAddress} object.
	 */
	public void setRoutemask(final InetAddress routemask) {
		m_routeMask = routemask;
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
	    return new ToStringBuilder(this)
	    .append("ifIndex", m_ifIndex)
	    .append("metric", m_metric)
	    .append("routeDest", m_routeDest)
	    .append("routeMask", m_routeMask)
	    .append("nextHop", m_nextHop)
	    .append("nextHopNodeId", m_nextHopNodeId)
	    .append("nextHopIfIndex", m_nextHopIfIndex)
	    .append("nextHopNetmask", m_nextHopNetmask)
	    .append("snmpIfType", m_snmpIfType)
	    .toString();
	}
}

