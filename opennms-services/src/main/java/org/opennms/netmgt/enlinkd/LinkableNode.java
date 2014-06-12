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

package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>LinkableNode class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class LinkableNode {

    private final int m_nodeId;

    private final InetAddress m_snmpprimaryaddr;
	
    private final String m_sysoid;
        
	/**
	 * <p>Constructor for LinkableNode.</p>
	 *
	 * @param nodeId a int.
	 * @param snmprimaryaddr a {@link java.net.InetAddress} object.
	 * @param sysoid a {@link java.lang.String} object.
	 */
	public LinkableNode(final int nodeId, final InetAddress snmpPrimaryAddr, final String sysoid) {
		m_nodeId = nodeId;
		m_snmpprimaryaddr = snmpPrimaryAddr;
		m_sysoid = sysoid;
	}
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
	    return new ToStringBuilder(this)
	        .append("nodeId", m_nodeId)
	        .append("snmpPrimaryAddr", str(m_snmpprimaryaddr))
	        .append("sysOid", m_sysoid)
	        .toString();
	}

	/**
	 * <p>getNodeId</p>
	 *
	 * @return a int.
	 */
	public int getNodeId() {
		return m_nodeId;
	}

	/**
	 * <p>getSnmpPrimaryIpAddr</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public InetAddress getSnmpPrimaryIpAddr() {
		return m_snmpprimaryaddr;
	}

	/**
	 * <p>getSysoid</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSysoid() {
		return m_sysoid;
	}

}
