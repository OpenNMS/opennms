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
//      http://www.opennms.com/
//

package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * <P>
 * The PollableService class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
final class PollableService extends PollableElement implements Pollable {
    /**
     * interface that this service belongs to
     */
    private PollableInterface m_pInterface;

    /**
     * Flag which indicates if previous poll returned
     * SnmpMonitor.SERVICE_UNRESPONSIVE.
     */
    private boolean m_unresponsive;

    /**
     * Indicates if the service changed status as the result of most recent
     * poll.
     * 
     * Set by poll() method.
     */
    private boolean m_statusChangedFlag;

    /**
     * When the last status change occured.
     * 
     * Set by the poll() method.
     */
    private long m_statusChangeTime;

    /**
     * Deletion flag...set to indicate that the service/interface/node tuple
     * represented by this PollableService object has been deleted and should no
     * longer be polled.
     */
    private boolean m_deletionFlag;

    /**
     * List of all scheduled PollableService objects
     */
    private final List m_pollableServices;

    private Schedule m_schedule;
    
    private IPv4NetworkInterface m_netInterface;

    /**
     * Constructs a new instance of a pollable service object that is polled
     * using the passed monitor. The service is scheduled based upon the values
     * in the packages.
     * 
     * @param pInterface
     *            The interface to poll
     * @param svcName
     *            The name of the service being polled.
     * @param pkg
     *            The package with the polling information
     * 
     */
    PollableService(PollableInterface pInterface, String svcName, Package pkg, int status, Date svcLostDate) {
        super(status);
        m_pInterface = pInterface;
        m_netInterface = new IPv4NetworkInterface(pInterface.getAddress());
        m_deletionFlag = false;

        m_pollableServices = getPoller().getPollableServiceList();

        ServiceConfig svcConfig = new ServiceConfig(getPoller(), pkg, svcName);
        m_schedule = new Schedule(this, svcConfig);
        m_schedule.setLastPoll(0L);

        // Set status change values.
        setStatusChangeTime(0L);
        resetStatusChanged();
        if (getStatus() == ServiceMonitor.SERVICE_UNAVAILABLE) {
            if (svcLostDate == null)
                throw new IllegalArgumentException("The svcLostDate parm cannot be null if status is UNAVAILABLE!");

            setStatusChangeTime(svcLostDate.getTime());
        }
        setUnresponsive(false);

    }

    /**
     * Returns the service name
     */
    public String getServiceName() {
        return m_schedule.getServiceName();
    }

    /**
     * Returns true if status of service changed as a result of the last poll.
     * 
     * WARNING: value of m_statusChangedFlag is only reliable immediately
     * following a call to poll()
     */
    public boolean statusChanged() {
        return m_statusChangedFlag;
    }

    public void resetStatusChanged() {
        m_statusChangedFlag = false;
    }
    
    public void setStatusChanged() {
        m_statusChangedFlag = true;
    }

    public void updateStatus(int status) {
        if (getStatus() != status) {
            setStatus(status);
            setStatusChangeTime(System.currentTimeMillis());
        }
    }

    public void markAsDeleted() {
        m_deletionFlag = true;
    }

    public boolean isDeleted() {
        return m_deletionFlag;
    }

    public String getPackageName() {
        return m_schedule.getPackageName();
    }

    void deleteService() {
        // Generate 'deleteService' event
        sendEvent(EventConstants.DELETE_SERVICE_EVENT_UEI, null);

        // Delete this pollable service from the service updates
        // map maintained by the Scheduler and mark any
        // equivalent pollable services (scheduled via other packages)
        // as deleted. The services marked as deleted will subsequently
        // be removed the next time the sheduler pops them from the
        // interval queues for polling.
        //
        this.cleanupScheduledServices();

        // remove this service from the interfaces' service list
        // so it is no longer polled via node outage processing
        //
        getInterface().removeService(this);
    }

