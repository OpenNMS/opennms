//
// Copyright (C) 2002-2003 Sortova Consulting Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.trapd;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.protocols.snmp.*;
import org.opennms.protocols.ip.IPv4Address;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;

import org.opennms.netmgt.eventd.EventIpcManagerFactory;

// castor generated
import org.opennms.netmgt.xml.event.*;

/**
 * The TrapQueueProcessor handles the conversion of V1 and V2 traps to
 * events and sending them out the JSDT channel that eventd is listening on
 *
 * @author	<A HREF="mailto:weave@opennms.org">Brian Weaver</A>
 * @author 	<A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author 	<A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author 	<A HREF="mailto:mike@opennms.org">Mike Davidson</A> 
 * @author 	<A HREF="mailto:tarus@opennms.org">Tarus Balog</A> 
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 *
 */
class TrapQueueProcessor 
	implements Runnable, PausableFiber
{
	/**
	 * The sysUpTimeOID, which should be the first varbind in a V2 trap
	 */
	private static final String SNMP_SYSUPTIME_OID=".1.3.6.1.2.1.1.3.0";

	/**
	 * The sysUpTimeOID, which should be the first varbind in a V2 trap, 
	 * but in the case of Extreme Networks only mostly 
	 */
	private static final String EXTREME_SNMP_SYSUPTIME_OID=".1.3.6.1.2.1.1.3";

	/**
	 * The snmpTrapOID, which should be the second varbind in a V2 trap
	 */
	private static final String SNMP_TRAP_OID=".1.3.6.1.6.3.1.1.4.1.0";

	/**
	 * The snmp trap enterprise OID, which if present in a V2 trap
	 * is the last varbind
	 *
	 * ref - book 'SNMP, SNMPv2, SNMPv3..'
	 *       by William Stallings, third edition, section 13.1.3
	 */
	private static final String SNMP_TRAP_ENTERPRISE_ID=".1.3.6.1.6.3.1.1.4.3.0";

	/**
	 * The snmpTraps value to be used in case a standard trap comes in
	 * without the SNMP_TRAP_ENTERPRISE_ID as the last varbind
	 */
	private static final String SNMP_TRAPS=".1.3.6.1.6.3.1.1.5";

	/**
	 * The standard traps list
	 */
	private static final ArrayList GENERIC_TRAPS;

	/**
	 * The snmp trap OID is the second varbind
	 */
	private static final int SNMP_TRAP_OID_INDEX	= 1;

	/**
	 * The dot separator in an OID
	 */
	private static final char DOT_CHAR		= '.';

	/**
	 * The input queue
	 */
	private FifoQueue	m_backlogQ;

	/**
	 * The name of the local host.
	 */
	private String 		m_localAddr;

	/**
	 * Current status of the fiber
	 */
	private int		m_status;
	
	/**
	 * The thread that is executing the <code>run</code>
	 * method on behalf of the fiber.
	 */
	private Thread		m_worker;
	
	/**
	 * Whether or not a newSuspect event should be
	 * generated with a trap from an unknown IP address
	 */
	private boolean		m_newSuspect;
 

	/**
	 * Create the standard traps list - used in v2 processing
	 */
	static
	{
		GENERIC_TRAPS = new ArrayList();
		GENERIC_TRAPS.add(new SnmpObjectId("1.3.6.1.6.3.1.1.5.1"));	// coldStart
		GENERIC_TRAPS.add(new SnmpObjectId("1.3.6.1.6.3.1.1.5.2"));	// warmStart
		GENERIC_TRAPS.add(new SnmpObjectId("1.3.6.1.6.3.1.1.5.3"));	// linkDown
		GENERIC_TRAPS.add(new SnmpObjectId("1.3.6.1.6.3.1.1.5.4"));	// linkUp
		GENERIC_TRAPS.add(new SnmpObjectId("1.3.6.1.6.3.1.1.5.5"));	// authenticationFailure
		GENERIC_TRAPS.add(new SnmpObjectId("1.3.6.1.6.3.1.1.5.6"));	// egpNeighborLoss
	}

	/**
	 * <p>Process a V2 trap and convert it to an event for transmission</p>
	 * 
	 * <p>From RFC2089 ('Mapping SNMPv2 onto SNMPv1'), section 3.3 
	 * ('Processing an outgoing SNMPv2 TRAP')</p>
	 *
	 * <p><strong>2b</strong>
	 * <p>If the snmpTrapOID.0 value is one of the standard traps the
	 * specific-trap field is set to zero and the generic trap
	 * field is set according to this mapping:<p>
	 *
	 * <pre>
	 *      value of snmpTrapOID.0                generic-trap
	 *      ===============================       ============
	 *      1.3.6.1.6.3.1.1.5.1 (coldStart)                  0
	 *      1.3.6.1.6.3.1.1.5.2 (warmStart)                  1
	 *      1.3.6.1.6.3.1.1.5.3 (linkDown)                   2
	 *      1.3.6.1.6.3.1.1.5.4 (linkUp)                     3
	 *      1.3.6.1.6.3.1.1.5.5 (authenticationFailure)      4
	 *      1.3.6.1.6.3.1.1.5.6 (egpNeighborLoss)            5
	 * </pre>
	 *
	 * <p>The enterprise field is set to the value of
	 *  snmpTrapEnterprise.0 if this varBind is present, otherwise
	 *  it is set to the value snmpTraps as defined in RFC1907 [4].</p>
	 *
	 * <p><strong>2c.</strong></p>
	 * <p>If the snmpTrapOID.0 value is not one of the standard
	 * traps, then the generic-trap field is set to 6 and the
	 * specific-trap field is set to the last subid of the
	 * snmpTrapOID.0 value.</p>
	 *
	 * <p>If the next to last subid of snmpTrapOID.0 is zero,
	 * then the enterprise field is set to snmpTrapOID.0 value
	 * and the last 2 subids are truncated from that value.
	 * If the next to last subid of snmpTrapOID.0 is not zero,
	 * then the enterprise field is set to snmpTrapOID.0 value
	 * and the last 1 subid is truncated from that value.</p>
	 *
	 * <p>In any event, the snmpTrapEnterprise.0 varBind (if present)
	 * is ignored in this case.</p>
	 *
	 * @param info		V2 trap
	 */
	private void process(Trapd.V2TrapInformation info)
	{
		Category log = ThreadCategory.getInstance(getClass());

		SnmpPduPacket pdu = info.getPdu();
		InetAddress agent = info.getAgent();
		
		//
		// verify the type
		//
		if(pdu.typeId() != (byte)(SnmpPduPacket.V2TRAP))
		{
			// if not V2 trap, do nothing
			log.warn("Recieved not SNMPv2 Trap from host " + agent.getHostAddress());
			log.warn("PDU Type = " + pdu.getCommand());
			return;
		}
		
		//
		// get the address converted
		//
		IPv4Address addr = new IPv4Address(agent);
		String trapInterface = addr.toString();

		Event event = new Event();
		event.setSource("trapd");
		event.setHost(trapInterface);
		event.setSnmphost(trapInterface);
		event.setInterface(trapInterface);
		event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));

		String ipNodeId = TrapdIPMgr.getNodeId(trapInterface);

		if (ipNodeId != null)
			{
			int intNodeId = Integer.parseInt(ipNodeId);
			event.setNodeid((long)intNodeId);
			}

		if(log.isDebugEnabled())
			log.debug("V2 trap - trapInterface: " + trapInterface);
		
		//
		// set the information
		//
		int numVars = pdu.getLength();
		if(log.isDebugEnabled())
			log.debug("V2 trap numVars or pdu length: " + numVars);
		if(numVars >= 2) // check number of varbinds
		{
			//
			// The first varbind has the sysUpTime
			// Modify the sysUpTime varbind to add the trailing 0 if it is missing
			// The second varbind has the snmpTrapOID
			// Confirm that these two are present
			//
			String varBindName0 = pdu.getVarBindAt(0).getName().toString();
			String varBindName1 = pdu.getVarBindAt(1).getName().toString();
			if (varBindName0.equals(EXTREME_SNMP_SYSUPTIME_OID))
			{
				log.warn("V2 trap from " + trapInterface + " has been corrected due to the sysUptime.0 varbind not having been sent with a trailing 0.\n\tVarbinds received are : " + varBindName0 + " and " + varBindName1);
				varBindName0 = SNMP_SYSUPTIME_OID;
			}

			if ( (!(varBindName0.equals(SNMP_SYSUPTIME_OID))) ||
			     (!(varBindName1.equals(SNMP_TRAP_OID)))      )
			{
				log.warn("V2 trap from " + trapInterface + " IGNORED due to not having the required varbinds.\n\tThe first varbind must be sysUpTime.0 and the second snmpTrapOID.0\n\tVarbinds received are : " + varBindName0 + " and " + varBindName1);
				return;
			}

			Snmp snmpInfo = new Snmp();

			if(log.isDebugEnabled())
				log.debug("V2 trap first varbind value: " 
					  + pdu.getVarBindAt(0).getValue().toString());
			
			// Get the value for the snmpTrapOID
			SnmpObjectId snmpTrapOid = (SnmpObjectId)pdu.getVarBindAt(SNMP_TRAP_OID_INDEX).getValue();
			String snmpTrapOidValue = snmpTrapOid.toString();

			// Force leading "." (dot) if not present
			if (!snmpTrapOidValue.startsWith("."))
			{
				snmpTrapOidValue = "." + snmpTrapOidValue;
			}
			
			if(log.isDebugEnabled())
				log.debug("snmpTrapOID: " + snmpTrapOidValue);

			// get the last subid
			int length = snmpTrapOidValue.length();
			int lastIndex = snmpTrapOidValue.lastIndexOf(DOT_CHAR);

			String lastSubIdStr = snmpTrapOidValue.substring(lastIndex+1);
			int lastSubId = -1;
			try
			{
				lastSubId = Integer.parseInt(lastSubIdStr);
			}
			catch(NumberFormatException nfe)
			{
				lastSubId = -1;
			}

			// Check if standard trap
			if (GENERIC_TRAPS.contains(snmpTrapOid))
			{
				// set generic
				snmpInfo.setGeneric(lastSubId - 1);

				// set specific to zero
				snmpInfo.setSpecific(0);

				// if present, the 'snmpTrapEnterprise' OID occurs as
				// the last OID
				// Check the last varbind to see if it is the enterprise ID
				String varBindName = pdu.getVarBindAt(numVars-1).getName().toString();
				if (varBindName.equals(SNMP_TRAP_ENTERPRISE_ID))
				{
					// if present, set the value of the varbind as the enterprise id
					snmpInfo.setId(pdu.getVarBindAt(numVars-1).getValue().toString());
				}
				else
				{
					// if not present, set the value of the varbind as the
					// snmpTraps value defined as in RFC 1907
					snmpInfo.setId(SNMP_TRAPS + "." + snmpTrapOidValue.charAt(snmpTrapOidValue.length()-1));
				}
				
			}
			else // not standard trap
			{
				// set generic to 6
				snmpInfo.setGeneric(6);

				// set specific to lastsubid
				snmpInfo.setSpecific(lastSubId);
				
				// get the next to last subid
				int nextToLastIndex = snmpTrapOidValue.lastIndexOf(DOT_CHAR, lastIndex-1);

				// check if value is zero
				String nextToLastSubIdStr = snmpTrapOidValue.substring(nextToLastIndex+1, lastIndex);
				if (nextToLastSubIdStr.equals("0"))
				{
					// set enterprise value to trap oid minus the
					// the last two subids
					snmpInfo.setId(snmpTrapOidValue.substring(0, nextToLastIndex));
				}
				else
				{
					snmpInfo.setId(snmpTrapOidValue.substring(0, lastIndex));
				}
			}

			if(log.isDebugEnabled())
				log.debug("snmp specific/generic/eid: " 
					  + snmpInfo.getSpecific() + "\t" 
					  + snmpInfo.getGeneric() + "\t" 
					  + snmpInfo.getId());

			// version
			snmpInfo.setVersion("v2");

			// community
			snmpInfo.setCommunity(new String(info.getCommunity().getString()));

			event.setSnmp(snmpInfo);
			
			Parms parms = new Parms();
			
			for(int i = 0; i < pdu.getLength(); i++)
			{
				Value val = new Value();
				
				String name    = pdu.getVarBindAt(i).getName().toString();
				SnmpSyntax obj = pdu.getVarBindAt(i).getValue();

				if(obj instanceof SnmpInt32)
				{
					val.setType(EventConstants.TYPE_SNMP_INT32 );
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
				}
				else if(obj instanceof SnmpNull)
				{
					val.setType(EventConstants.TYPE_SNMP_NULL);
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
				}
				else if(obj instanceof SnmpObjectId)
				{
					val.setType(EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER);
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
				}
				else if(obj instanceof SnmpIPAddress)
				{
					val.setType(EventConstants.TYPE_SNMP_IPADDRESS);
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
				}
				else if(obj instanceof SnmpTimeTicks)
				{
					val.setType(EventConstants.TYPE_SNMP_TIMETICKS);
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
				}
				else if(obj instanceof SnmpCounter32)
				{
					val.setType(EventConstants.TYPE_SNMP_COUNTER32);
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
				}
				else if(obj instanceof SnmpGauge32)
				{
					val.setType(EventConstants.TYPE_SNMP_GAUGE32);
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
				}
				else if(obj instanceof SnmpOpaque)
				{
					val.setType(EventConstants.TYPE_SNMP_OPAQUE);
					val.setEncoding(EventConstants.XML_ENCODING_BASE64);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_BASE64, obj));
				}
				else if(obj instanceof SnmpOctetString)
				{
					//
					// check for non-printable characters. If they
					// exist then print the string out as hexidecimal
					//
					boolean asHex = false;
					byte[]  data  = ((SnmpOctetString)obj).getString();
					for(int x = 0; x < data.length; x++)
					{
						byte b = data[x];
						if((b < 32 && b != 10 && b != 13) ||  b == 127)
						{
							asHex = true;
							break;
						}
					}
					data = null;
					
					String encoding = asHex ? EventConstants.XML_ENCODING_BASE64 
									: EventConstants.XML_ENCODING_TEXT; 
					val.setType(EventConstants.TYPE_SNMP_OCTET_STRING);
					val.setEncoding(encoding);
					val.setContent(EventConstants.toString(encoding, obj));

					// DEBUG
					if (!asHex && log.isDebugEnabled())
					{
						log.debug("snmpReceivedTrap: string varbind: " 
							  + ( ((SnmpOctetString)obj).toString() ));
					}
				}
				else if(obj instanceof SnmpCounter64)
				{
					val.setType(EventConstants.TYPE_SNMP_COUNTER64);
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
				}
				else
				{
					val.setType(EventConstants.TYPE_STRING);
					val.setEncoding(EventConstants.XML_ENCODING_TEXT);
					val.setContent(obj.toString());
				}
				
				Parm parm = new Parm();
				parm.setParmName( name );
				parm.setValue( val );
				parms.addParm( parm );
			} // end for loop
			
			event.setParms(parms);
		}
		
		// send the event to eventd
		EventIpcManagerFactory.getInstance().getManager().sendNow(event);

		if(log.isDebugEnabled())
			log.debug("V2 Trap successfully converted and sent to eventd");
		
		if (TrapdIPMgr.getNodeId(trapInterface) == null && m_newSuspect)
		{
			sendNewSuspectEvent(trapInterface);

			if(log.isDebugEnabled())
				log.debug("Sent newSuspectEvent for interface: " + trapInterface);
		}
	}
	
	/**
	 * Process a V1 trap and convert it to an event. Once
	 * the event is formatted, send it to eventd. 
	 *
	 * @param info		V1 trap
	 */
	private void process(Trapd.V1TrapInformation info)
	{
		Category log = ThreadCategory.getInstance(getClass());

		SnmpPduTrap pdu = info.getPdu();
		InetAddress agent = info.getAgent();
		
		IPv4Address addr = new IPv4Address(agent);
		String trapInterface = pdu.getAgentAddress().toString();

		Event event = new Event();
		event.setSource("trapd");
		event.setHost(addr.toString());
		event.setSnmphost(trapInterface);
		event.setInterface(trapInterface);
		event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));
		
		String ipNodeId = TrapdIPMgr.getNodeId(trapInterface);

		if (ipNodeId != null)
			{
			int intNodeId = Integer.parseInt(ipNodeId);
			event.setNodeid((long)intNodeId);
			}

		if(log.isDebugEnabled())
			log.debug("V1 trap - trapInterface: " + trapInterface);
		
		//
		// set the snmp information
		//
		Snmp snmpInfo = new Snmp();

		// id
		// 
		// NOTE:  Force leading "." (dot) on all id's
		String entId = pdu.getEnterprise().toString();
		if (!entId.startsWith("."))
		{
			entId = "." + entId;
		}
		snmpInfo.setId(entId);

		// version
		snmpInfo.setVersion("v1");

		// specific
		snmpInfo.setSpecific(pdu.getSpecific());

		// generic
		snmpInfo.setGeneric(pdu.getGeneric());

		// community
		snmpInfo.setCommunity(new String(info.getCommunity().getString()));
		
		event.setSnmp(snmpInfo);
		
		Parms parms = new Parms();
			
		for(int i = 0; i < pdu.getLength(); i++)
		{
			Value val = new Value();
			
			String name    = pdu.getVarBindAt(i).getName().toString();
			SnmpSyntax obj = pdu.getVarBindAt(i).getValue();

			if(obj instanceof SnmpInt32)
			{
				val.setType(EventConstants.TYPE_SNMP_INT32);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
			}
			else if(obj instanceof SnmpNull)
			{
				val.setType(EventConstants.TYPE_SNMP_NULL);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
			}
			else if(obj instanceof SnmpObjectId)
			{
				val.setType(EventConstants.TYPE_SNMP_OBJECT_IDENTIFIER);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
			}
			else if(obj instanceof SnmpIPAddress)
			{
				val.setType(EventConstants.TYPE_SNMP_IPADDRESS);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
			}
			else if(obj instanceof SnmpTimeTicks)
			{
				val.setType(EventConstants.TYPE_SNMP_TIMETICKS);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
			}
			else if(obj instanceof SnmpCounter32)
			{
				val.setType(EventConstants.TYPE_SNMP_COUNTER32);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
			}
			else if(obj instanceof SnmpGauge32)
			{
				val.setType(EventConstants.TYPE_SNMP_GAUGE32);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
			}
			else if(obj instanceof SnmpOpaque)
			{
				val.setType(EventConstants.TYPE_SNMP_OPAQUE);
				val.setEncoding(EventConstants.XML_ENCODING_BASE64);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_BASE64, obj));
			}
			else if(obj instanceof SnmpOctetString)
			{
				//
				// check for non-printable characters. If they
				// exist then print the string out as hexidecimal
				//
				boolean asHex = false;
				byte[]  data  = ((SnmpOctetString)obj).getString();
				for(int x = 0; x < data.length; x++)
				{
					byte b = data[x];
					if((b < 32 && b != 9 && b != 10 && b != 13) ||  b == 127)
					{
						asHex = true;
						break;
					}
				}

				data = null;

				String encoding = asHex ? EventConstants.XML_ENCODING_BASE64 
								: EventConstants.XML_ENCODING_TEXT; 

				val.setType(EventConstants.TYPE_SNMP_OCTET_STRING);
				val.setEncoding(encoding);
				val.setContent(EventConstants.toString(encoding, obj));

				// DEBUG
				if (!asHex && log.isDebugEnabled())
				{
					log.debug("snmpReceivedTrap: string varbind: " + ( ((SnmpOctetString)obj).toString() ));
				}
			}
			else if(obj instanceof SnmpCounter64)
			{
				val.setType(EventConstants.TYPE_SNMP_COUNTER64);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(EventConstants.toString(EventConstants.XML_ENCODING_TEXT, obj));
			}
			else
			{
				val.setType(EventConstants.TYPE_STRING);
				val.setEncoding(EventConstants.XML_ENCODING_TEXT);
				val.setContent(obj.toString());
			}

			Parm parm = new Parm();
			parm.setParmName(name);
			parm.setValue(val);
			
			parms.addParm(parm);
		} // end for loop

		event.setParms(parms);
		
		// send the event to eventd
		EventIpcManagerFactory.getInstance().getManager().sendNow(event);

		if(log.isDebugEnabled())
				log.debug("V1 Trap successfully converted and sent to eventd");
		
		if (TrapdIPMgr.getNodeId(trapInterface) == null && m_newSuspect)
		{
			sendNewSuspectEvent(trapInterface);

			if(log.isDebugEnabled())
				log.debug("Sent newSuspectEvent for interface: " + trapInterface);
		}
	}

	/**
	 * Send a newSuspect event for the interface
	 *
	 * @param trapInterface	The interface for which the newSuspect event is to be generated
	 */
	private void sendNewSuspectEvent(String trapInterface)
	{
		// construct event with 'trapd' as source
		Event event = new Event();
		event.setSource("trapd");
		event.setUei(org.opennms.netmgt.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
		event.setHost(m_localAddr);
		event.setInterface(trapInterface);
		event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));
		
		// send the event to eventd
		EventIpcManagerFactory.getInstance().getManager().sendNow(event);
	}
	
	/**
	 * Returns true if the status is ok and the thread
	 * should continue running. If the status returend is
	 * false then the thread should exit.
	 *
	 */
	private synchronized boolean statusOK()
	{
		Category log = ThreadCategory.getInstance(getClass());

		//
		// Loop until there is a new client or we are shutdown
		//
		boolean exitThread = false;
		boolean exitCheck  = false;
		while(!exitCheck)
		{
			//
			// check the child thread!
			//
			if(m_worker.isAlive() == false && m_status != STOP_PENDING)
			{
				log.warn(getName() + " terminated abnormally");
				m_status = STOP_PENDING;
			}
					
			//
			// do normal status checks now
			//
			if(m_status == STOP_PENDING)
			{
				exitCheck = true;
				exitThread= true;
				m_status = STOPPED;
			}
			else if(m_status == PAUSE_PENDING)
			{
				pause();
			}
			else if(m_status == RESUME_PENDING)
			{
				resume();
			}
			else if(m_status == PAUSED)
			{
				try
				{
					wait();
				}
				catch(InterruptedException e)
				{
					m_status = STOP_PENDING;
				}
			}
			else if(m_status == RUNNING)
			{
				exitCheck = true;
			}

		} // end !exit check

		return !exitThread;

	} // statusOK

	/**
	 * The constructor
	 */
	TrapQueueProcessor(FifoQueue backlog, boolean newSuspect)
	{
		m_backlogQ= backlog;
		m_newSuspect = newSuspect;
		try
		{
			m_localAddr = InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException uhE)
		{
			Category log = ThreadCategory.getInstance(getClass());
			m_localAddr = "localhost";
			log.error("<ctor>: Error looking up local hostname", uhE);
		}
	}

	/**
	 * Starts the current fiber. If the fiber has already
	 * been started, regardless of it's current state, then
	 * an IllegalStateException is thrown.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the 
	 * 	fiber has already been started.
	 *
	 */
	public synchronized void start()
	{
		Category log = ThreadCategory.getInstance(getClass());

		if(m_worker != null)
			throw new IllegalStateException("The fiber is running or has already run");

		m_status = STARTING;

		m_worker = new Thread(this, getName());
		m_worker.start();
		
		if (log.isDebugEnabled())
			log.debug(getName() + " started");
	}

	/**
	 * <p>Pauses the current fiber</P>
	 */
	public synchronized void pause()
	{
		if(m_worker == null || m_worker.isAlive() == false)
			throw new IllegalStateException("The fiber is not running");

		m_status = PAUSED;
		notifyAll();
	}

	/**
	 * Resumes the currently paused fiber.
	 */
	public synchronized void resume()
	{
		if(m_worker == null || m_worker.isAlive() == false)
			throw new IllegalStateException("The fiber is not running");

		m_status = RUNNING;
		notifyAll();
	}

	/**
	 * <p>Stops this fiber. If the fiber has never
	 * been started then an <code>IllegalStateExceptio</code>
	 * is generated.</p>
	 *
	 * @throws java.lang.IllegalStateException Thrown if the fiber has
	 * 	never been started.
	 */
	public synchronized void stop()
	{
		if(m_worker == null)
			throw new IllegalStateException("The fiber has never run");

		m_status = STOP_PENDING;
		m_worker.interrupt();
		notifyAll();
	}

	/**
	 * Returns the name of the fiber.
	 *
	 * @return The name of the Fiber.
	 */
	public String getName()
	{
		return "TrapQueueProcessor";
	}

	/**
	 * Returns the current status of the fiber
	 *
	 * @return The status of the Fiber.
	 */
	public synchronized int getStatus()
	{
		if(m_worker != null && !m_worker.isAlive())
			m_status = STOPPED;

		return m_status;
	}

	
	/**
	 * Reads off of the input queue and depending on the type (V1 or V2 trap)
	 * of object read, process the traps to convert them to events and
	 * send them out
	 */
	public void run()
	{
		Category log = ThreadCategory.getInstance(getClass());

		synchronized(this)
		{
			m_status = RUNNING;
		}

		while(statusOK())
		{
			Object o = null;
			try
			{
				o = m_backlogQ.remove(1000);
			}
			catch(InterruptedException iE)
			{
				log.debug("Trapd.QueueProcessor: caught interrupted exception");
				log.debug(iE.getLocalizedMessage(), iE);

				o = null;

				m_status = STOP_PENDING;
			}
			catch(FifoQueueException qE)
			{
				log.debug("Trapd.QueueProcessor: caught fifo queue exception");
				log.debug(qE.getLocalizedMessage(), qE);

				o = null;

				m_status = STOP_PENDING;
			}

			if(o != null && statusOK())
			{
				if(o instanceof Trapd.V1TrapInformation)
				{
					try
					{
						process((Trapd.V1TrapInformation)o);
					}
					catch(Throwable t)
					{
						log.error("Unexpected error processing V1 trap", t);
					}
				}
				else
				{
					try
					{
						process((Trapd.V2TrapInformation)o);
					}
					catch(Throwable t)
					{
						log.error("Unexpected error processing V2 trap", t);
					}
				}
			}
		}
	} 
}


