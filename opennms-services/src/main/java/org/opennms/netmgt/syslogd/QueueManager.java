/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;

 /**
  * <p>QueueManager class.</p>
  *
  * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
  * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
  * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
  * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
  */
 public class QueueManager {

    FifoQueue<ConvertToEvent> m_backlogQ = new FifoQueueImpl<ConvertToEvent>();

    ConvertToEvent ret;

    /**
     * <p>putInQueue</p>
     *
     * @param re a {@link org.opennms.netmgt.syslogd.ConvertToEvent} object.
     */
    public synchronized void putInQueue(ConvertToEvent re) {
        // This synchronized method places a message in the queue
        // Category log = ThreadCategory.getInstance(this.getClass());

        ret = re;

        try {
            m_backlogQ.add(ret);

        } catch (FifoQueueException e) {
            // log.debug("Caught an exception adding to queue");
        } catch (InterruptedException e) {
            // Error handling by ignoring the problem.
        }
        // wake up getByteFromQueue() if it has invoked wait().
        notify();
    }// end method putByteInQueue()

    // -----------------------------------------------------//

    /**
     * <p>getFromQueue</p>
     *
     * @return a {@link org.opennms.netmgt.syslogd.ConvertToEvent} object.
     */
    public synchronized ConvertToEvent getFromQueue() {
        // This synchronized method removes a message from the queue
        ThreadCategory log = ThreadCategory.getInstance(this.getClass());

        try {
            while (m_backlogQ.isEmpty()) {
                wait();
            }// end while
        } catch (InterruptedException E) {
            log.info("InterruptedException: " + E, E);
        }// end catch block

        // get the byte from the queue

        try {
            ret = m_backlogQ.remove();
        } catch (FifoQueueException e) {
            log.debug("FifoQueue exception " + e);
        } catch (InterruptedException e) {
            log.debug("Interrupted exception " + e);
        }

        // wake up putByteInQueue() if it has invoked wait().
        notify();
        return ret;
    }// end getByteFromQueue()

}
