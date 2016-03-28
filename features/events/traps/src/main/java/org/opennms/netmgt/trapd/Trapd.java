/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.trapd;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import javax.annotation.Resource;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.snmp.TrapProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
public class Trapd extends AbstractServiceDaemon implements TrapProcessorFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(Trapd.class);

    public static final String LOG4J_CATEGORY = "trapd";
    
    /**
     * The last status sent to the service control manager.
     */
    private int m_status = START_PENDING;

    /**
     * The queue processing thread
     */
    @Autowired
    private TrapQueueProcessorFactory m_processorFactory;

    /**
     * The class instance used to receive new events from for the system.
     */
    @Autowired
    private BroadcastEventProcessor m_eventReader;

    /**
     * The class instance used to receive new events from for the system.
     */
    @Autowired
    private TrapReceiver m_trapReceiver;

    /**
     * Trapd IP manager.  Contains IP address -> node ID mapping.
     */
    @Autowired
    private TrapdIpMgr m_trapdIpMgr;

    @Resource(name="snmpTrapAddress")
    private String m_snmpTrapAddress;

    @Resource(name="snmpTrapPort")
    private Integer m_snmpTrapPort;

    @Resource(name="snmpV3Users")
    private List<SnmpV3User> m_snmpV3Users;

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
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>createTrapProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.TrapProcessor} object.
     */
    @Override
    public TrapProcessor createTrapProcessor() {
        return new EventCreator(m_trapdIpMgr);
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected synchronized void onInit() {
        BeanUtils.assertAutowiring(this);

        m_trapdIpMgr.dataSourceSync();

        try {
            // Start the event listener
            m_eventReader.open();
        } catch (final Throwable e) {
            LOG.error("init: Failed to open event reader", e);
            throw new UndeclaredThrowableException(e);
        }
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
    @Override
    protected synchronized void onStart() {
        m_status = STARTING;

        LOG.debug("start: Initializing the Trapd receiver");

        m_trapReceiver.start();

        m_status = RUNNING;

        LOG.debug("start: Trapd is ready to receive traps");
    }

    /**
     * Pauses Trapd
     */
    @Override
    protected void onPause() {
        if (m_status != RUNNING) {
            return;
        }

        m_status = PAUSE_PENDING;

        LOG.debug("pause: Calling pause on trap receiver");
        m_trapReceiver.stop();

        m_status = PAUSED;

        LOG.debug("pause: Trapd paused");
    }

    /**
     * Resumes Trapd
     */
    @Override
    protected void onResume() {
        if (m_status != PAUSED) {
            return;
        }

        m_status = RESUME_PENDING;

        LOG.debug("resume: Calling resume on trap receiver");
        m_trapReceiver.start();

        m_status = RUNNING;

        LOG.debug("resume: Trapd resumed");
    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    @Override
    protected synchronized void onStop() {
        m_status = STOP_PENDING;

        // shutdown and wait on the background processing thread to exit.
        LOG.debug("stop: Closing communication paths");

        m_trapReceiver.stop();

        // TODO: Should we do this? m_eventReader is launched in onInit()
        // but it should probably be launched in onStart() so that it
        // follows the lifecycle of this class.
        LOG.debug("stop: Stopping event processor");

        m_eventReader.close();

        m_status = STOPPED;

        LOG.debug("stop: Trapd stopped");
    }

    /**
     * Returns the current status of the service.
     *
     * @return The service's status.
     */
    @Override
    public synchronized int getStatus() {
        return m_status;
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

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }

    public long getV1TrapsReceived() {
        return TrapQueueProcessor.getV1TrapsReceived();
    }

    public long getV2cTrapsReceived() {
        return TrapQueueProcessor.getV2cTrapsReceived();
    }

    public long getV3TrapsReceived() {
        return TrapQueueProcessor.getV3TrapsReceived();
    }

    public long getTrapsDiscarded() {
        return TrapQueueProcessor.getTrapsDiscarded();
    }

    public long getTrapsErrored() {
        return TrapQueueProcessor.getTrapsErrored();
    }
}
