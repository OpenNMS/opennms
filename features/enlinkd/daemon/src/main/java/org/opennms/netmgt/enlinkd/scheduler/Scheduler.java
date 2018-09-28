/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.scheduler;


import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.opennms.core.fiber.PausableFiber;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a simple scheduler to ensure the polling occurs at the
 * expected intervals. The scheduler employees a dynamic thread pool that adjust
 * to the load until a maximum thread count is reached.
 */
public class Scheduler implements Runnable, PausableFiber, ScheduleTimer {
    private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);

	/**
	 * The map of queue that contain {@link ReadyRunnable ready runnable}
	 * instances. The queues are mapped according to the interval of scheduling.
	 */
	public Map<Long,BlockingQueue<ReadyRunnable>> m_queues;

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
	public Scheduler(String parent, int maxSize) {
		m_status = START_PENDING;
		m_runner = Executors.newFixedThreadPool(
			maxSize,
			new LogPreservingThreadFactory(parent, maxSize)
		);
		m_queues = new ConcurrentSkipListMap<Long,BlockingQueue<ReadyRunnable>>();
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
	public Scheduler(String parent, int maxSize, float lowMark, float hiMark) {
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
		LOG.debug("schedule: Adding ready runnable at interval {}", interval);

		if (!m_queues.containsKey(interval)) {
			LOG.debug("schedule: interval queue did not exist, a new one has been created");
			m_queues.put(interval, new LinkedBlockingQueue<ReadyRunnable>());
		}

		(m_queues.get(interval)).add(runnable);
		if (m_scheduled++ == 0) {
			LOG.debug("schedule: queue element added, calling notify all since none were scheduled");
			notifyAll();
		} else {
			LOG.debug("schedule: queue element added, notification not performed");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method is used to schedule a ready runnable in the system. The
	 * interval is used as the key for determining which queue to add the
	 * runnable.
	 */
        @Override
	public synchronized void schedule(long interval,
			final ReadyRunnable runnable) {

		final long timeToRun = getCurrentTime() + interval;
		ReadyRunnable timeKeeper = new ReadyRunnable() {
                        @Override
			public boolean isReady() {
				return getCurrentTime() >= timeToRun && runnable.isReady();
			}
			
                        @Override
			public String getInfo() {
				return runnable.getInfo();
			}

                        @Override
			public void run() {
				runnable.run();
			}

                        @Override
			public void schedule() {
				runnable.schedule();
			}
			
                        @Override
			public void suspend() {
				runnable.suspend();
			}

                        @Override
			public void wakeUp() {
				runnable.wakeUp();
			}

                        @Override
			public boolean isSuspended() {
				return runnable.isSuspended();
			}
			
                        @Override
			public boolean equals(Object r) {
				return runnable.equals(r);
			}
			
                        @Override
			public void unschedule() {
				runnable.unschedule();
			}

                        @Override
			public String toString() {
				return runnable.toString() + " (ready in "
						+ Math.max(0, timeToRun - getCurrentTime()) + "ms)";
			}
			
		};
		schedule(timeKeeper, interval);
	}

	/**
	 * This method is used to unschedule a ready runnable in the system.
	 * The runnable is removed from all queue interval where is found.
	 *
	 * @param runnable
	 *            The element to remove from queue intervals.
	 */
	public synchronized void unschedule(ReadyRunnable runnable) {
	    LOG.debug("unschedule: Removing all {}", runnable.getInfo());
		
		synchronized(m_queues) {
		  Iterator<Long> iter = m_queues.keySet().iterator();
		  while (iter.hasNext()) {
		
			Long key = iter.next();
			unschedule(runnable, key.longValue());
			}
		}
	}

	/**
	 * This method is used to unschedule a ready runnable in the system. The
	 * interval is used as the key for determining which queue to remove the
	 * runnable.
	 *
	 * @param interval
	 *            The queue to add the runnable to.
	 * @param runnable
	 *            The element to remove.
	 */
	public synchronized void unschedule(ReadyRunnable runnable, long interval) {
	    LOG.debug("unschedule: Removing {} at interval {}", runnable.getInfo(), interval);
		synchronized(m_queues) {
			if (!m_queues.containsKey(interval)) {
			    LOG.debug("unschedule: interval queue did not exist, exit");
				return;
			}
			
			BlockingQueue<ReadyRunnable> in = m_queues.get(interval);
			if (in.isEmpty()) {
			    LOG.debug("unschedule: interval queue is empty, exit");
				return;
			}
			
			ReadyRunnable readyRun = null;
			int maxLoops = in.size();
			boolean first = true;
			do {
				try {
					readyRun = in.take();
					if (in.size() == maxLoops && first) {
						maxLoops++;
					}
					first = false;
					if (readyRun != null && readyRun.equals(runnable)) {
						LOG.debug("unschedule: removing found {}", readyRun.getInfo());

						// Pop the interface/readyRunnable from the
						// queue for execution.
						//
						m_scheduled--;
					} else {
						in.add(readyRun);
					}
				} catch (InterruptedException ie) {
					LOG.info("unschedule: failed to remove instance {} from scheduler", runnable.getInfo(), ie);
					Thread.currentThread().interrupt();
				}
			} while ( --maxLoops > 0) ; 
		}
	}

	/**
	 * This method is used to get a ready runnable in the system.
	 *
	 * @param runnable
	 *            The element to get from queues interval.
	 * @return a {@link org.opennms.netmgt.enlinkd.scheduler.ReadyRunnable} object.
	 */
	public synchronized ReadyRunnable getReadyRunnable(ReadyRunnable runnable) {
		ReadyRunnable rr = null;
		synchronized (m_queues) {
			// get an iterator so that we can cycle
			// through the queue elements.
			//
			Iterator<Long> iter = m_queues.keySet().iterator();
			while (iter.hasNext() && rr==null) {
				Long interval = iter.next();
				rr = getReadyRunnable(runnable, interval.longValue());
			}
		}

		return rr;
	}
	
	/**
	 * <p>getReadyRunnable</p>
	 *
	 * @param runnable a {@link org.opennms.netmgt.enlinkd.scheduler.ReadyRunnable} object.
	 * @param interval a long.
	 * @return a {@link org.opennms.netmgt.enlinkd.scheduler.ReadyRunnable} object.
	 */
	public synchronized ReadyRunnable getReadyRunnable(ReadyRunnable runnable, long interval) {
	    LOG.debug("getReadyRunnable: Retrieving {} at interval {}", runnable.getInfo(), interval);

		if (!m_queues.containsKey(interval)) {
		    LOG.debug("getReadyRunnable: interval queue did not exist, exit");
			return null;
		}

		ReadyRunnable rr = null;
		synchronized (m_queues) {
			BlockingQueue<ReadyRunnable> in = m_queues.get(interval);
			if (in.isEmpty()) {
			    LOG.warn("getReadyRunnable: queue is Empty");
				return null;
			}
			
			int maxLoops = in.size();
			ReadyRunnable readyRun = null;
			boolean first = true;
			do {
				try {
					readyRun = in.take();
					LOG.debug("getReadyRunnable: parsing ready runnable {}", readyRun);
					if (in.size() == maxLoops && first) {
						maxLoops++;
					}
					first = false;
					if (readyRun != null && readyRun.equals(runnable)) {
						LOG.debug("getReadyRunnable: found ready runnable {}", readyRun);
						rr = readyRun;
					}
					in.add(readyRun);
				} catch (InterruptedException ie) {
					LOG.info("getReadyRunnable: failed to get instance {} from scheduler", readyRun.getInfo(), ie);
					Thread.currentThread().interrupt();
				}
			} while (--maxLoops > 0) ;
		}

		if (rr == null) {
			LOG.info("getReadyRunnable: instance {} not found on scheduler", runnable.getInfo());
		}
		return rr;
	}

	/**
	 * This returns the current time for the scheduler
	 *
	 * @return a long.
	 */
	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

	/**
	 * Starts the fiber.
	 *
	 * @throws java.lang.IllegalStateException
	 *             Thrown if the fiber is already running.
	 */
	@Override
	public synchronized void start() {
		if (m_worker != null)
			throw new IllegalStateException(
					"The fiber has already run or is running");

		m_worker = new Thread(this, getName());
		m_worker.start();
		m_status = STARTING;

		LOG.debug("start: scheduler started");
	}

	/**
	 * Stops the fiber. If the fiber has never been run then an exception is
	 * generated.
	 *
	 * @throws java.lang.IllegalStateException
	 *             Throws if the fiber has never been started.
	 */
        @Override
	public synchronized void stop() {
		if (m_worker == null)
			throw new IllegalStateException("The fiber has never been started");

		m_status = STOP_PENDING;
		m_worker.interrupt();
		m_runner.shutdown();

		LOG.debug("stop: scheduler stopped");
	}

	/**
	 * Pauses the scheduler if it is current running. If the fiber has not been
	 * run or has already stopped then an exception is generated.
	 *
	 * @throws java.lang.IllegalStateException
	 *             Throws if the operation could not be completed due to the
	 *             fiber's state.
	 */
        @Override
	public synchronized void pause() {
		if (m_worker == null)
			throw new IllegalStateException("The fiber has never been started");

		if (m_status == STOPPED || m_status == STOP_PENDING)
			throw new IllegalStateException(
					"The fiber is not running or a stop is pending");

		if (m_status == PAUSED)
			return;

		m_status = PAUSE_PENDING;
		notifyAll();
	}

	/**
	 * Resumes the scheduler if it has been paused. If the fiber has not been
	 * run or has already stopped then an exception is generated.
	 *
	 * @throws java.lang.IllegalStateException
	 *             Throws if the operation could not be completed due to the
	 *             fiber's state.
	 */
        @Override
	public synchronized void resume() {
		if (m_worker == null)
			throw new IllegalStateException("The fiber has never been started");

		if (m_status == STOPPED || m_status == STOP_PENDING)
			throw new IllegalStateException(
					"The fiber is not running or a stop is pending");

		if (m_status == RUNNING)
			return;

		m_status = RESUME_PENDING;
		notifyAll();
	}

	/**
	 * Returns the current of this fiber.
	 *
	 * @return The current status.
	 */
        @Override
	public synchronized int getStatus() {
		if (m_worker != null && m_worker.isAlive() == false)
			m_status = STOPPED;
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
     * The main method of the scheduler. This method is responsible for
     * checking the runnable queues for ready objects and then enqueuing them
     * into the thread pool for execution.
     */
    @Override
    public void run() {

        synchronized (this) {
            m_status = RUNNING;
        }

        LOG.debug("run: scheduler running");

        // Loop until a fatal exception occurs or until
        // the thread is interrupted.
        //
        for (;;) {
            // block if there is nothing in the queue(s)
            // When something is added to the queue it
            // signals us to wakeup
            //
            synchronized (this) {
                if (m_status != RUNNING && m_status != PAUSED
                        && m_status != PAUSE_PENDING
                        && m_status != RESUME_PENDING) {
                    LOG.debug("run: status = {}, time to exit", m_status);
                    break;
                }

                if (m_scheduled == 0) {
                    try {
                        LOG.debug("run: no interfaces scheduled, waiting...");
                        wait();
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }

            // cycle through the queues checking for
            // what's ready to run. The queues are keyed
            // by the interval, but the mapped elements
            // are peekable fifo queues.
            //

            // cycle through the queues checking for
            // what's ready to run. The queues are keyed
            // by the interval, but the mapped elements
            // are peekable fifo queues.
            //
            int runned = 0;
            synchronized (m_queues) {
                // get an iterator so that we can cycle
                // through the queue elements.
                //
                for (Entry<Long, BlockingQueue<ReadyRunnable>> entry : m_queues.entrySet()) {
                    // Peak for Runnable objects until
                    // there are no more ready runnables
                    //
                    // Also, only go through each queue once!
                    // if we didn't add a count then it would
                    // be possible to starve other queues.
                    //
                    Long key = entry.getKey();
                    BlockingQueue<ReadyRunnable> in = m_queues.get(key);
                    if (in == null || in.isEmpty()) {
                        continue;
                    }
                    ReadyRunnable readyRun = null;
                    int maxLoops = in.size();
                    do {
                        try {
                            readyRun = in.peek();
                            if (readyRun != null) {
                                // Pop the interface/readyRunnable from the
                                // queue for execution.
                                //
                                in.take();

                                if (readyRun.isReady()) {
                                    LOG.debug("run: runnable {}, executing",
                                              readyRun.getInfo());

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

                                } else {
                                    in.add(readyRun);
                                }
                            }
                        } catch (InterruptedException ex) {
                            return; // jump all the way out
                        }
                    } while (--maxLoops > 0);
                }

            }

            // Wait for 1 second if there were no runnables
            // executed during this loop, otherwise just
            // start over.
            //
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

        } // end for(;;)

        LOG.debug("run: scheduler exiting, state = STOPPED");
        synchronized (this) {
            m_status = STOPPED;
        }

    } // end run
    
    public long getNumTasksExecuted() {
        return m_numTasksExecuted;
    }


}
