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
//
//
package org.opennms.netmgt.poller;

import java.lang.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Priority;
import org.apache.log4j.Category;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.scheduler.Scheduler;

import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;


// castor classes generated from the poller-configuration.xsd
import org.opennms.netmgt.config.poller.*;

public final class Poller
	implements PausableFiber
{
	private final static String LOG4J_CATEGORY	= "OpenNMS.Pollers";

	private final static String SQL_RETRIEVE_INTERFACES = "SELECT nodeid,ipaddr FROM ifServices, service WHERE ifServices.serviceid = service.serviceid AND service.servicename = ? AND ifServices.status='A'";

	private final static String SQL_RETRIEVE_SERVICE_IDS = "SELECT serviceid,servicename  FROM service";
	
	private final static String SQL_RETRIEVE_SERVICE_STATUS = "SELECT ifregainedservice,iflostservice FROM outages WHERE nodeid = ? AND ipaddr = ? AND serviceid = ? AND iflostservice = (SELECT max(iflostservice) FROM outages WHERE nodeid = ? AND ipaddr = ? AND serviceid = ?)";

	/**
	 * Integer constant for passing in to PollableNode.getNodeLock() method
	 * in order to indicate that the method should block until node lock is 
	 * available.
	 */
	private static int WAIT_FOREVER = 0;

	private final static Poller 	m_singleton	= new Poller();

	/** 
	 * Holds map of service names to service identifiers
	 */
	private final static Map 	m_serviceIds 	= new HashMap();
	
	/**
	 * List of all PollableService objects.
	 */
	private List			m_pollableServices;
	
	private Scheduler		m_scheduler;

	private int			m_status;

	private BroadcastEventProcessor	m_receiver;

	/**
	 * Map of 'PollableNode' objects keyed by nodeId
	*/
	private Map 			m_pollableNodes;
		
	/**  
	 * Map of all available 'ServiceMonitor' objects indexed by 
	 * service name
		*/
	private static Map 		m_svcMonitors;
	
	private Poller()
	{
		m_scheduler = null;
		m_status    = START_PENDING;
		m_svcMonitors = Collections.synchronizedMap(new TreeMap());
		m_pollableServices = Collections.synchronizedList(new LinkedList());
		m_pollableNodes = Collections.synchronizedMap(new HashMap());
	}

	public synchronized void init()
	{
		// Set the category prefix
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		
		// get the category logger 
		Category log = ThreadCategory.getInstance();

		// Load up the configuration for the 
		// poller(s). Use this to start everything
		//
		try
		{
			PollerConfigFactory.reload();
		}
		catch(MarshalException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load poller configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load poller configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load poller configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		// Load up the configuration for the poll outages. 
		//
		try
		{
			PollOutagesConfigFactory.reload();
		}
		catch(MarshalException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load poll-outage configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load poll-outage configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load poll-outage configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		
		// Initialize the Capsd configuration factory 
		// Necessary for testing if an interface is managed/unmanaged
		//
		try
		{
			CapsdConfigFactory.reload();
		}
		catch(MarshalException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load Capsd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		if (log.isDebugEnabled())
			log.debug("start: Testing database connection");

		// Make sure we can connect to the database and load
		// the services table so we can easily convert from
		// service name to service id
		//
		java.sql.Connection ctest = null;
		ResultSet rs = null;
		try
		{
			DatabaseConnectionFactory.reload();
			ctest = DatabaseConnectionFactory.getInstance().getConnection();
			
			PreparedStatement loadStmt= ctest.prepareStatement(SQL_RETRIEVE_SERVICE_IDS);
			
			// go ahead and load the service table 
			//
			rs = loadStmt.executeQuery();
			while(rs.next())
			{
				Integer id = new Integer(rs.getInt(1));
				String name = rs.getString(2);

				m_serviceIds.put(name, id);
			}
		}
		catch (IOException iE)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: IOException getting database connection", iE);
			throw new UndeclaredThrowableException(iE);
		}
		catch (MarshalException mE)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Marshall Exception getting database connection", mE);
			throw new UndeclaredThrowableException(mE);
		}
		catch (ValidationException vE)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Validation Exception getting database connection", vE);
			throw new UndeclaredThrowableException(vE);
		}
		catch (SQLException sqlE)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Error accessing database.", sqlE);
			throw new UndeclaredThrowableException(sqlE);
		}
		catch (ClassNotFoundException cnfE)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Error accessing database.", cnfE);
			throw new UndeclaredThrowableException(cnfE);
		}
		finally
		{
			if (rs != null)
			{
				try
				{
					rs.close();
				}
				catch(Exception e)
				{
					if(log.isInfoEnabled())
						log.info("start: an error occured closing the result set", e);
				}
			}
			if(ctest != null)
			{
				try
				{
					ctest.close();
				}
				catch(Exception e)
				{
					if(log.isInfoEnabled())
						log.info("start: an error occured closing the SQL connection", e);
				}
			}
		}

		// serviceUnresponsive behavior enabled/disabled?
		if (log.isDebugEnabled())
		{
			if (PollerConfigFactory.getInstance().serviceUnresponsiveEnabled())
			{
				log.debug("start: serviceUnresponsive behavior: enabled");
			}
			else
			{
				log.debug("start: serviceUnresponsive behavior: disabled");
			}
		}
		
		// Load up an instance of each monitor from the config
		// so that the event processor will have them for
		// new incomming events to create pollable service objects.
		//
		if (log.isDebugEnabled())
			log.debug("start: Loading monitors");
		PollerConfiguration pConfig = PollerConfigFactory.getInstance().getConfiguration();
		Enumeration eiter = pConfig.enumerateMonitor();
		while(eiter.hasMoreElements())
		{
			Monitor monitor = (Monitor)eiter.nextElement();
			try
			{
				if(log.isDebugEnabled())
				{
					log.debug("start: Loading monitor "
						  + monitor.getService()
						  + ", classname "
						  + monitor.getClassName());
				}
				Class mc = Class.forName(monitor.getClassName());
				ServiceMonitor sm = (ServiceMonitor)mc.newInstance();
				
				// Attempt to initialize the service monitor
				//
				Map properties = null; // properties not currently used
				sm.initialize(properties);
				
				m_svcMonitors.put(monitor.getService(), sm);
			}
			catch(Throwable t)
			{
				if(log.isEnabledFor(Priority.WARN))
				{
					log.warn("start: Failed to load monitor " + monitor.getClassName()
						 + " for service " + monitor.getService(), t);
				}
			}
		}

		// Create a scheduler
		//
		try
		{
			if(log.isDebugEnabled())
				log.debug("start: Creating poller scheduler");

			m_scheduler = new Scheduler("Poller", pConfig.getThreads());	
		}
		catch(RuntimeException e)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to create poller scheduler", e);
			throw e;
		}

		if(log.isDebugEnabled())
			log.debug("start: Scheduling existing interfaces");

		// Schedule the interfaces currently in the database
		//
		try
		{
			scheduleExistingInterfaces();
		}
		catch(SQLException sqlE)
		{
			if(log.isEnabledFor(Priority.ERROR))
				log.error("start: Failed to schedule existing interfaces", sqlE);
		}
		
		// Create an event receiver. The receiver will
		// receive events, process them, creates network
		// interfaces, and schedulers them.
		//
		try
		{
			if(log.isDebugEnabled())
				log.debug("start: Creating event broadcast event processor");

			m_receiver = new BroadcastEventProcessor(m_pollableServices);
		}
		catch(Throwable t)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to initialized the broadcast event receiver", t);

			throw new UndeclaredThrowableException(t);
		}
	}
	
	public synchronized void start()
	{
		m_status = STARTING;

		// Set the category prefix
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		
		// get the category logger 
		Category log = ThreadCategory.getInstance();

		if (log.isDebugEnabled())
			log.debug("start: Initializing PollerConfigFactory");

		// start the scheduler
		//
		try
		{
			if(log.isDebugEnabled())
				log.debug("start: Starting poller scheduler");

			m_scheduler.start();
		}
		catch(RuntimeException e)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to start scheduler", e);
			throw e;
		}

		// Set the status of the service as running.
		//
		m_status = RUNNING;

		if (log.isDebugEnabled())
			log.debug("start: Poller running");
	}
	
	public synchronized void stop()
	{
		m_status    = STOP_PENDING;
		m_scheduler.stop();
		m_receiver.close();

		m_scheduler = null;
		m_status    = STOPPED;
		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("stop: Poller stopped");
	}

	public synchronized void pause()
	{
		if(m_status != RUNNING)
			return;

		m_status = PAUSE_PENDING;
		m_scheduler.pause();
		m_status = PAUSED;

		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("pause: Poller paused");
	}

	public synchronized void resume()
	{
		if(m_status != PAUSED)
			return;

		m_status = RESUME_PENDING;
		m_scheduler.resume();
		m_status = RUNNING;

		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("resume: Poller resumed");
	}

	public synchronized int getStatus()
	{
		return m_status;
	}

	public String getName()
	{
		return "OpenNMS.Poller";
	}

	public static Poller getInstance()
	{
		return m_singleton;
	}
	
	public Scheduler getScheduler()
	{
		return m_scheduler;
	}
	
	public ServiceMonitor getServiceMonitor(String svcName)
	{
		return (ServiceMonitor)m_svcMonitors.get(svcName);
	}
	
	public List getPollableServiceList()
	{
		return m_pollableServices;
	}
	
	public PollableNode getNode(int nodeId)
	{
		Integer key = new Integer(nodeId);
		boolean nodeInMap = m_pollableNodes.containsKey(key);
		
		if (nodeInMap)
		{
			return (PollableNode)m_pollableNodes.get(key);
		}
		else
		{
			return null;
		}
	}
	
	public void addNode(PollableNode pNode)
	{
		m_pollableNodes.put(new Integer(pNode.getNodeId()), pNode);
		Category log = ThreadCategory.getInstance();
		log.debug("Poller.addNode: adding pollable node with id: " + pNode.getNodeId() + " new size: " + m_pollableNodes.size());
	}
	
	public void removeNode(int nodeId)
	{
		m_pollableNodes.remove(new Integer(nodeId));
	}
	
	private void scheduleExistingInterfaces()
		throws SQLException
	{
		// get the category logger 
		//
		Category log = ThreadCategory.getInstance();
		
		PollerConfigFactory pollerCfgFactory = PollerConfigFactory.getInstance();

		// Database connection
		java.sql.Connection dbConn = null;

		PreparedStatement stmt = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			stmt = dbConn.prepareStatement(SQL_RETRIEVE_INTERFACES);
		
			// Loop through loaded monitors and schedule for each one present
			//
			Set svcNames = m_svcMonitors.keySet();
			Iterator i = svcNames.iterator();
			while (i.hasNext())
			{
				String svcName = (String)i.next();
				ServiceMonitor monitor = (ServiceMonitor)m_svcMonitors.get(svcName);
				
				if(log.isDebugEnabled())
					log.debug("scheduleExistingInterfaces: Scheduling existing interfaces for monitor: " + svcName);

				// Retrieve list of interfaces from the database which 
				// support the service polled by this monitor
				//
				try
				{
					if(log.isDebugEnabled())
						log.debug("scheduleExistingInterfaces: dbConn = " + dbConn + ", svcName = " + svcName);

					stmt.setString(1, svcName);    	// Service name
					ResultSet rs = stmt.executeQuery();

					// Iterate over result set and schedule each interface/service
					// pair which passes the criteria
					//
					while (rs.next())
					{
						int nodeId = rs.getInt(1);
						String ipAddress = rs.getString(2);

						PollerConfiguration pConfig = pollerCfgFactory.getConfiguration();
						Enumeration epkgs = pConfig.enumeratePackage();
						
						// Compare interface/service pair against each poller package
						// For each match, create new PollableService object and 
						// schedule it for polling.
						//
						while(epkgs.hasMoreElements())
						{
							org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package)epkgs.nextElement();

							// Make certain the the current service is in the package
							// and enabled!
							//
							if (!pollerCfgFactory.serviceInPackageAndEnabled(svcName, pkg))
							{
								if (log.isDebugEnabled())
									log.debug("scheduleExistingInterfaces: address/service: " + 
											ipAddress + "/" + svcName + 
											" not scheduled, service is not enabled or does not exist in package: " + 
											pkg.getName());
								continue;
							}

							// Is the interface in the package?
							//
							if (!pollerCfgFactory.interfaceInPackage(ipAddress, pkg))
							{
								if (log.isDebugEnabled())
									log.debug("scheduleExistingInterfaces: address/service: " + 
											ipAddress + "/" + svcName + 
											" not scheduled, interface does not belong to package: " + 
											pkg.getName());
								continue;
							}
							
							//
							// getServiceLostDate() method will return the date a service 
							// was lost if the service was last known to be unavailable or
							// will return null if the service was last known to be 
							// available...based on outage information on the 'outages' table.
							Date svcLostDate = getServiceLostDate(dbConn, nodeId, ipAddress, svcName);
							int lastKnownStatus = -1;
							if (svcLostDate != null)
							{
								lastKnownStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
								if (log.isDebugEnabled())
									log.debug("scheduleExistingInterfaces: address= " + ipAddress + 
											" svc= " + svcName + 
											" lastKnownStatus= unavailable" + 
											" svcLostDate= " + svcLostDate);
							}
							else
							{
								lastKnownStatus = ServiceMonitor.SERVICE_AVAILABLE;
								if (log.isDebugEnabled())
									log.debug("scheduleExistingInterfaces: address= " + ipAddress + 
											" svc= " + svcName + " lastKnownStatus= available");
							}
							
							// Criteria checks have all been padded...update Node Outage 
							// Hierarchy and create new service for polling
							//
							PollableNode pNode = null;
							PollableInterface pInterface = null;
							PollableService pSvc = null;
							boolean ownLock = false;
							boolean nodeCreated = false;
							boolean interfaceCreated = false;
							
							try
							{
								// Does the node already exist in the poller's pollable node map?
								//
								pNode = this.getNode(nodeId);
								if (pNode == null)
								{
									// Nope...so we need to create it
									pNode = new PollableNode(nodeId);
									nodeCreated = true;
								}
								else
								{
									// Obtain node lock
									//
									ownLock = pNode.getNodeLock(WAIT_FOREVER);
								}
								
								// Does the interface exist in the pollable node?
								//
								pInterface = pNode.getInterface(ipAddress);
								if (pInterface == null)
								{
									// Create the PollableInterface
									pInterface = new PollableInterface(pNode, InetAddress.getByName(ipAddress));
									interfaceCreated = true;
								}

								// Create a new PollableService representing this node, interface,
								// service and package pairing
								//
								pSvc = new PollableService(pInterface,
												svcName,
												pkg,
												lastKnownStatus,
												svcLostDate);

								// Initialize the service monitor with the pollable service 
								//
								monitor.initialize(pSvc);
				
								// Add new service to the pollable services list.
								//
								m_pollableServices.add(pSvc);
								
								// Add the service to the PollableInterface object
								//
								// WARNING:  The PollableInterface stores services in a map
								//           keyed by service name, therefore, only the LAST
								//           PollableService aded to the interface for a 
								//           particular service will be represented in the
								//           map.  THIS IS BY DESIGN.
								//
								// NOTE:     addService() calls recalculateStatus() on the interface
								log.debug("scheduleExistingInterfaces: adding pollable service to service list of interface: " + ipAddress);
								pInterface.addService(pSvc);
									
								if (interfaceCreated)
								{
									// Add the interface to the node
									//
									// NOTE:  addInterface() calls recalculateStatus() on the node
									if (log.isDebugEnabled())
										log.debug("scheduleExistingInterfaces: adding new pollable interface "
												 + ipAddress + " to pollable node " + nodeId);
									pNode.addInterface(pInterface);
								}
								else
								{
									// Recalculate node status
									//
									pNode.recalculateStatus();
								}
								
								if (nodeCreated)
								{
									// Add the node to the node map
									//
									if (log.isDebugEnabled())
										log.debug("scheduleExistingInterfaces: adding new pollable node: " + 
												nodeId);
									this.addNode(pNode);
								}
								
								// Schedule the service
								//
								m_scheduler.schedule(pSvc, pSvc.recalculateInterval());
							} 
							catch(UnknownHostException ex)
							{
								log.error("scheduleExistingInterfaces: Failed to schedule interface " + ipAddress + 
												" for service monitor " + svcName + ", illegal address", ex);
							}
							catch(InterruptedException ie)
							{
								log.error("scheduleExistingInterfaces: Failed to schedule interface " + ipAddress + 
												" for service monitor " + svcName + ", thread interrupted", ie);
							}
							catch(RuntimeException rE)
							{
								log.warn("scheduleExistingInterfaces: Unable to schedule " + 
										ipAddress + " for service monitor " + svcName + 
										", reason: " + rE.getMessage());
							}
							catch(Throwable t)
							{
								log.error("scheduleExistingInterfaces: Uncaught exception, failed to schedule interface "
										+ ipAddress + " for service monitor " + svcName, t);
							}
							finally
							{
								if (ownLock)
								{
									try
									{
										pNode.releaseNodeLock();
									}
									catch (InterruptedException iE)
									{
										log.error("scheduleExistingInterfaces: Failed to release node lock on nodeid " + 
												pNode.getNodeId() + ", thread interrupted.");
									}
								}
								
							}
						} // end while more packages exist
					} // end while more interfaces in result set

					rs.close();
				}
				catch (SQLException sqle)
				{
					log.warn("scheduleExistingInterfaces: SQL exception while querying ipInterface table", sqle);
					throw sqle;
				}
			} // end while more service monitors exist

			// Debug dump pollable node map content
			//
			if (log.isDebugEnabled())
			{
				log.debug("scheduleExistingInterfaces: dumping content of pollable node map: " );
				Iterator j = m_pollableNodes.values().iterator();
				while (j.hasNext())
				{
					PollableNode pNode = (PollableNode)j.next();
					log.debug("	nodeid=" + pNode.getNodeId() + " status=" + Pollable.statusType[pNode.getStatus()] );
					Iterator k = pNode.getInterfaces().iterator();
					while(k.hasNext())
					{
						PollableInterface pIf = (PollableInterface)k.next();
						log.debug("		interface=" + pIf.getAddress().getHostAddress() + " status=" + Pollable.statusType[pIf.getStatus()]);

						Iterator s = pIf.getServices().iterator();
						while(s.hasNext())
						{
							PollableService pSvc = (PollableService)s.next();
							log.debug("			service=" + pSvc.getServiceName() + " status=" + Pollable.statusType[pSvc.getStatus()]);
						}
					}
				}
			}
		}
		finally
		{
			if (stmt != null)
			{
				try
				{
					stmt.close();
				}
				catch (Exception e)
				{
					if(log.isDebugEnabled())
						log.debug("scheduleExistingInterfaces: an exception occured closing the SQL statement", e);
				}
			}

			if(dbConn != null)
			{
				try
				{
					dbConn.close();
				}
				catch(Throwable t)
				{
					if(log.isDebugEnabled())
						log.debug("scheduleExistingInterfaces: an exception occured closing the SQL connection", t);
				}
			}
		}
	}
	
	/**
	 * Determines the last known status of a ipaddr/service pair based on outage information
	 * from the 'outages' table and if the last known status is UNAVAILABLE returns a 
	 * date object representing when the service was lost.  If last known status is 
	 * AVAILABLE null is returned.
	 *
	 * @param dbConn	Database connection
	 * @param nodeId	Node identifier
	 * @param ipAddr	IP address
	 * @param svcName	service name
	 * 
	 * @return Date object representing the time the service was lost if the service is 
	 * UNAVAILABLE or null if the service is AVAILABLE.
	 */
	public static final Date getServiceLostDate(Connection dbConn, int nodeId, String ipAddr, String svcName)
	{
		Category log = ThreadCategory.getInstance(Poller.class);
		log.debug("getting last known status for address: " + ipAddr + " service: " + svcName);
		
		Date svcLostDate = null;
		// Convert service name to service identifier
		//
		Integer temp = (Integer)m_serviceIds.get(svcName);
		int serviceId = -1;
		if (temp != null)
			serviceId = temp.intValue();
		else
		{
			log.warn("Failed to retrieve service identifier for interface " + ipAddr + " and service '" + svcName + "'");
			return svcLostDate;
		}
		
		ResultSet outagesResult = null;
		Timestamp regainedDate = null;
		Timestamp lostDate = null;
			
		try
		{
			//get the outage information for this service on this ip address
			PreparedStatement outagesQuery = dbConn.prepareStatement(SQL_RETRIEVE_SERVICE_STATUS);
			
			//add the values for the main query
			outagesQuery.setInt(1, nodeId);
			outagesQuery.setString(2, ipAddr);
			outagesQuery.setInt(3, serviceId);
			
			//add the values for the subquery
			outagesQuery.setInt(4, nodeId);
			outagesQuery.setString(5, ipAddr);
			outagesQuery.setInt(6, serviceId);
			
			outagesResult = outagesQuery.executeQuery();
			
			//if there was a result then the service has been down before,
			if (outagesResult.next())
			{
				regainedDate = outagesResult.getTimestamp(1);
				lostDate  = outagesResult.getTimestamp(2);
				log.debug("getServiceLastKnownStatus: lostDate: " + lostDate);
			}
			//the service has never been down, need to use current date for both
			else
			{
				Date currentDate = new Date(System.currentTimeMillis());
				regainedDate = new Timestamp(currentDate.getTime());
				lostDate = lostDate = new Timestamp(currentDate.getTime());
			}
		} 
		catch (SQLException sqlE)
		{
			log.error("SQL exception while retrieving last known service status for " + ipAddr + "/" + svcName);
		}
		finally
		{
			if (outagesResult != null)
			{
				try
				{
					outagesResult.close();
				}
				catch (SQLException slqE)
				{
					// Do nothing
				}
			}
		}
		
		// Now use retrieved outage times to determine current status
		// of the service.  If there was an error and we were unable
		// to retrieve the outage times the default of AVAILABLE will
		// be returned.
		//
		if (lostDate != null)
		{
			// If the service was never regained then simply 
			// assign the svc lost date.
			if (regainedDate == null)
			{				
				svcLostDate = new Date(lostDate.getTime());
				log.debug("getServiceLastKnownStatus: svcLostDate: " + svcLostDate);
			}
		}
		
		return svcLostDate;
	}
}
