//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//	Brian Weaver	<weave@oculan.com>
//	http://www.opennms.org/
//
//
// Tab Size = 8
//
// snmpwalk.java,v 1.1.1.1 2001/11/11 17:35:57 ben Exp
//
//

package org.opennms.test;

import java.lang.*;
import java.io.*;
import java.net.*;
import org.opennms.protocols.snmp.*;

/**
 * <P>This class is designed to provide an example of how to use
 * the JoeSNMP libraries from <A HREF="http://www.opennms.org">OpenNMS</A>.
 * This example illustrates the code required to walk an SNMP tree
 * of an individual agent on a remote host.</P>
 *
 * <P>The class will walk the entire agent tree, or a subsection, as
 * defined by the command line arguments. Through the command line
 * options it is possible to set the community string, timeouts, retries,
 * and other options used by the JoeSNMP library.</P>
 *
 * <P>Usage: java org.opennms.test.snmpwalk [options] remote-host [object-id]</P>
 * <P>The command line options are as follows:
 * 	<TABLE>
 *	<TR>
 *	 	<TD nowrap>-v</TD>
 *	 	<TD nowrap>1 || 2 || 2c</TD>
 *	 	<TD>Sets the SNMP protocol version. 2 & 2c are identical and stand for
 *	 	    SNMPv2 community string based.
 *	 	</TD>
 * 	</TR>
 *	<TR>
 *		<TD nowrap>-c</TD>
 *		<TD nowrap>community</TD>
 *		<TD>Sets the community string used to authenticate</TD>
 *	</TR>
 *	<TR>
 *		<TD nowrap>-r</TD>
 *		<TD nowrap>retries</TD>
 *		<TD>Sets the number of time the SNMP message is retransmitted before
 *		    the communication times out.</TD>
 * 	</TR>
 *	<TR>
 *		<TD nowrap>-t</TD>
 *		<TD nowrap>timeout (seconds)</TD>
 *		<TD>Sets the timeout value. Fraction of a second is acceptable, but
 *		    a millisecond resolution is the smallest supported. For example:
 *		    .8 is equalivant to 800 milliseconds.</TD>
 *	</TR>
 *	<TR>
 *		<TD nowrap>-p</TD>
 *		<TD nowrap>port</TD>
 *		<TD>The remote port to communicate with the remote agent on.</TD>
 *	</TR>
 *	</TABLE>
 * </P>
 *
 * @version	1.1.1.1
 * @author	<A HREF="mailto:weave@oculan.com">Brian Weaver</A>
 * @author	<A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class snmpwalk extends Object implements SnmpHandler
{
	/**
	 * The version of the SNMP protocol used to communicate
	 */
	int		m_version 	= SnmpSMI.SNMPV1;	// -v
	
	/**
	 * The community string used to "authenticate" the request.
	 */
	String		m_community  	= null;			// -c
	
	/**
	 * The number of retries to use.
	 */
	int		m_retries 	= -1;			// -r
	
	/**
	 * The time period to wait before considering the last transmission
	 * a failure. This should be in milliseconds.
	 */
	int		m_timeout	= -1;			// -t
	
	/**
	 * The port where request are sent & received from.
	 */
	int		m_port		= -1;			// -p
	
	/**
	 * The remote agent to communicate with.
	 */
	String		m_host		= "127.0.0.1";
	
	/**
	 * The default location to start querying the table.
	 * This is the entire iso(1).org(3) tree by default.
	 */
	String		m_startOid	= ".1.3";
	
	/**
	 * The object identifier where the walk of 
	 * the tree should stop.
	 */
	SnmpObjectId	m_stopAt	= null;
	
	/**
	 * <P>Parse the command line options. If there is an illegal
	 * option then an exception is thrown.</P>
	 *
	 * <P>The command line options are as follows:
	 * 	<TABLE>
	 *	<TR>
	 *	 	<TD nowrap>-v</TD>
	 *	 	<TD nowrap>1 || 2 || 2c</TD>
	 *	 	<TD>Sets the SNMP protocol version. 2 & 2c are identical and stand for
	 *	 	    SNMPv2 community string based.
	 *	 	</TD>
	 * 	</TR>
	 *	<TR>
	 *		<TD nowrap>-c</TD>
	 *		<TD nowrap>community</TD>
	 *		<TD>Sets the community string used to authenticate</TD>
	 *	</TR>
	 *	<TR>
	 *		<TD nowrap>-r</TD>
	 *		<TD nowrap>retries</TD>
	 *		<TD>Sets the number of time the SNMP message is retransmitted before
	 *		    the communication times out.</TD>
	 * 	</TR>
	 *	<TR>
	 *		<TD nowrap>-t</TD>
	 *		<TD nowrap>timeout (seconds)</TD>
	 *		<TD>Sets the timeout value. Fraction of a second is acceptable, but
	 *		    a millisecond resolution is the smallest supported. For example:
	 *		    .8 is equalivant to 800 milliseconds.</TD>
	 *	</TR>
	 *	<TR>
	 *		<TD nowrap>-p</TD>
	 *		<TD nowrap>port</TD>
	 *		<TD>The remote port to communicate with the remote agent on.</TD>
	 *	</TR>
	 *	</TABLE>
	 * </P>	 
	 *
	 * @params args	The command line arguments from the main program.
	 * @exceception IllegalArgumentException Thrown if there is an
	 * 	unknown or malformed argument.
	 *
	 */
	void parseOptions(String[] args) throws IllegalArgumentException
	{
		int lastArg = 0;
		for(int x = 0; x < args.length; x++)
		{
			if(args[x].startsWith("-"))
			{
				if(args[x].equals("-c"))
				{
					m_community = args[++x];
				}
				else if(args[x].equals("-v"))
				{
					if(args[++x].equals("1"))
					{
						m_version = SnmpSMI.SNMPV1;
					}
					else if(args[x].equals("2") ||
						args[x].equals("2c"))
					{
						m_version = SnmpSMI.SNMPV2;
					}
				}
				else if(args[x].equals("-r"))
				{
					try
					{
						m_retries = Integer.parseInt(args[++x]);
					}
					catch(NumberFormatException e)
					{
						throw new IllegalArgumentException("Malformed retry number");
					}
				}
				else if(args[x].equals("-t"))
				{
					try
					{
						float f = Float.parseFloat(args[++x]);
						m_timeout = (int)(f * 1000);
					}
					catch(NumberFormatException e)
					{
						throw new IllegalArgumentException("Malformed timeout period");
					}
				}
				else if(args[x].equals("-p"))
				{
					try
					{
						m_port = Integer.parseInt(args[++x]);
					}
					catch(NumberFormatException e)
					{
						throw new IllegalArgumentException("Malformed port number");
					}
				}
				else if(args[x].equals("--"))
				{
					//
					// end of arguments
					//
					lastArg = x+1;
					break;
				}
				else
				{
					throw new IllegalArgumentException("Unknown Option " + args[x]);
				}
				lastArg = x+1;
			}
		} // end for
		
		//
		// Now the last two values should be the (host, oid) pair!
		//
		if((args.length - lastArg) == 1) // just the host!
		{
			m_host = args[lastArg++];
		}
		else if((args.length - lastArg) == 2)
		{
			m_host = args[lastArg++];
			m_startOid  = args[lastArg++];
		}
		else
		{
			throw new IllegalArgumentException("Invalid number of arguments");
		}
	} // end of parseOptions
	
	/**
	 * Defined by the SnmpHandler interface. Used to process internal session
	 * errors.
	 *
	 * @param session	The SNMP session in error.
	 * @param err		The Error condition
	 * @param pdu		The pdu associated with this error condition
	 *
	 */
	public void snmpInternalError(SnmpSession session, int err, SnmpSyntax pdu)
	{
		System.err.println("An unexpected error occured with the SNMP Session");
		System.err.println("The error code is " + err);
		synchronized(session)
		{
			session.notify();
		}
	}
	
	/**
	 * This method is define by the SnmpHandler interface and invoked
	 * if an agent fails to respond.
	 *
	 * @param session	The SNMP session in error.
	 * @param pdu		The PDU that timedout.
	 *
	 */
	public void snmpTimeoutError(SnmpSession session, SnmpSyntax pdu)
	{
		System.err.println("The session timed out trying to communicate with the remote host");
		synchronized(session)
		{
			session.notify();
		}
	}
	
	/**
	 * This method is defined by the SnmpHandler interface and invoked
	 * when the agent responds to the management application.
	 *
	 * @param session	The session receiving the pdu.
	 * @param cmd		The command from the pdu. 
	 * @param pdu		The received pdu.
	 *
	 * @see org.opennms.protocols.snmp.SnmpPduPacket#getCommand
	 */ 
	public void snmpReceivedPdu(SnmpSession session, int cmd, SnmpPduPacket pdu)
	{
		SnmpPduRequest req = null;
		if(pdu instanceof SnmpPduRequest)
		{
			req = (SnmpPduRequest)pdu;
		}
		
		if(pdu.getCommand() != SnmpPduPacket.RESPONSE)
		{
			System.err.println("Error: Received non-response command " + pdu.getCommand());
			synchronized(session)
			{
				session.notify();
			}
			return;
		}
		
		if(req.getErrorStatus() != 0)
		{
			System.out.println("End of mib reached");
			synchronized(session)
			{
				session.notify();
			}
			return;
		}
		
		//
		// Passed the checks so lets get the first varbind and
		// print out it's value
		//
		SnmpVarBind vb = pdu.getVarBindAt(0);
		if(vb.getValue().typeId() == SnmpEndOfMibView.ASNTYPE ||
		   (m_stopAt != null && m_stopAt.compare(vb.getName()) < 0))
		{
			System.out.println("End of mib reached");
			synchronized(session)
			{
				session.notify();
			}
			return;
		}
		
		System.out.println(vb.getName().toString() + ": " + vb.getValue().toString());
		
		//
		// make the next pdu
		//
		SnmpVarBind[] vblist  = { new SnmpVarBind(vb.getName()) };
		SnmpPduRequest newReq = new SnmpPduRequest(SnmpPduPacket.GETNEXT, vblist);
		newReq.setRequestId(SnmpPduPacket.nextSequence());
		
		session.send(newReq);
	}
	
	/**
	 * The main routine.
	 */
	public static void main(String[] args)
	{
		snmpwalk walker = new snmpwalk();
		InetAddress remote = null;
		
		try
		{
			walker.parseOptions(args);
			remote = InetAddress.getByName(walker.m_host);
		}
		catch(IllegalArgumentException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		catch(UnknownHostException e)
		{
			System.err.println("UnknownHostException: " + e.getMessage());
			System.exit(1);
		}
		
		//
		// Initialize the peer
		//
		SnmpPeer peer = new SnmpPeer(remote);
		if(walker.m_port != -1)
			peer.setPort(walker.m_port);
		
		if(walker.m_timeout != -1)
			peer.setTimeout(walker.m_timeout);
		
		if(walker.m_retries != -1)
			peer.setRetries(walker.m_retries);

		SnmpParameters parms = peer.getParameters();
		parms.setVersion(walker.m_version);
		if(walker.m_community != null)
			parms.setReadCommunity(walker.m_community);
				
	
		//
		// Now create the session, set the initial request
		// and walk the tree!
		//
		SnmpSession session = null;
		try
		{
			session = new SnmpSession(peer);
		}
		catch(SocketException e)
		{
			System.err.println("SocketException creating the SNMP session");
			System.err.println("SocketException: " + e.getMessage());
			System.exit(1);
		}
		
		session.setDefaultHandler(walker);
		
		//
		// set the stop point
		//
		SnmpObjectId id = new SnmpObjectId(walker.m_startOid);
		int[] ids = id.getIdentifiers();
		++ids[ids.length-1];
		id.setIdentifiers(ids);
		walker.m_stopAt = id;
		
		//
		// send the first request
		//
		SnmpVarBind[]  vblist = { new SnmpVarBind(walker.m_startOid) };
		SnmpPduRequest pdu = new SnmpPduRequest(SnmpPduPacket.GETNEXT, vblist);
		pdu.setRequestId(SnmpPduPacket.nextSequence());
		try
		{
			synchronized(session)
			{
				session.send(pdu);
				session.wait();
			}
		}
		catch(InterruptedException e) 
		{ 
			// do nothing
		}
		finally
		{
			session.close();
		}
	} // end main
}

