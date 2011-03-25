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
 * 2008 Jun 14: Improving logging and some exceptions, use Java 5
 *              generics and loops. - dj@opennms.org
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
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
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

    private OpennmsServerConfigFactory m_serverConfig;

    private XmlrpcdConfigFactory m_config;

    /**
     * <P>
     * Constructs a new Xmlrpcd object that receives events subscribed by the
     * external XMLRPC server and sends corresponding message to the external
     * XMLRPC server via XMLRPC protocol.
     */
    public Xmlrpcd() {
    	super("OpenNMS.Xmlrpcd");
    }

    /**
     * <p>onInit</p>
     */
    protected void onInit() {


        log().debug("start: Creating the xmlrpc event queue processor");

        // set up the event queue processor
        try {
            log().debug("start: Initializing the xmlrpcd config factory");

            boolean verifyServer = getServerConfig().verifyServer();
            String localServer = null;

            if (verifyServer) {
                localServer = getServerConfig().getServerName();
            }

            // create a BroadcastEventProcessor per server 
            Enumeration<ExternalServers> servers = getConfig().getExternalServerEnumeration();
            int i = 0;
            while (servers.hasMoreElements()) {
                ExternalServers server = servers.nextElement();
                XmlrpcServer[] xServers = server.getXmlrpcServer();
                FifoQueue<Event> q = new FifoQueueImpl<Event>();
                m_eventlogQs.add(q);
                m_eventReceivers.add(new BroadcastEventProcessor(
                            Integer.toString(i), q, getConfig().getMaxQueueSize(), 
                                    getConfig().getEventList(server)));

                // create an EventQueueProcessor per server 
                m_processors.add( new EventQueueProcessor(q, xServers, server.getRetries(), server.getElapseTime(), verifyServer, localServer, getConfig().getMaxQueueSize()) );
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
    public void setConfig(XmlrpcdConfigFactory config) {
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
    public void setServerConfig(OpennmsServerConfigFactory serverConfig) {
        m_serverConfig = serverConfig;
    }

    /**
     * <p>onStart</p>
     */
    protected void onStart() {
        log().debug("start: Initializing the xmlrpcd config factory");
        
        for (EventQueueProcessor proc : m_processors) {
            proc.start();
        }

        log().debug("start: xmlrpcd ready to process events");
    }

    /**
     * <p>onPause</p>
     */
    protected void onPause() {
        log().debug("Calling pause on processor");

        for (EventQueueProcessor proc : m_processors) {
            proc.pause();
        }

        log().debug("Processor paused");
    }
    
    /**
     * <p>onResume</p>
     */
    protected void onResume() {
        log().debug("Calling resume on processor");

        for (EventQueueProcessor proc : m_processors) {
            proc.resume();
        }

        log().debug("Processor resumed");
    }

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
        // shutdown and wait on the background processing thread to exit.
        log().debug("exit: closing communication paths.");

        log().debug("stop: Stopping queue processor.");

        // interrupt the processor daemon thread
        for (EventQueueProcessor proc : m_processors) {
            proc.stop();
        }
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
