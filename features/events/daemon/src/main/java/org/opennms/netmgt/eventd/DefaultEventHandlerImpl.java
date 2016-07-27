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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.camel.Produce;
import org.opennms.core.camel.DefaultDispatcher;
import org.opennms.netmgt.events.api.EventHandler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * The EventHandler builds Runnables that essentially do all the work on an
 * incoming event.
 *
 * Operations done on an incoming event are handled by the List of injected
 * EventProcessors, in the order in which they are given in the list.  If any
 * of them throw an exception, further processing of that event Log is stopped.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class DefaultEventHandlerImpl extends DefaultDispatcher implements EventHandler {

    @Produce(property="endpointUri")
    EventHandler m_proxy;

    private final Timer processTimer;

    private final Histogram logSizes;

    /**
     * <p>Constructor for DefaultEventHandlerImpl.</p>
     */
    public DefaultEventHandlerImpl(final String endpointUri, final MetricRegistry registry) {
        super(endpointUri);
        processTimer = Objects.requireNonNull(registry).timer("eventlogs.process");
        logSizes = registry.histogram("eventlogs.sizes");
    }

    /**
     * Process the received events. For each event, use the EventExpander to
     * look up matching eventconf entry and load info from that match, expand
     * event parms, add event to database and send event to appropriate
     * listeners.
     */
    @Override
    public void handle(Log m_eventLog) {
        Events events = m_eventLog.getEvents();
        if (events == null || events.getEventCount() <= 0) {
            // no events to process
            return;
        }

        logSizes.update(events.getEventCount());
        try (Timer.Context context = processTimer.time()) {
            m_proxy.handle(m_eventLog);
        }
    }

    public static List<String> getPrettyParms(final Event event) {
        final List<String> parms = new ArrayList<>();
        for (final Parm p : event.getParmCollection()) {
            parms.add(p.getParmName() + "=" + p.getValue().getContent());
        }
        return parms;
    }
}
