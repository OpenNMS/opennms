//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
// Tab Size = 8
//
//

package org.opennms.netmgt.discovery;

import java.lang.*;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.MissingResourceException;

import java.sql.SQLException;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
final class IPGenerator
	implements FifoQueue
{
	/**
	 * The specific list items to be iterated through
	 */
	private List		m_specificAddresses;

	/**
	 * The range of items to be enumerated through
	 */
	private List		m_includeRanges;

	/**
	 * The iterator used to cycle through the ranges.
	 */
	private Iterator	m_iter;

	/**
	 * Set true until the initial wait time is over.
	 */
	private boolean		m_isInitial;

	/**
	 * The system time when this instance was created.
	 */
	private long		m_createTime;

	/**
	 * Set to true if the end of the iterators has
	 * been reached and restarted.
	 */
	private boolean		m_isRestarted;

	/**
	 * The time the end of the iteration was reached
	 * in system milliseconds.
	 */
	private long		m_restartTime;

	/**
	 * The initial wait time.
	 */
	private long		m_initialWait;

	/**
	 * The time to wait between restarts.
	 */
	private long		m_restartWait;

	/**
	 * This class is used to chain a set of common iterators together
	 * so that when on iterator is exhausted the next one is polled.
	 * This allows a set of iterators to be treated as a single iterator.
	 *
	 * @author <a href="mailto:weave@opennms.org">Brian Weaver</a>
	 * @author <a href="http://www.opennms.org/">OpenNMS</a>
	 *
	 */
	static final class IteratorChain
		implements Iterator
	{
		/**
		 * The chained list of iterators.
		 */
		private List		m_iterators;

		/** 
		 * Constructs a new iterator chain
		 */
		IteratorChain()
		{
			m_iterators = new LinkedList();
		}

		/**
		 * Adds an iterator to the chain
		 *
		 * @param iter	The iterator to add.
		 */
		void add(Iterator iter)
		{
			m_iterators.add(iter);
		}

		/**
		 * This method returns true if an iterator in 
		 * the chain has a next element.
		 *
		 * @return True if there is a next element.
		 */
		public boolean hasNext()
		{
			boolean rc = false;
			while(m_iterators.size() > 0 && !rc)
			{
				Iterator i = (Iterator)m_iterators.get(0);
				rc = i.hasNext();
				if(!rc)
					m_iterators.remove(0);
			}
			return rc;
		}

		/**
		 * This method returns the next element in the 
		 * iterator chain. If there are no elements remaining
		 * then an exception is generated.
		 *
		 * @return The next object in the chain.
		 *
		 * @throws java.util.NoSuchElementException Thrown if there
		 * 	are no more elements.
		 *
		 */
		public Object next()
		{
			Object obj = null;
			while(m_iterators.size() > 0 && obj == null)
			{
				Iterator i = (Iterator)m_iterators.get(0);
				if(i.hasNext())
					obj = i.next();
				else
					m_iterators.remove(0);
			}

			if(obj == null)
				throw new NoSuchElementException("No elements remain in the iterator chain");

			return obj;
		}

		/**
		 * Removes the current element from the iterator. This
		 * method is not supported and always throws the exception
		 * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}.
		 *
		 * @throws java.lang.UnsupportedOperationException Always Thrown.
		 */
		public void remove()
		{
			throw new UnsupportedOperationException("The remove method is not supported");
		}

	} // end class IteratorChain

	/**
	 * Synchronizes the Discovered IP Manager with the database.
	 */
	private void sync()
	{
		try
		{
			DiscoveredIPMgr.dataSourceSync();
		}
		catch(SQLException sqlE)
		{
			ThreadCategory.getInstance(getClass()).error("Failed to synchronize discovered addresses with the database", sqlE);
		}
		catch(MissingResourceException fnfE)
		{
			ThreadCategory.getInstance(getClass()).error("Failed to synchronize discovered addresses with the database", fnfE);
		}
	}

	/**
	 * This method is used to block a thread the generator
	 * is in a timed wait. If there is no timed wait then
	 * the thread returns immediantly. If the lock is not
	 * released in the time passed to the method then a 
	 * false value is returned.
	 *
	 * @param maxWait	The maximum time to wait, or zero for indefinite.
	 *
	 * @return True if the wait was successful.
	 *
	 */
	private synchronized boolean doWait(long maxWait)
		throws InterruptedException
	{
		boolean completed = true;
		if(m_isInitial)
		{
			Category log = ThreadCategory.getInstance(getClass());
			completed = false;

			long start = System.currentTimeMillis();
			if(log.isDebugEnabled())
				log.debug("doWait(initial): entry time " + start);

			if((start - m_createTime) >= m_initialWait)
			{
				// Pre Check completion
				//
				if(log.isDebugEnabled())
					log.debug("doWait(initial): wait over, setting complete flag");

				completed = true;
				if(m_isInitial)
					sync();
				m_isInitial = false;
				notifyAll();
			}
			else
			{
				// find time to wait
				//
				long max_wait = start - m_createTime + m_initialWait;
				if(0 < maxWait && maxWait < max_wait) // user wait specified
				{
					max_wait = maxWait;
				}
				if(log.isDebugEnabled())
					log.debug("doWait(initial): maximum wait = " + max_wait);

				// Wait through notifications
				//
				long finish = start;
				do
				{
					wait(max_wait - (finish - start));
					finish = System.currentTimeMillis();
				} while((finish - start) < max_wait && m_isInitial);

				if(log.isDebugEnabled())
					log.debug("doWait(initial): finished wait loop");

				if((finish - m_createTime) >= m_initialWait)
				{
					if(log.isDebugEnabled())
						log.debug("doWait(initial): wait over, setting complete flag");
					completed = true;
					if(m_isInitial)
						sync();
					m_isInitial = false;
					notifyAll();
				}
			}
		}
		else if(m_isRestarted)
		{
			Category log = ThreadCategory.getInstance(getClass());
			completed = false;
			// Pre Check completion
			//
			long start = System.currentTimeMillis();
			if(log.isDebugEnabled())
				log.debug("doWait(restart): entry time " + start);

			if((start - m_restartTime) >= m_restartWait)
			{
				if(log.isDebugEnabled())
					log.debug("doWait(restart): wait over, setting complete flag");

				completed = true;
				if(m_isRestarted)
					sync();
				m_isRestarted = false;
				notifyAll();
			}
			else
			{
				// find time to wait
				//
				long max_wait = start - m_restartTime + m_restartWait;
				if(0 < maxWait && maxWait < max_wait) // user wait specified
				{
					max_wait = maxWait;
				}
				if(log.isDebugEnabled())
					log.debug("doWait(restart): maximum wait = " + max_wait);

				// Wait through notifications
				//
				long finish = start;
				do
				{
					wait(max_wait - (finish - start));
					finish = System.currentTimeMillis();
				} while((finish - start) < max_wait && m_isRestarted);

				if(log.isDebugEnabled())
					log.debug("doWait(restart): finished wait loop");

				if((finish - m_restartTime) >= m_restartWait)
				{
					if(log.isDebugEnabled())
						log.debug("doWait(restart): wait over, setting complete flag");
					completed = true;
					if(m_isRestarted)
						sync();
					m_isRestarted = false;
					notifyAll();
				}
			}
		}
		return completed;
	}

	/**
	 * Constructs a new instance of the class that can be used
	 * to iterate over the list of encapsulated objects.
	 *
	 * @param specific	The list of specific nodes.
	 * @param ranges	The range list for address generation
	 * @param initialWait	The initial wait time in milliseconds.
	 * @param restartWait	The cycle wait time in milliseconds.
	 *
	 */
	IPGenerator(List specifics, List ranges, long initialWait, long restartWait)
	{
		m_specificAddresses = specifics;
		m_includeRanges = ranges;
		m_iter		= iterator();
		
		m_isInitial	= true;
		m_createTime	= System.currentTimeMillis();
		m_isRestarted	= false;
		m_restartTime	= m_createTime;
		
		m_initialWait	= initialWait;
		m_restartWait	= restartWait;
	}

	/**
	 * Returns an iterator that may be used to step through
	 * all the encapsualted {@link IPPollAddress pollable
	 * addresses} in the generator. Each call to the 
	 * <code>next</code> method on the returned iterator returns
	 * an instance of a {@link IPPollAddress pollable address}.
	 *
	 * @return An iterator to traverse the encapsualted addresses.
	 *
	 */
	Iterator iterator()
	{
		IteratorChain chainedIter = new IteratorChain();

		// Add the specific IPPollAddress iterator
		//
		chainedIter.add(m_specificAddresses.iterator());

		// Add the iterators for the ranges
		//
		Iterator x = m_includeRanges.iterator();
		while(x.hasNext())
		{
			IPPollRange range = (IPPollRange)x.next();
			chainedIter.add(range.iterator());
		}

		return chainedIter;
	}

	/**
	 * This method is used to add an element to the queue. The
	 * method is not implemented in this class and the exception
	 * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}
	 * is always generated.
	 *
	 * @param element	The element to add.
	 *
	 * @throws java.lang.UnsupportedOperationException Always thrown.
	 *
	 */
	public void add(Object element)
		throws InterruptedException, 
			FifoQueueException
	{
		throw new UnsupportedOperationException("Add operation is not supported");
	}

	/**
	 * This method is used to add an element to the queue. The
	 * method is not implemented in this class and the exception
	 * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}
	 * is always generated.
	 *
	 * @param element	The element to add.
	 * @param timeout	The maximum time spent trying to add.
	 *
	 * @returns True if the object is successfully added.
	 *
	 * @throws java.lang.UnsupportedOperationException Always thrown.
	 *
	 */
	public boolean add(Object element, long timeout)
		throws InterruptedException, 
			FifoQueueException
	{
		throw new UnsupportedOperationException("Add operation is not supported");
	}

	/** 
	 * Returns the next element in the internal interator.
	 * If the iterator has reached it's end, then the 
	 * iterator is regenerated and thus this call will
	 * always return a valid element.
	 *
	 * @param timeout	The maximum time to wait (ignored).
	 *
	 * @return The next element in the range(s).
	 */
	public Object remove()
		throws InterruptedException, 
			FifoQueueException
	{
		return remove(0);
	}

	/** 
	 * Returns the next element in the internal interator.
	 * If the iterator has reached it's end, then the 
	 * iterator is regenerated and thus this call will
	 * always return a valid element.
	 *
	 * @param timeout	The maximum time to wait (ignored).
	 *
	 * @return The next element in the range(s).
	 */
	public synchronized Object remove(long timeout)
		throws InterruptedException,
			FifoQueueException
	{
		Category log = ThreadCategory.getInstance(getClass());
		if(!m_iter.hasNext())
		{
			if(log.isDebugEnabled())
				log.debug("remove: end of iterator reached, resetting iterator");

			m_restartTime = System.currentTimeMillis();
			m_isRestarted = true;
			m_iter = iterator();
		}
		doWait(timeout);

		Object rval = null;
		if(m_iter.hasNext())
			rval = m_iter.next();

		return rval;
	}

	/**
	 * Returns the current size of the queue. Since the queue
	 * size is not easily determined, this method will always
	 * return <tt>1</tt>.
	 *
	 * @return The constant value 1.
	 */
	public int size()
	{
		return 1;
	}

	/**
	 * Returns true if the generator is empty. This 
	 * method always returns <tt>true</tt>.
	 *
	 * @return True.
	 */
	public boolean isEmpty()
	{
		return false;
	}

}

