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

package org.opennms.netmgt.actiond;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used as a thread for launching and executing actions as they
 * are discovered by the action daemon. The actions are read from an execution
 * queue and the processes are created by the fiber. Each created process is
 * added to garbage collection list that is periodically polled and culled based
 * upon the status of the process or how long the process is run. If the process
 * has run long than allocated it is terminated during collection.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/>OpenNMS </a>
 * 
 */
final class Executor implements Runnable, PausableFiber {
    private static final Logger LOG = LoggerFactory.getLogger(Executor.class);
    /**
     * The input queue of runnable commands.
     */
    private FifoQueue<String> m_execQ;

    /**
     * The list of outstanding commands.
     */
    private List<DatedProc> m_processes;

    /**
     * The maximum time that a command can execute.
     */
    private long m_maxWait;

    /**
     * The maximum number of outstanding processes.
     */
    private int m_maxProcCount;

    /**
     * The process garbage collection thread.
     */
    private Thread m_reaper;

    /**
     * The garbage collection instance.
     */
    private Runnable m_reaperRun;

    /**
     * The worker thread that executes the <code>run</code> method.
     */
    private Thread m_worker;

    /**
     * The name of this Fiber
     */
    private String m_name;

    /**
     * The status of this fiber.
     */
    private int m_status;

    /**
     * This class is designed to encapsulated a process and its start time. The
     * start time is based upon the system clock and the runtime is the
     * difference between the current time and the started time.
     * 
     * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
     * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
     * @author <a href="http://www.opennms.org/>OpenNMS </a>
     * 
     */
    private static final class DatedProc {
        /**
         * The executable running
         */
        private final String m_cmd;

        /**
         * The process returned from the {@link java.lang.Runtime Runtime}
         * instance.
         */
        private final Process m_proc;

        /**
         * The time the process was started.
         */
        private final long m_started;

        /**
         * Constructs a new dated process.
         * 
         * @param cmd
         *            The command used to start the process.
         * @param p
         *            The running process.
         */
        DatedProc(String cmd, Process p) {
            m_cmd = cmd;
            m_proc = p;
            m_started = System.currentTimeMillis();
        }

        /**
         * Returns the encapsulated process.
         * 
         */
        Process getProcess() {
            return m_proc;
        }

        /**
         * Returns the current runtime of the process.
         */
        long getRunTime() {
            return System.currentTimeMillis() - m_started;
        }

        /**
         * Returns the command being run by the dated process.
         */
        @Override
        public String toString() {
            return m_cmd;
        }
    } // end class DatedProc

    /**
     * This class encapsules a singular run method that is used to
     * <em>garbage collect</em> expired processes. If a process has exceeded
     * its maximum runtime then it is killed and removed from the process queue.
     * 
     * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
     * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
     * @author <a href="http://www.opennms.org/>OpenNMS </a>
     * 
     */
    private final class Reaper implements Runnable {
        /**
         * The reaper execution enviroment. This method scans the process array
         * and removes expired and completed commands from the array on a
         * periodic basis. In that respect it is a garbage collection thread for
         * processes.
         * 
         */
        @Override
        public void run() {
            // Wait for a maximum of 15 seconds between checks!
            //
            long waitPeriod = m_maxWait / 5;
            if (waitPeriod > 15000) {
                waitPeriod = 15000;
            }


            // Begin the checking process.
            //
            // Make sure to leave the 'this' keyword associated with the
            // getClass() call or jikes will complain. The 'this' keyword
            // removes all ambiguity in the call.
            //
            for (;;) {
                // run and check the queue once about
                // 1/5 of the maximum run time.
                //
                synchronized (m_processes) {
                    Iterator<DatedProc> i = m_processes.iterator();
                    while (i.hasNext()) {
                        DatedProc dp = i.next();
                        try {
                            int rc = dp.getProcess().exitValue();

                            LOG.debug("Process {} completed, rc = {}", rc, dp);

                            i.remove();
                            continue;
                        } catch (IllegalThreadStateException ex) {
                        } // still running

                        if (dp.getRunTime() > m_maxWait) {

                            LOG.info("Process {} did not complete in the alloted time, terminating.", dp);

                            dp.getProcess().destroy();
                            i.remove();
                        }
                    }
                }

                synchronized (this) {
                    // the 'this' keyword should not be removed
                    // or else jikes will complain about an ambiguous
                    // call.
                    this.notifyAll();

                    // sleep for 1/5 of wait time or
                    // 15 seconds, which ever is smaller.
                    //
                    try {
                        // the 'this' keyword should not be removed
                        // or else jikes will complain about an ambiguous
                        // call.
                        this.wait(waitPeriod);
                    } catch (InterruptedException ex) {
                        // this is used as a shutdown mechinism
                        break;
                    }
                }

            } // end for(;;)

        } // end run

    } // end class Reaper

