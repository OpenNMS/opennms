/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * Modifications:
 * 
 * 2007 May 21: Use java 5 generics, loops, and format code. - dj@opennms.org
 * 
 * Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.core.concurrent;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.queue.ClosableFifoQueue;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueClosedException;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.util.Assert;

/**
 * <p>RunnableConsumerThreadPool class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class RunnableConsumerThreadPool extends Object implements Fiber {
    /**
     * The queue where runnable objects are added.
     */
    private SizingFifoQueue<Runnable> m_delegateQ;

    /**
     * The list of running fibers in the pool. The list allows the size of the
     * pool to vary.
     */
    private Fiber[] m_fibers;

    /**
     * The name of the pool.
     */
    private String m_poolName;

    /**
     * The high water mark ratio for the pool.
     */
    private float m_hiRatio;

    /**
     * The low water mark ratio for the pool.
     */
    private float m_loRatio;

    /**
     * The maximum size for the thread pool.
     */
    private int m_maxSize;

    /**
     * The log4j prefix used when starting up a new fiber!
     */
    private String m_log4jPrefix;

    /**
     * The pool status
     */
    private int m_poolStatus;

    /**
     * The set of listeners to call when a Runnable completes successfully.
     */
    private List<RunnableCompletionListener> m_completedListeners;

    /**
     * The thread group that all pool threads belong to.
     */
    private ThreadGroup m_tGroup;

    /**
     * The set of listeners to call when a Runnable fails to complete
     * successfully.
     */
    private List<RunnableErrorListener> m_errorListeners;

    /**
     * <p>
     * This class is used to create a queue that auto adjust the number of
     * threads in the pool. Each time an addition or removal of queue elements
     * occur the pool will be adjusted. This will cause some synchronization
     * overhead, but it should be correct implementation.
     * </p>
     * 
     * <p>
     * To avoid the condition of toggleing a single thread the lo and hi water
     * marks should have a large cushion between them.
     * </p>
     * 
     * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
     * @author <a href="http://www.opennms.org/">OpenNMS </a>
     * 
     */
    private class SizingFifoQueue<T> extends FifoQueueImpl<T> implements ClosableFifoQueue<T> {
        /**
         * Determines if the queue is open or closed. If the queue is closed
         * then an exception is thrown on queue additions. Also, queue removals
         * will cause a queue exception if the queue is empty.
         */
        private volatile boolean m_isClosed = false;

        /**
         * Adjust the size of the thread pool based on the ratio of queue
         * elements to threads. The thread pool is adjusted by the lo and hi
         * water marks which are a ratio of elements to threads.
         */
        private void adjust() {
            int e = size();
            synchronized (m_fibers) {
                int alive = livingFiberCount();
                float ratio = (float) e / (float) (alive <= 0 ? 1 : alive);

                // Never stop the last thread!?
                if (alive > 1 && ratio <= m_loRatio) {
                    /*
                     * If:
                     * 1) Fibers greater than one, and...
                     * 2) ratio less than low water mark
                     */
                    Fiber f = null;
                    int last = Fiber.START_PENDING;
                    for (Fiber fiber : m_fibers) {
                        if (fiber != null) {
                            switch (fiber.getStatus()) {
                            case Fiber.RUNNING:
                                if (last < Fiber.RUNNING) {
                                    f = fiber;
                                    last = f.getStatus();
                                }
                                break;

                            case Fiber.STOP_PENDING:
                                if (last < Fiber.STOP_PENDING) {
                                    f = null;
                                    last = Fiber.STOP_PENDING;
                                }
                                break;
                            }
                        }
                    }

                    if (f != null && f.getStatus() != Fiber.STOP_PENDING) {
                        if (log().isDebugEnabled()) {
                            log().debug("adjust: calling stop on fiber " + f.getName());
                        }
                        f.stop();
                    }

                } else if (((alive == 0 && e > 0) || ratio > m_hiRatio) && alive < m_maxSize) {
                    /*
                     * If:
                     * 1a) Fibers equal to zero and queue not empty, or..
                     * 1a) ratio greater than hiRatio, and...
                     * 2) Fibers less than max size
                     */
                    for (int x = 0; x < m_fibers.length; x++) {
                        if (m_fibers[x] == null || m_fibers[x].getStatus() == Fiber.STOPPED) {
                            Fiber f = new FiberThreadImpl(m_poolName + "-fiber" + x);
                            f.start();
                            m_fibers[x] = f;
                            if (log().isDebugEnabled()) {
                                log().debug("adjust: started fiber " + f.getName() + " ratio = " + ratio + ", alive = " + alive);
                            }
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Returns true if the queue is currently open.
         * 
         * @return True if the queue is open.
         */
        public boolean isOpen() {
            return !m_isClosed;
        }

        /**
         * Returns true if the queue is currently closed.
         * 
         * @return True if the queue is closed.
         */
        public boolean isClosed() {
            return m_isClosed;
        }

        /**
         * Closes a currently open queue. When a queue is closed is should still
         * allow elements already in the queue to be removed, but new elements
         * should not be added.
         * 
         * @exception org.opennms.core.queue.FifoQueueException
         *                Thrown if an error occurs closing the queue.
         */
        public void close() throws FifoQueueException {
            m_isClosed = true;
        }

        /**
         * Ensures that the queue is open and new elements can be added to the
         * queue.
         * 
         * @exception org.opennms.core.queue.FifoQueueException
         *                Thrown if an error occurs opening the queue.
         */
        public void open() throws FifoQueueException {
            m_isClosed = false;
        }

        /**
         * Inserts a new element into the queue.
         * 
         * @param element
         *            The object to append to the queue.
         * 
         * @exception org.opennms.core.queue.FifoQueueException
         *                Thrown if a queue error occurs.
         * @exception java.lang.InterruptedException
         *                Thrown if the thread is interrupted.
         */
        public void add(T element) throws FifoQueueException, InterruptedException {
            if (m_isClosed) {
                throw new FifoQueueClosedException("Queue Closed");
            }

            super.add(element);
            adjust();
        }

        /**
         * Inserts a new element into the queue. If the queue has reached an
         * implementation limit and the <code>
         * timeout</code> expires, then a
         * false value is returned to the caller.
         * 
         * @param element
         *            The object to append to the queue.
         * @param timeout
         *            The time to wait on the insertion to succeed.
         * 
         * @exception org.opennms.core.queue.FifoQueueException
         *                Thrown if a queue error occurs.
         * @exception java.lang.InterruptedException
         *                Thrown if the thread is interrupted.
         * 
         * @return True if the element was successfully added to the queue
         *         before the timeout expired, false otherwise.
         */
        public boolean add(T element, long timeout) throws FifoQueueException, InterruptedException {
            if (m_isClosed) {
                throw new FifoQueueClosedException("Queue Closed");
            }

            boolean result = super.add(element, timeout);
            adjust();
            return result;
        }

        /**
         * Removes the oldest element from the queue.
         * 
         * @exception org.opennms.core.queue.FifoQueueException
         *                Thrown if a queue error occurs.
         * @exception java.lang.InterruptedException
         *                Thrown if the thread is interrupted.
         * 
         * @return The oldest object in the queue.
         */
        public T remove() throws FifoQueueException, InterruptedException {
            if (m_isClosed && size() == 0) {
                throw new FifoQueueClosedException("Queue Closed");
            }

            T result = super.remove();
            adjust();
            return result;
        }

        /**
         * Removes the next element from the queue if one becomes available
         * before the timeout expires. If the timeout expires before an element
         * is available then a <code>null</code> reference is returned to the
         * caller.
         * 
         * @param timeout
         *            The time to wait on an object to be available.
         * 
         * @exception org.opennms.core.queue.FifoQueueException
         *                Thrown if a queue error occurs.
         * @exception java.lang.InterruptedException
         *                Thrown if the thread is interrupted.
         * 
         * @return The oldest object in the queue, or <code>null</code> if one
         *         is not available.
         */
        public T remove(long timeout) throws FifoQueueException, InterruptedException {
            if (m_isClosed && size() == 0) {
                throw new FifoQueueClosedException("Queue Closed");
            }

            T result = super.remove(timeout);
            adjust();
            return result;
        }

        private ThreadCategory log() {
            return ThreadCategory.getInstance(getClass());
        }
    }

    /**
     * This class implements the {@link org.opennms.core.fiber.Fiber Fiber}
     * interface on top of a Java {@link java.lang.Thread Thread}instance.
     * These fibers are the basic unit of work in the pool structure. Each fiber
     * reads from the input queue and calls the run method on the associated
     * instance. When finished the fiber invoked the appropriate callback and
     * then repeats the process.
     * 
     * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
     * @author <a href="http://www.opennms.org">OpenNMS </a>
     * 
     */
    private class FiberThreadImpl extends Object implements Fiber, Runnable {
        /**
         * The core thread that is running this fiber.
         */
        private Thread m_delegateThread;

        /**
         * if set true then the thread should exist as soon as possible.
         */
        private volatile boolean m_shutdown;

        /**
         * The status of this fiber.
         */
        private volatile int m_status;

        /**
         * Constructs a new fiber thread and adds the instance to the pool.
         * 
         * @param name
         *            The name of the fiber.
         */
        FiberThreadImpl(String name) {
            m_shutdown = false;
            m_status = START_PENDING;
            m_delegateThread = new Thread(m_tGroup, this, name);
        }

        /**
         * <p>
         * The run method preforms the actual work for the fiber. It loops
         * infinitely until the shutdown flag is set, during which time it
         * processes queue elements. Each element in the queue should be a
         * instance of {@link java.lang.Runnable Runnable}. After each instance
         * is recieved its <code>run</code> method is invoked.
         * </p>
         * 
         * <p>
         * After the object's <code>run</code> method completes it is passed
         * to any interested callers via the listener list in the encapsulated
         * class.
         * </p>
         * 
         */
        public void run() {
            ThreadCategory.setPrefix(m_log4jPrefix);
            m_status = RUNNING;
            while (!m_shutdown) {
                Runnable runnable = null;
                try {
                    runnable = m_delegateQ.remove(500);
                    if (runnable == null) {
                        continue;
                    }
                } catch (InterruptedException e) {
                    m_status = STOP_PENDING;
                    break; // exit, log?
                } catch (FifoQueueException e) {
                    m_status = STOP_PENDING;
                    break; // exit, log?
                }

                try {
                    if (runnable != null) {
                        runnable.run();

                        // If successful, invoke callback to process message
                        RunnableCompletionListener[] list = null;
                        synchronized (m_completedListeners) {
                            list = m_completedListeners.toArray(new RunnableCompletionListener[m_completedListeners.size()]);
                        }
                        for (RunnableCompletionListener listener : list) {
                            listener.onRunnableCompletion(runnable);
                        }
                    }
                } catch (Throwable t) {
                    log().debug("run: an unexpected error occured during fiber run, calling error liseners");

                    /*
                     * call a listener to handle errors?
                     * or should it be logged
                     */
                    RunnableErrorListener[] list = null;
                    synchronized (m_errorListeners) {
                        list = m_errorListeners.toArray(new RunnableErrorListener[m_errorListeners.size()]);
                    }
                    if (list.length == 0) {
                        log().error("No error listeners defined for unexpected error: " + t, t);
                    }
                    for (RunnableErrorListener listener : list) {
                        listener.onRunnableError(runnable, t);
                    }
                }
            }

            m_status = STOPPED;
        }

        /**
         * Starts up the thread.
         */
        public void start() {
            m_status = STARTING;
            m_shutdown = false;
            m_delegateThread.start();
        }

        /**
         * Sets the stop flag in the thread.
         */
        public void stop() {
            m_status = STOP_PENDING;
            m_shutdown = true;
            // m_delegateThread.interrupt();
        }

        /**
         * Returns the name of the thread.
         */
        public String getName() {
            return m_delegateThread.getName();
        }

        /**
         * Returns the current status of the fiber.
         */
        public int getStatus() {
            return m_status;
        }

        private ThreadCategory log() {
            return ThreadCategory.getInstance(getClass());
        }
    }

    /**
     * This interface is used to define a listener for the thread pool that is
     * notified when an enqueued runnable completes. The actually completed
     * runnable is passed to the listener.
     * 
     * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
     * @author <a href="http://www.opennms.org">OpenNMS </a>
     * 
     */
    public static interface RunnableCompletionListener {
        /**
         * This method is invoked when a {@link java.lang.RunnableRunnable}
         * complete.
         * 
         * @param r
         *            The completed Runnable.
         */
        public void onRunnableCompletion(Runnable r);
    }

    /**
     * This interface is used to define a listerer for the thread pool that is
     * notified if an error occurs processing a runnable. The instance is passed
     * as an object just incase the actually object does not implement the
     * {@link java.lang.Runnable Runnable}interface.
     * 
     * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
     * @author <a href="http://www.opennms.org">OpenNMS </a>
     * 
     */
    public static interface RunnableErrorListener {
        /**
         * This method is invoked when a {@link java.lang.RunnableRunnable}
         * fails with an exception.
         * 
         * @param r
         *            The Runnable in error.
         * @param t
         *            The caught error.
         */
        public void onRunnableError(Runnable r, Throwable t);
    }

    /**
     * Returns the current number of living fibers in the pool. A living fiber
     * is any fiber that is not currently in a Stopped state. If the fiber is
     * stopped then its entry will be removed from the internal array to allow
     * for garbage collection.
     * 
     * @return The number of non-stopped fibers
     */
    private int livingFiberCount() {
        int alive = 0;
        synchronized (m_fibers) {
            for (int i = 0; i < m_fibers.length; i++) {
                if (m_fibers[i] != null) {
                    if (m_fibers[i].getStatus() != Fiber.STOPPED) {
                        alive++;
                    } else {
                        m_fibers[i] = null;
                    }
                }
            }
        }
        return alive;
    }

    /**
     * <p>
     * Constructs a new instance of the thread pool class. The thread pool
     * consumes {@link java.lang.Runnable Runnable}instances from the input
     * queue, calling the <code>run</code> method as they are consumed.
     * </p>
     *
     * <p>
     * The size of the thread pool is controlled by the low and high water
     * marks, each water mark being the ratio of queue elements to threads. As
     * the ratio grows past the high water mark more threads are added, up to
     * the maximum amount. As the ratio drops past the low water mark the number
     * of threads are reduced.
     * </p>
     *
     * @param name
     *            The name of the thread pool.
     * @param loMark
     *            The lower ratio used to mark thread reduction.
     * @param hiMark
     *            The high ration used to mark thread production.
     * @param max
     *            The maximum number of threads in the pool.
     * @exception java.lang.IllegalArgumentException
     *                Thrown if the low or high marks are invalid, or the
     *                maximum number of threads is invalid.
     */
    public RunnableConsumerThreadPool(String name, float loMark, float hiMark, int max) {
        Assert.state(loMark <= hiMark, "The lo-mark must be less than the hi-mark");
        Assert.state(max > 0, "The maximum number of fibers must be greater than zero");

        m_delegateQ = new SizingFifoQueue<Runnable>();
        m_fibers = new Fiber[max];
        m_poolName = name;
        m_hiRatio = hiMark;
        m_loRatio = loMark;
        m_maxSize = max;
        m_tGroup = new ThreadGroup(name + "-tgroup");
        m_poolStatus = START_PENDING;

        m_completedListeners = new ArrayList<RunnableCompletionListener>();
        m_errorListeners = new ArrayList<RunnableErrorListener>();

        m_log4jPrefix = ThreadCategory.getPrefix();
    }

    /**
     * Returns the input queue where {@link java.lang.Runnable Runnable}objects
     * are enqueued. Each runnable object will be executed in the order that it
     * is entered.
     *
     * @return The Runnable input queue.
     */
    public FifoQueue<Runnable> getRunQueue() {
        return m_delegateQ;
    }

    /**
     * Starts the thread pool. The first thread is delayed until at least one
     * element is added to the input queue.
     */
    public void start() {
        try {
            m_delegateQ.open();
        } catch (FifoQueueException e) {
            throw new RuntimeException(e.getMessage());
        }
        m_poolStatus = RUNNING;
    }

    /**
     * Begins the shutdown process of the thread pool. The status is set to stop
     * pending and each thread is notified of the new status.
     */
    public void stop() {
        synchronized (m_fibers) {
            for (Fiber fiber : m_fibers) {
                if (fiber != null) {
                    fiber.stop();
                }
            }
        }
        m_poolStatus = STOP_PENDING;

        try {
            m_delegateQ.close();
        } catch (FifoQueueException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Returns the current status of the fiber.
     *
     * @return The current pool status.
     */
    public int getStatus() {
        if (m_poolStatus == STOP_PENDING) {
            if (livingFiberCount() == 0) {
                m_poolStatus = STOPPED;
            }
        }

        return m_poolStatus;
    }

    /**
     * Returns the name of the thread pool.
     *
     * @return The name of the pool.
     */
    public String getName() {
        return m_poolName;
    }

    /**
     * Adds a listener that is notified upon completion of each runnable.
     *
     * @param listener
     *            The listener notified on runnable completion.
     */
    public void addCompletionListener(RunnableCompletionListener listener) {
        synchronized (m_completedListeners) {
            m_completedListeners.add(listener);
        }
    }

    /**
     * Removes a previous register listener.
     *
     * @param listener
     *            The listener to remove from notification.
     */
    public void removeCompletionListener(RunnableCompletionListener listener) {
        synchronized (m_completedListeners) {
            m_completedListeners.remove(listener);
        }
    }

    /**
     * Adds a listener that is notified of error from any runnable.
     *
     * @param listener
     *            The listener notified on runnable completion.
     */
    public void addErrorListener(RunnableErrorListener listener) {
        synchronized (m_errorListeners) {
            m_errorListeners.add(listener);
        }
    }

    /**
     * Removes a previous register listener.
     *
     * @param listener
     *            The listener to remove from notification.
     */
    public void removeErrorListener(RunnableErrorListener listener) {
        synchronized (m_errorListeners) {
            m_errorListeners.remove(listener);
        }
    }
}
