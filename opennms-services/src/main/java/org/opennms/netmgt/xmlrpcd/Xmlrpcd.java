/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 13, 2004
 * 
 *
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

package org.opennms.netmgt.xmlrpcd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Enumeration;
import java.util.ArrayList;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.XmlrpcdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.xml.event.Event;

import org.opennms.netmgt.config.xmlrpcd.XmlrpcServer;
import org.opennms.netmgt.config.xmlrpcd.ExternalServers;
/**
 * <p>
 * The Xmlrpcd receives events selectively and sends notification to an external
 * XMLRPC server via the XMLRPC protocol.
 * </p>
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class Xmlrpcd extends AbstractServiceDaemon {

	/**
     * The singleton instance.
     */
    private static final AbstractServiceDaemon m_singleton = new Xmlrpcd();

    /**
     * The communication queues -- ArrayList of FifoQueues
     */
    private ArrayList<FifoQueue<Event>> m_eventlogQs = new ArrayList<FifoQueue<Event>>();

    /**
     * The queue processing threads -- ArrayList of EventQueueProcessors
     */
    private ArrayList<EventQueueProcessor> m_processors = new ArrayList<EventQueueProcessor>();

    /**
     * The class instance used to receive new events from for the system.
     *  -- ArrayList of BroadcastEventProcessors
     */
    private ArrayList<BroadcastEventProcessor> m_eventReceivers = new ArrayList<BroadcastEventProcessor>();

    /**
     * <P>
     * Constructs a new Xmlrpcd object that receives events subscribed by the
     * external XMLRPC server and sends corresponding message to the external
     * XMLRPC server via XMLRPC protocol.
     */
    public Xmlrpcd() {
    	super("OpenNMS.Xmlrpcd");
    }

    protected void onInit() {


        if (log().isDebugEnabled())
            log().debug("start: Creating the xmlrpc event queue processor");

        // set up the event queue processor
        try {
            if (log().isDebugEnabled())
                log().debug("start: Initializing the xmlrpcd config factory");

            XmlrpcdConfigFactory.reload();
            OpennmsServerConfigFactory.reload();

            XmlrpcdConfigFactory xFactory = XmlrpcdConfigFactory.getInstance();
            boolean verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();
            String localServer = null;

            if (verifyServer)
                localServer = OpennmsServerConfigFactory.getInstance().getServerName();

            // create a BroadcastEventProcessor per server 
            Enumeration<ExternalServers> servers = xFactory.getExternalServerEnumeration();
            int i = 0;
            while (servers.hasMoreElements()) {
                ExternalServers server = servers.nextElement();
                XmlrpcServer[] xServers = server.getXmlrpcServer();
                FifoQueue<Event> q = new FifoQueueImpl<Event>();
                m_eventlogQs.add(q);
                m_eventReceivers.add(new BroadcastEventProcessor(
                            Integer.toString(i), q, xFactory.getMaxQueueSize(), 
                                    xFactory.getEventList(server)));

                // create an EventQueueProcessor per server 
                m_processors.add( new EventQueueProcessor(q, xServers, server.getRetries(), server.getElapseTime(), verifyServer, localServer, xFactory.getMaxQueueSize()) );
                i++;
            }

        } catch (MarshalException e) {
            log().error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log().error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (Throwable t) {
            log().error("Failed to load configuration", t);
        }
    }

    protected void onStart() {
		if (log().isDebugEnabled())
            log().debug("start: Initializing the xmlrpcd config factory");

        
        for (int i = 0; i < m_processors.size(); i++) {
            EventQueueProcessor proc = m_processors.get(i);
            proc.start();
        }

        if (log().isDebugEnabled())
            log().debug("start: xmlrpcd ready to process events");
	}

    protected void onPause() {
		if (log().isDebugEnabled())
            log().debug("Calling pause on processor");

        for (int i = 0; i < m_processors.size(); i++) {
            EventQueueProcessor proc = m_processors.get(i);
            proc.pause();
        }

        if (log().isDebugEnabled())
            log().debug("Processor paused");
	}

    protected void onResume() {
		if (log().isDebugEnabled())
            log().debug("Calling resume on processor");

        for (int i = 0; i < m_processors.size(); i++) {
            EventQueueProcessor proc = m_processors.get(i);
            proc.resume();
        }

        if (log().isDebugEnabled())
            log().debug("Processor resumed");
	}

    protected void onStop() {
		// shutdown and wait on the background processing thread to exit.
        if (log().isDebugEnabled())
            log().debug("exit: closing communication paths.");

        if (log().isDebugEnabled())
            log().debug("stop: Stopping queue processor.");

        // interrupt the processor daemon thread
        for (int i = 0; i < m_processors.size(); i++) {
            EventQueueProcessor proc = m_processors.get(i);
            proc.stop();
        }
	}

    /**
     * Returns the singular instance of the xmlrpcd daemon. There can be only
     * one instance of this service per virtual machine.
     */
    public static AbstractServiceDaemon getInstance() {
        return m_singleton;
    }

}
