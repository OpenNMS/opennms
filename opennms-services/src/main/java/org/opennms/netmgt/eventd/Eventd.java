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

package org.opennms.netmgt.eventd;


import java.util.Collection;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.EventdServiceManager;
import org.opennms.netmgt.eventd.adaptors.EventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * <p>
 * Eventd listens for events from the discovery, capsd, trapd processes and
 * sends events to the Master Station when queried for.
 * </p>
 *
 * <p>
 * Eventd receives events sent in as XML, looks up the event.conf and adds
 * information to these events and stores them to the db. It also reconverts
 * them back to XML to be sent to other processes like 'actiond'
 * </p>
 *
 * <p>
 * Process like trapd, capsd etc. that are local to the distributed poller send
 * events to the eventd. Events can also be sent via TCP or UDP to eventd.
 * </p>
 *
 * <p>
 * Eventd listens for incoming events, loads info from the 'event.conf', adds
 * events to the database and sends the events added to the database to
 * subscribed listeners. It also maintains a servicename to serviceid mapping
 * from the services table so as to prevent a database lookup for each incoming
 * event
 * </P>
 *
 * <P>
 * The number of threads that processes events is configurable via the eventd
 * configuration xml
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public final class Eventd extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Eventd.class);
    
    /**
     * The log4j category used to log debug messsages and statements.
     */
    public static final String LOG4J_CATEGORY = "eventd";

    /**
     * Reference to the event processor
     */
    private BroadcastEventProcessor m_receiver;

    /**
     * Class that handles mapping of service names to service IDs.
     */
    private EventdServiceManager m_eventdServiceManager;

    /**
     * All handlers that can receive events to be started/stopped with Eventd.
     */
    private Collection<EventReceiver> m_eventReceivers;

    /**
     * Constuctor creates the localhost address(to be used eventually when
     * eventd originates events during correlation) and the broadcast queue
     */
    public Eventd() {
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        Assert.state(m_eventdServiceManager != null, "property eventdServiceManager must be set");
        Assert.state(m_eventReceivers != null, "property eventReceivers must be set");
        Assert.state(m_receiver != null, "property receiver must be set");
        
        m_eventdServiceManager.dataSourceSync();
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        for (EventReceiver eventReceiver : m_eventReceivers) {
            eventReceiver.start();
        }
        
        LOG.debug("Listener threads started");

        LOG.debug("Eventd running");
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        LOG.debug("calling shutdown on tcp/udp listener threads");

        // Stop listener threads
        for (EventReceiver eventReceiver : m_eventReceivers) {
            eventReceiver.stop();
        }

        if (m_receiver != null) {
            m_receiver.close();
        }

        LOG.debug("shutdown on tcp/udp listener threads returned");
    }

    /**
     * <p>getEventdServiceManager</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.EventdServiceManager} object.
     */
    public EventdServiceManager getEventdServiceManager() {
        return m_eventdServiceManager;
    }

    /**
     * <p>setEventdServiceManager</p>
     *
     * @param eventdServiceManager a {@link org.opennms.netmgt.dao.api.EventdServiceManager} object.
     */
    public void setEventdServiceManager(EventdServiceManager eventdServiceManager) {
        m_eventdServiceManager = eventdServiceManager;
    }

    /**
     * <p>getReceiver</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.BroadcastEventProcessor} object.
     */
    public BroadcastEventProcessor getReceiver() {
        return m_receiver;
    }

    /**
     * <p>setReceiver</p>
     *
     * @param receiver a {@link org.opennms.netmgt.eventd.BroadcastEventProcessor} object.
     */
    public void setReceiver(BroadcastEventProcessor receiver) {
        m_receiver = receiver;
    }

    /**
     * <p>getEventReceivers</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<EventReceiver> getEventReceivers() {
        return m_eventReceivers;
    }

    /**
     * <p>setEventReceivers</p>
     *
     * @param eventReceivers a {@link java.util.Collection} object.
     */
    public void setEventReceivers(Collection<EventReceiver> eventReceivers) {
        m_eventReceivers = eventReceivers;
    }
}
