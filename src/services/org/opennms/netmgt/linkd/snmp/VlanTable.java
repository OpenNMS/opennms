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
// 2003 Sep 29: Modifications to allow for OpenNMS to handle duplicate IP Addresses.
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
//

package org.opennms.netmgt.linkd.snmp;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.Signaler;
import org.opennms.protocols.snmp.SnmpHandler;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <P>VlanTable uses a SnmpSession to collect the Vlan IDs
 * It implements the SnmpHandler to receive notifications when a reply is 
 * received/error occurs in the SnmpSession used to send requests /recieve 
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo</A>
 * @author <A HREF="mailto:jamesz@opennms.org">James Zuo</A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@oculan.com">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public class VlanTable implements SnmpHandler {

	
	public final static	String	VLAN_INDEX		= "vtpVlanIndex";

	public final static	String	VLAN_NAME		= "vtpVlanName";
	
	/**
	 * <P>The String representing the vlanoid from which get vlans ID.</P>
	 */
	private String m_vlanoid;

	/**
	 * <P>The list of collected VlanIDS built from
	 * the infomation collected from the remote agent.</P>
	 */
	private List m_entries;

	/**
	 * <P>Flag indicating if query was successful.</P>
	 */
	private boolean m_error;

	/**
	 * <P>Used to synchronize the class to ensure that the
	 * session has finished collecting data before the
	 * value of success or failure is set, and control
	 * is returned to the caller.</P>
	 */
	private Signaler m_signal;

	/**
	 * <P>Used to generate the proper command for fetching the
	 * SNMP data from the agent (via GETBULK for version 2 or
	 * GETNEXT for version 1.
	 */
	private int m_version;

	/**
	 * <P>The default constructor is marked as private and will
	 * always throw an exception. This is done to disallow the
	 * constructor to be called. Since any instance of this class
	 * must have an SnmpSession and Signaler to properly work, the
	 * correct constructor must be used.</P>
	 *
	 * @exception java.lang.UnsupportedOperationException Always thrown from
	 *	this method since it is not supported.
	 *
	 * @see #VlanTable(SnmpSession, Signaler)
	 *
	 */
	private VlanTable() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Default constructor not supported!");
	}

	/**
	 * <P>Constructs a VlanTable object that is used to collect
	 * the address elements from the remote agent. Once all
	 * the elements are collected, or there is an error in
	 * the collection the signaler object is <EM>notified</EM>
	 * to inform other threads.</P>
	 *
	 * @param session	The session with the remote agent.
	 * @param signaler	The object to notify waiters.
	 * @param vlanoid   The oid to get vlans IDs.
	 *
	 * @see VlanTableEntry
	 */
	public VlanTable(SnmpSession session, Signaler signaler, String vlanoid) {
		m_signal = signaler;
		m_vlanoid = vlanoid;
		m_entries = new ArrayList();
		m_error = false;
		m_version = SnmpSMI.SNMPV1;

		SnmpPduPacket pdu = getNextPdu(m_vlanoid);
		
		session.send(pdu, this);
	}

	/**
	 * <P>Returns the success or failure code for collection
	 * of the data.</P>
	 */
	public boolean failed() {
		return m_error;
	}

	/**
	 * <P>Returns the list of entry maps
	 * that can be used to access all the
	 * information about the ip routing table.</P>
	 *
	 * @return The list of VlanTableEntry maps.
	 */
	public List getEntries() {
		return m_entries;
	}

	/**
	 * <P>This class support SNMP version V1, this method is used to get a 
	 * generic SNMP GETNEXT PDU that contains one varbind per member 
	 * element.</P>
	 *
	 * <P>The PDU can then be used to perform an <EM>SNMP walk</EM> of 
	 * the Vlan Ids of a remote host.</P>
	 * 
	 * @param vlanoid	the OID of the vlan table
	 * 
	 * @return An SnmpPduPacket object with a command of GETNEXT (for SNMPv1).
	 *
	 */
	public static SnmpPduPacket getNextPdu(String vlanoid) {
		/**
		 * <P>Initialize the element list for the class. This
		 * is class wide data, but will be used by each instance.</P>
		 */
		SnmpPduPacket pdu = null;

		pdu = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
		pdu.setRequestId(SnmpPduPacket.nextSequence());
		SnmpObjectId oid = new SnmpObjectId(vlanoid);
		pdu.addVarBind(new SnmpVarBind(oid));

		return pdu;
	}

	/**
	 * <P>This method is used to process received SNMP PDU packets from
	 * the remote agent. The method is part of the SnmpHandler interface
	 * and will be invoked when a PDU is successfully decoded. The method
	 * is passed the receiving session, the PDU command, and the actual
	 * PDU packet.</P>
	 *
	 * <P>When all the data has been received from the session the signaler
	 * object, initialized in the constructor, is signaled. In addition,
	 * the receiving instance will call notifyAll() on itself at the same
	 * time.</P>
	 *
	 * @param session	The SNMP Session that received the PDU
	 * @param command	The command contained in the received pdu
	 * @param pdu		The actual received PDU.
	 *
	 */
	public void snmpReceivedPdu(SnmpSession session, int command,
			SnmpPduPacket pdu) {
		boolean doNotify = true;

		Category log = ThreadCategory.getInstance(getClass());

		if (log.isDebugEnabled()) {
			log.debug("snmpReceivedPdu: got SNMP response, current version: "
					+ ((m_version == SnmpSMI.SNMPV1) ? "SNMPv1" : "SNMPv2"));
		}

		if (command != SnmpPduPacket.RESPONSE) {
			m_error = true;
		} else {
			//
			// Check for error stored in request pdu
			//
			int errStatus = ((SnmpPduRequest) pdu).getErrorStatus();
			if (errStatus != SnmpPduPacket.ErrNoError) {
				m_error = true;
			} else {
				if (m_version == SnmpSMI.SNMPV1)// GETNEXT response handling 
				{
					// Create a new map of the interface entry
					//
					// The last varbind will be the first one to walk off the end 
					// of the VLAn Table. So verify that the last entry in the 
					// received pdu is still within the scope of the Vlan Table...
					// if it is then create a new entry.
					//
					SnmpObjectId ROOT = new SnmpObjectId(m_vlanoid);

					if (ROOT.isRootOf(pdu.getVarBindAt(pdu.getLength() - 1)
							.getName())) {
						if (log.isDebugEnabled())
							log
									.debug("snmpReceivedPdu: got SNMPv1 response and still within Vlan Table, creating new entry.");
						SnmpVarBind[] vblist = pdu.toVarBindArray();
						for (int i=0; i<vblist.length;i++){
							TreeMap vlanEnt = new TreeMap();
							String index = vblist[i].getName().toString().replaceAll(m_vlanoid+".","");
							if (log.isDebugEnabled()) {
								log.debug("VlanTable: get SNMP Alias: "
										+ vblist[i].getName()
										+ "  Vars[y]: " + vblist[i].toString() + "; ifindex: " + index);
							}
							vlanEnt.put(VLAN_INDEX,index);
							vlanEnt.put(VLAN_NAME,vblist[i].getValue());
							m_entries.add(vlanEnt);
						}

						// next pdu
						//
						SnmpPduRequest nxt = new SnmpPduRequest(
								SnmpPduPacket.GETNEXT);
						for (int x = 0; x < pdu.getLength(); x++) {
							nxt.addVarBind(new SnmpVarBind(pdu.getVarBindAt(x)
									.getName()));
						}
						nxt.setRequestId(SnmpPduPacket.nextSequence());
						session.send(nxt, this);
						doNotify = false;
					}
				}
			}
		}

		if (doNotify) {
			synchronized (this) {
				notifyAll();
			}
			if (m_signal != null) {
				synchronized (m_signal) {
					m_signal.signalAll();
				}
			}
		}
	}

	/**
	 * <P>This method is part of the SnmpHandler interface and called when
	 * an internal error happens in a session. This is usually the result
	 * of an I/O error. This method will not be called if the session times
	 * out sending a packet, see snmpTimeoutError for timeout handling.</P>
	 *
	 * @param session	The session that had an unexpected error
	 * @param error		The error condition
	 * @param pdu		The PDU being sent when the error occured
	 *
	 * @see #snmpTimeoutError
	 * @see org.opennms.protocols.snmp.SnmpHandler SnmpHandler
	 */
	public void snmpInternalError(SnmpSession session, int error, SnmpSyntax pdu) {
		Category log = ThreadCategory.getInstance(getClass());
		if (log.isDebugEnabled()) {
			log.debug("snmpInternalError: " + error + " for: "
					+ session.getPeer().getPeer());
		}

		m_error = true;

		synchronized (this) {
			notifyAll();
		}
		if (m_signal != null) {
			synchronized (m_signal) {
				m_signal.signalAll();
			}
		}
	}

	/**
	 * <P>This method is part of the SnmpHandler interface and is invoked
	 * when the SnmpSession does not receive a reply after exhausting 
	 * the retransmission attempts.</P>
	 *
	 * @param session	The session invoking the error handler
	 * @param pdu		The PDU that the remote failed to respond to.
	 *
	 * @see org.opennms.protocols.snmp.SnmpHandler SnmpHandler
	 *
	 */
	public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu) {
		Category log = ThreadCategory.getInstance(getClass());
		if (log.isDebugEnabled()) {
			log.debug("snmpTimeoutError for: " + session.getPeer().getPeer());
		}

		m_error = true;

		synchronized (this) {
			notifyAll();
		}
		if (m_signal != null) {
			synchronized (m_signal) {
				m_signal.signalAll();
			}
		}
	}

}

