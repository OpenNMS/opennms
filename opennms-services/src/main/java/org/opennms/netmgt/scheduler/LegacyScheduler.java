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

package org.opennms.netmgt.scheduler;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.fiber.PausableFiber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * This class implements a simple scheduler to ensure the polling occurs at the
 * expected intervals. The scheduler employees a dynamic thread pool that adjust
 * to the load until a maximum thread count is reached.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public class LegacyScheduler implements Runnable, PausableFiber, Scheduler {
    
    private static final Logger LOG = LoggerFactory.getLogger(LegacyScheduler.class);
    
    /**
     * The map of queue that contain {@link ReadyRunnable ready runnable}
     * instances. The queues are mapped according to the interval of scheduling.
     */
    private final Map<Long, BlockingQueue<ReadyRunnable>> m_queues;

    /**
     * The total number of elements currently scheduled. This should be the sum
     * of all the elements in the various queues.
     */
    private volatile int m_scheduled;

    /**
     * The pool of threads that are used to executed the runnable instances
     * scheduled by the class' instance.
     */
    private final ExecutorService m_runner;

    /**
     * The status for this fiber.
     */
    private volatile int m_status;

    /**
     * The worker thread that executes this instance.
     */
    private volatile Thread m_worker;

    /**
     * Used to keep track of the number of tasks that have been executed.
     */
    private volatile long m_numTasksExecuted = 0;

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
     */
    public LegacyScheduler(final String parent, final int maxSize) {
        m_status = START_PENDING;
        m_runner = Executors.newFixedThreadPool(maxSize, new LogPreservingThreadFactory(parent, maxSize));
        m_queues = new ConcurrentSkipListMap<Long, BlockingQueue<ReadyRunnable>>();
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
     */
    public LegacyScheduler(String parent, int maxSize, float lowMark, float hiMark) {
        this(parent, maxSize);
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
     * @throws java.lang.RuntimeException
     *             Thrown if an error occurs adding the element to the queue.
     */
    public synchronized void schedule(ReadyRunnable runnable, long interval) {
        LOG.debug("schedule: Adding ready runnable {} at interval {}", runnable, interval);

        Long key = Long.valueOf(interval);
        if (!m_queues.containsKey(key)) {
            LOG.debug("schedule: interval queue did not exist, a new one has been created");
            m_queues.put(key, new LinkedBlockingQueue<ReadyRunnable>());
        }

        m_queues.get(key).add(runnable);
        if (m_scheduled++ == 0) {
            LOG.debug("schedule: queue element added, calling notify all since none were scheduled");
            notifyAll();
        } else {
            LOG.debug("schedule: queue element added, notification not performed");
        }
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#schedule(long, org.opennms.netmgt.scheduler.ReadyRunnable)
	 */
    /** {@inheritDoc} */
    @Override
    public synchronized void schedule(long interval, final ReadyRunnable runnable) {
        final long timeToRun = getCurrentTime()+interval;
        ReadyRunnable timeKeeper = new ReadyRunnable() {
            @Override
            public boolean isReady() {
                return getCurrentTime() >= timeToRun && runnable.isReady();
            }
            
            @Override
            public void run() {
                runnable.run();
            }
            
            @Override
            public String toString() { return runnable.toString()+" (ready in "+Math.max(0, timeToRun-getCurrentTime())+"ms)"; }
        };
        schedule(timeKeeper, interval);
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#getCurrentTime()
	 */
    /**
     * <p>getCurrentTime</p>
     *
     * @return a long.
     */
    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
    
    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#start()
	 */
    /**
     * <p>start</p>
     */
    @Override
    public synchronized void start() {
        Assert.state(m_worker == null, "The fiber has already run or is running");

        m_worker = new Thread(this, getName());
        m_worker.start();
        m_status = STARTING;

        LOG.info("start: scheduler started");
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#stop()
	 */
    /**
     * <p>stop</p>
     */
    @Override
    public synchronized void stop() {
        Assert.state(m_worker != null, "The fiber has never been started");

        m_status = STOP_PENDING;
        m_worker.interrupt();
        m_runner.shutdown();

        LOG.info("stop: scheduler stopped");
    }

    /* (non-Javadoc)
	 * @see org.opennms.netmgt.scheduler.Scheduler#pause()
	 */
    /**
     * <p>pause</p>
     */
    @Override
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
    /**
     * <p>resume</p>
     */
    @Override
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
    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public synchronized int getStatus() {
        if (m_worker != null && m_worker.isAlive() == false) {
            m_status = STOPPED;
        }
        return m_status;
    }

    /**
     * Returns the name of this fiber.
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_runner.toString();
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
    public ExecutorService getRunner() {
        return m_runner;
    }

    /**
     * The main method of the scheduler. This method is responsible for checking
     * the runnable queues for ready objects and then enqueuing them into the
     * thread pool for execution.
     */
    @Override
    public void run() {
        synchronized (this) {
            m_status = RUNNING;
        }

        LOG.debug("run: scheduler running");

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
                    LOG.debug("run: status = {}, time to exit", m_status);
                    break;
                }

                // if paused or pause pending then block
                while (m_status == PAUSE_PENDING || m_status == PAUSED) {
                    if (m_status == PAUSE_PENDING) {
                        LOG.debug("run: pausing.");
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
                    LOG.debug("run: resuming.");
                    
                    m_status = RUNNING;
                }

                if (m_scheduled == 0) {
                    try {
                        LOG.debug("run: no ready runnables scheduled, waiting...");
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
            synchronized (m_queues) {
                /*
                 * Get an iterator so that we can cycle
                 * through the queue elements.
                 */
                for (Entry<Long, BlockingQueue<ReadyRunnable>> entry : m_queues.entrySet()) {
                    /*
                     * Peak for Runnable objects until
                     * there are no more ready runnables.
                     *
                     * Also, only go through each queue once!
                     * if we didn't add a count then it would
                     * be possible to starve other queues.
                     */
                    BlockingQueue<ReadyRunnable> in = entry.getValue();
                    ReadyRunnable readyRun = null;
                    int maxLoops = in.size();
                    do {
                        try {
                            readyRun = in.peek();
                            if (readyRun != null && readyRun.isReady()) {
                                LOG.debug("run: found ready runnable {}", readyRun);

                                /*
                                 * Pop the interface/readyRunnable from the
                                 * queue for execution.
                                 */
                                in.take();

                                // Add runnable to the execution queue
                                m_runner.execute(readyRun);
                                ++runned;

                                // Increment the execution counter
                                ++m_numTasksExecuted;

                                // Thread Pool Statistics
                                if (m_runner instanceof ThreadPoolExecutor) {
                                    ThreadPoolExecutor e = (ThreadPoolExecutor) m_runner;
                                    String ratio = String.format("%.3f", e.getTaskCount() > 0 ? new Double(e.getCompletedTaskCount())/new Double(e.getTaskCount()) : 0);
                                    LOG.debug("thread pool statistics: activeCount={}, taskCount={}, completedTaskCount={}, completedRatio={}, poolSize={}",
                                        e.getActiveCount(), e.getTaskCount(), e.getCompletedTaskCount(), ratio, e.getPoolSize());
                                }
                            }
                        } catch (InterruptedException e) {
                            return; // jump all the way out
                        } catch (RejectedExecutionException e) {
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

        LOG.debug("run: scheduler exiting, state = STOPPED");
        synchronized (this) {
            m_status = STOPPED;
        }

    }

    /** {@inheritDoc} */
    @Override
    public long getNumTasksExecuted() {
        return m_numTasksExecuted;
    }
}
