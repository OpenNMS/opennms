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
// 2004 Jan 06: Added a log.debug entry to note when a service will no longer
// 		be polled
// 2003 Jul 02: Fixed ClassCastException.
// 2003 Jan 31: Added the option to match any IP address in an outage calendar.
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 21: Added a check to prevent a Null Pointer exception when deleting
//              a service based on the downtime model.
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

package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <P>The PollableService class ...</P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class PollableService
	extends	IPv4NetworkInterface
	implements Pollable, ReadyRunnable
{
	/**
	 * interface that this service belongs to
	 */
	private PollableInterface	m_pInterface;

	/**
	 * The service inforamtion for this interface.
	 */
	private final Service		m_service;
	
	/**
	 * The package for this polling interface.
	 */
	private final org.opennms.netmgt.config.poller.Package		m_package;		

	/**
	 * Last known/current status. 
	 */
	private int 			m_status;
	
	/** 
	 * Flag which indicates if previous poll returned
	 * SnmpMonitor.SERVICE_UNRESPONSIVE.
	 */
	private boolean			m_unresponsiveFlag;
	
	/**
	 * Indicates if the service changed status as the
	 * result of most recent poll.
	 *
	 * Set by poll() method.
	 */
	private boolean			m_statusChangedFlag;
	
	/**
	 * When the last status change occured.
	 *
	 * Set by the poll() method.
	 */
	private long			m_statusChangeTime;
	
	/** 
	 * Deletion flag...set to indicate that the service/interface/node
	 * tuple represented by this PollableService object has been
	 * deleted and should no longer be polled.
	 */
	private boolean			m_deletionFlag;
	
	/**
	 * The service monitor used to poll this 
	 * service/interface pair.
	 */
	private final ServiceMonitor	m_monitor;
	
	/**
	 * The scheduler for the poller
	 */
	private final Scheduler		m_scheduler;
	
	/**  
	 * List of all scheduled PollableService objects
	 */
	private final List		m_pollableServices;
	
	/** 
	 * Set to true when service is first constructed which
	 * will cause the recalculateInterval() method to return
	 * 0 resulting in an immediate poll.  
	 */
	private boolean			m_pollImmediate;

	/** 
	 * The last time the service was polled...whether due
	 * to a scheduled poll or node outage processing.
	 */
	private long			m_lastPoll;
	
	/** 
	 * The last time the service was scheduled for a poll.
	 */
	private long			m_lastScheduledPoll;

	/**
	 * This was the interval to use when the node was
	 * last rescheduled. This must be used or it could
	 * block a queue! (i.e. its ready time gets longer
	 * while the elements behind it are ready to go!)
	 */
	private long			m_lastInterval;
	
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
	 * Constructs a new instance of a pollable service object that is 
	 * polled using the passed monitor. The service is scheduled based
	 * upon the values in the packages.
	 *
	 * @param dbNodeId	The database identifier key for the interfaces' node.
	 * @param iface		The interface to poll
	 * @param svcName	The name of the service being polled.
	 * @param pkg		The package with the polling information
	 *
	 */
	PollableService(	PollableInterface pInterface,
				String		svcName,
				org.opennms.netmgt.config.poller.Package pkg,
				int status,
				Date svcLostDate)
	{
		m_pInterface	= pInterface;
		m_address	= pInterface.getAddress(); // IPv4NetworkInterface address
		m_package	= pkg;
		m_status	= status;
		m_deletionFlag	= false;
		
		m_monitor	= Poller.getInstance().getServiceMonitor(svcName);
		m_scheduler	= Poller.getInstance().getScheduler();
		m_pollableServices = Poller.getInstance().getPollableServiceList();
		
		m_pollImmediate = true;  // set for immediate poll
		m_lastScheduledPoll = 0L;
		m_lastPoll	  = 0L;
		m_lastInterval = 0L;
			
		// Set status change values.  
		m_statusChangeTime = 0L;
		m_statusChangedFlag = false;
		if (m_status == ServiceMonitor.SERVICE_UNAVAILABLE)
		{
			if (svcLostDate == null)
				throw new IllegalArgumentException("The svcLostDate parm cannot be null if status is UNAVAILABLE!");
			
			m_statusChangeTime = svcLostDate.getTime();
		}
		m_unresponsiveFlag = false;
		
		// find the service matching the name
		//
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
	}

	public PollableInterface getInterface()
	{	
		return m_pInterface;
	}

	/**
	 * Returns the service name 
	 */
	public String getServiceName()
	{
		return m_service.getName();
	}
	
	/**
	 * Returns true if status of service changed as a result
	 * of the last poll.
	 *
	 * WARNING:  value of m_statusChangedFlag is only reliable immediately
	 *		following a call to poll()
	 */
	public boolean statusChanged()
	{
		return m_statusChangedFlag;
	}
	
	public void resetStatusChanged()
	{
		m_statusChangedFlag = false;
	}
	
	public int getStatus()
	{
		return m_status;
	}
	
	public void setStatus(int status)
	{
		if (m_status != status)
		{
			m_status = status;
			m_statusChangeTime = System.currentTimeMillis();
		}
	}
	
	public void markAsDeleted()
	{
		m_deletionFlag = true;
	}
	
	public boolean isDeleted()
	{
		return m_deletionFlag;
	}
	
	public long getLastPollTime()
	{
		return m_lastPoll;
	}
	
	public long getLastScheduleInterval()
	{
		return m_lastInterval;
	}
	
	/**
	 * Returns the time (in milliseconds) after which this is
	 * scheduled to run.
	 */
	public long getScheduledRuntime()
	{
		return (this.getLastPollTime() + this.getLastScheduleInterval());
	}
	
	public String getPackageName()
	{
		return m_package.getName();
	}
	
	/**
	 * Returns the package associated with this service. 
	 */
	public org.opennms.netmgt.config.poller.Package getPackage()
	{
		return m_package;
	}
	
	/**
	 * This method is used to evaluate the status of this
	 * interface and service pair. If it is time to run the
	 * poll again then a value of true is returned. If the
	 * interface is not ready then a value of false is returned.
	 *
	 * @throws java.lang.RuntimeException Throws if the ready
	 * 	time cannot be computed due to invalid downtime model.
	 */
	public boolean isReady()
	{
		long when = m_lastInterval;
		boolean ready = false;

		if(when < 1)
		{
			ready = true;
		}
		else
		{
			ready = ((when - (System.currentTimeMillis() - m_lastScheduledPoll)) < 1);
		}

		return ready;
	}
	
	/**
	 * Reschedules the service at the specified interval
	 * (in milliseconds).
	 */
	void reschedule(long interval)
	{
		// Update m_lastInterval
		// 
		// NOTE: Never want to reschedule at less than 1 milliscond interval
		//
		if (interval <= 0)
			m_lastInterval = m_service.getInterval();
		else
			m_lastInterval = interval;
		
		// Reschedule the service
		m_scheduler.schedule(this, interval);
	}
	
	/**
	 * This method is called to reschedule the service for polling. 
	 *
	 * NOTE:  Scheduler calls reschedule() with reUseInterval parm 
	 *	  set to true in the event that a scheduled outage
	 * 	  is in effect when a service is popped from the interval
	 * 	  queue for polling.
	 *
	 * @param reUseInterval Flag which controls how the interval
	 * 			at which to reschedule the interface
	 * 			is determined.  If true, value of
	 * 			m_lastInterval is used.  Otherwise
	 * 			recalculateInterval() is called to
	 * 			recalculate the interval.
	 */
	void reschedule(boolean reUseInterval)
	{
		// Determine interval at which to reschedule the interface
		// 
		long interval = 0L;

		if (reUseInterval)
		{
			interval = m_lastInterval;
		}
		else
		{
			// Recalculate polling interval
			// 
			// NOTE:  interval of -1 indicates interface/service
			//        pair has exceeded the downtime model and
			//        is to be deleted.
			interval = recalculateInterval();
			
			if(interval < 0)
			{
				// Generate 'deleteService' event
				sendEvent(EventConstants.DELETE_SERVICE_EVENT_UEI, null);
	
				// Delete this pollable service from the service updates 
				// map maintained by the Scheduler and mark any 
				// equivalent pollable services (scheduled via other packages)
				// as deleted.  The services marked as deleted will subsequently
				// be removed the next time the sheduler pops them from the
				// interval queues for polling.
				//
				this.cleanupScheduledServices();
				
				// remove this service from the interfaces' service list 
				// so it is no longer polled via node outage processing
				//
				m_pInterface.removeService(this);
				
				return; // Return without rescheduling
			} // end delete event
		}
		
		this.reschedule(interval);
	}
	
	/**
	 * This method is used to return the next interval for this
	 * interface. If the interval is zero then this service 
	 * has never run and should be scheduled immediantly. If the
	 * time is -1 then the node should be deleted. Otherwise the
	 * appropriate scheduled time is returned.
	 *
	 * @throws java.lang.RuntimeException Throws if the ready
	 * 	time cannot be computed due to invalid downtime model.
	 */
	long recalculateInterval()
	{
		Category log = ThreadCategory.getInstance(getClass());

		// If poll immediate flag is set the service hasn't
		// been polled yet.  Return 0 to cause an immediate 
		// poll of the interface.
		if (m_pollImmediate)
		{
			return 0;
		}
		
		long when = m_service.getInterval();
		long downSince = 0;
		if (m_status == ServiceMonitor.SERVICE_UNAVAILABLE)
			downSince = System.currentTimeMillis() - m_statusChangeTime;
		 
		if (log.isDebugEnabled())
			log.debug("recalculateInterval for " + 
				m_pInterface.getAddress().getHostAddress() + "/" + m_service.getName() + " : " +
				" status= " + Pollable.statusType[m_status] +  
				" downSince= " + downSince);

		switch(m_status)
		{
			case ServiceMonitor.SERVICE_AVAILABLE:
				break;
	
			case ServiceMonitor.SERVICE_UNAVAILABLE:
				boolean matched = false;
				Enumeration edowntime = m_package.enumerateDowntime();
				while(edowntime.hasMoreElements())
				{
					Downtime dt = (Downtime)edowntime.nextElement();
					if(dt.getBegin() <= downSince)
					{
						if(dt.getDelete() != null && (dt.getDelete().equals("yes") || dt.getDelete().equals("true")))
						{
							when    = -1;
							matched = true;
						}
						else if(dt.hasEnd() && dt.getEnd() > m_statusChangeTime)
						{
							// in this interval
							//
							when = dt.getInterval();
							matched = true;
						}
						else // no end
						{
							when = dt.getInterval();
							matched = true;
						}
					}
				}
				if(!matched)
				{
					log.warn("recalculateInterval: Could not locate downtime model, throwing runtime exception");
					throw new RuntimeException("Downtime model is invalid, cannot schedule interface "
								   + m_pInterface.getAddress().getHostAddress() + ", service = "
								   + m_service.getName());
				}
	
				break;
	
			default:
				log.warn("recalculateInterval: invalid status found, downtime model lookup failed. throwing runtime exception");
				throw new RuntimeException("Invalid Polling Status for interface " + m_pInterface.getAddress().getHostAddress() 
							   + ", service = " + m_service.getName() + ", status = " + m_status);

		} // end switch()
		
		if (log.isDebugEnabled())
			log.debug("recalculateInterval: new scheduling interval for " + 
					m_pInterface.getAddress().getHostAddress() + "/" +
					m_service.getName() + " = " + when);
		return when;
	}
	
	/**
	 * 
	 */
	private void sendEvent(String uei, Map properties)
	{
		Category log = ThreadCategory.getInstance(getClass());
		Event event = new Event();
		event.setUei(uei);
		event.setNodeid((long)m_pInterface.getNode().getNodeId());
		event.setInterface(m_pInterface.getAddress().getHostAddress());
		event.setService(m_service.getName());
		event.setSource("OpenNMS.Poller");
		try
		{
			event.setHost(InetAddress.getLocalHost().getHostAddress());
		}
		catch(UnknownHostException ex)
		{
			event.setHost("unresolved.host");
		}

		event.setTime(EventConstants.formatToString(new java.util.Date()));
		
		// Add parms
		//
		Parms parms = null;
		
		// Qualifier parm (if available)
		String qualifier = null;
		if (properties != null)
			try
			{
				qualifier = (String)properties.get("qualifier");
			}
			catch(ClassCastException ex)
			{
				qualifier = null;
			}
		if(qualifier != null && qualifier.length() > 0)
		{
			if (parms == null)
				parms = new Parms();
			Parm parm = new Parm();
			parm.setParmName("qualifier");

			Value val = new Value();
			val.setContent(qualifier);
			val.setEncoding("text");
			val.setType("string");
			parm.setValue(val);

			parms.addParm(parm);
		}

		// Add parms for Timeout, Retry, Attempts for 
		// 'serviceUnresponsive' event
		if (uei.equals(EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI))
		{
			int timeout = ParameterMap.getKeyedInteger(properties, "timeout", -1);
			int retry   = ParameterMap.getKeyedInteger(properties, "retry", -1);
			int attempts = retry + 1;
			
			// Timeout parm
			if (timeout != -1)
			{
				if (parms == null)
					parms = new Parms();
				Parm parm = new Parm();
				parm.setParmName("timeout");
	
				Value val = new Value();
				val.setContent(Integer.toString(timeout));
				val.setEncoding("text");
				val.setType("string");
				parm.setValue(val);
				
				parms.addParm(parm);
			}
			
			// Retry parm
			if (retry != -1)
			{
				if (parms == null)
					parms = new Parms();
				Parm parm = new Parm();
				parm.setParmName("retry");
	
				Value val = new Value();
				val.setContent(Integer.toString(retry));
				val.setEncoding("text");
				val.setType("string");
				parm.setValue(val);
				
				parms.addParm(parm);
			}
			
			// Attempts parm
			if (attempts > 0)
			{
				if (parms == null)
					parms = new Parms();
				Parm parm = new Parm();
				parm.setParmName("attempts");
	
				Value val = new Value();
				val.setContent(Integer.toString(attempts));
				val.setEncoding("text");
				val.setType("string");
				parm.setValue(val);
	
				parms.addParm(parm);
			}
		}
		
		// Set event parms
		event.setParms(parms);
		
		// Send the event
		//
		try
		{
			EventIpcManagerFactory.getInstance().getManager().sendNow(event);
			if (log.isDebugEnabled())	
			{
				log.debug("Sent event " + uei + " for " + 
					  m_pInterface.getNode().getNodeId() + "/" + 
					  m_pInterface.getAddress().getHostAddress() + "/" + 
					  m_service.getName());
			}
		}
		catch(Throwable t)
		{
			log.error("Failed to send the event " + uei + " for interface " + 
					m_pInterface.getAddress().getHostAddress(), t);
		}
	}
	
	/**
	 * Tests if two PollableService objects refer to the same 
	 * nodeid/interface/service tuple.  
	 *
	 * @param aService  	the PollableService object to compare
	 * 
	 * @return TRUE if the two pollable service objects are equivalent,
	 * 		FALSE otherwise.
	 */
	public boolean equals(Object aService)
	{
		boolean isEqual = false;
		
		if (aService instanceof PollableService)
		{
			PollableService temp = (PollableService)aService;
			
			if (    this.m_pInterface.getNode().getNodeId() == temp.m_pInterface.getNode().getNodeId() &&
				this.m_address.equals(temp.m_address) &&
				this.m_service.getName().equals(temp.m_service.getName()) )
				{
					isEqual = true;
				}
		}
					
		return isEqual;
	}
	
	/**
	 * This method is called to remove a pollable service from the service  
	 * updates map.  It is necessary to not only remove the passed PollableService 
	 * object but also to mark any other pollable services which share the same nodeid, 
	 * interface address and service name for deletion.  The reason for this is that the
	 * interface/service pair may have applied to multiple packages resulting
	 * in the same interface/service pair being polled multiple times.  
	 */
	void cleanupScheduledServices()
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		// Go ahead and remove 'this' service from the list.
		m_pollableServices.remove(this);
		if (log.isDebugEnabled())
			log.debug("cleanupScheduledServices: deleted " + m_address.getHostAddress() + 
					":" + m_service.getName() + ":" + m_package.getName());
		
		// Next interate over the pollable service list and mark any pollable service  
		// objects which refer to the same node/interface/service pairing
		// for deletion.
		synchronized(m_pollableServices)
		{
			Iterator iter = m_pollableServices.iterator();
			if (log.isDebugEnabled())
				log.debug("cleanupScheduledServices: iterating over serviceUpdatesMap(numEntries=" + 
						m_pollableServices.size() + ") looking for " + 
						m_address.getHostAddress() + ":" + m_service.getName());
			
			while (iter.hasNext())
			{
				PollableService temp = (PollableService)iter.next();
				
				if (log.isDebugEnabled())
					log.debug("cleanupScheduledServices: comparing " + ((InetAddress)temp.getAddress()).getHostAddress() + 
							":" + temp.getServiceName());
						
				// If the two objects are equal but not identical (in other
				// words they refer to two different objects with the same
				// nodeid, ipAddress, and service name) then need to set the
				// deletion flag so that the next time the interface
				// is pulled from the queue for execution it will be deleted.
				if ( this.equals(temp) )
				{
					// Now set the deleted flag
					temp.markAsDeleted();
					if (log.isDebugEnabled())
						log.debug("cleanupScheduledServices: marking " + ((InetAddress)temp.getAddress()).getHostAddress() + 
								":" + temp.getServiceName() + ":" + temp.getPackageName() + " as deleted.");
				}			
			}
		}
	}
	
	/**
	 * Checks the package information for the pollable service and determines
	 * if any of the calendar outages associated with the package apply to 
	 * the current time and the service's interface.  If an outage applies
	 * true is returned...otherwise false is returned.
	 *
	 * @return false if no outage found (indicating a poll may be performed)
	 * or true if applicable outage is found (indicating poll should be skipped).
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
				
				if ((outageFactory.isInterfaceInOutage(m_address.getHostAddress(), outageName)) ||
				   (outageFactory.isInterfaceInOutage("match-any", outageName)))
				{
					if (ThreadCategory.getInstance(getClass()).isDebugEnabled())
						ThreadCategory.getInstance(getClass()).debug("scheduledOutage: configured outage '" + 
											outageName + 
											"' applies, interface " + 
											m_address.getHostAddress() + 
											" will not be polled for " + 
											m_service.getName());
					outageFound  = true;
					break;
				}
			}
		}
		
		return outageFound;
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

		try 
		{
			this.doRun(true);
		} 
		catch (LockUnavailableException e) 
		{
			// failed to acquire lock, just reschedule on 10 second queue
			if (log.isDebugEnabled())
				log.debug("Lock unavailable, rescheduling on 10 sec queue, reason: " + e.getMessage());
			this.reschedule(10000);
		}
		catch (InterruptedException e)
		{
			// The thread was interrupted; reschedule on 10 second queue
			if (log.isDebugEnabled())
				log.debug(e);
			this.reschedule(10000);
		}
	}

	/**
	 * This an alternative entry point into the class. This was originally
	 * created in order to support the PollableServiceProxy, which needed
	 * the option of handling its own scheduling and needed to keep the
	 * PollableService from rescheduling itself.
	 *
	 * In addition to allowing this, it also allows exceptions that require
	 * a rescheduling decision to pass back up the stack.  In all other ways,
	 * this method works the same as run().
	 *
	 * @param reschedule set this to true if you want the pollable service 
	 * to reschedule itself when done processing.
	 *
	 * @throws LockUnavailableException If it was unable to obtain a node
	 *                                  lock
	 * @throws ThreadInterruped If the thread was interrtuped while
	 *                          waiting for a node lock.
	 */
	public void run(boolean reschedule)
		throws LockUnavailableException, InterruptedException
	{
		this.doRun(reschedule);
	}

	/**
	 * This used to be the implementation for the run() method.  When
	 * we created run(boolean), however, we needed to move the
	 * implementation down a level lower so that we could overload the
	 * run() method.
	 *
	 * @param allowedToRescheduleMyself set this to true if you want the
	 *  pollable service to reschedule itself when done processing.
	 *
	 * @throws LockUnavailableException If it was unable to obtain a node
	 *                                  lock
	 * @throws ThreadInterruped If the thread was interrtuped while
	 *                          waiting for a node lock.
	 *
	 */
	private void doRun(boolean allowedToRescheduleMyself)
		throws LockUnavailableException, InterruptedException
	{
		Category log = ThreadCategory.getInstance(getClass());

		// Is the service marked for deletion?  If so simply return.
		//
		if (this.isDeleted())
		{
			if (log.isDebugEnabled())
			{
				log.debug("PollableService doRun: Skipping service marked as deleted on "
						+ m_pInterface.getAddress().getHostAddress()
						+ ", service = " + m_service.getName() + ", status = " + m_status);
			}
			return;
		}
		
		// NodeId
		int nodeId = m_pInterface.getNode().getNodeId();
		
		// Update last scheduled poll time if allowedToRescheduleMyself
		// flag is true
		if (allowedToRescheduleMyself)
		m_lastScheduledPoll = System.currentTimeMillis();
		
		// Check scheduled outages to see if any apply indicating
		// that the poll should be skipped
		//
		if (scheduledOutage())
		{
			// Outage applied...reschedule the service and return
			if (allowedToRescheduleMyself)
				this.reschedule(true);

			return;
		}
		
		// Is node outage processing enabled?
		if (PollerConfigFactory.getInstance().nodeOutageProcessingEnabled())
		{
			// Lookup PollableNode object using nodeId as index
			//
			PollableNode pNode = Poller.getInstance().getNode(nodeId);
			
			/*
			 * Acquire lock to 'PollableNode'
			 */
			boolean ownLock = false;
			try
			{
				// Attempt to obtain node lock...wait no longer than 500ms
				// We don't want to tie up the thread for long periods of time
				// waiting for the lock on the PollableNode to be released.
				if (log.isDebugEnabled())
					log.debug("run: ------------- requesting node lock for nodeid: " + nodeId + " -----------");
		
				if (!(ownLock = pNode.getNodeLock(500)))
					throw new LockUnavailableException("failed to obtain lock on nodeId " + nodeId);
			}
			catch (InterruptedException iE)
			{
				// failed to acquire lock
				throw new InterruptedException("failed to obtain lock on nodeId " + nodeId + ": " + iE.getMessage());
			}
			// Now we have a lock
			
			if (ownLock) // This is probably redundant, but better to be sure.
			{
				try 
				{
					// Make sure the node hasn't been deleted.
					if (!pNode.isDeleted())
					{
						if (log.isDebugEnabled())
							log.debug("run: calling poll() for " +
								nodeId + "/" +
								m_pInterface.getAddress().getHostAddress() + "/" +
								m_service.getName());

						pNode.poll(this);

						if (log.isDebugEnabled())
							log.debug("run: call to poll() finished for " +
								nodeId + "/" +
								m_pInterface.getAddress().getHostAddress() + "/" +
								m_service.getName());
					}
				}
				finally
				{
					if (log.isDebugEnabled())
						log.debug("run: ----------- releasing node lock for nodeid: " + nodeId + " ----------");
					try
					{
						pNode.releaseNodeLock();
					}
					catch (InterruptedException iE)
					{
						log.error("run: thread interrupted...failed to release lock on nodeId " + nodeId);
					}
				}
			}
		}
		else
		{
			// Node outage processing disabled so simply poll the service
			if (log.isDebugEnabled())
				log.debug("run: node outage processing disabled, polling: " + 
						m_pInterface.getAddress().getHostAddress() + "/" + 
						m_service.getName());
			this.poll();
		}
		
		// reschedule the service for polling
		if (allowedToRescheduleMyself)
			this.reschedule(false);
			
		return;
	}	
	
	/**
	 * <P>Invokes a poll of the service via the ServiceMonitor.</P>
	 */
	public int poll()
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		m_lastPoll = System.currentTimeMillis();
		m_statusChangedFlag = false;
		InetAddress addr = (InetAddress)m_pInterface.getAddress();
		
		if (log.isDebugEnabled())
			log.debug("poll: starting new poll for " + addr.getHostAddress() + 
					"/" + m_service.getName() + "/" + m_package.getName());
		
		// Poll the interface/service pair via the service monitor
		//
		int status = ServiceMonitor.SERVICE_UNAVAILABLE;
		Map propertiesMap = (Map)SVC_PROP_MAP.get(m_svcPropKey);
		try
		{
			status = m_monitor.poll(this, propertiesMap, m_package);
		}
		catch(NetworkInterfaceNotSupportedException ex)
		{
			log.error("poll: Interface " + addr.getHostAddress() + " Not Supported!", ex);
			return status;
		}
		catch(Throwable t)
		{
			log.error("poll: An undeclared throwable was caught polling interface " + addr.getHostAddress(), t);
		}
		
		// serviceUnresponsive behavior disabled?
                //
                if (!PollerConfigFactory.getInstance().serviceUnresponsiveEnabled())
                {
                        // serviceUnresponsive behavior is disabled, a status
                        // of SERVICE_UNRESPONSIVE is treated as SERVICE_UNAVAILABLE
                        if (status == ServiceMonitor.SERVICE_UNRESPONSIVE)
                                status = ServiceMonitor.SERVICE_UNAVAILABLE;
                }
                else
                {
			// Update unresponsive flag based on latest status
			// returned by the monitor and generate serviceUnresponsive
			// or serviceResponsive event if necessary.
			//
			switch (status)
			{
				case ServiceMonitor.SERVICE_UNRESPONSIVE: 
					// Check unresponsive flag to determine if we need
					// to generate a 'serviceUnresponsive' event.
					//
					if (m_unresponsiveFlag == false )
					{
						m_unresponsiveFlag = true;
						sendEvent(EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, propertiesMap);
						
						// Set status back to available, don't want unresponsive 
						// service to generate outage
						status = ServiceMonitor.SERVICE_AVAILABLE;
					}
					break;
			
				case ServiceMonitor.SERVICE_AVAILABLE: 
					// Check unresponsive flag to determine if we
					// need to generate a 'serviceResponsive' event
					if (m_unresponsiveFlag == true)
					{
						m_unresponsiveFlag = false;
						sendEvent(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, propertiesMap);
					}
					break;
		
				case ServiceMonitor.SERVICE_UNAVAILABLE: 
					// Clear unresponsive flag
					m_unresponsiveFlag = false;
					break;
	
				default:
					break;
			}
		}
			
		// Any change in status?
		//
		if(status != m_status)
		{
			// get the time of the status change
			//
			m_statusChangedFlag = true;
			m_statusChangeTime = System.currentTimeMillis();
				
			// Is node outage processing disabled? 
			if (!PollerConfigFactory.getInstance().nodeOutageProcessingEnabled())
			{
				// node outage processing disabled, go ahead and generate
				// transition events.
				if (log.isDebugEnabled())
					log.debug("poll: node outage disabled, status change will trigger event.");
	
				// get the "qualifier" property from the properties map if it exists.
				// This is mainly used by HTTP at the moment.
				//
				String qualifier = (String)propertiesMap.get("qualifier");
	
				// Send the appropriate event
				//
				switch(status)
				{
				case ServiceMonitor.SERVICE_AVAILABLE: // service up!
					sendEvent(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, propertiesMap);
					break;
	
				case ServiceMonitor.SERVICE_UNAVAILABLE: // service down!
					sendEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, propertiesMap);
					break;

				default:
					break;
				}
			}
		}

		// Set the new status
		m_status = status;
		
		// Reset poll immediate flag
		m_pollImmediate = false;
		
		// Reschedule the interface
		// 
		// NOTE: rescheduling now handled by PollableService.run()
		//reschedule(false);
			
		return m_status;
	}
}

