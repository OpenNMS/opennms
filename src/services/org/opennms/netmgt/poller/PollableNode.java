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
// 2002 Nov 13: Added the nodelabel and interfaceresolve variables to notifications.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <P>The PollableNode class...</P>
 * 
 * @author <A HREF="mailto:jamesz@blast.com">James Zuo</A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public class PollableNode
{
	/**
	 * nodeId
	 */
	private int	m_nodeId;
	
	/**
	 * last known/current status of the node
	 */
	private int	m_status;
	
	/**
	 * Set by poll() method.
	 */
	private boolean	m_statusChangedFlag;
	
	/** 
	 * Map of 'PollableInterface' objects keyed by IP address
	 */
	private Map	m_interfaces;
	
	/**
	 * Used to lock access to the PollableNode during a poll()
	 */
	private Object	m_lock;
	private boolean m_isLocked;
	
	private boolean m_isDeleted;
	
	private static final String EVENT_SOURCE = "OpenNMS.Poller";
	
	/** 
	 * Constructor.
	 */
	public PollableNode(int nodeId)
	{
		m_nodeId = nodeId;
		m_lock = new Object();
		m_isLocked = false;
		m_interfaces = Collections.synchronizedMap(new HashMap());
		m_statusChangedFlag = false;
		m_status = Pollable.STATUS_UNKNOWN;
		m_isDeleted = false;
	}
	
	public int getNodeId()
	{
		return m_nodeId;
	}
	
	public Collection getInterfaces()
	{
		return m_interfaces.values();
	}
	
	public synchronized void addInterface(PollableInterface pInterface)
	{
		m_interfaces.put(pInterface.getAddress().getHostAddress(), pInterface);
		this.recalculateStatus();
	}
	
	public synchronized void deleteAllInterfaces()
	{
		m_interfaces.clear();
	}
	
	public PollableInterface getInterface(String ipAddress)
	{
		return (PollableInterface)m_interfaces.get(ipAddress);
	} 
	
	public synchronized void removeInterface(PollableInterface pInterface)
	{
		m_interfaces.remove(pInterface.getAddress().getHostAddress());
		this.recalculateStatus();
	}
	
	public int getStatus()
	{
		return m_status;
	}
	
	public boolean statusChanged()
	{
		return m_statusChangedFlag;
	}
	
	public synchronized void resetStatusChanged()
	{
		m_statusChangedFlag = false;
		
		// Iterate over interface list and reset each interface's 
		// status changed flag
		Iterator i = m_interfaces.values().iterator();
		while (i.hasNext())
		{
			PollableInterface pIf = (PollableInterface)i.next();
			pIf.resetStatusChanged();
		}
	}
	
	public void markAsDeleted()
	{
		m_isDeleted = true;
	}
	
	public boolean isDeleted()
	{
		return m_isDeleted;
	}
	
	/**
	 * Responsible for recalculating this node's UP/DOWN status.
	 */
	public synchronized void recalculateStatus()
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
		log.debug("recalculateStatus: nodeId=" + m_nodeId);
		
		int status = Pollable.STATUS_UNKNOWN;
		
		// Inspect status of each of the node's interfaces
		// in order to determine the current status of the node.
		//
		boolean allInterfacesDown = true;
		Iterator iter = m_interfaces.values().iterator();
		while (iter.hasNext())
		{
			PollableInterface pIf = (PollableInterface)iter.next();
			if (pIf.getStatus() == Pollable.STATUS_UNKNOWN)
				pIf.recalculateStatus();
				
			if (pIf.getStatus() == Pollable.STATUS_UP)
			{
				if (log.isDebugEnabled())
				log.debug("recalculateStatus: interface=" + pIf.getAddress().getHostAddress() + 
						" status=Up, atleast one interface is UP!");
				allInterfacesDown = false;
				break;
			}
		}
		
		if (allInterfacesDown)
			status = Pollable.STATUS_DOWN;
		else
			status = Pollable.STATUS_UP;
			
		m_status = status;
		
		if (log.isDebugEnabled())
		log.debug("recalculateStatus: completed, nodeId=" + m_nodeId + " status=" + Pollable.statusType[m_status]);
	
	}
	
	public boolean getNodeLock(long timeout)
		throws InterruptedException
	{
		boolean ownLock = false;
		
		synchronized(m_lock)
		{
			// Is the node currently locked?
			if (!m_isLocked)
			{
				// Now it is...
				m_isLocked = true;
				ownLock = true;
			}
			else
			{
				// Someone else has the lock, wait
				// for the specified timeout...
				m_lock.wait(timeout);
				
				// Was the lock released?
				if (!m_isLocked)
				{
					// Yep...
					m_isLocked = true;
					ownLock = true;
				}
			}
		}
		
		return ownLock;
	}
	
	public void releaseNodeLock()
		throws InterruptedException
	{
		synchronized(m_lock)
		{
			if (m_isLocked)
			{
				m_isLocked = false;
				m_lock.notifyAll();
			}
		}
	} 

	public void generateEvents()
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		Events events = new Events();
		
		// Create date object which will serve as the source
		// for the time on all generated events
		java.util.Date date    = new java.util.Date();

		if (m_statusChangedFlag && m_status == Pollable.STATUS_DOWN)
		{
			// create nodeDown event and add it to the event list
			events.addEvent( createEvent(EventConstants.NODE_DOWN_EVENT_UEI, 
							null, null, date) );
			
			resetStatusChanged();
		}
		else if (m_statusChangedFlag && m_status == Pollable.STATUS_UP)
		{
			// send nodeUp event
			events.addEvent( createEvent(EventConstants.NODE_UP_EVENT_UEI, 
							null, null, date) );
			resetStatusChanged();

			// iterate over the node's interfaces
			//	if interface status is DOWN 
			//		generate interfaceDown event
			//	else if interface status is UP 
			//		iterate over interface's services 
			//			if service status is DOWN
			//				generate serviceDown event
			//
			Iterator i = m_interfaces.values().iterator();
			while (i.hasNext())
			{
				PollableInterface pIf = (PollableInterface)i.next();
				if (pIf.getStatus() == Pollable.STATUS_DOWN)
				{
					events.addEvent( createEvent(EventConstants.INTERFACE_DOWN_EVENT_UEI, 
									pIf.getAddress(), 
									null, 
									date) );
					pIf.resetStatusChanged();
				}
				else if (pIf.getStatus() == Pollable.STATUS_UP)
				{
					Iterator s = pIf.getServices().iterator();
					while (s.hasNext())
					{
						PollableService pSvc = (PollableService)s.next();
						if (pSvc.getStatus() == Pollable.STATUS_DOWN)
						{
							events.addEvent( createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, 
											pIf.getAddress(), 
											pSvc.getServiceName(),
											date) );
							pSvc.resetStatusChanged();
						}
					}
				}
			}
		}
		else if (m_status == Pollable.STATUS_UP)
		{
			// iterate over the node's interfaces
			// 	if status of interface changed to DOWN
			//		generate interfaceDown event
			//	else if status of interface changed to UP
			//		generate interfaceUp event
			//		iterate over interface's services
			//			if status of service is DOWN
			//				generate serviceDown event
			//	else
			//		iterate over interface's services
			//			if status of service changed to UP
			//				generate serviceUp event
			//			else if status of service changed to DOWN
			//				generate serviceDown event
			//
			Iterator i = m_interfaces.values().iterator();
			while (i.hasNext())
			{
				PollableInterface pIf = (PollableInterface)i.next();
				if (pIf.statusChanged() && pIf.getStatus() == Pollable.STATUS_DOWN)
				{
					events.addEvent( createEvent(EventConstants.INTERFACE_DOWN_EVENT_UEI, 
									pIf.getAddress(), 
									null,
									date) );
					pIf.resetStatusChanged();
				}
				else if (pIf.statusChanged() && pIf.getStatus() == Pollable.STATUS_UP)
				{
					events.addEvent( createEvent(EventConstants.INTERFACE_UP_EVENT_UEI, 
							pIf.getAddress(), 
							null,
							date) );
					pIf.resetStatusChanged();
					
					Iterator s = pIf.getServices().iterator();
					while (s.hasNext())
					{
						PollableService pSvc = (PollableService)s.next();
						if (pSvc.getStatus() == Pollable.STATUS_DOWN)
						{
							events.addEvent( createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, 
											pIf.getAddress(), 
											pSvc.getServiceName(),
											date) );
							pSvc.resetStatusChanged();
						}
					}
				}
				else
				{
					Iterator s = pIf.getServices().iterator();
					while (s.hasNext())
					{
						PollableService pSvc = (PollableService)s.next();
						if (pSvc.statusChanged() && pSvc.getStatus() == Pollable.STATUS_DOWN)
						{
							events.addEvent( createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, 
											pIf.getAddress(), 
											pSvc.getServiceName(),
											date) );
							pSvc.resetStatusChanged();
						}
						else if (pSvc.statusChanged() && pSvc.getStatus() == Pollable.STATUS_UP)
						{
							events.addEvent( createEvent(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, 
									pIf.getAddress(), 
									pSvc.getServiceName(),
									date) );
							pSvc.resetStatusChanged();
						}
					}
				}
			}
		}
		
		// Send events to eventd
		if (events.getEventCount() > 0)
		{
			try
			{
				Log eventLog = new Log();
				eventLog.setEvents(events);
				EventIpcManagerFactory.getInstance().getManager().sendNow(eventLog);
			}
			catch(RuntimeException e)
			{
				log.error("generateEvents: Failed sending events to eventd...", e);
			}
			catch(Throwable t)
			{
				log.error("generateEvents: Failed sending events to eventd...", t);
			}
		}
	}
	
	private Event createEvent(String 	uei, 
				InetAddress 	address, 
				String		svcName,
				java.util.Date  date)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled()) 
			log.debug("createEvent: uei = " + uei + " nodeid = " + m_nodeId);
		
		// create the event to be sent
		Event newEvent = new Event();
		newEvent.setUei(uei);
		newEvent.setSource(EVENT_SOURCE);
		newEvent.setNodeid((long)m_nodeId);
		if (address != null)
			newEvent.setInterface(address.getHostAddress());
		
		if (svcName != null)
			newEvent.setService(svcName);
		
		try
		{
			newEvent.setHost(InetAddress.getLocalHost().getHostName());
		}
		catch(UnknownHostException uhE)
		{
			newEvent.setHost("unresolved.host");
			log.warn("Failed to resolve local hostname", uhE);
		}
		
		// Set event time
		newEvent.setTime(EventConstants.formatToString(date));
		
		// For node level events (nodeUp/nodeDown) retrieve the
		// node's nodeLabel value and add it as a parm
		if (uei.equals(EventConstants.NODE_UP_EVENT_UEI) ||
			uei.equals(EventConstants.NODE_DOWN_EVENT_UEI))
		{
			String nodeLabel = null;
			try
			{
				nodeLabel = getNodeLabel(m_nodeId);
			}
			catch (SQLException sqlE)
			{
				 // Log a warning
				 log.warn("Failed to retrieve node label for nodeid " + m_nodeId, sqlE);
			}
			 
			if (nodeLabel == null)
			{
				// This should never happen but if it does just
				// use nodeId for the nodeLabel so that the 
				// event description has something to display.
				nodeLabel = String.valueOf(m_nodeId);
			}
			
			// Add appropriate parms
			Parms eventParms = new Parms();
			
			// Add nodelabel parm
			Parm eventParm = new Parm();
			eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
			Value parmValue = new Value();
			parmValue.setContent(nodeLabel);
			eventParm.setValue(parmValue);
			eventParms.addParm(eventParm);
			
			// Add Parms to the event
			newEvent.setParms(eventParms);
		}

		// The following code can be uncommented to add the nodelabel parm
		// to Interface Up/Down events. This is deprecated with the addition of 
		// special tags in EventUtil.
                // else if (uei.equals(EventConstants.INTERFACE_UP_EVENT_UEI) ||
                //         uei.equals(EventConstants.INTERFACE_DOWN_EVENT_UEI))
                // {
                //         String nodeLabel = null;
                //         try
                //         {
                //                 nodeLabel = getIntNodeLabel(address);
                //         }
                //         catch (SQLException sqlE)
                //         {
                //                  // Log a warning
                //                  log.warn("Failed to retrieve node label for nodeid " + address, sqlE);
                //         }

                //         if (nodeLabel == null)
                //         {
                //                 // This should never happen but if it does just
                //                 // use nodeId for the nodeLabel so that the
                //                 // event description has something to display.
                //                 nodeLabel = (String)address.getHostAddress();
                //         }

                //         // Add appropriate parms
                //         Parms eventParms = new Parms();

                //         // Add nodelabel parm
                //         Parm eventParm = new Parm();
                //         eventParm.setParmName(EventConstants.PARM_NODE_LABEL);
                //         Value parmValue = new Value();
                //         parmValue.setContent(nodeLabel);
                //         eventParm.setValue(parmValue);
                //         eventParms.addParm(eventParm);

                //         // Add Parms to the event
                //         newEvent.setParms(eventParms);
                // }

		
		return newEvent;
	}
	
	/** 
	 * Retrieve nodeLabel from the node table of the database
	 * given a particular nodeId.
	 *
	 * @param nodeId	Node identifier
	 * 
	 * @return nodeLabel	Retreived nodeLabel
	 * 
	 * @throws SQLException if database error encountered
	 */
	private String getNodeLabel(int nodeId)
		throws SQLException
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		String nodeLabel = null;
		java.sql.Connection dbConn = null;
		Statement stmt = null;
		try
		{
			// Get datbase connection from the factory
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();

			// Issue query and extract nodeLabel from result set
			stmt = dbConn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT nodelabel FROM node WHERE nodeid=" + String.valueOf(nodeId));
			if (rs.next())
			{
				nodeLabel = (String)rs.getString("nodelabel");
				if (log.isDebugEnabled())
					log.debug("getNodeLabel: nodeid=" + nodeId + " nodelabel=" + nodeLabel);
			}
		}
		finally
		{
			// Close the statement
			if (stmt != null)
			{
				try
				{
					stmt.close();
				}
				catch (Exception e)
				{
					if(log.isDebugEnabled())
						log.debug("getNodeLabel: an exception occured closing the SQL statement", e);
				}
			}

			// Close the database connection
			if(dbConn != null)
			{
				try
				{
					dbConn.close();
				}
				catch(Throwable t)
				{
					if(log.isDebugEnabled())
						log.debug("getNodeLabel: an exception occured closing the SQL connection", t);
				}
			}
		}
		
		return nodeLabel;
	}

        /**
         * Retrieve nodeLabel from the node table of the database
         * given a particular IP Address.
         *
         * @param ipaddr        Interface IP Address
         *
         * @return nodeLabel    Retreived nodeLabel
         *
         * @throws SQLException if database error encountered
         */
        private String getIntNodeLabel(InetAddress ipaddr)
                throws SQLException
        {
                Category log = ThreadCategory.getInstance(getClass());

                String nodeLabel = null;
                java.sql.Connection dbConn = null;
                Statement stmt = null;
                try
                {
                        // Get datbase connection from the factory
                        dbConn = DatabaseConnectionFactory.getInstance().getConnection();

                        // Issue query and extract nodeLabel from result set
                        stmt = dbConn.createStatement();
			String sql = "SELECT node.nodelabel FROM node, ipinterface WHERE ipinterface.ipaddr='" + ipaddr.getHostAddress() + "' AND ipinterface.nodeid=node.nodeid";
                        ResultSet rs = stmt.executeQuery(sql);
                        if (rs.next())
                        {
                                nodeLabel = rs.getString(1);
                                if (log.isDebugEnabled())
                                        log.debug("getNodeLabel: ipaddr=" + ipaddr.getHostAddress() + " nodelabel=" + nodeLabel);
                        }
                }
                finally
                {
                        // Close the statement
                        if (stmt != null)
                        {
                                try
                                {
                                        stmt.close();
                                }
                                catch (Exception e)
                                {
                                        if(log.isDebugEnabled())
                                                log.debug("getNodeLabel: an exception occured closing the SQL statement", e);
                                }
                        }

                        // Close the database connection
                        if(dbConn != null)
                        {
                                try
                                {
                                        dbConn.close();
                                }
                                catch(Throwable t)
                                {
                                        if(log.isDebugEnabled())
                                                log.debug("getNodeLabel: an exception occured closing the SQL connection", t);
                                }
                        }
                }

                return nodeLabel;
        }

	/**  
	 * Invokes a poll of the remote interface. 
	 * 
	 * If the interface changes status then node outage processing 
	 * will be invoked and the status of the entire node will be
	 * evaluated.
	 */
	public synchronized int poll(PollableService pSvc)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		if (log.isDebugEnabled())
			log.debug("poll: polling nodeid " + m_nodeId + " status=" + Pollable.statusType[m_status]);
					
		m_statusChangedFlag = false;
		
		// Retrieve PollableInterface object from the NIF
		PollableInterface pInterface = pSvc.getInterface();
		
		int ifStatus = Pollable.STATUS_UNKNOWN;
		
		// Get critical service
		String criticalSvc = PollerConfigFactory.getInstance().getCriticalService();
		
		// Polling logic if node is currently DOWN
		//
		if (m_status == Pollable.STATUS_DOWN)
		{
			// Poll the service via the PollableInterface object
			ifStatus = pInterface.poll(pSvc);
			
			// If interface status changed to UP
			if (ifStatus == Pollable.STATUS_UP && pInterface.statusChanged())
			{
				// Check status of node's other interfaces
				//
				if (m_interfaces.size() > 1)
				{
					// Iterate over list of interfaces
					Iterator iter = m_interfaces.values().iterator();
					while (iter.hasNext())
					{
						PollableInterface pIf = (PollableInterface)iter.next();
						
						// Skip interface that was already polled
						if (pIf == pInterface)
							continue;
							
						// If critical service defined and interface supports the 
						// critical service (regardless of package) then poll the 
						// interface passing it the critical service
					        if (criticalSvc != null && pInterface.supportsService(criticalSvc))
						{
						        PollableService criticalNif = pInterface.getService(criticalSvc);
							pIf.poll(criticalNif);
						}
						else
						{
							// No critical service defined or interface doesn't
							// support it...still need to test the interface so 
							// simply retrieve any (.i.e, the first) service supported
							// by the interface and poll the interface passing it 
							// that service
							Iterator s = pInterface.getServices().iterator();
							PollableService firstSvc = (PollableService)s.next();
							pIf.poll(firstSvc);
						}
					}
				}
				
				// Mark node as UP 
				m_status = Pollable.STATUS_UP;
				m_statusChangedFlag = true;				
			}
		}
		// Polling logic if node is currently UP
		//
		else if (m_status == Pollable.STATUS_UP)
		{
			// Poll the service via the PollableInterface object
			ifStatus = pInterface.poll(pSvc);
			
			// If interface status changed to DOWN
			if (ifStatus == Pollable.STATUS_DOWN && pInterface.statusChanged())
			{
				boolean allInterfacesDown = true;
				
				log.debug("poll: requested interface is down; testing remaining interfaces");
				// Check status of node's other interfaces to determine
				// if ALL the interfaces on the node are now DOWN
				//
				if (m_interfaces.size() > 1)
				{
					// Iterate over list of interfaces
					Iterator iter = m_interfaces.values().iterator();
					while (iter.hasNext())
					{
						PollableInterface pIf = (PollableInterface)iter.next();
						
						// Skip the interface that was already polled
						if (pIf == pInterface)
							continue;
							
					        log.debug("poll: (node outage) testing interface " 
                                                        + pIf.getAddress().getHostAddress());
					
						// If critical service defined and interface supports the 
						// critical service (regardless of package) then poll the 
						// interface passing it the critical service
						int tmpStatus = Pollable.STATUS_UNKNOWN;
					        if (criticalSvc != null && pInterface.supportsService(criticalSvc))
						{
						        PollableService criticalNif = pIf.getService(criticalSvc);
							tmpStatus = pIf.poll(criticalNif);
						}
						else
						{
							// No critical service defined or interface doesn't
							// support it...still need to test the interface so 
							// simply retrieve any (.i.e, the first) service supported
							// by the interface and poll the interface passing it 
							// that service
						        Iterator s = pIf.getServices().iterator();
							PollableService firstSvc = (PollableService)s.next();
							tmpStatus = pIf.poll(firstSvc);
						}
						
						if (tmpStatus == Pollable.STATUS_UP)
					        {
							allInterfacesDown = false;
						        log.debug("poll: (node outage) not a node outage - at least one interface is up");
					        }
					}
				}
				
				// If all interfaces are now DOWN then mark node DOWN
				if (allInterfacesDown)
				{
					m_status = Pollable.STATUS_DOWN;
					m_statusChangedFlag = true;
				}
			}
		}
		
		// Call generateEvents() which will inspect the current status
		// of the N/I/S tree and generate any events necessary to keep
		// RTC and OutageManager in sync.
		generateEvents();
		
		if (log.isDebugEnabled())
			log.debug("poll: poll of nodeid " + m_nodeId + " completed, status=" + Pollable.statusType[m_status]);
			
		return m_status;
	}
}
