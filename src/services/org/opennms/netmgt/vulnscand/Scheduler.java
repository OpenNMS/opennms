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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.vulnscand;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Category;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.VulnscandConfigFactory;
import org.opennms.netmgt.config.vulnscand.ScanLevel;
import org.opennms.netmgt.config.vulnscand.VulnscandConfiguration;

/**
* This class implements a simple scheduler to ensure
* that Vulnscand rescans occurs at the expected intervals.
*
* @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
* @author <a href="http://www.opennms.org/">OpenNMS</a>
*
*/
final class Scheduler
	implements Runnable, PausableFiber
{
	/**
	* The prefix for the fiber name.
	*/
	private static final String	FIBER_NAME 		= "Vulnscand Scheduler";

	/**
	 * The SQL statement used to retrieve all non-deleted/non-forced unamanaged IP interfaces
	 * from the 'ipInterface' table.
	 */
	private static final String	SQL_DB_RETRIEVE_IP_INTERFACE =
		"SELECT ipaddr FROM ipinterface WHERE ipaddr!='0.0.0.0' AND isManaged!='D' AND isManaged!='F'";

	/**
	* SQL used to retrieve the last poll time for all the
	* managed interfaces belonging to a particular node.
	*/
	private static final String	SQL_GET_LAST_POLL_TIME	=
		"SELECT lastAttemptTime FROM vulnerabilities WHERE ipaddr=? ORDER BY lastAttemptTime DESC";

	/**
	* The name of this fiber.
	*/
	private String	m_name;

	/**
	* The status for this fiber.
	*/
	private int	m_status;

	/**
	* The worker thread that executes this instance.
	*/
	private Thread	m_worker;

	/**
	* List of NessusScanConfiguration objects representing the IP addresses
	* that will be scheduled.
	*/
	private List	m_knownAddresses;

	/**
	* The configured interval (in milliseconds) between rescans
	*/
	private long	m_interval;

	/**
	* The configured initial sleep (in milliseconds) prior to scheduling rescans
	*/
	private long	m_initialSleep;

	/**
	* The rescan queue where new NessusScan objects are
	* enqueued for execution.
	*/
	private FifoQueue m_scheduledScanQ;

	/**
	* Constructs a new instance of the scheduler.
	*
	*/
	Scheduler(FifoQueue rescanQ)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance(Scheduler.class);

		m_scheduledScanQ = rescanQ;

		m_name = FIBER_NAME;
		m_status = START_PENDING;
		m_worker = null;

		m_knownAddresses = Collections.synchronizedList(new LinkedList());

		// Get rescan interval from configuration factory
		//
		m_interval = VulnscandConfigFactory.getInstance().getRescanFrequency();
		if (log.isDebugEnabled())
			log.debug("Scheduler: rescan interval(millis): " + m_interval);

		// Get initial rescan sleep time from configuration factory
		//
		m_initialSleep = VulnscandConfigFactory.getInstance().getInitialSleepTime();
		if (log.isDebugEnabled())
			log.debug("Scheduler: initial rescan sleep time(millis): " + m_initialSleep);

		// Load the list of IP addresses from the config file and schedule
		// them in the appropriate level

		VulnscandConfigFactory configFactory = VulnscandConfigFactory.getInstance();
		VulnscandConfiguration config = VulnscandConfigFactory.getConfiguration();

		// If the status of the daemon is "true" (meaning "on")...
		if (config.getStatus())
		{
			Enumeration scanLevels = config.enumerateScanLevel();

			while(scanLevels.hasMoreElements())
			{
				ScanLevel scanLevel = (ScanLevel)scanLevels.nextElement();
				int level = scanLevel.getLevel();

				// Grab the list of included addresses for this level
				Set levelAddresses = configFactory.getAllIpAddresses(scanLevel);

				// If scanning of the managed IPs is enabled...
				if (configFactory.getManagedInterfacesStatus())
				{
					// And the managed IPs are set to be scanned at the current level...
					if (configFactory.getManagedInterfacesScanLevel() == level)
					{
						// Then schedule those puppies to be scanned
						levelAddresses.add(getAllManagedInterfaces());
						log.info("Scheduled the managed interfaces at scan level " + level + ".");
					}
				}

				// Remove all of the excluded addresses (the excluded
				// addresses are cached, so this operation is lighter
				// than constructing the exclusion list each time)
				levelAddresses.removeAll(configFactory.getAllExcludes());

				log.info("Adding " + levelAddresses.size() + " addresses to the vulnerability scan scheduler.");

				Iterator itr = levelAddresses.iterator();
 				while (itr.hasNext())
 				{
 					Object next = itr.next();
 					String nextAddress = null;
 
 					if(next instanceof String) { nextAddress = (String)next;}
 					try
					{
						addToKnownAddresses(InetAddress.getByName(nextAddress), level);
					}
					catch (UnknownHostException ex)
					{
						log.error("Could not add invalid address to schedule: " + nextAddress, ex);
					}
				}
			}
		}
		else
		{
			log.info("Vulnerability scanning is DISABLED.");
		}
	}

	private Set getAllManagedInterfaces()
	{
		Category log = ThreadCategory.getInstance(Scheduler.class);

		Set retval = new TreeSet();
		String addressString = null;

		Connection connection = null;
		Statement selectInterfaces = null;
		ResultSet interfaces = null;
		try
		{
			connection = DatabaseConnectionFactory.getInstance().getConnection();
			selectInterfaces = connection.createStatement();
			interfaces = selectInterfaces.executeQuery(SQL_DB_RETRIEVE_IP_INTERFACE);

			int i = 0;
			while (interfaces.next())
			{
				addressString = interfaces.getString(1);
				if (addressString != null)
				{
					retval.add(addressString);
				}
				else
				{
					log.warn("UNEXPECTED CONDITION: NULL string in the results of the query for managed interfaces from the ipinterface table.");
				}

				i++;
			}
			log.info("Loaded " + i + " managed interfaces from the database.");
		}
		catch (SQLException ex)
		{
			log.error(ex.getLocalizedMessage(), ex);
		}
		finally
		{
			try
			{
				if (interfaces != null) interfaces.close();
				if (selectInterfaces != null) selectInterfaces.close();
			}
			catch (Exception ex) { }
            finally {
                try { if (connection != null) connection.close(); } catch (Exception e) {}
            }
		}
		return retval;
	}

	/**
	* Creates a NessusScanConfiguration object representing the specified node and
	* adds it to the known node list for scheduling.
	*
	* @param address 	the internet address.
	* @param scanLevel 	the scan level.
	*
	* @throws SQLException if there is any problem accessing the database
	*/
	void addToKnownAddresses(InetAddress address, int scanLevel)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance(getClass());

		// Retrieve last poll time for the node from the ipInterface
		// table.
		Connection db = null;
		try
		{
			db = DatabaseConnectionFactory.getInstance().getConnection();
			PreparedStatement ifStmt = db.prepareStatement(SQL_GET_LAST_POLL_TIME);
			ifStmt.setString(1, address.getHostAddress());
			ResultSet rset = ifStmt.executeQuery();
			if (rset.next())
			{
				Timestamp lastPolled = rset.getTimestamp(1);
				if(lastPolled != null && rset.wasNull() == false)
				{
					if (log.isDebugEnabled())
						log.debug("scheduleAddress: adding node " + address.toString() + " with last poll time " + lastPolled);
					m_knownAddresses.add(new NessusScanConfiguration(address, scanLevel, lastPolled, m_interval));
				}
			}
			else
			{
				if (log.isDebugEnabled())
					log.debug("scheduleAddress: adding ipAddr " + address.toString() + " with no previous poll");
				m_knownAddresses.add(new NessusScanConfiguration(address, scanLevel, new Timestamp(0), m_interval));
			}
		}
		finally
		{
			if(db != null)
			{
				try { db.close(); }
				catch(Exception e) { }
			}
		}
	}

	/**
	* Removes the specified node from the known node list.
	*
	* @param address	Address of interface to be removed.
	*/
	void unscheduleAddress(InetAddress address)
	{
		synchronized(m_knownAddresses)
		{
			Iterator iter = m_knownAddresses.iterator();
			while(iter.hasNext())
			{
				NessusScanConfiguration addressInfo = (NessusScanConfiguration)iter.next();
				if (addressInfo.getAddress() == address)
				{
					ThreadCategory.getInstance(getClass()).debug("unscheduleAddress: removing node " + address + " from the scheduler.");
					m_knownAddresses.remove(addressInfo);
					break;
				}
			}
		}
	}

	/**
	* Starts the fiber.
	*
	* @throws java.lang.IllegalStateException Thrown if the fiber is
	* 	already running.
	*/
	public synchronized void start()
	{
		if(m_worker != null)
			throw new IllegalStateException("The fiber has already run or is running");

		Category log = ThreadCategory.getInstance(getClass());

		m_worker = new Thread(this, getName());
		m_worker.start();
		m_status = STARTING;

		log.debug("Scheduler.start: scheduler started");
	}

	/**
	* Stops the fiber. If the fiber has never been run
	* then an exception is generated.
	*
	* @throws java.lang.IllegalStateException Throws if the fiber
	* 	has never been started.
	*/
	public synchronized void stop()
	{
		if(m_worker == null)
			throw new IllegalStateException("The fiber has never been started");

		Category log = ThreadCategory.getInstance(getClass());

		m_status = STOP_PENDING;
		m_worker.interrupt();

		log.debug("Scheduler.stop: scheduler stopped");
	}

	/**
	* Pauses the scheduler if it is current running. If the fiber
	* has not been run or has already stopped then an exception
	* is generated.
	*
	* @throws java.lang.IllegalStateException Throws if the operation could
	* 	not be completed due to the fiber's state.
	*/
	public synchronized void pause()
	{
		if(m_worker == null)
			throw new IllegalStateException("The fiber has never been started");

		if(m_status == STOPPED || m_status == STOP_PENDING)
			throw new IllegalStateException("The fiber is not running or a stop is pending");

		if(m_status == PAUSED)
			return;

		m_status = PAUSE_PENDING;
		notifyAll();
	}

	/**
	* Resumes the scheduler if it has been paused. If the fiber
	* has not been run or has already stopped then an exception
	* is generated.
	*
	* @throws java.lang.IllegalStateException Throws if the operation could
	* 	not be completed due to the fiber's state.
	*/
	public synchronized void resume()
	{
		if(m_worker == null)
			throw new IllegalStateException("The fiber has never been started");

		if(m_status == STOPPED || m_status == STOP_PENDING)
			throw new IllegalStateException("The fiber is not running or a stop is pending");

		if(m_status == RUNNING)
			return;

		m_status = RESUME_PENDING;
		notifyAll();
	}

	/**
	* Returns the current of this fiber.
	*
	* @return The current status.
	*/
	public synchronized int getStatus()
	{
		if(m_worker != null && m_worker.isAlive() == false)
			m_status = STOPPED;
		return m_status;
	}

	/**
	* Returns the name of this fiber.
	*
	*/
	public String getName()
	{
		return FIBER_NAME;
	}

	/**
	* The main method of the scheduler. This method is responsible
	* for checking the runnable queues for ready objects and
	* then enqueuing them into the thread pool for execution.
	*
	*/
	public void run()
	{
		Category log = ThreadCategory.getInstance(getClass());

		synchronized(this)
		{
			m_status = RUNNING;
		}
		log.debug("Scheduler.run: scheduler running");

		// Loop until a fatal exception occurs or until
		// the thread is interrupted.
		//
		boolean firstPass = true;
		while(true)
		{
			// Status check
			//
			synchronized(this)
			{
				if(m_status != RUNNING		 &&
				m_status != PAUSED		 &&
				m_status != PAUSE_PENDING	 &&
				m_status != RESUME_PENDING)
				{
					log.debug("Scheduler.run: status = " + m_status + ", time to exit");
					break;
				}
			}

			// If this is the first pass we want to pause momentarily
			// This allows the rest of the background processes to come
			// up and stabilize before we start generating events from rescans.
			//
			if (firstPass)
			{
				firstPass = false;
				synchronized(this)
				{
					try
					{
						log.debug("Scheduler.run: initial sleep configured for " + m_initialSleep + "ms...sleeping...");
						wait(m_initialSleep);
					}
					catch(InterruptedException ex)
					{
						log.debug("Scheduler.run: interrupted exception during initial sleep...exiting.");
						break; // exit for loop
					}
				}
			}

			// iterate over the known node list, add any
			// nodes ready for rescan to the rescan queue
			// for processing.
			//
			int added = 0;

			synchronized(m_knownAddresses)
			{

				log.debug("Scheduler.run: iterating over known nodes list to schedule...");
				Iterator iter = m_knownAddresses.iterator();
				while (iter.hasNext())
				{
					NessusScanConfiguration addressInfo = (NessusScanConfiguration)iter.next();

					// Don't schedule if already scheduled
					if (addressInfo.isScheduled())
						continue;

					// Don't schedule if its not time for rescan yet
					if (!addressInfo.isTimeForRescan())
						continue;

					// Must be time for a rescan!
					//
					try
					{
						addressInfo.setScheduled(true); // Mark node as scheduled

						// Create a new NessusScan object
						// and add it to the rescan queue for execution
						//
						log.debug("Scheduler.run: adding node " + addressInfo.getAddress().toString() + " to the rescan queue.");

						m_scheduledScanQ.add(new NessusScan(addressInfo));
						added++;
					}
					catch(InterruptedException ex)
					{
						log.info("Scheduler.schedule: failed to add new node to rescan queue", ex);
						throw new UndeclaredThrowableException(ex);
					}
					catch(FifoQueueException ex)
					{
						log.info("Scheduler.schedule: failed to add new node to rescan queue", ex);
						throw new UndeclaredThrowableException(ex);
					}
				}
			}

			// Wait for 60 seconds if there were no nodes
			// added to the rescan queue during this loop,
			// otherwise just start over.
			//
			synchronized(this)
			{
				if(added == 0)
				{
					try
					{
						wait(60000);
					}
					catch(InterruptedException ex)
					{
						break; // exit for loop
					}
				}
			}
		} // end while(true)

		log.debug("Scheduler.run: scheduler exiting, state = STOPPED");
		synchronized(this)
		{
			m_status = STOPPED;
		}
	} // end run
}
