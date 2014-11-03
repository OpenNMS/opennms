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

package org.opennms.netmgt.eventd.adaptors.tcp;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to do the initial read of data from the input stream and
 * break it up into records. Each record is written to a piped writer. This
 * means that the reader never gets too far ahead of the parse. It means more
 * threads for less memory usage. As always there is a tradeoff.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http;//www.opennms.org">OpenNMS </a>
 * 
 */
final class TcpRecordHandler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(TcpRecordHandler.class);
    /**
     * When set the runnable should exit as fast as possible.
     */
    private volatile boolean m_stop;

    /**
     * The thread context running this runnable
     */
    private Thread m_context;

    /**
     * The list of piped output streams and execptions.
     */
    private List<Object> m_xchange;

    /**
     * The input stream socket
     */
    private Socket m_connection;

    /**
     * The current pipe.
     */
    private OutputStream m_out;

    /**
     * The set of state managers
     */
    private StateManager[] m_tokenizer;

    /**
     * This class is use to model the set of states, the attached
     * TcpRecordHandler, and the transition actions.
     */
    private static class StateManager {
        /**
         * The level of this manager
         */
        protected int m_level;

        /**
         * The record handler this manager is attached to
         */
        protected TcpRecordHandler m_handler;

        /**
         * Constructs a new state manager.
         * 
         * @param level
         *            The level of the state manager
         * @param handler
         *            The handler to fire events at.
         */
        StateManager(final int level, final TcpRecordHandler handler) {
            m_level = level;
            m_handler = handler;
        }

        /**
         * The level for this manager
         */
        @SuppressWarnings("unused")
        int getLevel() {
            return m_level;
        }

        /**
         * handle the next character, returns the next level
         */
        int next(final char ch) throws IOException {
            onTransition(ch);
            return m_level;
        }

        /**
         * Handle the transition from character to character.
         */
        void onTransition(final char ch) throws IOException {
            m_handler.forward(ch);
        }
    }

    /**
     * Closes the current stream if any
     */
    private void closeStream() throws IOException {
        // close the current output stream
        if (m_out != null) {
            m_out.close();
        }
        m_out = null;
    }

    /**
     * Allocates a new stream
     */
    private void newStream() throws IOException {
        LOG.debug("Opening new PipedOutputStream and adding it to the queue");

        // create a new piped writer
        final PipedOutputStream pipeOut = new PipedOutputStream();
        try {
            synchronized (pipeOut) {
                synchronized (m_xchange) {
                    m_xchange.add(pipeOut);
                    m_xchange.notify();
                }
                LOG.debug("Added pipe to the xchange list");

                pipeOut.wait();

                LOG.debug("Pipe Signaled");
            }
        } catch (final InterruptedException e) {
		LOG.debug("An I/O error occured.", e);
            throw new IOException("The thread was interrupted");
        }

        LOG.debug("PipedOutputStream connected");

        m_out = pipeOut;
    }

    /**
     * forwards the characters to the attached pipe.
     */
    private void forward(final char ch) throws IOException {
        try {
            if (m_out != null) {
                m_out.write((int) ch);
            }
        } catch (final IOException e) {
		LOG.debug("An I/O error occured.", e);
            throw e;
        }
    }

    /**
     * Constructs a new record handler.
     * 
     * @param s
     *            The socket to read from
     * @param xchange
     *            The io exchange
     */
    TcpRecordHandler(final Socket s, final List<Object> xchange) {
        m_stop = false;
        m_context = null;
        m_xchange = xchange;
        m_connection = s;

        // looks for '</([a-zA-Z0-9]+:)?log>'
        m_tokenizer = new StateManager[] { new StateManager(0, this) {
            @Override
            int next(final char ch) throws IOException {
                onTransition(ch);
                if (ch == '<') {
                    return 1;
                }
                return m_level;
            }
        }, new StateManager(1, this) {
            @Override
            int next(final char ch) throws IOException {
                onTransition(ch);
                if (ch == '/') {
                    return 2;
                }
                return 0;
            }
        }, new StateManager(2, this) {
            @Override
            int next(final char ch) throws IOException {
                onTransition(ch);
                if (ch == 'l') {
                    return 5;
                } else if (Character.isLetterOrDigit(ch)){
                    return 3;
                }
                return 0;
            }
        }, new StateManager(3, this) {
            @Override
            int next(final char ch) throws IOException {
                onTransition(ch);
                if (ch == ':') {
                    return 4;
                } else if (Character.isLetterOrDigit(ch)) {
                    return m_level;
                }
                return 0;
            }
        }, new StateManager(4, this) {
            @Override
            int next(final char ch) throws IOException {
                onTransition(ch);
                if (ch == 'l') {
                    return 5;
                }
                return 0;
            }
        }, new StateManager(5, this) {
            @Override
            int next(final char ch) throws IOException {
                onTransition(ch);
                if (ch == 'o') {
                    return 6;
                }
                return 0;
            }
        }, new StateManager(6, this) {
            @Override
            int next(final char ch) throws IOException {
                onTransition(ch);
                if (ch == 'g') {
                    return 7;
                }
                return 0;
            }
        }, new StateManager(7, this) {
            @Override
            int next(final char ch) throws IOException {
                onTransition(ch);
                if (ch == '>') {
                    m_handler.closeStream();
                    return 8;
                }
                return 0;
            }
        },
        
        // The state tree starts here!
        new StateManager(8, this) { // gobbles up white space after
            // record
            @Override
            int next(final char ch) throws IOException {
                if (ch == '<') {
                    onTransition(ch);
                    return 1;
                } // else discard
                return m_level;
            }

            @Override
            void onTransition(final char ch) throws IOException {
                m_handler.newStream();
                super.onTransition(ch);
            }
        } };
    }

    /**
     * Returns true if the context is alive
     */
    boolean isAlive() {
        if (m_context != null) {
            return m_context.isAlive();
        } else {
            return false;
        }
    }

    /**
     * Stops and joins the current context.
     */
    void stop() throws InterruptedException {
        m_stop = true;
        if (m_context != null) {
		LOG.debug("Interrupting thread {}", m_context.getName());
            m_context.interrupt();
            LOG.debug("Joining Thread {}", m_context.getName());
            m_context.join();
            LOG.debug("Thread {} Joined", m_context.getName());
        }
    }

    /**
     * The execution context.
     */
    @Override
    public void run() {
        // get the thread context right off
        m_context = Thread.currentThread();
        synchronized (m_context) {
            m_context.notifyAll();
        }

        /*
         * Check the stop flag, if it is set then go a head and exit
         * before doing any work on the socket
         */
        if (m_stop) {
            LOG.debug("Stop flag set before thread startup, thread exiting");

            return;
        } else {
            LOG.debug("Thread started, remote is {}", InetAddressUtils.str(m_connection.getInetAddress()));
        }

        // get the input stream
        InputStream socketIn = null;
        try {
            m_connection.setSoTimeout(500); // needed in case connection closed!
            socketIn = new BufferedInputStream(m_connection.getInputStream());
        } catch (final IOException e) {
            if (!m_stop) {
                LOG.warn("An I/O Exception occured.", e);
            }
            m_xchange.add(e);

            LOG.debug("Thread exiting due to socket exception, stop flag = {}", Boolean.valueOf(m_stop));

            return;
        }

        int level = 8;
        int ch = 0;
        boolean moreInput = true;
        while (moreInput) {
            // check to see if the thread is interrupted
            if (Thread.interrupted()) {
                LOG.debug("Thread Interrupted");
                break;
            }

            try {
                ch = socketIn.read();
                if (ch == -1) {
                    moreInput = false;
                    continue;
                }

            } catch (final InterruptedIOException e) {
                // this was expected
                continue;
            } catch (final EOFException e) {
                m_xchange.add(e);
                moreInput = false;
                continue;
            } catch (final IOException e) {
                m_xchange.add(e);
                if (!m_stop) {
                    LOG.warn("An I/O error occured reading from the remote host.", e);
                }
                moreInput = false;
                continue;
            }

            try {
                level = m_tokenizer[level].next((char) ch);
            } catch (final IOException e) {
                if (!m_stop) {
                    LOG.warn("An I/O error occured writing to the processor stream.", e);
                    LOG.warn("Discarding the remainder of the event contents");
                    try {
                        /*
                         * this will discard current stream
                         * and cause all forwards to be discarded.
                         */
                        closeStream();
                    } catch (final IOException e2) {
                    }
                } else {
                    m_xchange.add(e);
                    moreInput = false;
                }
            }
        }

        // ensure that the receiver knows that no new element is coming!
        try {
            if (m_out != null) {
                m_out.close();
            }
        } catch (final IOException e) {
            if (!m_stop) {
                LOG.warn("An I/O Error occured closing the processor stream.", e);
            }
        }

        m_xchange.add(new EOFException("No More Input"));

        LOG.debug("Thread Terminated");

    }
}
