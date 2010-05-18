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
// 2008 Jan 26: Switch dependency injection to use setter injection
//              instead of constructor injection and implement
//              InitializingBean interface.  Improve assertions and
//              exceptions. - dj@opennms.org
// 2008 Jan 08: Format code, Java 5 generics, use dependency injection
//              for EventConfDao and use it instead of EventConfigurationManager. - dj@opennms.org
// 2008 Jan 05: Indent. - dj@opennms.org
// 2005 Jan 11: Added a check to insure V2 traps had TIMTICKS varbind.
// 2003 Aug 21: Modifications to support ScriptD.
// 2003 Feb 28: Small fix for null terminated strings in traps.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 08: Added code to associate IP addresses from traps with nodes.
// 2002 Nov 29: Fixed a small bug in trap handler. Bug #676.
// 2002 Jul 18: Added a check for bad varbind from Extreme traps.
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

package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The TrapQueueProcessor handles the conversion of V1 and V2 traps to events
 * and sending them out the JSDT channel that eventd is listening on
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 *  
 */
class TrapQueueProcessor implements Runnable, PausableFiber, InitializingBean {
    /**
     * The input queue
     */
    private FifoQueue<TrapNotification> m_backlogQ;

    /**
     * The name of the local host.
     */
    private String m_localAddr;

    /**
     * Current status of the fiber
     */
    private int m_status;

    /**
     * The thread that is executing the <code>run</code> method on behalf of
     * the fiber.
     */
    private Thread m_worker;

    /**
     * Whether or not a newSuspect event should be generated with a trap from an
     * unknown IP address
     */
    private Boolean m_newSuspect;

    /**
     * The event IPC manager to which we send events created from traps.
     */
    private EventIpcManager m_eventMgr;

    /**
     * The event configuration DAO that we use to convert from traps to events.
     */
    private EventConfDao m_eventConfDao;

    /**
     * Process a V2 trap and convert it to an event for transmission.
     * 
     * <p>
     * From RFC2089 ('Mapping SNMPv2 onto SNMPv1'), section 3.3 ('Processing an
     * outgoing SNMPv2 TRAP')
     * </p>
     * 
     * <p>
     * <strong>2b </strong>
     * <p>
     * If the snmpTrapOID.0 value is one of the standard traps the specific-trap
     * field is set to zero and the generic trap field is set according to this
     * mapping:
     * <p>
     * 
     * <pre>
     * 
     *  
     *   
     *    
     *     
     *      
     *            value of snmpTrapOID.0                generic-trap
     *            ===============================       ============
     *            1.3.6.1.6.3.1.1.5.1 (coldStart)                  0
     *            1.3.6.1.6.3.1.1.5.2 (warmStart)                  1
     *            1.3.6.1.6.3.1.1.5.3 (linkDown)                   2
     *            1.3.6.1.6.3.1.1.5.4 (linkUp)                     3
     *            1.3.6.1.6.3.1.1.5.5 (authenticationFailure)      4
     *            1.3.6.1.6.3.1.1.5.6 (egpNeighborLoss)            5
     *       
     *      
     *     
     *    
     *   
     *  
     * </pre>
     * 
     * <p>
     * The enterprise field is set to the value of snmpTrapEnterprise.0 if this
     * varBind is present, otherwise it is set to the value snmpTraps as defined
     * in RFC1907 [4].
     * </p>
     * 
     * <p>
     * <strong>2c. </strong>
     * </p>
     * <p>
     * If the snmpTrapOID.0 value is not one of the standard traps, then the
     * generic-trap field is set to 6 and the specific-trap field is set to the
     * last subid of the snmpTrapOID.0 value.
     * </p>
     * 
     * <p>
     * If the next to last subid of snmpTrapOID.0 is zero, then the enterprise
     * field is set to snmpTrapOID.0 value and the last 2 subids are truncated
     * from that value. If the next to last subid of snmpTrapOID.0 is not zero,
     * then the enterprise field is set to snmpTrapOID.0 value and the last 1
     * subid is truncated from that value.
     * </p>
     * 
     * <p>
     * In any event, the snmpTrapEnterprise.0 varBind (if present) is ignored in
     * this case.
     * </p>
     * 
     * @param info
     *            V2 trap
     */
    private void process(TrapNotification info) {
        try {
            processTrapEvent(((EventCreator)info.getTrapProcessor()).getEvent());
        } catch (IllegalArgumentException e) {
            log().info(e.getMessage());
        }
    }

    public void processTrapEvent(Event event) {
        String trapInterface = event.getInterface();

        org.opennms.netmgt.xml.eventconf.Event econf = m_eventConfDao.findByEvent(event);
        if (econf == null || econf.getUei() == null) {
            event.setUei("uei.opennms.org/default/trap");
        } else {
            event.setUei(econf.getUei());
        }

        if (econf != null) {
            Logmsg logmsg = econf.getLogmsg();
            if (logmsg != null) {
                String dest = logmsg.getDest();
                if ("discardtraps".equals(dest)) {
                    log().debug("Trap discarded due to matching event having logmsg dest == discardtraps");
                    return;
                }
            }
        }

        // send the event to eventd
        m_eventMgr.sendNow(event);

        log().debug("Trap successfully converted and sent to eventd with UEI " + event.getUei());

        if (!event.hasNodeid() && m_newSuspect) {
            sendNewSuspectEvent(trapInterface);

            if (log().isDebugEnabled()) {
                log().debug("Sent newSuspectEvent for interface: " + trapInterface);
            }
        }
    }

