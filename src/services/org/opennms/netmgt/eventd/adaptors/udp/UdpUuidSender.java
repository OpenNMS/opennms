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
package org.opennms.netmgt.eventd.adaptors.udp;

import java.lang.*;
import java.lang.reflect.UndeclaredThrowableException;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InterruptedIOException;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.core.utils.ThreadCategory;

import org.apache.log4j.Category;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.netmgt.eventd.adaptors.EventReceiver;
import org.opennms.netmgt.eventd.adaptors.EventHandler;

/**
 * This class implements the User Datagram Protocol (UDP) event
 * receiver. When the an agent sends an event via UDP/IP the receiver
 * will process the event and then add the UUIDs to the internal list.
 * If the event is successfully processed then an event-receipt is 
 * returned to the caller.
 *
 * @author <a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @author <a href="http://www.oculan.com">Oculan Corporation</a>
 *
 */
final class UdpUuidSender
	implements Runnable
{
	/** 
	 * The list of outgoing event-receipts
	 * by UUID.
	 */
	private List		m_eventUuidsOut;
	
	/**
	 * The stop flag
	 */
	private volatile boolean m_stop;

	/** 
	 * The UDP socket for receipt and transmission
	 * of packets from agents.
	 */
	private DatagramSocket	m_dgSock;

	/**
	 * The thread context
	 */
	private Thread 		m_context;

	/** 
	 * The list of handlers
	 */
	private List		m_handlers;

	/**
	 * The log prefix
	 */
	private String		m_logPrefix;

	/**
	 * Constructs a new instance of this runnable.
	 */
	UdpUuidSender(DatagramSocket sock, List uuidsOut, List handlers)
	{
		m_context = null;
		m_dgSock  = sock;
		m_stop    = false;
		m_eventUuidsOut = uuidsOut;
		m_handlers = handlers;
		m_logPrefix = org.opennms.netmgt.eventd.Eventd.LOG4J_CATEGORY;
	}

	/**
	 * Stops the current context.
	 */
	void stop()
		throws InterruptedException
	{
		m_stop = true;
		if(m_context != null)
		{
			Category log = ThreadCategory.getInstance(getClass());
			if(log.isDebugEnabled())
				log.debug( "Stopping and joining thread context " + m_context.getName());

			m_context.interrupt();
			m_context.join();

			if(log.isDebugEnabled())
				log.debug( "Thread context stopped and joined");
		}
	}

	/**
	 * Returns true if the runnable is still
	 * running in its context
	 */
	boolean isAlive()
	{
		return (m_context == null ? false : m_context.isAlive());
	}

	public void run()
	{
		// get the context
		//
		m_context = Thread.currentThread();

		// get a logger
		//
		ThreadCategory.setPrefix(m_logPrefix);
		Category log = ThreadCategory.getInstance(getClass());
		boolean isTracing = log.isDebugEnabled();

		if(m_stop)
		{
			if(isTracing)
				log.debug( "Stop flag set before thread started, exiting");
			return;
		}
		else if(isTracing)
			log.debug( "Thread context started");

		// This loop is labeled so that it can be
		// exited quickly when the thread is interrupted
		//
		ArrayList eventHold = new ArrayList(30);
		Map receipts = new HashMap();

		RunLoop:
		while(!m_stop)
		{
			if(isTracing)
				log.debug( "Waiting on event receipts to be generated");

			synchronized(m_eventUuidsOut)
			{
				// wait for an event to show up.
				// wait in 1 second intervals
				//
				while(m_eventUuidsOut.isEmpty())
				{
					try
					{
						// use wait instead of sleep to 
						// release the lock!
						//
						m_eventUuidsOut.wait(1000);
					}
					catch(InterruptedException ie)
					{
						if(isTracing)
							log.debug( "Thread context interrupted");
						break RunLoop;
					}
				}
				
				eventHold.addAll(m_eventUuidsOut);
				m_eventUuidsOut.clear();
			}

			if(isTracing)
			{
				log.debug( "Received " + eventHold.size() + " event receipts to process");
				log.debug( "Processing receipts");
			}

			// build an event-receipt
			//
			Iterator iter = eventHold.iterator();
			while(iter.hasNext())
			{
				UdpReceivedEvent re = (UdpReceivedEvent)iter.next();
				Iterator xiter = re.getAckedEvents().iterator();
				while(xiter.hasNext())
				{
					Event e = (Event)xiter.next();
					if(e.getUuid() != null)
					{
						EventReceipt receipt = (EventReceipt) receipts.get(re);
						if(receipt == null)
						{
							receipt = new EventReceipt();
							receipts.put(re, receipt);
						}
						receipt.addUuid(e.getUuid());
					}
				}
			}
			eventHold.clear();

			if(isTracing)
				log.debug( "Event receipts sorted, transmitting receipts");

			// turn them into XML and send it out the socket
			// 
			iter = receipts.entrySet().iterator();
			while(iter.hasNext())
			{
				Map.Entry entry = (Map.Entry)iter.next();
				UdpReceivedEvent re = (UdpReceivedEvent)entry.getKey();
				EventReceipt receipt = (EventReceipt)entry.getValue();

				StringWriter writer = new StringWriter();
				try
				{
					Marshaller.marshal(receipt, writer);
				}
				catch(ValidationException e)
				{
					log.warn("Failed to build event receipt for agent " 
						 + re.getSender().getHostAddress() + ":" + re.getPort(), e);
				}
				catch(MarshalException e)
				{
					log.warn("Failed to build event receipt for agent " 
						 + re.getSender().getHostAddress() + ":" + re.getPort(), e);
				}

				String xml = writer.getBuffer().toString();
				try
				{
					byte[] xml_bytes = xml.getBytes("US-ASCII");
					DatagramPacket pkt = new DatagramPacket(xml_bytes, xml_bytes.length, re.getSender(), re.getPort());

					if(isTracing)
					{
						log.debug( "Transmitting receipt to destination "
							+ re.getSender().getHostAddress()
							+ ":"
							+ re.getPort());
					}

					m_dgSock.send(pkt);
					synchronized(m_handlers)
					{
						Iterator i = m_handlers.iterator();
						while(i.hasNext())
						{
							try
							{
								((EventHandler)i.next()).receiptSent(receipt);
							}
							catch(Throwable t)
							{
								log.warn("Error processing event receipt", t);
							}
						}
					}

					if(isTracing)
					{
						log.debug( "Receipt transmitted OK {");
						log.debug( xml);
						log.debug( "}");
					}
				}
				catch(UnsupportedEncodingException e)
				{
					log.warn("Failed to convert XML to byte array", e);
				}
				catch(IOException e)
				{
					log.warn("Failed to send packet to host" + re.getSender().getHostAddress() + ":" + re.getPort(), e);
				}
			}
			receipts.clear();

		} // end RunLoop
		
		if(isTracing)
			log.debug( "Context finished, returning");
		
	} // end run()

	void setLogPrefix(String prefix)
	{
		m_logPrefix = prefix;
	}
} // end EventProcessor Class

