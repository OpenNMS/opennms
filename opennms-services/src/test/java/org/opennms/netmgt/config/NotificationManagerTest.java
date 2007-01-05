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
import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.notifd.mock.MockNotifdConfigManager;
import org.opennms.netmgt.utils.EventBuilder;

public class NotificationManagerTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private NotificationManagerImpl m_notificationManager;
    private DataSource m_dataSource;
    private NotifdConfigManager m_configManager;
    
    private static final String s_notifdConfigurationString =
        "<notifd-configuration\n"
        + "    status='on'\n"
        + "    pages-sent=\"SELECT * FROM notifications\"\n"
        + "    next-notif-id=\"SELECT nextval('notifynxtid')\"\n"
        + "    next-user-notif-id=\"SELECT nextval('userNotifNxtId')\"\n"
        + "    next-group-id=\"SELECT nextval('notifygrpid')\"\n"
        + "    service-id-sql=\"SELECT serviceID from service where serviceName = ?\"\n"
        + "    outstanding-notices-sql=\"SELECT notifyid FROM notifications where notifyId = ? AND respondTime is not null\"\n"
        + "    acknowledge-id-sql=\"SELECT notifyid FROM notifications WHERE eventuei=? AND nodeid=? AND interfaceid=? AND serviceid=?\"\n"
        + "     acknowledge-update-sql=\"UPDATE notifications SET answeredby=?, respondtime=? WHERE notifyId=?\"\n"
        + "    match-all='true'>\n"
        + "  <queue>\n"
        + "    <queue-id>default</queue-id>\n"
        + "    <interval>20s</interval>\n"
        + "    <handler-class>\n"
        + "      <name>org.opennms.netmgt.notifd.DefaultQueueHandler</name>\n"
        + "    </handler-class>\n"
        + "  </queue>\n"
        + "</notifd-configuration>";
    
    public NotificationManagerTest() {
        System.setProperty("opennms.home", "../opennms-daemon/src/main/filtered");
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
        };
    }
    
    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();

        m_configManager = new MockNotifdConfigManager(s_notifdConfigurationString);
        m_notificationManager = new NotificationManagerImpl(m_configManager, m_dataSource);
        
        assertNotNull("getJdbcTemplate() should not return null", getJdbcTemplate());
        assertNotNull("getJdbcTemplate().getJdbcOperations() should not return null", getJdbcTemplate().getJdbcOperations());

        getJdbcTemplate().update("INSERT INTO service ( serviceId, serviceName ) VALUES ( 1, 'HTTP' )");

        getJdbcTemplate().update("INSERT INTO node ( nodeId, nodeCreateTime ) VALUES ( 1, now() )");
        getJdbcTemplate().update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 1, '192.168.1.1' )");
        getJdbcTemplate().update("INSERT INTO ifServices ( nodeId, ipAddr, serviceId ) VALUES ( 1, '192.168.1.1', 1 )");

        getJdbcTemplate().update("INSERT INTO node ( nodeId, nodeCreateTime ) VALUES ( 2, now() )");
        getJdbcTemplate().update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 2, '192.168.1.1' )");
        getJdbcTemplate().update("INSERT INTO ipInterface ( nodeId, ipAddr ) VALUES ( 2, '0.0.0.0' )");
        getJdbcTemplate().update("INSERT INTO ifServices ( nodeId, ipAddr, serviceId ) VALUES ( 2, '192.168.1.1', 1 )");

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
    
    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
        super.setDataSource(dataSource);
    }
    
    public void testSetUp() {
        // That's all, folks!
    }

    public void testNoElement() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           0, null, null,
                                           "(ipaddr IPLIKE *.*.*.*)",
                                           true);
    }
    
    /**
     * This should match because even though the node is not set in the event,
     * the IP address is in the database on *some* node.
     */
    public void testNoNodeIdWithIpAddr() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
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
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           0, "192.168.1.2", null,
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }

    /**
     * This should match because even though the node is not set in the event,
     * the IP address and service is in the database on *some* node.
     */
    public void testNoNodeIdWithService() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           0, null, "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }

    // FIXME... do we really want to return true if the rule is wrong?????
    public void FIXMEtestRuleBogus() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           1, "192.168.1.1", "HTTP",
                                           "(aklsdfjweklj89jaikj)",
                                           false);
    }
    
    // FIXME .... PopulatedTemporaryDatabaseTestCase doesn't add iplike
    public void FIXMEtestIplikeAllStars() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr IPLIKE *.*.*.*)",
                                           true);
    }

    public void testNodeOnlyMatch() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           1, null, null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testNodeOnlyMatchZeroesIpAddr() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           1, "0.0.0.0", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testNodeOnlyNoMatch() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           3, null, null,
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }
    
    public void testWrongNodeId() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           2, "192.168.1.1", "HTTP",
                                           "(nodeid == 1)",
                                           false);
    }
    
    public void testIpAddrSpecificPass() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           1, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testIpAddrSpecificFail() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be false",
                                           1, "192.168.1.1", null,
                                           "(ipaddr == '192.168.1.2')",
                                           false);
    }
    

    public void testIpAddrServiceSpecificPass() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           true);
    }
    
    public void testIpAddrServiceSpecificFail() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be false",
                                           1, "192.168.1.1", "HTTP",
                                           "(ipaddr == '192.168.1.2')",
                                           false);
    }
    
    public void testIpAddrServiceSpecificWrongService() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be false",
                                           1, "192.168.1.1", "ICMP",
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }

    public void testIpAddrServiceSpecificWrongIP() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be false",
                                           1, "192.168.1.2", "HTTP",
                                           "(ipaddr == '192.168.1.1')",
                                           false);
    }
    
    public void testMultipleCategories() {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           1, "192.168.1.1", "HTTP",
                                           "(catincCategoryOne) & (catincCategoryTwo) & (catincCategoryThree)",
                                           true);
    }
    
    public void testMultipleCategoriesNotMember() throws InterruptedException {
        doTestNodeInterfaceServiceWithRule("was expecting the node/interface/service match to be true",
                                           2, "192.168.1.1", "HTTP",
                                           "(catincCategoryOne) & (catincCategoryTwo) & (catincCategoryThree)",
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
