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

package org.opennms.core.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * </p>
 * This interface defines a queue that uses <em>F</em> irst <em>I</em>n,
 * <em>F</em> irst <em>O</em> ut semantics when adding and removing objects.
 * Each object that is added to the queue is effectively placed at the end of
 * the list of previous elements. Each call to <code>remove</code> will result
 * in the removal of the next element, or the oldest element in the queue.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class FifoQueueImpl<T> implements FifoQueue<T> {
    /**
     * The delegate list where queue elements are stored. The elements are
     * removed from the front of the list and added to the end of the list,
     * always!
     */
    protected final BlockingQueue<T> m_delegate;

    /**
     * Constructs a new First In, First Out queue that can be used to exchange
     * data. The implementation is thread safe and can be used to exchange data
     * between to concurrent processes.
     */
    public FifoQueueImpl() {
        m_delegate = new LinkedBlockingQueue<T>();
    }

    /**
     * Inserts a new element into the queue.
     *
     * @param element
     *            The object to append to the queue.
     * @exception org.opennms.core.queue.FifoQueueException
     *                Thrown if a queue error occurs.
     * @exception java.lang.InterruptedException
     *                Thrown if the thread is interrupted.
     * @throws org.opennms.core.queue.FifoQueueException if any.
     * @throws java.lang.InterruptedException if any.
     */
    @Override
    public void add(T element) throws InterruptedException {
        m_delegate.put(element);
    }

    /**
     * {@inheritDoc}
     *
     * Inserts a new element into the queue. If the queue has reached an
     * implementation limit and the <code>
     * timeout</code> expires, then a false
     * value is returned to the caller.
     * 
     * @throws java.lang.InterruptedException
     *                Thrown if the thread is interrupted.
     */
    @Override
    public boolean add(T element, long timeout) throws InterruptedException {
        return m_delegate.offer(element, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Removes the oldest element from the queue.
     *
     * @exception org.opennms.core.queue.FifoQueueException
     *                Thrown if a queue error occurs.
     * @exception java.lang.InterruptedException
     *                Thrown if the thread is interrupted.
     * @return The oldest object in the queue.
     * @throws org.opennms.core.queue.FifoQueueException if any.
     * @throws java.lang.InterruptedException if any.
     */
    @Override
    public T remove() throws InterruptedException {
        return m_delegate.take();
    }

    /**
     * {@inheritDoc}
     *
     * Removes the next element from the queue if one becomes available before
     * the timeout expires. If the timeout expires before an element is
     * available then a <code>null</code> reference is returned to the caller.
     * @exception org.opennms.core.queue.FifoQueueException
     *                Thrown if a queue error occurs.
     * @exception java.lang.InterruptedException
     *                Thrown if the thread is interrupted.
     */
    @Override
    public T remove(long timeout) throws InterruptedException {
        return m_delegate.poll(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the current number of elements that are in the queue.
     *
     * @return The number of elements in the queue.
     */
    @Override
    public int size() {
        return m_delegate.size();
    }

    /**
     * Used to test if the current queue has no stored elements.
     *
     * @return True if the queue is empty.
     */
    @Override
    public boolean isEmpty() {
        return m_delegate.isEmpty();
    }
}
