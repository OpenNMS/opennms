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

package org.opennms.netmgt.actiond;

import java.lang.*;

import java.lang.reflect.UndeclaredThrowableException;

import java.io.IOException;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.fiber.PausableFiber;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.config.ActiondConfigFactory;

/**
 * This class is used to represent the auto action execution
 * service. When an event is received by this service that has
 * one of either a notification, trouble ticket, or auto action
 * then a process is launched to execute the appropriate commands.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org</a>
 *
 */
public final class Actiond
	implements PausableFiber
{
	/**
	 * The log4j category used to log debug messsages
	 * and statements.
	 */
	private static final String LOG4J_CATEGORY = "OpenNMS.Actiond";

	/**
	 * The singleton instance.
	 */
	private static final Actiond	m_singleton = new Actiond();

	/**
	 * The execution launcher and reaper
	 */
	private Executor		m_execution;

	/**
	 * The broadcast event receiver.
	 */
	private BroadcastEventProcessor	m_eventReader;

	/**
	 * The current status of this fiber
	 */
	private int			m_status;

	/**
	 * Constructs a new Action execution daemon.
	 */
	private Actiond()
	{
		m_execution   = null;
		m_eventReader = null;
		m_status      = START_PENDING;
	}

	public synchronized void init()
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();

		// Load the configuration informatin
		//
		ActiondConfigFactory aFactory = null;
		try
		{
			ActiondConfigFactory.reload();
			aFactory = ActiondConfigFactory.getInstance();
		}
		catch(MarshalException ex)
		{
			log.error("Failed to load actiond configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			log.error("Failed to load actiond configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			log.error("Failed to load actiond configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}

		// A queue for execution
		//
		FifoQueue execQ = new FifoQueueImpl();

		// start the event reader
		//
		try
		{
			m_eventReader = new BroadcastEventProcessor(execQ);
		}
		catch(Exception ex)
		{
			log.error("Failed to setup event reader", ex);
			throw new UndeclaredThrowableException(ex);
		}

		m_execution = new Executor(execQ, aFactory.getMaxProcessTime(), aFactory.getMaxOutstandingActions());
	}

	/**
	 * Starts the <em>Actiond</em> service. The process of starting
	 * the service involves reading the configuration data, starting
	 * an event receiver, and creating an execution fiber. If the
	 * services is already running then an exception is thrown.
	 *
	 * @throws java.lang.IllegalStateException Thrown if the service
	 * 	is already running.
	 */
	public synchronized void start()
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();

		if(m_status == START_PENDING)
		{
			m_status = STARTING;
			if(m_execution == null)
			{
				 init();
			}

			m_execution.start();
			m_status = RUNNING;

			log.info("Actiond running");
		}
		else if(m_execution != null && m_execution.getStatus() != STOPPED)
		{
			// Service is already running?
			throw new IllegalStateException("The actiond service is already running");
		}
	}

	/**
	 * Stops the currently running service. If the service is
	 * not running then the command is silently discarded.
	 *
	 */
	public synchronized void stop()
	{
		m_status  = STOP_PENDING;

		try
		{
			if(m_execution != null)
			{
				m_execution.stop();
			}
		}
		catch(Exception e) { }

		if(m_eventReader != null)
		{
			m_eventReader.close();
		}

		m_eventReader = null;
		m_execution   = null;
		m_status = STOPPED;
	}

	/**
	 * Returns the current status of the service.
	 *
	 * @return The service's status.
	 */
	public synchronized int getStatus()
	{
		return m_status;
	}

	/**
	 * Returns the name of the service.
	 *
	 * @return The service's name.
	 */
	public String getName()
	{
		return "OpenNMS.Actiond";
	}

	/**
	 * Pauses the service if its currently running
	 */
	public synchronized void pause()
	{
		if(m_status != RUNNING)
		{
			return;
		}

		m_status = PAUSE_PENDING;

		m_execution.pause();
		m_status = PAUSED;
	}

	/**
	 * Resumes the service if its currently paused
	 */
	public synchronized void resume()
	{
		if(m_status != PAUSED)
		{
			return;
		}

		m_status = RESUME_PENDING;

		m_execution.resume();
		m_status = RUNNING;
	}

	/**
	 * Returns the singular instance of the actiond
	 * daemon. There can be only one instance of this
	 * service per virtual machine.
	 */
	public static Actiond getInstance()
	{
		return m_singleton;
	}
}
