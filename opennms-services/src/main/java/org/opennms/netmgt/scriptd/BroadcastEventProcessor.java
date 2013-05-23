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

package org.opennms.netmgt.scriptd;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;

/**
 * This class receives all events on behalf of the <em>Scriptd</em> service.
 * All events are placed on a queue, so they can be handled by the "Executor"
 * (this allows the Executor to pause and resume without losing events).
 * 
 * @author <a href="mailto:jim.doble@tavve.com">Jim Doble </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class BroadcastEventProcessor implements EventListener {
    /**
     * The location where executable events are enqueued to be executed.
     */
    private final FifoQueue<Event> m_execQ;

    /**
     * This constructor subscribes to eventd for all events
     * 
     * @param execQ
     *            The queue where executable events are stored.
     * 
     */
    BroadcastEventProcessor(FifoQueue<Event> execQ) {
        // set up the executable queue first

        m_execQ = execQ;

        // subscribe for all events

        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(this);
    }

    /**
     * Close the BroadcastEventProcessor
     */
    public synchronized void close() {
        // unsubscribe all events

        EventIpcManagerFactory.getIpcManager().removeEventListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing. Each event is queued for handling by the
     * Executor.
     */
    @Override
    public void onEvent(Event event) {
        if (event == null) {
            return;
        }

        ThreadCategory log = ThreadCategory.getInstance(BroadcastEventProcessor.class);

        try {
            m_execQ.add(event);

            if (log.isDebugEnabled()) {
                log.debug("Added event \'" + event.getUei() + "\' to scriptd execution queue.");
            }
        }

        catch (FifoQueueException ex) {
            log.error("Failed to add event to scriptd execution queue", ex);
        } catch (InterruptedException ex) {
            log.error("Failed to add event to scriptd execution queue", ex);
        }

    } // end onEvent()

    /**
     * Return an id for this event listener
     *
     * @return The ID of this event listener.
     */
    @Override
    public String getName() {
        return "Scriptd:BroadcastEventProcessor";
    }

} // end class
