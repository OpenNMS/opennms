/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
final class BroadcastEventProcessor implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);
    /**
     * List of ThresholdableService objects.
     */
    private final List<ThresholdableService> m_thresholdableServices;

    private final Threshd m_threshd;

    /**
     * This constructor is called to initialize the JMS event receiver. A
     * connection to the message server is opened and this instance is setup as
     * the endpoint for broadcast events. When a new event arrives it is
     * processed and the appropriate action is taken.
     * 
     * @param thresholdableServices
     *            List of all the ThresholdableService objects scheduled for
     *            thresholding.
     * 
     */
    BroadcastEventProcessor(Threshd threshd, List<ThresholdableService> thresholdableServices) {

        // Set the configuration for this event
        // receiver.
        //
        m_threshd = threshd;
        m_thresholdableServices = thresholdableServices;

        // Create the message selector
        installMessageSelector();
    }

    /**
     * Create message selector to set to the subscription
     */
    private void installMessageSelector() {
        // Create the JMS selector for the UEIs this service is interested in
        //
        List<String> ueiList = new ArrayList<String>();

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

	// scheduled outage configuration change
	ueiList.add(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI);
        
        //thresholds configuration change
        ueiList.add(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI);

        EventIpcManagerFactory.getIpcManager().addEventListener(this, ueiList);
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
        EventIpcManagerFactory.getIpcManager().removeEventListener(this);
    }

    /**
     * This method may be invoked by the garbage thresholding. Once invoked it
     * ensures that the <code>close</code> method is called <em>at least</em>
     * once during the cycle of this object.
     *
     * @throws java.lang.Throwable if any.
     */
    @Override
    protected void finalize() throws Throwable {
        close(); // ensure it's closed
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "Threshd:BroadcastEventProcessor";
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the JMS topic session when a new event is
     * available for processing. Currently only text based messages are
     * processed by this callback. Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each UEI.
     */
    @Override
    public void onEvent(Event event) {

        // print out the uei
        //
        LOG.debug("received event, uei = {}", event.getUei());
	if(event.getUei().equals(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI)) {
		m_threshd.refreshServicePackages();
        } else if (event.getUei().equals(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI)) {
            thresholdConfigurationChangedHandler(event);
	} else if(!event.hasNodeid()) {
	    // For all other events, if the event doesn't have a nodeId it can't be processed.
            LOG.info("no database node id found, discarding event");
        } else if (event.getUei().equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null) {
                LOG.info("no interface found, discarding event");
            } else {
                nodeGainedServiceHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null) {
                LOG.info("no interface found, discarding event");
            } else {
                primarySnmpInterfaceChangedHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null) {
                LOG.info("no interface found, discarding event");
            } else {
                reinitializePrimarySnmpInterfaceHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null) {
                LOG.info("no interface found, discarding event");
            } else {
                interfaceReparentedHandler(event);
            }
        }
        // NEW NODE OUTAGE EVENTS
        else if (event.getUei().equals(EventConstants.NODE_DELETED_EVENT_UEI) || event.getUei().equals(EventConstants.DUP_NODE_DELETED_EVENT_UEI)) {
            nodeDeletedHandler(event);
        } else if (event.getUei().equals(EventConstants.INTERFACE_DELETED_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null) {
                LOG.info("no interface found, discarding event");
            } else {
                interfaceDeletedHandler(event);
            }
        } else if (event.getUei().equals(EventConstants.SERVICE_DELETED_EVENT_UEI)) {
            // If there is no interface then it cannot be processed
            //
            if (event.getInterface() == null) {
                LOG.info("no interface found, discarding event");
            } else if (event.getService() == null || event.getService().length() == 0) {
                // If there is no service then it cannot be processed
                //
                LOG.info("no service found, discarding event");
            } else {
                serviceDeletedHandler(event);
            }
        }

    } // end onEvent()

    /**
     * Process the event.
     * 
     * This event is generated when a managed node which supports SNMP gains a
     * new interface. In this situation the ThresholdableService object
     * representing the primary SNMP interface of the node must be
     * reinitialized.
     * 
     * The ThresholdableService object associated with the primary SNMP
     * interface for the node will be marked for reinitialization.
     * Reinitializing the ThresholdableService object consists of calling the
     * ServiceThresholder.release() method followed by the
     * ServiceThresholder.initialize() method which will refresh attributes such
     * as the interface key list and number of interfaces (both of which most
     * likely have changed).
     * 
     * Reinitialization will take place the next time the ThresholdableService
     * is popped from an interval queue for thresholding.
     * 
     * If any errors occur scheduling the service no error is returned.
     * 
     * @param event
     *            The event to process.
     */
    private void reinitializePrimarySnmpInterfaceHandler(Event event) {

        if (event.getInterface() == null) {
            LOG.error("reinitializePrimarySnmpInterface event is missing an interface.");
            return;
        }
        // Mark the primary SNMP interface for reinitialization in
        // order to update any modified attributes associated with
        // the collectable service..
        //
        // Iterate over the ThresholdableService objects in the
        // updates map and mark any which have the same interface
        // address for reinitialization
        //
        synchronized (m_thresholdableServices) {
            for (ThresholdableService tSvc : m_thresholdableServices) {
                InetAddress addr = tSvc.getAddress();
                if (addr.equals(event.getInterfaceAddress())) {
                    synchronized (tSvc) {
                        // Got a match! Retrieve the ThresholderUpdates object
                        // associated
                        // with this ThresholdableService.
                        ThresholderUpdates updates = tSvc.getThresholderUpdates();
                        // Now set the reinitialization flag
                        updates.markForReinitialization();

                        LOG.debug("markServicesForReinit: marking {} for reinitialization for service SNMP.", event.getInterface());
                    }
                }
            }
        }
 
    }

    /**
     * Process the event.
     * 
     * This event is generated when the threshold configuration files are modified.
     * In this situation the ThresholdableService object
     * representing the primary SNMP interface of the node must be
     * reinitialized.
     * 
     * The ThresholdableService object associated with the primary SNMP
     * interface for the node will be marked for reinitialization.
     * Reinitializing the ThresholdableService object consists of calling the
     * ServiceThresholder.release() method followed by the
     * ServiceThresholder.initialize() method which will refresh various attributes
     * 
     * Reinitialization will take place the next time the ThresholdableService
     * is popped from an interval queue for thresholding.
     * 
     * If any errors occur scheduling the service no error is returned.
     * 
     * @param event
     *            The event to process.
     */
    private void thresholdConfigurationChangedHandler(Event event) {
        //Force a reload of the configuration, then tell the thresholders to reinitialize
        try {
            ThresholdingConfigFactory.reload();
        } catch (Throwable e) {
            LOG.error("thresholdConfigurationChangedHandler: Failed to reload threshold configuration because {}", e.getMessage(), e);
            return; //Do nothing else - the config is borked, so we carry on with what we've got which should still be relatively ok
        }
        //Tell the service thresholders to reinit 
        m_threshd.reinitializeThresholders();
        
       //Mark *all* thresholdable Services for reinit (very similar to reinitializePrimarySnmpInterfaceHandler but without the interface check)
        synchronized (m_thresholdableServices) {
            for (ThresholdableService tSvc : m_thresholdableServices) {
                InetAddress addr = (InetAddress) tSvc.getAddress();
                synchronized (tSvc) {
                    ThresholderUpdates updates = tSvc.getThresholderUpdates();
                    updates.markForReinitialization();

                    LOG.debug("thresholdConfigurationChangedHandler: marking {} for reinitialization for service SNMP.", InetAddressUtils.str(addr));
                }
            }
        }
    }

    /**
     * Process the event, construct a new ThresholdableService object
     * representing the node/interface combination, and schedule the interface
     * for thresholding.
     * 
     * If any errors occur scheduling the interface no error is returned.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void nodeGainedServiceHandler(Event event) {

        // Currently only support SNMP data thresholding.
        //
        if (!event.getService().equals("SNMP"))
            return;

        // Schedule the new service...
        //
        m_threshd.scheduleService(event.getNodeid().intValue(), event.getInterface(), event.getService(), false);
    }

    /**
     * Process the 'primarySnmpInterfaceChanged' event.
     * 
     * Extract the old and new primary SNMP interface addresses from the event
     * parms. Any ThresholdableService objects located in the collectable
     * services list which match the IP address of the old primary interface and
     * have a service name of "SNMP" are flagged for deletion. This will ensure
     * that the old primary interface is no longer collected against.
     * 
     * Finally the new primary SNMP interface is scheduled. The packages are
     * examined and new ThresholdableService objects are created, initialized
     * and scheduled for thresholding.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void primarySnmpInterfaceChangedHandler(Event event) {

        LOG.debug("primarySnmpInterfaceChangedHandler:  processing primary SNMP interface changed event...");

        // Extract the old and new primary SNMP interface addresses from the
        // event parms.
        //
        String oldPrimaryIfAddr = null;
        @SuppressWarnings("unused")
        String newPrimaryIfAddr = null;
        
        for (final Parm parm : event.getParmCollection()) {
            final String parmName = parm.getParmName();
            final Value parmValue = parm.getValue();
            final String parmContent;
            if (parmValue == null) {
                continue;
            } else {
                parmContent = parmValue.getContent();
            }

            // old primary SNMP interface (optional parameter)
            if (parmName.equals(EventConstants.PARM_OLD_PRIMARY_SNMP_ADDRESS)) {
                oldPrimaryIfAddr = parmContent;
            }

            // new primary SNMP interface (optional parameter)
            else if (parmName.equals(EventConstants.PARM_NEW_PRIMARY_SNMP_ADDRESS)) {
                newPrimaryIfAddr = parmContent;
            }
        }

        if (oldPrimaryIfAddr != null) {
            // Mark the service for deletion so that it will not be rescheduled
            // for
            // thresholding.
            //
            // Iterate over the ThresholdableService objects in the service
            // updates map
            // and mark any which have the same interface address as the old
            // primary SNMP interface and a service name of "SNMP" for deletion.
            //
            synchronized (m_thresholdableServices) {
                ThresholdableService tSvc = null;
                ListIterator<ThresholdableService> liter = m_thresholdableServices.listIterator();
                while (liter.hasNext()) {
                    tSvc = liter.next();

                    InetAddress addr = (InetAddress) tSvc.getAddress();
                    oldPrimaryIfAddr = InetAddressUtils.normalize(oldPrimaryIfAddr);
                    if (InetAddressUtils.str(addr).equals(oldPrimaryIfAddr)) {
                        synchronized (tSvc) {
                            // Got a match! Retrieve the ThresholderUpdates
                            // object associated
                            // with this ThresholdableService.
                            ThresholderUpdates updates = tSvc.getThresholderUpdates();

                            // Now set the deleted flag
                            updates.markForDeletion();

                            LOG.debug("primarySnmpInterfaceChangedHandler: marking {} as deleted for service SNMP.", oldPrimaryIfAddr);
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
        m_threshd.scheduleService(event.getNodeid().intValue(), event.getInterface(), event.getService(), false);


        LOG.debug("primarySnmpInterfaceChangedHandler: processing of primarySnmpInterfaceChanged event for nodeid {} completed.", event.getNodeid());
    }

    /**
     * This method is responsible for processing 'interfacReparented' events. An
     * 'interfaceReparented' event will have old and new nodeId parms associated
     * with it. All ThresholdableService objects in the service updates map
     * which match the event's interface address and the SNMP service have a
     * reparenting update associated with them. When the scheduler next pops one
     * of these services from an interval queue for thresholding all of the RRDs
     * associated with the old nodeId are moved under the new nodeId and the
     * nodeId of the collectable service is updated to reflect the interface's
     * new parent nodeId.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void interfaceReparentedHandler(Event event) {

        LOG.debug("interfaceReparentedHandler:  processing interfaceReparented event for {}", event.getInterface());

        // Verify that the event has an interface associated with it
        if (event.getInterface() == null)
            return;

        // Extract the old and new nodeId's from the event parms
        String oldNodeIdStr = null;
        String newNodeIdStr = null;
        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        for (Parm parm : event.getParmCollection()) {
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

        // Only proceed provided we have both an old and a new nodeId
        //
        if (oldNodeIdStr == null || newNodeIdStr == null) {
            LOG.warn("interfaceReparentedHandler: old and new nodeId parms are required, unable to process.");
            return;
        }

        // Iterate over the ThresholdableService objects in the services
        // list looking for entries which share the same interface
        // address as the reparented interface. Mark any matching objects
        // for reparenting.
        //
        // The next time the service is scheduled for execution it
        // will move all of the RRDs associated
        // with the old nodeId under the new nodeId and update the service's
        // SnmpMonitor.NodeInfo attribute to reflect the new nodeId. All
        // subsequent thresholdings will then be updating the appropriate RRDs.
        //
        
        //unused - commented out
        //boolean isPrimarySnmpInterface = false;
        synchronized (m_thresholdableServices) {
            ThresholdableService tSvc = null;
            Iterator<ThresholdableService> iter = m_thresholdableServices.iterator();
            while (iter.hasNext()) {
                tSvc = iter.next();

                InetAddress addr = (InetAddress) tSvc.getAddress();
                if (addr.equals(event.getInterfaceAddress())) {
                    synchronized (tSvc) {
                        // Got a match!

                        LOG.debug("interfaceReparentedHandler: got a ThresholdableService match for {}", event.getInterface());

                        // Retrieve the ThresholderUpdates object associated
                        // with this ThresholdableService.
                        ThresholderUpdates updates = tSvc.getThresholderUpdates();

                        // Now set the reparenting flag
                        updates.markForReparenting(oldNodeIdStr, newNodeIdStr);

                        LOG.debug("interfaceReparentedHandler: marking {} for reparenting for service SNMP.", event.getInterface());
                    }
                }
            }
        }


        LOG.debug("interfaceReparentedHandler: processing of interfaceReparented event for interface {} completed.", event.getInterface());
    }

    /**
     * This method is responsible for handling nodeDeleted events.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void nodeDeletedHandler(Event event) {

        long nodeId = event.getNodeid();

        // Iterate over the collectable service list and mark any entries
        // which match the deleted nodeId for deletion.
        synchronized (m_thresholdableServices) {
            ThresholdableService tSvc = null;
            ListIterator<ThresholdableService> liter = m_thresholdableServices.listIterator();
            while (liter.hasNext()) {
                tSvc = liter.next();

                // Only interested in entries with matching nodeId
                if (!(tSvc.getNodeId() == nodeId))
                    continue;

                synchronized (tSvc) {
                    // Retrieve the ThresholderUpdates object associated
                    // with this ThresholdableService.
                    ThresholderUpdates updates = tSvc.getThresholderUpdates();

                    // Now set the update's deletion flag so the next
                    // time it is selected for execution by the scheduler
                    // the thresholding will be skipped and the service will not
                    // be rescheduled.
                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }


        LOG.debug("nodeDeletedHandler: processing of nodeDeleted event for nodeid {} completed.", nodeId);
    }

    /**
     * This method is responsible for handling interfaceDeleted events.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void interfaceDeletedHandler(Event event) {

        long nodeId = event.getNodeid();
        InetAddress ipAddr = event.getInterfaceAddress();

        // Iterate over the collectable services list and mark any entries
        // which match the deleted nodeId/IP address pair for deletion
        synchronized (m_thresholdableServices) {
            ThresholdableService tSvc = null;
            ListIterator<ThresholdableService> liter = m_thresholdableServices.listIterator();
            while (liter.hasNext()) {
                tSvc = liter.next();

                // Only interested in entries with matching nodeId and IP
                // address
                InetAddress addr = (InetAddress) tSvc.getAddress();
                if (!(tSvc.getNodeId() == nodeId && addr.equals(ipAddr)))
                    continue;

                synchronized (tSvc) {
                    // Retrieve the ThresholderUpdates object associated with
                    // this ThresholdableService if one exists.
                    ThresholderUpdates updates = tSvc.getThresholderUpdates();

                    // Now set the update's deletion flag so the next
                    // time it is selected for execution by the scheduler
                    // the thresholding will be skipped and the service will not
                    // be rescheduled.
                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }


        LOG.debug("interfaceDeletedHandler: processing of interfaceDeleted event for {}/{} completed.", nodeId, ipAddr);
    }

    /**
     * This method is responsible for handling serviceDeleted events.
     * 
     * @param event
     *            The event to process.
     * 
     */
    private void serviceDeletedHandler(Event event) {

        // Currently only support SNMP data thresholding.
        //
        if (!event.getService().equals("SNMP"))
            return;

        long nodeId = event.getNodeid();
        InetAddress ipAddr = event.getInterfaceAddress();
        String svcName = event.getService();

        // Iterate over the collectable services list and mark any entries
        // which match the nodeId/ipAddr of the deleted service
        // for deletion.
        synchronized (m_thresholdableServices) {
            ThresholdableService tSvc = null;
            ListIterator<ThresholdableService> liter = m_thresholdableServices.listIterator();
            while (liter.hasNext()) {
                tSvc = liter.next();

                // Only interested in entries with matching nodeId, IP address
                // and service
                InetAddress addr = (InetAddress) tSvc.getAddress();
                if (!(tSvc.getNodeId() == nodeId && addr.equals(ipAddr)) && tSvc.getServiceName().equals(svcName))
                    continue;

                synchronized (tSvc) {
                    // Retrieve the ThresholderUpdates object associated with
                    // this ThresholdableService if one exists.
                    ThresholderUpdates updates = tSvc.getThresholderUpdates();

                    // Now set the update's deletion flag so the next
                    // time it is selected for execution by the scheduler
                    // the thresholding will be skipped and the service will not
                    // be rescheduled.
                    updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }


        LOG.debug("serviceDeletedHandler: processing of serviceDeleted event for {}/{}/{} completed.", nodeId, ipAddr, svcName);
    }
} // end class
