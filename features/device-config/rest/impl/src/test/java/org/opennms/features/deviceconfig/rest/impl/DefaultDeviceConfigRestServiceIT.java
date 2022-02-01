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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigDto;
import org.opennms.features.deviceconfig.rest.api.DeviceConfigRestService;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionOperations;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultDeviceConfigRestServiceIT {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    @Autowired
    private TransactionOperations operations;

    private DeviceConfigRestService deviceConfigRestService;

    private static int INTERFACES = 2;
    private static int VERSIONS = 35;

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
        deviceConfigRestService = new DefaultDeviceConfigRestService(deviceConfigDao);
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

    private List<DeviceConfigDto> getDeviceConfigs(
            Integer limit,
            Integer offset,
            String orderBy,
            String order,
            Integer ipInterfaceId,
            String deviceType,
            Long createdAfter,
            Long createdBefore
    ) {
        var response = deviceConfigRestService.getDeviceConfigs(limit, offset, orderBy, order, ipInterfaceId, deviceType, createdAfter, createdBefore);
        if (response.hasEntity()) {
            return (List<DeviceConfigDto>) response.getEntity();
        } else {
            return Collections.emptyList();
        }
    }

    @Test
    public void retrieveAll() {
        var res = getDeviceConfigs(null, null, null, null, null, null, null, null);
        assertThat(res, hasSize(VERSIONS * INTERFACES));
        for (var itf : interfaces) {
            var set = res.stream().filter(dc -> dc.getIpInterfaceId() == itf.getId()).collect(Collectors.toSet());
            assertThat(set, hasSize(VERSIONS));
        }
    }

    @Test
    public void filterOnInterface() {
        for (var itf : interfaces) {
            var res = getDeviceConfigs(null, null, null, null, itf.getId(), null, null, null);
            assertThat(res, hasSize(VERSIONS));
            assertThat(res, everyItem(hasProperty("ipInterfaceId", is(itf.getId()))));
        }
    }

    @Test
    public void filterOnDeviceType() {
        for (int v = 0; v < VERSIONS; v++) {
            var res = getDeviceConfigs(null, null, null, null, null, String.valueOf(v), null, null);
            assertThat(res, hasSize(INTERFACES));
        }
    }

    @Test
    public void filterOnCreatedTime() {
        for (var itf : interfaces) {
            {
                var createdAfter = createdTime(5);
                var res = getDeviceConfigs(null, null, null, null, itf.getId(), null, createdAfter, null);
                assertThat(res, hasSize(VERSIONS - 5));
                assertThat(res, everyItem(hasProperty("createdTime", Matchers.greaterThanOrEqualTo(new Date(createdAfter)))));
            }
            {
                var createdBefore = createdTime(5);
                var res = getDeviceConfigs(null, null, null, null, itf.getId(), null, null, createdBefore);
                assertThat(res, hasSize(6));
                assertThat(res, everyItem(hasProperty("createdTime", Matchers.lessThanOrEqualTo(new Date(createdBefore)))));
            }
            {
                var createdAfter = createdTime(5);
                var createdBefore = createdTime(10);
                var res = getDeviceConfigs(null, null, null, null, itf.getId(), null, createdAfter, createdBefore);
                assertThat(res, hasSize(6));
                assertThat(res, everyItem(hasProperty("createdTime", Matchers.greaterThanOrEqualTo(new Date(createdAfter)))));
                assertThat(res, everyItem(hasProperty("createdTime", Matchers.lessThanOrEqualTo(new Date(createdBefore)))));
            }
        }
    }

    @Test
    public void sortDesc() {
        for (var itf : interfaces) {
            var res = getDeviceConfigs(null, null, "version", "desc", itf.getId(), null, null, null);
            assertThat(res, hasSize(VERSIONS));
            assertThat(res.stream().map(DeviceConfigDto::getVersion).collect(Collectors.toList()), contains(IntStream.range(0, VERSIONS).map(v -> VERSIONS - 1 - v).boxed().toArray()));
        }
    }

    @Test
    public void sortAsc() {
        for (var itf : interfaces) {
            var res = getDeviceConfigs(null, null, "version", "asc", itf.getId(), null, null, null);
            assertThat(res, hasSize(VERSIONS));
            assertThat(res.stream().map(DeviceConfigDto::getVersion).collect(Collectors.toList()), contains(IntStream.range(0, VERSIONS).boxed().toArray()));
        }
    }

    @Test
    public void sortAscWithLimitAndOffset() {
        for (var itf : interfaces) {
            {
                var limit = 5;
                var res = getDeviceConfigs(limit, 0, "version", "asc", itf.getId(), null, null, null);
                assertThat(res, hasSize(limit));
                assertThat(res.stream().map(DeviceConfigDto::getVersion).collect(Collectors.toList()), contains(IntStream.range(0, limit).boxed().toArray()));
            }
            {
                var limit = 5;
                var offset = 10;
                var res = getDeviceConfigs(limit, offset, "version", "asc", itf.getId(), null, null, null);
                assertThat(res, hasSize(limit));
                assertThat(res.stream().map(DeviceConfigDto::getVersion).collect(Collectors.toList()), contains(IntStream.range(offset, offset + limit).boxed().toArray()));
            }
        }
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
        dc.setDeviceType(String.valueOf(version));
        dc.setCreatedTime(new Date(createdTime(version)));
        dc.setEncoding(String.valueOf(version));
        dc.setIpInterface(ipInterface1);
        dc.setVersion(version);
        return dc;
    }

    private static long createdTime(int num) {
        return num * 1000l * 60 * 60 * 24;
    }

}
