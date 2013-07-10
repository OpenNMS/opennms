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

package org.opennms.netmgt.xmlrpcd;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.XmlrpcdConfigFactory;
import org.opennms.netmgt.config.xmlrpcd.XmlrpcServer;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * The EventQueueProcessor processes the events received by xmlrpcd and sends
 * notifications to the external XMLRPC server via XMLRPC protocol.
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * 
 */
class EventQueueProcessor implements Runnable, PausableFiber {
    private static final Logger LOG = LoggerFactory.getLogger(EventQueueProcessor.class);
    /**
     * The input queue
     */
    private FifoQueue<Event> m_eventQ;

    /**
     * The max size of the event queue
     */
    private int m_maxQSize;

    /**
     * An object used to communicate with exteranl xmlrpc servers
     */
    private XmlRpcNotifier m_notifier;

    /**
     * Current status of the fiber
     */
    private int m_status;

    /**
     * The thread that is executing the <code>run</code> method on behalf of
     * the fiber.
     */
    private Thread m_worker;

    /**
     * Use generic messages flag -- based on a setting in the config file,
     *  if this flags is true, then we will send all events with the sendEvent
     *  RPC call.  If it's false, we'll use the backward-compatible 6 specific
     *  event RPC calls.
     */
    private boolean m_useGenericMessages;

    /**
     * The constructor
     */
    EventQueueProcessor(final FifoQueue<Event> eventQ, final XmlrpcServer[] rpcServers, final int retries, final int elapseTime, final boolean verifyServer, final String localServer, final int maxQSize) {
        m_eventQ = eventQ;
        m_maxQSize = maxQSize;
        m_notifier = new XmlRpcNotifier(rpcServers, retries, elapseTime, verifyServer, localServer);
        m_useGenericMessages = XmlrpcdConfigFactory.getInstance().getConfiguration().getGenericMsgs();
    }

    private void processEvent(final Event event) {
    	final String uei = event.getUei();
        if (uei == null) {
            LOG.debug("Event received with null UEI, ignoring event");
            return;
        }

        LOG.debug("About to process event: {}", event.getUei());

        LOG.debug(event.toString());

        if (m_useGenericMessages) {
            // new single RPC for all events (subject to config uei filter)
            if (!m_notifier.sendEvent(event)) {
                pushBackEvent(event);
            }
        } else {
            /*
             * original specific RPC calls -- limits us to exporting a max of
             * 6 specific events
             */
            if (uei.equals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)) {
                if (!m_notifier.sendServiceDownEvent(event)) {
                    pushBackEvent(event);
                }
            } else if (uei.equals(EventConstants.INTERFACE_DOWN_EVENT_UEI)) {
                if (!m_notifier.sendInterfaceDownEvent(event)) {
                    pushBackEvent(event);
                }
            } else if (uei.equals(EventConstants.NODE_DOWN_EVENT_UEI)) {
                if (!m_notifier.sendNodeDownEvent(event)) {
                    pushBackEvent(event);
                }
            } else if (uei.equals(EventConstants.NODE_UP_EVENT_UEI)) {
                if (!m_notifier.sendNodeUpEvent(event)) {
                    pushBackEvent(event);
                }
            } else if (uei.equals(EventConstants.INTERFACE_UP_EVENT_UEI)) {
                if (!m_notifier.sendInterfaceUpEvent(event)) {
                    pushBackEvent(event);
                }
            } else if (uei.equals(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)) {
                if (!m_notifier.sendServiceUpEvent(event)) {
                    pushBackEvent(event);
                }
            } 
        }

