//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
// Copyright (C) 2001 Oculan Corp. All rights reserved.
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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
//
//
package org.opennms.netmgt.collectd;

import java.lang.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.PropertyConstants;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.poller.IPv4NetworkInterface;

import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.utils.EventProxy;

// Castor generated
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Parm;

// castor classes generated from the collectd-configuration.xsd
import org.opennms.netmgt.config.collectd.*;

/**
 * <P>The CollectableService class ...</P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class CollectableService
	extends	IPv4NetworkInterface
	implements ReadyRunnable
{
	/**
	 * Interface's parent node identifier
	 */
	private int	m_nodeId;

	/**
	 * The package information for this interface/service pair
	 */
	private final org.opennms.netmgt.config.collectd.Package m_package;
	
	/**
	 * The service informaion for this interface/service pair
	 */
	private final Service m_service;
	
	/**
	 * Last known/current status
	 */
	private int m_status;
	
	/** 
	 * The last time data collection ocurred
	 */
	private long m_lastCollectionTime;
	
	/** 
	 * The last time the collector was scheduled for collection.
	 */
	private long m_lastScheduledCollectionTime;

	/**
	 * The scheduler for collectd
	 */
	private final Scheduler	m_scheduler;
	
	/**
	 * Service updates
	 */
	private CollectorUpdates	m_updates;

	/**
	 * The event proxy
	 */
	private EventProxy		m_proxy;

	/**
	 * 
	 */
	private static final boolean ABORT_COLLECTION = true;
	
	/**
	 * The map of collection parameters
	 */
	private static Map		m_properties = new TreeMap();
	
	private ServiceCollector 	m_collector;
	
	/**
	 * The key used to lookup the service properties
	 * that are passed to the monitor.
	 */
	private final String		m_svcPropKey;

	/**
	 * The map of service parameters. These parameters are
	 * mapped by the composite key <em>(package name, service name)</em>.
	 */
	private static Map		SVC_PROP_MAP = Collections.synchronizedMap(new TreeMap());
	
	/**
	 * Constructs a new instance of a CollectableService object.
	 *
	 * @param dbNodeId	The database identifier key for the interfaces' node
	 * @param address	InetAddress of the interface to collect from
	 * @param svcName	Service name
	 * @param pkg		The package containing parms for this collectable service.
	 *
	 */
	CollectableService(	int dbNodeId,
				InetAddress address,
				String svcName,
				org.opennms.netmgt.config.collectd.Package pkg)
	{
		m_nodeId 	= dbNodeId;
		m_address	= address; // IPv4NetworkInterface address
		m_package 	= pkg;
		m_status	= ServiceCollector.COLLECTION_SUCCEEDED;
		
		m_scheduler	= Collectd.getInstance().getScheduler();
		m_collector	= Collectd.getInstance().getServiceCollector(svcName);
		m_updates 	= new CollectorUpdates();
		
		m_lastScheduledCollectionTime = 0L;
		m_lastCollectionTime	  = 0L;
		
		// find the service matching the name
		//m
		Service svc = null;
		Enumeration esvc = m_package.enumerateService();
		while(esvc.hasMoreElements())
		{
			Service s = (Service)esvc.nextElement();
			if(s.getName().equalsIgnoreCase(svcName))
			{
				svc = s;
				break;
			}
		}
		if(svc == null)
			throw new RuntimeException("Service name not part of package!");
		
		// save reference to the service
		m_service    = svc;
		
		// add property list for this service/package combination if
		// it doesn't already exist in the service property map
		//
		m_svcPropKey = m_package.getName() + "." + m_service.getName();
		synchronized(SVC_PROP_MAP)
		{
			if(! SVC_PROP_MAP.containsKey(m_svcPropKey))
			{
				Map m = Collections.synchronizedMap(new TreeMap());
				Enumeration ep = m_service.enumerateParameter();
				while(ep.hasMoreElements())
				{
					Parameter p = (Parameter)ep.nextElement();
					m.put(p.getKey(), p.getValue());
				}

				SVC_PROP_MAP.put(m_svcPropKey, m);
			}
		}

		m_proxy = new EventProxy() {
			public void send(Event e)
			{
				EventIpcManagerFactory.getInstance().getManager().sendNow(e);
			}
			public void send(Log log)
			{
				EventIpcManagerFactory.getInstance().getManager().sendNow(log);
			}
		};
				
	}
	
	/**
	 * Returns node identifier
	 */
	public int getNodeId()
	{
		return m_nodeId;
	}
	
	/** 
	 * Set node nodentifier
	 */
	public void setNodeId(int nodeId)
	{
		m_nodeId = nodeId;
	}
	
	/**
	 * Returns the service name 
	 */
	public String getServiceName()
	{
		return m_service.getName();
	}
	
	/**
	 * Returns the package name 
	 */
	public String getPackageName()
	{
		return m_package.getName();
	}
	
	/** 
	 * Returns updates object
	 */
	public CollectorUpdates getCollectorUpdates()
	{
		return m_updates;
	}

	/**
	 * This method is used to evaluate the status of this
	 * interface and service pair. If it is time to run the
	 * collection again then a value of true is returned. If the
	 * interface is not ready then a value of false is returned.
	 */
	public boolean isReady()
	{
		boolean ready = false;

		if(m_service.getInterval() < 1)
		{
			ready = true;
		}
		else
		{
			ready = ((m_service.getInterval() - (System.currentTimeMillis() - m_lastScheduledCollectionTime)) < 1);
		}

		return ready;
	}
	
	/**
	 * Generate event and send it to eventd via the event proxy.
	 *
	 * uei	Universal event identifier of event to generate.
	 */
	private void sendEvent(String uei)
	{
		Category log = ThreadCategory.getInstance(getClass());
		Event event = new Event();
		event.setUei(uei);
		event.setNodeid((long)m_nodeId);
		event.setInterface(m_address.getHostAddress());
		event.setService("SNMP");
		event.setSource("OpenNMS.Collectd");
		try
		{
			event.setHost(InetAddress.getLocalHost().getHostAddress());
		}
		catch(UnknownHostException ex)
		{
			event.setHost("unresolved.host");
		}

		event.setTime(EventConstants.formatToString(new java.util.Date()));
		
		// Send the event
		//
		try
		{
			EventIpcManagerFactory.getInstance().getManager().sendNow(event);
		}
		catch(Exception ex)
		{
			log.error("Failed to send the event " + uei + " for interface " + 
					m_address.getHostAddress(), ex);
		}
		
		if (log.isDebugEnabled())	
			log.debug("sendEvent: Sent event " + uei + " for " + 
					m_nodeId + "/" + 
					m_address.getHostAddress() + "/" + 
					m_service.getName());
	}
	
	/**
	 * This is the main method of the class. An instance is normally
	 * enqueued on the scheduler which checks its <code>isReady</code>
	 * method to determine execution. If the instance is ready for 
	 * execution then it is started with it's own thread context
	 * to execute the query. The last step in the method before
	 * it exits is to reschedule the interface.
	 *
	 */
	public void run()
	{
		Category log = ThreadCategory.getInstance(getClass());

		// Process any oustanding updates.
		//
		if (processUpdates() == ABORT_COLLECTION)
			return;
		
		// Update last scheduled poll time
		m_lastScheduledCollectionTime = System.currentTimeMillis();
		
		// Check scheduled outages to see if any apply indicating
		// that the collection should be skipped
		//
		if (scheduledOutage())
		{
			// Outage applied...reschedule the service and return
			m_scheduler.schedule(this, m_service.getInterval());
			return;
		}
		
		// Perform SNMP data collection
		//
		if (log.isDebugEnabled())
			log.debug("run: starting new collection for " + m_address.getHostAddress());
		
		int status = ServiceCollector.COLLECTION_FAILED;
		Map propertiesMap = (Map)SVC_PROP_MAP.get(m_svcPropKey);
		try
		{
			status = m_collector.collect(this, m_proxy, propertiesMap);
		}
		catch(Throwable t)
		{
			log.error("run: An undeclared throwable was caught during SNMP collection for interface " + m_address.getHostAddress(), t);
		}
		
		// Update last poll time
		m_lastCollectionTime = System.currentTimeMillis();
		
		// Any change in status?
		//
		if(status != m_status)
		{			
			// Generate SNMP collection transition events
			if (log.isDebugEnabled())
				log.debug("run: change in collection status, generating event.");

			// Send the appropriate event
			//
			switch(status)
			{
				case ServiceCollector.COLLECTION_SUCCEEDED:
					sendEvent(EventConstants.DATA_COLLECTION_SUCCEEDED_EVENT_UEI);
					break;
	
				case ServiceCollector.COLLECTION_FAILED: 
					sendEvent(EventConstants.DATA_COLLECTION_FAILED_EVENT_UEI);
					break;
			
				default:
					break;
			}
		}

		// Set the new status
		m_status = status;
		
		// Reschedule the service
		//
		m_scheduler.schedule(this, m_service.getInterval());
		
		return;
	}	
	
	Map getPropertyMap()
	{
		return (Map)SVC_PROP_MAP.get(m_svcPropKey);
	}
	
	/**
	 * Checks the package information for the collectable service and determines
	 * if any of the calendar outages associated with the package apply to 
	 * the current time and the service's interface.  If an outage applies
	 * true is returned...otherwise false is returned.
	 * 
	 * @return false if no outage found (indicating a collection may be performed)
	 * or true if applicable outage is found (indicating collection should be skipped).
	 */
	private boolean scheduledOutage()
	{
		boolean outageFound = false;
		
		PollOutagesConfigFactory outageFactory = PollOutagesConfigFactory.getInstance();
		
		// Iterate over the outage names defined in the interface's package.
		// For each outage...if the outage contains a calendar entry which 
		// applies to the current time and the outage applies to this 
		// interface then break and return true.  Otherwise process the 
		// next outage.
		// 
		Iterator iter = m_package.getOutageCalendarCollection().iterator();
		while (iter.hasNext())
		{
			String outageName = (String)iter.next();
			
			// Does the outage apply to the current time?
			if (outageFactory.isCurTimeInOutage(outageName))
			{
				// Does the outage apply to this interface?
				if (outageFactory.isInterfaceInOutage(m_address.getHostAddress(), outageName))
				{
					if (ThreadCategory.getInstance(getClass()).isDebugEnabled())
						ThreadCategory.getInstance(getClass()).debug("scheduledOutage: configured outage '" + 
											outageName + 
											"' applies, interface " + 
											m_address.getHostAddress() + 
											" will not be collected for " + 
											m_service);
					outageFound  = true;
					break;
				}
			}
		}
		
		return outageFound;
	}
	
	/**
	 * Process any outstanding updates.  
	 *
	 * @return true if update indicates that collection should be aborted
	 *         (for example due to deletion flag being set), false otherwise.
	 */
	private boolean processUpdates()
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		// All update processing takes place within synchronized block
		// to ensure that no updates are missed.  
		//
		synchronized(this)
		{
			if (!m_updates.hasUpdates())
				return !ABORT_COLLECTION;
				
			// Update: deletion flag
			//
			if (m_updates.isDeletionFlagSet())
			{
				// Deletion flag is set, simply return without polling
				// or rescheduling this collector.
				//
				if(log.isDebugEnabled())
					log.debug("Collector for  " + m_address.getHostAddress() + 
						" is marked for deletion...skipping collection, will not reschedule.");
				
				return ABORT_COLLECTION;
			}
			
			// Update: reinitialization flag
			//
			if (m_updates.isReinitializationFlagSet())
			{
				// Reinitialization flag is set, call initialize() to
				// reinit the collector for this interface
				//
				if(log.isDebugEnabled())
					log.debug("ReinitializationFlag set for " + m_address.getHostAddress());
					
				try
				{
					m_collector.release(this);
					m_collector.initialize(this, this.getPropertyMap());
					if (log.isDebugEnabled())
						log.debug("Completed reinitializing SNMP collector for " + m_address.getHostAddress());
				}
				catch(RuntimeException rE)
				{
					log.warn("Unable to initialize " + m_address.getHostAddress() + 
						" for " + m_service.getName() + " collection, reason: " + rE.getMessage());
				}
				catch(Throwable t)
				{
					log.error("Uncaught exception, failed to intialize interface " + m_address.getHostAddress() + 
							" for " + m_service.getName() + " data collection", t);
				}
			}
				
			// Update: reparenting flag
			//
			if (m_updates.isReparentingFlagSet())
			{
				if(log.isDebugEnabled())
					log.debug("ReparentingFlag set for " + m_address.getHostAddress());
					
				// The interface has been reparented under a different node (with
				// a different nodeId).  
				//
				// If the new directory doesn't already exist simply need to rename the old 
				// directory:
				//		/opt/OpenNMS/share/rrd/snmp/<oldNodeId>
				// to the new directory:
				//		/opt/OpenNMS/share/rrd/snmp/<newNodeId>
				//
				// Otherwise must iterate over each of the files/dirs in the <oldNodeId>
				// directory and move/rename them under the <newNodeId> directory.
				
				// Get path to RRD repository
				//
				String rrdPath = DataCollectionConfigFactory.getInstance().getRrdRepository();
		
				if(rrdPath == null)
				{
					log.warn("Configuration error, failed to retrieve path to RRD repository. Unable to process reparenting updated.");
					return !ABORT_COLLECTION;
				}
		
				// Strip the File.separator char off of the end of the path
				if (rrdPath.endsWith(File.separator))
				{
					rrdPath = rrdPath.substring(0, (rrdPath.length() - File.separator.length()));
				}
			
				// Does the <newNodeId> directory already exist?
				File newNodeDir = new File(rrdPath + File.separator + m_updates.getReparentNewNodeId());
				if (!newNodeDir.isDirectory())
				{
					// New directory does not exist yet so simply rename the old directory to
					// the new directory.
					//
					
					// <oldNodeId> directory
					File oldNodeDir = new File(rrdPath + File.separator + m_updates.getReparentOldNodeId());
					
					try
					{
						// Rename <oldNodeId> dir to <newNodeId> dir.
						if (log.isDebugEnabled())
							log.debug("Attempting to rename " + oldNodeDir + " to " + newNodeDir);
						oldNodeDir.renameTo(newNodeDir); 
						if (log.isDebugEnabled())
							log.debug("Rename successful!!");
					}
					catch (SecurityException se)
					{
						log.error("Insufficient authority to rename RRD directory.", se);
					}
					catch (Throwable t)
					{
						log.error("Unexpected exception while attempting to rename RRD directory.", t);
					}
				}
				else
				{
					// New node directory already exists so we must move/rename each of the
					// old node directory contents under the new node directory.
					//
					
					// Get list of files to be renamed/moved
					File oldNodeDir = new File(rrdPath + File.separator + m_updates.getReparentOldNodeId());
					String[] filesToMove = oldNodeDir.list();
					
					if (filesToMove != null)
					{	
						// Iterate over the file list and rename/move each one
						for (int i=0; i<filesToMove.length; i++)
						{
							File srcFile = new File(oldNodeDir.toString() + File.separator + filesToMove[i]);
							File destFile = new File(newNodeDir.toString() + File.separator + filesToMove[i]);
							try
							{
								if (log.isDebugEnabled())
									log.debug("Attempting to move " + srcFile + " to " + destFile);
								srcFile.renameTo(destFile);
							}
							catch (SecurityException se)
							{
								log.error("Insufficient authority to move RRD files.", se);
								break;
							}
							catch (Throwable t)
							{
								log.warn("Unexpected exception while attempting to move " + srcFile + " to " + destFile, t);
							}
						}
					}
				}
					
				// Convert new nodeId to integer value
				int newNodeId = -1;
				try
				{
					newNodeId = Integer.parseInt(m_updates.getReparentNewNodeId());
				}
				catch (NumberFormatException nfE)
				{
					log.warn("Unable to convert new nodeId value to an int while processing reparenting update: " + m_updates.getReparentNewNodeId());
				}
				
				// Set this collector's nodeId to the value of the interface's
				// new parent nodeid.
				m_nodeId = newNodeId;
				
				// We must now reinitialize the collector for this interface,
				// in order to update the NodeInfo object to reflect changes
				// to the interface's parent node among other things.
				//
				try
				{
					if (log.isDebugEnabled())
						log.debug("Reinitializing SNMP collector for " + m_address.getHostAddress());
					m_collector.release(this);
					m_collector.initialize(this, this.getPropertyMap());
					if (log.isDebugEnabled())
						log.debug("Completed reinitializing SNMP collector for " + m_address.getHostAddress());
				}
				catch(RuntimeException rE)
				{
					log.warn("Unable to initialize " + m_address.getHostAddress() + 
						" for " + m_service.getName() + " collection, reason: " + rE.getMessage());
				}
				catch(Throwable t)
				{
					log.error("Uncaught exception, failed to initialize interface " + m_address.getHostAddress() + 
							" for " + m_service.getName() + " data collection", t);
				}
			}
	
			// Updates have been applied. Reset CollectorUpdates object.
			//.
			m_updates.reset();
		} // end synchronized
		
		return !ABORT_COLLECTION;
	}
}

