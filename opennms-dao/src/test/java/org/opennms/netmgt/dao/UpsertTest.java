/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * UpsertTest
 *
 * @author brozow
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:upsertTest-context.xml"
})
@JUnitTemporaryDatabase()
public class UpsertTest {
    
    @Autowired
    UpsertService m_upsertService;

    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    SnmpInterfaceDao m_snmpIfaceDao;
    
    @Autowired
    JdbcTemplate m_jdbcTemplate;
    
    @Autowired
    DatabasePopulator m_populator;
    
    @Autowired
    TransactionTemplate m_transTemplate;
    
    @Before
    public void setUp() {
        m_populator.populateDatabase();
    }
    
    @Test
    public void testInsert() {
        String newIfName = "newIf0";
        assertEquals(0, countIfs(1, 1001, newIfName));

        // add non existent snmpiface
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface();
        snmpIface.setIfIndex(1001);
        snmpIface.setIfName(newIfName);
        
        m_upsertService.upsert(1 /* nodeid */, snmpIface, 0);
        
        assertEquals(1, countIfs(1, 1001, newIfName));
    }
    
    private int countIfs(int nodeId, int ifIndex, String ifName) {
        return m_jdbcTemplate.queryForInt("select count(*) from snmpInterface where nodeid=? and snmpifindex=? and snmpifname=?", nodeId, ifIndex, ifName);
    }
    
    @Test
    public void testUpdate() {
        String oldIfName = "eth0";
        String newIfName = "newIf0";
        assertEquals(1, countIfs(1, 2, oldIfName));
        assertEquals(0, countIfs(1, 2, newIfName));
        
        // add non existent snmpiface
        OnmsSnmpInterface snmpIface = new OnmsSnmpInterface();
        snmpIface.setIfIndex(2);
        snmpIface.setIfName(newIfName);
        
        m_upsertService.upsert(1, snmpIface, 0);

        assertEquals(0, countIfs(1, 2, oldIfName));
        assertEquals(1, countIfs(1, 2, newIfName));
    }
    
    @Test
    public void testConcurrentInsert() throws InterruptedException {
        Inserter one = new Inserter(1, 1001, "ifName1");
        Inserter two = new Inserter(1, 1001, "ifName2");
        
        one.start();
        two.start();
        
        one.join();
        two.join();
        
        assertNull("Exception on upsert two "+two.getThrowable(), two.getThrowable());
        assertNull("Exception on upsert one "+one.getThrowable(), one.getThrowable());
    }

    private class Inserter extends Thread {
        private int m_nodeId;
        private int m_ifIndex;
        private String m_ifName;
        private AtomicReference<Throwable> m_throwable = new AtomicReference<Throwable>();
        
        public Inserter(int nodeId, int ifIndex, String ifName) {
            m_nodeId = nodeId;
            m_ifIndex = ifIndex;
            m_ifName = ifName;
        }
        
        public void run() {
            try {
                OnmsSnmpInterface snmpIface = new OnmsSnmpInterface();
                snmpIface.setIfIndex(m_ifIndex);
                snmpIface.setIfName(m_ifName);
                m_upsertService.upsert(m_nodeId, snmpIface, 1000);
            } catch(Throwable t) {
                t.printStackTrace();
                m_throwable.set(t);
            }
        }
        
        public Throwable getThrowable() {
            return m_throwable.get();
        }
    }
    
    

    
}
