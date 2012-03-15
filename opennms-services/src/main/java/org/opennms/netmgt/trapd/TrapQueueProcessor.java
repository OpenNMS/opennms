/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
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
    private static final String LOCALHOST_ADDRESS = InetAddressUtils.getLocalHostName();

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

    /**
     * <p>processTrapEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void processTrapEvent(final Event event) {
    	final InetAddress trapInterface = event.getInterfaceAddress();

    	final org.opennms.netmgt.xml.eventconf.Event econf = m_eventConfDao.findByEvent(event);
        if (econf == null || econf.getUei() == null) {
            event.setUei("uei.opennms.org/default/trap");
        } else {
            event.setUei(econf.getUei());
        }

        if (econf != null) {
        	final Logmsg logmsg = econf.getLogmsg();
            if (logmsg != null) {
                final String dest = logmsg.getDest();
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
            sendNewSuspectEvent(InetAddressUtils.str(trapInterface));

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
        EventBuilder bldr = new EventBuilder(org.opennms.netmgt.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
        bldr.setInterface(addr(trapInterface));
        bldr.setHost(LOCALHOST_ADDRESS);

        // send the event to eventd
        m_eventMgr.sendNow(bldr.getEvent());
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
    }



    /**
     * Starts the current fiber. If the fiber has already been started,
     * regardless of it's current state, then an IllegalStateException is
     * thrown.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber has already been started.
     */
    @Override
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
    @Override
    public synchronized void pause() {
        Assert.state(m_worker != null && m_worker.isAlive(), "The fiber is not running");

        m_status = PAUSED;
        notifyAll();
    }

    /**
     * Resumes the currently paused fiber.
     */
    @Override
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
    @Override
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
    @Override
    public String getName() {
        return "TrapQueueProcessor";
    }

    /**
     * Returns the current status of the fiber
     *
     * @return The status of the Fiber.
     */
    @Override
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
    @Override
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

    /**
     * <p>getBacklogQ</p>
     *
     * @return a {@link org.opennms.core.queue.FifoQueue} object.
     */
    public FifoQueue<TrapNotification> getBacklogQ() {
        return m_backlogQ;
    }

    /**
     * <p>setBacklogQ</p>
     *
     * @param backlogQ a {@link org.opennms.core.queue.FifoQueue} object.
     */
    public void setBacklogQ(FifoQueue<TrapNotification> backlogQ) {
        m_backlogQ = backlogQ;
    }

    /**
     * <p>getEventConfDao</p>
     *
     * @return a {@link org.opennms.netmgt.config.EventConfDao} object.
     */
    public EventConfDao getEventConfDao() {
        return m_eventConfDao;
    }

    /**
     * <p>setEventConfDao</p>
     *
     * @param eventConfDao a {@link org.opennms.netmgt.config.EventConfDao} object.
     */
    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }

    /**
     * <p>getEventMgr</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public EventIpcManager getEventMgr() {
        return m_eventMgr;
    }

    /**
     * <p>setEventMgr</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public void setEventMgr(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    /**
     * <p>isNewSuspect</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean isNewSuspect() {
        return m_newSuspect;
    }

    /**
     * <p>setNewSuspect</p>
     *
     * @param newSuspect a {@link java.lang.Boolean} object.
     */
    public void setNewSuspect(Boolean newSuspect) {
        m_newSuspect = newSuspect;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    public void afterPropertiesSet() throws IllegalStateException {
        Assert.state(m_backlogQ != null, "property backlogQ must be set");
        Assert.state(m_eventConfDao != null, "property eventConfDao must be set");
        Assert.state(m_eventMgr != null, "property eventMgr must be set");
        Assert.state(m_newSuspect != null, "property newSuspect must be set");
    }
}
