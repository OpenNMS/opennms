//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This software is Proprietary and Confidental.
// 
// For more information contact: 
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
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

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

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
import org.opennms.netmgt.eventd.adaptors.EventHandlerMBeanProxy;

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
public final class UdpEventReceiver
	implements EventReceiver,
		UdpEventReceiverMBean
{
	/**
	 * The default User Datagram Port for the receipt and
	 * transmission of events.
	 */
	private static final int UDP_PORT = 5817;

	/** 
	 * The UDP receiver thread.
	 */
	private UdpReceiver	m_receiver;

	/** 
	 * The user datagram packet processor
	 */
	private UdpProcessor	m_processor;

	/** 
	 * The event receipt generator and sender thread.
	 */
	private UdpUuidSender	m_output;

	/**
	 * The list of incomming events.
	 */
	private List 		m_eventsIn;

	/** 
	 * The list of outgoing event-receipts
	 * by UUID.
	 */
	private List		m_eventUuidsOut;
	
	/**
	 * The list of registered event handlers.
	 */
	private List		m_handlers;

	/**
	 * The Fiber's status.
	 */
	private volatile int	m_status;

	/** 
	 * The UDP socket for receipt and transmission
	 * of packets from agents.
	 */
	private DatagramSocket	m_dgSock;

	/**
	 * The UDP socket port binding.
	 */
	private int		m_dgPort;

	/**
	 * The log prefix
	 */
	private String		m_logPrefix;

	public UdpEventReceiver()
	{
		m_dgSock  = null;
		m_dgPort  = UDP_PORT;

		m_eventsIn = new LinkedList();
		m_eventUuidsOut = new LinkedList();

		m_handlers = new ArrayList(3);
		m_status   = START_PENDING; 

		m_dgSock = null;
		m_receiver = null;
		m_processor= null;
		m_output   = null;
		m_logPrefix = null;
	}

	public UdpEventReceiver(int port)
	{
		m_dgSock  = null;
		m_dgPort  = port;

		m_eventsIn = new LinkedList();
		m_eventUuidsOut = new LinkedList();

		m_handlers = new ArrayList(3);
		m_status   = START_PENDING; 

		m_dgSock = null;
		m_receiver = null;
		m_processor= null;
		m_output   = null;
		m_logPrefix = null;
	}

	public synchronized void start()
	{
		if(m_status != START_PENDING)
			throw new RuntimeException("The Fiber is in an incorrect state");

		m_status = STARTING;

		try
		{
			m_dgSock = new DatagramSocket(m_dgPort);

			m_receiver  = new UdpReceiver(m_dgSock, m_eventsIn);
			m_processor = new UdpProcessor(m_handlers,
						       m_eventsIn,
						       m_eventUuidsOut);
			m_output    = new UdpUuidSender(m_dgSock,
							m_eventUuidsOut,
							m_handlers);

			if(m_logPrefix != null)
			{
				m_receiver.setLogPrefix(m_logPrefix);
				m_processor.setLogPrefix(m_logPrefix);
				m_output.setLogPrefix(m_logPrefix);
			}
		}
		catch(IOException e)
		{
			throw new java.lang.reflect.UndeclaredThrowableException(e);
		}

		Thread rThread = new Thread(m_receiver, "UDP Event Receiver[" + m_dgPort + "]");
		Thread pThread = new Thread(m_processor, "UDP Event Processor[" + m_dgPort + "]");
		Thread oThread = new Thread(m_output, "UDP UUID Sender[" + m_dgPort + "]");
		try
		{
			rThread.start();
			pThread.start();
			oThread.start();
		}
		catch(RuntimeException e)
		{
			rThread.interrupt();
			pThread.interrupt();
			oThread.interrupt();

			m_status = STOPPED;
			throw e;
		}

		m_status = RUNNING;
	}

	public synchronized void stop()
	{
		if(m_status == STOPPED)
			return;
		if(m_status == START_PENDING)
		{
			m_status = STOPPED;
			return;
		}

		m_status = STOP_PENDING;

		try
		{
			m_receiver.stop();
			m_processor.stop();
			m_output.stop();
		}
		catch(InterruptedException e)
		{
			Category log = ThreadCategory.getInstance(this.getClass());
			log.warn("The thread was interrupted while attempting to join sub-threads", e);
		}

		m_dgSock.close();

		m_status = STOPPED;
	}

	public String getName()
	{
		return "Event UDP Receiver[" + m_dgPort + "]";
	}

	public int getStatus()
	{
		return m_status;
	}

	public void init()
	{
	}

	public void destroy()
	{
	}

	public void setPort(Integer port)
	{
		if(m_status == STARTING || m_status == RUNNING || m_status == STOP_PENDING)
			throw new IllegalStateException("The process is already running");

		m_dgPort = port.intValue();
	}

	public Integer getPort()
	{
		return new Integer(m_dgPort);
	}

	/**
	 * Adds a new event handler to receiver. When new
	 * events are received the decoded event is passed
	 * to the handler.
	 *
	 * @param handler	A reference to an event handler
	 *
	 */
	public void addEventHandler(EventHandler handler)
	{
		synchronized(m_handlers)
		{
			if(!m_handlers.contains(handler))
				m_handlers.add(handler);
		}
	}

	/**
	 * Removes an event handler from the list of handler
	 * called when an event is received. The handler is
	 * removed based upon the method <code>equals()</code>
	 * inherieted from the <code>Object</code> class.
	 *
	 * @param handler	A reference to the event handler.
	 *
	 */
	public void removeEventHandler(EventHandler handler)
	{
		synchronized(m_handlers)
		{
			m_handlers.remove(handler);
		}
	}

	public void addEventHandler(String name)
		throws MalformedObjectNameException, 
			InstanceNotFoundException
	{
		addEventHandler(new EventHandlerMBeanProxy(new ObjectName(name)));
	}

	public void removeEventHandler(String name)
		throws MalformedObjectNameException, 
			InstanceNotFoundException
	{
		removeEventHandler(new EventHandlerMBeanProxy(new ObjectName(name)));
	}

	public void setLogPrefix(String prefix)
	{
		m_logPrefix = prefix;
	}
}
