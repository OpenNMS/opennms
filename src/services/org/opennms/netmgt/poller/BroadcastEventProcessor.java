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
// 2004 Jan 06: Added support for SUSPEND_POLLING_SERVICE_EVENT_UEI and
// 		RESUME_POLLING_SERVICE_EVENT_UEI
// 2003 Nov 11: Merged changes from Rackspace project
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
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.utils.XmlrpcUtil;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 *
 * @author <a href="mailto:jamesz@opennms.com">James Zuo</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
final class BroadcastEventProcessor
	implements EventListener
{
	/**
	 * SQL statement used to delete oustanding SNMP service outages for the specified
	 * nodeid/interface in the event in the event of a primary snmp interface changed
	 * event.
	 */
	private static String SQL_DELETE_SNMP_OUTAGE = "DELETE FROM outages WHERE nodeid=? AND ipaddr=? AND ifregainedservice=null AND outages.serviceid=service.serviceid AND service.servicename='SNMP'";
	
	/**
	 * SQL statement used to query the 'ifServices' for a nodeid/ipaddr/service
	 * combination on the receipt of a 'nodeGainedService' to make sure there is
	 * atleast one row where the service status for the tuple is 'A'.
	 */
	private static String SQL_COUNT_IFSERVICE_STATUS = "select count(*) FROM ifServices, service WHERE nodeid=? AND ipaddr=? AND status='A' AND ifServices.serviceid=service.serviceid AND service.servicename=?";
	
        /**
         * SQL statement used to count the active ifservices on the specified ip address.
         */
        private static String SQL_COUNT_IFSERVICES_TO_POLL = "SELECT COUNT(*) FROM ifservices WHERE status = 'A' AND ipaddr = ?";

        /**
         * SQL statement used to retrieve an active ifservice for the scheduler to poll.
         */
        private static String SQL_FETCH_IFSERVICES_TO_POLL = "SELECT if.serviceid, s.servicename FROM ifservices if, service s WHERE if.serviceid = s.serviceid AND if.status = 'A' AND if.ipaddr = ?";
        
        /**
         * SQL statement used to retrieve nodeid from the database for a specified ip address.
         */
        private static String SQL_RETRIEVE_NODE_ID = "SELECT nodeid FROM ipinterface WHERE ipaddr = ?";


        /**
	 * Integer constant for passing in to PollableNode.getNodeLock() method
	 * in order to indicate that the method should block until node lock is 
	 * available.
	 */
	private static int WAIT_FOREVER = 0;
	
	/**
	 * The map of service names to service models.
	 */
	private Map		m_monitors;

	/**
	 * The scheduler assocated with this reciever
	 */
	private Scheduler	m_scheduler;

	/**
	 * List of PollableService objects.
         */
	private	List		m_pollableServices;
	
        /**
         * Boolean flag to indicate if need to notify external xmlrpc server with
         * event processing failure.
         */
        private boolean         m_xmlrpc = false;

	/**
	 * Create message selector to set to the subscription
	 */
	private void createMessageSelectorAndSubscribe()
	{
		// Create the selector for the ueis this service is interested in
		//
		List ueiList = new ArrayList();

		// nodeGainedService
		ueiList.add(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);

                // restartPollingInterface
                ueiList.add(EventConstants.RESTART_POLLING_INTERFACE_EVENT_UEI);
                
                // serviceDeleted
		// deleteService
		/* NOTE:  deleteService is only generated by the PollableService
		 * itself.  Therefore, we ignore it.  If future implementations
		 * allow other subsystems to generate this event, we may have
		 * to listen for it as well.
		 * 'serviceDeleted' is the response event that the outage manager
		 * generates.  We ignore this as well, since the PollableService
		 * has already taken action at the time it generated 'deleteService'
		 */
		ueiList.add(EventConstants.SERVICE_DELETED_EVENT_UEI);
		//ueiList.add(EventConstants.DELETE_SERVICE_EVENT_UEI);

		// serviceManaged
		// serviceUnmanaged
		// interfaceManaged
		// interfaceUnmanaged
		/* NOTE:  These are all ignored because the responsibility
		 * is currently on the class generating the event to restart
		 * the poller service.  If that implementation is ever
		 * changed, this message selector should listen for these and 
		 * act on them.
		 */
		//ueiList.add(EventConstants.SERVICE_MANAGED_EVENT_UEI);
		//ueiList.add(EventConstants.SERVICE_UNMANAGED_EVENT_UEI);
		//ueiList.add(EventConstants.INTERFACE_MANAGED_EVENT_UEI);
		//ueiList.add(EventConstants.INTERFACE_UNMANAGED_EVENT_UEI);

		// interfaceIndexChanged
		// NOTE:  No longer interested in this event...if Capsd detects
		//        that in interface's index has changed a 
		//        'reinitializePrimarySnmpInterface' event is generated.
		//ueiList.add(EventConstants.INTERFACE_INDEX_CHANGED_EVENT_UEI);

		// interfaceReparented
		ueiList.add(EventConstants.INTERFACE_REPARENTED_EVENT_UEI);

		// reloadPollerConfig
		/* NOTE:  This is ignored because the reload is handled through
		 * an autoaction.
		 */
		//ueiList.add(EventConstants.RELOAD_POLLER_CONFIG_EVENT_UEI);


		// NODE OUTAGE RELATED EVENTS
		// 

		// nodeAdded
		/* NOTE:  This is ignored.  The real trigger will be the first
		 * nodeGainedService event, at which time the interface and
		 * node will be created
		 */
		//ueiList.add(EventConstants.NODE_ADDED_EVENT_UEI);
		
		// nodeDeleted
		ueiList.add(EventConstants.NODE_DELETED_EVENT_UEI);
		
		// duplicateNodeDeleted
		ueiList.add(EventConstants.DUP_NODE_DELETED_EVENT_UEI);
		
		// nodeGainedInterface
		/* NOTE:  This is ignored.  The real trigger will be the first
		 * nodeGainedService event, at which time the interface and
		 * node will be created
		 */
		//ueiList.add(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI);

		// interfaceDeleted
		ueiList.add(EventConstants.INTERFACE_DELETED_EVENT_UEI);

		// suspendPollingService
		 ueiList.add(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI);

		 // resumePollingService
		 ueiList.add(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI);
		
		// Subscribe to eventd
		EventIpcManagerFactory.init();
		EventIpcManagerFactory.getInstance().getManager().addEventListener(this, ueiList);
	}

	/**
	 * Process the event, construct a new PollableService object representing
 	 * the node/interface/service/pkg combination, and schedule the service
	 * for polling. 
	 * 
	 * If any errors occur scheduling the interface no error is returned.
	 *
	 * @param event	The event to process.
	 *
	 */
	private void nodeGainedServiceHandler(Event event)
	{
		Category log = ThreadCategory.getInstance(getClass());

		// Is this the result of a resumePollingService event?
		String whichEvent = "Unexpected Event: " + event.getUei() + ": ";
		if(event.getUei().equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI))
		{
			whichEvent = "nodeGainedService: ";
		}
		else if(event.getUei().equals(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI))
		{
			whichEvent = "resumePollingService: ";
		}

		// First make sure the service gained is in active state before trying to
		// schedule
		java.sql.Connection dbConn = null;
		PreparedStatement stmt = null;
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
		
			stmt = dbConn.prepareStatement(SQL_COUNT_IFSERVICE_STATUS);
	
			stmt.setInt(1, (int)event.getNodeid());
			stmt.setString(2, event.getInterface());
			stmt.setString(3, event.getService());
	
			int count = -1;
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				count = rs.getInt(1);
			}

			// count should be 1 to indicate an active status
			if (count <= 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug(whichEvent + "number check to see if service is in status: " + count); 
					log.debug(whichEvent + event.getNodeid() + "/" + event.getInterface() 
                                                + "/" + event.getService() + " not active - hence not scheduled");
				}
				return;
			}

			if (log.isDebugEnabled())
				log.debug(whichEvent + event.getNodeid() + "/" + event.getInterface() 
                                        + "/" + event.getService() + " active");
		}
		catch(SQLException sqlE)
		{
			log.error("SQLException during check to see if nodeid/ip/service is active", sqlE);
		}
		finally
		{
			// close the statement
			if (stmt != null)
				try { stmt.close(); } catch(SQLException sqlE) { };

			// close the connection
			if (dbConn != null)
				try { dbConn.close(); } catch(SQLException sqlE) { };					
		}
		
		PollerConfigFactory pCfgFactory = PollerConfigFactory.getInstance();
		PollerConfiguration config =  pCfgFactory.getConfiguration();
		Enumeration epkgs = config.enumeratePackage();
		while(epkgs.hasMoreElements())
		{
			org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package)epkgs.nextElement();
			
			// Make certain the the current service is in the package
			// and enabled!
			//
			if (!pCfgFactory.serviceInPackageAndEnabled(event.getService(), pkg))
			{
				if(log.isDebugEnabled())
					log.debug(whichEvent + "interface " + event.getInterface() + 
							" gained service " + event.getService() + 
							", but the service is not enabled or does not exist in package: " 
							+ pkg.getName());
				continue;
			}
					
			// Is the interface in the package?
			//
			if(!pCfgFactory.interfaceInPackage(event.getInterface(), pkg))
			{
                                // The interface might be a newly added one, rebuild the package
                                // to ipList mapping and again verify if the interface is in the 
                                // package.
                                //
                                pCfgFactory.rebuildPackageIpListMap();
			        if(!pCfgFactory.interfaceInPackage(event.getInterface(), pkg))
                                {
				        if(log.isDebugEnabled())
					        log.debug("nodeGainedService: interface " + event.getInterface() + 
						          " gained service " + event.getService() + 
							  ", but the interface was not in package: " 
							  + pkg.getName());
				        continue;
                                }
			}
			
			// Update Node Outage Hierarchy and schedule new service for polling
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
				int nodeId = (int)event.getNodeid();
				pNode = Poller.getInstance().getNode(nodeId);
				log.debug(whichEvent + "attempting to retrieve pollable node object for nodeid " + nodeId);
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
				pInterface = pNode.getInterface(event.getInterface());
				if (pInterface == null)
				{
					// Create the PollableInterface and add it to the node
					if (log.isDebugEnabled())
						log.debug(whichEvent + "creating new pollable interface: " + event.getInterface() + 
								" to pollable node " + pNode.getNodeId());
					pInterface = new PollableInterface(pNode, InetAddress.getByName(event.getInterface()));
					interfaceCreated = true;
				}
				
				// Create a new PollableService representing this node, interface,
				// service and package pairing
				log.debug(whichEvent + "creating new pollable service object for: " + nodeId + "/" 
                                        + event.getInterface() + "/" + event.getService());
				pSvc = new PollableService(pInterface,
								event.getService(),
								pkg,
								ServiceMonitor.SERVICE_AVAILABLE,
								new Date());

				// Initialize the service monitor with the pollable service and schedule 
				// the service for polling. 
				//							
				ServiceMonitor monitor = Poller.getInstance().getServiceMonitor(event.getService());
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
				//           map.  THIS IS BY DESIGN
				log.debug(whichEvent + "adding pollable service to service list of interface: " 
                                        + event.getInterface());
				pInterface.addService(pSvc);
				
				if (interfaceCreated)
				{
					// Add the interface to the node
					//
					// NOTE:  addInterface() calls recalculateStatus() automatically
					if (log.isDebugEnabled())
						log.debug(whichEvent + "adding new pollable interface " + 
								event.getInterface() + " to pollable node " + pNode.getNodeId());
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
						log.debug(whichEvent + "adding new pollable node: " + pNode.getNodeId());
					Poller.getInstance().addNode(pNode);
				}
								
				// Schedule the service for polling
				m_scheduler.schedule(pSvc, pSvc.recalculateInterval());
				if (log.isDebugEnabled())
					log.debug(whichEvent + event.getNodeid() + "/" + event.getInterface() + 
							"/" + event.getService() + " scheduled ");
			}
			catch(UnknownHostException ex)
			{
				log.error("Failed to schedule interface " + event.getInterface() + 
						" for service monitor " + event.getService() + ", illegal address", ex);
			}
			catch(InterruptedException ie)
			{
				log.error("Failed to schedule interface " + event.getInterface() + 
						" for service monitor " + event.getService() + ", thread interrupted", ie);
			}
			catch(RuntimeException rE)
			{
				log.warn("Unable to schedule " + event.getInterface() + " for service monitor " + event.getService() + 
						", reason: " + rE.getMessage());
			}
			catch(Throwable t)
			{
				log.error("Uncaught exception, failed to schedule interface " + event.getInterface() + 
						" for service monitor " + event.getService(), t);
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
						log.error("Failed to release node lock on nodeid " + 
								pNode.getNodeId() + ", thread interrupted.");
					}
				}
			}
				
		} // end while more packages exist
	}
	
	/**
	 * Process the event, construct new PollableService object representing
 	 * the node/interface/service/pkg combination, and schedule all services
	 * on the specified interface for polling. 
	 * 
	 * If any errors occur scheduling the interface no error is returned, but 
         * any SQL Exception will cause the process to stop, and an error will be
         * logged.
	 *
	 * @param event	The event to process.
	 *
	 */
	private void restartPollingInterfaceHandler(Event event)
	{
                String ipaddr = event.getInterface();
                String sourceUei = event.getUei();

		Category log = ThreadCategory.getInstance(getClass());
		if (log.isDebugEnabled())
			log.debug("restartPollingInterfaceHandler: start process event for interface: " + ipaddr);

                // Extract node label and transaction No. from the event parms
                long txNo = -1L;
                Parms parms = event.getParms();
                if (parms != null)
                {
                        String parmName = null;
                        Value parmValue = null;
                        String parmContent = null;
                                                   
                        Enumeration parmEnum = parms.enumerateParm();
                        while(parmEnum.hasMoreElements())
                        {
                                Parm parm = (Parm)parmEnum.nextElement();
                                parmName  = parm.getParmName();
                                parmValue = parm.getValue();
                                if (parmValue == null)
                                        continue;
                                else
                                        parmContent = parmValue.getContent();

                                //  get the external transaction number
                                if (parmName.equals(EventConstants.PARM_TRANSACTION_NO))
                                {
                                        String temp = parmContent;
                                        if (log.isDebugEnabled())
                                                log.debug("restartPollingInterfaceHandler:  parmName: " + parmName
                                                        + " /parmContent: " + parmContent);
                                        try
                                        {
                                                txNo = Long.valueOf(temp).longValue();
                                        }
                                        catch (NumberFormatException nfe)
                                        {
                                                log.warn("restartPollingInterfaceHandler: Parameter " 
                                                        + EventConstants.PARM_TRANSACTION_NO
                                                        + " cannot be non-numberic", nfe);
                                                txNo = -1;
                                        }
                                }
                        }
                }

                boolean invalidParameters = (ipaddr == null);
                if (m_xmlrpc)
                        invalidParameters = invalidParameters || (txNo == -1L);

                if (invalidParameters)
                {
                        if (log.isDebugEnabled())
                                log.debug("restartPollingInterfaceHandler: Invalid parameters.");

                        if (m_xmlrpc)
                        {
                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                XmlrpcUtil. createAndSendXmlrpcNotificationEvent(txNo, sourceUei, "Invalid parameters", 
                                        status, "OpenNMS.Poller");
                        }
                }
		
                java.sql.Connection dbConn = null;
		PreparedStatement stmt = null;
		
		try
		{
			dbConn = DatabaseConnectionFactory.getInstance().getConnection();
		
                        // retrieve the nodeid
			stmt = dbConn.prepareStatement(SQL_RETRIEVE_NODE_ID);
	                int nodeid = -1;
                        
			stmt.setString(1, ipaddr);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				nodeid  = rs.getInt(1);
		                if (log.isDebugEnabled())
			                log.debug("restartPollingInterfaceHandler: retrieved the nodeid: " + nodeid 
                                                + " for interface: " + ipaddr);
			}

                        if (nodeid < 0)
                        {
                                log.error("restartPollingInterfaceHandler: node does not exist.");
                                if (m_xmlrpc)
                                {
                                        int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                        XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                "Node does not exist.", status, "OpenNMS.Poller");
                                }
                                return;
                        }        
		        stmt.close();
               
                        // Count active services to poll
			stmt = dbConn.prepareStatement(SQL_COUNT_IFSERVICES_TO_POLL);
	                int count = -1;
                        
			stmt.setString(1, ipaddr);
	
			rs = stmt.executeQuery();
			while(rs.next())
			{
			        count  = rs.getInt(1);
		                if (log.isDebugEnabled())
			                log.debug("restartPollingInterfaceHandler: count active ifservices to poll for interface: " + ipaddr);
			}

			if (count <= 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("restartPollingInterfaceHandler: counted services in status to poll: " + count); 
					log.debug("restartPollingInterfaceHandler: " + nodeid + "/" + ipaddr +
                                                " has no active services to poll");
				}
				return;
			}

			if (log.isDebugEnabled())
				log.debug("restartPollingInterfaceHandler: " + nodeid + "/" + ipaddr + 
                                " has " + count +  " active services to poll");
                        stmt.close();

                        // Fetch each active service to poll
			stmt = dbConn.prepareStatement(SQL_FETCH_IFSERVICES_TO_POLL);
			stmt.setString(1, ipaddr);
			rs = stmt.executeQuery();
		        if (log.isDebugEnabled())
			        log.debug("restartPollingInterfaceHandler: retrieve active service to poll on interface: " + ipaddr);

			while(rs.next())
			{
			        int serviceid  = rs.getInt(1);
                                String serviceName = rs.getString(2);
		                
                                if (log.isDebugEnabled())
			                log.debug("restartPollingInterfaceHandler: retrieved active service: " + serviceName);

		                PollerConfigFactory pCfgFactory = PollerConfigFactory.getInstance();
		                PollerConfiguration config =  pCfgFactory.getConfiguration();
		                Enumeration epkgs = config.enumeratePackage();
		                while(epkgs.hasMoreElements())
		                {
			                org.opennms.netmgt.config.poller.Package pkg = 
                                                (org.opennms.netmgt.config.poller.Package)epkgs.nextElement();
			
			                // Make certain the service is in the package and enabled!
			                //
			                if (!pCfgFactory.serviceInPackageAndEnabled(serviceName, pkg))
			                {
				                if(log.isDebugEnabled())
					                log.debug("restartPollingInterfaceHandler: interface " + ipaddr + 
							        " has active service " + serviceName + 
							        ", but the service is not enabled or does not exist in package: " 
							        + pkg.getName());
				                continue;
			                }
					
			                // Is the interface in the package?
			                //
			                if(!pCfgFactory.interfaceInPackage(ipaddr, pkg))
			                {
				                if(log.isDebugEnabled())
					                log.debug("restartPollingInterfaceHandler: interface " + ipaddr + 
							" has active service " + serviceName + 
							", but the interface was not in package: " 
							+ pkg.getName());
				                continue;
			                }
			
			                // Update Node Outage Hierarchy and schedule new service for polling
			                //
			                PollableNode pNode = null;
			                PollableInterface pInterface = null;
			                PollableService pSvc = null;
			                boolean ownLock = false;
			                boolean nodeCreated = false;
			                boolean interfaceCreated = false;
			
				        // Does the node already exist in the poller's pollable node map?
				        //
                                        try 
                                        {
        				        pNode = Poller.getInstance().getNode(nodeid);
        				        log.debug("restartPollingInterfaceHandler: attempting to retrieve pollable "
                                                        + "node object for nodeid " + nodeid);
        				        if (pNode == null)
        				        {
        					        // Nope...so we need to create it
        					        pNode = new PollableNode(nodeid);
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
        				        pInterface = pNode.getInterface(ipaddr);
        				        if (pInterface == null)
        				        {
        					        // Create the PollableInterface and add it to the node
        					        if (log.isDebugEnabled())
        						        log.debug("restartPollingInterfaceHandler: creating new pollable interface: " 
                                                                + ipaddr + 
        							" to pollable node " + pNode.getNodeId());
        					        pInterface = new PollableInterface(pNode, 
                                                                        InetAddress.getByName(ipaddr));
        					        interfaceCreated = true;
        				        }
        				
        				        // Create a new PollableService representing this node, interface,
        				        // service and package pairing
        				        log.debug("restartPollingInterfaceHandler: creating new pollable service object for: " 
                                                + nodeid + "/" + ipaddr + "/" + serviceName);
        				
                                                pSvc = new PollableService(pInterface,
        								serviceName,
        								pkg,
        								ServiceMonitor.SERVICE_AVAILABLE,
        								new Date());
        
        		        		// Initialize the service monitor with the pollable service and schedule 
        			        	// the service for polling. 
        				        //							
        				        ServiceMonitor monitor = Poller.getInstance().getServiceMonitor(serviceName);
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
        				        //           map.  THIS IS BY DESIGN
        			        	log.debug("restartPollingInterfaceHandler: adding pollable service to service list " 
                                                        + "of interface: " + ipaddr);
        				        pInterface.addService(pSvc);
        				
        				        if (interfaceCreated)
        				        {
        					        // Add the interface to the node
        					        //
        					        // NOTE:  addInterface() calls recalculateStatus() automatically
        					        if (log.isDebugEnabled())
        						        log.debug("restartPollingInterfaceHandler: adding new pollable interface " + 
        								ipaddr + " to pollable node " + pNode.getNodeId());
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
        						        log.debug("restartPollingInterfaceHandler: adding new pollable node: " 
                                                                        + pNode.getNodeId());
        					        Poller.getInstance().addNode(pNode);
        				        }
        								
        				        // Schedule the service for polling
        				        m_scheduler.schedule(pSvc, pSvc.recalculateInterval());
        				        if (log.isDebugEnabled())
        					        log.debug("restartPollingInterfaceHandler: " + pNode.getNodeId() + "/" + ipaddr + 
        							"/" + serviceName + " scheduled ");
                                        }
                        		catch(UnknownHostException ex)
                        		{
                        			log.error("Failed to schedule interface " + ipaddr + 
                        					" for service monitor " + serviceName + ", illegal address", ex);
                                                if (m_xmlrpc)
                                                {
                                                        int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                                        XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                                        ex.getMessage(), status, "OpenNMS.Poller");
                                                }
                        		}
                        		catch(InterruptedException ie)
                        		{
                        			log.error("Failed to schedule interface " + ipaddr + 
                        					" for service monitor " + serviceName + ", thread interrupted", ie);
                                                if (m_xmlrpc)
                                                {
                                                        int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                                        XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                                        ie.getMessage(), status, "OpenNMS.Poller");
                                                }
                        		}
                        		catch(RuntimeException rE)
                        		{
                        			log.warn("Unable to schedule " + ipaddr + " for service monitor " + serviceName + 
                        					", reason: " + rE.getMessage());
                                                if (m_xmlrpc)
                                                {
                                                        int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                                        XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                                        rE.getMessage(), status, "OpenNMS.Poller");
                                                }
                        		}
                        		catch(Throwable t)
                        		{
                        			log.error("Uncaught exception, failed to schedule interface " + ipaddr + 
                        					" for service monitor " + serviceName, t);
                                                if (m_xmlrpc)
                                                {
                                                        int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                                        XmlrpcUtil.createAndSendXmlrpcNotificationEvent(
                                                                txNo, 
                                                                sourceUei, 
                                                                "Failed to schedule interface " + 
                                                                "because of uncaught exception.", 
                                                                status,
                                                                "OpenNMS.Poller");
                                                }
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
                        					log.error("Failed to release node lock on nodeid " + 
                        							pNode.getNodeId() + ", thread interrupted.");
                                                                if (m_xmlrpc)
                                                                {
                                                                        int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                                                        XmlrpcUtil.createAndSendXmlrpcNotificationEvent(
                                                                                txNo, 
                                                                                sourceUei, 
                                                                                iE.getMessage(), 
                                                                                status,
                                                                                "OpenNMS.Poller");
                                                                }
                        				}
                        			}
                                        } // end inner try
			
			        } //end package while loop
                        } //end services while loop
                }//end try
		catch(SQLException sqlE)
		{
			log.error("SQLException during check to see if nodeid/ip/service is active", sqlE);
                        if (m_xmlrpc)
                        {
                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, sqlE.getMessage(), 
                                        status, "OpenNMS.Poller");
                        }
		}
		finally
		{
                        // close the statement
			if (stmt != null)
				try { stmt.close(); } catch(SQLException sqlE) { };

			// close the connection
			if (dbConn != null)
				try { dbConn.close(); } catch(SQLException sqlE) { };					
		}
	}
       
        /**
	 * This method is responsible for processing 'interfacReparented' events.  
	 * An 'interfaceReparented' event will have old and new nodeId parms
	 * associated with it. Node outage processing hierarchy will be updated to 
	 * reflect the new associations.
	 *
	 * @param event	The event to process.
	 *
	 */
	private void interfaceReparentedHandler(Event event)
	{
		Category log = ThreadCategory.getInstance(getClass());
		if (log.isDebugEnabled())
			log.debug("interfaceReparentedHandler:  processing interfaceReparented event for " + event.getInterface());
		
		// Verify that the event has an interface associated with it
		if (event.getInterface() == null)
			return;
			
		// Extract the old and new nodeId's from the event parms
		String oldNodeIdStr = null;
		String newNodeIdStr = null;
		Parms parms = event.getParms();
		if (parms != null)
		{
			String parmName = null;
			Value parmValue = null;
			String parmContent = null;
		
			Enumeration parmEnum = parms.enumerateParm();
			while(parmEnum.hasMoreElements())
			{
				Parm parm = (Parm)parmEnum.nextElement();
				parmName  = parm.getParmName();
				parmValue = parm.getValue();
				if (parmValue == null)
					continue;
				else 
					parmContent = parmValue.getContent();
	
				// old nodeid 
				if (parmName.equals(EventConstants.PARM_OLD_NODEID))
				{
					oldNodeIdStr = parmContent;
				}
						
				// new nodeid 
				else if (parmName.equals(EventConstants.PARM_NEW_NODEID))
				{
					newNodeIdStr = parmContent;
				}
			}
		}

		// Only proceed provided we have both an old and a new nodeId
		//
		if (oldNodeIdStr == null || newNodeIdStr == null)
		{
			log.error("interfaceReparentedHandler: old and new nodeId parms are required, unable to process.");
			return;
		}
		
		// Update node outage processing hierarchy based on this reparenting 
		// event.  Must "move" the interface from the "old" PollableNode object
		// to the "new" PollableNode object as identified by the old and new
		// nodeid parms.  
		// 	
		// In order to perform this "move" a node lock must be obtained on both
		// PollableNode objects.
		//
		
		// Retrieve old and new PollableNode objects from the Poller's pollable
		// node map.
		PollableNode oldPNode = null;
		PollableNode newPNode = null;
		try
		{
			oldPNode = Poller.getInstance().getNode(Integer.parseInt(oldNodeIdStr));
			newPNode = Poller.getInstance().getNode(Integer.parseInt(newNodeIdStr));
		}
		catch (NumberFormatException nfe)
		{
			log.error("interfaceReparentedHandler: failed converting old/new nodeid parm to integer, unable to process.");
			return;
		}
		
		// Sanity check, make certain we've were able to obtain both 
		// PollableNode objects.
		//
		if (oldPNode == null || newPNode == null)
		{
			log.error("interfaceReparentedHandler: old or new nodeId doesn't exist, unable to process.");
			return;
		}
		
		// Obtain node lock on both pollable node objects and then move the 
		// interface from the old node to the new node.
		//
                boolean ownOldLock = false;
                boolean ownNewLock = false;
		
                try
                {
			// Obtain lock on old nodeId...wait indefinitely
			log.debug("interfaceReparentedHandler: requesting node lock for old nodeId " + oldPNode.getNodeId());
			ownOldLock = oldPNode.getNodeLock(WAIT_FOREVER);
			PollableInterface pIf = oldPNode.getInterface(event.getInterface());
			log.debug("interfaceReparentedHandler: old node lock obtained, removing interface...");
			oldPNode.removeInterface(pIf);
			log.debug("interfaceReparentedHandler: recalculating old node status...");
			oldPNode.recalculateStatus();
			
			// Obtain lock on new nodeId...wait indefinitely
			log.debug("interfaceReparentedHandler: requesting node lock for new nodeId " + newPNode.getNodeId());
			ownNewLock = newPNode.getNodeLock(WAIT_FOREVER);
			log.debug("interfaceReparentedHandler: new node lock obtained, adding interface...");
			newPNode.addInterface(pIf);
			log.debug("interfaceReparentedHandler: recalculating new node status...");
			newPNode.recalculateStatus();
                }
                catch (InterruptedException iE)
                {
                        log.error("interfaceReparentedHandler: thread interrupted...failed to obtain required node locks");
                        return;
               	}
                finally
                {
			if (ownOldLock)
                        {
                                try
                                {
                                	oldPNode.releaseNodeLock();
                                }
                                catch (InterruptedException iE)
                                {
                                	log.error("interfaceReparentedHandler: thread interrupted...failed to release old node lock on nodeid " + 
							oldPNode.getNodeId());
                                }
                       	}

			if (ownNewLock)
                        {
                                try
                                {
                                	newPNode.releaseNodeLock();
                                }
                                catch (InterruptedException iE)
                                {
                                	log.error("interfaceReparentedHandler: thread interrupted...failed to release new node lock on nodeid " + 
							newPNode.getNodeId());
                                }
                       	}
		}	
	}

	/**
	 * This method is responsible for removing a node's
	 * pollable service from the pollable services list
	 */
	private void nodeRemovePollableServiceHandler(Event event)
	{
		Category log = ThreadCategory.getInstance(getClass());

		int nodeId = (int)event.getNodeid();
		String intfc = event.getInterface();
		String svc = event.getService();

		PollableNode pNode = Poller.getInstance().getNode(nodeId);
		if (pNode == null)  // Sanity check
		{
			log.error("Nodeid " + nodeId + " does not exist in pollable node map, unable to remove service from pollable services list.");
			return;
		}

		PollableInterface pInterface = pNode.getInterface(event.getInterface());
		if (pInterface == null)  // Sanity check
		{
			log.error("Interface " + intfc + "on node " + nodeId + " does not exist in pollable node map, unable to remove service from pollable services list.");
			return;
		}

		PollableService pService = pInterface.getService(event.getService());
		if (pService == null)  // Sanity check
		{
			log.error("Service " + svc + "on Interface " + intfc + "on node " + nodeId + " does not exist in pollable node map, unable to remove service from pollable services list.");
			return;
		}

		// acquire lock to 'PollableNode'
		//
		boolean ownLock = false;
		try
		{
			// Attempt to obtain node lock...wait as long as it takes.
			//
			if (log.isDebugEnabled())
				log.debug("nodeRemovePollableServiceHandler: Trying to get node lock for nodeId " + nodeId);

			ownLock = pNode.getNodeLock(WAIT_FOREVER);
			if (ownLock)
			{
				if (log.isDebugEnabled())
					log.debug("nodeRemovePollableServiceHandler: obtained node lock for nodeid: " + nodeId);

				// Mark the service as deleted
				pService.markAsDeleted();
				if (log.isDebugEnabled())
					log.debug("nodeRemovePollableServiceHandler: Marking service " + svc + " for deletion from active polling on node " + nodeId);
			}
			else
			{
				// failed to acquire lock
				log.error("nodeRemovePollableServiceHandler: failed to obtain lock on nodeId " + nodeId);
			}
		}
		catch (InterruptedException iE)
		{
			// failed to acquire lock
			log.error("nodeRemovePollableServiceHandler: thread interrupted...failed to obtain lock on nodeId " + nodeId);
		}
		catch (Throwable t)
		{
			log.error("exception caught processing suspendPollingService event for " + nodeId, t);
		}
		finally
		{
			if (ownLock)
			{
				if (log.isDebugEnabled())
					log.debug("nodeRemovePollableServiceHandler: releasing node lock for nodeid: " + nodeId);
				try
				{
					pNode.releaseNodeLock();
				}
				catch (InterruptedException iE)
				{
					log.error("nodeRemovePollableServiceHandler: thread interrupted...failed to release lock on nodeId " + nodeId);
				}
			}
		}
	}


	
	/** 
	 * This method is responsible for removing the node specified
	 * in the nodeDeleted event from the Poller's pollable node map.
	 */
	private void nodeDeletedHandler(Event event)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		int nodeId = (int)event.getNodeid();
	        String sourceUei = event.getUei();

                // Extract node label and transaction No. from the event parms
                long txNo = -1L;
                Parms parms = event.getParms();
                if (parms != null)
                {
                        String parmName = null;
                        Value parmValue = null;
                        String parmContent = null;
                        
                        Enumeration parmEnum = parms.enumerateParm();
                        while(parmEnum.hasMoreElements())
                        {
                                Parm parm = (Parm)parmEnum.nextElement();
                                parmName  = parm.getParmName();
                                parmValue = parm.getValue();
                                if (parmValue == null)
                                        continue;
                                else
                                        parmContent = parmValue.getContent();

                                //  get the external transaction number
                                if (parmName.equals(EventConstants.PARM_TRANSACTION_NO))
                                {
                                        String temp = parmContent;
                                        if (log.isDebugEnabled())
                                                log.debug("nodeDeletedHandler:  parmName: " + parmName
                                                        + " /parmContent: " + parmContent);
                                        try
                                        {
                                                txNo = Long.valueOf(temp).longValue();
                                        }
                                        catch (NumberFormatException nfe)
                                        {
                                                log.warn("nodeDeletedHandler: Parameter " 
                                                        + EventConstants.PARM_TRANSACTION_NO
                                                        + " cannot be non-numberic", nfe);
                                                txNo = -1;
                                        }
                                }
                        }
                }
		
                PollableNode pNode = Poller.getInstance().getNode(nodeId);
		if (pNode == null)  // Sanity check
		{
			log.error("Nodeid " + nodeId + " does not exist in pollable node map, unable to delete node.");
                        if (m_xmlrpc)
                        {
                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                        "Node does not exist in pollable node map.", status, "OpenNMS.Poller");
                        }
			return;
		}
		
                // acquire lock to 'PollableNode'
		//
		boolean ownLock = false;
		try
		{
			// Attempt to obtain node lock...wait as long as it takes.
			// 
			if (log.isDebugEnabled())
				log.debug("nodeDeletedHandler: deleting nodeId: " + nodeId);
	
			ownLock = pNode.getNodeLock(WAIT_FOREVER);
			if (ownLock)
			{
				if (log.isDebugEnabled())
					log.debug("nodeDeletedHandler: obtained node lock for nodeid: " + nodeId);
			
				// Remove the node from the Poller's node map
				Poller.getInstance().removeNode(nodeId);
				
				// Iterate over the node's interfaces and delete
				// all services on each interface.
				Iterator iter = pNode.getInterfaces().iterator();
				while (iter.hasNext())
				{
					PollableInterface pIf = (PollableInterface)iter.next();
					
					// Iterate over the interface's services and mark
					// them for deletion.
					Iterator svc_iter = pIf.getServices().iterator();
					while (svc_iter.hasNext())
					{
						PollableService pSvc = (PollableService)svc_iter.next();
						pSvc.markAsDeleted();
						
						// Now remove the service from the pollable services list
						m_pollableServices.remove(pSvc);
					}
					
					// Delete all entries from the interface's internal service map
					pIf.deleteAllServices();
				}
			
				// Delete all entries from the node's internal interface map
				pNode.deleteAllInterfaces();
				
				// Mark the node as deleted to prevent any further node 
				// outage processing on this node
				pNode.markAsDeleted();
				
				if (log.isDebugEnabled())
					log.debug("nodeDeletedHandler: deletion of nodeid " + pNode.getNodeId() + " completed.");
			}
			else
			{
				// failed to acquire lock
				log.error("nodeDeletedHandler: failed to obtain lock on nodeId " + nodeId);
                                if (m_xmlrpc)
                                {
                                        int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                        XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                "Internal error.", status, "OpenNMS.Poller");
                                }
			}
		}
		catch (InterruptedException iE)
		{
			// failed to acquire lock
			log.error("nodeDeletedHandler: thread interrupted...failed to obtain lock on nodeId " + nodeId);
                        if (m_xmlrpc)
                        {
                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, iE.getMessage(), 
                                        status, "OpenNMS.Poller");
                        }
		}
		catch (Throwable t)
		{
			log.error("exception caught processing nodeDeleted event for " + nodeId, t);
                        if (m_xmlrpc)
                        {
                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                        "Caught unknown exception.", status, "OpenNMS.Poller");
                        }
		}
		finally
		{
			if (ownLock)
			{
				if (log.isDebugEnabled())
					log.debug("nodeDeletedHandler: releasing node lock for nodeid: " + nodeId);
				try
				{
					pNode.releaseNodeLock();
				}
				catch (InterruptedException iE)
				{
					log.error("nodeDeletedHandler: thread interrupted...failed to release lock on nodeId " + nodeId);
                                        if (m_xmlrpc)
                                        {
                                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                           iE.getMessage(), status, "OpenNMS.Poller");
                                        }
				}
			}
		}
	}
	
	/** 
	 * 
	 */
	private void interfaceDeletedHandler(Event event)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		int nodeId = (int)event.getNodeid();
	        String sourceUei = event.getUei();	
		
                // Extract node label and transaction No. from the event parms
                long txNo = -1L;
                Parms parms = event.getParms();
                if (parms != null)
                {
                        String parmName = null;
                        Value parmValue = null;
                        String parmContent = null;
                                                                                                                                                                     Enumeration parmEnum = parms.enumerateParm();
                        while(parmEnum.hasMoreElements())
                        {
                                Parm parm = (Parm)parmEnum.nextElement();
                                parmName  = parm.getParmName();
                                parmValue = parm.getValue();
                                if (parmValue == null)
                                        continue;
                                else
                                        parmContent = parmValue.getContent();

                                //  get the external transaction number
                                if (parmName.equals(EventConstants.PARM_TRANSACTION_NO))
                                {
                                        String temp = parmContent;
                                        if (log.isDebugEnabled())
                                                log.debug("interfaceDeletedHandlerHandler:  parmName: " + parmName
                                                        + " /parmContent: " + parmContent);
                                        try
                                        {
                                                txNo = Long.valueOf(temp).longValue();
                                        }
                                        catch (NumberFormatException nfe)
                                        {
                                                log.warn("interfaceDeletedHandlerHandler: Parameter " 
                                                        + EventConstants.PARM_TRANSACTION_NO
                                                        + " cannot be non-numberic", nfe);
                                                txNo = -1;
                                        }
                                }
                        }
                }
		
                PollableNode pNode = Poller.getInstance().getNode(nodeId);
		if (pNode == null)  // Sanity check
		{
			log.error("Nodeid " + nodeId + " does not exist in pollable node map, unable to delete interface " 
                                + event.getInterface());
                        
                        if (m_xmlrpc)
                        {
                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                           "Node does not exist.", status, "OpenNMS.Poller");
                        }
			return;
		}
		
                // acquire lock to 'PollableNode'
		//
		boolean ownLock = false;
		try
		{
			// Attempt to obtain node lock...wait as long as it takes.
			// 
			if (log.isDebugEnabled())
				log.debug("interfaceDeletedHandler: deleting nodeid/interface: " + nodeId + 
							"/" + event.getInterface());
	
			ownLock = pNode.getNodeLock(WAIT_FOREVER);
			if (ownLock)
			{
				if (log.isDebugEnabled())
					log.debug("interfaceDeletedHandler: obtained node lock for nodeid: " + nodeId);
				
				// Retrieve the PollableInterface object corresponding to 
				// the interface address specified in the event
				PollableInterface pIf = pNode.getInterface(event.getInterface());
				if (pIf == null)
				{
					if (log.isDebugEnabled())
						log.debug("interfaceDeletedHandler: interface " + event.getInterface() + 
								" not in interface map for " + nodeId);
                                        if (m_xmlrpc)
                                        {
                                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
						String message = "Interface " + event.getInterface() + 
								" not in interface map for " + nodeId;
                                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                        message, status, "OpenNMS.Poller"); 
                                        }
					return;
				}
				
				// Iterate over the interface's services and mark
				// them for deletion.
				//
				// NOTE:  This is probably overkill because by the time
				//        the Outage Mgr generates the interfaceDeleted
				// 	  event all of the interface's underlying 
				// 	  services have already been deleted...but just
				//	  to be safe...
				Iterator svc_iter = pIf.getServices().iterator();
				while (svc_iter.hasNext())
				{
					PollableService pSvc = (PollableService)svc_iter.next();
					pSvc.markAsDeleted();
					
					// Now remove the service from the pollable services list
					m_pollableServices.remove(pSvc);
				}
				
				// Delete all entries from the interface's internal service map
				pIf.deleteAllServices();
				
				// Delete the interface from the node
				pNode.removeInterface(pIf);
				
				// Recalculate node status
				pNode.recalculateStatus();
				
				// Debug dump pollable node content
				//
				if (log.isDebugEnabled())
				{
					log.debug("Interface deletion completed, dumping node info for nodeid " + pNode.getNodeId() 
                                                + ", status=" + Pollable.statusType[pNode.getStatus()] );
					Iterator k = pNode.getInterfaces().iterator();
					while(k.hasNext())
					{
						PollableInterface tmpIf = (PollableInterface)k.next();
						log.debug("		interface=" + tmpIf.getAddress().getHostAddress() 
                                                        + " status=" + Pollable.statusType[tmpIf.getStatus()]);
						
						Iterator s = tmpIf.getServices().iterator();
						while(s.hasNext())
						{
							PollableService tmpSvc = (PollableService)s.next();
							log.debug("			service=" + tmpSvc.getServiceName() 
                                                                + " status=" + Pollable.statusType[tmpSvc.getStatus()]);
						}
					}
				}
			}
			else
			{
				// failed to acquire lock
				log.error("interfaceDeletedHandler: failed to obtain lock on nodeId " + nodeId);
                                if (m_xmlrpc)
                                {
                                        int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
				        String message = new String("Internal error.");
                                        XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                message, status, "OpenNMS.Poller"); 
                                }
			}
		}
		catch (InterruptedException iE)
		{
			// failed to acquire lock,
			log.error("interfaceDeletedHandler: thread interrupted...failed to obtain lock on nodeId " + nodeId);
                        if (m_xmlrpc)
                        {
                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, iE.getMessage(), 
                                        status, "OpenNMS.Poller"); 
                        }
		}
		catch (Throwable t)
		{
			log.error("exception caught processing interfaceDeleted event for " + 
					nodeId + "/" + event.getInterface(), t);
                        if (m_xmlrpc)
                        {
                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                        "Caught unknown exception.", status, "OpenNMS.Poller");
                        }
		}
		finally
		{
			if (ownLock)
			{
				if (log.isDebugEnabled())
					log.debug("interfaceDeletedHandler: releasing node lock for nodeid: " + nodeId);
				
				try
				{
					pNode.releaseNodeLock();
				}
				catch (InterruptedException iE)
				{
					log.error("interfaceDeletedHandler: thread interrupted...failed to release lock on nodeId " + nodeId);
                                        if (m_xmlrpc)
                                        {
                                                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                                                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, sourceUei, 
                                                        iE.getMessage(), status, "OpenNMS.Poller"); 
                                        }
				}
			}
		}
	}
	
	/** 
	 * <p>This method remove a deleted service from the pollable service list
         * of the specified interface, so that it will not be scheduled by the 
         * poller.</p>
	 */
	private void serviceDeletedHandler(Event event)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		int nodeId = (int)event.getNodeid();
                String ipAddr = event.getInterface();
                String service = event.getService();
		
                PollableNode pNode = Poller.getInstance().getNode(nodeId);
		if (pNode == null)  // Sanity check
		{
			log.error("Nodeid " + nodeId + " does not exist in pollable node map, " +
                                "unable to delete service " + event.getService());
                        
			return;
		}
		
                // acquire lock to 'PollableNode'
		//
		boolean ownLock = false;
		try
		{
			// Attempt to obtain node lock...wait as long as it takes.
			// 
			if (log.isDebugEnabled())
				log.debug("serviceDeletedHandler: deleting nodeid/interface/service: " + nodeId + 
        				"/" + ipAddr + "/" + service);
	
			ownLock = pNode.getNodeLock(WAIT_FOREVER);
			if (ownLock)
			{
				if (log.isDebugEnabled())
					log.debug("serviceDeletedHandler: obtained node lock for nodeid: " + nodeId);
				
				// Retrieve the PollableInterface object corresponding to 
				// the interface address specified in the event
				PollableInterface pIf = pNode.getInterface(ipAddr);
				if (pIf == null)
				{
					if (log.isDebugEnabled())
						log.debug("serviceDeletedHandler: interface " + ipAddr + 
								" not in interface map for " + nodeId);
					return;
				}
				
				// Iterate over the interface's services and find the service
				// to delete and mark it for deletion.
				//
				Iterator svc_iter = pIf.getServices().iterator();
				while (svc_iter.hasNext())
				{
					PollableService pSvc = (PollableService)svc_iter.next();
                                        if (pSvc.getServiceName().equals(service))
                                        {
					        pSvc.markAsDeleted();
					
					        // Now remove the service from the pollable services list
					        m_pollableServices.remove(pSvc);

                                                // remove the service from the interface's internal service map
                                                pIf.removeService(pSvc);
                                                break;
                                        }
				}
				
				// Recalculate interface status
				pIf.recalculateStatus();
				
				// Debug dump pollable node content
				//
				if (log.isDebugEnabled())
				{
					log.debug("Service deletion completed, dumping node info for nodeid " + pNode.getNodeId() 
                                                + ", status=" + Pollable.statusType[pNode.getStatus()] );
					Iterator k = pNode.getInterfaces().iterator();
					while(k.hasNext())
					{
						PollableInterface tmpIf = (PollableInterface)k.next();
						log.debug("		interface=" + tmpIf.getAddress().getHostAddress() 
                                                        + " status=" + Pollable.statusType[tmpIf.getStatus()]);
						
						Iterator s = tmpIf.getServices().iterator();
						while(s.hasNext())
						{
							PollableService tmpSvc = (PollableService)s.next();
							log.debug("			service=" + tmpSvc.getServiceName() 
                                                                + " status=" + Pollable.statusType[tmpSvc.getStatus()]);
						}
					}
				}
			}
			else
			{
				// failed to acquire lock
				log.error("serviceDeletedHandler: failed to obtain lock on nodeId " + nodeId);
			}
		}
		catch (InterruptedException iE)
		{
			// failed to acquire lock,
			log.error("serviceDeletedHandler: thread interrupted...failed to obtain lock on nodeId " + nodeId);
		}
		catch (Throwable t)
		{
			log.error("exception caught processing interfaceDeleted event for " + 
					nodeId + "/" + ipAddr, t);
		}
		finally
		{
			if (ownLock)
			{
				if (log.isDebugEnabled())
					log.debug("serviceDeletedHandler: releasing node lock for nodeid: " + nodeId);
				
				try
				{
					pNode.releaseNodeLock();
				}
				catch (InterruptedException iE)
				{
					log.error("serviceDeletedHandler: thread interrupted...failed to release lock on nodeId " + nodeId);
				}
			}
		}
	}

        
	/**
	 * Constructor
	 *
	 * @param pollableServices List of all the PollableService objects 
	 * 			  scheduled for polling
	 */
	BroadcastEventProcessor(List pollableServices)
	{
		Category log = ThreadCategory.getInstance(getClass());
		
		// Set the configuration for this event 
		// receiver.
		//
		m_scheduler= Poller.getInstance().getScheduler();
		m_pollableServices    = pollableServices;

		// If need to notify external xmlrpc server
                m_xmlrpc = PollerConfigFactory.getInstance().getXmlrpc();

                // Create the message selector and subscribe to eventd
		createMessageSelectorAndSubscribe();
		if(log.isDebugEnabled())
			log.debug("Subscribed to eventd");

	}

	/**
	 * Unsubscribe from eventd
	*/
	public void close()
	{
		EventIpcManagerFactory.getInstance().getManager().removeEventListener(this);
	}

	/**
	 * This method is invoked by the EventIpcManager
	 * when a new event is available for processing.
	 * Each message is examined for its Universal Event Identifier
	 * and the appropriate action is taking based on each UEI.
	 *
	 * @param event	The event 
	 */
	public void onEvent(Event event)
	{
		if (event == null)
			return;

		Category log = ThreadCategory.getInstance(getClass());

		// print out the uei
		//
		if(log.isDebugEnabled())
		{
			log.debug("BroadcastEventProcessor: received event, uei = " + event.getUei());
		}

		// If the event doesn't have a nodeId it can't be processed.
		if(!event.hasNodeid())
		{
			log.info("BroadcastEventProcessor: no database node id found, discarding event");
		}
		else if(event.getUei().equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI))
		{
			// If there is no interface then it cannot be processed
			//
			if(event.getInterface() == null || event.getInterface().length() == 0)
			{
				log.info("BroadcastEventProcessor: no interface found, discarding event");
			}
			else
			{
				nodeGainedServiceHandler(event);
			}
		}
		else if(event.getUei().equals(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI))
		{
			// If there is no interface then it cannot be processed
			//
			if(event.getInterface() == null || event.getInterface().length() == 0)
			{
				log.info("BroadcastEventProcessor: no interface found, cannot resume polling service, discarding event");
			}
			else
			{
				nodeGainedServiceHandler(event);
			}
		}
		else if(event.getUei().equals(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI))
		{
			// If there is no interface then it cannot be processed
			//
			if(event.getInterface() == null || event.getInterface().length() == 0)
			{
				log.info("BroadcastEventProcessor: no interface found, cannot suspend polling service, discarding event");
			}
			else
			{
				nodeRemovePollableServiceHandler(event);
			}
		}
		else if(event.getUei().equals(EventConstants.RESTART_POLLING_INTERFACE_EVENT_UEI))
		{
			// If there is no interface then it cannot be processed
			//
			if(event.getInterface() == null || event.getInterface().length() == 0)
			{
				log.info("BroadcastEventProcessor: no interface found, discarding event");
			}
			else
			{
				restartPollingInterfaceHandler(event);
			}
                }
		else if(event.getUei().equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI))
		{
			// If there is no interface then it cannot be processed
			//
			if(event.getInterface() == null || event.getInterface().length() == 0)
			{
				log.info("BroadcastEventProcessor: no interface found, discarding event");
			}
			else
			{
				interfaceReparentedHandler(event);
			}
                }
		else if(event.getUei().equals(EventConstants.NODE_DELETED_EVENT_UEI) ||
				event.getUei().equals(EventConstants.DUP_NODE_DELETED_EVENT_UEI))
		{
			if(event.getNodeid() < 0 )
			{
				log.info("BroadcastEventProcessor: no node or interface found, discarding event");
			}
			// NEW NODE OUTAGE EVENTS
			nodeDeletedHandler(event);
		}
		else if(event.getUei().equals(EventConstants.INTERFACE_DELETED_EVENT_UEI))
		{
			// If there is no interface then it cannot be processed
			//
			if(event.getNodeid() < 0 || event.getInterface() == null || event.getInterface().length() == 0)
			{
				log.info("BroadcastEventProcessor: invalid nodeid or no interface found, discarding event");
			}
			else
			{
				interfaceDeletedHandler(event);
			}
		}
		else if(event.getUei().equals(EventConstants.SERVICE_DELETED_EVENT_UEI))
		{
			// If there is no interface then it cannot be processed
			//
			if((event.getNodeid() < 0) || (event.getInterface() == null) || 
                                (event.getInterface().length() == 0) || (event.getService() == null))
			{
				log.info("BroadcastEventProcessor: invalid nodeid or no nodeinterface " +
                                        "or service found, discarding event");
			}
			else
			{
				serviceDeletedHandler(event);
			}
			
		} //end single event proces

	} // end onEvent()

	/**
	 * Return an id for this event listener
	 */
	public String getName()
	{
		return "Poller:BroadcastEventProcessor";
	}
} // end class
