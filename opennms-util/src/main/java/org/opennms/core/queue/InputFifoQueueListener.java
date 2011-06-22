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

package org.opennms.core.queue;

/**
 * <p>
 * This interface is implemented by objects that need to be notified when a new
 * element is added to a queue. The notification method is invoked aftet the
 * element is added to the queue, the exact semantics of which are defined by
 * the queue.
 * </p>
 *
 * <p>
 * This is most often used to notify a class that an empty queue has new
 * elements that need to be processed. This allows the object to perform other
 * potentially useful work while waiting on new queue elements.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public interface InputFifoQueueListener<T> {
    /**
     * This method is invoked by a queue implementation when a new element is
     * added its queue. The exact instance when the method is invoked is
     * dependent upon the implementation.
     *
     * @param queue
     *            The queue where the element was added.
     */
    public void onQueueInput(NotifiableInputFifoQueue<T> queue);
}
