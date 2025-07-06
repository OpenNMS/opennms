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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.mock.MockNotifdConfigManager;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Rule;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.JdbcFilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.google.common.collect.ImmutableMap;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
public class NotificationManagerIT implements InitializingBean {
	@Autowired
	private DataSource m_dataSource;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
	private MonitoringLocationDao m_locationDao;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private IpInterfaceDao m_ipInterfaceDao;
	
	@Autowired
	private MonitoredServiceDao m_serviceDao;

	@Autowired
	private ServiceTypeDao m_serviceTypeDao;
	
	@Autowired
	private CategoryDao m_categoryDao;

    private NotificationManagerImpl m_notificationManager;
    private NotifdConfigManager m_configManager;

    private OnmsNode node1;
    private OnmsIpInterface ipInterfaceOnNode1;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    @Transactional(propagation = Propagation.REQUIRED)
    public void setUp() throws Exception {
        // Initialize Filter DAO
        DatabaseSchemaConfigFactory.init();
        JdbcFilterDao jdbcFilterDao = new JdbcFilterDao();
        jdbcFilterDao.setDataSource(m_dataSource);
        jdbcFilterDao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        jdbcFilterDao.afterPropertiesSet();
        FilterDaoFactory.setInstance(jdbcFilterDao);

        m_configManager = new MockNotifdConfigManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifd-configuration.xml"));
        m_notificationManager = new NotificationManagerIT.NotificationManagerImpl(m_configManager, m_dataSource);

        OnmsNode node;
        OnmsIpInterface ipInterface;
        OnmsMonitoredService service;
        OnmsServiceType serviceType;

        OnmsCategory category1 = new OnmsCategory("CategoryOne");
        m_categoryDao.save(category1);
        OnmsCategory category2 = new OnmsCategory("CategoryTwo");
        m_categoryDao.save(category2);
        OnmsCategory category3 = new OnmsCategory("CategoryThree");
        m_categoryDao.save(category3);
        OnmsCategory category4 = new OnmsCategory("CategoryFour");
        m_categoryDao.save(category4);
        m_categoryDao.flush();

        serviceType = new OnmsServiceType("ICMP");
        m_serviceTypeDao.save(serviceType);

        serviceType = new OnmsServiceType("HTTP");
        m_serviceTypeDao.save(serviceType);

        // node 1
        node = new OnmsNode(m_locationDao.getDefaultLocation(), "node 1");
        node.addCategory(category1);
        node.addCategory(category2);
        node.addCategory(category3);
        node1 = node;

        ipInterface = new OnmsIpInterface(addr("192.168.1.1"), node);
        ipInterfaceOnNode1 = ipInterface;
        service = new OnmsMonitoredService(ipInterface, serviceType);
        m_nodeDao.save(node);

        // node 2
        node = new OnmsNode(m_locationDao.getDefaultLocation(), "node 2");
        node.addCategory(category1);
        node.addCategory(category2);
        node.addCategory(category4);
        m_nodeDao.save(node);

        ipInterface = new OnmsIpInterface(addr("192.168.1.1"), node);
        m_ipInterfaceDao.save(ipInterface);
        service = new OnmsMonitoredService(ipInterface, serviceType);
        m_serviceDao.save(service);

        ipInterface = new OnmsIpInterface(addr("0.0.0.0"), node);
        m_ipInterfaceDao.save(ipInterface);

        // node 3
        node = new OnmsNode(m_locationDao.getDefaultLocation(), "node 3");
        m_nodeDao.save(node);

        ipInterface = new OnmsIpInterface(addr("192.168.1.2"), node);
        m_ipInterfaceDao.save(ipInterface);
        service = new OnmsMonitoredService(ipInterface, serviceType);
        m_serviceDao.save(service);

        // node 4 has an interface, but no services
        node = new OnmsNode(m_locationDao.getDefaultLocation(), "node 4");
        m_nodeDao.save(node);

        ipInterface = new OnmsIpInterface(addr("192.168.1.3"), node);
        m_ipInterfaceDao.save(ipInterface);

        // node 5 has no interfaces
        node = new OnmsNode(m_locationDao.getDefaultLocation(), "node 5");
        m_nodeDao.save(node);

        m_nodeDao.flush();
        m_ipInterfaceDao.flush();
        m_serviceDao.flush();
        m_serviceTypeDao.flush();
        m_categoryDao.flush();
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNoElement() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, null, null,
                                           "(ipaddr IPLIKE *.*.*.*)",
                                           true);
    }
    
    /**
     * This should match because even though the node is not set in the event,
     * the IP address is in the database on *some* node.
     */
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNoNodeIdWithIpAddr() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    /**
     * Trapd sends events like this (with no nodeId set but an interface set)
     * when it gets a trap from a device with an IP that isn't in the
     * database.  This shouldn't send an event.
     */
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNoNodeIdWithIpAddrNotInDb() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, "192.168.1.2", null,
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }

    /**
     * This should match because even though the node is not set in the event,
     * the IP address and service is in the database on *some* node.
     */
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNoNodeIdWithService() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, null, "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }

    // FIXME... do we really want to return true if the rule is wrong?????
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testRuleBogus() {
        try {
            doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                               1, "192.168.1.1", "HTTP",
                                               "(aklsdfjweklj89jaikj)",
                                               false);
            Assert.fail("Expected exception to be thrown!");
        } catch (FilterParseException e) {
            // I expected this
        }
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testIplikeAllStars() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr IPLIKE *.*.*.*)",
                                           true);
    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNodeOnlyMatch() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, null, null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNodeOnlyMatchZeroesIpAddr() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "0.0.0.0", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNodeOnlyNoMatch() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           3, null, null,
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testWrongNodeId() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           2, "192.168.1.1", "HTTP",
                                           "(nodeid == 1)",
                                           false);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testIpAddrSpecificPass() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testIpAddrSpecificFail() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.2')",
                                           false);
    }
    

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testIpAddrServiceSpecificPass() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testIpAddrServiceSpecificFail() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr == '192.168.1.2')",
                                           false);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testIpAddrServiceSpecificWrongService() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "ICMP",
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testIpAddrServiceSpecificWrongIP() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.2", "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testMultipleCategories() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(catincCategoryOne) & (catincCategoryTwo) & (catincCategoryThree)",
                                           true);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testMultipleCategoriesNotMember() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           2, "192.168.1.1", "HTTP",
                                           "(catincCategoryOne) & (catincCategoryTwo) & (catincCategoryThree)",
                                           false);
    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testIpAddrMatchWithNoServiceOnInterface() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           4, null, null,
                                           "(ipaddr == '192.168.1.3')",
                                           true);
    }

    /**
     * This test returns false because the ipInterface table is the
     * "primary" table in database-schema.xml, so it is joined with
     * every query, even if we don't ask for it to be joined and if
     * it isn't referenced in the filter query.  Sucky, huh?
     */
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testNodeMatchWithNoInterfacesOnNode() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           5, null, null,
                                           "(nodeId == 5)",
                                           false);
    }
    
    /**
     * This tests bugzilla bug #1807.  The problem happened when we add our
     * own constraints to the filter but fail to wrap the user's filter in
     * parens.  This isn't a problem when the outermost logic expression in
     * the user's filter (if any) is an AND, but it is if it's an OR.
     */
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testRuleWithOrNoMatch() {
        /*
         * Note: the nodeLabel for nodeId=3/ipAddr=192.168.1.2 is 'node 3'
         * which shouldn't match the filter.
         */
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                3, "192.168.1.2", "HTTP",
                "(nodelabel=='node 1') | (nodelabel=='node 2')",
                false);
    }


    @Test
    @JUnitTemporaryDatabase
    public void canMatchEventParametersWhenAcknowledgingNotices() throws IOException, SQLException {
        // Insert some event in the database with a few event parameters
        OnmsEvent dbEvent = new OnmsEvent();
        dbEvent.setDistPoller(m_distPollerDao.whoami());
        dbEvent.setEventUei(EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI);
        dbEvent.setEventCreateTime(new Date());
        dbEvent.setEventLog("Y");
        dbEvent.setEventDisplay("Y");
        dbEvent.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        dbEvent.setEventSource("test");
        dbEvent.setEventTime(new Date());
        dbEvent.setNode(node1);
        dbEvent.setEventParameters(Arrays.asList(
                new OnmsEventParameter(dbEvent, "some-parameter", "some-specific-value", "string"),
                new OnmsEventParameter(dbEvent, "some-other-parameter", "some-other-specific-value", "string")
        ));
        m_eventDao.save(dbEvent);
        m_eventDao.flush();

        // Create some notification referencing the event we just created
        Notification notification = new Notification();
        Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put(NotificationManager.PARAM_TEXT_MSG, "some text message")
                .put(NotificationManager.PARAM_NODE, node1.getNodeId())
                .put(NotificationManager.PARAM_INTERFACE, InetAddressUtils.toIpAddrString(ipInterfaceOnNode1.getIpAddress()))
                .put(NotificationManager.PARAM_SERVICE, "ICMP")
                .put("eventUEI", dbEvent.getEventUei())
                .put("eventID", Long.toString(dbEvent.getId()))
                .build();
        m_notificationManager.insertNotice(1, params, "q1", notification);

        final String[] parmMatchList = new String[] {"parm[some-parameter]", "parm[#2]"};

        // Verify that we're able to match the the notice when we have the same parameters set
        Event e = new EventBuilder(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, "test")
                .addParam("some-parameter", "some-specific-value")
                .addParam("some-other-parameter", "some-other-specific-value")
                .getEvent();
        Collection<Integer> eventIds = m_notificationManager.acknowledgeNotice(e, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, parmMatchList);
        assertEquals(1, eventIds.size());
        assertEquals(dbEvent.getId(), Long.valueOf(eventIds.iterator().next()));
        unacknowledgeAllNotices();

        // It should not match when either of the event parameters are different
        e = new EventBuilder(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, "test")
                .addParam("some-parameter", "!some-specific-value")
                .addParam("some-other-parameter", "some-other-specific-value")
                .getEvent();
        eventIds = m_notificationManager.acknowledgeNotice(e, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, parmMatchList);
        assertEquals(0, eventIds.size());
        unacknowledgeAllNotices();

        e = new EventBuilder(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, "test")
                .addParam("some-parameter", "some-specific-value")
                .addParam("some-other-parameter", "!some-other-specific-value")
                .getEvent();
        eventIds = m_notificationManager.acknowledgeNotice(e, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, parmMatchList);
        assertEquals(0, eventIds.size());
        unacknowledgeAllNotices();

        // It should not match when either of the event parameters are missing
        e = new EventBuilder(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, "test")
                .addParam("some-other-parameter", "some-other-specific-value")
                .getEvent();
        eventIds = m_notificationManager.acknowledgeNotice(e, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, parmMatchList);
        assertEquals(0, eventIds.size());
        unacknowledgeAllNotices();

        e = new EventBuilder(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, "test")
                .addParam("some-parameter", "some-specific-value")
                .getEvent();
        eventIds = m_notificationManager.acknowledgeNotice(e, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, parmMatchList);
        assertEquals(0, eventIds.size());
        unacknowledgeAllNotices();

        // Now try matching on other fields without any event parameters
        final String[] fieldMatchList = new String[] {"nodeid", "interfaceid", "serviceid"};
        e = new EventBuilder(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, "test")
                .setNodeid(node1.getId())
                .setInterface(ipInterfaceOnNode1.getIpAddress())
                .setService("ICMP")
                .getEvent();
        eventIds = m_notificationManager.acknowledgeNotice(e, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, fieldMatchList);
        assertEquals(1, eventIds.size());
        unacknowledgeAllNotices();

        // Expect no match if we set different values responsive event
        e = new EventBuilder(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, "test")
                .setNodeid(node1.getId() + 1)
                .setInterface(InetAddressUtils.UNPINGABLE_ADDRESS)
                .setService("HTTP")
                .getEvent();
        eventIds = m_notificationManager.acknowledgeNotice(e, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, fieldMatchList);
        assertEquals(0, eventIds.size());
        unacknowledgeAllNotices();
    }

    private void unacknowledgeAllNotices() throws SQLException {
        final DBUtils dbUtils = new DBUtils(getClass());
        Connection connection = m_dataSource.getConnection();
        dbUtils.watch(connection);
        PreparedStatement statement = connection.prepareStatement("update notifications set answeredby = null, respondtime = null");
        dbUtils.watch(statement);
        statement.execute();
    }

    private void doTestNodeInterfaceServiceWithRule(String description, int nodeId, String intf, String svc, String rule, boolean matches) {
        Notification notif = new Notification();
        notif.setName("a notification");
        Rule filterRule = new Rule();
        filterRule.setContent(rule);
        notif.setRule(filterRule);
        
        EventBuilder builder = new EventBuilder("uei.opennms.org/doNotCareAboutTheUei", "Test.Event");
        builder.setNodeid(nodeId);
        builder.setInterface(addr(intf));
        builder.setService(svc);

        assertEquals(description, matches, m_notificationManager.nodeInterfaceServiceValid(notif, builder.getEvent()));
    }
    
    public static class NotificationManagerImpl extends NotificationManager {
        protected NotificationManagerImpl(NotifdConfigManager configManager, DataSource dcf) {
            super(configManager, dcf);
        }

        @Override
        protected void saveXML(String xmlString) throws IOException {
            return;
            
        }

        @Override
        public void update() throws IOException {
            return;
        }
    }
}
