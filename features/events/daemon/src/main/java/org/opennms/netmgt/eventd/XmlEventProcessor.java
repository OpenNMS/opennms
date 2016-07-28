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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Provides the logic and context of execution necessary to actually process a
 * client's event XML document. When a new stream handler is created and
 * assigned to an execution context it will unmarshal the remote document. The
 * events from the remote document are then passed to the registered event
 * handlers. All successfully processed events are acknowledged to the client by
 * the generation of an XML event receipt.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http;//www.opennms.org">OpenNMS </a>
 * 
 */
public class XmlEventProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(XmlEventProcessor.class);

    private EventForwarder m_eventForwarder;

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    @Override
    public void process(Exchange exchange) {

        final InputStream stream = exchange.getIn().getBody(InputStream.class);

        // Unmarshal the XML document
        Log eventLog = null;
        try {
            eventLog = JaxbUtils.unmarshal(Log.class, new InputSource(stream));
            LOG.debug("Event record converted");
        } catch (final Throwable e) {
            LOG.error("Could not unmarshal the XML record", e);
            // TODO: Throw an exception or something?
            return;
        }

        // Now that we have a list of events, process them
        final Event[] events = eventLog.getEvents().getEvent();

        // process the events
        if (events != null && events.length != 0) {
            // sort the events by time
            Arrays.sort(events, new Comparator<Event>() {
                @Override
                public int compare(final Event e1, final Event e2) {
                    final boolean e1t = (e1.getTime() != null);
                    final boolean e2t = (e2.getTime() != null);
                    if (e1t && !e2t) {
                        return 1;
                    } else if (!e1t && e2t) {
                        return -1;
                    } else if (!e1t && !e2t) {
                        return 0;
                    }

                    Date de1 = e1.getTime();
                    Date de2 = e2.getTime();

                    if (de1 != null && de2 != null) {
                        return (int) (de1.getTime() - de2.getTime());
                    } else if (de1 == null && de2 != null) {
                        return -1;
                    } else if (de1 != null && de2 == null) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            final List<Event> okEvents = new ArrayList<Event>(events.length);

            /*
             * Get the handler and then have it process all
             * the events in the document before moving to the
             * next event handler.
             */
            for (final Event event : events) {
                /*
                 * Process the event and log any errors,
                 * but don't die on these errors
                 */
                try {
                    LOG.debug("handling event: {}", event);

                    m_eventForwarder.sendNow(event);
                    if (!okEvents.contains(event)) {
                        okEvents.add(event);
                    }
                } catch (final Throwable t) {
                    LOG.warn("An exception occured while processing an event.", t);
                }
            }

            // Now process the good events and send a receipt message
            boolean hasReceipt = false;
            final EventReceipt receipt = new EventReceipt();
            
            for (final Event event : okEvents) {
                if (event.getUuid() != null) {
                    receipt.addUuid(event.getUuid());
                    hasReceipt = true;
                }
            }

            if (hasReceipt) {
                // Transform it to XML and send it to the socket in one call
                final StringWriter writer = new StringWriter();
                JaxbUtils.marshal(receipt, writer);
                writer.flush();
                exchange.getOut().setBody(writer.toString());

                if (LOG.isDebugEnabled()) {
                    try {
                        LOG.debug("Sent Event Receipt {");
                        LOG.debug(writer.toString());
                        LOG.debug("}");
                    } catch (final Throwable e) {
                        LOG.error("An error occured during marshalling of event receipt for the log.", e);
                    }
                }
            }
        } else {
            LOG.debug("The agent sent an empty event stream.");
        }
    }
}
