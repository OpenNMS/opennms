/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.eventd.listener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

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
public class XmlEventProcessor extends ChannelInboundHandlerAdapter {

    // sort the events by time
    private static final Comparator<Event> EVENT_COMPARATOR = (e1, e2) -> {
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
    };

    private static final Logger LOG = LoggerFactory.getLogger(XmlEventProcessor.class);

    private final EventForwarder eventForwarder;

    public XmlEventProcessor(EventForwarder eventForwarder) {
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (!(msg instanceof ByteBuf)) {
                LOG.warn("Expected message to be of type {} but received {}. Ignoring message.", ByteBuf.class, msg == null ? null : msg.getClass());
                return;
            }
            final ByteBuf buffer = (ByteBuf) msg;
            final Log eventLog = getEventLog(buffer);
            if (eventLog.getEvents() != null
                    && eventLog.getEvents().getEvent() != null
                    && eventLog.getEvents().getEvent().length != 0) {
                final Optional<String> receipt = process(eventLog);
                if (receipt.isPresent()) {
                    ctx.write(receipt.get());
                    LOG.debug("Sent Event Receipt: {}", receipt.get());
                }
            } else {
                LOG.debug("The agent sent an empty event stream.");
            }
        } finally {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush(); // Flush afterwards, also "releases" the ByteBuf
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("An error occurred while processing the event: {}", cause.getMessage(), cause);
        // We do not close the channel here, as we want to serve future requests
    }

    private Optional<String> process(Log eventLog) {
        // Now that we have a list of events, process them
        final Event[] events = eventLog.getEvents().getEvent();

        // process the events
        Arrays.sort(events, EVENT_COMPARATOR);

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
                eventForwarder.sendNow(event);
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
            return Optional.of(writer.toString());
        }
        return Optional.empty();
    }

    private static Log getEventLog(ByteBuf buf) throws IOException {
        final String xml = buf.toString(CharsetUtil.UTF_8);
        try (ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes())) {
            final Log eventLog = JaxbUtils.unmarshal(Log.class, input);
            LOG.debug("Event record converted");
            return eventLog;
        } catch (Throwable e) {
            LOG.error("Could not unmarshal the XML record", e);
            throw e;
        }
    }
}