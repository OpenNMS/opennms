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
package org.opennms.features.deviceconfig.persistence;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigQueryResult;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        deviceConfig.setStatus(DeviceConfigStatus.SUCCESS);
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
        Assert.assertEquals(DeviceConfigStatus.SUCCESS, deviceConfig.getStatus());
        Assert.assertNull(deviceConfig.getLastFailed());
    }

    @Test
    public void testFetchDeviceConfigSortedByDate() {
        populateIpInterface();
        final int count = 10;
        populateDeviceConfigs(count, ipInterface, "DeviceConfig-default");

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

        // there can be a few milliseconds jitter in this, so check that it's within 5ms
        final var lastUpdated = retrievedConfig.getLastUpdated().getTime();
        final var lastFailed = retrievedConfig.getLastFailed().getTime();
        Assert.assertTrue("lastUpdated and lastFailed should be within a few milliseconds of each other: lastUpdated=" + lastUpdated + ", lastFailed=" + lastFailed, Math.abs(lastUpdated - lastFailed) <= 5);
    }

    @Test
    public void testGetLatestConfigOnEachInterface() {
        Set<OnmsIpInterface> ipInterfaces = populateIpInterfaces();
        int count = 10;
        ipInterfaces.forEach(ipInterface -> populateDeviceConfigs(count, ipInterface, "DeviceConfig-default"));
        List<DeviceConfigQueryResult> results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, null, null);
        Assert.assertThat(results, Matchers.hasSize(5));
        Iterator<OnmsIpInterface> iterator = ipInterfaces.iterator();
        Assert.assertArrayEquals(results.get(0).getConfig(),
                (iterator.next().getInterfaceId() + ":" + (count - 1)).getBytes(Charset.defaultCharset()));
        Assert.assertArrayEquals(results.get(1).getConfig(),
                (iterator.next().getInterfaceId() + ":" + (count - 1)).getBytes(Charset.defaultCharset()));

        // Should give one result.
        results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, InetAddressUtils.str(iterator.next().getIpAddress()), null);
        Assert.assertThat(results, Matchers.hasSize(1));

        // Should give no results.
        results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, "192.168.32.254", null);
        Assert.assertThat(results, Matchers.hasSize(0));

        // Should give all 5 interfaces
        results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, iterator.next().getNode().getLabel(), null);
        Assert.assertThat(results, Matchers.hasSize(5));

        ipInterfaces.forEach(this::populateFailedRetrievalDeviceConfig);

        results = deviceConfigDao.getLatestConfigForEachInterface(null,
                null, null, null, iterator.next().getNode().getLabel(), null);

        Assert.assertThat(results.get(0).getFailureReason(), Matchers.notNullValue());
    }

    @Test
    public void testDeviceConfigsWithoutServiceName() {
        populateIpInterface();
        int count = 1;
        String serviceName = "DeviceConfig-running";
        populateDeviceConfigs(count, ipInterface, null);
        populateDeviceConfigs(count, ipInterface, serviceName);
        Optional<DeviceConfig> deviceConfigOptional = deviceConfigDao.getLatestConfigForInterface(ipInterface, null);
        Assert.assertFalse(deviceConfigOptional.isEmpty());
        Assert.assertThat(deviceConfigOptional.get().getServiceName(), Matchers.nullValue());
        deviceConfigOptional = deviceConfigDao.getLatestConfigForInterface(ipInterface, serviceName);
        Assert.assertFalse(deviceConfigOptional.isEmpty());
        Assert.assertThat(deviceConfigOptional.get().getServiceName(), Matchers.is(serviceName));
    }

    @Test
    public void testDeviceConfigNodesBySysOidCount() {
        Set<OnmsIpInterface> ipInterfaces = populateIpInterfacesWithSysOid();
        ipInterfaces.forEach(ipInterface -> populateDeviceConfigs(1, ipInterface, "DeviceConfig-default"));

        Map<String, Long> nodesWithConfigBySysOid = deviceConfigDao.getNumberOfNodesWithDeviceConfigBySysOid();
        Assert.assertEquals(nodesWithConfigBySysOid.get(".1.3.6.1.4.1.9.1.799").longValue(), 2L);
        Assert.assertEquals(nodesWithConfigBySysOid.get(".1.3.6.1.4.1.2636.1.1.1.2.137").longValue(), 1L);
        Assert.assertEquals(nodesWithConfigBySysOid.get("none").longValue(), 1L);
    }

    private void populateDeviceConfigs(int count, OnmsIpInterface ipInterface, String serviceName) {
        for (int i = 0; i < count; i++) {
            DeviceConfig deviceConfig = new DeviceConfig();
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setServiceName(serviceName);
            deviceConfig.setConfig((ipInterface.getInterfaceId() + ":" + i).getBytes(Charset.defaultCharset()));
            deviceConfig.setEncoding(Charset.defaultCharset().name());
            deviceConfig.setCreatedTime(Date.from(Instant.now().plusSeconds(i * 60)));
            if (serviceName != null) {
                String configType = serviceName.substring(serviceName.lastIndexOf("-") + 1);
                deviceConfig.setConfigType(configType);
            } else {
                deviceConfig.setConfigType(ConfigType.Default);
            }
            deviceConfig.setLastUpdated(Date.from(Instant.now().plusSeconds(i * 60)));
            deviceConfig.setLastSucceeded(deviceConfig.getLastUpdated());
            deviceConfig.setStatus(DeviceConfigStatus.SUCCESS);
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
        deviceConfig.setStatus(DeviceConfigStatus.FAILED);
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

    private Set<OnmsIpInterface> populateIpInterfacesWithSysOid() {
        Set<OnmsIpInterface> interfaces = new HashSet<>();
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("node1").setForeignSource("imported:").setForeignId("2").setType(OnmsNode.NodeType.ACTIVE).setSysObjectId(".1.3.6.1.4.1.9.1.799");
        builder.addInterface("192.168.2.1").setIsManaged("M").setIsSnmpPrimary("P");
        nodeDao.saveOrUpdate(builder.getCurrentNode());
        interfaces.add(builder.getCurrentNode().getPrimaryInterface());

        builder.addNode("node2").setForeignSource("imported:").setForeignId("3").setType(OnmsNode.NodeType.ACTIVE).setSysObjectId(".1.3.6.1.4.1.9.1.799");
        builder.addInterface("192.168.2.2").setIsManaged("M").setIsSnmpPrimary("P");
        nodeDao.saveOrUpdate(builder.getCurrentNode());
        interfaces.add(builder.getCurrentNode().getPrimaryInterface());

        builder.addNode("node3").setForeignSource("imported:").setForeignId("4").setType(OnmsNode.NodeType.ACTIVE).setSysObjectId(".1.3.6.1.4.1.2636.1.1.1.2.137");
        builder.addInterface("192.168.2.3").setIsManaged("M").setIsSnmpPrimary("P");
        nodeDao.saveOrUpdate(builder.getCurrentNode());
        interfaces.add(builder.getCurrentNode().getPrimaryInterface());

        builder.addNode("node4").setForeignSource("imported:").setForeignId("5").setType(OnmsNode.NodeType.ACTIVE);
        builder.addInterface("192.168.2.4").setIsManaged("M").setIsSnmpPrimary("P");
        nodeDao.saveOrUpdate(builder.getCurrentNode());
        interfaces.add(builder.getCurrentNode().getPrimaryInterface());

        return interfaces;
    }
}
