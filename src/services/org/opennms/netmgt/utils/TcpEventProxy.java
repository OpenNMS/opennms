//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 31 Jan 2003: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.utils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.Socket;

import org.exolab.castor.xml.Marshaller;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

/**
 * This is the interface used to send events into the event subsystem -
 * It is typically used by the poller framework plugins that perform
 * service monitoring to send out aprropriate events. Can also be used by
 * capsd, discovery etc.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public final class TcpEventProxy
	implements EventProxy 
{
	private static final int		DEFAULT_PORT = 5817;
	private static final InetAddress	DEFAULT_HOST;

	private InetAddress	m_target;
	private int		m_port;
	private Socket		m_sock;
	private Writer		m_writer;
	private Reader		m_reader;
	private Thread		m_rdrThread;

	static
	{
		try
		{
			DEFAULT_HOST = InetAddress.getByName("127.0.0.1");
		}
		catch(IOException e)
		{
			throw new UndeclaredThrowableException(e);
		}
	}

	public TcpEventProxy()
		throws IOException
	{
		this(DEFAULT_HOST, DEFAULT_PORT);
	}

	public TcpEventProxy(int port)
		throws IOException
	{
		this(DEFAULT_HOST, port);
	}

	public TcpEventProxy(InetAddress target)
		throws IOException
	{
		this(target, DEFAULT_PORT);
	}

	public TcpEventProxy(InetAddress target, int port)
		throws IOException
	{
		m_port = port;
		m_target = target;

		// get a socket and set the timeout
		//
		m_sock = new Socket(m_target, m_port);
		m_sock.setSoTimeout(500);

		m_writer = new OutputStreamWriter(new BufferedOutputStream(m_sock.getOutputStream()));
		m_reader = new InputStreamReader(m_sock.getInputStream());
		m_rdrThread = new Thread("TcpEventProxy Input Discarder") {
			public void run()
			{
				int ch = 0;
				while(ch != -1)
				{
					try
					{
						ch = m_reader.read();
					}
					catch(InterruptedIOException e)
					{
						ch = 0;
					}
					catch(IOException e)
					{
						ch = -1;
					}
				}
			}// end run
		};
		m_rdrThread.setDaemon(true);
		m_rdrThread.start();
	}

	public void close()
	{
		if(m_sock != null)
		{
			try
			{
				m_sock.close();
			}
			catch(IOException e)
			{
				ThreadCategory.getInstance(getClass()).warn("Error closing socket", e);
			}
		}
		m_sock = null;
		if(m_rdrThread.isAlive())
			m_rdrThread.interrupt();
	}

	protected void finalize()
		throws Throwable
	{
		close();
	}

	/**
	 * This method is called to send the event out
	 *
	 * @param event		the event to be sent out
	 *
	 * @exception java.lang.RuntimeException thrown if the send fails for any reason
	 */
	public void send(Event event)
	{
		try
		{
			Log elog = new Log();
			Events events = new Events();
			events.addEvent(event);
			elog.setEvents(events);

			Marshaller.marshal(elog, m_writer);
			m_writer.flush();
		}
		catch(Throwable t)
		{
			throw new UndeclaredThrowableException(t);
		}
	}

	/**
	 * This method is called to send an event log containing multiple events out
	 *
	 * @param eventLog	the events to be sent out
	 *
	 * @exception java.lang.RuntimeException thrown if the send fails for any reason
	 */
	public void send(Log eventLog)
	{
		try
		{
			Marshaller.marshal(eventLog, m_writer);
			m_writer.flush();
		}
		catch(Throwable t)
		{
			throw new UndeclaredThrowableException(t);
		}
	}
}
