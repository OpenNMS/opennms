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
// 2004 Feb 12: Rebuild the package IP list when new discovered interface
//              is scheduled.
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

package org.opennms.netmgt.threshd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.Thresholder;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public final class Threshd
	implements PausableFiber
{
	/** 
	 * Log4j category
	 */
	private final static String LOG4J_CATEGORY	= "OpenNMS.Threshd";
	
	/** 
	 * SQL used to retrieve all the interfaces which support a 
	 * particular service.
	 */
	private final static String SQL_RETRIEVE_INTERFACES = "SELECT nodeid,ipaddr FROM ifServices, service WHERE ifServices.serviceid = service.serviceid AND service.servicename = ?";

	/**
	 * SQL used to retrieve all the service id's and names from
	 * the database.
	 */
	private final static String SQL_RETRIEVE_SERVICE_IDS = "SELECT serviceid,servicename FROM service";
	
	/**
	 * Singleton instance of the Threshd class
	 */
	private final static Threshd 	m_singleton	= new Threshd();

	/** 
	 * Holds map of service names to service identifiers
	 */
	private final static Map 	m_serviceIds 	= new HashMap();
	
	/**
	 * List of all ThresholdableService objects.
	 */
	private List		m_thresholdableServices;
	
	/** 
	 * Reference to the threshd scheduler
	 */
	private Scheduler	m_scheduler;
	
	/**
	 * Indicates if all the existing interfaces have been scheduled
	 */
	private boolean m_schedulingCompleted = false;

	/**
	 * Reference to the JMS event proxy for sending events.
	 */
	private EventProxy	m_proxy;

	/** 
	 * Status of the Threshd instance.
	 */
	private int		m_status;

	/** 
	 * Reference to the event processor
	 */
	private BroadcastEventProcessor	m_receiver;

	/**
	 * Map of all available ServiceThresholder objects indexed by 
	 * service name
	 */
	private static Map 	m_svcThresholders;
	
	/**
	 * Constructor.
	 */
	private Threshd()
	{
		m_scheduler = null;
		m_status    = START_PENDING;
		m_svcThresholders = Collections.synchronizedMap(new TreeMap());
		m_thresholdableServices = Collections.synchronizedList(new LinkedList());

		m_proxy = new EventProxy() {
			public void send(Event e)
			{
				EventIpcManagerFactory.getInstance().getManager().sendNow(e);
			}
			public void send(Log l)
			{
				EventIpcManagerFactory.getInstance().getManager().sendNow(l);
			}
		};
	}

	public synchronized void init()
	{
		// Set the category prefix
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		
		// get the category logger 
		final Category log = ThreadCategory.getInstance();

		if (log.isDebugEnabled())
			log.debug("start: Initializing thresholding daemon");

		// Load threshd configuration file
		//
		try
		{
			ThreshdConfigFactory.reload();
		}
		catch(MarshalException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load threshd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load threshd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load threshd configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		// Initialize the ThresholdingConfigFactory 
		//
		try
		{
			ThresholdingConfigFactory.reload();

		}
		catch(MarshalException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load thresholding configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load thresholding configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to load thresholding configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}
		
		// Load up the configuration for the scheduled outages. 
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
		
		// Make sure we can connect to the database and load
		// the services table so we can easily convert from
		// service name to service id
		//
		if (log.isDebugEnabled())
			log.debug("start: Testing database connection");

		java.sql.Connection ctest = null;
		ResultSet rs = null;
		try
		{
			DatabaseConnectionFactory.init();
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

		if (log.isDebugEnabled())
			log.debug("start: Loading thresholders");

		// Threshd configuration
		//
		ThreshdConfigFactory cfgFactory = ThreshdConfigFactory.getInstance();
		ThreshdConfiguration config = cfgFactory.getConfiguration();
		
		// Load up an instance of each thresholder from the config
		// so that the event processor will have them for
		// new incomming events to create collectable service objects.
		//
		Enumeration eiter = config.enumerateThresholder();
		while(eiter.hasMoreElements())
		{
			Thresholder thresholder = (Thresholder)eiter.nextElement();
			try
			{
				if(log.isDebugEnabled())
				{
					log.debug("start: Loading thresholder "
						  + thresholder.getService()
						  + ", classname "
						  + thresholder.getClassName());
				}
				Class tc = Class.forName(thresholder.getClassName());
				ServiceThresholder st = (ServiceThresholder)tc.newInstance();
				
				// Attempt to initialize the service thresholder
				//
				
				// Store service name in map keyed by "svcName"
				Map properties = new HashMap(); 
				properties.put("svcName", thresholder.getService());
				
				st.initialize(properties);
				
				m_svcThresholders.put(thresholder.getService(), st);
			}
			catch(Throwable t)
			{
				if(log.isEnabledFor(Priority.WARN))
				{
					log.warn("start: Failed to load thresholder " + thresholder.getClassName()
						 + " for service " + thresholder.getService(), t);
				}
			}
		}
		
		// Create a scheduler
		//
		try
		{
			if(log.isDebugEnabled())
				log.debug("start: Creating threshd scheduler");

			m_scheduler = new Scheduler("Threshd", config.getThreads());
		}
		catch(RuntimeException e)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to create threshd scheduler", e);
			throw e;
		}

		if(log.isDebugEnabled())
			log.debug("start: Scheduling existing interfaces");


		// Schedule existing interfaces for thresholding
		//
		
		ReadyRunnable interfaceScheduler = new ReadyRunnable() {
			
			public boolean isReady() { return true; }
			
			public void run() {
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
				finally {
				    setSchedulingCompleted(true);
				}
		
			}
		};
		
		m_scheduler.schedule(interfaceScheduler, 0);
		
		// Create an event receiver. The receiver will
		// receive events, process them, creates network
		// interfaces, and schedulers them.
		//
		try
		{
			if(log.isDebugEnabled())
				log.debug("start: Creating event broadcast event processor");

			m_receiver = new BroadcastEventProcessor(m_thresholdableServices);
		}
		catch(Throwable t)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("start: Failed to initialized the broadcast event receiver", t);

			throw new UndeclaredThrowableException(t);
		}
		
		// Set the status of the service as running.
		//
		m_status = RUNNING;

		if (log.isDebugEnabled())
			log.debug("start: Threshd running");
	}
	

	/** 
	 * Responsible for starting the thresholding daemon.
	 */
	public synchronized void start()
	{
		m_status = STARTING;

		// Set the category prefix
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		
		// get the category logger 
		Category log = ThreadCategory.getInstance();

		if (log.isDebugEnabled())
			log.debug("start: Initializing thresholding daemon");

		// start the scheduler
		//
		try
		{
			if(log.isDebugEnabled())
				log.debug("start: Starting threshd scheduler");

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
			log.debug("start: Threshd running");
	}
	
	/** 
	 * Responsible for stopping the thresholding daemon.
	 */
	public synchronized void stop()
	{
		m_status    = STOP_PENDING;
		m_scheduler.stop();
		m_receiver.close();

		m_scheduler = null;
		m_status    = STOPPED;
		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("stop: Threshd stopped");
	}

	/** 
	 * Responsible for pausing the thresholding daemon.
	 */
	public synchronized void pause()
	{
		if(m_status != RUNNING)
			return;

		m_status = PAUSE_PENDING;
		m_scheduler.pause();
		m_status = PAUSED;

		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("pause: Threshd paused");
	}

	/**
	 * Responsible for resuming the thresholding daemon.
	 */
	public synchronized void resume()
	{
		if(m_status != PAUSED)
			return;

		m_status = RESUME_PENDING;
		m_scheduler.resume();
		m_status = RUNNING;

		Category log = ThreadCategory.getInstance();
		if (log.isDebugEnabled())
			log.debug("resume: Threshd resumed");
	}

	/** 
	 * Returns current status of the thresholding daemon.
	 */
	public synchronized int getStatus()
	{
		return m_status;
	}

	/**
	 * Returns the name of the thresholding daemon.
	 */
	public String getName()
	{
		return "OpenNMS.Threshd";
	}

	/** 
	 * Returns singleton instance of the thresholding daemon.
	 */
	public static Threshd getInstance()
	{
		return m_singleton;
	}
	
	/** 
	 * Returns reference to the scheduler
	 */
	public Scheduler getScheduler()
	{
		return m_scheduler;
	}
	
	/** 
	 * Returns reference to the event proxy
	 */
	public EventProxy getEventProxy()
	{
		return m_proxy;
	}
	
	/**
	 * Returns the loaded ServiceThresholder for the specified
	 * service name.
	 * 
	 * @param svcName	Service name to lookup.
	 * 
	 * @return ServiceThresholder responsible for performing 
		thresholding on the specified service.
	 */
	public ServiceThresholder getServiceThresholder(String svcName)
	{
		return (ServiceThresholder)m_svcThresholders.get(svcName);
	}
	 
	/**
	 * Schedule existing interfaces for thresholding.
	 *  
	 * @throws SQLException if database errors encountered.
	 */
	private void scheduleExistingInterfaces()
		throws SQLException
	{
		// get the category logger 
		//
		Category log = ThreadCategory.getInstance();
		
		// Database connection
		java.sql.Connection dbConn = null;

		PreparedStatement stmt = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			stmt = dbConn.prepareStatement(SQL_RETRIEVE_INTERFACES);
		
			// Loop through loaded thresholders and schedule for each one present
			//
			Set svcNames = m_svcThresholders.keySet();
			Iterator i = svcNames.iterator();
			while (i.hasNext())
			{
				String svcName = (String)i.next();
				ServiceThresholder thresholder = (ServiceThresholder)m_svcThresholders.get(svcName);
				
				if(log.isDebugEnabled())
					log.debug("scheduleExistingInterfaces: Scheduling existing interfaces for thresholder: " + svcName);
					
				// Retrieve list of interfaces from the database which 
				// support the service collected by this thresholder
				//
				List interfaceList = new ArrayList();
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
						
						scheduleInterface(nodeId, ipAddress, svcName, true);
					} // end while more interfaces in result set
	
					rs.close();
				}
				catch (SQLException sqle)
				{
					log.warn("scheduleExistingInterfaces: SQL exception while querying ipInterface table", sqle);
					throw sqle;
				}
			} // end while more service thresholders
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
	 * This method is responsible for scheduling the specified node/address/svcname
	 * tuple for thresholding.  
	 * 
	 * @param nodeId	Node id
	 * @param ipAddress	IP address 
	 * @param svcName	Service name
	 * @param existing	True if called by scheduleExistingInterfaces(), false otheriwse
	 */
	void scheduleInterface(int nodeId, String ipAddress, String svcName, boolean existing)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		ThreshdConfigFactory cfgFactory = ThreshdConfigFactory.getInstance();
		ThreshdConfiguration config = cfgFactory.getConfiguration();
		Enumeration epkgs = config.enumeratePackage();
		
		// Compare interface/service pair against each threshd package
		// For each match, create new ThresholdableService object and 
		// schedule it for collection
		//
		while(epkgs.hasMoreElements())
		{
			org.opennms.netmgt.config.threshd.Package pkg = (org.opennms.netmgt.config.threshd.Package)epkgs.nextElement();

			// Make certain the the current service is in the package
			// and enabled!
			//
			if (!cfgFactory.serviceInPackageAndEnabled(svcName, pkg))
			{
				if (log.isDebugEnabled())
					log.debug("scheduleInterface: address/service: " + 
							ipAddress + "/" + svcName + 
							" not scheduled, service is not enabled or does not exist in package: " + 
							pkg.getName());
				continue;
			}

			// Is the interface in the package?
			//
			log.debug("scheduleInterface: checking ipaddress " + ipAddress + " for inclusion in pkg " + pkg.getName());
			boolean foundInPkg = cfgFactory.interfaceInPackage(ipAddress, pkg);
			if (!foundInPkg && existing == false)
			{
			    // The interface might be a newly added one, rebuild the package
			    // to ipList mapping and again to verify if the interface is in
			    // the package.
			    cfgFactory.rebuildPackageIpListMap();
			    foundInPkg = cfgFactory.interfaceInPackage(ipAddress, pkg);
			}
			if (!foundInPkg)
			{
			    if (log.isDebugEnabled())
			        log.debug("scheduleInterface: address/service: " + 
			                ipAddress + "/" + svcName + 
			                " not scheduled, interface does not belong to package: " + 
			                pkg.getName());
			    continue;
			}
		
			
			log.debug("scheduleInterface: ipaddress " + ipAddress + " IS in pkg " + pkg.getName());
			
			if (existing == false)
			{
				// It is possible that both a nodeGainedService and a primarySnmpInterfaceChanged
				// event are generated for an interface during a rescan.  To handle 
				// this scenario we must verify that the ipAddress/pkg pair identified by 
				// this event does not already exist in the thresholdable services list.
				//
				if (alreadyScheduled(ipAddress, pkg.getName()))
				{
					if (log.isDebugEnabled())
					{
						log.debug("scheduleInterface: ipAddr/pkgName " + 
							ipAddress + "/" + pkg.getName() + 
							" already in thresholdable service list, skipping.");
					}
					continue;
				}
			}
			
			try
			{
				// Criteria checks have all passed.  The interface/service pair
				// can be scheduled.
				//
				ThresholdableService tSvc = null;
				
				// Create a new SnmpThresholder object representing this node, interface,
				// service and package pairing
				//
				tSvc = new ThresholdableService(nodeId,
								InetAddress.getByName(ipAddress),
								svcName,
								pkg);
				
				// Initialize the thresholder with the service.
				//
				ServiceThresholder thresholder = this.getServiceThresholder(svcName);
				thresholder.initialize(tSvc, tSvc.getPropertyMap());

				// Add new service to the thresholdable service list.
				//
				m_thresholdableServices.add(tSvc);
				
				// Schedule the service for threshold checking
				//
				// NOTE:  Service will be scheduled at the configured
				//        interval (default is 5 minutes).  This should give
				// 	  the collector a chance to update the RRD file so
				//        there is data available to be fetched.
				m_scheduler.schedule(tSvc, tSvc.getInterval());
				
				if (log.isDebugEnabled())
					log.debug("scheduleInterface: " + nodeId + "/" + 
						ipAddress + " scheduled for " + svcName + " threshold checking");
			} 
			catch(UnknownHostException ex)
			{
				log.error("scheduleInterface: Failed to schedule interface " + ipAddress + 
								" for service " + svcName + ", illegal address", ex);
			}
			catch(RuntimeException rE)
			{
				log.warn("scheduleInterface: Unable to schedule " + 
						ipAddress + " for service " + svcName + 
						", reason: " + rE.getMessage());
			}
			catch(Throwable t)
			{
				log.error("scheduleInterface: Uncaught exception, failed to schedule interface "
						  + ipAddress + " for service " + svcName, t);
			}
		} // end while more packages exist
	}
	
	/**
	 * Returns true if specified address/pkg pair is already represented in the
	 * thresholdable services list.  False otherwise.
	 */
	private boolean alreadyScheduled(String ipAddress, String pkgName)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		synchronized(m_thresholdableServices)
		{
			ThresholdableService tSvc = null;
			Iterator iter = m_thresholdableServices.iterator();
			while (iter.hasNext())
			{
				tSvc = (ThresholdableService)iter.next();
						
				InetAddress addr = (InetAddress)tSvc.getAddress();
				if (addr.getHostAddress().equals(ipAddress) &&
					tSvc.getPackageName().equals(pkgName))
				{
					return true;
				}
			}
		}
		
		return false;
	}

    /**
     * @return Returns the schedulingCompleted.
     */
    public boolean isSchedulingCompleted() {
        return m_schedulingCompleted;
    }
    /**
     * @param schedulingCompleted The schedulingCompleted to set.
     */
    public void setSchedulingCompleted(boolean schedulingCompleted) {
        m_schedulingCompleted = schedulingCompleted;
    }
}
