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
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.scheduler.ReadyRunnable;
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
final class PollableService extends PollableElement implements Pollable, ReadyRunnable {
    /**
     * interface that this service belongs to
     */
    private PollableInterface m_pInterface;

    /**
     * Flag which indicates if previous poll returned
     * SnmpMonitor.SERVICE_UNRESPONSIVE.
     */
    private boolean m_unresponsiveFlag;

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
     * The service monitor used to poll this service/interface pair.
     */
    private final ServiceMonitor m_monitor;

    /**
     * List of all scheduled PollableService objects
     */
    private final List m_pollableServices;

    /**
     * The last time the service was polled...whether due to a scheduled poll or
     * node outage processing.
     */
    private long m_lastPoll;
    
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

        m_monitor = getPoller().getServiceMonitor(svcName);
        m_pollableServices = getPoller().getPollableServiceList();

        ServiceConfig svcConfig = new ServiceConfig(pkg, svcName, getPoller().getPollOutagesConfig());
        m_schedule = new Schedule(this, getPoller().getScheduler(), svcConfig);
        m_lastPoll = 0L;

        // Set status change values.
        setStatusChangeTime(0L);
        m_statusChangedFlag = false;
        if (getStatus() == ServiceMonitor.SERVICE_UNAVAILABLE) {
            if (svcLostDate == null)
                throw new IllegalArgumentException("The svcLostDate parm cannot be null if status is UNAVAILABLE!");

            setStatusChangeTime(svcLostDate.getTime());
        }
        m_unresponsiveFlag = false;

    }

    public PollableInterface getInterface() {
        return m_pInterface;
    }

    /**
     * Returns the service name
     */
    public String getServiceName() {
        return getSvcConfig().getServiceName();
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

    public long getLastPollTime() {
        return m_lastPoll;
    }

    public long getLastScheduleInterval() {
        return m_schedule.getLastInterval();
    }

    /**
     * Returns the time (in milliseconds) after which this is scheduled to run.
     */
    public long getScheduledRuntime() {
        return (this.getLastPollTime() + this.getLastScheduleInterval());
    }

    public String getPackageName() {
        return getSvcConfig().getPackageName();
    }

    /**
     * This method is used to evaluate the status of this interface and service
     * pair. If it is time to run the poll again then a value of true is
     * returned. If the interface is not ready then a value of false is
     * returned.
     * 
     * @throws java.lang.RuntimeException
     *             Throws if the ready time cannot be computed due to invalid
     *             downtime model.
     */
    public boolean isReady() {
        return m_schedule.isReady();
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
        m_pInterface.removeService(this);
    }

    /**
     * This method is used to return the next interval for this interface. If
     * the interval is zero then this service has never run and should be
     * scheduled immediantly. If the time is -1 then the node should be deleted.
     * Otherwise the appropriate scheduled time is returned.
     * 
     * @throws java.lang.RuntimeException
     *             Throws if the ready time cannot be computed due to invalid
     *             downtime model.
     */
    long recalculateInterval() {
        return m_schedule.recalculateInterval();
    }

    /**
     * 
     */
    private void sendEvent(String uei, Map properties) {
        Category log = ThreadCategory.getInstance(getClass());
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid((long) m_pInterface.getNode().getNodeId());
        event.setInterface(m_pInterface.getAddress().getHostAddress());
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
                log.debug("Sent event " + uei + " for " + m_pInterface.getNode().getNodeId() + "/" + m_pInterface.getAddress().getHostAddress() + "/" + getServiceName());
            }
        } catch (Throwable t) {
            log.error("Failed to send the event " + uei + " for interface " + m_pInterface.getAddress().getHostAddress(), t);
        }
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

            if (this.m_pInterface.getNode().getNodeId() == temp.m_pInterface.getNode().getNodeId() && this.getAddress().equals(temp.getAddress()) && this.getServiceName().equals(temp.getServiceName())) {
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

    /**
     * This is the main method of the class. An instance is normally enqueued on
     * the scheduler which checks its <code>isReady</code> method to determine
     * execution. If the instance is ready for execution then it is started with
     * it's own thread context to execute the query. The last step in the method
     * before it exits is to reschedule the interface.
     * 
     */
    public void run() {
        m_schedule.run();
    }

    /**
     * This an alternative entry point into the class. This was originally
     * created in order to support the PollableServiceProxy, which needed the
     * option of handling its own scheduling and needed to keep the
     * PollableService from rescheduling itself.
     * 
     * In addition to allowing this, it also allows exceptions that require a
     * rescheduling decision to pass back up the stack. In all other ways, this
     * method works the same as run().
     * 
     * @param reschedule
     *            set this to true if you want the pollable service to
     *            reschedule itself when done processing.
     * 
     * @throws LockUnavailableException
     *             If it was unable to obtain a node lock
     * @throws ThreadInterruped
     *             If the thread was interrtuped while waiting for a node lock.
     */
    public void run(boolean reschedule) throws LockUnavailableException, InterruptedException {
        this.doRun(reschedule);
    }

    /**
     * This used to be the implementation for the run() method. When we created
     * run(boolean), however, we needed to move the implementation down a level
     * lower so that we could overload the run() method.
     * 
     * @param allowedToRescheduleMyself
     *            set this to true if you want the pollable service to
     *            reschedule itself when done processing.
     * 
     * @throws LockUnavailableException
     *             If it was unable to obtain a node lock
     * @throws ThreadInterruped
     *             If the thread was interrtuped while waiting for a node lock.
     * 
     */
    void doRun(boolean allowedToRescheduleMyself) throws LockUnavailableException, InterruptedException {
        // Update last scheduled poll time if allowedToRescheduleMyself
        // flag is true
        if (allowedToRescheduleMyself)
            m_schedule.setLastScheduledPoll(System.currentTimeMillis());

        Category log = ThreadCategory.getInstance(getClass());

        // Is the service marked for deletion? If so simply return.
        //
        if (this.isDeleted()) {
            if (log.isDebugEnabled()) {
                log.debug("PollableService doRun: Skipping service marked as deleted on " + m_pInterface.getAddress().getHostAddress() + ", service = " + getServiceName() + ", status = " + getStatus());
            }
            return;
        }

        // NodeId
        int nodeId = m_pInterface.getNode().getNodeId();


        // Check scheduled outages to see if any apply indicating
        // that the poll should be skipped
        //
        if (getSvcConfig().scheduledOutage(this)) {
            // Outage applied...reschedule the service and return
            if (allowedToRescheduleMyself) {
                m_schedule.reschedule(true);
            }

            return;
        }

        // Is node outage processing enabled?
        if (getPollerConfig().nodeOutageProcessingEnabled()) {
            // Lookup PollableNode object using nodeId as index
            //
            // TODO: We alrady have the pollable node via the pollable interface
            PollableNode pNode = getPoller().findNode(nodeId);

            /*
             * Acquire lock to 'PollableNode'
             */
            boolean ownLock = false;
            try {
                // Attempt to obtain node lock...wait no longer than 500ms
                // We don't want to tie up the thread for long periods of time
                // waiting for the lock on the PollableNode to be released.
                if (log.isDebugEnabled())
                    log.debug("run: ------------- requesting node lock for nodeid: " + nodeId + " -----------");

                if (!(ownLock = pNode.getNodeLock(500)))
                    throw new LockUnavailableException("failed to obtain lock on nodeId " + nodeId);
            } catch (InterruptedException iE) {
                // failed to acquire lock
                throw new InterruptedException("failed to obtain lock on nodeId " + nodeId + ": " + iE.getMessage());
            }
            // Now we have a lock

            if (ownLock) // This is probably redundant, but better to be
                            // sure.
            {
                try {
                    // Make sure the node hasn't been deleted.
                    if (!pNode.isDeleted()) {
                        if (log.isDebugEnabled())
                            log.debug("run: calling poll() for " + nodeId + "/" + m_pInterface.getAddress().getHostAddress() + "/" + getServiceName());

                        pNode.poll(this);

                        if (log.isDebugEnabled())
                            log.debug("run: call to poll() finished for " + nodeId + "/" + m_pInterface.getAddress().getHostAddress() + "/" + getServiceName());
                    }
                } finally {
                    if (log.isDebugEnabled())
                        log.debug("run: ----------- releasing node lock for nodeid: " + nodeId + " ----------");
                    try {
                        pNode.releaseNodeLock();
                    } catch (InterruptedException iE) {
                        log.error("run: thread interrupted...failed to release lock on nodeId " + nodeId);
                    }
                }
            }
        } else {
            // Node outage processing disabled so simply poll the service
            if (log.isDebugEnabled())
                log.debug("run: node outage processing disabled, polling: " + m_pInterface.getAddress().getHostAddress() + "/" + getServiceName());
            this.poll();
        }

        // reschedule the service for polling
        if (allowedToRescheduleMyself) {
            m_schedule.reschedule(false);
        }

        return;
    }

    /**
     * @return
     */
    Poller getPoller() {
        return m_pInterface.getPoller();
    }

    /**
     * @return
     */
    private PollerConfig getPollerConfig() {
        return getPoller().getPollerConfig();
    }

    /**
     * <P>
     * Invokes a poll of the service via the ServiceMonitor.
     * </P>
     */
    public int poll() {
        Category log = ThreadCategory.getInstance(getClass());

        m_lastPoll = System.currentTimeMillis();
        m_statusChangedFlag = false;
        if (log.isDebugEnabled())
            log.debug("poll: starting new poll for " + this + ":" + getPackageName());

        // Poll the interface/service pair via the service monitor
        //
        int status = ServiceMonitor.SERVICE_UNAVAILABLE;
        Map propertiesMap = getSvcConfig().getPropertyMap();
        try {
            status = m_monitor.poll(m_netInterface, propertiesMap, getSvcConfig().getPackage());
            if (log.isDebugEnabled())
                log.debug("poll: polled for " + this + ":" + getPackageName()+" with result: " + Pollable.statusType[status]);
        } catch (NetworkInterfaceNotSupportedException ex) {
            log.error("poll: Interface " + getAddress().getHostAddress() + " Not Supported!", ex);
            return status;
        } catch (Throwable t) {
            log.error("poll: An undeclared throwable was caught polling interface " + getAddress().getHostAddress(), t);
        }

        // serviceUnresponsive behavior disabled?
        //
        if (!getPollerConfig().serviceUnresponsiveEnabled()) {
            // serviceUnresponsive behavior is disabled, a status
            // of SERVICE_UNRESPONSIVE is treated as SERVICE_UNAVAILABLE
            if (status == ServiceMonitor.SERVICE_UNRESPONSIVE)
                status = ServiceMonitor.SERVICE_UNAVAILABLE;
        } else {
            // Update unresponsive flag based on latest status
            // returned by the monitor and generate serviceUnresponsive
            // or serviceResponsive event if necessary.
            //
            switch (status) {
            case ServiceMonitor.SERVICE_UNRESPONSIVE:
                // Check unresponsive flag to determine if we need
                // to generate a 'serviceUnresponsive' event.
                //
                if (m_unresponsiveFlag == false) {
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
                if (m_unresponsiveFlag == true) {
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
        if (status != getStatus()) {
            // get the time of the status change
            //
            m_statusChangedFlag = true;
            setStatusChangeTime(System.currentTimeMillis());

            // Is node outage processing disabled?
            if (!getPollerConfig().nodeOutageProcessingEnabled()) {
                // node outage processing disabled, go ahead and generate
                // transition events.
                if (log.isDebugEnabled())
                    log.debug("poll: node outage disabled, status change will trigger event.");

                // Send the appropriate event
                //
                switch (status) {
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

        // Set status
        setStatus(status);

        m_schedule.setPollImmediate(false);

        // Reschedule the interface
        // 
        // NOTE: rescheduling now handled by PollableService.run()
        // reschedule(false);

        return getStatus();
    }

    /**
     * @return Returns the address.
     */
    public InetAddress getAddress() {
        return m_pInterface.getAddress();
    }

    /**
     * @param sm
     */
    public void releaseMonitor(ServiceMonitor sm) {
        sm.release(m_netInterface);
    }

    /**
     * @param monitor
     */
    public void initializeMonitor(ServiceMonitor monitor) {
        monitor.initialize(m_netInterface);
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
        return getAddress().getHostAddress() + ":" + getServiceName();
    }

    ServiceConfig getSvcConfig() {
        return m_schedule.getServiceConfig();
    }

}
