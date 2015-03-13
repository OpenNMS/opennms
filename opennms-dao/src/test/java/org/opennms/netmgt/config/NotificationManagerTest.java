/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;

import javax.sql.DataSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.mock.MockNotifdConfigManager;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.JdbcFilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
public class NotificationManagerTest implements InitializingBean {
	@Autowired
	private DataSource m_dataSource;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        // Initialize Filter DAO
        DatabaseSchemaConfigFactory.init();
        JdbcFilterDao jdbcFilterDao = new JdbcFilterDao();
        jdbcFilterDao.setDataSource(m_dataSource);
        jdbcFilterDao.setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory.getInstance());
        jdbcFilterDao.afterPropertiesSet();
        FilterDaoFactory.setInstance(jdbcFilterDao);

        m_configManager = new MockNotifdConfigManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifd-configuration.xml"));
        m_notificationManager = new NotificationManagerImpl(m_configManager, m_dataSource);
        
        final OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
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

        // node 1
        serviceType = new OnmsServiceType("HTTP");
        m_serviceTypeDao.save(serviceType);

		node = new OnmsNode(distPoller, "node 1");
		node.addCategory(category1);
		node.addCategory(category2);
		node.addCategory(category3);
		
		ipInterface = new OnmsIpInterface(addr("192.168.1.1"), node);
        service = new OnmsMonitoredService(ipInterface, serviceType);
		m_nodeDao.save(node);

        // node 2
        node = new OnmsNode(distPoller, "node 2");
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
        node = new OnmsNode(distPoller, "node 3");
        m_nodeDao.save(node);
        
        ipInterface = new OnmsIpInterface(addr("192.168.1.2"), node);
        m_ipInterfaceDao.save(ipInterface);
        service = new OnmsMonitoredService(ipInterface, serviceType);
        m_serviceDao.save(service);
        
        // node 4 has an interface, but no services
        node = new OnmsNode(distPoller, "node 4");
        m_nodeDao.save(node);

        ipInterface = new OnmsIpInterface(addr("192.168.1.3"), node);
        m_ipInterfaceDao.save(ipInterface);
        
        // node 5 has no interfaces
        node = new OnmsNode(distPoller, "node 5");
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
    
    private void doTestNodeInterfaceServiceWithRule(String description, int nodeId, String intf, String svc, String rule, boolean matches) {
        Notification notif = new Notification();
        notif.setName("a notification");
        notif.setRule(rule);
        
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
        public void update() throws IOException, MarshalException, ValidationException {
            return;
        }
    }
}
