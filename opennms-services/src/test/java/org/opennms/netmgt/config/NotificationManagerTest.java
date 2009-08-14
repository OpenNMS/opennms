//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jul 02: Get rid of DataSource stuff since it is now in our superclass.
//              Use DaoTestConfigBean.  Add some comments. - dj@opennms.org
// 2007 Jul 03: Fix test resource calls. - dj@opennms.org
// 2007 Jul 03: Enable testIplikeAllStars. - dj@opennms.org
// 2007 Jul 03: Move notifd configuration to a resource. - dj@opennms.org
// 2007 Jun 29: Add additional tests for nodes without interfaces and interfaces
//              without services.  Reset FilterDaoFactory on setup. - dj@opennms.org
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
package org.opennms.netmgt.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.notifd.mock.MockNotifdConfigManager;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.DaoTestConfigBean;

public class NotificationManagerTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private NotificationManagerImpl m_notificationManager;
    private NotifdConfigManager m_configManager;
    
    @Override
    protected void setUpConfiguration() {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
            "classpath:/META-INF/opennms/applicationContext-dao.xml",
            "classpath*:/META-INF/opennms/component-dao.xml",
            "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
        };
    }
    
    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();

        /*
         * Make sure we get a new FilterDaoFactory every time because our
         * DataSource changes every test.
         */
        FilterDaoFactory.setInstance(null);
        FilterDaoFactory.getInstance();
        
        m_configManager = new MockNotifdConfigManager(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, "notifd-configuration.xml"));
        m_notificationManager = new NotificationManagerImpl(m_configManager, getDataSource());
        
        assertNotNull("getJdbcTemplate() should not return null", getJdbcTemplate());
        assertNotNull("getJdbcTemplate().getJdbcOperations() should not return null", getSimpleJdbcTemplate().getJdbcOperations());

        getJdbcTemplate().update("INSERT INTO service ( serviceId, serviceName ) VALUES ( 1, 'HTTP' )");

        getJdbcTemplate().update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 1, now(), 'node 1' )");
        getJdbcTemplate().update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 1, '192.168.1.1' )");
        getJdbcTemplate().update("INSERT INTO ifServices ( nodeId, ipAddr, serviceId ) VALUES ( 1, '192.168.1.1', 1 )");

        getJdbcTemplate().update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 2, now(), 'node 2' )");
        getJdbcTemplate().update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 2, '192.168.1.1' )");
        getJdbcTemplate().update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 2, '0.0.0.0' )");
        getJdbcTemplate().update("INSERT INTO ifServices ( nodeId, ipAddr, serviceId ) VALUES ( 2, '192.168.1.1', 1 )");

        getJdbcTemplate().update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 3, now(), 'node 3' )");
        getJdbcTemplate().update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 3, '192.168.1.2' )");
        getJdbcTemplate().update("INSERT INTO ifServices ( nodeId, ipAddr, serviceId ) VALUES ( 3, '192.168.1.2', 1 )");

        // Node 4 has an interface, but no services
        getJdbcTemplate().update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 4, now(), 'node 4' )");
        getJdbcTemplate().update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 4, '192.168.1.3' )");

        // Node 5 has no interfaces
        getJdbcTemplate().update("INSERT INTO node ( nodeId, nodeCreateTime, nodeLabel ) VALUES ( 5, now(), 'node 5' )");

        getJdbcTemplate().update("INSERT INTO categories ( categoryId, categoryName ) VALUES ( 1, 'CategoryOne' )");
        getJdbcTemplate().update("INSERT INTO categories ( categoryId, categoryName ) VALUES ( 2, 'CategoryTwo' )");
        getJdbcTemplate().update("INSERT INTO categories ( categoryId, categoryName ) VALUES ( 3, 'CategoryThree' )");
        getJdbcTemplate().update("INSERT INTO categories ( categoryId, categoryName ) VALUES ( 4, 'CategoryFour' )");

        getJdbcTemplate().update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 1, 1 )");
        getJdbcTemplate().update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 2, 1 )");
        getJdbcTemplate().update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 3, 1 )");

        getJdbcTemplate().update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 1, 2 )");
        getJdbcTemplate().update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 2, 2 )");
        // Not a member of the third category, but is a member of the fourth
        getJdbcTemplate().update("INSERT INTO category_node ( categoryId, nodeId ) VALUES ( 4, 2 )");

        setComplete();
        endTransaction();
        startNewTransaction();
    }
    
    public void testSetUp() {
        // That's all, folks!
    }

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
    public void testNoNodeIdWithService() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           0, null, "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }

    // FIXME... do we really want to return true if the rule is wrong?????
    public void testRuleBogus() {
        try {
            doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                               1, "192.168.1.1", "HTTP",
                                               "(aklsdfjweklj89jaikj)",
                                               false);
            fail("Expected exception to be thrown!");
        } catch (FilterParseException e) {
            // I expected this
        }
    }
    
    public void testIplikeAllStars() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr IPLIKE *.*.*.*)",
                                           true);
    }

    public void testNodeOnlyMatch() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, null, null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testNodeOnlyMatchZeroesIpAddr() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "0.0.0.0", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testNodeOnlyNoMatch() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           3, null, null,
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }
    
    public void testWrongNodeId() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           2, "192.168.1.1", "HTTP",
                                           "(nodeid == 1)",
                                           false);
    }
    
    public void testIpAddrSpecificPass() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testIpAddrSpecificFail() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.2')",
                                           false);
    }
    

    public void testIpAddrServiceSpecificPass() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testIpAddrServiceSpecificFail() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr == '192.168.1.2')",
                                           false);
    }
    
    public void testIpAddrServiceSpecificWrongService() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "ICMP",
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }

    public void testIpAddrServiceSpecificWrongIP() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.2", "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }
    
    public void testMultipleCategories() {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           1, "192.168.1.1", "HTTP",
                                           "(catincCategoryOne) & (catincCategoryTwo) & (catincCategoryThree)",
                                           true);
    }
    
    public void testMultipleCategoriesNotMember() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("node/interface/service match",
                                           2, "192.168.1.1", "HTTP",
                                           "(catincCategoryOne) & (catincCategoryTwo) & (catincCategoryThree)",
                                           false);
    }

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
        builder.setInterface(intf);
        builder.setService(svc);

        assertEquals(description, matches, m_notificationManager.nodeInterfaceServiceValid(notif, builder.getEvent()));
    }
    
    public class NotificationManagerImpl extends NotificationManager {
        protected NotificationManagerImpl(NotifdConfigManager configManager, DataSource dcf) {
            super(configManager, dcf);
        }

        @Override
        protected void saveXML(String xmlString) throws IOException {
            return;
            
        }

        @Override
        protected void update() throws IOException, MarshalException, ValidationException {
            return;
        }
    }
}
