//
// Copyright (C) 2000 Shivakumar C. Patil <shivakumar.patil@stdc.com>
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
// Tab Size = 8
//
// trapd.java,v 1.1.1.1 2001/11/11 17:35:57 ben Exp
//
// Log:
//	06/08/00 - Brian Weaver <weave@opennms.org>
//		Commented and added file to CVS
//
package org.opennms.test;

import java.lang.*;
import java.net.*;
import java.util.*;

import org.opennms.protocols.snmp.*;

/**
 * <P>Implements a sample SNMP trap daemon that listens and prints
 * traps received from remote agents on port 162.</P>
 *
 * @author <A HREF="mailto:shivakumar.patil@stdc.com">Shivakumar C. Patil</A>
 * @author <A HREF="mailto:weave@opennms.org">Brian Weaver</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version 1.1.1.1
 */
public class trapd implements SnmpTrapHandler
{
	/**
	 * The main routine. All arguments are ignored. The program
	 * will terminate if any error in the trap session occur. However,
	 * malformed packets will be discarded in the error handling method
	 * of this class.
	 *
	 * @param args	The command line arguments -- IGNORED.
	 */
	public static void main (String args[])
	{
		try
		{
			SnmpTrapSession testTrapSession = new SnmpTrapSession(new trapd());
			System.out.println("SNMP Trap Receiver Started");
			synchronized(testTrapSession)
			{
				testTrapSession.wait();
			}
			System.out.println("SNMP Trap Receiver Exiting");
			testTrapSession.close();
		}
		catch (Exception e)
		{
			System.out.println("Exception in main(): " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Receives and prints information about SNMPv2c traps.
	 *
	 * @param session	The Trap Session that received the PDU.
	 * @param agent		The address of the remote sender.
	 * @param port		The remote port where the pdu was transmitted from.
	 * @param community	The decoded community string.
	 * @param pdu		The decoded V2 trap pdu.
	 *
	 */
	public void snmpReceivedTrap(SnmpTrapSession 		session, 
				     java.net.InetAddress 	agent, 
				     int 			port,
				     SnmpOctetString 		community,
				     SnmpPduPacket 		pdu)
	{
		System.out.println("V2 Trap from agent " + agent.toString() + " on port " + port);
		System.out.println("V2 Trap PDU command......... " + pdu.getCommand());
		System.out.println("V2 Trap PDU ID.............. " + pdu.getRequestId());
		System.out.println("V2 Trap PDU Length.......... " + pdu.getLength());
		
		if(pdu instanceof SnmpPduRequest)
		{
			System.out.println("V2 Trap PDU Error Status.... " + ((SnmpPduRequest)pdu).getErrorStatus());
			System.out.println("V2 Trap PDU Error Index..... " + ((SnmpPduRequest)pdu).getErrorIndex());
		}
	
		int k = pdu.getLength();
		for (int i = 0; i < k ; i++ )
		{
			SnmpVarBind vb = pdu.getVarBindAt(i);
			System.out.print("Varbind[" + i + "] := " + vb.getName().toString());
			System.out.println(" --> " + vb.getValue().toString());		
		}
		System.out.println("");	 
		
		//synchronized(session)
		//{
		//	session.notify();
		//}
	}

	/**
	 * Receives and prints information about SNMPv1 traps.
	 *
	 * @param session	The Trap Session that received the PDU.
	 * @param agent		The address of the remote sender.
	 * @param port		The remote port where the pdu was transmitted from.
	 * @param community	The decoded community string.
	 * @param pdu		The decoded V1 trap pdu.
	 *
	 */
	public void snmpReceivedTrap(SnmpTrapSession 		session,
				     java.net.InetAddress 	agent,
				     int 			port,
				     SnmpOctetString 		community,
				     SnmpPduTrap 		pdu)

	{
		System.out.println("V1 Trap from agent " + agent.toString() + " on port " + port);
		System.out.println("Ip Address................. " + pdu.getAgentAddress() );
		System.out.println("Enterprise Id.............. " + pdu.getEnterprise() );
		System.out.println("Generic ................... " + pdu.getGeneric() );
		System.out.println("Specific .................. " + pdu.getSpecific() );
		System.out.println("TimeStamp ................. " + pdu.getTimeStamp() );
		System.out.println("Length..................... " + pdu.getLength() );
	
	
		int k = pdu.getLength();
		for (int i = 0; i < k ; i++ )
		{
			SnmpVarBind vb = pdu.getVarBindAt(i);
			System.out.print("Varbind[" + i + "] := " + vb.getName().toString());
			System.out.println(" --> " + vb.getValue().toString());		
		}
		System.out.println("");
		
		//synchronized(session)
		//{
		//session.notify();
		//}		
	}
	
	/**
	 * Process session errors.
	 *
	 * @param session	The trap session in error.
	 * @param error		The error condition.
	 * @param ref		The reference object, if any.
	 *
	 */
	public void snmpTrapSessionError(SnmpTrapSession 	session,
					 int 			error,
					 java.lang.Object 	ref)
	{
		System.out.println("An error occured in the trap session");
		System.out.println("Session error code = " + error);
		if(ref != null)
		{
			System.out.println("Session error reference: " + ref.toString());
		}
		
		if(error == SnmpTrapSession.ERROR_EXCEPTION)
		{
			synchronized(session)
			{
				session.notify(); // close the session
			}
		}
	}
}
