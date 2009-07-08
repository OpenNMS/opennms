/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.eventd;


import java.util.Collection;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.adaptors.EventReceiver;
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
    /**
     * The log4j category used to log debug messsages and statements.
     */
    public static final String LOG4J_CATEGORY = "OpenNMS.Eventd";

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
        super("OpenNMS.Eventd");
    }

    protected void onInit() {
        Assert.state(m_eventdServiceManager != null, "property eventdServiceManager must be set");
        Assert.state(m_eventReceivers != null, "property eventReceivers must be set");
        Assert.state(m_receiver != null, "property receiver must be set");
        
        m_eventdServiceManager.dataSourceSync();
    }

    protected void onStart() {
        for (EventReceiver eventReceiver : m_eventReceivers) {
            eventReceiver.start();
        }
        
        log().debug("Listener threads started");

        log().debug("Eventd running");
    }

    protected void onStop() {
        log().debug("calling shutdown on tcp/udp listener threads");

        // Stop listener threads
        for (EventReceiver eventReceiver : m_eventReceivers) {
            eventReceiver.stop();
        }

        if (m_receiver != null) {
            m_receiver.close();
        }

        log().debug("shutdown on tcp/udp listener threads returned");
    }

    public EventdServiceManager getEventdServiceManager() {
        return m_eventdServiceManager;
    }

    public void setEventdServiceManager(EventdServiceManager eventdServiceManager) {
        m_eventdServiceManager = eventdServiceManager;
    }

    public BroadcastEventProcessor getReceiver() {
        return m_receiver;
    }

    public void setReceiver(BroadcastEventProcessor receiver) {
        m_receiver = receiver;
    }

    public Collection<EventReceiver> getEventReceivers() {
        return m_eventReceivers;
    }

    public void setEventReceivers(Collection<EventReceiver> eventReceivers) {
        m_eventReceivers = eventReceivers;
    }
}
