/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;


import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath*:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class RancidProvisioningAdapterTest implements InitializingBean {
    @Autowired
    private RancidProvisioningAdapter m_adapter;
    
    @Autowired
    private NodeDao m_nodeDao;

    private AdapterOperation m_addOperation;
    private AdapterOperation m_deleteOperation;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("test.example.com").setForeignSource("rancid").setForeignId("1");
        nb.addInterface("192.168.0.1");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        m_addOperation = m_adapter.new AdapterOperation(
            m_nodeDao.findByForeignId("rancid", "1").getId(),
            AdapterOperationType.ADD,
            new SimpleQueuedProvisioningAdapter.AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS)
        );
        m_deleteOperation = m_adapter.new AdapterOperation(
            m_nodeDao.findByForeignId("rancid", "1").getId(),
            AdapterOperationType.DELETE,
            new SimpleQueuedProvisioningAdapter.AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS)
        );
    }

    /**
     * TODO: This test needs to be updated so that it properly connects to the JUnitHttpServer
     * for simulated RANCID REST operations.
     */
    @Test
    @Transactional
    @JUnitHttpServer(port=7081,basicAuth=true)
    @Ignore
    public void testAdd() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("rancid", "1");
        m_adapter.addNode(n.getId());
        m_adapter.processPendingOperationForNode(m_addOperation);
        Thread.sleep(3000);
    }
    
    /**
     * TODO: This test needs to be updated so that it properly connects to the JUnitHttpServer
     * for simulated RANCID REST operations.
     * TODO: This test seems to pass even though it fails to connect with a mock RANCID server
     */
    @Test
    @Transactional
    @JUnitHttpServer(port=7081,basicAuth=true)
    public void testDelete() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("rancid", "1");
        m_adapter.deleteNode(n.getId());
        m_adapter.processPendingOperationForNode(m_deleteOperation);
        Thread.sleep(3000);
    }
}
