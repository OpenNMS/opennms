//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
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

package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.ping.Reply;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * This class represents a suspect event generator that takes
 * instances of ping replies and converts them to events. The
 * events are then sent to the event daemon.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 *
 */
final class SuspectEventGenerator
	implements Runnable, Fiber
{
	/**
	 * The value used as the source of the event.
	 */
	final static String	EVENT_SOURCE_VALUE 	= "OpenNMS.Discovery";

	/**
	 * The queue where ping replies are recovered.
	 */
	private FifoQueue	m_replies;

	/**
	 * The time to live to set on events sent out
	 */
	private long		m_ttl;

	/**
	 * The status of the fiber.
	 */
	private int		m_status;

	/**
	 * The name of the fiber.
	 */
	private String		m_name;

	/**
	 * The worker thread that reads replies and sends
	 * events.
	 */
	private Thread		m_worker;

	/**
	 * Construts a new instance of the class that is used to send
	 * new suspect events. The reply queue passed during construction
	 * is used to extract instances of {@link org.opennms.netmgt.ping.Reply ping replies}
	 * found by the discovery process. The replies are turned into suspect node
	 * events and sent to eventd
	 *
	 */
	SuspectEventGenerator(FifoQueue replyQ, long ttl)
	{
		m_name    = "Discovery:SuspectEventGenerator";
		m_replies = replyQ;
		m_ttl     = ttl;
		m_status  = START_PENDING;
		m_worker  = null;
	}

	/**
	 * <p>Starts the fiber. The fiber is transitioned from 
	 * <code>START_PENDING</code> to <code>STARTING</code>
	 * and once the fiber startup is complete the status 
	 * is changed to <code>RUNNING</code>.</p>
	 *
	 * <p>If the fiber has already been started then an
	 * exception is generated.</p>
	 *
	 * @throws java.lang.IllegalStateException Thrown if the fiber
	 * 	has already been started.
	 *
	 */
	public synchronized void start()
	{
		if(m_worker != null)
			throw new IllegalStateException("The fiber has already been start or has already run");

		m_status = STARTING;
		m_worker = new Thread(this, getName());
		m_worker.start();
	}
	
	/**
	 * Stops the fiber if it is running. If the fiber has
	 * never run then an illegal state exception is generated.
	 * This method sends the shutdown signal to the thread, but
	 * does not wait on the thread to complete.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the fiber was
	 * 	never started.
	 */
	public synchronized void stop()
	{
		if(m_worker == null)
			throw new IllegalStateException("The fiber has never been started");

		if(m_status != STOPPED)
		{
			m_status = STOP_PENDING;
			m_worker.interrupt();
		}
	}

	/**
	 * Returns the current status of the fiber.
	 *
	 * @return The fiber's status.
	 */
	public synchronized int getStatus()
	{
		if(m_worker != null && !m_worker.isAlive())
			m_status = STOPPED;

		return m_status;
	}

	/**
	 * Returns the name of this fiber.
	 *
	 * @return The name of the fiber.
	 */
	public String getName()
	{
		return m_name;
	}

	/**
	 * <p>This method is used to do the main work for the fiber. This
	 * method is invoked once and will not return until the fiber
	 * is stopped, or a non-recovereable error occurs.</p>
	 *
	 * <p>After starting the status is changed to <code>RUNNING</code>
	 * and a loop starts. The loop extracts the instances of
	 * {@link org.opennms.netmgt.ping.Reply Reply} objects and then
	 * generates XML based event messages that are sent to eventd</p>
	 *
	 */
	public void run()
	{
		Category log = ThreadCategory.getInstance(getClass());
		if(log.isDebugEnabled())
			log.debug("run: Thread Started");

		synchronized(this)
		{
			m_status = RUNNING;
		}

		for(;;)
		{
			// Check for exit!
			//
			synchronized(this)
			{
				if(m_status != RUNNING)
					break;
			}

			// Get the next element from the queue
			//
			Reply r = null;
			try
			{
				r = (Reply) m_replies.remove();

				if(log.isDebugEnabled())
					log.debug("run: received next reply, " + m_replies.size() + " left in queue");
			}
			catch(InterruptedException ex)
			{
				log.debug("run: thread interrupted", ex);
				break;
			}
			catch(FifoQueueException ex)
			{
				log.info("run: queue exception", ex);
				break;
			}
			catch(ClassCastException ex)
			{
				log.warn("run: Invalid class type found in queue", ex);
				continue;
			}

			// Make sure it's not null
			//
			if(r != null)
			{
				// Create an event
				//
				Event event = new Event();
				event.setSource(EVENT_SOURCE_VALUE);
				event.setUei(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
				event.setInterface(r.getAddress().getHostAddress());
				try
				{
					event.setHost(InetAddress.getLocalHost().getHostName());
				}
				catch(UnknownHostException uhE)
				{
					event.setHost("unresolved.host");
					log.warn("Failed to resolve local hostname", uhE);
				}

				event.setTime(EventConstants.formatToString(new java.util.Date()));

				Parms parms = new Parms();
				Parm rttParm = new Parm(); // the response time parm
				rttParm.setParmName("RTT");

				Value v = new Value();
				v.setType("int");
				v.setContent(Long.toString(r.getPacket().getReceivedTime() - r.getPacket().getSentTime()));
				rttParm.setValue(v);
				parms.addParm(rttParm);
				event.setParms(parms);
			

				try
				{
					EventIpcManagerFactory.getInstance().getManager().sendNow(event);

					if(log.isDebugEnabled())
					{
						log.debug("Sent event: " + EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
					}
				}
				catch(Throwable t)
				{
					log.warn("run: unexpected throwable exception caught during send to middleware", t);
				}
			}
		} // end for(;;)
	
		synchronized(this)
		{
			m_status = STOPPED;
		}
		if(log.isDebugEnabled())
			log.debug("run: Thread exiting");

	} // end run
}
