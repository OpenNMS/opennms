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
// 2008 Jan 08: Dependency inject EventConfDao and use that instead of
//              EventConfigurationManager.  Create log() method. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 08: Added code to associate the IP address in traps with nodes
//              and added the option to discover nodes based on traps.
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
// 

package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.log4j.Category;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.TrapNotificationListener;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>
 * The Trapd listens for SNMP traps on the standard port(162). Creates a
 * SnmpTrapSession and implements the SnmpTrapHandler to get callbacks when
 * traps are received
 * </p>
 * 
 * <p>
 * The received traps are converted into XML and sent to eventd
 * </p>
 * 
 * <p>
 * <strong>Note: </strong>Trapd is a PausableFiber so as to receive control
 * events. However, a 'pause' on Trapd has no impact on the receiving and
 * processing of traps
 * </p>
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * 
 */
public class TrapHandler implements PausableFiber, TrapProcessorFactory,
                                    TrapNotificationListener,
                                    InitializingBean {
    /**
     * The name of the logging category for Trapd.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Trapd";

    /**
     * The singlton instance.
     */
    private static final TrapHandler m_singleton = new TrapHandler();

    /**
     * Set the Trapd configuration
     */
    private TrapdConfig m_trapdConfig;
    
    /**
     * Event manager
     */
    private EventIpcManager m_eventMgr;

    /**
     * The name of this service.
     */
    private String m_name = LOG4J_CATEGORY;

    /**
     * The last status sent to the service control manager.
     */
    private int m_status = START_PENDING;

    /**
     * The communication queue
     */
    private FifoQueue<TrapNotification> m_backlogQ;

    /**
     * The queue processing thread
     */
    private TrapQueueProcessor m_processor;

    /**
     * The class instance used to recieve new events from for the system.
     */
    private BroadcastEventProcessor m_eventReader;

    private EventConfDao m_eventConfDao;

    /**
     * <P>
     * Constructs a new Trapd object that receives and forwards trap messages
     * via JSDT. The session is initialized with the default client name of <EM>
     * OpenNMS.trapd</EM>. The trap session is started on the default port, as
     * defined by the SNMP libarary.
     * </P>
     * 
     * @see org.opennms.protocols.snmp.SnmpTrapSession
     */
    public TrapHandler() {
    }

    public void setTrapdConfig(TrapdConfig trapdConfig) {
        m_trapdConfig = trapdConfig;
    }
    
    public TrapProcessor createTrapProcessor() {
        return new EventCreator();
    }

    public void trapReceived(TrapNotification trapNotification) {
        addTrap(trapNotification);
    }

    private void addTrap(TrapNotification o) {
        try {
            m_backlogQ.add(o);
        } catch (InterruptedException e) {
            log().warn("snmpReceivedTrap: Error adding trap to queue, it was "
                     + "interrupted", e);
        } catch (FifoQueueException e) {
            log().warn("snmpReceivedTrap: Error adding trap to queue", e);
        }
    }

    public synchronized void init() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        try {
            // Get the newSuspectOnTrap flag
            boolean m_newSuspect = m_trapdConfig.getNewSuspectOnTrap();

            // set up the trap processor
            m_backlogQ = new FifoQueueImpl<TrapNotification>();
            m_processor = new TrapQueueProcessor(m_backlogQ, m_newSuspect,
                                                 m_eventMgr, m_eventConfDao);

            log().debug("start: Creating the trap queue processor");
            
            SnmpUtils.registerForTraps(this, this,
                                       m_trapdConfig.getSnmpTrapPort());

            log().debug("start: Creating the trap session");
        } catch (IOException e) {
            log().error("Failed to setup SNMP trap port", e);
            throw new UndeclaredThrowableException(e);
        }

        try {
            m_eventReader = new BroadcastEventProcessor();
            m_eventReader.setEventManager(m_eventMgr);
            m_eventReader.open();
        } catch (Exception e) {
            log().error("Failed to create event reader",
                                               e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Create the SNMP trap session and create the communication channel
     * to communicate with eventd.
     * 
     * @exception java.lang.reflect.UndeclaredThrowableException
     *                if an unexpected database, or IO exception occurs.
     * 
     * @see org.opennms.protocols.snmp.SnmpTrapSession
     * @see org.opennms.protocols.snmp.SnmpTrapHandler
     */
    public synchronized void start() {
        m_status = STARTING;

        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        log().debug("start: Initializing the trapd config factory");

        m_processor.start();

        m_status = RUNNING;

        log().debug("start: Trapd ready to receive traps");
    }

    /**
     * Pauses Trapd
     */
    public void pause() {
        if (m_status != RUNNING) {
            return;
        }

        m_status = PAUSE_PENDING;

        log().debug("Calling pause on processor");

        m_processor.pause();

        log().debug("Processor paused");

        m_status = PAUSED;

        log().debug("Trapd paused");
    }

    /**
     * Resumes Trapd
     */
    public void resume() {
        if (m_status != PAUSED) {
            return;
        }

        m_status = RESUME_PENDING;

        log().debug("Calling resume on processor");

        m_processor.resume();

        log().debug("Processor resumed");

        m_status = RUNNING;

        log().debug("Trapd resumed");
    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    public synchronized void stop() {
        m_status = STOP_PENDING;

        // shutdown and wait on the background processing thread to exit.
        log().debug("exit: closing communication paths.");

        try {
            log().debug("stop: Closing SNMP trap session.");

            SnmpUtils.unregisterForTraps(this, m_trapdConfig.getSnmpTrapPort());

            log().debug("stop: SNMP trap session closed.");
        } catch (IOException e) {
            log().warn("stop: exception occurred closing session", e);
        } catch (IllegalStateException e) {
            log().debug("stop: The SNMP session was already closed");
        }

        log().debug("stop: Stopping queue processor.");

        // interrupt the processor daemon thread
        m_processor.stop();

        m_eventReader.close();

        m_status = STOPPED;

        log().debug("stop: Trapd stopped");
    }

    /**
     * Returns the current status of the service.
     * 
     * @return The service's status.
     */
    public synchronized int getStatus() {
        return m_status;
    }

    /**
     * Returns the singular instance of the trapd daemon. There can be only one
     * instance of this service per virtual machine.
     */
    public static TrapHandler getInstance() {
        return m_singleton;
    }

    /**
     * Returns the name of the service.
     * 
     * @return The service's name.
     */
    public String getName() {
        return m_name;
    }
    
    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    public void trapError(int error, String msg) {
        log().warn("Error Processing Received Trap: error = " + error
                 + (msg != null ? ", ref = " + msg : ""));
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void afterPropertiesSet() {
        Assert.state(m_eventMgr != null, "eventManager must be set");
        Assert.state(m_trapdConfig != null, "trapdConfig must be set");
        Assert.state(m_eventConfDao != null, "eventConfDao must be set");
    }

    public EventConfDao getEventConfDao() {
        return m_eventConfDao;
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }
}
