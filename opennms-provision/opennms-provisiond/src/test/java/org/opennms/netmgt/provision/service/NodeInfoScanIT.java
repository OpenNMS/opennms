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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.snmp.profile.mapper.impl.SnmpProfileMapperImpl;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.TaskCoordinator;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpProfileMapper;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-snmp.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(host = "192.0.1.206", resource = "classpath:/snmpProfileTestData.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NodeInfoScanIT {

    private static final Logger LOG = LoggerFactory.getLogger(NodeInfoScanIT.class);

    @Autowired
    private SnmpPeerFactory snmpPeerFactory;

    @Autowired
    private LocationAwareSnmpClient locationAwareSnmpClient;

    private ProvisionService provisionService;

    private NodeInfoScan nodeInfoScan;

    private OnmsNode node;

    private ScanProgress scanProgress;

    private OnmsMonitoringLocation monitoringLocation;

    @Before
    public void setup() {
        monitoringLocation = new OnmsMonitoringLocation(LocationUtils.DEFAULT_LOCATION_NAME, LocationUtils.DEFAULT_LOCATION_NAME);
        node = new OnmsNode(monitoringLocation, "node1");
        node.setId(1);
        scanProgress = new MockScanProgress();
    }

    @Test(timeout = 30000)
    public void testNodeInfoScanWithDefaultConfig() throws InterruptedException {
        URL url =  getClass().getResource("/snmp-config1.xml");
        try (InputStream configStream = url.openStream()) {
            snmpPeerFactory = new ProxySnmpAgentConfigFactory(configStream);
            // This is to not override snmp-config from etc
            SnmpPeerFactory.setFile(new File(url.getFile()));
            provisionService = Mockito.mock(ProvisionService.class);
            when(provisionService.getLocationAwareSnmpClient()).thenReturn(locationAwareSnmpClient);
            nodeInfoScan = new NodeInfoScan(node, InetAddressUtils.getInetAddress("192.0.1.206"),
                    "fs", monitoringLocation, scanProgress, snmpPeerFactory, provisionService, 1);
            TaskCoordinator taskCoordinator = new DefaultTaskCoordinator("TaskTest");
            BatchTask batchTask = new BatchTask(taskCoordinator, null);
            nodeInfoScan.run(batchTask);
            batchTask.schedule();
            await().atMost(10, TimeUnit.SECONDS).until(node::getSysObjectId, is(".1.3.6.1.4.1.8072.3.2.255"));
        } catch (IOException e) {
            fail();
        }
    }

    @Test(timeout = 60000)
    public void testNodeInfoScanWithProfile() throws InterruptedException {
        URL url =  getClass().getResource("/snmp-config1.xml");
        try (InputStream configStream = url.openStream()) {
            // Make default scan fail by setting wrong read community.
            snmpPeerFactory = new ProxySnmpAgentConfigFactoryExtension(configStream);
            SnmpPeerFactory.setFile(new File(url.getFile()));
            provisionService = Mockito.mock(ProvisionService.class);
            FilterDao filterDao = Mockito.mock(FilterDao.class);
            when(filterDao.isValid(Mockito.anyString(), Mockito.contains("IPLIKE"))).thenReturn(true);
            SnmpProfileMapper profileMapper = new SnmpProfileMapperImpl(filterDao, snmpPeerFactory, locationAwareSnmpClient);
            when(provisionService.getSnmpProfileMapper()).thenReturn(profileMapper);
            when(provisionService.getLocationAwareSnmpClient()).thenReturn(locationAwareSnmpClient);
            nodeInfoScan = new NodeInfoScan(node, InetAddressUtils.getInetAddress("192.0.1.206"),
                    "fs", monitoringLocation, scanProgress, snmpPeerFactory, provisionService, 1);
            TaskCoordinator taskCoordinator = new DefaultTaskCoordinator("TaskTest");
            BatchTask batchTask = new BatchTask(taskCoordinator, null);
            nodeInfoScan.run(batchTask);
            batchTask.schedule();
            await().atMost(30, TimeUnit.SECONDS).until(node::getSysObjectId, is(".1.3.6.1.4.1.8072.3.2.255"));
        } catch (IOException e) {
            fail();
        }
    }

    @Test(timeout = 60000)
    public void testNodeInfoScanWithProfileThatsGotUpdated() throws InterruptedException {
        URL url =  getClass().getResource("/snmp-config1.xml");
        try (InputStream configStream = url.openStream()) {
            // Make default scan fail by setting wrong read community.
            snmpPeerFactory = new ProxySnmpAgentConfigFactoryExtension2(configStream);
            SnmpPeerFactory.setFile(new File(url.getFile()));
            provisionService = Mockito.mock(ProvisionService.class);
            FilterDao filterDao = Mockito.mock(FilterDao.class);
            when(filterDao.isValid(Mockito.anyString(), Mockito.contains("IPLIKE"))).thenReturn(true);
            SnmpProfileMapper profileMapper = new SnmpProfileMapperImpl(filterDao, snmpPeerFactory, locationAwareSnmpClient);
            when(provisionService.getSnmpProfileMapper()).thenReturn(profileMapper);
            when(provisionService.getLocationAwareSnmpClient()).thenReturn(locationAwareSnmpClient);
            nodeInfoScan = new NodeInfoScan(node, InetAddressUtils.getInetAddress("192.0.1.206"),
                    "fs", monitoringLocation, scanProgress, snmpPeerFactory, provisionService, 1);
            TaskCoordinator taskCoordinator = new DefaultTaskCoordinator("TaskTest");
            BatchTask batchTask = new BatchTask(taskCoordinator, null);
            nodeInfoScan.run(batchTask);
            batchTask.schedule();
            await().atMost(30, TimeUnit.SECONDS).until(node::getSysObjectId, is(".1.3.6.1.4.1.8072.3.2.255"));
        } catch (IOException e) {
            fail();
        }
    }


    public static class MockScanProgress implements ScanProgress {

        @Override
        public void abort(String message) {
        }

        @Override
        public boolean isAborted() {
            return false;
        }
    }

    /**
     * This returns wrong read-community so that default snmp walk fails
     **/
    static class ProxySnmpAgentConfigFactoryExtension extends ProxySnmpAgentConfigFactory {

        ProxySnmpAgentConfigFactoryExtension(InputStream config) throws FileNotFoundException {
            super(config);
        }

        @Override
        public SnmpAgentConfig getAgentConfig(final InetAddress address, String location) {
            SnmpAgentConfig config = super.getAgentConfig(address, location);
            config.setReadCommunity("chandra");
            return config;
        }
    }

    /**
     * This returns wrong read-community as well as set a profile label.
     **/
    class ProxySnmpAgentConfigFactoryExtension2 extends ProxySnmpAgentConfigFactory {

        ProxySnmpAgentConfigFactoryExtension2(InputStream config) throws FileNotFoundException {
            super(config);
        }

        @Override
        public SnmpAgentConfig getAgentConfig(final InetAddress address, String location) {
            SnmpAgentConfig config = super.getAgentConfig(address, location);
            config.setReadCommunity("chandra");
            config.setDefault(false);
            config.setProfileLabel("sample1");
            return config;
        }
    }
}
