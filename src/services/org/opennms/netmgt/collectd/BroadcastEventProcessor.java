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

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class BroadcastEventProcessor implements EventListener {
    /**
     * The map of service names to service models.
     */
    private Map m_monitors;

    /**
     * The scheduler assocated with this receiver
     */
    private Scheduler m_scheduler;

    /**
     * List of CollectableService objects.
     */
    private List m_collectableServices;

    /**
     * This constructor is called to initilize the JMS event receiver. A
     * connection to the message server is opened and this instance is setup as
     * the endpoint for broadcast events. When a new event arrives it is
     * processed and the appropriate action is taken.
     * 
     * @param collectableServices
     *            List of all the CollectableService objects scheduled for
     *            collection.
     * 
     * @throws javax.naming.NamingException
     *             Thrown if the JNDI lookups fail.
     * @throws javax.jms.JMSException
     *             Thrown if an error occurs in the JMS subsystem.
     * 
     */
    BroadcastEventProcessor(List collectableServices) {
        // Set the configuration for this event
        // receiver.
        //
        m_scheduler = Collectd.getInstance().getScheduler();
        m_collectableServices = collectableServices;

        installMessageSelectors();
    }

    /**
     * Create message selector to set to the subscription
     */
    private void installMessageSelectors() {
        // Create the JMS selector for the ueis this service is interested in
        //
        List ueiList = new ArrayList();

        // nodeGainedService
        ueiList.add(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI);

        // interfaceIndexChanged
        // NOTE: No longer interested in this event...if Capsd detects
        // that in interface's index has changed a
        // 'reinitializePrimarySnmpInterface' event is generated.
        // ueiList.add(EventConstants.INTERFACE_INDEX_CHANGED_EVENT_UEI);

        // primarySnmpInterfaceChanged
        ueiList.add(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI);

        // reinitializePrimarySnmpInterface
        ueiList.add(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI);

        // interfaceReparented
        ueiList.add(EventConstants.INTERFACE_REPARENTED_EVENT_UEI);

        // nodeDeleted
        ueiList.add(EventConstants.NODE_DELETED_EVENT_UEI);

        // duplicateNodeDeleted
        ueiList.add(EventConstants.DUP_NODE_DELETED_EVENT_UEI);

        // interfaceDeleted
        ueiList.add(EventConstants.INTERFACE_DELETED_EVENT_UEI);

        // serviceDeleted
        ueiList.add(EventConstants.SERVICE_DELETED_EVENT_UEI);

	// outageConfigurationChanged
	ueiList.add(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI);

        EventIpcManagerFactory.getInstance().getManager().addEventListener(this, ueiList);
    }

    /**
     * </p>
     * Closes the current connections to the Java Message Queue if they are
     * still active. This call may be invoked more than once safely and may be
     * invoked during object finalization.
     * </p>
     * 
     */
    synchronized void close() {
        EventIpcManagerFactory.getInstance().getManager().removeEventListener(this);
    }

    /**
     * This method may be invoked by the garbage collection. Once invoked it
     * ensures that the <code>close</code> method is called <em>at least</em>
     * once during the cycle of this object.
     * 
     */
    protected void finalize() throws Throwable {
        close(); // ensure it's closed
    }

    public String getName() {
        return "Collectd:BroadcastEventProcessor";
    }

    /**
     * This method is invoked by the JMS topic session when a new event is
     * available for processing. Currently only text based messages are
     * processed by this callback. Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each UEI.
     * 
     * @param event
     *            The event message.
     * 
     */
    public void onEvent(Event event) {
        Category log = ThreadCategory.getInstance(getClass());

        // print out the uei
        //
        if (log.isDebugEnabled()) {
            log.debug("received event, uei = " + event.getUei());
        }

	if(event.getUei().equals(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI)) {
		log.warn("Reloading Collectd config factory");
		//Reload the collectd configuration
		try {
			CollectdConfigFactory.reload();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to reload CollectdConfigFactory because "+e.getMessage());
		}
		Collectd.getInstance().refreshServicePackages();
	}
	else if(!event.hasNodeid())
	{
		// For all other events, if the event doesn't have a nodeId it can't be processed.
		log.info("no database node id found, discarding event");
        } else if (event.getUei().equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null || event.getInterface().length() == 0) {
                log.info("no interface found, discarding event");
            } else {
                nodeGainedServiceHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null || event.getInterface().length() == 0) {
                log.info("no interface found, discarding event");
            } else {
                primarySnmpInterfaceChangedHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null || event.getInterface().length() == 0) {
                log.info("no interface found, discarding event");
            } else {
                reinitializePrimarySnmpInterfaceHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null || event.getInterface().length() == 0) {
                log.info("no interface found, discarding event");
            } else {
                interfaceReparentedHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.NODE_DELETED_EVENT_UEI) || event.getUei().equals(EventConstants.DUP_NODE_DELETED_EVENT_UEI)) {
            // NEW NODE OUTAGE EVENTS
            nodeDeletedHandler(event);
        } else if (event.getUei().equals(EventConstants.INTERFACE_DELETED_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null || event.getInterface().length() == 0) {
                log.info("no interface found, discarding event");
            } else {
                interfaceDeletedHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.SERVICE_DELETED_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null || event.getInterface().length() == 0) {
                log.info("no interface found, discarding event");
            } else if (event.getService() == null || event.getService().length() == 0) {
                // If there is no service then it cannot be processed
                //
                log.info("no service found, discarding event");
            } else {
                serviceDeletedHandler(event);
            }
        }

    } // end onEvent()

    /**
     * Process the event.
     * 
     * This event is generated when a managed node which supports SNMP gains a
     * new interface. In this situation the CollectableService object
     * representing the primary SNMP interface of the node must be
     * reinitialized.
     * 
     * The CollectableService object associated with the primary SNMP interface
     * for the node will be marked for reinitialization. Reinitializing the
     * CollectableService object consists of calling the
     * ServiceCollector.release() method followed by the
     * ServiceCollector.initialize() method which will refresh attributes such
     * as the interface key list and number of interfaces (both of which most
     * likely have changed).
     * 
     * Reinitialization will take place the next time the CollectableService is
     * popped from an interval queue for collection.
     * 
     * If any errors occur scheduling the service no error is returned.
     * 
     * @param event
     *            The event to process.
     */
    private void reinitializePrimarySnmpInterfaceHandler(Event event) {
        Category log = ThreadCategory.getInstance(getClass());

        if (event.getInterface() == null) {
            log.error("reinitializePrimarySnmpInterface event is missing an interface.");
            return;
        }

        // Mark the primary SNMP interface for reinitialization in
        // order to update any modified attributes associated with
        // the collectable service..
        //
        // Iterate over the CollectableService objects in the
        // updates map and mark any which have the same interface
        // address for reinitialization
        //
        synchronized (m_collectableServices) {
            Iterator iter = m_collectableServices.iterator();
            while (iter.hasNext()) {
                CollectableService cSvc = (CollectableService) iter.next();

                InetAddress addr = (InetAddress) cSvc.getAddress();
                if (log.isDebugEnabled())
                    log.debug("Comparing CollectableService ip address = " + addr.getHostAddress() + " and event ip interface = " + event.getInterface());
                if (addr.getHostAddress().equals(event.getInterface())) {
                    synchronized (cSvc) {
                        // Got a match! Retrieve the CollectorUpdates object
                        // associated
                        // with this CollectableService.
                        CollectorUpdates updates = cSvc.getCollectorUpdates();

                        // Now set the reinitialization flag
                        updates.markForReinitialization();
                        if (log.isDebugEnabled())
                            log.debug("reinitializePrimarySnmpInterfaceHandler: marking " + event.getInterface() + " for reinitialization for service SNMP.");
                    }
                }
            }
        }
    }

    /**
     * Process the event, construct a new CollectableService object representing
     * the node/interface combination, and schedule the interface for
     * collection.
     * 
     * If any errors occur scheduling the interface no error is returned.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void nodeGainedServiceHandler(Event event) {
        Category log = ThreadCategory.getInstance(getClass());
        // Currently only support SNMP data collection.
        //
        if (!event.getService().equals("SNMP")) {
            if (log.isDebugEnabled())
                log.debug("nodeGainedServiceHandler: Datacollection not scheduled for service "+event.getService() +", currently only supporting SNMP service for collection.");
            return;
        }

        // Schedule the interface
        //
        scheduleForCollection(event);
    }

    /**
     * Process the 'primarySnmpInterfaceChanged' event.
     * 
     * Extract the old and new primary SNMP interface addresses from the event
     * parms. Any CollectableService objects located in the collectable services
     * list which match the IP address of the old primary interface and have a
     * service name of "SNMP" are flagged for deletion. This will ensure that
     * the old primary interface is no longer collected against.
     * 
     * Finally the new primary SNMP interface is scheduled. The packages are
     * examined and new CollectableService objects are created, initialized and
     * scheduled for collection.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void primarySnmpInterfaceChangedHandler(Event event) {
        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("primarySnmpInterfaceChangedHandler:  processing primary SNMP interface changed event...");

        // Currently only support SNMP data collection.
        //
        if (!event.getService().equals("SNMP"))
            return;

        // Extract the old and new primary SNMP interface adddresses from the
        // event parms.
        //
        String oldPrimaryIfAddr = null;
        String newPrimaryIfAddr = null;
        Parms parms = event.getParms();
        if (parms != null) {
            String parmName = null;
            Value parmValue = null;
            String parmContent = null;

            Enumeration parmEnum = parms.enumerateParm();
            while (parmEnum.hasMoreElements()) {
                Parm parm = (Parm) parmEnum.nextElement();
                parmName = parm.getParmName();
                parmValue = parm.getValue();
                if (parmValue == null)
                    continue;
                else
                    parmContent = parmValue.getContent();

                // old primary SNMP interface (optional parameter)
                if (parmName.equals(EventConstants.PARM_OLD_PRIMARY_SNMP_ADDRESS)) {
                    oldPrimaryIfAddr = parmContent;
                }

                // old primary SNMP interface (optional parameter)
                else if (parmName.equals(EventConstants.PARM_NEW_PRIMARY_SNMP_ADDRESS)) {
                    newPrimaryIfAddr = parmContent;
                }
            }
        }

        if (oldPrimaryIfAddr != null) {
            // Mark the service for deletion so that it will not be rescheduled
            // for
            // collection.
            //
            // Iterate over the CollectableService objects in the service
            // updates map
            // and mark any which have the same interface address as the old
            // primary SNMP interface and a service name of "SNMP" for deletion.
            //
            synchronized (m_collectableServices) {
                CollectableService cSvc = null;
                ListIterator liter = m_collectableServices.listIterator();
                while (liter.hasNext()) {
                    cSvc = (CollectableService) liter.next();

                    InetAddress addr = (InetAddress) cSvc.getAddress();
                    if (addr.getHostAddress().equals(oldPrimaryIfAddr)) {
                        synchronized (cSvc) {
                            // Got a match! Retrieve the CollectorUpdates object
                            // associated
                            // with this CollectableService.
                            CollectorUpdates updates = cSvc.getCollectorUpdates();

                            // Now set the deleted flag
                            updates.markForDeletion();
                            if (log.isDebugEnabled())
                                log.debug("primarySnmpInterfaceChangedHandler: marking " + oldPrimaryIfAddr + " as deleted for service SNMP.");
                        }

                        // Now safe to remove the collectable service from
                        // the collectable services list
                        liter.remove();
                    }
                }
            }
        }

        // Now we can schedule the new service...
        //
        scheduleForCollection(event);

        if (log.isDebugEnabled())
            log.debug("primarySnmpInterfaceChangedHandler: processing of primarySnmpInterfaceChanged event for nodeid " + event.getNodeid() + " completed.");
    }

    /**
     * This method is responsible for processing 'interfacReparented' events. An
     * 'interfaceReparented' event will have old and new nodeId parms associated
     * with it. All CollectableService objects in the service updates map which
     * match the event's interface address and the SNMP service have a
     * reparenting update associated with them. When the scheduler next pops one
     * of these services from an interval queue for collection all of the RRDs
     * associated with the old nodeId are moved under the new nodeId and the
     * nodeId of the collectable service is updated to reflect the interface's
     * new parent nodeId.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void interfaceReparentedHandler(Event event) {
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
        if (parms != null) {
            String parmName = null;
            Value parmValue = null;
            String parmContent = null;

            Enumeration parmEnum = parms.enumerateParm();
            while (parmEnum.hasMoreElements()) {
                Parm parm = (Parm) parmEnum.nextElement();
                parmName = parm.getParmName();
                parmValue = parm.getValue();
                if (parmValue == null)
                    continue;
                else
                    parmContent = parmValue.getContent();

                // old nodeid
                if (parmName.equals(EventConstants.PARM_OLD_NODEID)) {
                    oldNodeIdStr = parmContent;
                }

                // new nodeid
                else if (parmName.equals(EventConstants.PARM_NEW_NODEID)) {
                    newNodeIdStr = parmContent;
                }
            }
        }

        // Only proceed provided we have both an old and a new nodeId
        //
        if (oldNodeIdStr == null || newNodeIdStr == null) {
            log.warn("interfaceReparentedHandler: old and new nodeId parms are required, unable to process.");
            return;
        }

        // Iterate over the CollectableService objects in the services
        // list looking for entries which share the same interface
        // address as the reparented interface. Mark any matching objects
        // for reparenting.
        //
        // The next time the service is scheduled for execution it
        // will move all of the RRDs associated
        // with the old nodeId under the new nodeId and update the service's
        // SnmpMonitor.NodeInfo attribute to reflect the new nodeId. All
        // subsequent collections will then be updating the appropriate RRDs.
        //
        synchronized (m_collectableServices) {
            CollectableService cSvc = null;
            Iterator iter = m_collectableServices.iterator();
            while (iter.hasNext()) {
                cSvc = (CollectableService) iter.next();

                InetAddress addr = (InetAddress) cSvc.getAddress();
                if (addr.getHostAddress().equals(event.getInterface())) {
                    synchronized (cSvc) {
                        // Got a match!
                        if (log.isDebugEnabled())
                            log.debug("interfaceReparentedHandler: got a CollectableService match for " + event.getInterface());

                        // Retrieve the CollectorUpdates object associated with
                        // this CollectableService.
                        CollectorUpdates updates = cSvc.getCollectorUpdates();

                        // Now set the reparenting flag
                        updates.markForReparenting(oldNodeIdStr, newNodeIdStr);
                        if (log.isDebugEnabled())
                            log.debug("interfaceReparentedHandler: marking " + event.getInterface() + " for reparenting for service SNMP.");
                    }
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("interfaceReparentedHandler: processing of interfaceReparented event for interface " + event.getInterface() + " completed.");
    }

    /**
     * This method is responsible for handling nodeDeleted events.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void nodeDeletedHandler(Event event) {
        Category log = ThreadCategory.getInstance(getClass());

        int nodeId = (int) event.getNodeid();

        // Iterate over the collectable service list and mark any entries
        // which match the deleted nodeId for deletion.
        synchronized (m_collectableServices) {
            CollectableService cSvc = null;
            ListIterator liter = m_collectableServices.listIterator();
            while (liter.hasNext()) {
                cSvc = (CollectableService) liter.next();

                // Only interested in entries with matching nodeId
                if (!(cSvc.getNodeId() == nodeId))
                    continue;

                synchronized (cSvc) {
                    // Retrieve the CollectorUpdates object associated
                    // with this CollectableService.
                    CollectorUpdates updates = cSvc.getCollectorUpdates();

                    // Now set the update's deletion flag so the next
                    // time it is selected for execution by the scheduler
                    // the collection will be skipped and the service will not
                    // be rescheduled.
                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }

        if (log.isDebugEnabled())
            log.debug("nodeDeletedHandler: processing of nodeDeleted event for nodeid " + nodeId + " completed.");
    }

    /**
     * This method is responsible for handling interfaceDeleted events.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void interfaceDeletedHandler(Event event) {
        Category log = ThreadCategory.getInstance(getClass());

        int nodeId = (int) event.getNodeid();
        String ipAddr = event.getInterface();

        // Iterate over the collectable services list and mark any entries
        // which match the deleted nodeId/IP address pair for deletion
        synchronized (m_collectableServices) {
            CollectableService cSvc = null;
            ListIterator liter = m_collectableServices.listIterator();
            while (liter.hasNext()) {
                cSvc = (CollectableService) liter.next();

                // Only interested in entries with matching nodeId and IP
                // address
                InetAddress addr = (InetAddress) cSvc.getAddress();
                if (!(cSvc.getNodeId() == nodeId && addr.getHostName().equals(ipAddr)))
                    continue;

                synchronized (cSvc) {
                    // Retrieve the CollectorUpdates object associated with
                    // this CollectableService if one exists.
                    CollectorUpdates updates = cSvc.getCollectorUpdates();

                    // Now set the update's deletion flag so the next
                    // time it is selected for execution by the scheduler
                    // the collection will be skipped and the service will not
                    // be rescheduled.
                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }

        if (log.isDebugEnabled())
            log.debug("interfaceDeletedHandler: processing of interfaceDeleted event for " + nodeId + "/" + ipAddr + " completed.");
    }

    /**
     * This method is responsible for handling serviceDeleted events.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void serviceDeletedHandler(Event event) {
        Category log = ThreadCategory.getInstance(getClass());

        // Currently only support SNMP data collection.
        //
        if (!event.getService().equals("SNMP"))
            return;

        int nodeId = (int) event.getNodeid();
        String ipAddr = event.getInterface();
        String svcName = event.getService();

        // Iterate over the collectable services list and mark any entries
        // which match the nodeId/ipAddr of the deleted service
        // for deletion.
        synchronized (m_collectableServices) {
            CollectableService cSvc = null;
            ListIterator liter = m_collectableServices.listIterator();
            while (liter.hasNext()) {
                cSvc = (CollectableService) liter.next();

                // Only interested in entries with matching nodeId, IP address
                // and service
                InetAddress addr = (InetAddress) cSvc.getAddress();
                if (!(cSvc.getNodeId() == nodeId && addr.getHostName().equals(ipAddr)) && cSvc.getServiceName().equals(svcName))
                    continue;

                synchronized (cSvc) {
                    // Retrieve the CollectorUpdates object associated with
                    // this CollectableService if one exists.
                    CollectorUpdates updates = cSvc.getCollectorUpdates();

                    // Now set the update's deletion flag so the next
                    // time it is selected for execution by the scheduler
                    // the collection will be skipped and the service will not
                    // be rescheduled.
                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }

        if (log.isDebugEnabled())
            log.debug("serviceDeletedHandler: processing of serviceDeleted event for " + nodeId + "/" + ipAddr + "/" + svcName + " completed.");
    }

    private void scheduleForCollection(Event event) {
        //This moved to here from the scheduleInterface() for better behavior during initialization
        CollectdConfigFactory cCfgFactory = CollectdConfigFactory.getInstance();
        cCfgFactory.rebuildPackageIpListMap();

        Collectd.getInstance().scheduleInterface((int) event.getNodeid(), event.getInterface(), event.getService(), false);
    }

} // end class
