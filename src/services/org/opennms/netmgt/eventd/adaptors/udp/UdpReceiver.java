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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InterruptedIOException;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.adaptors.EventReceiver;
import org.opennms.netmgt.eventd.adaptors.EventHandler;

/**
 *
 * @author <a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @author <a href="http://www.oculan.com">Oculan Corporation</a>
 *
 */
class UdpReceiver
	implements Runnable
{
	/**
	 * The list of incomming events.
	 */
	private List 		m_eventsIn;

	/**
	 * The Fiber's status.
	 */
	private volatile boolean m_stop;

	/** 
	 * The UDP socket for receipt and transmission
	 * of packets from agents.
	 */
	private DatagramSocket	m_dgSock;
	
	/**
	 * The context thread
	 */
	private Thread		m_context;

	/**
	 * The log prefix
	 */
	private String 		m_logPrefix;

	/**
	 * construct a new receiver
	 */
	UdpReceiver(DatagramSocket sock, List xchange)
	{
		m_eventsIn = xchange;
		m_stop = false;
		m_dgSock = sock;
		m_logPrefix = org.opennms.netmgt.eventd.Eventd.LOG4J_CATEGORY;
	}

	/**
	 * stop the current receiver
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
	 * Return true if this receiver is alive
	 */
	boolean isAlive()
	{
		return (m_context == null ? false : m_context.isAlive());
	}

	/**
	 * The execution context.
	 */
	public void run()
	{
		// get the context
		//
		m_context = Thread.currentThread();

		// Get a log instance
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

		// allocate a buffer
		//
		final int length = 0xffff;
		final byte[] buffer = new byte[length];
		DatagramPacket pkt = new DatagramPacket(buffer, length);

		// set an SO timout to make sure we don't block forever
		// if a socket is closed.
		//
		try
		{
			if(isTracing)
				log.debug( "Setting socket timeout to 500ms");

			m_dgSock.setSoTimeout(500);
		}
		catch(SocketException e)
		{
			log.warn("An I/O error occured while trying to set the socket timeout", e);
		}

		// Increase the receive buffer for the 
		// socket
		//
		try
		{
			if(isTracing)
				log.debug( "Setting receive buffer size to " + length);

			m_dgSock.setReceiveBufferSize(length);
		}
		catch(SocketException e)
		{
			log.info("Failed to set the receive buffer to " + length, e);
		}

		// set to avoid numerious tracing message
		// 
		boolean ioInterrupted = false;

		// now start processing incomming request
		//
		while(!m_stop)
		{
			if(m_context.isInterrupted())
			{
				if(isTracing)
					log.debug( "Thread context interrupted");

				break;
			}

			try
			{
				if(isTracing && !ioInterrupted)
					log.debug( "Wating on a datagram to arrive");

				m_dgSock.receive(pkt);
				ioInterrupted = false; // reset the flag
			}
			catch(InterruptedIOException e)
			{
				ioInterrupted = true;
				continue;
			}
			catch(IOException e)
			{
				log.error("An I/O exception occured on the datagram receipt port, exiting",e);
				break;
			}

			try
			{
				if(isTracing)
					log.debug( "Sending received packet to processor");

				UdpReceivedEvent re = UdpReceivedEvent.make(pkt);
				synchronized(m_eventsIn)
				{
					m_eventsIn.add(re);
					m_eventsIn.notify();
				}
			}
			catch(UnsupportedEncodingException e)
			{
				log.warn("Failed to convert received XML event, discarding", e);
			}

			pkt = new DatagramPacket(buffer, length);

		} // end while status ok

		if(isTracing)
			log.debug( "Thread context exiting");

	} // end run method

	void setLogPrefix(String prefix)
	{
		m_logPrefix = prefix;
	}

} // end class
