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
//
// Tab Stop = 8
//

package org.opennms.netmgt.capsd;

import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.concurrent.RunnableConsumerThreadPool;

import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;


// castor classes generated from the capsd-configuration.xsd
import org.opennms.netmgt.config.capsd.*;

/**
 * <P>The Capability daemon - it is notified by the discovery process when
 * a new node is discovered - it then polls for all the capabilities for
 * this node and is responsible for loading the data collecte1d into the
 * database.</P>
 *
 * <P>Once a node is added to the database, its sends an indication back to the 
 * discovery which then flags this node as 'known'.</P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public class Capsd implements PausableFiber
{
	/**
	 * The log4j category used to log messages.
	 */
	private static final String 	LOG4J_CATEGORY		= "OpenNMS.Capsd";
	
	/**
	 * Singleton instance of the Capsd class
	 */
	private static final Capsd	m_singleton = new Capsd();

	/**
	 * Current status of this fiber
	 */
	private int				m_status;
	
	/**
	 * Database synchronization lock for synchronizing write access
	 * to the database between the SuspectEventProcessor and
	 * RescanProcessor thread pools
	 */
	private static Object			m_dbSyncLock = new Object();
	
	/**
	 * <P>Contains dotted-decimal representation of the IP address
	 * where Capsd is running.  Used when capsd sends events out</P>
	 */
	private static String 			m_address = null;
	
	/**
	 * Rescan scheduler thread
	 */
	private Scheduler			m_scheduler;
	
	/**
	 * Event receiver.
	 */
	private BroadcastEventProcessor		m_receiver;

	/**
	 * The pool of threads that are used to executed the
	 * SuspectEventProcessor instances queued by the
	 * event processor (BroadcastEventProcessor).
	 */
	private RunnableConsumerThreadPool m_suspectRunner;
	
	/**
	 * The pool of threads that are used to executed 
	 * RescanProcessor instances queued by the rescan
	 * scheduler thread.
	 */
	private RunnableConsumerThreadPool m_rescanRunner;
	
	/**
	 * <P>Static initialization</P>
	 */
	 
	static
	{
		try
		{
			m_address = InetAddress.getLocalHost().getHostAddress();
		}
		catch(UnknownHostException uhE)
		{
			m_address = "localhost";
			ThreadCategory.getInstance(LOG4J_CATEGORY).warn("Could not lookup the host name for the local host machine, address set to localhost", uhE);
		}
	} // end static class initialization

	/**
	 * Constructs the Capsd objec
	 */
	public Capsd()
	{
		m_scheduler = null;
		m_status = START_PENDING;
	}

	/**
	 * Stop the Capsd threads.
	 */
	public void stop()
	{
		m_status = STOP_PENDING;

		// Stop the broadcast event receiver
		//
		m_receiver.close();
		
		// Stop the Suspect Event Processor thread pool
		//
		m_suspectRunner.stop();
		
		// Stop the Rescan Processor thread pool
		//
		m_rescanRunner.stop();
		
		m_status = STOPPED;
	}
	
	/**
	 * Start the Capsd threads.
	 */
	public void init()
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();

		// Initialize the Capsd configuration factory.
		//
		try
		{
			CapsdConfigFactory.reload();
		}
		catch(MarshalException ex)
		{
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			log.error("Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		// Initialize the poller configuration factory.
		//
		try
		{
			PollerConfigFactory.reload();
		}
		catch(MarshalException ex)
		{
			log.error("Failed to load poller configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			log.error("Failed to load poller configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			log.error("Failed to load poller configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		// Initialize the collectd configuration factory.
		//
		try
		{
			CollectdConfigFactory.reload();
		}
		catch(MarshalException ex)
		{
			log.error("Failed to load collectd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			log.error("Failed to load collectd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			log.error("Failed to load collectd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		// Initialize the Database configuration factory
		//
		try
		{
			DatabaseConnectionFactory.reload();
		}
		catch (IOException ie)
		{
			log.fatal("IOException loading database config", ie);
			throw new UndeclaredThrowableException(ie);
		}
		catch (MarshalException me)
		{
			log.fatal("Marshall Exception loading database config", me);
			throw new UndeclaredThrowableException(me);
		}
		catch (ValidationException ve)
		{
			log.fatal("Validation Exception loading database config", ve);
			throw new UndeclaredThrowableException(ve);
		}
		catch (ClassNotFoundException ce)
		{
			log.fatal("Class lookup failure loading database config", ce);
			throw new UndeclaredThrowableException(ce);
		}
		
		// Initialize the SNMP Peer Factory
		//
		try
		{
			SnmpPeerFactory.reload();
		}
		catch(MarshalException ex)
		{
			log.error("Failed to load SNMP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			log.error("Failed to load SNMP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			log.error("Failed to load SNMP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		// Get connection to the database and use it to sync the 
		// content of the database with the latest configuration
		// information.
		// 
		// First any new services are added to the services table
		// with a call to syncServices().
		//
		// Secondly the management state of interfaces and services
		// in the database is updated based on the latest configuration
		// information with a call to syncManagementState()
		//
		// Lastly the primary snmp interface state ('isSnmpPrimary')
		// of all interfaces which support SNMP is updated based on 
		// the latest configuration information via a call to syncSnmpPrimaryState()
		java.sql.Connection conn = null;
		try
		{
			conn = DatabaseConnectionFactory.getInstance().getConnection();
			if (log.isDebugEnabled())
			{
				log.debug("init: Loading services into database...");
			}
			CapsdConfigFactory.getInstance().syncServices(conn);
			if (log.isDebugEnabled())
			{
				log.debug("init: Syncing management state...");
			}
			CapsdConfigFactory.getInstance().syncManagementState(conn);
			if (log.isDebugEnabled())
			{
				log.debug("init: Syncing primary SNMP interface state...");
			}
			CapsdConfigFactory.getInstance().syncSnmpPrimaryState(conn);
		}
		catch (SQLException sqlE)
		{
			log.fatal("SQL Exception while syncing database with latest configuration information.", sqlE);
			throw new UndeclaredThrowableException(sqlE);
		}
		catch (Throwable t)
		{
			log.fatal("Unknown error while syncing database with latest configuration information.", t);
			throw new UndeclaredThrowableException(t);
		}
		finally
		{
			try
			{
				if(conn != null)
				{
					conn.close();
				}
			}
			catch(Exception e) { }
		}
		
		// Create the suspect event and rescan thread pools
		//
		m_suspectRunner = new RunnableConsumerThreadPool("Capsd Suspect Pool", 
								 0.0f, 
								 0.0f, 
								 CapsdConfigFactory.getInstance().getMaxSuspectThreadPoolSize());
		
		m_rescanRunner = new RunnableConsumerThreadPool("Capsd Rescan Pool", 
								0.0f, // Only stop thread if nothing in queue
								0.0f, // Always start thread if queue is not 
								      // empty and max threads has not been reached.
								CapsdConfigFactory.getInstance().getMaxRescanThreadPoolSize());
								
		// Create the rescan scheduler
		//
		if(log.isDebugEnabled())
		{
			log.debug("init: Creating rescan scheduler");
		}
		try
		{
			// During instantiation, the scheduler will load the
			// list of known nodes from the database
			m_scheduler = new Scheduler(m_rescanRunner.getRunQueue());
		}
		catch(SQLException sqlE)
		{
			log.error("Failed to initialize the rescan scheduler.", sqlE);
			throw new UndeclaredThrowableException(sqlE);
		}
		catch(Throwable t)
		{
			log.error("Failed to initialize the rescan scheduler.", t);
			throw new UndeclaredThrowableException(t);
		}
		
		// Create an event receiver. 
		//
		try
		{
			if(log.isDebugEnabled())
			{
				log.debug("init: Creating event broadcast event receiver");
			}

			m_receiver = new BroadcastEventProcessor(m_suspectRunner.getRunQueue(), m_scheduler);
		}
		catch(Throwable t)
		{
			log.error("Failed to initialized the broadcast event receiver", t);
			throw new UndeclaredThrowableException(t);
		}
	}
	/**
	 * Start the Capsd threads.
	 */
	public void start()
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log = ThreadCategory.getInstance();

		m_status = STARTING;
		
		// Start the suspect event and rescan thread pools
		//
		if(log.isDebugEnabled())
		{
			log.debug("start: Starting runnable thread pools...");
		}
		m_suspectRunner.start();
		m_rescanRunner.start();
		
		// Start the rescan scheduler
		//
		if(log.isDebugEnabled())
		{
			log.debug("start: Starting rescan scheduler");
		}
		m_scheduler.start();
		
		m_status = RUNNING;
	}
	
	public void pause()
	{
		if(m_status != RUNNING)
			return;

		m_status = PAUSE_PENDING;

		Category log = ThreadCategory.getInstance();

		// TBD - Pause all threads
		
		m_status = PAUSED;

		if(log.isDebugEnabled())
		{
			log.debug("pause: Finished pausing all threads");
		}
	}
	
	public void resume()
	{
		if(m_status != PAUSED)
		{
			return;
		}

		m_status = RESUME_PENDING;

		Category log = ThreadCategory.getInstance();

		// TBD - Resume all threads
		
		m_status = RUNNING;

		if(log.isDebugEnabled())
		{
			log.debug("pause: Finished resuming all threads");
		}
	}
	
	/**
	 * Returns a name/id for this process
	 */
	public String getName()
	{
		return "OpenNMS.Capsd";
	}
	
	/**
	 * Returns the current status
	 */
	public synchronized int getStatus()
	{
		return m_status;
	}
	
	/** 
	 * Used to retrieve the local host name/address.  The name/address
	 * of the machine on which Capsd is running.
	 */
	public static String getLocalHostAddress()
	{
		return m_address;
	}

	public static Capsd getInstance()
	{
		return m_singleton;
	}
	
	static Object getDbSyncLock()
	{
		return m_dbSyncLock;
	}

        /**
	 * This method is used by other managed beans to forward an IP Address
	 * for capability scanning. The If the interface converts properly then
	 * it is scanned as a suspect interface for the discovery of all the
	 * services and other interfaces that exists on the node.
	 *
	 * @param ifAddr	The address of the suspect interface.
	 *
	 * @throws java.net.UnknownHostException Thrown if the address cannot
	 * 	be converted to aa proper internet address.
         */
	public void scanSuspectInterface(String ifAddr)
		throws UnknownHostException
	{
		String prefix = ThreadCategory.getPrefix();
		try
		{
			ThreadCategory.setPrefix(LOG4J_CATEGORY);
			InetAddress addr = InetAddress.getByName(ifAddr);
			SuspectEventProcessor proc = new SuspectEventProcessor(addr.getHostAddress());
			proc.run();
		}
		finally
		{
			ThreadCategory.setPrefix(prefix);
		}
	}

	/**
	 * This method is used to force an existing node to be capability rescaned.
	 * The main reason for its existance is as a hook for JMX managed beans
	 * to invoke forced rescans allowing the main rescan logic to remain in
	 * the capsd agent.
	 *
	 * @param nodeId	The node identifier from the database.
	 */
	public void rescanInterfaceParent(Integer nodeId)
	{
		String prefix = ThreadCategory.getPrefix();
		try
		{
			ThreadCategory.setPrefix(LOG4J_CATEGORY);
			m_scheduler.forceRescan(nodeId.intValue());
		}
		finally
		{
			ThreadCategory.setPrefix(prefix);
		}
	}
	
} // end Capsd class

