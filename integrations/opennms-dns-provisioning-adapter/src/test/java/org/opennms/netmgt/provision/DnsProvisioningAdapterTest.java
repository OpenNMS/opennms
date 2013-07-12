/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.dns.JUnitDNSServerExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperation;
import org.opennms.netmgt.provision.SimpleQueuedProvisioningAdapter.AdapterOperationType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@TestExecutionListeners({
    JUnitDNSServerExecutionListener.class
})
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "importer.adapter.dns.server=127.0.0.1:9153",
        "importer.adapter.dns.privatekey=hmac-md5/test.example.com./QBMBi+8THN8iyAuGIhniB+fiURwQjrrpwFuq1L6NmHcya7QdKqjwp6kLIczPjsAUDcqiLAdQJnQUhCPThA4XtQ=="
})
public class DnsProvisioningAdapterTest implements InitializingBean {
    @Autowired
    private DnsProvisioningAdapter m_adapter;
    
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
        nb.addNode("test.example.com").setForeignSource("dns").setForeignId("1");
        nb.addInterface("192.168.0.1");
        m_nodeDao.save(nb.getCurrentNode());
        m_nodeDao.flush();

        // Call afterPropertiesSet() again so that the adapter is 
        // aware of the node that we just added.
        m_adapter.afterPropertiesSet();

        m_addOperation = m_adapter.new AdapterOperation(
            m_nodeDao.findByForeignId("dns", "1").getId(),
            AdapterOperationType.ADD,
            new SimpleQueuedProvisioningAdapter.AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS)
        );
        
        m_deleteOperation = m_adapter.new AdapterOperation(
            m_nodeDao.findByForeignId("dns", "1").getId(),
            AdapterOperationType.DELETE,
            new SimpleQueuedProvisioningAdapter.AdapterOperationSchedule(0, 1, 1, TimeUnit.SECONDS)
        );
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="test", address="192.168.0.1")
            })
    })
    public void testAdd() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("dns", "1");
        m_adapter.addNode(n.getId());
        m_adapter.processPendingOperationForNode(m_addOperation);
    }
    
    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="test", address="192.168.0.1")
            })
    })
    public void testDelete() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("dns", "1");
        m_adapter.deleteNode(n.getId());
        m_adapter.processPendingOperationForNode(m_deleteOperation);
    }
}