        if (uei.equals(EventConstants.XMLRPC_NOTIFICATION_EVENT_UEI)) {
            xmlrpcNotificationEventHandler(event);
        }
    }

	/**
     * Process xmlrpcNotificationEvent according the status flag to determine to
     * send a notifyReceivedEvent, or a notifySuccess, or a notifyFailure
     * notification to XMLRPC Server.
     */
    private void xmlrpcNotificationEventHandler(final Event event) {
        long txNo = -1L;
        String sourceUei = null;
        String notification = null;
        int status = -1;

        String parmName = null;
        Value parmValue = null;
        String parmContent = null;

        for (Parm parm : event.getParmCollection()) {
            parmName = parm.getParmName();
            parmValue = parm.getValue();
            if (parmValue == null) {
                continue;
            } else {
                parmContent = parmValue.getContent();
            }

            LOG.debug("ParmName: {} /parmContent: ", parmName, parmContent);

            // get txNo
            if (parmName.equals(EventConstants.PARM_TRANSACTION_NO)) {
                final String temp = parmContent;

                try {
                    txNo = Long.valueOf(temp).longValue();
                } catch (final NumberFormatException nfe) {
                    LOG.warn("Parameter {} cannot be non-numeric", EventConstants.PARM_TRANSACTION_NO, nfe);
                    txNo = -1L;
                }
            } else if (parmName.equals(EventConstants.PARM_SOURCE_EVENT_UEI)) {
                sourceUei = parmContent;
            } else if (parmName.equals(EventConstants.PARM_SOURCE_EVENT_MESSAGE)) {
                notification = parmContent;
            } else if (parmName.equals(EventConstants.PARM_SOURCE_EVENT_STATUS)) {
                String temp = parmContent;
                try {
                    status = Integer.valueOf(temp).intValue();
                } catch (final NumberFormatException nfe) {
                    LOG.warn("Parameter {} cannot be non-numeric", EventConstants.PARM_SOURCE_EVENT_STATUS, nfe);
                    status = -1;
                }
            }
        }

        final boolean validParameters = (txNo != -1L) && (sourceUei != null) && (notification != null) && (status != -1);
        if (!validParameters) {
            LOG.error("Invalid parameters.");
            return;
        }

        switch (status) {
	        case EventConstants.XMLRPC_NOTIFY_RECEIVED:
	            if (!m_notifier.notifyReceivedEvent(txNo, sourceUei, notification)) {
	                pushBackEvent(event);
	            }
	            break;
	        case EventConstants.XMLRPC_NOTIFY_SUCCESS:
	            if (!m_notifier.notifySuccess(txNo, sourceUei, notification)) {
	                pushBackEvent(event);
	            }
	            break;
	        case EventConstants.XMLRPC_NOTIFY_FAILURE:
	            if (!m_notifier.notifyFailure(txNo, sourceUei, notification)) {
	                pushBackEvent(event);
	            }
        }
    }

    /**
     * Push the event back to the event queue if OpenNMS failed to send message
     * to the external XMLRPC server, so that an attempt to send to the server
     * can be made again later.
     */
    private void pushBackEvent(final Event event) {
        // push the event back to the event queue
        try {
            if (m_eventQ.size() < m_maxQSize) {
                m_eventQ.add(event);
                LOG.debug("Push the event back to queue.");
            }

            // re-establish connection to xmlrpc servers
            m_notifier.createConnection();
        } catch (final FifoQueueException e) {
            LOG.error("Failed to push the event back to queue", e);
        } catch (final InterruptedException e) {
            LOG.error("Failed to push the event back to queue", e);
        }
    }

    /**
     * Returns true if the status is ok and the thread should continue running.
     * If the status returend is false then the thread should exit.
     * 
     */
    private synchronized boolean statusOK() {
        boolean exitThread = false;
        boolean exitCheck = false;
        
        // Loop until there is a new client or we are shutdown
        while (!exitCheck) {
            // check the child thread!
            if (m_worker.isAlive() == false && m_status != STOP_PENDING) {
		LOG.warn("{} terminated abnormally", getName());
                m_status = STOP_PENDING;
            }

            // do normal status checks now
            if (m_status == STOP_PENDING) {
                exitCheck = true;
                exitThread = true;
                m_status = STOPPED;
            } else if (m_status == PAUSE_PENDING) {
                pause();
            } else if (m_status == RESUME_PENDING) {
                resume();
            } else if (m_status == PAUSED) {
                try {
                    wait();
                } catch (final InterruptedException e) {
                    m_status = STOP_PENDING;
                }
            } else if (m_status == RUNNING) {
                exitCheck = true;
            }

        } // end !exit check

        return !exitThread;

    } // statusOK

    /**
     * Starts the current fiber. If the fiber has already been started,
     * regardless of it's current state, then an IllegalStateException is
     * thrown.
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber has already been started.
     */
    @Override
    public synchronized void start() {
        if (m_worker != null) {
            throw new IllegalStateException("The fiber is running or has already run");
        }

        m_status = STARTING;

        m_worker = new Thread(this, getName());
        m_worker.start();

        LOG.info("{} started", getName());
    }

    /**
     * Pauses the current fiber.
     */
    @Override
    public synchronized void pause() {
        if (m_worker == null || m_worker.isAlive() == false) {
            throw new IllegalStateException("The fiber is not running");
        }

        m_status = PAUSED;
        notifyAll();
    }

    /**
     * Resumes the currently paused fiber.
     */
    @Override
    public synchronized void resume() {
        if (m_worker == null || m_worker.isAlive() == false) {
            throw new IllegalStateException("The fiber is not running");
        }

        m_status = RUNNING;
        notifyAll();
    }

    /**
     * <p>
     * Stops this fiber. If the fiber has never been started then an
     * <code>IllegalStateExceptio</code> is generated.
     * </p>
     *
     * @throws java.lang.IllegalStateException
     *             Thrown if the fiber has never been started.
     */
    @Override
    public synchronized void stop() {
        if (m_worker == null) {
            throw new IllegalStateException("The fiber has never run");
        }

        m_status = STOP_PENDING;
        m_worker.interrupt();
        notifyAll();
    }

    /**
     * Returns the name of the fiber.
     *
     * @return The name of the Fiber.
     */
    @Override
    public String getName() {
        return "EventQueueProcessor";
    }

    /**
     * Returns the current status of the fiber
     *
     * @return The status of the Fiber.
     */
    @Override
    public synchronized int getStatus() {
        if (m_worker != null && !m_worker.isAlive()) {
            m_status = STOPPED;
        }

        return m_status;
    }

    /**
     * Reads off of the event queue and depends on the uei of the event of read,
     * process the event to send a notification to the external XMLRPC server
     * via XMLRPC protocol.
     */
    @Override
    public void run() {
        synchronized (this) {
            m_status = RUNNING;
        }

        while (statusOK()) {
            Event event = null;
            try {
                event = m_eventQ.remove(1000);
            } catch (final InterruptedException iE) {
		LOG.debug("Caught interrupted exception, transitioning to STOP_PENDING status", iE);

                event = null;

                m_status = STOP_PENDING;
            } catch (final FifoQueueException qE) {
		LOG.debug("Caught FIFO queue exception.", qE);

                event = null;

                m_status = STOP_PENDING;
            }

            if (event != null && statusOK()) {
                try {
                    processEvent(event);
                } catch (Throwable t) {
                    LOG.error("Unexpected error processing event.", t);
                }
            }
            if (event != null && !statusOK()) {
		LOG.error("EventQueueProcessor not OK, exiting with status: {}", m_status);
            }
        }
    }
}