    /**
     * <p>
     * Converts a single command to an array that can be passed to the
     * {@link java.lang.Runtime#exec(java.lang.String[]) exec}system call. The
     * element at index zero of the array is the name of the executable to run.
     * Indexs [1..length) are the arguments passed to the executable command.
     * </p>
     * 
     * <p>
     * The input command has is white space trimmed before processing. The basic
     * processing is to split on spaces, except when a double quote or single
     * quote is encountered. Also backspaces(\) should also be handled correctly
     * both in and out of the quotes. Shell escapes with <em>$</em> are not
     * supported.
     * </p>
     * 
     * @param cmd
     *            The command to split into an array.
     * 
     * @return The execution array.
     * 
     */
    private static String[] getExecArguments(String cmd) {

        // make sure we get rid of excess white space.
        //
        cmd = cmd.trim();

        // get the processing elements.
        //
        StringBuffer buf = new StringBuffer();
        List<String> args = new ArrayList<String>(5);
        char[] chars = cmd.toCharArray();

        boolean dquoted = false;
        boolean squoted = false;
        for (int x = 0; x < chars.length; x++) {
            if (chars[x] == '\\') {
                if (squoted) {
                    buf.append(chars[x]).append(chars[x + 1]);
                    x += 2;
                } else {
                    buf.append(chars[++x]);
                }
            } else if (chars[x] == '\"' && !squoted) {
                dquoted = dquoted ? false : true;
            } else if (chars[x] == '\'' && !dquoted) {
                squoted = squoted ? false : true;
            } else if (squoted || dquoted) {
                buf.append(chars[x]);
            } else if (chars[x] == ' ') {
                String arg = buf.toString().trim();

                LOG.debug("getExecArgument: adding argument: {}", arg);

                args.add(arg);
                buf.delete(0, buf.length());

                // trim off the remaining white space
                //
                while (chars[x + 1] == ' ') {
                    x++;
                }
            } else {
                buf.append(chars[x]);
            }
        }

        // Add remaining argument
        //
        if (buf.length() > 0) {
            args.add(buf.toString());
        }
        buf = null;

        // Convert to string array
        //
        String[] results = new String[args.size()];
        return args.toArray(results);
    }

    /**
     * Constructs a new action daemon execution environment. The constructor
     * takes two arguments that define the source of commands to be executed and
     * the maximum time that a command may run.
     * 
     * @param execQ
     *            The execution queue
     * @param maxRunTime
     *            The maximum runtime of a process.
     * 
     */
    Executor(FifoQueue<String> execQ, long maxRunTime, int maxProcesses) {
        m_processes = Collections.synchronizedList(new LinkedList<DatedProc>());
        m_execQ = execQ;
        m_maxWait = maxRunTime;
        m_worker = null;
        m_reaper = null;
        m_name = "Actiond-Executor";
        m_status = START_PENDING;
        m_reaperRun = null;
        m_maxProcCount = maxProcesses;
    }

