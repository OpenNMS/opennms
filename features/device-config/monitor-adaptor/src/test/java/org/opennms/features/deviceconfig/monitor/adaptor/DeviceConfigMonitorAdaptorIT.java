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
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.Poll;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
public class DeviceConfigMonitorAdaptorIT {

    @Autowired
    private ServiceTypeDao serviceTypeDao;

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

    /**
     *  Verfies following scenarios.
     *
     * scenario         created_time             last_updated       last_succeeded          last_failed          config
     * 1                  NULL                    Monday                NULL                  Monday             NULL
     * 2.                Tuesday                  Tuesday               Tuesday               Monday             Tuesday
     * 3.                Tuesday                  Wednesday             Wednesday             Monday             Tuesday
     * 4.                Thursday                 Thursday              Thursday              Monday             Thursday
     * 5.                Thursday                 Friday                Thursday              Friday             Thursday
     * 6.                Thursday                 Saturday              Thursday              Saturday           Thursday
     * 7.                Thursday                 Sunday                 Sunday               Saturday           Thursday
     *
     * 1. First retrieval failed, last_updated & last_failed are updated, config is NULL and created_time is NULL
     * 2. Retrieval succeeded, last_updated & last_succeeded got updated,  config is updated.
     * 3. Retrieval succeeded but config didn't change, created_time didn't update, last succeeded & last_updated are updated.
     * 4. Retrieval succeeded and config got updated, created_time & last_succeeded & last_updated are updated.
     * 5. Retrieval failed, last_updated & last_failed got updated
     * 6. Retrieval failed again, last_updated & last_failed got updated
     * 7. Retrieval succeded but config didn't update, last_updated & last_succeded are updated but config remained on Thurdsay.
     *
     */
    @Test
    public void testDeviceConfigPersistence() {
        String config = "OpenNMS-Device-Config";
        MonitoredService service = Mockito.mock(MonitoredService.class);
        PollStatus pollStatus = Mockito.mock(PollStatus.class);
        Mockito.when(service.getNodeId()).thenReturn(node.getId());
        Mockito.when(service.getIpAddr()).thenReturn(InetAddressUtils.toIpAddrString(ipInterface.getIpAddress()));
        Mockito.when(service.getSvcName()).thenReturn("DeviceConfig-default");
        Map<String, Object> attributes = new HashMap<>();
        // Set charset information in attributes.
        attributes.put("encoding", StandardCharsets.UTF_16.name());

        // Send failed update first ( scenario 1)
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(new org.opennms.netmgt.poller.DeviceConfig());
        Mockito.when(pollStatus.getReason()).thenReturn("Failed to connect to SSHServer");
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> configOnMonday = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(configOnMonday.isPresent());
        Assert.assertNull(configOnMonday.get().getConfig());
        Assert.assertEquals(configOnMonday.get().getLastFailed(), configOnMonday.get().getLastUpdated());
        Assert.assertNull(configOnMonday.get().getCreatedTime());
        Assert.assertNull(configOnMonday.get().getLastSucceeded());


        // Send valid config (Scenario 2)
        byte[] configInBytes = config.getBytes(StandardCharsets.UTF_16);
        var fileName = "fileName";
        var deviceConfig = new org.opennms.netmgt.poller.DeviceConfig(configInBytes, fileName);
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(deviceConfig);
        // Send pollStatus with config to adaptor.
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);

        Optional<DeviceConfig> configOnTuesday = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(configOnTuesday.isPresent());
        byte[] retrievedConfigInBytes = configOnTuesday.get().getConfig();
        // Compare binary values
        Assert.assertArrayEquals(configInBytes, retrievedConfigInBytes);
        // Make use of encoding
        String retrievedConfig = new String(retrievedConfigInBytes, Charset.forName(configOnTuesday.get().getEncoding()));
        Assert.assertEquals(config, retrievedConfig);
        // Check that same entry got overwritten
        Assert.assertEquals(configOnMonday.get().getId(), configOnTuesday.get().getId());
        Assert.assertEquals(configOnTuesday.get().getLastSucceeded(), configOnTuesday.get().getLastUpdated());
        Assert.assertEquals(configOnTuesday.get().getCreatedTime(), configOnTuesday.get().getLastUpdated());

        // Try to persist same config again ( Scenario 3)
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> configOnWednesday = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(configOnWednesday.isPresent());
        // Verify that config doesn't change.
        Assert.assertArrayEquals(configOnWednesday.get().getConfig(), configOnTuesday.get().getConfig());
        Assert.assertEquals(configOnWednesday.get().getLastSucceeded(), configOnWednesday.get().getLastUpdated());
        Assert.assertNotEquals(configOnWednesday.get().getCreatedTime(), configOnWednesday.get().getLastUpdated());

        // Send updated config ( Scenario 4)
        configInBytes = "updated-device-config".getBytes(StandardCharsets.UTF_16);
        deviceConfig = new org.opennms.netmgt.poller.DeviceConfig(configInBytes, fileName);
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(deviceConfig);
        // Send pollStatus with config to adaptor.
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> configOnThursday = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(configOnThursday.isPresent());
        // Creates new entry and all created time, succeded and updated are equal
        Assert.assertNotEquals(configOnThursday.get().getId(), configOnWednesday.get().getId());
        Assert.assertEquals(configOnThursday.get().getLastSucceeded(), configOnThursday.get().getLastUpdated());
        Assert.assertEquals(configOnThursday.get().getCreatedTime(), configOnThursday.get().getLastUpdated());

