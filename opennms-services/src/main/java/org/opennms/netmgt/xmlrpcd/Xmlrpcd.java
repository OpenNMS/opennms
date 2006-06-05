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
// 2004 Jan 13: Added this new code for the XML RPC Daemon
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

package org.opennms.netmgt.xmlrpcd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueImpl;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.XmlrpcdConfigFactory;
import org.opennms.netmgt.daemon.ServiceDaemon;

/**
 * <p>
 * The Xmlrpcd receives events selectively and sends notification to an external
 * XMLRPC server via the XMLRPC protocol.
 * </p>
 * 
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class Xmlrpcd extends ServiceDaemon {
    /**
     * The name of the logging category for Xmlrpcd.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Xmlrpcd";

    /**
     * The singlton instance.
     */
    private static final Xmlrpcd m_singleton = new Xmlrpcd();

    /**
     * The name of this service.
     */
    private String m_name;

    /**
     * The communication queue
     */
    private FifoQueue m_eventlogQ;

    /**
     * The queue processing thread
     */
    private EventQueueProcessor m_processor;

    /**
     * The class instance used to recieve new events from for the system.
     */
    private BroadcastEventProcessor m_eventReceiver;

    /**
     * <P>
     * Constructs a new Xmlrpcd object that receives events subscribed by the
     * external XMLRPC server and sends corresponding message to the external
     * XMLRPC server via XMLRPC protocol.
     */
    public Xmlrpcd() {
        m_name = "OpenNMS.Xmlrpcd";
    }

    public synchronized void init() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        Category log = ThreadCategory.getInstance();

        if (log.isDebugEnabled())
            log.debug("start: Creating the xmlrpc event queue processor");

        // set up the event queue processor
        m_eventlogQ = new FifoQueueImpl();

        try {
            if (log.isDebugEnabled())
                log.debug("start: Initializing the xmlrpcd config factory");

            XmlrpcdConfigFactory.reload();
            OpennmsServerConfigFactory.reload();

            XmlrpcdConfigFactory xFactory = XmlrpcdConfigFactory.getInstance();
            boolean verifyServer = OpennmsServerConfigFactory.getInstance().verifyServer();
            String localServer = null;

            if (verifyServer)
                localServer = OpennmsServerConfigFactory.getInstance().getServerName();

            Enumeration eventEnum = xFactory.getEventEnumeration();
            m_eventReceiver = new BroadcastEventProcessor(m_eventlogQ, xFactory.getMaxQueueSize(), eventEnum);

            m_processor = new EventQueueProcessor(m_eventlogQ, xFactory.getXmlrpcServer(), xFactory.getRetries(), xFactory.getElapseTime(), verifyServer, localServer, xFactory.getMaxQueueSize());

        } catch (MarshalException e) {
            log.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (Throwable t) {
            log.error("Failed to load configuration", t);
        }
    }

    /**
     * 
     * @exception java.lang.reflect.UndeclaredThrowableException
     *                if an unexpected database, or IO exception occurs.
     * 
     */
    public synchronized void start() {
        setStatus(STARTING);

        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        Category log = ThreadCategory.getInstance();

        if (log.isDebugEnabled())
            log.debug("start: Initializing the xmlrpcd config factory");

        m_processor.start();

        setStatus(RUNNING);

        if (log.isDebugEnabled())
            log.debug("start: xmlrpcd ready to process events");

    }

    /**
     * Pauses Xmlrpcd
     */
    public void pause() {
        if (!isRunning())
            return;

        setStatus(PAUSE_PENDING);

        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("Calling pause on processor");

        m_processor.pause();

        if (log.isDebugEnabled())
            log.debug("Processor paused");

        setStatus(PAUSED);

        if (log.isDebugEnabled())
            log.debug("Xmlrpcd paused");
    }

    /**
     * Resumes Xmlrpcd
     */
    public void resume() {
        if (!isPaused())
            return;

        setStatus(RESUME_PENDING);

        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("Calling resume on processor");

        m_processor.resume();

        if (log.isDebugEnabled())
            log.debug("Processor resumed");

        setStatus(RUNNING);

        if (log.isDebugEnabled())
            log.debug("Xmlrpcd resumed");
    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    public synchronized void stop() {
        Category log = ThreadCategory.getInstance(getClass());

        setStatus(STOP_PENDING);

        // shutdown and wait on the background processing thread to exit.
        if (log.isDebugEnabled())
            log.debug("exit: closing communication paths.");

        if (log.isDebugEnabled())
            log.debug("stop: Stopping queue processor.");

        // interrupt the processor daemon thread
        m_processor.stop();

        setStatus(STOPPED);

        if (log.isDebugEnabled())
            log.debug("stop: Xmlrpcd stopped");
    }

    /**
     * Returns the name of the service.
     * 
     * @return The service's name.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the singular instance of the xmlrpcd daemon. There can be only
     * one instance of this service per virtual machine.
     */
    public static Xmlrpcd getInstance() {
        return m_singleton;
    }

}
