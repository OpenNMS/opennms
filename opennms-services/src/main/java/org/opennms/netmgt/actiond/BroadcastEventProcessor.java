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

package org.opennms.netmgt.actiond;

import java.util.Enumeration;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Autoaction;
import org.opennms.netmgt.xml.event.Event;

/**
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class BroadcastEventProcessor implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);
    /**
     * The location where executable events are enqueued to be executed.
     */
    private final FifoQueue<String> m_execQ;

    /**
     * This constructor subscribes to eventd for all events
     * 
     * @param execQ
     *            The queue where executable events are stored.
     * 
     */
    BroadcastEventProcessor(FifoQueue<String> execQ) {
        // set up the exectuable queue first
        //
        m_execQ = execQ;

        // subscribe for all events
        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this);

    }

    /**
     * Unsubscribe from eventd
     */
    public synchronized void close() {
        EventIpcManagerFactory.getIpcManager().removeEventListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each event's autoactions and trouble tickets
     * are queued to be run
     */
    @Override
    public void onEvent(Event event) {

        if (event == null) {
            return;
        }

        // Handle autoactions
        //
        Enumeration<Autoaction> walker = event.enumerateAutoaction();
        while (walker.hasMoreElements()) {
            try {
                Autoaction aact = walker.nextElement();
                if ("on".equalsIgnoreCase(aact.getState())) {
                    m_execQ.add(aact.getContent()); // java.lang.String
                }

                LOG.debug("Added event \'{}\' to execute autoaction \'{}\'", event.getUei(), aact.getContent());
            } catch (FifoQueueException ex) {
                LOG.error("Failed to add event to execution queue", ex);
                break;
            } catch (InterruptedException ex) {
                LOG.error("Failed to add event to execution queue", ex);
                break;
            }
        }

        // Handle trouble tickets
        //
        if (event.getTticket() != null && event.getTticket().getState().equalsIgnoreCase("on")) {
            try {
                m_execQ.add(event.getTticket().getContent()); // java.lang.String


                LOG.debug("Added event \'{}\' to execute tticket \'{}\'", event.getUei(), event.getTticket().getContent());
            } catch (FifoQueueException ex) {
                LOG.error("Failed to add event to execution queue", ex);
            } catch (InterruptedException ex) {
                LOG.error("Failed to add event to execution queue", ex);
            }
        }

    } // end onMessage()

    /**
     * Return an id for this event listener
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "Actiond:BroadcastEventProcessor";
    }

} // end class
