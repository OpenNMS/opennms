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
package org.opennms.netmgt.provision;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.dns.JUnitDNSServerExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
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
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "importer.adapter.dns.server=127.0.0.1:9153",
        "importer.adapter.dns.privatekey=HmacMD5/test.example.com./QBMBi+8THN8iyAuGIhniB+fiURwQjrrpwFuq1L6NmHcya7QdKqjwp6kLIczPjsAUDcqiLAdQJnQUhCPThA4XtQ=="
})
public class DnsProvisioningAdapterIT implements InitializingBean {
    @Autowired
    private DnsProvisioningAdapter m_adapter;

    @Autowired
    private NodeDao m_nodeDao;

    private AdapterOperation m_addOperation;
    private AdapterOperation m_deleteOperation;

    @Override
    public void afterPropertiesSet() throws Exception {
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
        @DNSZone(name = "example.com", entries = {
            @DNSEntry(hostname = "test", data = "192.168.0.1")
        })
    })
    public void testAdd() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("dns", "1");
        m_adapter.addNode(n.getId());
        m_adapter.processPendingOperationForNode(m_addOperation);
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
        @DNSZone(name = "example.com", entries = {
            @DNSEntry(hostname = "test", data = "192.168.0.1")
        })
    })
    public void testDelete() throws Exception {
        OnmsNode n = m_nodeDao.findByForeignId("dns", "1");
        m_adapter.deleteNode(n.getId());
        m_adapter.processPendingOperationForNode(m_deleteOperation);
    }
}
