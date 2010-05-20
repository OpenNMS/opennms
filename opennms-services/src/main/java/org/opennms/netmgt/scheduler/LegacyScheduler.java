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
 * 2007 May 21: Use java 5 generics and loops and format code. - dj@opennms.org
 * 2003 Jan 31: Cleaned up some unused imports.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.scheduler;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.util.Assert;

/**
 * This class implements a simple scheduler to ensure the polling occurs at the
 * expected intervals. The scheduler employees a dynamic thread pool that adjust
 * to the load until a maximum thread count is reached.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
public class LegacyScheduler implements Runnable, PausableFiber, Scheduler {
    /**
     * The map of queue that contain {@link ReadyRunnable ready runnable}
     * instances. The queues are mapped according to the interval of scheduling.
     */
    private Map<Long, PeekableFifoQueue<ReadyRunnable>> m_queues;

    /**
     * The total number of elements currently scheduled. This should be the sum
     * of all the elements in the various queues.
     */
    private int m_scheduled;

    /**
     * The pool of threads that are used to executed the runnable instances
     * scheduled by the class' instance.
     */
    private RunnableConsumerThreadPool m_runner;

    /**
     * The status for this fiber.
     */
    private int m_status;

    /**
     * The worker thread that executes this instance.
     */
    private Thread m_worker;

    /**
     * This queue extends the standard FIFO queue instance so that it is
     * possible to peek at an instance without removing it from the queue.
     * 
     */
    public static final class PeekableFifoQueue<T> extends FifoQueueImpl<T> {
        /**
         * The object hold. This holds the last object peeked at by the
         * application.
         */
        private T m_hold;

        /**
         * Default constructor.
         */
        PeekableFifoQueue() {
            m_hold = null;
        }

        /**
         * This method allows the caller to peek at the next object that would
         * be returned on a <code>remove</code> call. If the queue is
         * currently empty then the caller is blocked until an object is put
         * into the queue.
         * 
         * @return The object that would be returned on the next call to
         *         <code>remove</code>.
         * 
         * @throws java.lang.InterruptedException
         *             Thrown if the thread is interrupted.
         * @throws org.opennms.core.queue.FifoQueueException
         *             Thrown if an error occurs removing an item from the
         *             queue.
         */
        public synchronized T peek() throws InterruptedException, FifoQueueException {
            if (m_hold == null) {
                m_hold = super.remove(1L);
            }

            return m_hold;
        }

        /**
         * Removes the next element from the queue and returns it to the caller.
         * If there is no objects available then the caller is blocked until an
         * item is available.
         * 
         * @return The next element in the queue.
         * 
         * @throws java.lang.InterruptedException
         *             Thrown if the thread is interrupted.
         * @throws org.opennms.core.queue.FifoQueueException
         *             Thrown if an error occurs removing an item from the
         *             queue.
         */
        public synchronized T remove() throws InterruptedException, FifoQueueException {
            T rval = null;
            if (m_hold != null) {
                rval = m_hold;
                m_hold = null;
            } else {
                rval = super.remove();
            }

            return rval;
        }

        /**
         * Removes the next element from the queue and returns it to the caller.
         * If there is no objects available then the caller is blocked until an
         * item is available. If an object is not available within the time
         * frame specified by <code>timeout</code>.
         * 
         * @param timeout
         *            The maximum time to wait.
         * 
         * @return The next element in the queue.
         * 
         * @throws java.lang.InterruptedException
         *             Thrown if the thread is interrupted.
         * @throws org.opennms.core.queue.FifoQueueException
         *             Thrown if an error occurs removing an item from the
         *             queue.
         */
        public synchronized T remove(long timeout) throws InterruptedException, FifoQueueException {
            T rval = null;
            if (m_hold != null) {
                rval = m_hold;
                m_hold = null;
            } else {
                rval = super.remove(timeout);
            }

            return rval;
        }
    }

    /**
     * Constructs a new instance of the scheduler. The maximum number of
     * executable threads is specified in the constructor. The executable
     * threads are part of a runnable thread pool where the scheduled runnables
     * are executed.
     * 
     * @param parent
     *            String prepended to "Scheduler" to create fiber name
     * @param maxSize
     *            The maximum size of the thread pool.
     * 
     */
    public LegacyScheduler(String parent, int maxSize) {
        String name = parent + "Scheduler-" + maxSize;
        m_status = START_PENDING;
        m_runner = new RunnableConsumerThreadPool(name + " Pool", 0.6f, 1.0f, maxSize);
        m_queues = Collections.synchronizedMap(new TreeMap<Long, PeekableFifoQueue<ReadyRunnable>>());
        m_scheduled = 0;
        m_worker = null;
    }

    /**
     * Constructs a new instance of the scheduler. The maximum number of
     * executable threads is specified in the constructor. The executable
     * threads are part of a runnable thread pool where the scheduled runnables
     * are executed.
     * 
     * @param parent
     *            String prepended to "Scheduler" to create fiber name
     * @param maxSize
     *            The maximum size of the thread pool.
     * @param lowMark
     *            The low water mark ratios of thread size to threads when
     *            threads are stopped.
     * @param hiMark
     *            The high water mark ratio of thread size to threads when
     *            threads are started.
     * 
     */
    public LegacyScheduler(String parent, int maxSize, float lowMark, float hiMark) {
        String name = parent + "Scheduler-" + maxSize;
        m_status = START_PENDING;
        m_runner = new RunnableConsumerThreadPool(name + " Pool", lowMark, hiMark, maxSize);
        m_queues = Collections.synchronizedMap(new TreeMap<Long, PeekableFifoQueue<ReadyRunnable>>());
        m_scheduled = 0;
        m_worker = null;
    }

    /**
     * This method is used to schedule a ready runnable in the system. The
     * interval is used as the key for determining which queue to add the
     * runnable.
     * 
     * @param runnable
     *            The element to run when interval expires.
     * @param interval
     *            The queue to add the runnable to.
     * 
     * @throws java.lang.RuntimeException
     *             Thrown if an error occurs adding the element to the queue.
     */
    public synchronized void schedule(ReadyRunnable runnable, long interval) {
        if (log().isDebugEnabled()) {
            log().debug("schedule: Adding ready runnable "+runnable+" at interval " + interval);
        }

        Long key = new Long(interval);
        if (!m_queues.containsKey(key)) {
            if (log().isDebugEnabled()) {
                log().debug("schedule: interval queue did not exist, a new one has been created");
            }
            m_queues.put(key, new PeekableFifoQueue<ReadyRunnable>());
        }

        try {
            m_queues.get(key).add(runnable);
            if (m_scheduled++ == 0) {
                log().debug("schedule: queue element added, calling notify all since none were scheduled");
                notifyAll();
            } else if (log().isDebugEnabled()) {
                log().debug("schedule: queue element added, notification not performed");
            }
        } catch (InterruptedException e) {
            log().info("schedule: failed to add new ready runnable instance " + runnable + " to scheduler: " + e, e);
            Thread.currentThread().interrupt();
        } catch (FifoQueueException e) {
            log().info("schedule: failed to add new ready runnable instance " + runnable + " to scheduler: " + e, e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#schedule(long, org.opennms.netmgt.scheduler.ReadyRunnable)
	 */
    public synchronized void schedule(long interval, final ReadyRunnable runnable) {
        final long timeToRun = getCurrentTime()+interval;
        ReadyRunnable timeKeeper = new ReadyRunnable() {
            public boolean isReady() {
                return getCurrentTime() >= timeToRun && runnable.isReady();
            }
            
            public void run() {
                runnable.run();
            }
            
            public String toString() { return runnable.toString()+" (ready in "+Math.max(0, timeToRun-getCurrentTime())+"ms)"; }
        };
        schedule(timeKeeper, interval);
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#getCurrentTime()
	 */
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#start()
	 */
    public synchronized void start() {
        Assert.state(m_worker == null, "The fiber has already run or is running");

        m_runner.start();
        m_worker = new Thread(this, getName());
        m_worker.start();
        m_status = STARTING;

        log().info("start: scheduler started");
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#stop()
	 */
    public synchronized void stop() {
        Assert.state(m_worker != null, "The fiber has never been started");

        m_status = STOP_PENDING;
        m_worker.interrupt();
        m_runner.stop();

        log().info("stop: scheduler stopped");
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#pause()
	 */
    public synchronized void pause() {
        Assert.state(m_worker != null, "The fiber has never been started");
        Assert.state(m_status != STOPPED && m_status != STOP_PENDING, "The fiber is not running or a stop is pending");

        if (m_status == PAUSED) {
            return;
        }

        m_status = PAUSE_PENDING;
        notifyAll();
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#resume()
	 */
    public synchronized void resume() {
        Assert.state(m_worker != null, "The fiber has never been started");
        Assert.state(m_status != STOPPED && m_status != STOP_PENDING, "The fiber is not running or a stop is pending");

        if (m_status == RUNNING) {
            return;
        }

        m_status = RESUME_PENDING;
        notifyAll();
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#getStatus()
	 */
    public synchronized int getStatus() {
        if (m_worker != null && m_worker.isAlive() == false) {
            m_status = STOPPED;
        }
        return m_status;
    }

    /**
     * Returns the name of this fiber.
     * 
     */
    public String getName() {
        return m_runner.getName();
    }
    
    /**
     * Returns total number of elements currently scheduled.
     * 
     * @return the sum of all the elements in the various queues
     */
    public int getScheduled() {
        return m_scheduled;
    }

    /**
     * Returns the pool of threads that are used to executed the runnable
     * instances scheduled by the class' instance.
     * 
     * @return thread pool
     */
    public RunnableConsumerThreadPool getRunner() {
        return m_runner;
    }

    /**
     * The main method of the scheduler. This method is responsible for checking
     * the runnable queues for ready objects and then enqueuing them into the
     * thread pool for execution.
     */
    public void run() {
        synchronized (this) {
            m_status = RUNNING;
        }

        log().debug("run: scheduler running");

        /*
         * Loop until a fatal exception occurs or until
         * the thread is interrupted.
         */
        for (;;) {
            /*
             * Block if there is nothing in the queue(s).
             * When something is added to the queue it
             * signals us to wakeup.
             */
            synchronized (this) {
                
                if (m_status != RUNNING && m_status != PAUSED && m_status != PAUSE_PENDING && m_status != RESUME_PENDING) {
                    if (log().isDebugEnabled()) {
                        log().debug("run: status = " + m_status + ", time to exit");
                    }
                    break;
                }

                // if paused or pause pending then block
                while (m_status == PAUSE_PENDING || m_status == PAUSED) {
                    if (m_status == PAUSE_PENDING) {
                        log().debug("run: pausing.");
                    }
                    m_status = PAUSED;
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // exit
                        break;
                    }
                }

                // if resume pending then change to running

                if (m_status == RESUME_PENDING) {
                    log().debug("run: resuming.");
                    
                    m_status = RUNNING;
                }

                if (m_scheduled == 0) {
                    try {
                        log().debug("run: no ready runnables scheduled, waiting...");
                        wait();
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }

            /*
             * Cycle through the queues checking for
             * what's ready to run.  The queues are keyed
             * by the interval, but the mapped elements
             * are peekable fifo queues.
             */
            int runned = 0;
            FifoQueue<Runnable> out = m_runner.getRunQueue();
            synchronized (m_queues) {
                /*
                 * Get an iterator so that we can cycle
                 * through the queue elements.
                 */
                for (Entry<Long, PeekableFifoQueue<ReadyRunnable>> entry : m_queues.entrySet()) {
                    /*
                     * Peak for Runnable objects until
                     * there are no more ready runnables.
                     *
                     * Also, only go through each queue once!
                     * if we didn't add a count then it would
                     * be possible to starve other queues.
                     */
                    PeekableFifoQueue<ReadyRunnable> in = entry.getValue();
                    ReadyRunnable readyRun = null;
                    int maxLoops = in.size();
                    do {
                        try {
                            readyRun = in.peek();
                            if (readyRun != null && readyRun.isReady()) {
                                if (log().isDebugEnabled()) {
                                    log().debug("run: found ready runnable "+readyRun);
                                }

                                /*
                                 * Pop the interface/readyRunnable from the
                                 * queue for execution.
                                 */
                                in.remove();

                                // Add runnable to the execution queue
                                out.add(readyRun);
                                ++runned;
                            }
                        } catch (InterruptedException e) {
                            return; // jump all the way out
                        } catch (FifoQueueException e) {
                            throw new UndeclaredThrowableException(e);
                        }

                    } while (readyRun != null && readyRun.isReady() && --maxLoops > 0);
                }
            }

            /*
             * Wait for 1 second if there were no runnables
             * executed during this loop, otherwise just
             * start over.
             */
            synchronized (this) {
                m_scheduled -= runned;
                if (runned == 0) {
                    try {
                        wait(1000);
                    } catch (InterruptedException ex) {
                        break; // exit for loop
                    }
                }
            }

        }

        log().debug("run: scheduler exiting, state = STOPPED");
        synchronized (this) {
            m_status = STOPPED;
        }

    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
