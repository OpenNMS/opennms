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
package org.opennms.features.deviceconfig.monitor.adaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;

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
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.service.DeviceConfigConstants;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-deviceConfig-MonitorAdaptor.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class DeviceConfigMonitorAdaptorIT {
    private static final String DEFAULT_SERVICE_NAME = "DeviceConfig-default";

    @Autowired
    private ServiceTypeDao serviceTypeDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    @Qualifier(value = "deviceConfigMonitorAdaptor")
    private ServiceMonitorAdaptor deviceConfigAdaptor;

    @Autowired
    private SessionUtils sessionUtils;
    
    @Autowired
    private MockEventIpcManager eventIpcManager;

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
        Mockito.when(service.getSvcName()).thenReturn(DEFAULT_SERVICE_NAME);
        Map<String, Object> attributes = new HashMap<>();
        // Set charset information in attributes.
        attributes.put("encoding", StandardCharsets.UTF_16.name());

        // Send failed update first ( scenario 1)
        String failureReason = "Failed to connect to SSHServer";
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(new org.opennms.netmgt.poller.DeviceConfig());
        Mockito.when(pollStatus.getReason()).thenReturn(failureReason);
        // start event
        EventBuilder builder = new EventBuilder(EventConstants.DEVICE_CONFIG_BACKUP_STARTED_UEI, "poller");
        builder.setInterface(ipInterface.getIpAddress());
        builder.setNodeid(ipInterface.getNodeId());
        builder.setService(DEFAULT_SERVICE_NAME);
        eventIpcManager.getEventAnticipator().anticipateEvent(builder.getEvent());
        // failed event
        builder = new EventBuilder(EventConstants.DEVICE_CONFIG_BACKUP_FAILED_UEI, "poller");
        builder.addParam(EventConstants.PARM_LOSTSERVICE_REASON, failureReason);
        builder.setInterface(ipInterface.getIpAddress());
        builder.setNodeid(ipInterface.getNodeId());
        builder.setService(DEFAULT_SERVICE_NAME);
        eventIpcManager.getEventAnticipator().anticipateEvent(builder.getEvent());
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        // Verify that event for config failure was sent.
        eventIpcManager.getEventAnticipator().verifyAnticipated();
        Optional<DeviceConfig> configOnMonday = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
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

        builder = new EventBuilder(EventConstants.DEVICE_CONFIG_BACKUP_STARTED_UEI, "poller");
        builder.setInterface(ipInterface.getIpAddress());
        builder.setNodeid(ipInterface.getNodeId());
        builder.setService(DEFAULT_SERVICE_NAME);
        builder.addParam(DeviceConfigConstants.PARM_DEVICE_CONFIG_BACKUP_DATA_PROTOCOL, "TFTP");
        builder.addParam(DeviceConfigConstants.PARM_DEVICE_CONFIG_BACKUP_CONTROL_PROTOCOL, DeviceConfigConstants.CRON);
        eventIpcManager.getEventAnticipator().anticipateEvent(builder.getEvent());

        builder = new EventBuilder(EventConstants.DEVICE_CONFIG_BACKUP_SUCCEEDED_UEI, "poller");
        builder.setInterface(ipInterface.getIpAddress());
        builder.setNodeid(ipInterface.getNodeId());
        builder.setService(DEFAULT_SERVICE_NAME);
        eventIpcManager.getEventAnticipator().anticipateEvent(builder.getEvent());
        
        // Send pollStatus with config to adaptor.
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        // Verify that event for config success was sent.
        eventIpcManager.getEventAnticipator().verifyAnticipated();

        Optional<DeviceConfig> configOnTuesday = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
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
        Optional<DeviceConfig> configOnWednesday = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
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
        Optional<DeviceConfig> configOnThursday = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
        Assert.assertTrue(configOnThursday.isPresent());
        // Creates new entry and all created time, succeded and updated are equal
        Assert.assertNotEquals(configOnThursday.get().getId(), configOnWednesday.get().getId());
        Assert.assertEquals(configOnThursday.get().getLastSucceeded(), configOnThursday.get().getLastUpdated());
        Assert.assertEquals(configOnThursday.get().getCreatedTime(), configOnThursday.get().getLastUpdated());

        // Send failed update ( Scenario 5)
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(new org.opennms.netmgt.poller.DeviceConfig());
        Mockito.when(pollStatus.getReason()).thenReturn("Failed to connect to SSHServer");
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> configOnFriday = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
        Assert.assertTrue(configOnFriday.isPresent());
        // Verify that failed config doesn't create new entry
        Assert.assertEquals(configOnFriday.get().getId(), configOnThursday.get().getId());
        // Verify that lastupdated matches with lastfailed.
        Assert.assertEquals(configOnFriday.get().getLastUpdated(), configOnFriday.get().getLastFailed());

        // Send failed update again ( Scenario 6)
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        Optional<DeviceConfig> configOnSaturday = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
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
        Optional<DeviceConfig> configOnSunday = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
        Assert.assertTrue(configOnSunday.isPresent());
        // Verify that config is same
        Assert.assertArrayEquals(configOnSunday.get().getConfig(), configOnThursday.get().getConfig());
        Assert.assertEquals(configOnSunday.get().getLastUpdated(), configOnSunday.get().getLastSucceeded());
        Assert.assertNotEquals(configOnSunday.get().getLastUpdated(), configOnSunday.get().getCreatedTime());

        eventIpcManager.sendNowSync(EventUtils.createInterfaceDeletedEvent("dcb-test", node.getId(), ipInterface.getIpAddress(),ipInterface.getId()));
        List<DeviceConfig> allConfigs = deviceConfigDao.getAllDeviceConfigsWithAnInterfaceId(ipInterface.getId());
        // Verify that they got deleted
        Assert.assertTrue(allConfigs.isEmpty());

    }

    @Test
    public void testDeviceConfigNonBackedupDevices() {
        MonitoredService service = Mockito.mock(MonitoredService.class);
        PollStatus pollStatus = Mockito.mock(PollStatus.class);
        Mockito.when(service.getNodeId()).thenReturn(node.getId());
        Mockito.when(service.getIpAddr()).thenReturn(InetAddressUtils.toIpAddrString(ipInterface.getIpAddress()));
        Mockito.when(service.getSvcName()).thenReturn(DEFAULT_SERVICE_NAME);
        Map<String, Object> attributes = new HashMap<>();
        // Set charset information in attributes.
        attributes.put("encoding", StandardCharsets.UTF_16.name());

        // No config should exist for this device.
        Optional<DeviceConfig> noDeviceConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
        Assert.assertFalse(noDeviceConfig.isPresent());

        // Now poller returns Unknown status without any config.
        Mockito.when(pollStatus.getDeviceConfig()).thenReturn(null);
        Mockito.when(pollStatus.getStatusName()).thenReturn("Unknown");
        deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);
        // Verify that we create empty Device Config entry
        Optional<DeviceConfig> emptyConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
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
        Mockito.when(service.getSvcName()).thenReturn(DEFAULT_SERVICE_NAME);
        Map<String, Object> attributes = new HashMap<>();
        // Set charset information in attributes.
        attributes.put("encoding", StandardCharsets.UTF_16.name());

        Optional<DeviceConfig> noDeviceConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
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
        Optional<DeviceConfig> failedConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
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

    @Test
    public void testDeleteStaleConfigs() {
        // Ensure after a poll that a new records is added, stale records are deleted, non-stale records are retained
        this.sessionUtils.withTransaction(() -> {
            Date currentDate = new Date();

            List<Date> previousDates = List.of(
                // within the 10 day retention period
                Date.from(currentDate.toInstant().minusSeconds(100)),
                // beyond the 10 day retention period - should be cleaned up after a new config backup
                Date.from(currentDate.toInstant().minus(100, ChronoUnit.DAYS)),
                Date.from(currentDate.toInstant().minus(200, ChronoUnit.DAYS))
            );

            List<DeviceConfig> previousDeviceConfigs =
                previousDates.stream().map(dt -> {
                    DeviceConfig dc = new DeviceConfig();
                    dc.setConfig(String.format("%d", dt.getTime()).getBytes(StandardCharsets.UTF_8));
                    dc.setStatus(DeviceConfigStatus.SUCCESS);
                    dc.setLastUpdated(dt);
                    dc.setLastSucceeded(dt);
                    dc.setEncoding("UTF-8");
                    dc.setConfigType("default");
                    dc.setCreatedTime(dt);
                    dc.setFileName("dcb.cfg");
                    dc.setIpInterface(ipInterface);
                    dc.setServiceName(DEFAULT_SERVICE_NAME);
                    return dc;
                }).collect(Collectors.toList());

            previousDeviceConfigs.forEach(deviceConfigDao::save);

            final String config = "OpenNMS-Device-Config";
            MonitoredService service = Mockito.mock(MonitoredService.class);
            PollStatus pollStatus = Mockito.mock(PollStatus.class);
            Mockito.when(service.getNodeId()).thenReturn(node.getId());
            Mockito.when(service.getIpAddr()).thenReturn(InetAddressUtils.toIpAddrString(ipInterface.getIpAddress()));
            Mockito.when(service.getSvcName()).thenReturn(DEFAULT_SERVICE_NAME);
            Map<String, Object> attributes = new HashMap<>();
            // Set charset and retention information in attributes.
            attributes.put("encoding", StandardCharsets.UTF_16.name());
            attributes.put(DeviceConfigConstants.RETENTION_PERIOD, "P10D");

            // Send valid config
            byte[] configInBytes = config.getBytes(StandardCharsets.UTF_16);
            final String fileName = "fileName";
            var deviceConfig = new org.opennms.netmgt.poller.DeviceConfig(configInBytes, fileName);
            Mockito.when(pollStatus.getDeviceConfig()).thenReturn(deviceConfig);
            // Send pollStatus with config to adaptor.
            deviceConfigAdaptor.handlePollResult(service, attributes, pollStatus);

            // Confirm new record was added
            Optional<DeviceConfig> latestConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, DEFAULT_SERVICE_NAME);
            assertThat(latestConfig.isPresent(), is(true));
            byte[] retrievedConfigInBytes = latestConfig.get().getConfig();
            // Compare binary values
            assertArrayEquals(configInBytes, retrievedConfigInBytes);
            // Make use of encoding
            final String retrievedConfig = new String(retrievedConfigInBytes, Charset.forName(latestConfig.get().getEncoding()));
            assertThat(config, equalTo(retrievedConfig));

            // Get all configs for this interface
            // Confirm that only ones are the latest/newest one just added, plus the other entry that is within
            // the retention period
            List<DeviceConfig> allConfigs = deviceConfigDao.findConfigsForInterfaceSortedByDate(ipInterface, DEFAULT_SERVICE_NAME);
            assertThat(allConfigs.size(), equalTo(2));

            // most recent device config - the one that was just added
            DeviceConfig dc0 = allConfigs.get(0);
            assertThat(dc0.getId(), equalTo(latestConfig.get().getId()));
            assertThat(dc0.getLastUpdated(), equalTo(latestConfig.get().getLastUpdated()));
            assertThat(dc0.getCreatedTime(), equalTo(latestConfig.get().getCreatedTime()));
            assertArrayEquals(dc0.getConfig(), latestConfig.get().getConfig());

            // this is the older record that is still within the retention period
            DeviceConfig retainedConfig = allConfigs.get(1);
            DeviceConfig expectedRetainedConfig = previousDeviceConfigs.get(0);

            assertThat(retainedConfig.getId(), equalTo(expectedRetainedConfig.getId()));
            assertThat(retainedConfig.getLastUpdated(), equalTo(expectedRetainedConfig.getLastUpdated()));
            assertThat(retainedConfig.getCreatedTime(), equalTo(expectedRetainedConfig.getCreatedTime()));
            assertArrayEquals(retainedConfig.getConfig(), expectedRetainedConfig.getConfig());
        });
    }

    private void populateIpInterface() {
        NetworkBuilder builder = new NetworkBuilder();
        node = builder.addNode("node2").setForeignSource("device-config").setForeignId("2").setType(OnmsNode.NodeType.ACTIVE).getNode();
        ipInterface = builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P").getInterface();
        final var service = builder.addService(DEFAULT_SERVICE_NAME);

        serviceTypeDao.saveOrUpdate(service.getServiceType());
        nodeDao.save(node);
    }
}
