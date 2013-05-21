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

package org.opennms.netmgt.eventd.adaptors.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.opennms.netmgt.xml.event.Log;
import org.xml.sax.InputSource;

/**
 * Provides the logic and context of execution necessary to actually process a
 * client's event XML document. When a new stream handler is created and
 * assigned to an execution context it will unmarshal the remote document. The
 * events from the remote document are then passed to the registered event
 * handlers. All successfully processed events are acknowledged to the client by
 * the generation of an XML event receipt.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http;//www.opennms.org">OpenNMS </a>
 * 
 */
final class TcpStreamHandler implements Runnable {
    /**
     * The registered list of event handlers. Each incoming event will be
     * passed to all event handlers. The event handlers <em>MUST NOT</em>
     * modify the passed event.
     */
    private List<EventHandler> m_handlers;

    /**
     * Set to stop the thread context.
     */
    private volatile boolean m_stop;

    /**
     * The parent of this stream handler.
     */
    private Fiber m_parent;

    /**
     * The socket connection to receive and process events from. The
     * successfully processed events will be acknowledged in an event-recipt
     * document.
     */
    private Socket m_connection;

    /**
     * The thread context this runnable is executing in
     */
    private Thread m_context;

    /**
     * The number of records per connection
     */
    private int m_recsPerConn;

    /**
     * Constructs a new TCP/IP stream handler to process the remote document.
     * 
     * @param parent
     *            The parent fiber
     * @param sock
     *            The socket connection
     * @param handlers
     *            The list of event handlers.
     * @param number
     *            The number of event records to process
     */
    TcpStreamHandler(Fiber parent, Socket sock, List<EventHandler> handlers, int number) {
        m_parent = parent;
        m_connection = sock;
        m_handlers = handlers;
        m_stop = false;
        m_context = null;
        m_recsPerConn = number;
    }

    /**
     * Returns true if the context is alive.
     */
    boolean isAlive() {
        boolean rc = false;
        if (m_context != null)
            rc = m_context.isAlive();

        return rc;
    }

    /**
     * Stops and joins the context.
     */
    void stop() throws InterruptedException {
        m_stop = true;
        if (m_context != null) {
        	LogUtils.debugf(this, "Interrupting and joining the thread context %s", m_context.getName());

            m_context.interrupt();
            m_context.join();

            LogUtils.debugf(this, "Context stopped and joined");
        }
    }

