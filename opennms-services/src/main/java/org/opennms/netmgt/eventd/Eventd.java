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
// 2008 Jan 17: Use JdbcTemplate for getting service name -> ID mapping. - dj@opennms.org
// 2008 Jan 06: Dependency injection of EventConfDao, delay creation of
//              BroadcastEventProcessor until onInit instead of in
//              setEventIpcManager, and pass in EventConfDao. - dj@opennms.org
// 2008 Jan 06: Indent, format code, Java 5 generics, eliminate warnings,
//              use log() from superclass. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Nov 14: Used non-blocking socket class to speed up capsd and pollerd.
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

package org.opennms.netmgt.eventd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.EventdConfigManager;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.eventd.adaptors.EventReceiver;
import org.opennms.netmgt.eventd.adaptors.tcp.TcpEventReceiver;
import org.opennms.netmgt.eventd.adaptors.udp.UdpEventReceiver;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.EventReceipt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;

/**
 * <p>
 * Eventd listens for events from the discovery, capsd, trapd processes and
 * sends events to the Master Station when queried for.
 * </p>
 * 
 * <p>
 * Eventd receives events sent in as XML, looks up the event.conf and adds
 * information to these events and stores them to the db. It also reconverts
 * them back to XML to be sent to other processes like 'actiond'
 * </p>
 * 
 * <p>
 * Process like trapd, capsd etc. that are local to the distributed poller send
 * events to the eventd. Events can also be sent via TCP or UDP to eventd.
 * </p>
 * 
 * <p>
 * Eventd listens for incoming events, loads info from the 'event.conf', adds
 * events to the database and sends the events added to the database to
 * subscribed listeners. It also maintains a servicename to serviceid mapping
 * from the services table so as to prevent a database lookup for each incoming
 * event
 * </P>
 * 
 * <P>
 * The number of threads that processes events is configurable via the eventd
 * configuration xml
 * </P>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public final class Eventd extends AbstractServiceDaemon implements org.opennms.netmgt.eventd.adaptors.EventHandler {
    private static EventIpcManager m_eventIpcManager;
    
    /**
     * The log4j category used to log debug messsages and statements.
     */
    public static final String LOG4J_CATEGORY = "OpenNMS.Eventd";

    /**
     * Singleton instance of this class
     */
    private static final Eventd m_singleton = new Eventd();

    /**
     * The service table map
     */
    private static Map<String, Integer> m_serviceTableMap;

    /**
     * The handler for events coming in through TCP
     */
    private EventReceiver m_tcpReceiver;

    /**
     * The handler for events coming in through UDP
     */
    private EventReceiver m_udpReceiver;

    /**
     * Reference to the event processor
     */
    private BroadcastEventProcessor m_receiver;

    /**
     * Contains dotted-decimal representation of the IP address where Eventd is
     * running. Used when eventd broadcasts events.
     */
    private String m_address = null;

    private EventdConfigManager m_eFactory;
    private EventDao m_eventDao;

    private DataSource m_dataSource;

    private EventConfDao m_eventConfDao;

    static {
        // map of service names to service identifer
        m_serviceTableMap = Collections.synchronizedMap(new HashMap<String, Integer>());
    }

    /**
     * Constuctor creates the localhost address(to be used eventually when
     * eventd originates events during correlation) and the broadcast queue
     */
    public Eventd() {
        super("OpenNMS.Eventd");
        try {
            m_address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhE) {
            m_address = "localhost";
            log().warn("Could not lookup the host name for the local host machine, address set to localhost", uhE);
        }


    }

    protected void onStop() {
        // Stop listener threads
        if (log().isDebugEnabled()) {
            log().debug("calling shutdown on tcp/udp listener threads");
        }

        if (m_tcpReceiver != null) {
            m_tcpReceiver.stop();
        }

        if (m_udpReceiver != null) {
            m_udpReceiver.stop();
        }

        if (m_receiver != null) {
            m_receiver.close();
        }

        if (log().isDebugEnabled()) {
            log().debug("shutdown on tcp/udp listener threads returned");
        }
    }

    protected void onInit() {
        Assert.state(m_dataSource != null, "dataSource not initialized");
        Assert.state(m_eventConfDao != null, "eventConfDao not initialized");
        
        createBroadcastEventProcessor(m_eventIpcManager);

        initializeServiceTableMap();

        initializeExternalIpcReceivers();
    }

    private void initializeServiceTableMap() {
        new JdbcTemplate(m_dataSource).query(EventdConstants.SQL_DB_SVC_TABLE_READ, new RowCallbackHandler() {
            public void processRow(ResultSet resultSet) throws SQLException {
                addServiceMapping(resultSet.getString(2), resultSet.getInt(1));
            }
        });
    }

    private void initializeExternalIpcReceivers() {
        // Create all the threads

        m_tcpReceiver = null;
        m_udpReceiver = null;
        
        try {
            // XXX this is unused, but should it be used?
            // String timeoutReq = m_eFactory.getSocketSoTimeoutRequired();
            m_tcpReceiver = new TcpEventReceiver(m_eFactory.getTCPPort());
            m_udpReceiver = new UdpEventReceiver(m_eFactory.getUDPPort());

            m_tcpReceiver.addEventHandler(this);
            m_udpReceiver.addEventHandler(this);

        } catch (IOException e) {
            log().error("Error starting up the TCP/UDP threads of eventd: " + e, e);
            throw new UndeclaredThrowableException(e);
        }
    }

    protected void onStart() {
        m_tcpReceiver.start();
        m_udpReceiver.start();

        if (log().isDebugEnabled()) {
            log().debug("Listener threads started");
        }

        if (log().isDebugEnabled()) {
            log().debug("Eventd running");
        }
    }

    private void createBroadcastEventProcessor(EventIpcManager manager) {
        try {
            if (log().isDebugEnabled()) {
                log().debug("start: Creating event broadcast event processor");
            }

            m_receiver = new BroadcastEventProcessor(manager, m_eventConfDao);
        } catch (Throwable t) {
            log().fatal("start: Failed to initialized the broadcast event receiver", t);

            throw new UndeclaredThrowableException(t);
        }
    }
    /**
     * Used to retrieve the local host address. The address of the machine on
     * which Eventd is running.
     * 
     * @return The local machines hostname.
     */
    public String getLocalHostAddress() {
        return m_address;
    }

    /**
     * Return the service id for the name passed
     * 
     * @param svcname
     *            the service name whose service id is required
     * 
     * @return the service id for the name passed, -1 if not found
     */
    public static synchronized int getServiceID(String svcname) {
        if (m_serviceTableMap.containsKey(svcname)) {
            return m_serviceTableMap.get(svcname).intValue();
        } else {
            return -1;
        }
    }

    /**
     * Add the svcname/svcid mapping to the servicetable map
     */
    public static synchronized void addServiceMapping(String svcname, int serviceid) {
        m_serviceTableMap.put(svcname, new Integer(serviceid));
    }

    public static Eventd getInstance() {
        return m_singleton;
    }

    public boolean processEvent(Event event) {
        m_eventIpcManager.sendNow(event);
        return true;
    }

    public void receiptSent(EventReceipt event) {
        // do nothing
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public void setConfigManager(EventdConfigManager configManager) {
        m_eFactory = configManager;
    }

    public EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    public void setEventIpcManager(EventIpcManager manager) {
        m_eventIpcManager = manager;
    }

    public void setEventDao(EventDao dao) {
        m_eventDao = dao;
    }

    public EventDao getEventDao() {
        return m_eventDao;
    }

    public EventConfDao getEventConfDao() {
        return m_eventConfDao;
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }
}