    /**
     * The main worker of the fiber. This method is executed by the encapsualted
     * thread to read commands from the execution queue and to execute those
     * commands. If the thread is interrupted or the status changes to
     * <code>STOP_PENDING</code> then the method will return as quickly as
     * possible.
     */
    @Override
    public void run() {

        synchronized (this) {
            m_status = RUNNING;
        }

        for (;;) {
            synchronized (this) {
                // if stopped or stop pending then break out
                //
                if (m_status == STOP_PENDING || m_status == STOPPED) {
                    break;
                }

                // if paused or pause pending then block
                //
                while (m_status == PAUSE_PENDING || m_status == PAUSED) {
                    m_status = PAUSED;
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // exit
                        break;
                    }
                }

                // if resume pending then change to running
                //
                if (m_status == RESUME_PENDING) {
                    m_status = RUNNING;
                }
            }

            // check to see if we can execute more
            // processes. Block until we can.
            //
            if (m_maxProcCount == m_processes.size()) {

                LOG.debug("Number of processes at {} - being wait for a process to finish or be reaped!", m_maxProcCount);

                synchronized (m_reaperRun) {
                    m_reaperRun.notifyAll();
                    try {
                        m_reaperRun.wait();
                    } catch (InterruptedException ex) {
                        // exit command
                        break;
                    }
                }
                continue; // check status and count again.
            }

            // Extract the next command
            //
            String cmd = null;
            try {
                cmd = m_execQ.remove(1000);
                if (cmd == null) // status check time
                {
                    continue; // goto top of loop
                }
            } catch (InterruptedException ex) {
                break;
            } catch (FifoQueueException ex) {
                LOG.warn("The input execution queue has errors, exiting...", ex);
                break;
            }

            // start a new process
            //
            LOG.debug("Parsing cmd args: {}", cmd);

            String[] execArgs = getExecArguments(cmd);
            if (execArgs != null && execArgs.length > 0) {
                try {

                    LOG.debug("Getting ready to execute \'{}\'", cmd);

                    Process px = Runtime.getRuntime().exec(execArgs);
                    // Added by Nick Wesselman to attempt to workaround
                    // 1.4.1 JDK bug
                    // http://developer.java.sun.com/developer/bugParade/bugs/4763384.html
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        // log?
                    }
                    m_processes.add(new DatedProc(cmd, px));
                } catch (IOException ex) {
                    LOG.warn("Failed to execute command: {}", cmd, ex);
                } catch (SecurityException ex) {
                    LOG.warn("Application not authorized to exec commands!", ex);
                    break;
                }
            }

        } // end infinite loop

        synchronized (this) {
            m_status = STOPPED;
        }

    } // end run

    /**
     * Starts the fiber. If the fiber has already been run or is currently
     * running then an exception is generated. The status of the fiber is
     * updated to <code>STARTING</code> and will transisition to <code>
     * RUNNING</code>
     * when the fiber finishes initializing and begins processing the
     * encapsulaed queue.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    @Override
    public synchronized void start() {
        if (m_worker != null) {
            throw new IllegalStateException("The fiber has already be run");
        }

        m_status = STARTING;

        m_reaperRun = new Reaper();
        m_reaper = new Thread(m_reaperRun, getName() + "-Reaper");
        m_reaper.setDaemon(true);
        m_reaper.start();

        m_worker = new Thread(this, getName());
        m_worker.start();
    }

    /**
     * Stops a currently running fiber. If the fiber has already been stopped
     * then the command is silently ignored. If the fiber was never started then
     * an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber was never started.
     */
    @Override
    public synchronized void stop() {
        if (m_worker == null) {
            throw new IllegalStateException("The fiber has never been run");
        }

        if (m_status != STOPPED) {
            m_status = STOP_PENDING;
        }

        if (m_reaper.isAlive()) {
            m_reaper.interrupt();
        }

        if (m_worker.isAlive()) {
            m_worker.interrupt();
        }

        notifyAll();
    }

    /**
     * Pauses a currently running fiber. If the fiber was not in a running or
     * resuming state then the command is silently discarded. If the fiber is
     * not running or has terminated then an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    @Override
    public synchronized void pause() {
        if (m_worker == null || !m_worker.isAlive()) {
            throw new IllegalStateException("The fiber is not running");
        }

        if (m_status == RUNNING || m_status == RESUME_PENDING) {
            m_status = PAUSE_PENDING;
            notifyAll();
        }
    }

    /**
     * Resumes the fiber if it is paused. If the fiber was not in a paused or
     * pause pending state then the request is discarded. If the fiber has not
     * been started or has already stopped then an exception is generated.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber is stopped or has never run.
     */
    @Override
    public synchronized void resume() {
        if (m_worker == null || !m_worker.isAlive()) {
            throw new IllegalStateException("The fiber is not running");
        }

        if (m_status == PAUSED || m_status == PAUSE_PENDING) {
            m_status = RESUME_PENDING;
            notifyAll();
        }
    }

    /**
     * Returns the name of this fiber.
     *
     * @return The name of the fiber.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Returns the current status of the pausable fiber.
     *
     * @return The current status of the fiber.
     * @see org.opennms.core.fiber.PausableFiber
     * @see org.opennms.core.fiber.Fiber
     */
    @Override
    public synchronized int getStatus() {
        if (m_worker != null && !m_worker.isAlive()) {
            if (m_reaper.isAlive())
                m_reaper.interrupt();

            m_status = STOPPED;
        }

        return m_status;
    }
}
