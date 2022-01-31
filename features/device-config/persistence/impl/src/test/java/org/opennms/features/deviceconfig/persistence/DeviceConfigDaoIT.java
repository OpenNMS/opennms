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
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
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

    @Before
    public void init() {
        ipInterface = populateIpInterfaceAndGet();
    }

    @Test
    @Transactional
    public void testPersistenceOfDeviceConfig() {
        DeviceConfig deviceConfig = new DeviceConfig();
        deviceConfig.setIpInterface(ipInterface);
        deviceConfig.setConfig(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        deviceConfig.setEncoding("ASCII");
        deviceConfig.setCreatedTime(new Date());
        deviceConfig.setConfigType("default");
        deviceConfig.setLastUpdated(new Date());
        deviceConfigDao.saveOrUpdate(deviceConfig);
        List<DeviceConfig> configs = deviceConfigDao.findAll();
        Assert.assertThat(configs, Matchers.not(Matchers.empty()));
        DeviceConfig retrieved = configs.get(0);
        Assert.assertNotNull(retrieved);
        Assert.assertEquals(ipInterface, retrieved.getIpInterface());
        Assert.assertEquals(deviceConfig.getConfig(), retrieved.getConfig());
    }

    private OnmsIpInterface populateIpInterfaceAndGet() {
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("node2").setForeignSource("imported:").setForeignId("2").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        nodeDao.saveOrUpdate(builder.getCurrentNode());
        Assert.assertThat(builder.getCurrentNode().getIpInterfaces(), Matchers.hasSize(1));
        Set<OnmsIpInterface> ipInterfaces = builder.getCurrentNode().getIpInterfaces();
        OnmsIpInterface ipInterface = ipInterfaces.iterator().next();
        Assert.assertNotNull(ipInterface);
        return ipInterface;
    }

    @Test
    public void testFetchDeviceConfigSortedByDate() {
        int count = 10;
        populateDeviceConfigs(count);
        List<DeviceConfig> deviceConfigList = deviceConfigDao.findAll();
        Assert.assertEquals(count, deviceConfigList.size());
        deviceConfigList = deviceConfigDao.findConfigsForInterfaceSortedByDate(ipInterface, "default");
        Assert.assertEquals(count, deviceConfigList.size());
        DeviceConfig deviceConfig = deviceConfigList.get(0);
        Assert.assertNotNull(deviceConfig);
        // Take middle element and update it's created time.
        // This is not the way we should update versions of the latest. This is just for the test.
        DeviceConfig middleElement = deviceConfigList.get((count/2) - 1);
        middleElement.setCreatedTime(Date.from(Instant.now().plus(1, HOURS)));
        deviceConfigDao.saveOrUpdate(middleElement);
        deviceConfigList = deviceConfigDao.findConfigsForInterfaceSortedByDate(ipInterface, "default");
        DeviceConfig retrievedMiddleElement = deviceConfigList.get(0);
        Optional<DeviceConfig> latestElementOptional = deviceConfigDao.getLatestSucceededConfigForInterface(ipInterface, "default");
        Assert.assertTrue(latestElementOptional.isPresent());
        DeviceConfig latestConfig = latestElementOptional.get();
        Assert.assertArrayEquals(retrievedMiddleElement.getConfig(), latestConfig.getConfig());
        // Populate failed retrieval for devices.
        populateFailedRetrievalDeviceConfig();
        deviceConfigList = deviceConfigDao.findAll();
        // Verify that it got persisted
        Assert.assertThat(deviceConfigList.size(), Matchers.is(count + 1));
        // Verify that query doesn't consider failed elements.
        Optional<DeviceConfig> elementsWithNullConfig = deviceConfigDao.getLatestSucceededConfigForInterface(ipInterface, "default");
        Assert.assertTrue(elementsWithNullConfig.isPresent());
        DeviceConfig retrievedConfig = elementsWithNullConfig.get();
        Assert.assertArrayEquals(retrievedConfig.getConfig(), latestConfig.getConfig());
        // Verify that this will give all elements including last failed one.
        deviceConfigList = deviceConfigDao.findConfigsForInterfaceSortedByDate(ipInterface, "default");
        DeviceConfig failedElement = deviceConfigList.get(0);
        Assert.assertNull(failedElement.getConfig());
    }

    private void populateDeviceConfigs(int count) {

        for (int i = 0; i < count; i++) {
            DeviceConfig deviceConfig = new DeviceConfig();
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setConfig(UUID.randomUUID().toString().getBytes(StandardCharsets.US_ASCII));
            deviceConfig.setEncoding("ASCII");
            deviceConfig.setCreatedTime(Date.from(Instant.now().plusSeconds(i * 60)));
            deviceConfig.setConfigType("default");
            deviceConfig.setLastUpdated(Date.from(Instant.now().plusSeconds(i * 60)));
            deviceConfigDao.saveOrUpdate(deviceConfig);
        }
    }

    private void populateFailedRetrievalDeviceConfig() {

        DeviceConfig deviceConfig = new DeviceConfig();
        deviceConfig.setIpInterface(ipInterface);
        deviceConfig.setEncoding(Charset.defaultCharset().name());
        deviceConfig.setCreatedTime(Date.from(Instant.now().plus(2, HOURS)));
        deviceConfig.setConfigType("default");
        deviceConfig.setLastUpdated(Date.from(Instant.now().plus(2, HOURS)));
        deviceConfig.setLastFailed(Date.from(Instant.now().plus(2, HOURS)));
        deviceConfig.setFailureReason("Not able to connect to SSHServer");
        deviceConfigDao.saveOrUpdate(deviceConfig);
    }

}
