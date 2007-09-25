//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd.adaptors.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.opennms.netmgt.xml.event.Log;

/**
 * Provides the logic and context of execution necessary to actually process a
 * client's event XML document. When a new stream handler is created and
 * assigned to an execution context it will unmarshall the remote document. The
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
     * The registered list of event handlers. Each incomming event will be
     * passed to all event handlers. The event handlers <em>MUST NOT</em>
     * modify the passed event.
     */
    private List m_handlers;

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
    TcpStreamHandler(Fiber parent, Socket sock, List handlers, int number) {
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
            Category log = log();
            if (log.isDebugEnabled())
                log.debug("Interrupting and joining the thread context " + m_context.getName());

            m_context.interrupt();
            m_context.join();

            if (log.isDebugEnabled())
                log.debug("Context stopped and joined");
        }
    }

    /**
     * The main execution context for processing a remote XML document. Once the
     * document is processed and an event receipt is returned to the client the
     * thread will exit.
     */
    public void run() {
        // get the context and stop if necessary
        //
        m_context = Thread.currentThread();
        synchronized (m_context) {
            m_context.notifyAll();
        }

        boolean isTracing = log().isDebugEnabled();

        // check the stop flag
        //
        if (m_stop) {
            if (isTracing)
                log().debug("The stop flag was set prior to thread entry, closing connection");
            try {
                m_connection.close();
            } catch (IOException e) {
                if (isTracing)
                    log().error("An error occured while closing the connection", e);
            }

            if (isTracing)
                log().debug("Thread context exiting");

            return;
        }

        // Log the startup of this stream handler
        //
        InetAddress sender = m_connection.getInetAddress();
        if (isTracing) {
            log().debug("Event Log Stream Handler Started for " + sender);
        }

        // This linked list is used to exchange
        // instances of PipedOutputStreams. Whenever a
        // pipe output stream is recovered it must be
        // signaed to inform the EOT thread of the
        // ability to write to the pipe. Also, when
        // the descriptor is close a EOFException is
        // passed on the list.
        //
        LinkedList pipeXchange = new LinkedList();
        TcpRecordHandler chunker = new TcpRecordHandler(m_connection, pipeXchange);
        Thread tchunker = new Thread(chunker, "TCPRecord Chuncker[" + m_connection.getInetAddress().getHostAddress() + ":" + m_connection.getPort() + "]");
        synchronized (tchunker) {
            tchunker.start();
            try {
                tchunker.wait();
            } catch (InterruptedException e) {
                log().error("The thread was interrupted", e);
            }
        }

        MAINLOOP: while (!m_stop && m_parent.getStatus() != Fiber.STOP_PENDING && m_parent.getStatus() != Fiber.STOPPED && m_recsPerConn != 0) {
            // get a new pipe input stream
            //
            PipedInputStream pipeIn = null;
            synchronized (pipeXchange) {
                while (pipeXchange.isEmpty()) {
                    if (chunker.isAlive()) {
                        try {
                            pipeXchange.wait(500);
                        } catch (InterruptedException e) {
                            log().error("The thread was interrupted", e);
                            break MAINLOOP;
                        }
                    } else {
                        break MAINLOOP;
                    }
                }

                // if an exception occured then just
                // exit the BAL (Big Ass Loop)
                //
                Object o = pipeXchange.removeFirst();
                if (o instanceof Throwable)
                    break MAINLOOP;

                // construct the other end of the pipe
                //
                try {
                    pipeIn = new PipedInputStream((PipedOutputStream) o);
                } catch (IOException e) {
                    log().error("An I/O exception occured construction a record reader", e);
                    break MAINLOOP;
                }

                // signal that we got the stream
                //
                synchronized (o) {
                    o.notify();
                }

            } // end synchronization on xchange

            // decrement the record count if greater than zero
            //
            m_recsPerConn -= (m_recsPerConn > 0 ? 1 : 0);

            // convert the pipe input stream into
            // and actual buffered input stream
            //
            InputStreamReader in = new InputStreamReader(new BufferedInputStream(pipeIn));

            // Unmarshall the XML document
            //
            Log eLog = null;
            boolean doCleanup = false;
            try {
                eLog = (Log) Unmarshaller.unmarshal(Log.class, in);
                if (log().isDebugEnabled())
                    log().debug("Event record converted");
            } catch (ValidationException e) {
                log().error("The XML record is not valid", new ValidationException(e.getMessage()));
                doCleanup = true;
            } catch (MarshalException e) {
                log().error("Could not unmarshall the XML record", new MarshalException(e.getMessage()));
                doCleanup = true;
            }

            // clean up the data on the current pipe if necessary
            //
            if (doCleanup) {
                // cleanup a failed record. Need to read
                // the remaining bytes from the other thread
                // to synchronize up. The other thread might
                // be blocked writing
                //
                try {
                    while (in.read() != -1)
                        /* do nothing */;
                } catch (IOException e) {
                    // do nothing
                }

                // start from the top!
                //
                continue MAINLOOP;
            }

            // Now that we have a list of events, process them
            //
            Event[] events = eLog.getEvents().getEvent();

            // sort the events by time
            //
            Arrays.sort(events, new Comparator() {
                public int compare(Object o1, Object o2) {
                    Event e1 = (Event) o1;
                    Event e2 = (Event) o2;
                    boolean e1t = (e1.getTime() != null);
                    boolean e2t = (e2.getTime() != null);
                    if (e1t && !e2t) {
                        return 1;
                    } else if (!e1t && e2t) {
                        return -1;
                    } else if (!e1t && !e2t) {
                        return 0;
                    }
                    // else
                    //
                    DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);

                    Date de1 = null;
                    try {
                        de1 = fmt.parse(e1.getTime());
                    } catch (Throwable t) {
                    }

                    Date de2 = null;
                    try {
                        de2 = fmt.parse(e2.getTime());
                    } catch (Throwable t) {
                    }

                    if (de1 != null && de2 != null)
                        return (int) (de1.getTime() - de2.getTime());
                    else if (de1 == null && de2 != null)
                        return -1;
                    else if (de1 != null && de2 == null)
                        return 1;
                    // else
                    return 0;
                }
            });

            // process the events
            //
            if (events != null && events.length != 0) {
                List okEvents = new ArrayList(events.length);

                // This synchronization loop will hold onto the lock
                // for a while. If the handlers are going to change
                // often, which is shouldn't then might want to consider
                // duplicating the handlers into an array before processing
                // the events.
                //
                // Doing the synchronization in the outer loop prevents spending
                // lots of cycles doing synchronization when it should not
                // normally
                // be necesary.
                //
                synchronized (m_handlers) {
                    Iterator iter = m_handlers.iterator();
                    while (iter.hasNext()) {
                        // get the handler and then have it process all
                        // the events in the document before moving to the
                        // next event handler.
                        //
                        EventHandler hdl = (EventHandler) iter.next();

                        for (int ndx = 0; ndx < events.length; ndx++) {
                            // Process the event and log any errors,
                            // but don't die on these errors
                            //
                            try {
                                if (isTracing)
                                    log().debug("handling event, uei = " + events[ndx].getUei());

                                // shortcut and BOTH parts MUST execute!
                                //
                                if (hdl.processEvent(events[ndx])) {
                                    if (!okEvents.contains(events[ndx])) {
                                        okEvents.add(events[ndx]);
                                    }
                                }
                            } catch (Throwable t) {
                                log().warn("An exception occured while processing an event", t);
                            }
                        }

                    } // end iteration over handler list.

                } // end synchronization

                // Now process the good events and send
                // a receipt message
                //
                boolean hasReceipt = false;
                EventReceipt receipt = new EventReceipt();
                Iterator iter = okEvents.iterator();
                while (iter.hasNext()) {
                    Event e = (Event) iter.next();
                    if (e.getUuid() != null) {
                        receipt.addUuid(e.getUuid());
                        hasReceipt = true;
                    }
                }

                if (hasReceipt) {
                    // Transform it to XML and send it across the
                    // socket all in one call
                    //
                    try {
                        OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(m_connection.getOutputStream()));
                        Marshaller.marshal(receipt, writer);
                        writer.flush();

                        synchronized (m_handlers) {
                            iter = m_handlers.iterator();
                            while (iter.hasNext()) {
                                // get the handler and then have it process all
                                // the events in the document before moving to
                                // the
                                // next event handler.
                                //
                                EventHandler hdl = (EventHandler) iter.next();
                                try {
                                    hdl.receiptSent(receipt);
                                } catch (Throwable t) {
                                    log().warn("An exception occured while processing an event receipt", t);
                                }

                            } // end iteration over handler list.

                        } // end synchronization

                        if (isTracing) {
                            log().debug("Sending Event Receipt {");
                            StringWriter swriter = new StringWriter();
                            try {
                                Marshaller.marshal(receipt, swriter);
                            } catch (Exception e) {
                                log().error("An error occured during marshalling", e);
                            }
                            log().debug(swriter.getBuffer().toString());
                            log().debug("}");
                        }
                    } catch (ValidationException e) {
                        log().warn("Failed to send event-receipt XML document", e);
                        break MAINLOOP;
                    } catch (MarshalException e) {
                        log().warn("Failed to send event-receipt XML document", e);
                        break MAINLOOP;
                    } catch (IOException e) {
                        log().warn("Failed to send event-receipt XML document", e);
                        break MAINLOOP;
                    }
                }
            } else if (isTracing) {
                log().debug("The agent sent an empty event stream");
            }

        } // end main loop

        try {
            if (isTracing)
                log().debug("stopping record handler");

            chunker.stop();

            if (isTracing)
                log().debug("record handler stopped");
        } catch (InterruptedException e) {
            log().warn("The thread was interrupted while trying to close the record handler", e);
        }

        // regardless of any errors, be sure to release the socket.
        //
        try {
            if (isTracing)
                log().debug("closing connnection");

            m_connection.close();

            if (isTracing)
                log().debug("connnection closed ");
        } catch (IOException e) {
            log().warn("An I/O exception occured while closing the TCP/IP connection", e);
        }

        if (isTracing)
            log().debug("Thread exiting");

    } // end run

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}
}
