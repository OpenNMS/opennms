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

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
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
public class Trapd extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Trapd.class);

    public static final String LOG4J_CATEGORY = "trapd";
    
    /**
     * The last status sent to the service control manager.
     */
    private int m_status = START_PENDING;

    /**
     * The class instance used to receive new events from for the system.
     */
    @Autowired
    private TrapListener m_trapListener;

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
     * <p>onInit</p>
     */
    @Override
    protected synchronized void onInit() {
        BeanUtils.assertAutowiring(this);
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

        m_trapListener.start();

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
        m_trapListener.stop();

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
        m_trapListener.start();

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

        m_trapListener.stop();

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

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }
}
