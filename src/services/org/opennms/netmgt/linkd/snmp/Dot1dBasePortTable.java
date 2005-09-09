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
 * <P>Dot1DBasePortTable uses a SnmpSession to collect the dot1dBridge.dot1dBase.
 * Port table entries.
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
public class Dot1dBasePortTable
	implements SnmpHandler
{


	/**
	 * The bridge port oid for ifIndex, used to get the number of port entries.
	 */
	private static String	BASE_PORT_IFINDEX = ".1.3.6.1.2.1.17.1.4.1.2";

	/**
	 * <P>The list of collected dot1dBaseTableEntries built from
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
	 * variable listed within Dot1dBaseTableEntry.  By keeping these
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
	 * @see #Dot1dBasePortTable(SnmpSession, Signaler)
	 *
	 */
	private Dot1dBasePortTable( ) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Default constructor not supported!");
	}
	
	/**
	 * <P>Constructs an Dot1dBasePortTable object that is used to collect
	 * the bridge port elements from the remote agent. Once all
	 * the elements are collected, or there is an error in
	 * the collection the signaler object is <EM>notified</EM>
	 * to inform other threads.</P>
	 *
	 * @param session	The session with the remote agent.
	 * @param signaler	The object to notify waiters.
	 *
	 * @see Dot1dBasePortTableEntry
	 */
	public Dot1dBasePortTable(SnmpSession session, Signaler signaler)
	{
		m_signal  = signaler;
		m_entries = new ArrayList();
		m_error = false;
		m_version = SnmpSMI.SNMPV1;
		m_stopAt = Dot1dBasePortTableEntry.stop_oid();
		m_snmpVarBindList = new ArrayList();
		
		SnmpPduPacket pdu = Dot1dBasePortTableEntry.getNextPdu(m_version);
		
		session.send(pdu, this);
	}
	
	/**
	 * <P>Constructs an Dot1dBasePortTable object that is used to collect
	 * the bridge port elements from the remote agent. Once all
	 * the elements are collected, or there is an error in
	 * the collection the signaler object is <EM>notified</EM>
	 * to inform other threads.</P>
	 *
	 * @param session	The session with the remote agent.
	 * @param signaler	The object to notify waiters.
	 * @param version   The SNMP version 
	 *
	 * @see Dot1dBasePortTableEntry
	 */
	public Dot1dBasePortTable(SnmpSession session, Signaler signaler, int version)
	{
		m_signal  = signaler;
		m_entries = new ArrayList();
		m_error = false;
		m_version = version;
		m_stopAt = Dot1dBasePortTableEntry.stop_oid();
		m_snmpVarBindList = new ArrayList();
		
		SnmpPduPacket pdu = Dot1dBasePortTableEntry.getNextPdu(version);
		ThreadCategory.getInstance(getClass()).debug("Dot1dBasePortTable: initial pdu request id: " + pdu.getRequestId());
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
	 * information about the Bridge Port table.</P>
	 *
	 * @return The list of Dot1dBridgePortTableEntry maps.
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
						    
							int numBridgePorts = 0;
							SnmpObjectId AddrIndex = new SnmpObjectId(BASE_PORT_IFINDEX);
					    
							while (AddrIndex.compare(tempStorage[numBridgePorts].getName()) > 0) 
								numBridgePorts++;

							//store the dot1Dbase Port Table data for each port into a map.
							//
							int numEntries = Dot1dBasePortTableEntry.getElementListSize();

							for (int addr_index = 0; addr_index < numBridgePorts; addr_index++) 
							{
								SnmpVarBind[] vblist = new SnmpVarBind[numEntries];
								for (int vb_index = 0;  vb_index < numEntries; vb_index++)
								{
									vblist[vb_index] = tempStorage[addr_index + (vb_index*numBridgePorts)];
								}
								Dot1dBasePortTableEntry ent = new Dot1dBasePortTableEntry(vblist);
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
					// of the Bridge Port Table. So verify that the last entry in the 
					// received pdu is still within the scope of the BridgePortTable Entry...
					// if it is then create a new entry.
					//
					if (Dot1dBasePortTableEntry.ROOT.isRootOf(pdu.getVarBindAt(pdu.getLength()-1).getName()))
					{
						if(log.isDebugEnabled())
						log.debug("snmpReceivedPdu: got SNMPv1 response and still within Dot1dBasePortTable, creating new entry.");
						SnmpVarBind [] vblist = pdu.toVarBindArray();
						Dot1dBasePortTableEntry ent = new Dot1dBasePortTableEntry(vblist);
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
				



