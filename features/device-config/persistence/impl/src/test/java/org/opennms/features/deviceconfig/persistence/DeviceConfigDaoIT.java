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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
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
        deviceConfig.setCreatedTime(Date.from(Instant.now()));
        deviceConfig.setDeviceType("Cisco-IOS");
        deviceConfig.setVersion(1);
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
        deviceConfigList = deviceConfigDao.findConfigsForInterfaceSortedByDate(ipInterface);
        Assert.assertEquals(count, deviceConfigList.size());
        DeviceConfig deviceConfig = deviceConfigList.get(0);
        Assert.assertNotNull(deviceConfig);
        Assert.assertEquals(count, (long) deviceConfig.getVersion());
        // Take middle element and update it's created time.
        // This is not the way we should update versions of the latest. This is just for the test.
        DeviceConfig middleElement = deviceConfigList.stream().filter(config -> config.getVersion() == count/2).findFirst().get();
        middleElement.setCreatedTime(Date.from(Instant.now().plus(12, HOURS)));
        deviceConfigDao.saveOrUpdate(middleElement);
        deviceConfigList = deviceConfigDao.findConfigsForInterfaceSortedByDate(ipInterface);
        DeviceConfig retrievedMiddleElement = deviceConfigList.get(0);
        Assert.assertEquals(middleElement.getVersion(), retrievedMiddleElement.getVersion());
    }

    private void populateDeviceConfigs(int count) {

        for (int i = 0; i < count; i++) {
            DeviceConfig deviceConfig = new DeviceConfig();
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setConfig(UUID.randomUUID().toString().getBytes(StandardCharsets.US_ASCII));
            deviceConfig.setEncoding("ASCII");
            deviceConfig.setCreatedTime(Date.from(Instant.now().plusSeconds(i * 60)));
            deviceConfig.setDeviceType("Cisco-IOS");
            deviceConfig.setVersion(i+1);
            deviceConfigDao.saveOrUpdate(deviceConfig);
        }
    }

}
