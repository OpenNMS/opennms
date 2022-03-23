/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2022 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.deviceconfig.rest.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertArrayEquals;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-config-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
    "classpath:/META-INF/opennms/applicationContext-deviceconfig-persistence.xml",
    "classpath:/META-INF/opennms/applicationContext-deviceconfig-service.xml",
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-rpc-poller.xml",
    "classpath:/META-INF/opennms/applicationContext-rpc-client-mock.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class DefaultDeviceConfigRestServiceScheduleIT {
    private static final int RECORD_COUNT = 3;

    private static final List<String> CRON_SCHEDULES = List.of(
        "0 15 10 ? * *",
        "0 * 14 * * ?",
        "0 15 10 ? * 6#3"
    );

    private static final List<String> EXPECTED_CRON_SCHEDULE_DESCRIPTIONS = List.of(
        "At 10:15 am",
        "Every minute, at 2:00 pm",
        "At 10:15 am, on the third Saturday of the month"
    );

    private static final List<byte[]> CONFIG_BYTES = List.of(
        "one".getBytes(StandardCharsets.UTF_8),
        "two".getBytes(StandardCharsets.UTF_8),
        "three".getBytes(StandardCharsets.UTF_8)
    );

    private static final List<String> CONFIG_STRINGS = List.of("one", "two", "three");

    private static final List<String> CONFIG_TYPES = List.of("default", "running", "wurstblinker");

    @Autowired
    private NodeDao nodeDao;
    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    private DeviceConfigService deviceConfigService;

    @Autowired
    private ServiceTypeDao serviceTypeDao;

    @Autowired
    private SessionUtils sessionUtils;

    private DeviceConfigRestService deviceConfigRestService;

    @Before
    public void before() throws IOException {
        deviceConfigRestService = new DefaultDeviceConfigRestService(deviceConfigDao, deviceConfigService);
    }

    @After
    public void after() {
    }

    @Test
    public void testGetDeviceConfigsWithScheduleInfo() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            // sanity check that nodes and interfaces were created correctly
            List<Integer> ipInterfaceIds = ipInterfaces.stream().map(OnmsIpInterface::getId).collect(Collectors.toList());

            List<DeviceConfigService.RetrievalDefinition> services = ipInterfaces.stream()
                .flatMap(iface -> deviceConfigService.getRetrievalDefinitions(InetAddressUtils.str(iface.getIpAddress()), iface.getNode().getLocation().getLocationName()).stream())
                .collect(Collectors.toList());
            assertThat(services.size(), equalTo(RECORD_COUNT));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, 3);

            deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0), dates.get(0), CONFIG_BYTES.get(0)));
            deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(1), dates.get(1), CONFIG_BYTES.get(1)));
            deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(2), CONFIG_TYPES.get(2), dates.get(2), CONFIG_BYTES.get(2)));

            var response = deviceConfigRestService.getDeviceConfigs(10, 0, "lastUpdated", "asc", null, null, null, null, null, null);
            assertThat(response, notNullValue());
            assertThat(response.hasEntity(), is(true));

            var responseHeaders = response.getHeaders();
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
            assertThat(responseHeaders.containsKey("Content-Range"), is(true));

            String contentRange = responseHeaders.get("Content-Range").get(0).toString();
            String expectedContentRange = String.format("items %d-%d/%d", 0, RECORD_COUNT - 1, RECORD_COUNT);
            assertThat(contentRange, equalTo(expectedContentRange));

            List<DeviceConfigDTO> responseList = (List<DeviceConfigDTO>) response.getEntity();
            assertThat(responseList.size(), equalTo(RECORD_COUNT));

            for (int i = 0; i < RECORD_COUNT; i++) {
                DeviceConfigDTO dto = responseList.get(i);

                assertThat(dto.getMonitoredServiceId(), equalTo(ipInterfaceIds.get(i)));
                assertThat(CONFIG_TYPES.get(i).equalsIgnoreCase(dto.getConfigType()), is(true));
                assertThat(dto.getEncoding(), equalTo(DefaultDeviceConfigRestService.DEFAULT_ENCODING));
                assertThat(dto.getLastBackupDate().getTime(), equalTo(dates.get(i).getTime()));
                assertThat(dto.getLastUpdatedDate().getTime(), equalTo(dates.get(i).getTime()));
                assertThat(dto.getLastSucceededDate().getTime(), equalTo(dates.get(i).getTime()));
                assertThat(dto.getLastFailedDate(), nullValue());
                assertThat(dto.getFailureReason(), nullValue());
                assertThat(dto.getConfig(), equalTo(CONFIG_STRINGS.get(i)));
                assertThat(dto.getScheduledInterval().get("DeviceConfig-" + CONFIG_TYPES.get(i)),
                    equalTo(EXPECTED_CRON_SCHEDULE_DESCRIPTIONS.get(i)));
                assertThat(dto.getNextScheduledBackupDate().after(currentDate), is(true));
            }
        });
    }

    @Test
    public void testGetEmptyDeviceConfigs() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, 3);

            deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0), dates.get(0), CONFIG_BYTES.get(0)));
            deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(1), dates.get(1), CONFIG_BYTES.get(1)));
            deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(2), CONFIG_TYPES.get(2), dates.get(2), CONFIG_BYTES.get(2)));

            final int nonExistingIpInterfaceId = ipInterfaces.stream().mapToInt(OnmsIpInterface::getId).max().orElse(9999) + 1;

            var response = deviceConfigRestService.getDeviceConfigs(10, 0, "lastUpdated", "asc", null, null, nonExistingIpInterfaceId, null, null, null);
            assertThat(response, notNullValue());
            assertThat(response.hasEntity(), is(false));
            assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));

            var responseHeaders = response.getHeaders();
            assertThat(responseHeaders.containsKey("Content-Range"), is(false));
        });
    }

    @Test
    public void testGetDeviceConfigsWithBinaryConfig() {
        populateDeviceConfigServiceInfo();

        this.sessionUtils.withTransaction(() -> {
            // Add nodes, interfaces, services
            List<OnmsIpInterface> ipInterfaces = ipInterfaceDao.findAll();
            assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

            // Add DeviceConfig entries mapped to ipInterfaces and services
            Date currentDate = new Date();
            List<Date> dates = getTestDates(currentDate, 3);

            final byte[] configBytes = new byte[] { 0, 1, 2, 3, 11, 25, 127 };
            final String expectedConfig = "000102030B197F";
            DeviceConfig dc = createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0), dates.get(0), configBytes);
            dc.setEncoding(DefaultDeviceConfigRestService.BINARY_ENCODING);
            deviceConfigDao.saveOrUpdate(dc);

            var response = deviceConfigRestService.getDeviceConfig(dc.getId());

            assertThat(response, notNullValue());
            assertThat(response.hasEntity(), is(true));

            DeviceConfigDTO dto = (DeviceConfigDTO) response.getEntity();

            assertThat(dc.getId(), equalTo(dto.getId()));
            assertThat(CONFIG_TYPES.get(0).equalsIgnoreCase(dto.getConfigType()), is(true));
            assertThat(dto.getEncoding(), equalTo(DefaultDeviceConfigRestService.BINARY_ENCODING));
            assertThat(dto.getConfig(), equalTo(expectedConfig));
        });
    }

    @Test
    public void testDownloadNoDeviceConfig() {
        List<String> idParams = new ArrayList<>();
        idParams.add(null);
        idParams.add("");

        for (String id : idParams) {
            var response = deviceConfigRestService.downloadDeviceConfig(id);
            assertThat(response, notNullValue());
            assertThat(response.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
        }
    }

    @Test
    public void testDownloadInvalidRequest() {
        final List<String> idParams = List.of("abc", "a,b,c", ",,,,,0a", ";", "123,,", "123,,,456", ",123");

        for (String id : idParams) {
            var response = deviceConfigRestService.downloadDeviceConfig(id);
            assertThat(response, notNullValue());
            assertThat(response.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        }
    }

    @Test
    @Transactional
    public void testDownloadSingleDeviceConfig() {
        // Add nodes, interfaces, services
        List<OnmsIpInterface> ipInterfaces = populateDeviceConfigServiceInfo();
        assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

        // Add DeviceConfig entries mapped to ipInterfaces and services
        // Save off 2nd one to check below
        Date currentDate = new Date();
        List<Date> dates = getTestDates(currentDate, 3);

        deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0), dates.get(0), CONFIG_BYTES.get(0)));
        DeviceConfig dc = createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(1), dates.get(1), CONFIG_BYTES.get(1));
        deviceConfigDao.saveOrUpdate(dc);
        deviceConfigDao.saveOrUpdate(createDeviceConfig(ipInterfaces.get(2), CONFIG_TYPES.get(2), dates.get(2), CONFIG_BYTES.get(2)));

        var response = deviceConfigRestService.downloadDeviceConfig(dc.getId().toString());

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        var responseHeaders = response.getHeaders();
        assertThat(responseHeaders.containsKey("Content-Type"), is(true));
        assertThat(responseHeaders.containsKey("Content-Disposition"), is(true));

        assertThat(responseHeaders.get("Content-Type").get(0).toString(),
            equalTo("text/plain;charset=" + DefaultDeviceConfigRestService.DEFAULT_ENCODING));

        String expectedFileName = DefaultDeviceConfigRestService.createDownloadFileName(
            "dcb-2", "192.168.3.2", CONFIG_TYPES.get(1), dc.getCreatedTime());
        String expectedContentDisposition = "attachment; filename=" + expectedFileName;
        String actualContentDisposition = responseHeaders.get("Content-Disposition").get(0).toString();
        assertThat(actualContentDisposition, equalTo(expectedContentDisposition));

        Object responseObj = response.getEntity();
        byte[] responseBytes = (byte[]) response.getEntity();

        assertArrayEquals(dc.getConfig(), responseBytes);
    }

    @Test
    @Transactional
    public void testDownloadMultipleDeviceConfigs() {
        // Add nodes, interfaces, services
        List<OnmsIpInterface> ipInterfaces = populateDeviceConfigServiceInfo();
        assertThat(ipInterfaces.size(), equalTo(RECORD_COUNT));

        // Add DeviceConfig entries mapped to ipInterfaces and services
        // Save off 2nd one to check below
        Date currentDate = new Date();
        List<Date> dates = getTestDates(currentDate, 3);

        DeviceConfig dc1 = createDeviceConfig(ipInterfaces.get(0), CONFIG_TYPES.get(0), dates.get(0), CONFIG_BYTES.get(0));
        DeviceConfig dc2 = createDeviceConfig(ipInterfaces.get(1), CONFIG_TYPES.get(1), dates.get(1), CONFIG_BYTES.get(1));
        DeviceConfig dc3 = createDeviceConfig(ipInterfaces.get(2), CONFIG_TYPES.get(2), dates.get(2), CONFIG_BYTES.get(2));

        deviceConfigDao.saveOrUpdate(dc1);
        deviceConfigDao.saveOrUpdate(dc2);
        deviceConfigDao.saveOrUpdate(dc3);

        List<Long> ids = List.of(dc1.getId(), dc2.getId(), dc3.getId());
        String idParam = String.join(",", ids.stream().map(id -> id.toString()).collect(Collectors.toList()));

        var response = deviceConfigRestService.downloadDeviceConfig(idParam);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        var responseHeaders = response.getHeaders();
        assertThat(responseHeaders.containsKey("Content-Type"), is(true));
        assertThat(responseHeaders.containsKey("Content-Disposition"), is(true));

        assertThat(responseHeaders.get("Content-Type").get(0).toString(), equalTo("application/gzip"));

        String actualContentDisposition = responseHeaders.get("Content-Disposition").get(0).toString();

        assertThat(actualContentDisposition, startsWith("attachment; filename="));
        assertThat(actualContentDisposition, endsWith(".tar.gz"));

        var pattern = Pattern.compile(".*?filename=(.+)$");
        var matcher = pattern.matcher(actualContentDisposition);

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.groupCount(), equalTo(1));
        final String actualFileName = matcher.group(1);
        assertThat(actualFileName, startsWith("device-configs-"));

        assertThat(response.getEntity(), notNullValue());
        assertThat(response.getEntity(), instanceOf(byte[].class));

        Map<String,byte[]> fileMap = null;

        try {
            byte[] responseBytes = (byte[]) response.getEntity();
            fileMap = CompressionUtils.unTarGzipMultipleFiles(responseBytes);
        } catch (IOException e) {
            Assert.fail("IOException calling CompressionUtils.unTarGzipMultipleFiles");
        }

        assertThat(fileMap, notNullValue());
        assertThat(fileMap.size(), equalTo(RECORD_COUNT));

        Set<String> fileKeys = fileMap.keySet();

        List<String> sortedFileNames = fileMap.keySet().stream().sorted().collect(Collectors.toList());
        assertThat(sortedFileNames.size(), equalTo(3));

        final String fileName = sortedFileNames.get(0);
        assertThat(fileName, startsWith("dcb-1"));
        assertArrayEquals(CONFIG_BYTES.get(0), fileMap.get(fileName));

        final String fileName2 = sortedFileNames.get(1);
        assertThat(fileName2, startsWith("dcb-2"));
        assertArrayEquals(CONFIG_BYTES.get(1), fileMap.get(fileName2));

        final String fileName3 = sortedFileNames.get(2);
        assertThat(fileName3, startsWith("dcb-3"));
        assertArrayEquals(CONFIG_BYTES.get(2), fileMap.get(fileName3));
    }

    private List<OnmsIpInterface> populateDeviceConfigServiceInfo() {
        final var result = this.sessionUtils.withTransaction(() -> {
            List<OnmsIpInterface> ipInterfaces = new ArrayList<>();
            NetworkBuilder builder = new NetworkBuilder();

            List<String> nodeNames = List.of("dcb-1", "dcb-2", "dcb-3");
            List<String> foreignIds = List.of("21", "22", "23");
            List<String> ipAddresses = List.of("192.168.3.1", "192.168.3.2", "192.168.3.3");

            List<String> scheduleIntervals = List.of("daily", "weekly", "monthly");

            for (int i = 0; i < RECORD_COUNT; i++) {
                builder.addNode(nodeNames.get(i)).setForeignSource("imported:").setForeignId(foreignIds.get(i)).setType(OnmsNode.NodeType.ACTIVE);
                builder.addInterface(ipAddresses.get(i)).setIsManaged("M").setIsSnmpPrimary("P");
                builder.addService(addOrGetServiceType("DeviceConfig-" + CONFIG_TYPES.get(i)));
                builder.setServiceMetaDataEntry("requisition", "dcb:schedule", CRON_SCHEDULES.get(i));
                nodeDao.saveOrUpdate(builder.getCurrentNode());

                OnmsIpInterface ipInterface = builder.getCurrentNode().getIpInterfaceByIpAddress(ipAddresses.get(i));
                ipInterfaces.add(ipInterface);
            }

            nodeDao.flush();

            return ipInterfaces;
        });

        PollerConfigFactory.getInstance().rebuildPackageIpListMap();
        return result;
    }

    private OnmsServiceType addOrGetServiceType(final String serviceName) {
        OnmsServiceType serviceType = serviceTypeDao.findByName(serviceName);

        if (serviceType == null) {
            serviceType = new OnmsServiceType(serviceName);
            serviceTypeDao.save(serviceType);
            serviceTypeDao.flush();
        }

        return serviceType;
    }

    private static DeviceConfig createDeviceConfig(OnmsIpInterface ipInterface1, String configType, Date date, byte[] config) {
        var dc = new DeviceConfig();
        dc.setConfig(config);
        dc.setLastUpdated(date);
        dc.setLastSucceeded(date);
        dc.setCreatedTime(date);
        dc.setEncoding(DefaultDeviceConfigRestService.DEFAULT_ENCODING);
        dc.setIpInterface(ipInterface1);
        dc.setServiceName("DeviceConfig-" + configType);
        dc.setConfigType(configType);

        return dc;
    }

    private static List<Date> getTestDates(Date currentDate, int count) {
        return IntStream.range(1, count + 1).boxed()
            .sorted(Collections.reverseOrder())
            .map(seconds -> Date.from(currentDate.toInstant().minusSeconds(seconds)))
            .collect(Collectors.toList());
    }
}
