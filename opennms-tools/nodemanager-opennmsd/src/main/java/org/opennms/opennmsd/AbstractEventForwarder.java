/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.opennmsd;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * DefaultEventForwarder
 *
 * @author brozow
 */
public abstract class AbstractEventForwarder implements EventForwarder, Runnable {
    
    BlockingQueue<NNMEvent> m_queue = new LinkedBlockingQueue<>();
    Thread m_thread;
    
    public AbstractEventForwarder() {
        m_thread = new Thread(this, "EventForwarderThread");
        m_thread.start();
    }
    
    /* (non-Javadoc)
     * @see org.opennms.opennmsd.EventForwarder#accept(org.opennms.opennmsd.NNMEvent)
     */
    public void accept(NNMEvent event) {
        m_queue.offer(event);
    }

    /* (non-Javadoc)
     * @see org.opennms.opennmsd.EventForwarder#discard(org.opennms.opennmsd.NNMEvent)
     */
    public void discard(NNMEvent event) {
        // do nothing when we discard an event
    }

    /* (non-Javadoc)
     * @see org.opennms.opennmsd.EventForwarder#preserve(org.opennms.opennmsd.NNMEvent)
     */
    public void preserve(NNMEvent event) {
        m_queue.offer(event);
    }

    public void run() {
        
        try {

            while(true) {
                NNMEvent event = (NNMEvent)m_queue.take();
                
                List<NNMEvent> events = new LinkedList<>();
                events.add(event);
            
                m_queue.drainTo(events);
            
                forwardEvents(events);
            
            }
        
        } catch (InterruptedException e) {
            // thread interrupted so complete it
        }
        
    }

    protected abstract void forwardEvents(List<NNMEvent> events);

}
