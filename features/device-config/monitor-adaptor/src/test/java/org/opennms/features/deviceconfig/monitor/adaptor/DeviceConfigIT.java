/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.monitor.adaptor;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class DeviceConfigIT {

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    @Qualifier(value = "deviceConfigMonitorAdaptor")
    private ServiceMonitorAdaptor deviceConfigAdaptor;

    private OnmsIpInterface ipInterface;
    private OnmsNode node;

    @Before
    public void init() {
        populateIpInterface();
    }

    @Test
    public void testDeviceConfigPersistence() {
        int count = 10;
        String config = "OpenNMS-Device-Config";
        MonitoredService service = Mockito.mock(MonitoredService.class);
        PollStatus pollStatus = Mockito.mock(PollStatus.class);
        Mockito.when(service.getNodeId()).thenReturn(node.getId());
        Mockito.when(service.getIpAddr()).thenReturn(InetAddressUtils.toIpAddrString(ipInterface.getIpAddress()));
        Mockito.when(service.getSvcName()).thenReturn("DeviceConfig");
        Map<String, Object> attributes = new HashMap<>();
        // Set charset information in attributes.
        attributes.put("encoding", StandardCharsets.UTF_16.name());

        // Send failed update first
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(null);
        Mockito.when(pollStatus.getReason()).thenReturn("Failed to connect to SSHServer");
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> optionalDeviceConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, "default");
        Assert.assertTrue(optionalDeviceConfig.isPresent());
        Assert.assertNull(optionalDeviceConfig.get().getConfig());

        // Send valid config
        byte[] configInBytes = config.getBytes(StandardCharsets.UTF_16);
        populateDeviceConfigs(count);
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(configInBytes);
        // Send pollStatus with config to adaptor.
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);

        Optional<DeviceConfig> deviceConfigOptional = deviceConfigDao.getLatestSucceededConfigForInterface(ipInterface, "default");
        Assert.assertTrue(deviceConfigOptional.isPresent());
        byte[] retrievedConfigInBytes = deviceConfigOptional.get().getConfig();
        // Compare binary values
        Assert.assertArrayEquals(configInBytes, retrievedConfigInBytes);
        // Make use of encoding
        String retrievedConfig = new String(retrievedConfigInBytes, Charset.forName(deviceConfigOptional.get().getEncoding()));
        Assert.assertEquals(config, retrievedConfig);
        // Try to persist same config again.
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> deviceConfigOptional1 = deviceConfigDao.getLatestSucceededConfigForInterface(ipInterface, "default");
        Assert.assertTrue(deviceConfigOptional1.isPresent());
        // Verify that config doesn't change
        Assert.assertArrayEquals(deviceConfigOptional1.get().getConfig(), deviceConfigOptional.get().getConfig());

        // Send failed update again
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(null);
        Mockito.when(pollStatus.getReason()).thenReturn("Failed to connect to SSHServer");
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        optionalDeviceConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, "default");
        Assert.assertTrue(optionalDeviceConfig.isPresent());
        Assert.assertNull(optionalDeviceConfig.get().getConfig());
    }

    private void populateDeviceConfigs(int count) {

        for (int i = 1; i <= count; i++) {
            DeviceConfig deviceConfig = new DeviceConfig();
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setConfig(UUID.randomUUID().toString().getBytes(StandardCharsets.US_ASCII));
            deviceConfig.setEncoding("ASCII");
            // Persist old configs with time 1 day before.
            Date timeForDeviceConfig = Date.from(Instant.now().minus(1, ChronoUnit.DAYS).plusSeconds(i * 60));
            deviceConfig.setCreatedTime(timeForDeviceConfig);
            deviceConfig.setLastUpdated(timeForDeviceConfig);
            deviceConfigDao.saveOrUpdate(deviceConfig);
        }
    }

    private void populateIpInterface() {
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("node2").setForeignSource("device-config").setForeignId("2").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        nodeDao.saveOrUpdate(builder.getCurrentNode());
        node = nodeDao.get("device-config:2");
        Assert.assertThat(builder.getCurrentNode().getIpInterfaces(), Matchers.hasSize(1));
        Set<OnmsIpInterface> ipInterfaces = builder.getCurrentNode().getIpInterfaces();
        ipInterface = ipInterfaces.iterator().next();
        Assert.assertNotNull(ipInterface);
    }
}
