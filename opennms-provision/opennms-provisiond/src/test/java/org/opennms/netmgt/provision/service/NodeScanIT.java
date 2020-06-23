/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.core.snmp.profile.mapper.impl.SnmpProfileMapperImpl;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.snmp.SnmpProfileMapper;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath:/META-INF/opennms/applicationContext-snmp-profile-mapper.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/mockForeignSourceContext.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@DirtiesContext
public class NodeScanIT {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionerRescanExistingFalseIT.class);

    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;

    @Autowired
    private Provisioner m_provisioner;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceLoader m_resourceLoader;

    @Autowired
    private ProvisionService m_provisionService;

    @Autowired
    @Qualifier("scheduledExecutor")
    private PausibleScheduledThreadPoolExecutor m_scheduledExecutor;

    private SnmpPeerFactory m_snmpPeerFactory;

    private ForeignSourceRepository m_foreignSourceRepository;

    private ForeignSource m_foreignSource;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "ERROR");

        m_foreignSource = new ForeignSource();
        m_foreignSource.setName("noRescanOnImport");
        m_foreignSource.setScanInterval(Duration.standardDays(1));
        m_foreignSourceRepository = new MockForeignSourceRepository();
        m_foreignSourceRepository.save(m_foreignSource);
        m_foreignSourceRepository.flush();

        m_provisionService.setForeignSourceRepository(m_foreignSourceRepository);
        m_scheduledExecutor.pause();
    }

    @Test(timeout=60000)
    @JUnitSnmpAgent(host="192.0.2.201", port=161, resource = "classpath:/snmpProfileTestData.properties")
    public void testNodeScanWithSnmpProfiles() throws Exception {

        // This has profiles with valid config.
        URL url =  getClass().getResource("/snmp-config1.xml");
        try (InputStream configStream = url.openStream()) {
            SnmpPeerFactory snmpPeerFactory = new ProxySnmpAgentConfigFactory(configStream);
            // This is to not override snmp-config from etc
            SnmpPeerFactory.setFile(new File(url.getFile()));
            m_provisioner.setAgentConfigFactory(snmpPeerFactory);
            LocationAwareSnmpClient locationAwareSnmpClient = m_provisionService.getLocationAwareSnmpClient();
            FilterDao filterDao = Mockito.mock(FilterDao.class);
            when(filterDao.isValid(Mockito.anyString(), Mockito.contains("IPLIKE"))).thenReturn(true);
            SnmpProfileMapper profileMapper = new SnmpProfileMapperImpl(filterDao, snmpPeerFactory, locationAwareSnmpClient);
            m_provisionService.setSnmpProfileMapper(profileMapper);
            m_provisioner.start();
            m_provisioner.importModelFromResource(m_resourceLoader.getResource("classpath:/testScanWithoutSnmpService.xml"),
                    Boolean.TRUE.toString());

            final List<OnmsNode> nodes = getNodeDao().findAll();
            assertEquals(1, nodes.size());
            // Verify node and it's location.
            OnmsNode node = nodes.get(0);
            assertEquals("Hyderabad", node.getLocation().getLocationName());
            assertEquals(1, node.getIpInterfaces().size());
            m_scheduledExecutor.resume();
            // Verify that snmp data is updated for the node i.e. means agent has been detected.
            await().atMost(30, TimeUnit.SECONDS).until(node::getSysObjectId, is(".1.3.6.1.4.1.8072.3.2.255"));
        }
        m_scheduledExecutor.pause();
    }



    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

}