    /**
     * Send a newSuspect event for the interface
     * 
     * @param trapInterface
     *            The interface for which the newSuspect event is to be
     *            generated
     */
    private void sendNewSuspectEvent(String trapInterface) {
        // construct event with 'trapd' as source
        Event event = new Event();
        event.setSource("trapd");
        event.setUei(org.opennms.netmgt.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
        event.setHost(m_localAddr);
        event.setInterface(trapInterface);
        event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));

        // send the event to eventd
        m_eventMgr.sendNow(event);
    }

    /**
     * Returns true if the status is ok and the thread should continue running.
     * If the status returend is false then the thread should exit.
     *  
     */
    private synchronized boolean statusOK() {
        // Loop until there is a new client or we are shutdown
        boolean exitThread = false;
        boolean exitCheck = false;
        while (!exitCheck) {
            // check the child thread!
            if (m_worker.isAlive() == false && m_status != STOP_PENDING) {
                log().warn(getName() + " terminated abnormally");
                m_status = STOP_PENDING;
            }

            // do normal status checks now
            if (m_status == STOP_PENDING) {
                exitCheck = true;
                exitThread = true;
                m_status = STOPPED;
            } else if (m_status == PAUSE_PENDING) {
                pause();
            } else if (m_status == RESUME_PENDING) {
                resume();
            } else if (m_status == PAUSED) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    m_status = STOP_PENDING;
                }
            } else if (m_status == RUNNING) {
                exitCheck = true;
            }

        } // end !exit check

        return !exitThread;

    } // statusOK

    /**
     * The constructor
     */
    public TrapQueueProcessor() {
        try {
            m_localAddr = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            m_localAddr = "localhost";
            log().error("<ctor>: Error looking up local hostname: " + e, e);
        }

    }



    /**
     * Starts the current fiber. If the fiber has already been started,
     * regardless of it's current state, then an IllegalStateException is
     * thrown.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber has already been started.
     *  
     */
    public synchronized void start() {
        Assert.state(m_worker == null, "The fiber is running or has already run");

        m_status = STARTING;

        m_worker = new Thread(this, getName());
        m_worker.start();

        if (log().isDebugEnabled()) {
            log().debug(getName() + " started");
        }
    }

    /**
     * Pauses the current fiber.
     */
    public synchronized void pause() {
        Assert.state(m_worker != null && m_worker.isAlive(), "The fiber is not running");

        m_status = PAUSED;
        notifyAll();
    }

    /**
     * Resumes the currently paused fiber.
     */
    public synchronized void resume() {
        Assert.state(m_worker != null && m_worker.isAlive(), "The fiber is not running");

        m_status = RUNNING;
        notifyAll();
    }

    /**
     * <p>
     * Stops this fiber. If the fiber has never been started then an
     * <code>IllegalStateExceptio</code> is generated.
     * </p>
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber has never been started.
     */
    public synchronized void stop() {
        Assert.state(m_worker != null, "The fiber has never run");

        m_status = STOP_PENDING;
        m_worker.interrupt();
        notifyAll();
    }

    /**
     * Returns the name of the fiber.
     * 
     * @return The name of the Fiber.
     */
    public String getName() {
        return "TrapQueueProcessor";
    }

    /**
     * Returns the current status of the fiber
     * 
     * @return The status of the Fiber.
     */
    public synchronized int getStatus() {
        if (m_worker != null && !m_worker.isAlive()) {
            m_status = STOPPED;
        }

        return m_status;
    }

    /**
     * Reads off of the input queue and depending on the type (V1 or V2 trap) of
     * object read, process the traps to convert them to events and send them
     * out
     */
    public void run() {
        synchronized (this) {
            m_status = RUNNING;
        }

        while (statusOK()) {
            TrapNotification o = null;
            try {
                o = m_backlogQ.remove(1000);
            } catch (InterruptedException iE) {
                log().debug("Trapd.QueueProcessor: caught interrupted exception");

                o = null;

                m_status = STOP_PENDING;
            } catch (FifoQueueException qE) {
                log().debug("Trapd.QueueProcessor: caught fifo queue exception");
                log().debug(qE.getLocalizedMessage(), qE);

                o = null;

                m_status = STOP_PENDING;
            }

            if (o != null && statusOK()) {
                try {
                    process(o);
                } catch (Throwable t) {
                    log().error("Unexpected error processing trap: " + t, t);
                }
            }
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    public FifoQueue<TrapNotification> getBacklogQ() {
        return m_backlogQ;
    }

    public void setBacklogQ(FifoQueue<TrapNotification> backlogQ) {
        m_backlogQ = backlogQ;
    }

    public EventConfDao getEventConfDao() {
        return m_eventConfDao;
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }

    public EventIpcManager getEventMgr() {
        return m_eventMgr;
    }

    public void setEventMgr(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    public Boolean isNewSuspect() {
        return m_newSuspect;
    }

    public void setNewSuspect(Boolean newSuspect) {
        m_newSuspect = newSuspect;
    }

    public void afterPropertiesSet() throws IllegalStateException {
        Assert.state(m_backlogQ != null, "property backlogQ must be set");
        Assert.state(m_eventConfDao != null, "property eventConfDao must be set");
        Assert.state(m_eventMgr != null, "property eventMgr must be set");
        Assert.state(m_newSuspect != null, "property newSuspect must be set");
    }
}