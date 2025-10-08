/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.mock;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.EventConfUtil;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.mock.JdbcEventdServiceManager;
import org.opennms.netmgt.eventd.AbstractEventUtil;
import org.opennms.netmgt.eventd.BroadcastEventProcessor;
import org.opennms.netmgt.eventd.DefaultEventHandlerImpl;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.eventd.EventIpcManagerDefaultImpl;
import org.opennms.netmgt.eventd.Eventd;
import org.opennms.netmgt.eventd.adaptors.EventHandler;
import org.opennms.netmgt.eventd.adaptors.EventIpcManagerEventHandlerProxy;
import org.opennms.netmgt.eventd.processor.EventIpcBroadcastProcessor;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.codahale.metrics.MetricRegistry;

/**
 * @deprecated Please develop new unit tests by using the Spring unit test
 * framework instead of this base class.
 */
public class OpenNMSITCase {
    protected static MockDatabase m_db;
    protected static MockNetwork m_network;
    protected static Eventd m_eventd;
    protected static EventIpcManagerDefaultImpl m_eventdIpcMgr;

    protected static boolean m_allowWarnings = false;
    protected static boolean m_runSupers = true;
    public static int PROXY_PORT = SystemProperties.getInteger("proxy.port", 5837);


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
    
    private MetricRegistry m_registry = new MetricRegistry();

    public void setVersion(int version) {
        m_version = version;
    }

    @Before
    public void setUp() throws Exception {
        MockUtil.println("------------ Begin Test "+this+" --------------------------");
        MockLogAppender.setupLogging();
        
        if (m_runSupers) {
        
            createMockNetwork();
            
            populateDatabase();
            
            DataSourceFactory.setInstance(m_db);

            SnmpPeerFactory.setInstance(new SnmpPeerFactory(new ByteArrayResource(getSnmpConfig().getBytes())));
            
            if (isStartEventd()) {
                m_eventdIpcMgr = new EventIpcManagerDefaultImpl(m_registry);

                EventUtilJdbcImpl eventUtil = new EventUtilJdbcImpl();
                AbstractEventUtil.setInstance(eventUtil);

                JdbcEventdServiceManager eventdServiceManager = new JdbcEventdServiceManager();
                eventdServiceManager.setDataSource(m_db);
                eventdServiceManager.afterPropertiesSet();

                /*
                 * Make sure we specify a full resource path since "this" is
                 * the unit test class, which is most likely in another package. 
                 */
                File configFile = ConfigurationTestUtils.getFileForResource(this, "/org/opennms/netmgt/mock/eventconf.xml");
                DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
                List<EventConfEvent> eventConfEventList = EventConfUtil.parseResourcesAsEventConfEvents(new FileSystemResource(configFile));
                eventConfDao.loadEventsFromDB(eventConfEventList);
                
                EventExpander eventExpander = new EventExpander(m_registry);
                eventExpander.setEventConfDao(eventConfDao);
                eventExpander.setEventUtil(eventUtil);
                eventExpander.afterPropertiesSet();

                EventIpcBroadcastProcessor eventIpcBroadcastProcessor = new EventIpcBroadcastProcessor(m_registry);
                eventIpcBroadcastProcessor.setEventIpcBroadcaster(m_eventdIpcMgr);
                eventIpcBroadcastProcessor.afterPropertiesSet();

                List<EventProcessor> eventProcessors = new ArrayList<EventProcessor>(3);
                eventProcessors.add(eventExpander);
                eventProcessors.add(eventIpcBroadcastProcessor);
                
                DefaultEventHandlerImpl eventHandler = new DefaultEventHandlerImpl(m_registry);
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
                
                m_eventd = new Eventd();
                m_eventd.setEventdServiceManager(eventdServiceManager);
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
    
    @After
    public void runTest() throws Throwable {
        try {
            if (!m_allowWarnings) {
                MockLogAppender.assertNoWarningsOrGreater();
            }
        } finally {
            MockUtil.println("------------ End Test "+this+" --------------------------");
        }
    }

    @After
    public void tearDown() throws Exception {
        if(m_runSupers) {
            if (isStartEventd()) m_eventd.stop();
        }
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

    public JdbcTemplate getJdbcTemplate() {
        return m_db.getJdbcTemplate();
    }

    @Override
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
