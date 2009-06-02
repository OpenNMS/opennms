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
// Tab Size = 8
//

package org.opennms.netmgt.vulnscand;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;

/**
 * 
 * @author <a href="mailto:seth@opennms.org">Seth Leger</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
final class BroadcastEventProcessor implements EventListener {
    /**
     * SQL query to retrieve node ID of a particular interface address
     */
    @SuppressWarnings("unused")
    private static String SQL_RETRIEVE_NODEID = "select nodeid from ipinterface where ipaddr=? and isManaged!='D'";

    /**
     * The location where suspectInterface events are enqueued for processing.
     */
    @SuppressWarnings("unused")
    private final FifoQueue<Runnable> m_suspectQ;

    /**
     * The Vulnscand rescan scheduler
     */
    @SuppressWarnings("unused")
    private final Object m_scheduler;

    /**
     * Create message selector to set to the subscription
     */
    private void installMessageSelector() {
        // Create the message selector for the UEIs this service is interested in
        List<String> ueiList = new ArrayList<String>();

        // specificVulnerabilityScan
        ueiList.add(EventConstants.SPECIFIC_VULN_SCAN_EVENT_UEI);

        EventIpcManagerFactory.getIpcManager().addEventListener(this, ueiList);
    }

    /**
     * This constructor is called to initialize the event receiver. A
     * connection to the message server is opened and this instance is setup as
     * the endpoint for broadcast events. When a new event arrives it is
     * processed and the appropriate action is taken.
     * 
     * @param suspectQ
     *            The queue where new Runnable objects are enqueued for
     *            running..
     * @param scheduler
     *            Rescan scheduler.
     * 
     */
    BroadcastEventProcessor(FifoQueue<Runnable> suspectQ, Object scheduler) {
        // Suspect queue
        m_suspectQ = suspectQ;

        // Scheduler
        m_scheduler = scheduler;

        installMessageSelector();
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
     * This method may be invoked by the garbage collection. Once invoked it
     * ensures that the <code>close</code> method is called <em>at least</em>
     * once during the cycle of this object.
     * 
     */
    protected void finalize() throws Throwable {
        close(); // ensure it's closed
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

        String eventUei = event.getUei();
        if (eventUei == null)
            return;

        if (log.isDebugEnabled())
            log.debug("Received event: " + eventUei);

        if (eventUei.equals(EventConstants.SPECIFIC_VULN_SCAN_EVENT_UEI)) {

            // ADD RESCAN CAPABILITIES HERE
            // NEED TO GET THE SCAN LEVEL,
            // LAST SCAN DATE FROM THE DATABASE,
            // AND THE RESCAN INTERVAL

            /*
             * // new poll event try { if (log.isDebugEnabled())
             * log.debug("onMessage: Adding interface to suspectInterface Q: " +
             * event.getInterface()); m_suspectQ.add(new NessusScan(new
             * NessusScanConfiguration(InetAddress.getByName(event.getInterface()),
             * int newScanLevel, Date newLastScan, long newInterval))); } catch
             * (java.net.UnknownHostException ex) { log.error("onMessage: Could
             * not schedule invalid interface: \"" + event.getInterface() +
             * "\"", ex); } catch(Exception ex) { log.error("onMessage: Failed
             * to add interface \"" + event.getInterface() + "\" to suspect
             * queue", ex); }
             */
        } else {
            log.error("Cannot process event with UEI: " + event.getUei());
        }
    } // end onEvent()

    public String getName() {
        return "Vulnscand:BroadcastEventProcessor";
    }
} // end class
