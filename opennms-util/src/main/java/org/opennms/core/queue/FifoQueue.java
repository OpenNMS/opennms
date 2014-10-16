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

package org.opennms.core.queue;

/**
 * <p>
 * This interface defines a queue that uses <em>F</em> irst <em>I</em>n,
 * <em>F</em> irst <em>O</em> ut semantics when adding and removing objects.
 * Each object that is added to the queue is effectively placed at the end of
 * the list of previous elements. Each call to <code>remove</code> will result
 * in the removal of the next element, or the oldest element in the queue.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public interface FifoQueue<T> {
    /**
     * Inserts a new element into the queue.
     *
     * @param element
     *            The object to append to the queue.
     * @throws org.opennms.core.queue.FifoQueueException if any.
     * @throws java.lang.InterruptedException if any.
     */
    public void add(T element) throws FifoQueueException, InterruptedException;

    /**
     * Inserts a new element into the queue. If the queue has reached an
     * implementation limit and the <code>timeout</code> expires, then a false
     * value is returned to the caller.
     *
     * @param element
     *            The object to append to the queue.
     * @param timeout
     *            The time to wait on the insertion to succeed.
     * @return True if the element was successfully added to the queue before
     *         the timeout expired, false otherwise.
     * @throws org.opennms.core.queue.FifoQueueException if any.
     * @throws java.lang.InterruptedException if any.
     */
    public boolean add(T element, long timeout) throws FifoQueueException, InterruptedException;

    /**
     * Removes the oldest element from the queue.
     *
     * @return The oldest object in the queue.
     * @throws org.opennms.core.queue.FifoQueueException if any.
     * @throws java.lang.InterruptedException if any.
     */
    public T remove() throws FifoQueueException, InterruptedException;

    /**
     * Removes the next element from the queue if one becomes available before
     * the timeout expires. If the timeout expires before an element is
     * available then a <code>null</code> reference is returned to the caller.
     *
     * @param timeout
     *            The time to wait on an object to be available.
     * @return The oldest object in the queue, or <code>null</code> if one is
     *         not available.
     * @throws org.opennms.core.queue.FifoQueueException if any.
     * @throws java.lang.InterruptedException if any.
     */
    public T remove(long timeout) throws FifoQueueException, InterruptedException;

    /**
     * Returns the current number of elements that are in the queue.
     *
     * @return The number of elements in the queue.
     */
    public int size();

    /**
     * Used to test if the current queue has no stored elements.
     *
     * @return True if the queue is empty.
     */
    public boolean isEmpty();
}
