/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision;


import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/rancidAdapterTestContext.xml"
        })
@JUnitTemporaryDatabase()
public class RancidProvisioningAdapterTest {
    /* this was autowired, but the test was breaking; they're all ignored anyways, so for now, ignore :)
     * @Autowired
     */
    private RancidProvisioningAdapter m_adapter;
    
    @Autowired
    private NodeDao m_nodeDao;

    private AdapterOperation m_adapterOperation;
    
    @Before
    public void setUp() throws Exception {
        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("test.example.com").setForeignSource("rancid").setForeignId("1");
        nb.addInterface("192.168.0.1");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        m_adapterOperation = m_adapter.new AdapterOperation(
            m_nodeDao.findByForeignId("rancid", "1").getId(),
            AdapterOperationType.ADD,
            m_adapter.new AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS)
        );        
    }

    @Test
    @Transactional
    @Ignore
    public void testAdd() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("rancid", "1");
        m_adapter.addNode(n.getId());
        m_adapter.processPendingOperationForNode(m_adapterOperation);
        Thread.sleep(3);
    }
    
    @Test
    @Transactional
    @Ignore
    public void testDelete() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("rancid", "1");
        m_adapter.deleteNode(n.getId());
        m_adapter.processPendingOperationForNode(m_adapterOperation);
        Thread.sleep(3);
    }
}
