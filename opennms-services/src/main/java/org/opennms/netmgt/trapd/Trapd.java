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
// 2008 Jan 26: Rename TrapHandler to Trapd since Trapd did almost nothing.
//              Dependency inject all of the bits for Trapd. - dj@opennms.org
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
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
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
 * traps are received.
 * </p>
 *
 * <p>
 * The received traps are converted into XML and sent to eventd.
 * </p>
 *
 * <p>
 * <strong>Note: </strong>Trapd is a PausableFiber so as to receive control
 * events. However, a 'pause' on Trapd has no impact on the receiving and
 * processing of traps.
 * </p>
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class Trapd extends AbstractServiceDaemon implements PausableFiber, TrapProcessorFactory,
                                    TrapNotificationListener,
                                    InitializingBean {
    /*
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
     * The class instance used to receive new events from for the system.
     */
    private BroadcastEventProcessor m_eventReader;

    /**
     * Trapd IP manager.  Contains IP address -> node ID mapping.
     */
    private TrapdIpMgr m_trapdIpMgr;

    private Integer m_snmpTrapPort;

    private boolean m_registeredForTraps;


    /**
     * <P>
     * Constructs a new Trapd object that receives and forwards trap messages
     * via JSDT. The session is initialized with the default client name of <EM>
     * OpenNMS.trapd</EM>. The trap session is started on the default port, as
     * defined by the SNMP library.
     * </P>
     *
     * @see org.opennms.protocols.snmp.SnmpTrapSession
     */
    public Trapd() {
        super("OpenNMS.Trapd");
    }
    
    /**
     * <p>createTrapProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.TrapProcessor} object.
     */
    public TrapProcessor createTrapProcessor() {
        return new EventCreator(m_trapdIpMgr);
    }

    /** {@inheritDoc} */
    public void trapReceived(TrapNotification trapNotification) {
        addTrap(trapNotification);
    }

    private void addTrap(TrapNotification o) {
        try {
            m_backlogQ.add(o);
        } catch (InterruptedException e) {
            log().warn("addTrap: Error adding trap to queue, it was interrupted", e);
        } catch (FifoQueueException e) {
            log().warn("addTrap: Error adding trap to queue", e);
        }
    }

    /**
     * <p>onInit</p>
     */
    public synchronized void onInit() {
        Assert.state(m_trapdIpMgr != null, "trapdIpMgr must be set");
        Assert.state(m_eventReader != null, "eventReader must be set");
        Assert.state(m_backlogQ != null, "backlogQ must be set");
        Assert.state(m_snmpTrapPort != null, "snmpTrapPort must be set");
        Assert.state(m_processor != null, "processor must be set");

        try {
            m_trapdIpMgr.dataSourceSync();
        } catch (SQLException e) {
            log().error("init: Failed to load known IP address list: " + e, e);
            throw new UndeclaredThrowableException(e);
        }

        try {
            SnmpUtils.registerForTraps(this, this, getSnmpTrapPort());
            m_registeredForTraps = true;

            log().debug("init: Creating the trap session");
        } catch (IOException e) {
            if (e instanceof java.net.BindException) {
                managerLog().error("init: Failed to listen on SNMP trap port, perhaps something else is already listening?", e);
                log().error("init: Failed to listen on SNMP trap port, perhaps something else is already listening?", e);
            } else {
                log().error("init: Failed to initialize SNMP trap socket", e);
            }
            throw new UndeclaredThrowableException(e);
        }

        try {
            m_eventReader.open();
        } catch (Throwable e) {
            log().error("init: Failed to open event reader: " + e, e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private Category managerLog() {
        return Logger.getLogger("OpenNMS.Manager");
    }

    /**
     * Create the SNMP trap session and create the communication channel
     * to communicate with eventd.
     *
     * @exception java.lang.reflect.UndeclaredThrowableException
     *                if an unexpected database, or IO exception occurs.
     * @see org.opennms.protocols.snmp.SnmpTrapSession
     * @see org.opennms.protocols.snmp.SnmpTrapHandler
     */
    public synchronized void onStart() {
        m_status = STARTING;

        log().debug("start: Initializing the trapd config factory");

        m_processor.start();

        m_status = RUNNING;

        log().debug("start: Trapd ready to receive traps");
    }

    /**
     * Pauses Trapd
     */
    public void onPause() {
        if (m_status != RUNNING) {
            return;
        }

        m_status = PAUSE_PENDING;

        log().debug("pause: Calling pause on processor");

        m_processor.pause();

        log().debug("pause: Processor paused");

        m_status = PAUSED;

        log().debug("pause: Trapd paused");
    }

    /**
     * Resumes Trapd
     */
    public void onResume() {
        if (m_status != PAUSED) {
            return;
        }

        m_status = RESUME_PENDING;

        log().debug("resume: Calling resume on processor");

        m_processor.resume();

        log().debug("resume: Processor resumed");

        m_status = RUNNING;

        log().debug("resume: Trapd resumed");
    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    public synchronized void onStop() {
        m_status = STOP_PENDING;

        // shutdown and wait on the background processing thread to exit.
        log().debug("stop: closing communication paths.");

        try {
            if (m_registeredForTraps) {
                log().debug("stop: Closing SNMP trap session.");
                SnmpUtils.unregisterForTraps(this, getSnmpTrapPort());
                log().debug("stop: SNMP trap session closed.");
            } else {
                log().debug("stop: not attemping to closing SNMP trap session--it was never opened");
            }

        } catch (IOException e) {
            log().warn("stop: exception occurred closing session: " + e, e);
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

    /** {@inheritDoc} */
    public void trapError(int error, String msg) {
        log().warn("Error Processing Received Trap: error = " + error
                 + (msg != null ? ", ref = " + msg : ""));
    }

    /**
     * <p>getTrapdIpMgr</p>
     *
     * @return a {@link org.opennms.netmgt.trapd.TrapdIpMgr} object.
     */
    public TrapdIpMgr getTrapdIpMgr() {
        return m_trapdIpMgr;
    }

    /**
     * <p>setTrapdIpMgr</p>
     *
     * @param trapdIpMgr a {@link org.opennms.netmgt.trapd.TrapdIpMgr} object.
     */
    public void setTrapdIpMgr(TrapdIpMgr trapdIpMgr) {
        m_trapdIpMgr = trapdIpMgr;
    }

    /**
     * <p>getEventReader</p>
     *
     * @return a {@link org.opennms.netmgt.trapd.BroadcastEventProcessor} object.
     */
    public BroadcastEventProcessor getEventReader() {
        return m_eventReader;
    }

    /**
     * <p>setEventReader</p>
     *
     * @param eventReader a {@link org.opennms.netmgt.trapd.BroadcastEventProcessor} object.
     */
    public void setEventReader(BroadcastEventProcessor eventReader) {
        m_eventReader = eventReader;
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
     * <p>getProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.trapd.TrapQueueProcessor} object.
     */
    public TrapQueueProcessor getProcessor() {
        return m_processor;
    }

    /**
     * <p>setProcessor</p>
     *
     * @param processor a {@link org.opennms.netmgt.trapd.TrapQueueProcessor} object.
     */
    public void setProcessor(TrapQueueProcessor processor) {
        m_processor = processor;
    }

    /**
     * <p>getSnmpTrapPort</p>
     *
     * @return a int.
     */
    public int getSnmpTrapPort() {
        return m_snmpTrapPort;
    }
    
    /**
     * <p>setSnmpTrapPort</p>
     *
     * @param snmpTrapPort a int.
     */
    public void setSnmpTrapPort(int snmpTrapPort) {
        m_snmpTrapPort = snmpTrapPort;
    }
}
