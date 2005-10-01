//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.linkd;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.linkd.snmp.*;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.utils.BarrierSignaler;
import org.opennms.protocols.snmp.SnmpParameters;
import org.opennms.protocols.snmp.SnmpPeer;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session creating
 * and colletion occurs in the main run method of the instance. This allows the
 * collection to occur in a thread if necessary.
 * 
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 *  
 */
final class SnmpVlanCollection implements ReadyRunnable {
	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpPeer m_peer;

	/**
	 * The IP address to used to collect the SNMP information
	 */
	private final InetAddress m_address;

	/**
	 * The vlan index used to collect the SNMP information
	 */
	public String m_vlan = "1";

	/**
	 * The Dot1DBridge.dot1dbase map
	 */
	public Dot1dBaseGroup m_dot1dbase;

	/**
	 * The Dot1DBridge Ids List
	 */
	public Dot1dBasePortTable m_dot1dbaseTable;

	/**
	 * The Dot1dStp base map
	 */
	public Dot1dStpGroup m_dot1dstp;

	/**
	 * The Dot1dStp Port table List
	 */
	public Dot1dStpPortTable m_dot1dstptable;

	/**
	 * The Dot1dTpFdb table List
	 */
	public Dot1dTpFdbTable m_dot1dtpFdbtable;

	/**
	 * The default constructor. Since this class requires an IP address to
	 * collect SNMP information the default constructor is declared private and
	 * will also throw an exception
	 * 
	 * @throws java.lang.UnsupportedOperationException
	 *             Always thrown.
	 */
	private SnmpVlanCollection() {
		throw new UnsupportedOperationException(
				"default constructor not supported");
	}

	/**
	 * Constructs a new snmp collector for a node using the passed interface as
	 * the collection point. The collection does not occur until the
	 * <code>run</code> method is invoked.
	 * 
	 * @param peer
	 *            The SnmpPeer object to collect from.
	 *  
	 */
	SnmpVlanCollection(SnmpPeer peer) {
		
		m_peer = peer;
		m_address = m_peer.getPeer();
		m_dot1dbase = null;
		m_dot1dbaseTable = null;
		m_dot1dstp = null;
		m_dot1dstptable = null;
		m_dot1dtpFdbtable = null;
	}
	// for debug only
	public static void main(String[] a) throws UnknownHostException {
		//String localhost = "127.0.0.1";
		String localhost = "10.3.2.216";
		InetAddress ip = InetAddress.getByName(localhost);
		SnmpPeer peer = new SnmpPeer(ip);
		SnmpParameters snmpP = peer.getParameters();
		snmpP.setReadCommunity(snmpP.getReadCommunity() + "@1");

		SnmpVlanCollection snmpVlanCollector = new SnmpVlanCollection(peer);
		snmpVlanCollector.run();

		java.util.List macslist = snmpVlanCollector.m_dot1dtpFdbtable.getEntries();
		System.out.println("mac address bridge port entities = "
				+ macslist.size());
		for (int i = 0; i < macslist.size(); i++) {
			Dot1dTpFdbTableEntry mabp = (Dot1dTpFdbTableEntry)macslist.get(i);
			System.out.println("mac address = " + mabp.get(Dot1dTpFdbTableEntry.FDB_ADDRESS)
					+ " bridge port= " + mabp.get(Dot1dTpFdbTableEntry.FDB_PORT) + " bridge port status= "
					+ mabp.get(Dot1dTpFdbTableEntry.FDB_STATUS));
		}

	}

	/**
	 * Returns true if any part of the collection failed.
	 */
	boolean failed() {
		return !hasDot1dBase() || !hasDot1dBasePortTable() || !hasDot1dStp() || !hasDot1dStpPortTable()|| !hasDot1dTpFdbTable();
	}

	/**
	 * Returns true if the dot1DBridge table was collected.
	 */
	boolean hasDot1dBase() {
		return (m_dot1dbase != null && !m_dot1dbase.failed());
	}

	/**
	 * Returns the collected dot1base.
	 */
	Dot1dBaseGroup getDot1dBase() {
		return m_dot1dbase;
	}

	/**
	 * Returns true if the dot1DBridge table was collected.
	 */
	boolean hasDot1dBasePortTable() {
		return (m_dot1dbaseTable != null && !m_dot1dbaseTable.failed());
	}

	/**
	 * Returns the collected dot1dbridge.
	 */
	Dot1dBasePortTable getDot1dBasePortTable() {
		return m_dot1dbaseTable;
	}

