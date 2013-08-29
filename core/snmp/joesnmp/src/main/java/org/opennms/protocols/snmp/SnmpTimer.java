/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.snmp;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Provides a simple timer scheduler for use by the internal SnmpSession class.
 * Resolution is provided at the millisecond level.
 * 
 * @see SnmpSession
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 */
class SnmpTimer extends Object {
    /**
     * The list of runnable objects (stored as TimeoutElement)
     */
    private LinkedList<TimeoutElement> m_list;

    /**
     * The thread doing the scheduling
     */
    private Thread m_thread;

    /**
     * when true the internal thread should exit
     */
    private boolean m_exit;

    /**
     * The synchronization object
     */
    private Object m_sync;

    /**
     * Used to track the individual runnables and when the runnable "expires".
     * 
     */
    private class TimeoutElement {
        /**
         * The runnable object
         */
        public Runnable m_toRun;

        /**
         * The date to run the runnable
         */
        public long m_when;

        /**
         * Default Constructor. Takes an Offset from now and a runnable that
         * will be executed.
         * 
         * @param offset
         *            The offset from the current time in milliseconds.
         * @param what
         *            The runnable to be executed
         */
        TimeoutElement(long offset, Runnable what) {
            m_when = System.currentTimeMillis() + offset;
            m_toRun = what;
        }
    }

    /**
     * This object is the thread of execution that monitors and executes the
     * scheduled runnables.
     * 
     */
    private class Scheduler implements Runnable {
        /**
         * Runs in an infinite loop waiting for new runnables to expire or for
         * the m_exit variable to be set true. The m_sync in the parent class is
         * used to synchronize this method
         * 
         */
        @Override
        public void run() {
            LinkedList<Runnable> toRun = new LinkedList<Runnable>();
            while (true) {
                //
                // synchronize on the object
                //
                synchronized (m_sync) {
                    if (m_exit)
                        return;

                    //
                    // if there are no elements on the list
                    // then wait
                    //
                    if (m_list.size() == 0) {
                        try {
                            m_sync.wait();
                        } catch (InterruptedException err) {
                            return;
                        }

                        //
                        // restart the loop
                        //
                        continue;
                    }

                    //
                    // find the smallest time slice
                    // and run those in error
                    //
                    long now = System.currentTimeMillis();
                    boolean done = false;
                    long minTime = Long.MAX_VALUE;
                    ListIterator<TimeoutElement> iter = m_list.listIterator(0);

                    while (!done && iter.hasNext()) {
                        try {
                            //
                            // get the next timeout element
                            //
                            TimeoutElement elem = iter.next();
                            if (now > elem.m_when) {
                                //
                                // The element has expired
                                //
                                toRun.add(elem.m_toRun);
                                iter.remove();
                            } else {
                                //
                                // find out if this time is less
                                // than the one currently stored
                                //
                                if (elem.m_when < minTime)
                                    minTime = elem.m_when;
                            }
                        } catch (NoSuchElementException err) {
                            done = true;
                        } catch (ConcurrentModificationException err) {
                            done = true;
                        }
                    }

                    //
                    // if there are no elements to run
                    // then wait the minimum time until
                    // the synchronization object is signaled.
                    //
                    if (toRun.size() == 0) {
                        minTime -= now;
                        try {
                            if (minTime > 0)
                                m_sync.wait(minTime);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }

                } // end synchronization

                //
                // process the timeouts, if any
                //
                if (toRun.size() != 0) {
                    ListIterator<Runnable> iter = toRun.listIterator(0);
                    try {
                        while (true) {
                            Runnable runner = iter.next();
                            iter.remove();
                            runner.run();
                        }
                    } catch (NoSuchElementException err) {
                        // do nothing
                    } catch (Throwable err) {
                        //
                        // Bad, Bad Runnable!
                        //
                    }
                }

            } // end while loop

        }// end run method

    } // end inner class

    /**
     * Creates an SnmpTime object and it's internal thread that is used to
     * schedule the execution of the runnables.
     * 
     */
    SnmpTimer() {
        m_exit = false;
        m_sync = new Object();
        m_list = new LinkedList<TimeoutElement>();
        m_thread = new Thread(new Scheduler(), "SnmpTimer");

        m_thread.start();
    }

    /**
     * Schedules the runnable to be run after AT LEAST ms milliseconds of time
     * has expired. The runnable may be invoked in a delayed manner, but will
     * not be run BEFORE ms milliseconds have expired.
     * 
     * @param runner
     *            The runnable object
     * @param milliseconds
     *            The number of milliseconds to wait
     * 
     */
    void schedule(Runnable runner, long milliseconds) {
        if (runner != null) {
            synchronized (m_sync) {
                m_list.add(new TimeoutElement(milliseconds, runner));
                m_sync.notify();
            }
        }
    }

    /**
     * Cancels the current timer object and terminates the internal thread
     * 
     */
    void cancel() {
        synchronized (m_sync) {
            m_exit = true;
            m_sync.notify();
        }

        try {
            //
            // Do not allow the timer thread to join
            // itself. This will cause a deadlock
            // condition to occur!
            //
            if (m_thread.equals(Thread.currentThread()) == false)
                m_thread.join();
        } catch (InterruptedException err) {
            Thread.currentThread().interrupt();
        }
    }

} // end of class