    /**
     * 
     */
    void sendEvent(String uei, Map properties) {
        Category log = ThreadCategory.getInstance(getClass());
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid((long) getNodeId());
        event.setInterface(getIpAddr());
        event.setService(getServiceName());
        event.setSource("OpenNMS.Poller");
        try {
            event.setHost(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            event.setHost("unresolved.host");
        }

        event.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add parms
        //
        Parms parms = null;

        // Qualifier parm (if available)
        String qualifier = null;
        if (properties != null)
            try {
                qualifier = (String) properties.get("qualifier");
            } catch (ClassCastException ex) {
                qualifier = null;
            }
        if (qualifier != null && qualifier.length() > 0) {
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
        if (uei.equals(EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI)) {
            int timeout = ParameterMap.getKeyedInteger(properties, "timeout", -1);
            int retry = ParameterMap.getKeyedInteger(properties, "retry", -1);
            int attempts = retry + 1;

            // Timeout parm
            if (timeout != -1) {
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
            if (retry != -1) {
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
            if (attempts > 0) {
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
        try {
            getEventManager().sendNow(event);
            if (log.isDebugEnabled()) {
                log.debug("Sent event " + uei + " for " + this);
            }
        } catch (Throwable t) {
            log.error("Failed to send the event " + uei + " for interface " + getIpAddr(), t);
        }
    }

    public InetAddress getAddress() {
        return getInterface().getAddress();
    }

    /**
     * @return
     */
    private EventIpcManager getEventManager() {
        return getPoller().getEventManager();
    }

    /**
     * Tests if two PollableService objects refer to the same
     * nodeid/interface/service tuple.
     * 
     * @param aService
     *            the PollableService object to compare
     * 
     * @return TRUE if the two pollable service objects are equivalent, FALSE
     *         otherwise.
     */
    public boolean equals(Object aService) {
        boolean isEqual = false;

        if (aService instanceof PollableService) {
            PollableService temp = (PollableService) aService;

            if (this.getInterface().getNode().getNodeId() == temp.getInterface().getNode().getNodeId() && this.getAddress().equals(temp.getAddress()) && this.getServiceName().equals(temp.getServiceName())) {
                isEqual = true;
            }
        }

        return isEqual;
    }

    /**
     * This method is called to remove a pollable service from the service
     * updates map. It is necessary to not only remove the passed
     * PollableService object but also to mark any other pollable services which
     * share the same nodeid, interface address and service name for deletion.
     * The reason for this is that the interface/service pair may have applied
     * to multiple packages resulting in the same interface/service pair being
     * polled multiple times.
     */
    void cleanupScheduledServices() {
        Category log = ThreadCategory.getInstance(getClass());

        // Go ahead and remove 'this' service from the list.
        m_pollableServices.remove(this);
        if (log.isDebugEnabled())
            log.debug("cleanupScheduledServices: deleted " + this + ":" + getPackageName());

        // Next interate over the pollable service list and mark any pollable
        // service
        // objects which refer to the same node/interface/service pairing
        // for deletion.
        synchronized (m_pollableServices) {
            Iterator iter = m_pollableServices.iterator();
            if (log.isDebugEnabled())
                log.debug("cleanupScheduledServices: iterating over serviceUpdatesMap(numEntries=" + m_pollableServices.size() + ") looking for " + this);

            while (iter.hasNext()) {
                PollableService temp = (PollableService) iter.next();

                if (log.isDebugEnabled())
                    log.debug("cleanupScheduledServices: comparing " + this);

                // If the two objects are equal but not identical (in other
                // words they refer to two different objects with the same
                // nodeid, ipAddress, and service name) then need to set the
                // deletion flag so that the next time the interface
                // is pulled from the queue for execution it will be deleted.
                if (this.equals(temp)) {
                    // Now set the deleted flag
                    temp.markAsDeleted();
                    if (log.isDebugEnabled())
                        log.debug("cleanupScheduledServices: marking " + this + ":" + temp.getPackageName() + " as deleted.");
                }
            }
        }
    }

    public int getNodeId() {
        return getNode().getNodeId();
    }

    public PollableNode getNode() {
        return getInterface().getNode();
    }

    /**
     * @return
     */
    Poller getPoller() {
        return getInterface().getPoller();
    }

    /**
     * <P>
     * Invokes a poll of the service via the ServiceMonitor.
     * </P>
     */
    public int poll() {
        return m_schedule.poll();
    }

    public String getIpAddr() {
        return getAddress().getHostAddress();
    }

    /**
     * @param sm
     */
    public void releaseMonitor(ServiceMonitor sm) {
        sm.release(getNetInterface());
    }

    /**
     * @param monitor
     */
    public void initializeMonitor(ServiceMonitor monitor) {
        monitor.initialize(getNetInterface());
    }

    /**
     * @return
     */
    public Schedule getSchedule() {
        return m_schedule;
    }

    public void setStatusChangeTime(long statusChangeTime) {
        m_statusChangeTime = statusChangeTime;
    }

    public long getStatusChangeTime() {
        return m_statusChangeTime;
    }

    public String toString() {
        return getNodeId() + ":" + getIpAddr() + ":" + getServiceName();
        
    }

    ServiceConfig getSvcConfig() {
        return m_schedule.getServiceConfig();
    }

    /**
     * @param log
     */
    void adjustSchedule() {
        m_schedule.adjustSchedule();
    }

    public PollableInterface getInterface() {
        return m_pInterface;
    }

    public IPv4NetworkInterface getNetInterface() {
        return m_netInterface;
    }

    public void setUnresponsive(boolean unresponsiveFlag) {
        m_unresponsive = unresponsiveFlag;
    }

    public boolean isUnresponsive() {
        return m_unresponsive;
    }

}