        // Send failed update ( Scenario 5)
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(new org.opennms.netmgt.poller.DeviceConfig());
        Mockito.when(pollStatus.getReason()).thenReturn("Failed to connect to SSHServer");
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> configOnFriday = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(configOnFriday.isPresent());
        // Verify that failed config doesn't create new entry
        Assert.assertEquals(configOnFriday.get().getId(), configOnThursday.get().getId());
        // Verify that lastupdated matches with lastfailed.
        Assert.assertEquals(configOnFriday.get().getLastUpdated(), configOnFriday.get().getLastFailed());

        // Send failed update again ( Scenario 6)
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> configOnSaturday = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(configOnSaturday.isPresent());
        // Verify that failed config doesn't create new entry
        Assert.assertEquals(configOnSaturday.get().getId(), configOnFriday.get().getId());
        // Verify that lastUpdated got updated and matches with last failed.
        Assert.assertNotEquals(configOnSaturday.get().getLastUpdated(), configOnFriday.get().getLastUpdated());
        Assert.assertEquals(configOnSaturday.get().getLastUpdated(), configOnSaturday.get().getLastFailed());
        Assert.assertArrayEquals(configOnSaturday.get().getConfig(), configOnThursday.get().getConfig());

        // Send successful config but that matches old config ( Scenario 7)
        configInBytes = "updated-device-config".getBytes(StandardCharsets.UTF_16);
        deviceConfig = new org.opennms.netmgt.poller.DeviceConfig(configInBytes, fileName);
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(deviceConfig);
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> configOnSunday = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(configOnSunday.isPresent());
        // Verify that config is same
        Assert.assertArrayEquals(configOnSunday.get().getConfig(), configOnThursday.get().getConfig());
        Assert.assertEquals(configOnSunday.get().getLastUpdated(), configOnSunday.get().getLastSucceeded());
        Assert.assertNotEquals(configOnSunday.get().getLastUpdated(), configOnSunday.get().getCreatedTime());
    }

    @Test
    public void testDeviceConfigNonBackedupDevices() {
        MonitoredService service = Mockito.mock(MonitoredService.class);
        PollStatus pollStatus = Mockito.mock(PollStatus.class);
        Mockito.when(service.getNodeId()).thenReturn(node.getId());
        Mockito.when(service.getIpAddr()).thenReturn(InetAddressUtils.toIpAddrString(ipInterface.getIpAddress()));
        Mockito.when(service.getSvcName()).thenReturn("DeviceConfig-default");
        Map<String, Object> attributes = new HashMap<>();
        // Set charset information in attributes.
        attributes.put("encoding", StandardCharsets.UTF_16.name());

        // No config should exist for this device.
        Optional<DeviceConfig> noDeviceConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertFalse(noDeviceConfig.isPresent());

        // Now poller returns Unknown status without any config.
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(null);
        Mockito.when(pollStatus.getStatusName()).thenReturn("Unknown");
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        // Verify that we create empty Device Config entry
        Optional<DeviceConfig> emptyConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(emptyConfig.isPresent());
        Assert.assertThat(emptyConfig.get().getConfig(), Matchers.nullValue());
        Assert.assertThat(emptyConfig.get().getLastUpdated(), Matchers.nullValue());
    }

    @Test
    public void testPollStatusChangeForNonScheduledPolls() {
        MonitoredService service = Mockito.mock(MonitoredService.class);
        PollStatus pollStatus = Mockito.mock(PollStatus.class);
        Mockito.when(service.getNodeId()).thenReturn(node.getId());
        Mockito.when(service.getIpAddr()).thenReturn(InetAddressUtils.toIpAddrString(ipInterface.getIpAddress()));
        Mockito.when(service.getSvcName()).thenReturn("DeviceConfig-default");
        Map<String, Object> attributes = new HashMap<>();
        // Set charset information in attributes.
        attributes.put("encoding", StandardCharsets.UTF_16.name());

        Optional<DeviceConfig> noDeviceConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertFalse(noDeviceConfig.isPresent());
        // Send unknown status for the first time
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(null);
        Mockito.when(pollStatus.getStatusName()).thenReturn("Unknown");
        PollStatus status = deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Assert.assertEquals("Unknown", status.getStatusName());

        // Now send failed update for the config.
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(new org.opennms.netmgt.poller.DeviceConfig());
        Mockito.when(pollStatus.getReason()).thenReturn("Failed to retrieve");
        Mockito.when(pollStatus.getStatusName()).thenReturn("Down");
        status = deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> failedConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(failedConfig.isPresent());
        Assert.assertThat(failedConfig.get().getConfig(), Matchers.nullValue());
        Assert.assertThat(failedConfig.get().getLastUpdated(), Matchers.notNullValue());
        Assert.assertEquals("Down", status.getStatusName());

        // Send unknown status again, status should return Down based on earlier config.
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(null);
        Mockito.when(pollStatus.getStatusName()).thenReturn("Unknown");
        status = deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Assert.assertEquals("Down", status.getStatusName());

    }


    private void populateIpInterface() {
        NetworkBuilder builder = new NetworkBuilder();
        node = builder.addNode("node2").setForeignSource("device-config").setForeignId("2").setType(OnmsNode.NodeType.ACTIVE).getNode();
        ipInterface = builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P").getInterface();
        final var service = builder.addService("DeviceConfig-default");

        serviceTypeDao.saveOrUpdate(service.getServiceType());
        nodeDao.save(node);
    }
}
