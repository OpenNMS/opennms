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

package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>
 * LinkableNode class.
 * </p>
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class Node {

    private final int m_nodeId;

    private final InetAddress m_snmpprimaryaddr;

    private final String m_sysoid;

    private final String m_sysname;

    private final String m_location;

    public String getLocation() {
        return m_location;
    }

    /**
     * <p>
     * Constructor for LinkableSnmpNode.
     * </p>
     * 
     * @param nodeId
     *            a int.
     * @param snmprimaryaddr
     *            a {@link java.net.InetAddress} object.
     * @param sysoid
     *            a {@link java.lang.String} object.
     */
    public Node(final int nodeId,
            final InetAddress snmpPrimaryAddr, final String sysoid, final String sysname, final String location) {
        m_nodeId = nodeId;
        m_snmpprimaryaddr = snmpPrimaryAddr;
        m_sysoid = sysoid;
        m_sysname = sysname;
        m_location = location;
    }

    /**
     * <p>
     * toString
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("nodeId", m_nodeId).append("snmpPrimaryAddr",
                                                                           str(m_snmpprimaryaddr)).append("sysOid",
                                                                                                          m_sysoid).toString();
    }

    /**
     * <p>
     * getNodeId
     * </p>
     * 
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>
     * getSnmpPrimaryIpAddr
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public InetAddress getSnmpPrimaryIpAddr() {
        return m_snmpprimaryaddr;
    }

    /**
     * <p>
     * getSysoid
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getSysoid() {
        return m_sysoid;
    }

    /**
     * <p>
     * getSysname
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getSysname() {
        return m_sysname;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_nodeId;
		result = prime
				* result
				+ ((m_snmpprimaryaddr == null) ? 0 : m_snmpprimaryaddr
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (m_nodeId != other.m_nodeId)
			return false;
		if (m_snmpprimaryaddr == null) {
			if (other.m_snmpprimaryaddr != null)
				return false;
		} else if (!m_snmpprimaryaddr.equals(other.m_snmpprimaryaddr))
			return false;
		return true;
	}

}
