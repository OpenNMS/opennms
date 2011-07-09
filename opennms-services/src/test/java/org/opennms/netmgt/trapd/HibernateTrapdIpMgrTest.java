//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2011 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.netmgt.trapd;

import org.junit.Assert;

import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.test.DaoTestConfigBean;

public class HibernateTrapdIpMgrTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {

    private TrapdIpMgr m_trapdIpMgr;

    private DatabasePopulator m_databasePopulator;

    private int m_testNodeId;

    @Override
    protected void setUpConfiguration() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
                "classpath:/META-INF/opennms/trapdIpMgr-test.xml"
        };
    }

    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        m_databasePopulator.populateDatabase();
        m_trapdIpMgr.dataSourceSync();

        OnmsNode n = new OnmsNode(m_databasePopulator.getDistPollerDao().get("localhost"));
        n.setLabel("my-new-node");
        n.setForeignSource("junit");
        n.setForeignId("10001");
        OnmsIpInterface iface = new OnmsIpInterface("192.168.1.3", n);
        iface.setIsManaged("M");
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        OnmsSnmpInterface snmpIf = new OnmsSnmpInterface("192.168.1.3", 1001, n);
        iface.setSnmpInterface(snmpIf);
        snmpIf.getIpInterfaces().add(iface);
        n.addIpInterface(iface);
        m_databasePopulator.getNodeDao().save(n);
        m_testNodeId = n.getId();
    }

    public void testTrapdIpMgrSetId() throws Exception {
        OnmsNode n2 =  m_databasePopulator.getNodeDao().findByForeignId("imported:", "2");
        String ipAddr = n2.getPrimaryInterface().getIpAddress();
        long expectedNodeId = Long.parseLong(n2.getNodeId());

        long nodeId = m_trapdIpMgr.getNodeId(ipAddr);
        Assert.assertEquals(expectedNodeId, nodeId);

        // Address already exists on database and it is not primary.
        Assert.assertEquals(-1, m_trapdIpMgr.setNodeId("192.168.1.3", 1));

        // Address already exists on database but the new node also contain the address and is their primary address.
        Assert.assertEquals(1, m_trapdIpMgr.setNodeId("192.168.1.3", m_testNodeId)); // return old nodeId
        Assert.assertEquals(m_testNodeId, m_trapdIpMgr.getNodeId("192.168.1.3")); // return the new nodeId
    }

    public void setTrapdIpMgr(TrapdIpMgr trapdIpMgr) {
        this.m_trapdIpMgr = trapdIpMgr;
    }

    public void setDatabasePopulator(DatabasePopulator databasePopulator) {
        this.m_databasePopulator = databasePopulator;
    }

}
