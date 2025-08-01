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
package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.tasks.Task;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-dns.xml",
        "classpath:/META-INF/opennms/applicationContext-snmp-profile-mapper.xml",
        "classpath:/META-INF/opennms/applicationContext-tracer-registry.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
public class Nms5414IT extends ProvisioningITCase {
    private static final Logger LOG = LoggerFactory.getLogger(Nms5414IT.class);
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ResourceLoader m_resourceLoader;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private MockNodeDao m_nodeDao;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        final MockForeignSourceRepository mfsr = new MockForeignSourceRepository();
        final ForeignSource fs = new ForeignSource();
        fs.setName("default");
        fs.addDetector(new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector"));
        mfsr.putDefaultForeignSource(fs);
        m_provisioner.getProvisionService().setForeignSourceRepository(mfsr);
        m_provisioner.getProvisionService().setHostnameResolver(new HostnameResolver() {
            @Override public CompletableFuture<String> getHostnameAsync(final InetAddress addr, final String location) {
                return CompletableFuture.completedFuture("opennms-com");
            }
        });
    }

    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host="10.7.15.240", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
        @JUnitSnmpAgent(host="10.7.15.241", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
        @JUnitSnmpAgent(host="10.102.251.200", port=161, resource="classpath:/snmpwalk-NMS-5414.properties"),
        @JUnitSnmpAgent(host="10.211.140.149", port=161, resource="classpath:/snmpwalk-NMS-5414.properties")
    })
    public void testScanIPV6z() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        final CountDownLatch eventRecieved = anticipateEvents(1, EventConstants.PROVISION_SCAN_COMPLETE_UEI, EventConstants.PROVISION_SCAN_ABORTED_UEI);

        m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/NMS-5414.xml"), Boolean.TRUE.toString());
        waitForEverything();

        final OnmsNode node = getNodeDao().get(nextNodeId);
        
        eventRecieved.await();
        
        final NodeScan scan = m_provisioner.createNodeScan(node.getId(), node.getForeignSource(), node.getForeignId(), node.getLocation(), null);
        runScan(scan);
        
        for (final OnmsIpInterface iface : getInterfaceDao().findAll()) {
            LOG.debug("Interface: {}", iface);
        }

        //Verify ipinterface count
        assertEquals(4, getInterfaceDao().countAll());
        // Verify that all interfaces have resolved hostname set.
        List<OnmsIpInterface> ipInterfaces = getInterfaceDao().findAll();
        ipInterfaces.forEach(onmsIpInterface -> {
                    assertEquals("opennms-com", onmsIpInterface.getIpHostName());
                }
        );
        //Verify snmpinterface count
        assertEquals(79,getSnmpInterfaceDao().countAll());
        
        final OnmsSnmpInterface onmsinterface = getSnmpInterfaceDao().findByNodeIdAndIfIndex(nextNodeId, 160);

        assertEquals("Avaya Virtual Services Platform 7024XLS Module - Unit 2 Port 32  ", onmsinterface.getIfDescr());
        assertEquals("ifc160 (Slot: 2 Port: 32)", onmsinterface.getIfName());
        assertEquals("8dd69b5cafba",onmsinterface.getPhysAddr());
        
    }
    
    public void runScan(final NodeScan scan) throws InterruptedException, ExecutionException {
        final Task t = scan.createTask();
        t.schedule();
        t.waitFor();
        waitForEverything();
    }
    
    private NodeDao getNodeDao() {
        return m_nodeDao;
    }

    private IpInterfaceDao getInterfaceDao() {
        return m_ipInterfaceDao;
    }
    
    private SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }
}
