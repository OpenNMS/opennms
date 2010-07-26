//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Aug 22: Added the ScriptD code.
//
// Copyright (C) 2003 Tavve Software Company.  All rights reserved.
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

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
    public String getName() {
        return "Scriptd:BroadcastEventProcessor";
    }

} // end class
