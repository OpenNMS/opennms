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
// 2007 May 20: Use Java 5 generics. - dj@opennms.org
// 
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
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
//
// Tab Size = 8
//
//

package org.opennms.core.queue;

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
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public interface FifoQueue<T> {
    /**
     * Inserts a new element into the queue.
     *
     * @param element
     *            The object to append to the queue.
     * @exception org.opennms.core.queue.FifoQueueException
     *                Thrown if a queue error occurs.
     * @exception java.lang.InterruptedException
     *                Thrown if the thread is interrupted.
     * @param <T> a T object.
     * @throws org.opennms.core.queue.FifoQueueException if any.
     * @throws java.lang.InterruptedException if any.
     */
    public void add(T element) throws FifoQueueException, InterruptedException;

    /**
     * Inserts a new element into the queue. If the queue has reached an
     * implementation limit and the <code>
     * timeout</code> expires, then a false
     * value is returned to the caller.
     *
     * @param element
     *            The object to append to the queue.
     * @param timeout
     *            The time to wait on the insertion to succeed.
     * @exception org.opennms.core.queue.FifoQueueException
     *                Thrown if a queue error occurs.
     * @exception java.lang.InterruptedException
     *                Thrown if the thread is interrupted.
     * @exception java.lang.UnsupportedOperationException
     *                Thrown if the method is not supported.
     * @return True if the element was successfully added to the queue before
     *         the timeout expired, false otherwise.
     * @throws org.opennms.core.queue.FifoQueueException if any.
     * @throws java.lang.InterruptedException if any.
     */
    public boolean add(T element, long timeout) throws FifoQueueException, InterruptedException;

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
    public T remove() throws FifoQueueException, InterruptedException;

    /**
     * Removes the next element from the queue if one becomes available before
     * the timeout expires. If the timeout expires before an element is
     * available then a <code>null</code> reference is returned to the caller.
     *
     * @param timeout
     *            The time to wait on an object to be available.
     * @exception org.opennms.core.queue.FifoQueueException
     *                Thrown if a queue error occurs.
     * @exception java.lang.InterruptedException
     *                Thrown if the thread is interrupted.
     * @exception java.lang.UnsupportedOperationException
     *                Thrown if the method is not supported.
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
