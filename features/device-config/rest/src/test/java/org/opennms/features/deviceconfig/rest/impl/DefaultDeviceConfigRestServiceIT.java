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
package org.opennms.features.deviceconfig.rest.impl;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.features.deviceconfig.rest.BackupRequestDTO;
import org.opennms.features.deviceconfig.rest.BackupResponseDTO;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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
        deviceConfigRestService = new DefaultDeviceConfigRestService(deviceConfigDao, deviceConfigService, operations);
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

    @Test
    public void testBackupConfigService() throws IOException {
        String ipAddress = "127.0.0.1";
        String invalidIpAddress = "127.258.1.258";
        String message = "Invalid Ip Interface";
        CompletableFuture<Boolean> success = new CompletableFuture<>();
        Mockito.doReturn(success).when(deviceConfigService).triggerConfigBackup(Mockito.eq(ipAddress),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doThrow(new IllegalArgumentException(message)).when(deviceConfigService).triggerConfigBackup(Mockito.eq(invalidIpAddress),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean());

        var dto = new BackupRequestDTO(ipAddress, "MINION", "default", false);
        Response response = deviceConfigRestService.triggerDeviceConfigBackup(List.of(dto));
        assertThat(response.getStatusInfo().toEnum(), Matchers.is(Response.Status.ACCEPTED));

        var invalidDto = new BackupRequestDTO(invalidIpAddress, "MINION", "default", false);
        response = deviceConfigRestService.triggerDeviceConfigBackup(List.of(invalidDto));
        assertThat(response.getStatusInfo().toEnum(), Matchers.is(Response.Status.BAD_REQUEST));
        assertThat(response.getEntity(), Matchers.is(message));

        // if any fail, Multi response entity is returned
        response = deviceConfigRestService.triggerDeviceConfigBackup(List.of(dto, invalidDto));
        assertThat(response.getStatusInfo().getStatusCode(), Matchers.is(207));
        assertThat(response.getEntity(), Matchers.notNullValue());
        List<BackupResponseDTO> responseDTOList = (List<BackupResponseDTO>) response.getEntity();
        assertThat(responseDTOList.size(), Matchers.is(1));
        assertThat(responseDTOList.get(0).getStatus(), Matchers.is(400));

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
        dc.setId(Long.valueOf(version));
        dc.setCreatedTime(new Date(createdTime(version)));
        dc.setEncoding(DefaultDeviceConfigRestService.DEFAULT_ENCODING);
        dc.setIpInterface(ipInterface1);
        dc.setServiceName("DeviceConfig-default");
        dc.setLastUpdated(new Date(createdTime(version)));
        dc.setStatus(DeviceConfigStatus.SUCCESS);

        return dc;
    }

    private static long createdTime(int num) {
        return num * 1000L * 60 * 60 * 24;
    }

    @Test
    public void testDeleteDeviceConfigs() throws IOException {
        List<Long>  lstOfIds = new ArrayList<>();
        for(DeviceConfig dc : (List<DeviceConfig>)deviceConfigDao.findAll()){
            lstOfIds.add(dc.getId());
        }
        //passing valid ids
        Response response = deviceConfigRestService.deleteDeviceConfigs(lstOfIds.subList(1,2));
        Assert.assertEquals(204, response.getStatus());

        //passing empty or blank value of ids
        response = deviceConfigRestService.deleteDeviceConfigs(new ArrayList<>());
        Assert.assertEquals(400, response.getStatus());

        //passing empty or blank value of ids
        response = deviceConfigRestService.deleteDeviceConfigs(Arrays.asList(new Long []{1000L}));
        Assert.assertEquals(404, response.getStatus());

        //deleting single valid id
        response = deviceConfigRestService.deleteDeviceConfig(lstOfIds.get(0));
        Assert.assertEquals(204, response.getStatus());

        //trying to delete single not valid id
        response = deviceConfigRestService.deleteDeviceConfig(1000);
        Assert.assertEquals(404, response.getStatus());

    }
}
