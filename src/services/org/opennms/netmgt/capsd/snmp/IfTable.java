//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
//
// Tab Size = 8
//
//
package org.opennms.netmgt.capsd.snmp;

import java.lang.*;
import java.util.*;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.protocols.snmp.*;
import org.opennms.netmgt.utils.Signaler;

/**
 * <P>The IfTable uses a SnmpSession to collect the entries in the remote agent's
 * interface table. It implements the SnmpHandler to receive notifications
 * and handle errors associated with the data collection. Data is collected
 * using a series of GETNEXT PDU request to walk multiple parts of the interface
 * table at once. The number of SNMP packets should not exceed the number of 
 * interface + 1, assuming no lost packets or error conditions occur.</P>
 *
 * <p><em>Addition by Jon Whetzel</em></p>
 * <p>IfTable has an extra class variable for the SNMP version setting.
 * If this is set for SNMPv2, then a GETBULK command will be used for
 * retrieving the necessary data.  Otherwise, the method will resort
 * to its previous implementation with GETNEXT commands.</p>

 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="mailto:weave@opennms.org">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public final class IfTable
	implements SnmpHandler
{
	/**
	 * Used to convert decimal to hex
	 */
	private static final char[] m_hexDigit = { '0', '1', '2', '3', '4', 
						   '5', '6', '7', '8', '9', 
						   'A', 'B', 'C', 'D', 'E', 'F' };
	
	/**
	 * <P>The list of interfaces from the remote's interface table.
	 * The list contains a set of IfTableEntry objects that were
	 * collected from the remote host.</P>
	 *
	 * @see IfTableEntry
	 */
	private List		m_entries;

	/**
	 * <P>Flag indicating if query was successful
	 * or if the collection failed.</P>
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
	 * GETNEXT for version 1.</P>
	 */
	private int             m_version;

	/** 
	 * <P>The request id associated with the GetNext PDU generated
	 * to retrieve the number of interfaces associated with the
	 * remote host.</P>
	 */
	private int 		m_ifNumberRequestId;
	
	/**
	 * <P>This will be the OID where the information should cut 
	 * off from the return packet from the GETBULK command.</P>
	 */
	private SnmpObjectId 	m_stopAt = null;

       /**
	*<P>Used for storing the ifNumber variable from the MIB, 
	*the number of interfaces a device possesses.</P>
	*/
	private int 		m_ifNumber;
       
       /**
	*<P>Used as a temporary storage space for all the data
	*collected from the SNMP response packets for SNMP v2.
	*After receiving all the data, the information will
	*be sorted so that it mimics the SNMP v1 data storage;
	*one map per interface containing all the necessary MIB
	*values.</P>
	*/
	//private SnmpVarBind[] m_tempStorage = new SnmpVarBind[20000];
	private SnmpVarBind[] m_tempStorage = null;

       /**
	*<P>For SNMPv1 used to keep track of the number of SNMP response
	* packets received.  
	* 
	* For SNMPv2 used to keep track of the total number of varbinds
	* received in SNMP response packets.
	* 
	* For both SNMPv1 and SNMPv2 this value is used to determine
	* when all the necessary SNMP data has been retrieved.</P>
	*/
	private int m_responses = 0;

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
	 * @see #IfTable(SnmpSession, Signaler)
	 *
	 */
	private IfTable( ) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Default constructor not supported!");
	}

	/**
	 * <P>Constructs an IfTable object that is used to collect
	 * the interface elements from the remote agent. Once all
	 * the interfaces are collected, or there is an error in
	 * the collection the signaler object is <EM>notified</EM>
	 * to inform other threads.</P>
	 *
	 * @param session	The session with the remote agent.
	 * @param signaler	The object to notify waiters.
	 *
	 * @see IfTableEntry
	 */
	public IfTable(SnmpSession session, Signaler signaler)
	{
		m_signal  = signaler;
		m_entries = new ArrayList(2); // not synchronized.
		m_error   = false;
		
		m_version = SnmpSMI.SNMPV1;
		m_stopAt = IfTableEntry.stop_oid();
		
		//first process, attain ifNumber.
		SnmpPduRequest pdu = IfTableEntry.getIfNumberPdu();
		m_ifNumberRequestId = pdu.getRequestId();
		
		session.send(pdu, this);
	}

	/**
	 * <P>Constructs an IfTable object that is used to collect
	 * the interface elements from the remote agent. Once all
	 * the interfaces are collected, or there is an error in
	 * the collection the signaler object is <EM>notified</EM>
	 * to inform other threads.</P>
	 *
	 * @param session	The session with the remote agent.
	 * @param signaler	The object to notify waiters.
	 *
	 * @see IfTableEntry
	 */
	public IfTable(SnmpSession session, Signaler signaler, int version)
	{
		m_signal  = signaler;
		m_entries = new ArrayList(2); // not synchronized.
		m_error   = false;
		
		m_version = version;
		m_stopAt = IfTableEntry.stop_oid();
		
		//first process, attain ifNumber.
		SnmpPduRequest pdu = IfTableEntry.getIfNumberPdu();
		m_ifNumberRequestId = pdu.getRequestId();
		Category log = ThreadCategory.getInstance(getClass());
		if(log.isDebugEnabled())
		{
			log.debug("<ctor>: ifNumber retrieval pdu request id: " + m_ifNumberRequestId);
		}

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
	 * information about the interface table.</P>
	 *
	 * @return The list of ifTableEntry maps.
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
	 * <P>For SNMP version 2 devices, all the received data enters a 
	 * temporary array.  After the collecting process, the method
	 * sorts the data so that each interface has its own map.</P>
	 *
	 * @param session	The SNMP Session that received the PDU
	 * @param command	The command contained in the received pdu
	 * @param pdu		The actual received PDU.
	 *
	 */
	public void snmpReceivedPdu(SnmpSession session, int command, SnmpPduPacket pdu)
	{
		boolean doNotify = true;

		// lookup the category
		//
		Category log = ThreadCategory.getInstance(getClass());
		
		if(log.isDebugEnabled())
		{
			log.debug("snmpReceivedPdu: got SNMP response, current version: " + ((m_version==SnmpSMI.SNMPV1)?"SNMPv1":"SNMPv2"));
		}
			
		// handle the command.
		//
		if (command != SnmpPduPacket.RESPONSE)
		{
			m_error = true;
		}
		else
		{
			// Check for error stored in request pdu
			//
			int errStatus = ((SnmpPduRequest)pdu).getErrorStatus();
			if (errStatus != SnmpPduPacket.ErrNoError)
			{
				m_error = true;
			} 
			else
			{
				// Is this the response to our request to retrieve ifNumber?
				// If so, begin gathering all the MIB data for the device
				//
				if (pdu.getRequestId() ==  m_ifNumberRequestId) 
				{
					SnmpVarBind vb = pdu.getVarBindAt(0);
					if (m_version == SnmpSMI.SNMPV2)
					{
						// Check for v2 error in varbind
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPdu: checking for v2 error in response pdu varbind");
						if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error)
						{
							m_error = true;
							if (log.isDebugEnabled())
								log.debug("snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");								
						}
					}
						
					if (!m_error)
					{
						SnmpInt32 temp = (SnmpInt32)vb.getValue();
						m_ifNumber = temp.getValue();
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPdu: got response to ifNumber request: " + m_ifNumber);
					
						// 
						// Now that we know the number of interfaces we can can allocate
						// the temp storage to hold all the response variable bindings
						// 
						m_tempStorage = new SnmpVarBind[m_ifNumber * IfTableEntry.getElementListSize()];
						SnmpPduPacket nxt = null;
						if (m_version == SnmpSMI.SNMPV2)
						{
							nxt = IfTableEntry.getBulkPdu(m_ifNumber);
						}
						else
						{
							nxt = IfTableEntry.getNextPdu();
						}
					
						session.send(nxt, this);
						doNotify = false;
					}
				}
				else if (m_version == SnmpSMI.SNMPV2) // Handle SNMPv2 GetBulk responses...
				{
					if (log.isDebugEnabled())
					{
						log.debug("snmpReceivedPdu: got SNMPv2 GetBulk response...");
					}
					
					int numVarBinds = pdu.getLength();
					
					// Allocate it here to allow garbage collection!
					//
					for (int y = 0; y < numVarBinds; y++) 
					{
						// Check for v2 error in each returned varbind
						SnmpVarBind vb = pdu.getVarBindAt(y);
						
						if (vb.getValue() instanceof org.opennms.protocols.snmp.SnmpV2Error)
						{
							m_error = true;							
							if (log.isDebugEnabled())
								log.debug("snmpReceivedPDU: varbind: " + vb.getName() + "  error: '" + vb.getValue() + "'");								
							break;
						}
						
						m_tempStorage[m_responses] = vb;
						m_responses++;
					}
					
					if (!m_error)
					{
						//in case we did not receive all the data from the 
						//first packet, must generate a new GETBULK packet
						//starting at the OID the previous one left off.
						//
					
						// Calculate maxRepetitions for next GETBULK packet
						int maxReps = (m_ifNumber*IfTableEntry.getElementListSize()) - m_responses;
						if (log.isDebugEnabled())
							log.debug("snmpReceivedPdu: calculated number of maxRepetitions = " + maxReps);
					
						if (maxReps > 0 && m_stopAt.compare(pdu.getVarBindAt(numVarBinds-1).getName()) > 0) 
						{
							SnmpObjectId id = new SnmpObjectId(pdu.getVarBindAt(numVarBinds-1).getName());
							SnmpVarBind[] newvblist = {new SnmpVarBind(id)};
							SnmpPduPacket nxt = new SnmpPduBulk(0, maxReps, newvblist);
							nxt.setRequestId(nxt.nextSequence());
							if (log.isDebugEnabled())
								log.debug("smnpReceivedPDU: Starting new GETBULK packet at OID = " + id.toString() + ", with request ID: " + nxt.getRequestId());
							session.send(nxt, this);
							doNotify = false;
						}
						else 
						{
							if (log.isDebugEnabled())
								log.debug("smnpReceivedPDU: All SNMPv2 data received, processing...");
							
							//all the data has been retrieved from the MIB, so now
							//we must enter it into our maps.  Each map will hold all
							//the MIB variable values per interface.
							//
							//get the next possible index value from the temporary storage
							//array, since the first variable is the ifIndex value.  After
							//scan through the entire temporary array, comparing the
							//index of each OID to the index stored as 'ifIndex'.
							for (int x = 0; x < m_ifNumber; x++) 
							{
								SnmpVarBind[] templist = new SnmpVarBind[22];
								SnmpInt32 ifIndex = (SnmpInt32)m_tempStorage[x].getValue();
						
								//parse each oid to get index
								int tempcount = 0;
								
								for (int j = 0; j < m_responses && tempcount<22; j++) 
								{
									// Extract the "instance" id from the current SnmpVarBind's object id
									//
									String from_oid = m_tempStorage[j].getName().toString();
									SnmpObjectId id = new SnmpObjectId(from_oid);
									int[] ids = id.getIdentifiers();
									int instance_id = ids[ids.length-1];
									String temp_index = Integer.toString(instance_id);
								
									try 
									{
										Integer check = Integer.valueOf(temp_index);
										
										//if the indexes match, store it within templist
										if (check.intValue() == ifIndex.getValue()) 
										{
											templist[tempcount++] = m_tempStorage[j];
										}
									}
									catch (NumberFormatException nfE)
									{
										log.warn("snmpReceivedPdu: unable to convert last decimal of object identifier '" +
												m_tempStorage[j].getName().toString() + 
												"' to integer for ifIndex comparison.", nfE);
									}
								}
					
								//create VarBind list from templist.  
								SnmpVarBind[] vblist = new SnmpVarBind[tempcount];
								for (int a = 0; a < tempcount; a++) 
								{
									vblist[a] = templist[a];    
								}
						
								//create new IfTableEntry with all variables for a 
								//particular index.
								IfTableEntry ent = new IfTableEntry(vblist);
								m_entries.add(ent);
							} // end for()
						}
					} // end if (!m_error)
				} // end if SNMPv2
				else if (m_version == SnmpSMI.SNMPV1) // Handle SNMPv1 GetNext responses
				{
					if(log.isDebugEnabled())
						log.debug("snmpReceivedPdu: got SNMPv1 GetNext response...");
					
					//if the response count is less than the number of interfaces, continue to
					//store info and generate packets for gathering data.
					if (m_responses < m_ifNumber) 
					{
						SnmpVarBind[] vblist = pdu.toVarBindArray();
						IfTableEntry ent = new IfTableEntry(vblist);
						m_entries.add(ent);
				    
						SnmpPduRequest nxt = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
						for(int x = 0; x < pdu.getLength(); x++)
						{
							nxt.addVarBind(new SnmpVarBind(pdu.getVarBindAt(x).getName()));
						}
						nxt.setRequestId(nxt.nextSequence());
						session.send(nxt, this);
						doNotify = false;
						m_responses++;
					}
				} // end if (m_version == SnmpSMI.SNMPV1)

			} // end if (errStatus != SnmpPduPacket.ErrNoError)

		} // end if (command != SnmpPduPacket.RESPONSE) 
		
		//
		// call the notifyAll() method on self, and 
		// the signalAll() method on the signaler
		//
		if(doNotify)
		{
			// release the storage since we are not going
			// to be using it any further.
			//
			m_tempStorage = null;
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
		if(log.isDebugEnabled())
		{
			log.debug("snmpInternal error: " + error + " for: " + session.getPeer().getPeer());
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
	
	/**
	 * <P>This method converts the physical address, normally
	 * six bytes, into a hexidecimal string. The string is not
	 * prefixed with the traditional <EM>"0x"</EM>, but is the
	 * raw hexidecimal string in upper case.</P>
	 *
	 * <P><EM>NOTICE</EM>: The string is converted based on
	 * the starndard US-ASCII table. Each NIBBLE is converted
	 * to an integer and added to the character '0' (Zero).</P>
	 *
	 * @param physAddr	The physical address to convert to a string.
	 *
	 * @return The converted physical address as a hexidecimal string.
	 *
	 */
	public static String toHexString(byte[] physAddr)
	{
		//
		// Check to make sure that there 
		// is enough data.
		//
		if (physAddr == null || physAddr.length == 0)
		{
			return null;
		}

		//
		// Convert the actual data
		//
		StringBuffer buf = new StringBuffer(12);
		for(int i = 0; i < physAddr.length; i++)
		{
			int b = (int)physAddr[i];
			buf.append(m_hexDigit[(b >> 4) & 0xf]);	// based upon US-ASCII
			buf.append(m_hexDigit[(b & 0xf)]);		// based upon US-ASCII
		}
		return buf.toString().toUpperCase();
	}
}

