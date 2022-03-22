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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

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
        Assert.assertEquals(RECORD_COUNT, ipInterfaces.size());

        // sanity check that nodes and interfaces were created correctly
        List<Integer> ipInterfaceIds = ipInterfaces.stream().map(OnmsIpInterface::getId).collect(Collectors.toList());

        List<DeviceConfigService.RetrievalDefinition> services = ipInterfaces.stream()
                                                                             .flatMap(iface -> deviceConfigService.getRetrievalDefinitions(InetAddressUtils.str(iface.getIpAddress()), iface.getNode().getLocation().getLocationName()).stream())
                                                                             .collect(Collectors.toList());
        Assert.assertEquals(RECORD_COUNT, services.size());

        // Add DeviceConfig entries mapped to ipInterfaces and services
        deviceConfigDao.saveOrUpdate(createDeviceConfig(1, ipInterfaces.get(0), "default"));
        deviceConfigDao.saveOrUpdate(createDeviceConfig(2, ipInterfaces.get(1), "running"));
        deviceConfigDao.saveOrUpdate(createDeviceConfig(3, ipInterfaces.get(2), "wurstblinker"));

        Date currentDate = new Date();
        List<DeviceConfigDTO> responseList = getDeviceConfigs(10, 0, "lastUpdated", "asc", null, null, null, null, null, null);

        Assert.assertEquals(RECORD_COUNT, responseList.size());

        List<String> expectedConfigTypes = List.of("default", "running", "wurstblinker");

        for (int i = 0; i < RECORD_COUNT; i++) {
            DeviceConfigDTO dto = responseList.get(i);
            final int version = i + 1;

            Assert.assertEquals(ipInterfaceIds.get(i).intValue(), dto.getMonitoredServiceId());
            assertThat(expectedConfigTypes.get(i).equalsIgnoreCase(dto.getConfigType()), is(true));
            Assert.assertEquals(Integer.toString(version), dto.getEncoding());
            Assert.assertEquals(createdTime(version), dto.getLastBackupDate().getTime());
            Assert.assertEquals(createdTime(version), dto.getLastUpdatedDate().getTime());
            Assert.assertEquals(createdTime(version), dto.getLastSucceededDate().getTime());
            Assert.assertNull(dto.getLastFailedDate());
            Assert.assertNull(dto.getFailureReason());
            Assert.assertEquals(EXPECTED_CRON_SCHEDULE_DESCRIPTIONS.get(i), dto.getScheduledInterval().get("DeviceConfig-" + expectedConfigTypes.get(i)));
            assertThat(dto.getNextScheduledBackupDate().after(currentDate), is(true));
        }
        });
    }

    @Test
    public void testDownloadNoDeviceConfig() {
        List<String> idParams = new ArrayList<String>();
        idParams.add(null);
        idParams.add("");

        for (String id : idParams) {
            var response = deviceConfigRestService.downloadDeviceConfig(id);
            Assert.assertNotNull(response);
            Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testDownloadInvalidRequest() {
        final List<String> idParams = List.of("abc", "a,b,c", ",,,,,0a", ";", "123,,", "123,,,456", ",123");

        for (String id : idParams) {
            var response = deviceConfigRestService.downloadDeviceConfig(id);
            Assert.assertNotNull(response);
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    @Transactional
    public void testDownloadSingleDeviceConfig() {
        // Add nodes, interfaces, services
        List<OnmsIpInterface> ipInterfaces = populateDeviceConfigServiceInfo();
        Assert.assertEquals(RECORD_COUNT, ipInterfaces.size());

        // Add DeviceConfig entries mapped to ipInterfaces and services
        // Save off 2nd one to check below
        deviceConfigDao.saveOrUpdate(createDeviceConfig(1, ipInterfaces.get(0), "default"));
        DeviceConfig dc = createDeviceConfig(2, ipInterfaces.get(1), "default");
        deviceConfigDao.saveOrUpdate(dc);
        deviceConfigDao.saveOrUpdate(createDeviceConfig(3, ipInterfaces.get(2), "running"));

        var response = deviceConfigRestService.downloadDeviceConfig(dc.getId().toString());

        Assert.assertNotNull(response);
        var headerMap = response.getHeaders();

        Assert.assertEquals("text/plain;charset=UTF-8", headerMap.get("Content-Type").get(0).toString());

        String expectedFileName = DefaultDeviceConfigRestService.createDownloadFileName(
            "dcb-2", "192.168.3.2", "default", dc.getCreatedTime());
        String expectedContentDisposition = "attachment; filename=" + expectedFileName;
        String actualContentDisposition = headerMap.get("Content-Disposition").get(0).toString();
        Assert.assertEquals(expectedContentDisposition, actualContentDisposition);

        Object responseObj = response.getEntity();
        byte[] responseBytes = (byte[]) response.getEntity();

        Assert.assertArrayEquals(dc.getConfig(), responseBytes);
    }

    @Test
    @Transactional
    public void testDownloadMultipleDeviceConfigs() {
        // Add nodes, interfaces, services
        List<OnmsIpInterface> ipInterfaces = populateDeviceConfigServiceInfo();
        Assert.assertEquals(RECORD_COUNT, ipInterfaces.size());

        // Add DeviceConfig entries mapped to ipInterfaces and services
        // Save off 2nd one to check below
        DeviceConfig dc1 = createDeviceConfig(1, ipInterfaces.get(0), "default");
        deviceConfigDao.saveOrUpdate(dc1);
        DeviceConfig dc2 = createDeviceConfig(2, ipInterfaces.get(1), "default");
        deviceConfigDao.saveOrUpdate(dc2);
        DeviceConfig dc3 = createDeviceConfig(3, ipInterfaces.get(2), "running");
        deviceConfigDao.saveOrUpdate(dc3);

        List<Long> ids = List.of(dc1.getId(), dc2.getId(), dc3.getId());
        String idParam = String.join(",", ids.stream().map(id -> id.toString()).collect(Collectors.toList()));

        var response = deviceConfigRestService.downloadDeviceConfig(idParam);

        Assert.assertNotNull(response);
        var headerMap = response.getHeaders();

        Assert.assertEquals("application/gzip", headerMap.get("Content-Type").get(0).toString());

        String actualContentDisposition = headerMap.get("Content-Disposition").get(0).toString();

        Assert.assertTrue(actualContentDisposition.startsWith("attachment; filename="));
        Assert.assertTrue(actualContentDisposition.endsWith(".tar.gz"));

        var pattern = Pattern.compile(".*?filename=(.+)$");
        var matcher = pattern.matcher(actualContentDisposition);

        Assert.assertTrue(matcher.matches());
        Assert.assertEquals(1, matcher.groupCount());
        final String actualFileName = matcher.group(1);
        Assert.assertTrue(actualFileName.startsWith("device-configs-"));

        Assert.assertNotNull(response.getEntity());
        Assert.assertThat(response.getEntity(), instanceOf(byte[].class));

        Map<String,byte[]> fileMap = null;

        try {
            byte[] responseBytes = (byte[]) response.getEntity();
            fileMap = CompressionUtils.unTarGzipMultipleFiles(responseBytes);
        } catch (IOException e) {
            Assert.fail("IOException calling CompressionUtils.unTarGzipMultipleFiles");
        }

        Assert.assertNotNull(fileMap);
        Assert.assertEquals(RECORD_COUNT, fileMap.size());

        Set<String> fileKeys = fileMap.keySet();

        List<String> sortedFileNames = fileMap.keySet().stream().sorted().collect(Collectors.toList());
        Assert.assertEquals(3, sortedFileNames.size());

        final String fileName = sortedFileNames.get(0);
        assertThat(fileName, startsWith("dcb-1"));
        Assert.assertArrayEquals(new byte[] { 1 }, fileMap.get(fileName));

        final String fileName2 = sortedFileNames.get(1);
        assertThat(fileName2, startsWith("dcb-2"));
        Assert.assertArrayEquals(new byte[] { 2 }, fileMap.get(fileName2));

        final String fileName3 = sortedFileNames.get(2);
        assertThat(fileName3, startsWith("dcb-3"));
        Assert.assertArrayEquals(new byte[] { 3 }, fileMap.get(fileName3));
    }

    private List<OnmsIpInterface> populateDeviceConfigServiceInfo() {
        final var result = this.sessionUtils.withTransaction(() -> {
        List<OnmsIpInterface> ipInterfaces = new ArrayList<>();
        NetworkBuilder builder = new NetworkBuilder();

        List<String> nodeNames = List.of("dcb-1", "dcb-2", "dcb-3");
        List<String> foreignIds = List.of("21", "22", "23");
        List<String> ipAddresses = List.of("192.168.3.1", "192.168.3.2", "192.168.3.3");
        List<String> serviceNames = List.of(
            "DeviceConfig-default",
            "DeviceConfig-running",
            "DeviceConfig-wurstblinker");

        List<String> scheduleIntervals = List.of("daily", "weekly", "monthly");

        for (int i = 0; i < RECORD_COUNT; i++) {
            builder.addNode(nodeNames.get(i)).setForeignSource("imported:").setForeignId(foreignIds.get(i)).setType(OnmsNode.NodeType.ACTIVE);
            builder.addInterface(ipAddresses.get(i)).setIsManaged("M").setIsSnmpPrimary("P");
            builder.addService(addOrGetServiceType(serviceNames.get(i)));
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

    private List<DeviceConfigDTO> getDeviceConfigs(
        Integer limit,
        Integer offset,
        String orderBy,
        String order,
        String deviceName,
        String ipAddress,
        Integer ipInterfaceId,
        String configType,
        Long createdAfter,
        Long createdBefore
    ) {
        var response = deviceConfigRestService.getDeviceConfigs(limit, offset, orderBy, order, deviceName, ipAddress, ipInterfaceId, configType, createdAfter, createdBefore);
        if (response.hasEntity()) {
            return (List<DeviceConfigDTO>) response.getEntity();
        } else {
            return Collections.emptyList();
        }
    }

    private static DeviceConfig createDeviceConfig(int version, OnmsIpInterface ipInterface1, String configType) {
        Date date = new Date(createdTime(version));

        var dc = new DeviceConfig();
        dc.setConfig(new byte[] { (byte) (version % 128) });
        dc.setLastUpdated(date);
        dc.setLastSucceeded(date);
        dc.setCreatedTime(date);
        dc.setEncoding(String.valueOf(version));
        dc.setIpInterface(ipInterface1);
        dc.setServiceName("DeviceConfig-" + configType);
        dc.setConfigType(configType);

        return dc;
    }

    private static long createdTime(int num) {
        return num * 1000L * 60 * 60 * 24;
    }
}