    /**
     * The main execution context for processing a remote XML document. Once the
     * document is processed and an event receipt is returned to the client the
     * thread will exit.
     */
    @Override
    public void run() {
        // get the context and stop if necessary
        m_context = Thread.currentThread();
        synchronized (m_context) {
            m_context.notifyAll();
        }

        // check the stop flag
        if (m_stop) {
            LogUtils.debugf(this, "The stop flag was set prior to thread entry, closing connection");
            try {
                m_connection.close();
            } catch (final IOException e) {
            	LogUtils.errorf(this, e, "An error occured while closing the connection.");
            }

            LogUtils.debugf(this, "Thread context exiting");

            return;
        }

        // Log the startup of this stream handler
        final InetAddress sender = m_connection.getInetAddress();
        LogUtils.debugf(this, "Event Log Stream Handler Started for %s", sender);

        /*
         * This linked list is used to exchange
         * instances of PipedOutputStreams. Whenever a
         * pipe output stream is recovered it must be
         * signaled to inform the EOT thread of the
         * ability to write to the pipe. Also, when
         * the descriptor is close a EOFException is
         * passed on the list.
         */
        final LinkedList<Object> pipeXchange = new LinkedList<Object>();
        final TcpRecordHandler chunker = new TcpRecordHandler(m_connection, pipeXchange);
        final Thread tchunker = new Thread(chunker, "TCPRecord Chunker[" + InetAddressUtils.str(m_connection.getInetAddress()) + ":" + m_connection.getPort() + "]");
        synchronized (tchunker) {
            tchunker.start();
            try {
                tchunker.wait();
            } catch (final InterruptedException e) {
            	LogUtils.errorf(this, e, "The thread was interrupted.");
            }
        }

        MAINLOOP: while (!m_stop && m_parent.getStatus() != Fiber.STOP_PENDING && m_parent.getStatus() != Fiber.STOPPED && m_recsPerConn != 0) {
            // get a new pipe input stream
            PipedInputStream pipeIn = null;
            synchronized (pipeXchange) {
                while (pipeXchange.isEmpty()) {
                    if (chunker.isAlive()) {
                        try {
                            pipeXchange.wait(500);
                        } catch (final InterruptedException e) {
                            LogUtils.errorf(this, e, "The thread was interrupted.");
                            break MAINLOOP;
                        }
                    } else {
                        break MAINLOOP;
                    }
                }

                // if an exception occured then just exit the BAL (Big Ass Loop)
                final Object o = pipeXchange.removeFirst();
                if (o instanceof Throwable) {
                    break MAINLOOP;
                }

                // construct the other end of the pipe
                try {
                    pipeIn = new PipedInputStream((PipedOutputStream) o);
                } catch (final IOException e) {
                    LogUtils.errorf(this, e, "An I/O exception occured construction a record reader.");
                    break MAINLOOP;
                }

                // signal that we got the stream
                synchronized (o) {
                    o.notify();
                }
            }

            // decrement the record count if greater than zero
            m_recsPerConn -= (m_recsPerConn > 0 ? 1 : 0);

            // convert the pipe input stream into a buffered input stream
            final InputStream stream = new BufferedInputStream(pipeIn);

            // Unmarshal the XML document
            Log eLog = null;
            boolean doCleanup = false;
            try {
            	eLog = JaxbUtils.unmarshal(Log.class, new InputSource(stream));
                LogUtils.debugf(this, "Event record converted");
            } catch (final Exception e) {
                LogUtils.errorf(this, e, "Could not unmarshall the XML record.");
                doCleanup = true;
            } finally {
                if (stream != null) {
                    IOUtils.closeQuietly(stream);
                }
            }

            // clean up the data on the current pipe if necessary
            if (doCleanup) {
                /*
                 * Cleanup a failed record. Need to read
                 * the remaining bytes from the other thread
                 * to synchronize up. The other thread might
                 * be blocked writing.
                 */
                try {
                    while (stream.read() != -1) {
                        /* do nothing */;
                    }
                } catch (final IOException e) {
                    // do nothing
                }

                // start from the top!
                continue MAINLOOP;
            }

            // Now that we have a list of events, process them
            final Event[] events = eLog.getEvents().getEvent();

            // sort the events by time
            Arrays.sort(events, new Comparator<Event>() {
                @Override
                public int compare(final Event e1, final Event e2) {
                    final boolean e1t = (e1.getTime() != null);
                    final boolean e2t = (e2.getTime() != null);
                    if (e1t && !e2t) {
                        return 1;
                    } else if (!e1t && e2t) {
                        return -1;
                    } else if (!e1t && !e2t) {
                        return 0;
                    }

                    final DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);

                    Date de1 = null;
                    try {
                        de1 = fmt.parse(e1.getTime());
                    } catch (final Throwable t) {
                    }

                    Date de2 = null;
                    try {
                        de2 = fmt.parse(e2.getTime());
                    } catch (final Throwable t) {
                    }

                    if (de1 != null && de2 != null) {
                        return (int) (de1.getTime() - de2.getTime());
                    } else if (de1 == null && de2 != null) {
                        return -1;
                    } else if (de1 != null && de2 == null) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            // process the events
            if (events != null && events.length != 0) {
                final List<Event> okEvents = new ArrayList<Event>(events.length);

                /*
                 * This synchronization loop will hold onto the lock
                 * for a while. If the handlers are going to change
                 * often, which is shouldn't then might want to consider
                 * duplicating the handlers into an array before processing
                 * the events.
                 *
                 * Doing the synchronization in the outer loop prevents spending
                 * lots of cycles doing synchronization when it should not
                 * normally be necesary.
                 */
                synchronized (m_handlers) {
                    for (final EventHandler hdl : m_handlers) {
                        /*
                         * get the handler and then have it process all
                         * the events in the document before moving to the
                         * next event handler.
                         */
                        for (final Event event : events) {
                            /*
                             * Process the event and log any errors,
                             *  but don't die on these errors
                             */
                            try {
                            	LogUtils.debugf(this, "handling event: %s", event);

                                // shortcut and BOTH parts MUST execute!
                                if (hdl.processEvent(event)) {
                                    if (!okEvents.contains(event)) {
                                        okEvents.add(event);
                                    }
                                }
                            } catch (final Throwable t) {
                                LogUtils.warnf(this, t, "An exception occured while processing an event.");
                            }
                        }
                    }
                }

                // Now process the good events and send a receipt message
                boolean hasReceipt = false;
                final EventReceipt receipt = new EventReceipt();
                
                for (final Event event : okEvents) {
                    if (event.getUuid() != null) {
                        receipt.addUuid(event.getUuid());
                        hasReceipt = true;
                    }
                }

                if (hasReceipt) {
                    // Transform it to XML and send it to the socket in one call
                    try {
                    	final Writer writer = new BufferedWriter(new OutputStreamWriter(m_connection.getOutputStream(), "UTF-8"));
                    	JaxbUtils.marshal(receipt, writer);
                        writer.flush();

                        synchronized (m_handlers) {
                            for (final EventHandler hdl : m_handlers) {
                                /*
                                 * Get the handler and then have it process all
                                 * the events in the document before moving to
                                 * the next event hander.
                                 */
                                try {
                                    hdl.receiptSent(receipt);
                                } catch (final Throwable t) {
                                    LogUtils.warnf(this, t, "An exception occured while processing an event receipt.");
                                }
                            }
                        }

                        if (LogUtils.isDebugEnabled(this)) {
                            try {
                            	final StringWriter swriter = new StringWriter();
                            	JaxbUtils.marshal(receipt, swriter);

                                LogUtils.debugf(this, "Sent Event Receipt {");
                                LogUtils.debugf(this, swriter.getBuffer().toString());
                                LogUtils.debugf(this, "}");
                            } catch (final Throwable e) {
                                LogUtils.errorf(this, e, "An error occured during marshalling of event receipt for the log.");
                            }
                        }
                    } catch (final IOException e) {
                        LogUtils.warnf(this, e, "Failed to send event-receipt XML document.");
                        break MAINLOOP;
                    }
                }
            } else {
                LogUtils.debugf(this, "The agent sent an empty event stream");
            }
        }

        try {
            LogUtils.debugf(this, "stopping record handler");

            chunker.stop();

            LogUtils.debugf(this, "record handler stopped");
        } catch (final InterruptedException e) {
            LogUtils.warnf(this, e, "The thread was interrupted while trying to close the record handler.");
        }

        // regardless of any errors, be sure to release the socket.
        try {
            LogUtils.debugf(this, "closing connnection");

            m_connection.close();

            LogUtils.debugf(this, "connnection closed ");
        } catch (final IOException e) {
            LogUtils.warnf(this, e, "An I/O exception occured while closing the TCP/IP connection.");
        }

        LogUtils.debugf(this, "Thread exiting");
    }
}
