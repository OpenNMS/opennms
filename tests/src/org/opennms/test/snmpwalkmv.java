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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
//
// Tab Size = 8
//
// snmpwalkmv.java,v 1.1.1.1 2001/11/11 17:35:57 ben Exp
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
 * @author	<A HREF="mailto:weave@opennms.org">Brian Weaver</A>
 * @author	<A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class snmpwalkmv extends Object implements SnmpHandler
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
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the SNMP Interface table. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	private static NamedSnmpVar[]	ms_elemList = null;

	/**
	 * Lookup strings for specific table entries
	 */
	public final static	String	IF_INDEX 	= "ifIndex";
	public final static	String	IF_DESCR	= "ifDescr";
	public final static	String	IF_TYPE 	= "ifType";
	public final static	String	IF_MTU 		= "ifMtu";
	public final static	String	IF_SPEED 	= "ifSpeed";
	public final static	String	IF_PHYS_ADDR	= "ifPhysAddr";
	public final static	String	IF_ADMIN_STATUS = "ifAdminStatus";
	public final static	String	IF_OPER_STATUS 	= "ifOperStatus";
	public final static	String	IF_LAST_CHANGE 	= "ifLastChange";
	public final static	String	IF_IN_OCTETS 	= "ifInOctets";
	public final static	String	IF_IN_UCAST 	= "ifInUcastPkts";
	public final static	String	IF_IN_NUCAST 	= "ifInNUcastPkts";
	public final static	String	IF_IN_DISCARDS 	= "IfInDiscards";
	public final static	String	IF_IN_ERRORS 	= "IfInErrors";
	public final static	String	IF_IN_UKNOWN_PROTOS 	= "ifInUnknownProtos";
	public final static	String	IF_OUT_OCTETS 	= "ifOutOctets";
	public final static	String	IF_OUT_UCAST 	= "ifOutUcastPkts";
	public final static	String	IF_OUT_NUCAST 	= "ifOutNUcastPkts";
	public final static	String	IF_OUT_DISCARDS = "IfOutDiscards";
	public final static	String	IF_OUT_ERRORS 	= "IfOutErrors";
	public final static 	String	IF_OUT_QLEN	= "ifOutQLen";
	public final static 	String	IF_SPECIFIC	= "ifSpecific";

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static
	{
		ms_elemList = new NamedSnmpVar[22];
		int ndx = 0;
		
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_INDEX, 		".1.3.6.1.2.1.2.2.1.1",  1);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, 	IF_DESCR, 		".1.3.6.1.2.1.2.2.1.2",  2);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_TYPE, 		".1.3.6.1.2.1.2.2.1.3",  3);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_MTU, 		".1.3.6.1.2.1.2.2.1.4",  4);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32,		IF_SPEED, 		".1.3.6.1.2.1.2.2.1.5",  5);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,	IF_PHYS_ADDR,		".1.3.6.1.2.1.2.2.1.6",  6);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_ADMIN_STATUS,	".1.3.6.1.2.1.2.2.1.7",  7);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, 		IF_OPER_STATUS, 	".1.3.6.1.2.1.2.2.1.8",  8);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS, 	IF_LAST_CHANGE, 	".1.3.6.1.2.1.2.2.1.9",  9);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32, 	IF_IN_OCTETS,		".1.3.6.1.2.1.2.2.1.10", 10);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_UCAST,		".1.3.6.1.2.1.2.2.1.11", 11);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_NUCAST,	 	".1.3.6.1.2.1.2.2.1.12", 12);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_DISCARDS, 	".1.3.6.1.2.1.2.2.1.13", 13);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_ERRORS, 		".1.3.6.1.2.1.2.2.1.14", 14);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_IN_UKNOWN_PROTOS,	".1.3.6.1.2.1.2.2.1.15", 15);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_OCTETS, 		".1.3.6.1.2.1.2.2.1.16", 16);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_UCAST, 		".1.3.6.1.2.1.2.2.1.17", 17);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_NUCAST, 		".1.3.6.1.2.1.2.2.1.18", 18);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_DISCARDS,	".1.3.6.1.2.1.2.2.1.19", 19);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,	IF_OUT_ERRORS, 		".1.3.6.1.2.1.2.2.1.20", 20);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32,		IF_OUT_QLEN, 		".1.3.6.1.2.1.2.2.1.21", 21);
		ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID,	IF_SPECIFIC,	 	".1.3.6.1.2.1.2.2.1.22", 22);
	}
	
	
	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the interface table in the MIB forest.</P>
	 */
	public static final String	TABLE_OID	= ".1.3.6.1.2.1.2.2.1";	// start of table (GETNEXT)
	
	/**
	 * <P>The SnmpObjectId that represents the root of the 
	 * interface tree. It is created when the class is 
	 * initialized and contains the value of TABLE_OID.
	 *
	 * @see #TABLE_OID
	 */
	public static final SnmpObjectId ROOT = new SnmpObjectId(TABLE_OID);

	
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
			m_startOid  = ".1.3.6.1.2.1.2.2.1.1"; // hard code to ifTable
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
		// The last variable in the list of elements
		// is always the first to run off the table, so 
		// we only need to check that one.
		//
		SnmpVarBind[] vars = null;
		if(snmpwalkmv.ROOT.isRootOf(pdu.getVarBindAt(pdu.getLength()-1).getName()))
		{
			//
			// Create a new map of the interface entry
			//
			vars = pdu.toVarBindArray();
			
			//Loop through and print varbind values
			for(int x = 0; x < ms_elemList.length; x++)
			{
				SnmpObjectId id = new SnmpObjectId(ms_elemList[x].getOid());
				for(int y = 0; y < vars.length; y++)
				{
					if(id.isRootOf(vars[y].getName()))
					{
						try 
						{
							//
							// Retrieve the class object of the expected SNMP data type for this element
							//
							Class classObj = ms_elemList[x].getTypeClass();
						
							//
							// If the SnmpSyntax object matches the expected class 
							// then store it in the map. Else, store a null pointer
							// in the map.
							//
							if (classObj.isInstance(vars[y].getValue()))
							{
								System.out.println(vars[y].getName() + ": " + vars[y].getValue());
							}
							else
							{
								// do nothing
							}
						}
						catch (ClassNotFoundException e)
						{
							System.out.println("Failed retrieving SNMP type class for element: " 
							   		+ ms_elemList[x].getAlias());
							System.out.println(e.getLocalizedMessage());
						}
						catch (NullPointerException e)
						{
							System.out.println(e.getLocalizedMessage());
						}
						break;
					}
				}	
			}
		}
		else
		{
			System.out.println("End of mib reached");
			synchronized(session)
			{
				session.notify();
			}
			return;
		}
				
				
		//
		// next pdu
		//
		SnmpPduRequest nxt = new SnmpPduRequest(SnmpPduPacket.GETNEXT);
		for(int x = 0; x < vars.length; x++)
		{
			nxt.addVarBind(new SnmpVarBind(vars[x].getName()));
		}
				
		session.send(nxt, this);
	}
	
	/**
	 * The main routine.
	 */
	public static void main(String[] args)
	{
		snmpwalkmv walker = new snmpwalkmv();
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
		
		// build the first request
		SnmpPduRequest pdu = new SnmpPduRequest(SnmpPduRequest.GETNEXT);
		for(int x = 0; x < ms_elemList.length; x++)
		{
			SnmpObjectId   oid = new SnmpObjectId(ms_elemList[x].getOid());
			pdu.addVarBind(new SnmpVarBind(oid));
		}
		
		//
		// send the first request
		//
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

