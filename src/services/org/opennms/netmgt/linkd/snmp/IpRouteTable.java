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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.Signaler;
import org.opennms.protocols.snmp.SnmpHandler;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduBulk;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <P>IpRouteTable uses a SnmpSession to collect the ipRouteTable entries
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
public class IpRouteTable
	implements SnmpHandler
{
	/**
	 * The route ifIndex, used to get the number of route entries.
	 */
	private static String	IP_ROUTE_IFINDEX = ".1.3.6.1.2.1.4.21.1.2";
	
	/**
	 * <P>The list of collected IpAddrTableEntries built from
	 * the infomation collected from the remote agent.</P>
	 */
	private List		m_entries;
	
	/**
	 * <P>Flag indicating if query was successful.</P>
	 */
	private boolean		m_error;

	/**
	 * <P>Used to synchronize the class to ensure that the
	 * session has finished collecting data before the
	 * value of success or failure is set, and control
	 * is returned to the caller.</P>
	 */
	private Signaler	m_signal;

	/**
	 * <P>Used to generate the proper command for fetching the
	 * SNMP data from the agent (via GETBULK for version 2 or
	 * GETNEXT for version 1.
	 */
	private int         m_version;

	/**
	 * <P>This will be the OID where the information should cut 
	 * off from the return packet from the GETBULK command.
	 */
	private SnmpObjectId m_stopAt = null;

	/**
	 * <P>This list will hold each instance of the specific MIB 
	 * variable listed within IpRouteTableEntry.  By keeping these
	 * separate, we can generate our own usable map from the
	 * variables.
	 */
	private List 	m_snmpVarBindList;

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
	 * @see #IpRouteTable(SnmpSession, Signaler)
	 *
	 */
	private IpRouteTable( ) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Default constructor not supported!");
	}
	
	/**
	 * <P>Constructs an IpRouteTable object that is used to collect
	 * the address elements from the remote agent. Once all
	 * the elements are collected, or there is an error in
	 * the collection the signaler object is <EM>notified</EM>
	 * to inform other threads.</P>
	 *
	 * @param session	The session with the remote agent.
	 * @param signaler	The object to notify waiters.
	 *
	 * @see IpRouteTableEntry
	 */
	public IpRouteTable(SnmpSession session, Signaler signaler)
	{
		m_signal  = signaler;
		m_entries = new ArrayList();
		m_error = false;
		m_version = SnmpSMI.SNMPV1;
		m_stopAt = IpRouteTableEntry.stop_oid();
		m_snmpVarBindList = new ArrayList();
		
		SnmpPduPacket pdu = IpRouteTableEntry.getNextPdu(m_version);
		
		session.send(pdu, this);
	}
	
	/**
	 * <P>Constructs an IpRouteTable object that is used to collect
	 * the route elements from the remote agent. Once all
	 * the elements are collected, or there is an error in
	 * the collection the signaler object is <EM>notified</EM>
	 * to inform other threads.</P>
	 *
	 * @param session	The session with the remote agent.
	 * @param signaler	The object to notify waiters.
	 *
	 * @see IpRouteTableEntry
	 */
	public IpRouteTable(SnmpSession session, Signaler signaler, int version)
	{
		m_signal  = signaler;
		m_entries = new ArrayList();
		m_error = false;
		m_version = version;
		m_stopAt = IpRouteTableEntry.stop_oid();
		m_snmpVarBindList = new ArrayList();
		
		SnmpPduPacket pdu = IpRouteTableEntry.getNextPdu(version);
		ThreadCategory.getInstance(getClass()).debug("IpRouteTable: initial pdu request id: " + pdu.getRequestId());
		session.send(pdu, this);
	}
	
	/**
	 * <P>Returns the success or failure code for collection
	 * of the data.</P>
	 */
	public boolean failed()
	{
		return m_error;
	}
	
	/**
	 * <P>Returns the list of entry maps
	 * that can be used to access all the
	 * information about the ip routing table.</P>
	 *
	 * @return The list of IpRouteTableEntry maps.
	 */
	public List getEntries()
	{
		return m_entries;
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
	public void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu)
	{	
		boolean doNotify = true;

		Category log = ThreadCategory.getInstance(getClass());

		if(log.isDebugEnabled())
		{
			log.debug("snmpReceivedPdu: got SNMP response, current version: " + ((m_version==SnmpSMI.SNMPV1)?"SNMPv1":"SNMPv2"));
		}

		if(command != SnmpPduPacket.RESPONSE)
		{
			m_error = true;
		}
		else
		{
			//
			// Check for error stored in request pdu
			//
			int errStatus = ((SnmpPduRequest)pdu).getErrorStatus();
			if (errStatus != SnmpPduPacket.ErrNoError)
			{
				m_error = true;
			}
			else
			{
				// The last variable in the list of elements
				// is always the first to run off the table, so 
				// we only need to check that one.
				//
			    
				// GETBULK response handling
				if (m_version == SnmpSMI.SNMPV2) 
				{
					int numVarBinds = pdu.getLength();
					
					for (int x = 0; x < numVarBinds; x++)
					{
						SnmpVarBind vb = pdu.getVarBindAt(x);
						
						if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error)
						{
							m_error = true;
							if (log.isDebugEnabled())
								log.debug("snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");								
							break;
						}
						m_snmpVarBindList.add(vb);
					 }
				
					if (!m_error)
					{
						//in case we did not receive all the data from the 
						//first packet, must generate a new GETBULK packet
						//starting at the OID the previous one left off.
						//
						if (m_stopAt.compare(pdu.getVarBindAt(numVarBinds-1).getName()) > 0) 
						{
							SnmpObjectId id = new SnmpObjectId(pdu.getVarBindAt(numVarBinds-1).getName());
							SnmpVarBind[] newvblist = { new SnmpVarBind(id) };

							SnmpPduPacket nxt = new SnmpPduBulk(0, 10, newvblist);
							nxt.setRequestId(SnmpPduPacket.nextSequence());
							session.send(nxt, this);
							doNotify = false;
						}				
						else 
						{
							// Convert SNMP variable binding list to an array for processing
							//
							SnmpVarBind[] tempStorage = new SnmpVarBind[m_snmpVarBindList.size()];
							tempStorage = (SnmpVarBind[])m_snmpVarBindList.toArray(tempStorage);
						
							//since the MIB does not store the number of mac addresses stored
							//, the method must resort to an alternative.  By 
							//counting the number of values found for the ipRouteIfIndex variable,
							//we'll have the number of entries in.  Each route entry
							//will have a value for each MIB variable listed
							//
							int numAddresses = 0;
							SnmpObjectId AddrIndex = new SnmpObjectId(IP_ROUTE_IFINDEX);
					    
							while (AddrIndex.compare(tempStorage[numAddresses].getName()) > 0) 
								numAddresses++;
					
							//store the Ip Route Table data for each route dest into a map.
							//
							int numEntries = IpRouteTableEntry.getElementListSize();
							for (int addr_index = 0; addr_index < numAddresses; addr_index++) 
							{
								SnmpVarBind[] vblist = new SnmpVarBind[numEntries];
								for (int vb_index = 0;  vb_index < numEntries; vb_index++)
								{
									vblist[vb_index] = tempStorage[addr_index + (vb_index*numAddresses)];
								}
								IpRouteTableEntry ent = new IpRouteTableEntry(vblist);
								m_entries.add(ent);
							}
						}
					} 
				} 
				else if (m_version == SnmpSMI.SNMPV1)// GETNEXT response handling 
				{	
					// Create a new map of the interface entry
					//
					// The last varbind will be the first one to walk off the end 
					// of the ipAddrTable. So verify that the last entry in the 
					// received pdu is still within the scope of the IpAddrTable...
					// if it is then create a new entry.
					//
					if (IpRouteTableEntry.ROOT.isRootOf(pdu.getVarBindAt(pdu.getLength()-1).getName()))
					{
						if(log.isDebugEnabled())
						log.debug("snmpReceivedPdu: got SNMPv1 response and still within IpAddrTable, creating new entry.");
						SnmpVarBind [] vblist = pdu.toVarBindArray();
						IpRouteTableEntry ent = new IpRouteTableEntry(vblist);
						m_entries.add(ent);
				    
						// next pdu
						//
						SnmpPduRequest nxt = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
						for(int x = 0; x < pdu.getLength(); x++)
						{
							nxt.addVarBind(new SnmpVarBind(pdu.getVarBindAt(x).getName()));
						}
					
						nxt.setRequestId(SnmpPduPacket.nextSequence());
						session.send(nxt, this);
						doNotify = false;
					}
				}
			}
		}
		
		if(doNotify)
		{
			synchronized(this)
			{
				notifyAll();
			}
			if(m_signal != null)
			{
				synchronized(m_signal)
				{
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
	public void snmpInternalError(SnmpSession session, int error, SnmpSyntax pdu)
	{
		Category log = ThreadCategory.getInstance(getClass());
		if (log.isDebugEnabled())
		{
			log.debug("snmpInternalError: " + error + " for: " + session.getPeer().getPeer());
		}

		m_error = true;

		synchronized(this)
		{
			notifyAll();
		}
		if(m_signal != null)
		{
			synchronized(m_signal)
			{
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
	public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu)
	{
		Category log = ThreadCategory.getInstance(getClass());
		if (log.isDebugEnabled())
		{
			log.debug("snmpTimeoutError for: " + session.getPeer().getPeer());
		}

		m_error = true;

		synchronized(this)
		{
			notifyAll();
		}
		if(m_signal != null)
		{
			synchronized(m_signal)
			{
				m_signal.signalAll();
			}
		}
	}
	
	
}
				



