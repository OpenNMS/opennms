//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
// 
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.core.queue;

import java.util.LinkedList;

/** 
 * </p>This interface defines a queue that uses <em>F</em>irst
 * <em>I</em>n, <em>F</em>irst <em>O</em>ut semantics when
 * adding and removing objects. Each object that is added to 
 * the queue is effectively placed at the end of the list of
 * previous elements. Each call to <code>remove</code> will
 * result in the removal of the next element, or the oldest
 * element in the queue.</p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 *
 */
public class FifoQueueImpl
	implements FifoQueue
{
	/**
	 * The delegate list where queue elements are stored. The
	 * elements are removed from the front of the list and 
	 * added to the end of the list, always!
	 */
	private LinkedList	m_delegate;

	/**
	 * Constructs a new First In, First Out queue that
	 * can be used to exchange data. The implementation
	 * is thread safe and can be used to exchange data
	 * between to concurrent processes.
	 */
	public FifoQueueImpl()
	{
		m_delegate = new LinkedList();
	}

	/**
	 * Inserts a new element into the queue. 
	 *
	 * @param element	The object to append to the queue.
	 *
	 * @exception org.opennms.core.queue.FifoQueueException Thrown if
	 * 	a queue error occurs.
	 * @exception java.lang.InterruptedException Thrown if the thread
	 * 	is interrupted.
	 */
	public void add(Object element)
		throws 	FifoQueueException,
			InterruptedException
	{
		synchronized(m_delegate)
		{
			m_delegate.addLast(element);
			m_delegate.notifyAll();
		}
	}
	
	/**
	 * Inserts a new element into the queue. If the queue
	 * has reached an implementation limit and the <code>
	 * timeout</code> expires, then a false value is returned
	 * to the caller.
	 *
	 * @param element	The object to append to the queue.
	 * @param timeout	The time to wait on the insertion to succeed.
	 *
	 * @exception org.opennms.core.queue.FifoQueueException Thrown if
	 * 	a queue error occurs.
	 * @exception java.lang.InterruptedException Thrown if the thread
	 * 	is interrupted.
	 *
	 * @return True if the element was successfully added to the queue
	 *	before the timeout expired, false otherwise.
	 */
	public boolean add(Object element, long timeout)
		throws	FifoQueueException,
			InterruptedException
	{
		synchronized(m_delegate)
		{
			m_delegate.addLast(element);
			m_delegate.notifyAll();
		}
		return true;
	}

	/**
	 * Removes the oldest element from the queue.
	 *
	 * @exception org.opennms.core.queue.FifoQueueException Thrown if
	 * 	a queue error occurs.
	 * @exception java.lang.InterruptedException Thrown if the thread
	 * 	is interrupted.
	 *
	 * @return The oldest object in the queue.
	 */
	public Object remove()
		throws	FifoQueueException,
			InterruptedException
	{
		synchronized(m_delegate)
		{
			while(m_delegate.isEmpty())
			{
				m_delegate.wait();
			}
			return m_delegate.removeFirst();
		}
	}
	
	/**
	 * Removes the next element from the queue if one
	 * becomes available before the timeout expires. If
	 * the timeout expires before an element is available
	 * then a <code>null</code> reference is returned to
	 * the caller.
	 *
	 * @param timeout	The time to wait on an object to be available.
	 *
	 * @exception org.opennms.core.queue.FifoQueueException Thrown if
	 * 	a queue error occurs.
	 * @exception java.lang.InterruptedException Thrown if the thread
	 * 	is interrupted.
	 *
	 * @return The oldest object in the queue, or <code>null</code> if 
	 * 	one is not available.
	 */
	public Object remove(long timeout)
		throws	FifoQueueException,
			InterruptedException
	{
		Object rval = null;
		synchronized(m_delegate)
		{
			if(m_delegate.isEmpty())
			{
				long start = System.currentTimeMillis();
				long diff = 0;
				do
				{
					m_delegate.wait(timeout - diff);
					diff = System.currentTimeMillis() - start;
				} while(diff < timeout && m_delegate.isEmpty());
			}

			if(!m_delegate.isEmpty())
				rval = m_delegate.removeFirst();
		}

		return rval;
	}
	
	/**
	 * Returns the current number of elements that are
	 * in the queue.
	 *
	 * @return The number of elements in the queue.
	 */
	public int size()
	{
		synchronized(m_delegate)
		{
			return m_delegate.size();
		}
	}

	/**
	 * Used to test if the current queue has no stored
	 * elements.
	 *
	 * @return True if the queue is empty.
	 */
	public boolean isEmpty()
	{
		synchronized(m_delegate)
		{
			return m_delegate.isEmpty();
		}
	}
}
