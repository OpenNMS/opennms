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
 * by the Free Software Foundation, either version 2 of the License,
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

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
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
public class Trapd extends AbstractServiceDaemon implements PausableFiber, TrapProcessorFactory, TrapNotificationListener, InitializingBean {
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

	private String m_snmpTrapAddress;

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
            LogUtils.warnf(this, e, "addTrap: Error adding trap to queue, it was interrupted");
            Thread.currentThread().interrupt();
        } catch (final FifoQueueException e) {
            LogUtils.warnf(this, e, "addTrap: Error adding trap to queue");
        }
    }

    /**
     * <p>onInit</p>
     */
    public synchronized void onInit() {
        Assert.state(m_trapdIpMgr != null, "trapdIpMgr must be set");
        Assert.state(m_eventReader != null, "eventReader must be set");
        Assert.state(m_backlogQ != null, "backlogQ must be set");
        Assert.state(m_snmpTrapAddress != null, "snmpTrapAddress must be set");
        Assert.state(m_snmpTrapPort != null, "snmpTrapPort must be set");
        Assert.state(m_processor != null, "processor must be set");

        try {
            m_trapdIpMgr.dataSourceSync();
        } catch (final SQLException e) {
            LogUtils.errorf(this, e, "init: Failed to load known IP address list");
            throw new UndeclaredThrowableException(e);
        }

        try {
        	InetAddress address = getInetAddress();
    		LogUtils.infof(this, "Listening on %s:%d", address == null ? "[all interfaces]" : InetAddressUtils.str(address), getSnmpTrapPort());
            SnmpUtils.registerForTraps(this, this, address, getSnmpTrapPort());
            m_registeredForTraps = true;

            LogUtils.debugf(this, "init: Creating the trap session");
        } catch (final IOException e) {
            if (e instanceof java.net.BindException) {
                managerLog().error("init: Failed to listen on SNMP trap port, perhaps something else is already listening?", e);
                LogUtils.errorf(this, e, "init: Failed to listen on SNMP trap port, perhaps something else is already listening?");
            } else {
                LogUtils.errorf(this, e, "init: Failed to initialize SNMP trap socket");
            }
            throw new UndeclaredThrowableException(e);
        }

        try {
            m_eventReader.open();
        } catch (final Throwable e) {
            LogUtils.errorf(this, e, "init: Failed to open event reader");
            throw new UndeclaredThrowableException(e);
        }
    }

    private InetAddress getInetAddress() {
    	if (m_snmpTrapAddress.equals("*")) {
    		return null;
    	}
		return InetAddressUtils.addr(m_snmpTrapAddress);
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

        LogUtils.debugf(this, "start: Initializing the trapd config factory");

        m_processor.start();
        m_status = RUNNING;

        LogUtils.debugf(this, "start: Trapd ready to receive traps");
    }

    /**
     * Pauses Trapd
     */
    public void onPause() {
        if (m_status != RUNNING) {
            return;
        }

        m_status = PAUSE_PENDING;

        LogUtils.debugf(this, "pause: Calling pause on processor");

        m_processor.pause();
        m_status = PAUSED;

        LogUtils.debugf(this, "pause: Trapd paused");
    }

    /**
     * Resumes Trapd
     */
    public void onResume() {
        if (m_status != PAUSED) {
            return;
        }

        m_status = RESUME_PENDING;

        LogUtils.debugf(this, "resume: Calling resume on processor");

        m_processor.resume();
        m_status = RUNNING;

        LogUtils.debugf(this, "resume: Trapd resumed");
    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    public synchronized void onStop() {
        m_status = STOP_PENDING;

        // shutdown and wait on the background processing thread to exit.
        LogUtils.debugf(this, "stop: closing communication paths.");

        try {
            if (m_registeredForTraps) {
                LogUtils.debugf(this, "stop: Closing SNMP trap session.");
                SnmpUtils.unregisterForTraps(this, getInetAddress(), getSnmpTrapPort());
                LogUtils.debugf(this, "stop: SNMP trap session closed.");
            } else {
            	LogUtils.debugf(this, "stop: not attemping to closing SNMP trap session--it was never opened");
            }

        } catch (final IOException e) {
            LogUtils.warnf(this, e, "stop: exception occurred closing session");
        } catch (final IllegalStateException e) {
            LogUtils.debugf(this, e, "stop: The SNMP session was already closed");
        }

        LogUtils.debugf(this, "stop: Stopping queue processor.");

        m_processor.stop();
        m_eventReader.close();

        m_status = STOPPED;

        LogUtils.debugf(this, "stop: Trapd stopped");
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
    public void trapError(final int error, final String msg) {
        LogUtils.warnf(this, "Error Processing Received Trap: error = " + error + (msg != null ? ", ref = " + msg : ""));
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

    public String getSnmpTrapAddress() {
    	return m_snmpTrapAddress;
    }
    
    public void setSnmpTrapAddress(final String snmpTrapAddress) {
    	m_snmpTrapAddress = snmpTrapAddress;
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
