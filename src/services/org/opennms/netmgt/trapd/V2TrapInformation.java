//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Modifications:
//
//2003 Jan 31: Cleaned up some unused imports.
//2003 Jan 08: Added code to associate the IP address in traps with nodes
//           and added the option to discover nodes based on traps.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
//For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
//

package org.opennms.netmgt.trapd;

import java.net.InetAddress;

import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;

/**
 * V2 Trap information object for processing by the queue reader
 */
public class V2TrapInformation {
	/**
	 * The received PDU
	 */
	private SnmpPduPacket m_pdu;

	/**
	 * The internet address of the sending agent
	 */
	private InetAddress m_agent;

	/**
	 * The community string from the actual SNMP packet
	 */
	private SnmpOctetString m_community;

	/**
	 * Constructs a new trap information instance that contains the sending
	 * agent, the community string, and the Protocol Data Unit.
	 * 
	 * @param agent
	 *            The sending agent's address
	 * @param community
	 *            The community string from the SNMP packet.
	 * @param pdu
	 *            The encapsulated Protocol Data Unit.
	 * 
	 */
	public V2TrapInformation(InetAddress agent, SnmpOctetString community,
			SnmpPduPacket pdu) {
		m_pdu = pdu;
		m_agent = agent;
		m_community = community;
	}

	/**
	 * Returns the sending agent's internet address
	 */
	public InetAddress getAgent() {
		return m_agent;
	}

	/**
	 * Returns the Protocol Data Unit that was encapsulated within the SNMP
	 * Trap message
	 */
	public SnmpPduPacket getPdu() {
		return m_pdu;
	}

	/**
	 * Returns the SNMP community string from the received packet.
	 */
	public SnmpOctetString getCommunity() {
		return m_community;
	}
}