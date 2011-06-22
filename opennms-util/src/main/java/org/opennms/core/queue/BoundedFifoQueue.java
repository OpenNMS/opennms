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
 * by the Free Software Foundation, either version 2 of the License,
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
 * This interface is used to define a queue with <em>First
 * In, First Out</em>
 * semantics that has a maximum input size. The base interface class defines the
 * methods for preforming the first in, first out queue algorithm. This
 * interface is used to mark an implementation as having a maximum size in the
 * number of elements that can be added to the queue.
 * </p>
 *
 * <p>
 * Once the maximum size is reached it is up to the implementation to determine
 * if it should block the adding thread, or generate an exception.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public interface BoundedFifoQueue<T> extends FifoQueue<T> {
    /**
     * Returns the maximum number of elements that can be contained in the FIFO
     * queue.
     *
     * @return The maximum number of elements storable in the queue.
     */
    public int maxSize();

    /**
     * Returns true if the queue has reached the maximum number of elements it
     * can hold.
     *
     * @return True if the queue is at maximum capacity.
     */
    public boolean isFull();
}
