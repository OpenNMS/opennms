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
// Tab Stop = 8
//
//
package org.opennms.netmgt.dhcpd;

import java.util.StringTokenizer;
import java.lang.*;
import java.lang.reflect.UndeclaredThrowableException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.bucknell.net.JDHCP.*;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.config.DhcpdConfigFactory; 

/**
 * <P>Establishes a TCP socket connection with the DHCP daemon
 * and formats and sends request messages.</P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 * @version CVS 1.1.1.1
 */
final class Poller
{
	/**
	 * The hardware address (ex: 00:06:0D:BE:9C:B2)
	 */
	private static final byte[]	DEFAULT_ADDRESS = { (byte) 0x00, (byte) 0x06, (byte) 0x0d, (byte) 0xbe,
							     (byte) 0x9c, (byte) 0xb2, (byte) 0x00, (byte) 0x00,
							     (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
							     (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	private static byte[]   s_hwAddress = null;
  

	/** 
	 * Broadcast flag...when set in the 'flags' portion of the DHCP DISCOVER
	 * packet forces the DHCP server to broadcast the DHCP OFFER response.
	 * Without this option set the server will unicast the packet back using
	 * the IP address specified in the OFFER response set as the destination.
	 * This packet will never reach the application since the IP stack will
	 * discard it because the destination address of the packet doesn't 
	 * match the IP address of the interface.
	 */
	static final short		BROADCAST_FLAG = (short)0x8000;
	
	/** 
	 * Default retries
	 */
	static final int  		DEFAULT_RETRIES = 2;

	/**
	 * Default timeout
	 */
	static final long 		DEFAULT_TIMEOUT = 3000L; 
	
	/**
	 * The message type for the DHCP request.
	 */
	private static final int 	MESSAGE_TYPE 	= 53;

	/** 
	 * Holds the value for the next identifier sent to the 
	 * DHCP server.
	 */
	private static int		m_nextXid = (new java.util.Random(System.currentTimeMillis())).nextInt();
	
	/**
	 * TCP Socket connection with DHCP Daemon
	 */
	private Socket 			m_connection;

	/**
	 * Output Object stream
	 */
	private ObjectOutputStream 	m_outs;

	/**
	 * Objects from the server.
	 */
	private ObjectInputStream	m_ins;

	/**
	 * Returns a disconnection request message that can
	 * be sent to the server.
	 *
	 * @return A disconnection message.
	 *
	 */
	private static Message getDisconnectRequest()
		throws UnknownHostException
	{
		return new Message(InetAddress.getByName("0.0.0.0"), new DHCPMessage());
	}

	/**
	 * Returns a DHCP DISCOVER message that can be sent to the 
	 * DHCP server.  DHCP server should respond with a DHCP OFFER
	 * message in response..
	 * 
	 * NOTE:  BROADCAST flag is set on the packet to force the
	 *        server to broadcast the DHCP OFFER message.  
	 *        Without this flag the message will be unicasted
	 *        back to the IP address specified in the OFFER and
	 *        will be discarced by the IP stack never reaching
	 *        the DHCP client daemon process.  
	 * 
	 *
	 * @param addr	The address to poll
	 *
	 * @return The message to send to the DHCP server.
	 *
	 */
	private static Message getPollingRequest(InetAddress addr)
	{
		int xid = 0;
		synchronized(Poller.class)
		{
			xid = ++m_nextXid;
		}
		DHCPMessage messageOut = new DHCPMessage();
		
		// fill DHCPMessage object 
		//
		messageOut.setOp((byte) 1);
		messageOut.setHtype((byte) 1);
		messageOut.setHlen((byte) 6);
		messageOut.setHops((byte) 0);
		messageOut.setXid(xid); 	
		messageOut.setSecs((short) 0);
		messageOut.setFlags(BROADCAST_FLAG);  // Force server to broadcast response
	
		messageOut.setChaddr(s_hwAddress); // set hardware address
	
		messageOut.setOption(MESSAGE_TYPE, new byte[] { (byte)DHCPMessage.DISCOVER });
		
		return new Message(addr, messageOut);
	}

	
	/**
	 * Ensures that during garbage collection the 
	 * resources used by this object are released!
	 */
	protected void finalize()
		throws Throwable
	{
		close();
	}
	
	/**
	 * Constructor.
	 * 
	 * Establishes a TCP socket conection with the DHCP client daemon on port 5818.
	 * 
	 * @throws IOException if unable to establish the connection 
	 *  			with the DHCP client daemon.
	 */
	private Poller(long timeout)
		throws IOException 
	{
		Category log = ThreadCategory.getInstance(this.getClass());
		try
		{
			if (log.isDebugEnabled())
				log.debug("Poller.ctor: opening socket connection with DHCP client daemon on port " + DhcpdConfigFactory.getInstance().getPort());
			m_connection = new Socket(InetAddress.getLocalHost(), DhcpdConfigFactory.getInstance().getPort());

			if (log.isDebugEnabled())
				log.debug("Poller.ctor: setting socket timeout to " + timeout);
			m_connection.setSoTimeout((int)timeout);

			// Establish input/output object streams
			m_ins  = new ObjectInputStream(m_connection.getInputStream());
			m_outs = new ObjectOutputStream(m_connection.getOutputStream());
			m_outs.reset();
			m_outs.flush();
		}
		catch(IOException ex)
		{
			log.error("IO Exception during socket connection establishment with DHCP client daemon.", ex);
			if(m_connection != null)
			{
				try { m_connection.close(); } catch(Throwable t) { }
			}
			throw ex;
		}
		catch(Throwable t)
		{
			log.error("Unexpected exception during socket connection establishment with DHCP client daemon.", t);
			if(m_connection != null)
			{
				try { m_connection.close(); } catch(Throwable tx) { }
			}
			throw new UndeclaredThrowableException(t);
		}
	}

	/**
	 * Closes the client's socket connection to the DHCP daemon.
	 *
	 * @throws IOException if the socket close() method fails.
	 */
	public void close()
	{
		try
		{
			m_connection.close();
		}
		catch(Throwable ex) { }
	}
	
	/**
	 * <p>This method actually tests the remote host to determine if it is 
	 * running a functional DHCP server.</p>
	 * 
	 * <p>Formats a DHCP discover message and encodes it in a client request
	 * message which is sent to the DHCP daemon over the established
	 * TCP socket connection.  If a matching DHCP response packet is 
	 * not received from the DHCP daemon within the specified timeout
	 * the client request message will be re-sent up to the specified
	 * number of retries.</p>
	 * 
	 * <p>If a response is received from the DHCP daemon it is validated
	 * to ensure that:</p>
	 * <ul>
	 *	<li>
	 *    		The DHCP response packet was sent from the remote host
	 *    		to which the original request packet was directed.
	 *	</li>
	 *	<li>
	 *    		The XID of the DHCP offer response packet matches the
	 *    		XID of the original DHCP discover packet.
	 *	</li>
	 * </ul>
	 *
	 * <p>If the response validates 'true' is returned.    Otherwise
	 * the request is resent until max retry count is exceeded.</p>
	 *
	 * <p>Before returning, a client disconnect message (remote host 
	 * field set to zero) is sent to the DHCP daemon.</p>
	 * 
	 * @return response time in milliseconds if the specified host responded 
	 * 	with a valid DHCP offer	datagram within the context of the specified 
	 * 	timeout and retry values or negative one (-1) otherwise.
	 */
	static long isServer(InetAddress host, long timeout, int retries) 
		throws IOException
	{
		Category log = ThreadCategory.getInstance(Poller.class);
		
		if (log.isDebugEnabled())
			log.debug("isServer: checking for DHCP on " + host.getHostAddress() + " timeout=" + timeout + " retries=" + retries);
		boolean isDhcpServer = false;

		if (s_hwAddress == null)
		{
			String hwAddressStr = DhcpdConfigFactory.getInstance().getMacAddress();
			if (log.isDebugEnabled())
				log.debug("isServer: setting hardware/MAC address to " + hwAddressStr);
			setHwAddress(hwAddressStr);
		}
					
		Poller p = new Poller(timeout < 500L ? timeout : 500L);
		long responseTime = -1;
		try
		{
			// allocate an array to hold the retry count
			//
			Message ping = getPollingRequest(host);

			while(retries >= 0 && !isDhcpServer)
			{
				if (log.isDebugEnabled())
					log.debug("isServer: sending DISCOVER request to DHCP server for host " + 
						host.getHostAddress() + 
						" with Xid: " + 
							ping.getMessage().getXid());
				
				long start = System.currentTimeMillis();
				p.m_outs.writeObject(ping);
				long end;

				do
				{
					Message resp = null;
					try
					{
						resp = (Message)p.m_ins.readObject();
					}
					catch(InterruptedIOException ex) 
					{ 
						resp = null;
					} 
					
					if (resp != null)
					{
						responseTime = System.currentTimeMillis() - start;
						
						// DEBUG only
						if (log.isDebugEnabled())
							log.debug("isServer: got a DHCP poll response from host " + 
								resp.getAddress().getHostAddress() + 
								" with Xid: " + 
								resp.getMessage().getXid());
	
						if ( host.equals(resp.getAddress()) && 
							ping.getMessage().getXid() == resp.getMessage().getXid())
						{
							if (log.isDebugEnabled())
								log.debug("isServer: got a DHCP poll response for this poller, validating OFFER message...");
								
							// Inspect response message to see if it is a valid DHCP OFFER message
							byte [] type = resp.getMessage().getOption(MESSAGE_TYPE);
							if(type[0] == DHCPMessage.OFFER)
							{
								if (log.isDebugEnabled())
									log.debug("isServer: got a valid DHCP offer, responseTime= " + responseTime + "ms");
					
								isDhcpServer = true;
								break;
							}
						}
					}
					
					end = System.currentTimeMillis();

				} while((end - start) < timeout);

				if (!isDhcpServer)
				{
					if (log.isDebugEnabled())
						log.debug("Timed out waiting for DHCP response, remaining retries: " + retries);
				}

				--retries;
			}
			
			p.m_outs.writeObject(getDisconnectRequest());
		}
		catch(IOException ex)
		{
			log.error("IO Exception caught.", ex);
			p.close();
			throw ex;
		}
		catch(Throwable t)
		{
			log.error("Unexpected Exception caught.", t);
			p.close();
			throw new UndeclaredThrowableException(t);
		}

		// Return response time if the remote box IS a DHCP
		// server or -1 if the remote box is NOT a DHCP server.
		//
		if (isDhcpServer)
		{
			return responseTime;
		}
		else
		{
			return -1;
		}
	}

	// Converts the provided hardware address string (format= 00:00:00:00:00:00)
	// to an array of bytes which can be passed in a DHCP DISCOVER packet.
	// 
	private static void setHwAddress(String hwAddressStr) 
	{
		// initialize the address
		//
		s_hwAddress = DEFAULT_ADDRESS;

		StringTokenizer token = new StringTokenizer(hwAddressStr, ":");
		Integer tempInt = new Integer(0);
		int temp;
		int i = 0;
		while (i < 6) {
			temp = tempInt.parseInt(token.nextToken(), 16);
			s_hwAddress[i] = (byte) temp;
			i++;
		}
	}
}
