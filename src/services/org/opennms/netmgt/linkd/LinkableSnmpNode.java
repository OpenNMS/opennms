//
// Copyright (C) 2002 Sortova Consulting Group, Inc. All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//

package org.opennms.netmgt.linkd;

import org.opennms.protocols.snmp.SnmpPeer;

public class LinkableSnmpNode {

	int m_nodeId;

	String m_snmpprimaryaddr;

	String m_sysoid;

	String m_vlanoid;

	boolean m_hasvlanoid   = false;
	
	SnmpPeer m_snmpeer;
	
	SnmpCollection m_snmpcoll;

	private LinkableSnmpNode() {
		throw new UnsupportedOperationException(
		"default constructor not supported");
	}

	public LinkableSnmpNode(int nodeId, String snmprimaryaddr, String sysoid) {
		m_nodeId = nodeId;
		m_snmpprimaryaddr = snmprimaryaddr;
		m_sysoid = sysoid;
	}

	public String toString() {
		StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n");
		str.append("Snmp Primary Ip Address = " + m_snmpprimaryaddr + "\n");
		str.append("Snmp System Oid= " + m_sysoid + "\n");
		return str.toString();
	}

	/**
	 * @return
	 */
	public int getNodeId() {
		return m_nodeId;
	}

	/**
	 * @return
	 */
	public String getSnmpPrimaryIpAddr() {
		return m_snmpprimaryaddr;
	}

	/**
	 * @return
	 */
	public String getSysOid() {
		return m_sysoid;
	}

	/**
	 * @return Returns the m_vlanoid.
	 */
	public String getVlanOid() {
		return m_vlanoid;
	}

	/**
	 * @return Returns the m_vlanoid.
	 */
	public void setVlanOid(String m_vlanoid) {
		if (m_vlanoid == null) {
			m_hasvlanoid = false;
			return;
		} else {
			this.m_vlanoid = m_vlanoid;
			m_hasvlanoid = true;
		}
	}


	public boolean hasSameVlanOid(LinkableSnmpNode lnode) {
		if (lnode != null) {
			if (lnode.hasVlanOid() && this.hasVlanOid()) {
				if (lnode.getVlanOid().equals(this.getVlanOid())) return true;
			}
			if (!lnode.hasVlanOid() && !this.hasVlanOid()) return true;
		}
		return false;
	}
	/**
	 * @return 
	 */
	public boolean hasVlanOid() {
		return m_hasvlanoid;
	}

	/**
	 * @return Returns the m_snmpeer.
	 */
	public SnmpPeer getSnmpPeer() {
		return m_snmpeer;
	}
	/**
	 * @param m_snmpeer The m_snmpeer to set.
	 */
	public void setSnmpPeer(SnmpPeer m_snmpeer) {
		this.m_snmpeer = m_snmpeer;
	}

	/**
	 * @return Returns the m_snmpcoll.
	 */
	public SnmpCollection getSnmpCollection() {
		return m_snmpcoll;
	}
	
	/**
	 * @param m_snmpcoll The m_snmpcoll to set.
	 */
	public void setSnmpCollection(SnmpCollection m_snmpcoll) {
		this.m_snmpcoll = m_snmpcoll;
	}

}