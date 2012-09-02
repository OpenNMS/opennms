/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.EventExpander;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.eventd.BroadcastEventProcessor;
import org.opennms.netmgt.eventd.DefaultEventHandlerImpl;
import org.opennms.netmgt.eventd.EventIpcManagerDefaultImpl;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.Eventd;
import org.opennms.netmgt.eventd.JdbcEventdServiceManager;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.opennms.netmgt.eventd.adaptors.EventIpcManagerEventHandlerProxy;
import org.opennms.netmgt.eventd.adaptors.EventReceiver;
import org.opennms.netmgt.eventd.adaptors.tcp.TcpEventReceiver;
import org.opennms.netmgt.eventd.adaptors.udp.UdpEventReceiver;
import org.opennms.netmgt.eventd.processor.EventIpcBroadcastProcessor;
import org.opennms.netmgt.eventd.processor.JdbcEventWriter;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventProcessor;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public class OpenNMSTestCase extends TestCase {
    protected static MockDatabase m_db;
    protected static MockNetwork m_network;
    protected static Eventd m_eventd;
    protected static EventIpcManagerDefaultImpl m_eventdIpcMgr;

    protected static boolean m_runSupers = true;
    public static int PROXY_PORT = Integer.getInteger("proxy.port", 5837);


    /**
     * String representing snmp-config.xml
     */
    public String getSnmpConfig() throws IOException {
        return ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "/org/opennms/netmgt/mock/snmp-config.xml",
                new String[] { "\\$\\{myVersion\\}", myVersion() },
                new String[] { "\\$\\{myLocalHost\\}", InetAddressUtils.str(myLocalHost()) }
                );
    }

    private boolean m_startEventd = true;

    /**
     * Helper method for getting the ip address of the localhost as a
     * String to be used in the snmp-config.
     * @return
     */
    protected InetAddress myLocalHost() {
        
//        try {
//            return InetAddressUtils.str(InetAddress.getLocalHost());
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//            fail("Exception getting localhost");
//        }
//        
//        return null;
        
        return InetAddressUtils.getInetAddress("127.0.0.1");
    }
    
    protected String myVersion() {
        switch (m_version) {
        case SnmpAgentConfig.VERSION1 :
            return "v1";
        case SnmpAgentConfig.VERSION2C :
            return "v2c";
        case SnmpAgentConfig.VERSION3 :
            return "v3";
        default :
            return "v1";
        }
    }

    int m_version = SnmpAgentConfig.VERSION1;

    private EventProxy m_eventProxy;

    protected PlatformTransactionManager m_transMgr;
    
    public void setVersion(int version) {
        m_version = version;
    }

    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test "+this+" --------------------------");
        MockLogAppender.setupLogging();
        
        if (m_runSupers) {
        
            createMockNetwork();
            
            populateDatabase();
            
            DataSourceFactory.setInstance(m_db);

            SnmpPeerFactory.setInstance(new SnmpPeerFactory(new ByteArrayResource(getSnmpConfig().getBytes())));
            
            if (isStartEventd()) {
                m_eventdIpcMgr = new EventIpcManagerDefaultImpl();

                JdbcEventdServiceManager eventdServiceManager = new JdbcEventdServiceManager();
                eventdServiceManager.setDataSource(m_db);
                eventdServiceManager.afterPropertiesSet();

                /*
                 * Make sure we specify a full resource path since "this" is
                 * the unit test class, which is most likely in another package. 
                 */
                File configFile = ConfigurationTestUtils.getFileForResource(this, "/org/opennms/netmgt/mock/eventconf.xml");
                DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
                eventConfDao.setConfigResource(new FileSystemResource(configFile));
                eventConfDao.afterPropertiesSet();
                
                EventExpander eventExpander = new EventExpander();
                eventExpander.setEventConfDao(eventConfDao);
                eventExpander.afterPropertiesSet();

                JdbcEventWriter jdbcEventWriter = new JdbcEventWriter();
                jdbcEventWriter.setEventdServiceManager(eventdServiceManager);
                jdbcEventWriter.setDataSource(m_db);
                jdbcEventWriter.setGetNextIdString("select nextVal('eventsNxtId')"); // for HSQL: "SELECT max(eventId)+1 from events"
                jdbcEventWriter.afterPropertiesSet();
                
                EventIpcBroadcastProcessor eventIpcBroadcastProcessor = new EventIpcBroadcastProcessor();
                eventIpcBroadcastProcessor.setEventIpcBroadcaster(m_eventdIpcMgr);
                eventIpcBroadcastProcessor.afterPropertiesSet();

                List<EventProcessor> eventProcessors = new ArrayList<EventProcessor>(3);
                eventProcessors.add(eventExpander);
                eventProcessors.add(jdbcEventWriter);
                eventProcessors.add(eventIpcBroadcastProcessor);
                
                DefaultEventHandlerImpl eventHandler = new DefaultEventHandlerImpl();
                eventHandler.setEventProcessors(eventProcessors);
                eventHandler.afterPropertiesSet();
                
                m_eventdIpcMgr.setHandlerPoolSize(5);
                m_eventdIpcMgr.setEventHandler(eventHandler);
                m_eventdIpcMgr.afterPropertiesSet();
                
                m_eventProxy = m_eventdIpcMgr;
                
                EventIpcManagerFactory.setIpcManager(m_eventdIpcMgr);
                
                EventIpcManagerEventHandlerProxy proxy = new EventIpcManagerEventHandlerProxy();
                proxy.setEventIpcManager(m_eventdIpcMgr);
                proxy.afterPropertiesSet();
                List<EventHandler> eventHandlers = new ArrayList<EventHandler>(1);
                eventHandlers.add(proxy);
                
                TcpEventReceiver tcpEventReceiver = new TcpEventReceiver();
                tcpEventReceiver.setPort(5837);
                tcpEventReceiver.setEventHandlers(eventHandlers);
                
                UdpEventReceiver udpEventReceiver = new UdpEventReceiver();
                udpEventReceiver.setPort(5837);
                tcpEventReceiver.setEventHandlers(eventHandlers);
                
                List<EventReceiver> eventReceivers = new ArrayList<EventReceiver>(2);
                eventReceivers.add(tcpEventReceiver);
                eventReceivers.add(udpEventReceiver);

                m_eventd = new Eventd();
                m_eventd.setEventdServiceManager(eventdServiceManager);
                m_eventd.setEventReceivers(eventReceivers);
                m_eventd.setReceiver(new BroadcastEventProcessor(m_eventdIpcMgr, eventConfDao));
                
                m_eventd.init();
                m_eventd.start();
            }
        
        }
        
        m_transMgr = new DataSourceTransactionManager(DataSourceFactory.getInstance());

    }

    protected void populateDatabase() throws Exception {
        m_db = new MockDatabase();
        m_db.populate(m_network);
    }

    protected void createMockNetwork() {
        m_network = new MockNetwork();
        m_network.createStandardNetwork();
    }
    
    @Override
    public void runTest() throws Throwable {
        try {
            super.runTest();
            MockLogAppender.assertNoWarningsOrGreater();
        } finally {
            MockUtil.println("------------ End Test "+this+" --------------------------");
        }
    }

    protected void tearDown() throws Exception {
        if(m_runSupers) {
            if (isStartEventd()) m_eventd.stop();
        }

        super.tearDown();
    }

    protected void setStartEventd(boolean startEventd) {
        m_startEventd = startEventd;
    }

    protected boolean isStartEventd() {
        return m_startEventd;
    }

    @Test
    public void testDoNothing() { sleep(200); }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    protected EventProxy getEventProxy() {
        return m_eventProxy;
    }

    protected void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }

    public SimpleJdbcTemplate getJdbcTemplate() {
        return m_db.getJdbcTemplate();
    }

    public String toString() {
        return super.toString() + " - " + getSnmpImplementation() + " " + myVersion();
    }
    
    private static String getSnmpImplementation() {
        return SnmpUtils.getStrategy().getClass().getSimpleName();
    }

    public EventIpcManager getEventIpcManager() {
        return m_eventdIpcMgr;
    }

}
