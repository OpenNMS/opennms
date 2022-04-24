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

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.rest.BackupRequestDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigDTO;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;
import org.opennms.features.deviceconfig.service.DeviceConfigService;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import javax.ws.rs.core.Response;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultDeviceConfigRestServiceIT {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    private TransactionOperations operations;

    private DeviceConfigRestService deviceConfigRestService;

    private DeviceConfigService deviceConfigService;

    private static final int INTERFACES = 2;
    private static final int VERSIONS = 35;

    private static OnmsIpInterface[] interfaces;

    @Before
    public void before() {
        operations.execute(status -> {
            interfaces = IntStream.range(0, INTERFACES).mapToObj(i -> populateIpInterfaceAndGet(i)).toArray(size -> new OnmsIpInterface[size]);
            for (int version = 0; version < VERSIONS; version++) {
                for (var itf : interfaces) {
                    deviceConfigDao.save(createDeviceConfig(version, itf));
                }
            }
            return null;
        });
        deviceConfigService = Mockito.mock(DeviceConfigService.class);
        deviceConfigRestService = new DefaultDeviceConfigRestService(deviceConfigDao, deviceConfigService);
    }

    @After
    public void after() {
        operations.execute(status -> {
            for (var dc : deviceConfigDao.findAll()) {
                deviceConfigDao.delete(dc);
            }
            for (var itf : interfaces) {
                nodeDao.delete(itf.getNode());
            }
            return null;
        });
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
            Set<DeviceConfigStatus> statuses,
            Long createdAfter,
            Long createdBefore
    ) {
        var response = deviceConfigRestService.getDeviceConfigs(limit, offset, orderBy, order, deviceName, ipAddress, ipInterfaceId, configType, statuses, createdAfter, createdBefore);
        if (response.hasEntity()) {
            return (List<DeviceConfigDTO>) response.getEntity();
        } else {
            return Collections.emptyList();
        }
    }

    @Ignore("Fix to work with DeviceConfigDao.getLatestConfigs() or delete.")
    @Test
    @Transactional
    public void retrieveAll() {
        var res = getDeviceConfigs(null, null, null, null, null, null, null, null, null, null, null);
        assertThat(res, hasSize(VERSIONS * INTERFACES));
        for (var itf : interfaces) {
            var set = res.stream().filter(dc -> dc.getIpInterfaceId() == itf.getId()).collect(Collectors.toSet());
            assertThat(set, hasSize(VERSIONS));
        }
    }

    @Test
    @Transactional
    public void filterOnInterface() {
        for (var itf : interfaces) {
            var res = getDeviceConfigs(null, null, null, null, null, null, itf.getId(), null, null, null, null);
            assertThat(res, hasSize(VERSIONS));
            assertThat(res, everyItem(hasProperty("ipInterfaceId", is(itf.getId()))));
        }
    }

    @Test
    @Transactional
    public void filterOnLastBackupDate() {
        for (var itf : interfaces) {
            {
                var createdAfter = createdTime(5);
                List<DeviceConfigDTO> configList = getDeviceConfigs(null, null, null, null, null, null, itf.getId(), null, null, null, null);
                var res = getDeviceConfigs(null, null, null, null, null, null, itf.getId(), null, null, createdAfter, null);
                assertThat(res, hasSize(VERSIONS - 5));
                assertThat(res, everyItem(hasProperty("lastBackupDate", Matchers.greaterThanOrEqualTo(new Date(createdAfter)))));
            }
            {
                var createdBefore = createdTime(5);
                var res = getDeviceConfigs(null, null, null, null, null, null, itf.getId(), null, null, null, createdBefore);
                assertThat(res, hasSize(6));
                assertThat(res, everyItem(hasProperty("lastBackupDate", Matchers.lessThanOrEqualTo(new Date(createdBefore)))));
            }
            {
                var createdAfter = createdTime(5);
                var createdBefore = createdTime(10);
                var res = getDeviceConfigs(null, null, null, null, null, null, itf.getId(), null, null, createdAfter, createdBefore);
                assertThat(res, hasSize(6));
                assertThat(res, everyItem(hasProperty("lastBackupDate", Matchers.greaterThanOrEqualTo(new Date(createdAfter)))));
                assertThat(res, everyItem(hasProperty("lastBackupDate", Matchers.lessThanOrEqualTo(new Date(createdBefore)))));
            }
        }
    }

    @Test
    @Transactional
    public void sortDesc() {
        for (var itf : interfaces) {
            var res = getDeviceConfigs(null, null, "createdTime", "desc", null, null, itf.getId(), null, null, null, null);
            assertThat(res, hasSize(VERSIONS));

            assertThat(
                res.stream().map(DeviceConfigDTO::getLastBackupDate).map(Date::getTime).collect(toList()),
                contains(IntStream.range(0, VERSIONS).map(i -> VERSIONS - i - 1).boxed().map(DefaultDeviceConfigRestServiceIT::createdTime).toArray()));
        }
    }

    @Ignore("Fix to work with DeviceConfigDao.getLatestConfigs() or delete.")
    @Test
    @Transactional
    public void sortAsc() {
        for (var itf : interfaces) {
            var res = getDeviceConfigs(null, null, "createdTime", "asc", null, null, itf.getId(), null, null, null, null);
            assertThat(res, hasSize(VERSIONS));

            assertThat(
                res.stream().map(DeviceConfigDTO::getLastBackupDate).map(Date::getTime).collect(toList()),
                contains(IntStream.range(0, VERSIONS).boxed().map(DefaultDeviceConfigRestServiceIT::createdTime).toArray()));
        }
    }

    @Ignore("Fix to work with DeviceConfigDao.getLatestConfigs() or delete.")
    @Test
    @Transactional
    public void sortAscWithLimitAndOffset() {
        for (var itf : interfaces) {
            {
                final int limit = 5;
                var res = getDeviceConfigs(limit, 0, "createdTime", "asc", null, null, itf.getId(), null, null, null, null);
                assertThat(res, hasSize(limit));

                assertThat(
                    res.stream().map(DeviceConfigDTO::getLastBackupDate).map(Date::getTime).collect(toList()),
                    contains(IntStream.range(0, limit).boxed().map(DefaultDeviceConfigRestServiceIT::createdTime).toArray()));
            }
            {
                final int limit = 5;
                final int offset = 10;
                var res = getDeviceConfigs(limit, offset, "createdTime", "asc", null, null, itf.getId(), null, null, null, null);
                assertThat(res, hasSize(limit));

                assertThat(
                    res.stream().map(DeviceConfigDTO::getLastBackupDate).map(Date::getTime).collect(toList()),
                    contains(IntStream.range(offset, offset + limit).boxed().map(DefaultDeviceConfigRestServiceIT::createdTime).toArray()));
            }
        }
    }

    @Test
    public void testBackupConfigService() throws IOException {
        String ipAddress = "127.0.0.1";
        String invalidIpAddress = "127.258.1.258";
        String message = "Invalid Ip Interface";
        Mockito.doNothing().when(deviceConfigService).triggerConfigBackup(Mockito.eq(ipAddress),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doThrow(new IllegalArgumentException(message)).when(deviceConfigService).triggerConfigBackup(Mockito.eq(invalidIpAddress),
                Mockito.anyString(), Mockito.anyString());

        var dto = new BackupRequestDTO(ipAddress, "MINION", "default");
        Response response = deviceConfigRestService.triggerDeviceConfigBackup(List.of(dto));
        assertThat(response.getStatusInfo().toEnum(), Matchers.is(Response.Status.ACCEPTED));

        var invalidDto = new BackupRequestDTO(invalidIpAddress, "MINION", "default");
        response = deviceConfigRestService.triggerDeviceConfigBackup(List.of(invalidDto));
        assertThat(response.getStatusInfo().toEnum(), Matchers.is(Response.Status.BAD_REQUEST));
        assertThat(response.getEntity(), Matchers.is(message));

        // if any fail, BAD_REQUEST is returned
        response = deviceConfigRestService.triggerDeviceConfigBackup(List.of(dto, invalidDto));
        assertThat(response.getStatusInfo().toEnum(), Matchers.is(Response.Status.BAD_REQUEST));
        assertThat(response.getEntity(), Matchers.is(message));

        final String nullOrEmptyMessage = "Cannot trigger config backup on empty request list";

        response = deviceConfigRestService.triggerDeviceConfigBackup(null);
        assertThat(response.getStatusInfo().toEnum(), Matchers.is(Response.Status.BAD_REQUEST));
        assertThat(response.getEntity(), Matchers.is(nullOrEmptyMessage));

        response = deviceConfigRestService.triggerDeviceConfigBackup(new ArrayList<>());
        assertThat(response.getStatusInfo().toEnum(), Matchers.is(Response.Status.BAD_REQUEST));
        assertThat(response.getEntity(), Matchers.is(nullOrEmptyMessage));
    }

    private OnmsIpInterface populateIpInterfaceAndGet(int num) {
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("node" + num).setForeignSource("imported:").setForeignId(String.valueOf(num)).setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.2." + num).setIsManaged("M").setIsSnmpPrimary("P");
        nodeDao.saveOrUpdate(builder.getCurrentNode());
        Assert.assertThat(builder.getCurrentNode().getIpInterfaces(), hasSize(1));
        Set<OnmsIpInterface> ipInterfaces = builder.getCurrentNode().getIpInterfaces();
        OnmsIpInterface ipInterface = ipInterfaces.iterator().next();
        Assert.assertNotNull(ipInterface);
        return ipInterface;
    }

    private static DeviceConfig createDeviceConfig(int version, OnmsIpInterface ipInterface1) {
        var dc = new DeviceConfig();
        dc.setConfig(new byte[version]);
        dc.setCreatedTime(new Date(createdTime(version)));
        dc.setEncoding(DefaultDeviceConfigRestService.DEFAULT_ENCODING);
        dc.setIpInterface(ipInterface1);
        dc.setServiceName("DeviceConfig-default");
        dc.setLastUpdated(new Date(createdTime(version)));
        dc.setStatus(DeviceConfig.determineBackupStatus(dc));

        return dc;
    }

    private static long createdTime(int num) {
        return num * 1000L * 60 * 60 * 24;
    }
}