	/**
	 * Returns true if the dot1DStp info was collected.
	 */
	boolean hasDot1dStp() {
		return (m_dot1dstp != null && !m_dot1dstp.failed());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dStpGroup getDot1dStp() {
		return m_dot1dstp;
	}

	/**
	 * Returns true if the dot1DStpPortTable info was collected.
	 */
	boolean hasDot1dStpPortTable() {
		return (m_dot1dstptable != null && !m_dot1dstptable.failed());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dStpPortTable getDot1dStpPortTable() {
		return m_dot1dstptable;
	}

	/**
	 * Returns true if the dot1DStpPortTable info was collected.
	 */
	boolean hasDot1dTpFdbTable() {
		return (m_dot1dtpFdbtable != null && !m_dot1dtpFdbtable.failed());
	}

	/**
	 * Returns the collected dot1stp.
	 */
	Dot1dTpFdbTable getDot1dFdbTable() {
		return m_dot1dtpFdbtable;
	}

	/**
	 * Returns the target address that the collection occured for.
	 */

	InetAddress getTarget() {
		return m_address;
	}

	/**
	 * Returns the vlan index.
	 */

	String getVlanIndex() {
		return m_vlan;
	}

	/**
	 * <p>
	 * Performs the collection for the targeted internet address. The success or
	 * failure of the collection should be tested via the <code>failed</code>
	 * method.
	 * </p>
	 * 
	 * <p>
	 * No synchronization is performed, so if this is used in a separate thread
	 * context synchornization must be added.
	 * </p>
	 *  
	 */

	public void run() {
		Category log = ThreadCategory.getInstance(getClass());

		SnmpSession session = null;
		try {
			log
					.debug("SnmpVlanCollection.run: address: "
							+ m_address.getHostAddress()
							+ " Snmp version: "
							+ ((m_peer.getParameters().getVersion() == SnmpSMI.SNMPV1) ? "SNMPv1"
									: "SNMPv2") + " Community: " + m_peer.getParameters().getReadCommunity());
			session = new SnmpSession(m_peer);

			BarrierSignaler signaler = new BarrierSignaler(5);
			synchronized (signaler) {
				m_dot1dbase = new Dot1dBaseGroup(session, signaler);
				m_dot1dbaseTable = new Dot1dBasePortTable(session,
						signaler, m_peer.getParameters().getVersion());
				m_dot1dstp = new Dot1dStpGroup(session, signaler);
				m_dot1dstptable = new Dot1dStpPortTable(session, signaler,
						m_peer.getParameters().getVersion());
				m_dot1dtpFdbtable = new Dot1dTpFdbTable(session, signaler,
						m_peer.getParameters().getVersion());
				try {
					// wait a maximum of five minutes!
					//
					//signaler.wait(300000);
					signaler.wait(300000);
				} catch (InterruptedException e) {
					m_dot1dbase = null;
					m_dot1dbaseTable = null;
					m_dot1dstp = null;
					m_dot1dstptable = null;
					m_dot1dtpFdbtable = null;

					log
							.warn(
									"RunSnmpVlanCollection: collection interrupted, exiting",
									e);
					return;
				}
			}

			// Log any failures
			//
			if (!this.hasDot1dBase())
				log
						.info("RunSnmpVlanCollection: failed to collect Dot1dBase for "
								+ m_address.getHostAddress() + " Community: " + m_peer.getParameters().getReadCommunity());
			if (!this.hasDot1dBasePortTable())
				log
						.info("RunSnmpVlanCollection: failed to collect Dot1dBasePortTable for "
								+ m_address.getHostAddress() + " Community: " + m_peer.getParameters().getReadCommunity());
			if (!this.hasDot1dStp())
				log
						.info("RunSnmpVlanCollection: failed to collect Dot1dStp for "
								+ m_address.getHostAddress() + " Community: " + m_peer.getParameters().getReadCommunity());
			if (!this.hasDot1dStpPortTable())
				log
						.info("RunSnmpVlanCollection: failed to collect Dot1dStpPortTable for "
								+ m_address.getHostAddress() + " Community: " + m_peer.getParameters().getReadCommunity());
			if (!this.hasDot1dTpFdbTable())
				log
						.info("RunSnmpVlanCollection: failed to collect Dot1dTpFdbTable for "
								+ m_address.getHostAddress() + " Community: " + m_peer.getParameters().getReadCommunity());

		} catch (java.net.SocketException e) {
			log.error("Failed to create SNMP session to connect to host "
					+ m_address.getHostAddress(), e);
		} finally {
			if (session != null)
				session.close();
		}

	}

	public boolean isReady() {
		return true;
	}
}