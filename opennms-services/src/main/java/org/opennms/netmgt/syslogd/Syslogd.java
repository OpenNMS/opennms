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

package org.opennms.netmgt.syslogd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.daemon.ServiceDaemon;
import org.opennms.netmgt.dao.EventDao;

/**
 * The received messages are converted into XML and sent to eventd
 * </p>
 * 
 * <p>
 * <strong>Note: </strong>Syslogd is a PausableFiber so as to receive control
 * events. However, a 'pause' on Syslogd has no impact on the receiving and
 * processing of traps
 * </p>
 * 
 * 
 */
public class Syslogd extends ServiceDaemon implements PausableFiber {
    /**
     * The name of the logging category for Trapd.
     */
    static final String LOG4J_CATEGORY = "OpenNMS.Syslogd";

    /**
     * The singlton instance.
     */
    private static final Syslogd m_singleton = new Syslogd();

    private String m_name = LOG4J_CATEGORY;

    public synchronized static Syslogd getSingleton() {
        return m_singleton;
    }

    private SyslogHandler m_udpEventReceiver;

    private EventDao m_eventDao;
    
    public Syslogd() {
    	setStatus(START_PENDING);
    }
    
    /**
     * <P>
     * Constructs a new Trapd object that receives and forwards trap messages
     * via JSDT. The session is initialized with the default client name of <EM>
     * OpenNMS.trapd</EM>. The trap session is started on the default port, as
     * defined by the SNMP libarary.
     * </P>
     * 
     * @see org.opennms.protocols.snmp.SyslogMessageSession
     */
    
    public synchronized void init() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        Category log = log();

        try {
            log.debug("start: Initializing the syslogd config factory");
            SyslogdConfigFactory.init();
        } catch (MarshalException e) {
            log.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log.error("Failed to load configuration", e);
            throw new UndeclaredThrowableException(e);
        }

        try {
            // clear out the known nodes
            SyslogdIPMgr.dataSourceSync();
        } catch (SQLException e) {
            log.error("Failed to load known IP address list", e);
            throw new UndeclaredThrowableException(e);
        }

          
        SyslogHandler.setSyslogConfig(SyslogdConfigFactory.getInstance());
        log.debug("Starting SyslogProcessor");
          
        m_udpEventReceiver = new SyslogHandler();

    }

    /**
     * Create the Syslogd session and create the JSDT communication channel to
     * communicate with eventd.
     * 
     * @exception java.lang.reflect.UndeclaredThrowableException
     *                if an unexpected database, or IO exception occurs.
     * 
     * @see org.opennms.protocols.snmp.SyslogMessageSession
     * @see org.opennms.protocols.snmp.SnmpTrapHandler
     */
    public synchronized void start() {
    	setStatus(STARTING);
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        Category log = ThreadCategory.getInstance(getClass());

        m_udpEventReceiver.start();

        setStatus(RUNNING);

        if (log.isDebugEnabled()) {
            log.debug("Listener threads started");
        }

    }

    /**
     * Pauses Syslogd
     */
    public void pause() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        if (!isRunning()) {
            return;
        }
        Category log = ThreadCategory.getInstance();
        setStatus(PAUSE_PENDING);

        log.debug("Processor paused");

        setStatus(PAUSED);

        log.debug("Syslogd paused");

    }

   
    public void resume() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        if (!isPaused()) {
            return;
        }

        setStatus(RESUME_PENDING);

        Category log = log();
        
        setStatus(RUNNING);

        log.debug("Syslogd resumed");

    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    public synchronized void stop() {
        // Set the category prefix
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = log();

        setStatus(STOP_PENDING);

        // shutdown and wait on the background processing thread to exit.
        log.debug("exit: closing communication paths.");

        try {
            log.debug("stop: Closing SYSLOGD message session.");

            log.debug("stop: Syslog message session closed.");
        } catch (IllegalStateException e) {
            log.debug("stop: The Syslog session was already closed");
        }

        log.debug("stop: Stopping queue processor.");

        m_udpEventReceiver.stop();
        log.debug("Stopped the UDP Receiver on port 514");

        setStatus(STOPPED);
        log.debug("stop: Syslogd stopped");

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
     * Returns the singular instance of the syslogd daemon. There can be only
     * one instance of this service per virtual machine.
     */
    public static Syslogd getInstance() {
        return m_singleton;
    }

    public EventDao getEventDao() {
        return m_eventDao;
    }

    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }

}
