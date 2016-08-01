/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import java.util.Objects;

import org.apache.camel.InOnly;
import org.apache.camel.Produce;
import org.opennms.core.camel.DefaultDispatcher;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

/**
 * An implementation of the {@link EventIpcBroadcaster} interface that can be used to
 * communicate between services in the same JVM.
 *
 * This class is an {@link InOnly} endpoint that will send messages to the 
 * Camel endpoint specified by the <code>endpointUri</code> constructor argument.
 * 
 * @author Seth
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class EventIpcBroadcasterDefaultImpl extends DefaultDispatcher implements EventIpcBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(EventIpcBroadcasterDefaultImpl.class);

    private final Timer logBroadcastTimer;
    private final Meter eventBroadcastMeter;

    @Produce(property="endpointUri")
    EventIpcBroadcaster m_proxy;

    public EventIpcBroadcasterDefaultImpl(final String endpointUri, final MetricRegistry registry) {
        super(endpointUri);
        logBroadcastTimer = Objects.requireNonNull(registry).timer("eventlogs.process.broadcast");
        eventBroadcastMeter = registry.meter("events.process.broadcast");
    }

    /**
     * Send the incoming {@link Event} message into the Camel route
     * specified by the {@link #m_endpointUri} property.
     * 
     * TODO: Change this method to handle {@link Log} messages.
     */
    @Override
    public void broadcastNow(Event event) {
        LOG.debug("Event ID {} to be broadcasted: {}", event.getDbid(), event.getUei());

        if (event.getLogmsg() != null && event.getLogmsg().getDest().equals("suppress")) {
            LOG.debug("process: skip sending event {} to other daemons because is marked as suppress", event.getUei());
        } else {
            try (Context context = logBroadcastTimer.time()) {
                m_proxy.broadcastNow(event);
                eventBroadcastMeter.mark();
            }
        }
    }
}
