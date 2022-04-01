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

package org.opennms.features.deviceconfig.persistence;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigQueryResult;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.HOURS;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class DeviceConfigDaoIT {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private DeviceConfigDao deviceConfigDao;

    private OnmsIpInterface ipInterface;


    @Test
    @Transactional
    public void testPersistenceOfDeviceConfig() {
        populateIpInterface();
        Date currentDate = new Date();

        DeviceConfig deviceConfig = new DeviceConfig();
        deviceConfig.setIpInterface(ipInterface);
        deviceConfig.setConfig(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        deviceConfig.setEncoding("ASCII");
        deviceConfig.setLastUpdated(currentDate);
        deviceConfig.setCreatedTime(currentDate);
        deviceConfig.setLastSucceeded(currentDate);
        deviceConfig.setConfigType(ConfigType.Default);
        deviceConfigDao.saveOrUpdate(deviceConfig);

        List<DeviceConfig> configs = deviceConfigDao.findAll();
        Assert.assertThat(configs, Matchers.not(Matchers.empty()));

        DeviceConfig retrieved = configs.get(0);
        Assert.assertNotNull(retrieved);
        Assert.assertEquals(ipInterface, retrieved.getIpInterface());
        Assert.assertEquals(deviceConfig.getConfig(), retrieved.getConfig());
        Assert.assertEquals(currentDate, deviceConfig.getCreatedTime());
        Assert.assertEquals(currentDate, deviceConfig.getLastUpdated());
        Assert.assertEquals(currentDate, deviceConfig.getLastSucceeded());
        Assert.assertNull(deviceConfig.getLastFailed());
    }

    @Test
    public void testFetchDeviceConfigSortedByDate() {
        populateIpInterface();
        final int count = 10;
        populateDeviceConfigs(count, ipInterface);

        List<DeviceConfig> deviceConfigList = deviceConfigDao.findAll();
        Assert.assertEquals(count, deviceConfigList.size());
        deviceConfigList = deviceConfigDao.findConfigsForInterfaceSortedByDate(ipInterface, "DeviceConfig-default");
        Assert.assertEquals(count, deviceConfigList.size());
        DeviceConfig deviceConfig = deviceConfigList.get(0);
        Assert.assertNotNull(deviceConfig);

        // Take middle element and update its created time.
        // This is not the way we should update versions of the latest. This is just for the test.
        DeviceConfig middleElement = deviceConfigList.get((count / 2) - 1);
        middleElement.setLastUpdated(Date.from(Instant.now().plus(1, HOURS)));
        deviceConfigDao.saveOrUpdate(middleElement);
        deviceConfigList = deviceConfigDao.findConfigsForInterfaceSortedByDate(ipInterface, "DeviceConfig-default");

        DeviceConfig retrievedMiddleElement = deviceConfigList.get(0);
        Optional<DeviceConfig> latestElementOptional = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");

        Assert.assertTrue(latestElementOptional.isPresent());
        DeviceConfig latestConfig = latestElementOptional.get();
        Assert.assertArrayEquals(retrievedMiddleElement.getConfig(), latestConfig.getConfig());

        // Populate failed retrieval.
        populateFailedRetrievalDeviceConfig(ipInterface);
        Optional<DeviceConfig> elementWithFailedConfig = deviceConfigDao.getLatestConfigForInterface(ipInterface, "DeviceConfig-default");
        Assert.assertTrue(elementWithFailedConfig.isPresent());

        // Verify that last failed got updated and it is same as last updated.
        DeviceConfig retrievedConfig = elementWithFailedConfig.get();
        Assert.assertEquals(retrievedConfig.getLastUpdated(), retrievedConfig.getLastFailed());
    }

    @Test
    public void testGetLatestConfigOnEachInterface() {
        Set<OnmsIpInterface> ipInterfaces = populateIpInterfaces();
        int count = 10;
        ipInterfaces.forEach(ipInterface -> populateDeviceConfigs(count, ipInterface));
        List<DeviceConfigQueryResult> results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, null);
        Assert.assertThat(results, Matchers.hasSize(5));
        Iterator<OnmsIpInterface> iterator = ipInterfaces.iterator();
        Assert.assertArrayEquals(results.get(0).getConfig(),
                (iterator.next().getInterfaceId() + ":" + (count - 1)).getBytes(Charset.defaultCharset()));
        Assert.assertArrayEquals(results.get(1).getConfig(),
                (iterator.next().getInterfaceId() + ":" + (count - 1)).getBytes(Charset.defaultCharset()));

        // Should give one result.
        results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, InetAddressUtils.str(iterator.next().getIpAddress()));
        Assert.assertThat(results, Matchers.hasSize(1));

        // Should give no results.
        results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, "192.168.32.254");
        Assert.assertThat(results, Matchers.hasSize(0));

        // Should give all 5 interfaces
        results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, iterator.next().getNode().getLabel());
        Assert.assertThat(results, Matchers.hasSize(5));

        ipInterfaces.forEach(this::populateFailedRetrievalDeviceConfig);

        results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, iterator.next().getNode().getLabel());

        Assert.assertThat(results.get(0).getFailureReason(), Matchers.notNullValue());
    }

    private void populateDeviceConfigs(int count, OnmsIpInterface ipInterface) {
        for (int i = 0; i < count; i++) {
            DeviceConfig deviceConfig = new DeviceConfig();
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setServiceName("DeviceConfig-default");
            deviceConfig.setConfig((ipInterface.getInterfaceId() + ":" + i).getBytes(Charset.defaultCharset()));
            deviceConfig.setEncoding(Charset.defaultCharset().name());
            deviceConfig.setCreatedTime(Date.from(Instant.now().plusSeconds(i * 60)));
            deviceConfig.setConfigType(ConfigType.Default);
            deviceConfig.setLastUpdated(Date.from(Instant.now().plusSeconds(i * 60)));
            deviceConfigDao.saveOrUpdate(deviceConfig);
        }
    }

    private void populateFailedRetrievalDeviceConfig(OnmsIpInterface ipInterface) {
        DeviceConfig deviceConfig = new DeviceConfig();
        deviceConfig.setIpInterface(ipInterface);
        deviceConfig.setServiceName("DeviceConfig-default");
        deviceConfig.setEncoding(Charset.defaultCharset().name());
        deviceConfig.setConfigType(ConfigType.Default);
        deviceConfig.setLastUpdated(Date.from(Instant.now().plus(2, HOURS)));
        deviceConfig.setLastFailed(Date.from(Instant.now().plus(2, HOURS)));
        deviceConfig.setFailureReason("Not able to connect to SSHServer");
        deviceConfigDao.saveOrUpdate(deviceConfig);
    }

    private void populateIpInterface() {
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P");

        nodeDao.saveOrUpdate(builder.getCurrentNode());

        Assert.assertThat(builder.getCurrentNode().getIpInterfaces(), Matchers.hasSize(1));
        Set<OnmsIpInterface> ipInterfaces = builder.getCurrentNode().getIpInterfaces();
        OnmsIpInterface ipInterface = ipInterfaces.iterator().next();
        Assert.assertNotNull(ipInterface);

        this.ipInterface = ipInterface;
    }


    private Set<OnmsIpInterface> populateIpInterfaces() {
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addInterface("192.168.2.2").setIsManaged("M");
        builder.addInterface("192.168.2.3").setIsManaged("M");
        builder.addInterface("192.168.2.4").setIsManaged("M");
        builder.addInterface("192.168.2.5").setIsManaged("M");
        nodeDao.saveOrUpdate(builder.getCurrentNode());

        Assert.assertThat(builder.getCurrentNode().getIpInterfaces(), Matchers.hasSize(5));
        return builder.getCurrentNode().getIpInterfaces();
    }
}
