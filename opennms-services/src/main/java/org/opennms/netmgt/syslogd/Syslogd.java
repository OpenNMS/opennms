/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.EventDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The received messages are converted into XML and sent to eventd
 * </p>
 * <p/>
 * <strong>Note: </strong>Syslogd is a PausableFiber so as to receive control
 * events. However, a 'pause' on Syslogd has no impact on the receiving and
 * processing of traps
 * </p>
 */
 /**
  * <p>Syslogd class.</p>
  *
  * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
  * @author <a href="mailto:david@opennms.org">David Hustace</a>
  * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
  * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
  * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
  */
 public class Syslogd extends AbstractServiceDaemon {
     
     private static final Logger LOG = LoggerFactory.getLogger(Syslogd.class);
     
     /**
     * The name of the logging category for Syslogd.
     */
    static final String LOG4J_CATEGORY = "syslogd";

    /**
     * The singleton instance.
     */
    private static final Syslogd m_singleton = new Syslogd();

    /**
     * <p>getSingleton</p>
     *
     * @return a {@link org.opennms.netmgt.syslogd.Syslogd} object.
     */
    public synchronized static Syslogd getSingleton() {
        return m_singleton;
    }

    private SyslogHandler m_udpEventReceiver;

    private EventDao m_eventDao;

    /**
     * <p>Constructor for Syslogd.</p>
     */
    public Syslogd() {
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {

        try {
            LOG.debug("start: Initializing the syslogd config factory");
            SyslogdConfigFactory.init();
        } catch (MarshalException e) {
            LOG.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            LOG.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            LOG.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        }

        try {
            // clear out the known nodes
            SyslogdIPMgr.dataSourceSync();
        } catch (SQLException e) {
            LOG.error("Failed to load known IP address list", e);
            throw new UndeclaredThrowableException(e);
        }

        SyslogHandler.setSyslogConfig(SyslogdConfigFactory.getInstance());
        LOG.debug("Starting SyslogProcessor");

        m_udpEventReceiver = new SyslogHandler();

    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        m_udpEventReceiver.start();

        // // start the event reader
        // The Node list is update with new suspects
        // Also this enables the syslogd to act as
        // trapd and see New suspects.

        try {
            new BroadcastEventProcessor();
        } catch (Throwable ex) {
            LOG.error("Failed to setup event reader", ex);
            throw new UndeclaredThrowableException(ex);
        }
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        // shutdown and wait on the background processing thread to exit.
        LOG.debug("exit: closing communication paths.");

        try {
            LOG.debug("stop: Closing SYSLOGD message session.");

            LOG.debug("stop: Syslog message session closed.");
        } catch (IllegalStateException e) {
            LOG.debug("stop: The Syslog session was already closed");
        }

        LOG.debug("stop: Stopping queue processor.");

        m_udpEventReceiver.stop();
        LOG.debug("Stopped the Syslog UDP Receiver");
    }

    /**
     * Returns the singular instance of the syslogd daemon. There can be only
     * one instance of this service per virtual machine.
     *
     * @return Singleton
     */
    public static Syslogd getInstance() {
        return m_singleton;
    }

    /*
    * @return EventDao
     */
    /**
     * <p>getEventDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.EventDao} object.
     */
    public EventDao getEventDao() {
        return m_eventDao;
    }

    /**
     * <p>setEventDao</p>
     *
     * @param eventDao a {@link org.opennms.netmgt.dao.api.EventDao} object.
     */
    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }
}
