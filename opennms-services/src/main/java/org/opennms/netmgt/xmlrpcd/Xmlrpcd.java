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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
public class Xmlrpcd extends AbstractServiceDaemon {
    
    public static final Logger LOG = LoggerFactory.getLogger(Xmlrpcd.class);
    
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

    private OpennmsServerConfigFactory m_serverConfig;

    private XmlrpcdConfigFactory m_config;

    /**
     * <P>
     * Constructs a new Xmlrpcd object that receives events subscribed by the
     * external XMLRPC server and sends corresponding message to the external
     * XMLRPC server via XMLRPC protocol.
     */
    public Xmlrpcd() {
    	super("xmlrpcd");
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {


        LOG.debug("start: Creating the xmlrpc event queue processor");

        // set up the event queue processor
        try {
            LOG.debug("start: Initializing the xmlrpcd config factory");

            final boolean verifyServer = getServerConfig().verifyServer();
            String localServer = null;

            if (verifyServer) {
                localServer = getServerConfig().getServerName();
            }

            // create a BroadcastEventProcessor per server 
            final Enumeration<ExternalServers> servers = getConfig().getExternalServerEnumeration();
            int i = 0;
            while (servers.hasMoreElements()) {
            	final ExternalServers server = servers.nextElement();
                final XmlrpcServer[] xServers = server.getXmlrpcServer();
                final FifoQueue<Event> q = new FifoQueueImpl<Event>();
                m_eventlogQs.add(q);
                m_eventReceivers.add(new BroadcastEventProcessor(Integer.toString(i), q, getConfig().getMaxQueueSize(), getConfig().getEventList(server)));

                // create an EventQueueProcessor per server 
                m_processors.add( new EventQueueProcessor(q, xServers, server.getRetries(), server.getElapseTime(), verifyServer, localServer, getConfig().getMaxQueueSize()) );
                i++;
            }

        } catch (final Throwable t) {
            LOG.error("Failed to load configuration", t);
            throw new UndeclaredThrowableException(t);
        }
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.XmlrpcdConfigFactory} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public XmlrpcdConfigFactory getConfig() throws MarshalException, ValidationException, IOException {
        if (m_config == null) {
            createConfig();
        }
        return m_config;
    }

    /**
     * <p>createConfig</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public void createConfig() throws MarshalException, ValidationException, IOException {
        XmlrpcdConfigFactory.init();
        setConfig(XmlrpcdConfigFactory.getInstance());
    }

    /**
     * <p>setConfig</p>
     *
     * @param config a {@link org.opennms.netmgt.config.XmlrpcdConfigFactory} object.
     */
    public void setConfig(final XmlrpcdConfigFactory config) {
        m_config = config;
    }
    
    /**
     * <p>getServerConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.OpennmsServerConfigFactory} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public OpennmsServerConfigFactory getServerConfig() throws MarshalException, ValidationException, IOException {
        if (m_serverConfig == null) {
            createServerConfig(); 
        }
        return m_serverConfig;
    }

    /**
     * <p>createServerConfig</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public void createServerConfig() throws MarshalException, ValidationException, IOException {
        OpennmsServerConfigFactory.init();
        setServerConfig(OpennmsServerConfigFactory.getInstance());
    }

    /**
     * <p>setServerConfig</p>
     *
     * @param serverConfig a {@link org.opennms.netmgt.config.OpennmsServerConfigFactory} object.
     */
    public void setServerConfig(final OpennmsServerConfigFactory serverConfig) {
        m_serverConfig = serverConfig;
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        LOG.debug("start: Initializing the xmlrpcd config factory");
        
        for (final EventQueueProcessor proc : m_processors) {
        	proc.start();
        }

        LOG.debug("start: xmlrpcd ready to process events");
    }

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
        LOG.debug("pause: Calling pause on processor");

        for (final EventQueueProcessor proc : m_processors) {
            proc.pause();
        }

        LOG.debug("pause: Processor paused");
    }
    
    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
        LOG.debug("resume: Calling resume on processor");

        for (final EventQueueProcessor proc : m_processors) {
            proc.resume();
        }

        LOG.debug("resume: Processor resumed");
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        // shutdown and wait on the background processing thread to exit.
    	// LogUtils.debugf(this, "exit: closing communication paths.");

        LOG.debug("stop: Calling stop on processor");

        // interrupt the processor daemon thread
        for (final EventQueueProcessor proc : m_processors) {
            proc.stop();
        }
        
        LOG.debug("stop: Processor stopped");
    }

    /**
     * Returns the singular instance of the xmlrpcd daemon. There can be only
     * one instance of this service per virtual machine.
     *
     * @return a {@link org.opennms.netmgt.daemon.AbstractServiceDaemon} object.
     */
    public static AbstractServiceDaemon getInstance() {
        return m_singleton;
    }

}
