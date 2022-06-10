/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import com.google.common.collect.Lists;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.geoip.GeoIpConfig;
import org.opennms.netmgt.config.geoip.Subnet;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class GeoIpProvisioningAdapterIT implements InitializingBean {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private MonitoringLocationDao monitoringLocationDao;

    @Autowired
    private MockEventIpcManager mockEventIpcManager;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private GeoIpProvisioningAdapter geoIpProvisioningAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true);

        geoIpProvisioningAdapter.setDelay(1);
        geoIpProvisioningAdapter.setTimeUnit(TimeUnit.SECONDS);

        Assert.notNull(nodeDao, "Autowiring failed, NodeDao is null");
        Assert.notNull(ipInterfaceDao, "Autowiring failed, IpInterfaceDao is null");
        Assert.notNull(monitoringLocationDao, "Autowiring failed, MonitoringLocationDao is null");
        Assert.notNull(mockEventIpcManager, "Autowiring failed, IPC manager is null");
        Assert.notNull(databasePopulator, "Autowiring failed, DB populator is null");
        Assert.notNull(geoIpProvisioningAdapter, "Autowiring failed, GeoIpProvisioningAdapter is null");

        geoIpProvisioningAdapter.getGeoIpConfigDao().setConfigResource(new FileSystemResource("src/test/resources/geoip-adapter-configuration.xml"));
        geoIpProvisioningAdapter.getGeoIpConfigDao().getContainer().reload();
        geoIpProvisioningAdapter.getGeoIpConfigDao().afterPropertiesSet();

        databasePopulator.populateDatabase();
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testAddNodeDirectly() throws Exception {
        final DatabaseReader databaseReader = mock(DatabaseReader.class);

        final Map<String, String> cityNames = new TreeMap<>();
        cityNames.put("en", "Atlantis");

        final Map<String, String> countryNames = new TreeMap<>();
        countryNames.put("en", "Atlantic Ocean");

        when(databaseReader.city(InetAddress.getByName("1.2.3.4"))).thenReturn(
                new CityResponse(new City(Lists.newArrayList("en"), 100, 100, cityNames),
                        null,
                        new Country(Lists.newArrayList("en"), null, (Integer) null, false, null, countryNames),
                        new Location(null, null, 30.0, -40.0, null, null, null),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));

        final OnmsMonitoringLocation fulda = monitoringLocationDao.get("Fulda");
        final OnmsMonitoringLocation rdu = monitoringLocationDao.get("RDU");

        geoIpProvisioningAdapter.setDatabaseReader(databaseReader);

        final OnmsNode node1 = nodeDao.get(1);
        final OnmsNode node2 = nodeDao.get(2);
        final OnmsNode node3 = nodeDao.get(3);
        final OnmsNode node4 = nodeDao.get(4);
        final OnmsNode node5 = nodeDao.get(5);
        final OnmsNode node6 = nodeDao.get(6);

        final OnmsIpInterface theInterface = node6.getIpInterfaceByIpAddress("10.1.2.3");
        theInterface.setIpAddress(InetAddress.getByName("1.2.3.4"));
        ipInterfaceDao.saveOrUpdate(theInterface);

        node3.setLocation(fulda);
        node4.setLocation(fulda);

        node5.setLocation(rdu);
        node6.setLocation(rdu);

        nodeDao.saveOrUpdate(node1);
        nodeDao.saveOrUpdate(node2);
        nodeDao.saveOrUpdate(node3);
        nodeDao.saveOrUpdate(node4);
        nodeDao.saveOrUpdate(node5);
        nodeDao.saveOrUpdate(node6);

        geoIpProvisioningAdapter.doAddNode(1);
        geoIpProvisioningAdapter.doAddNode(2);
        geoIpProvisioningAdapter.doAddNode(3);
        geoIpProvisioningAdapter.doAddNode(4);
        geoIpProvisioningAdapter.doAddNode(5);
        geoIpProvisioningAdapter.doAddNode(6);

        assertEquals("Pittsboro", node1.getAssetRecord().getCity());
        assertEquals("USA", node1.getAssetRecord().getCountry());
        assertEquals(-79.1625700509753, node1.getAssetRecord().getLongitude().doubleValue(), 0.00000000000001);
        assertEquals(35.71572416796933, node1.getAssetRecord().getLatitude().doubleValue(), 0.00000000000001);
        assertEquals("A", node1.getAssetRecord().getBuilding());

        assertEquals("Pittsboro", node2.getAssetRecord().getCity());
        assertEquals("USA", node2.getAssetRecord().getCountry());
        assertEquals(-79.1625700509753, node2.getAssetRecord().getLongitude().doubleValue(), 0.00000000000001);
        assertEquals(35.71572416796933, node2.getAssetRecord().getLatitude().doubleValue(), 0.00000000000001);
        assertEquals("A", node2.getAssetRecord().getBuilding());

        assertEquals("Fulda", node3.getAssetRecord().getCity());
        assertEquals("Germany", node3.getAssetRecord().getCountry());
        assertEquals(9.67484665470827, node3.getAssetRecord().getLongitude().doubleValue(), 0.00000000000001);
        assertEquals(50.553159563701065, node3.getAssetRecord().getLatitude().doubleValue(), 0.00000000000001);
        assertEquals("C", node3.getAssetRecord().getBuilding());

        assertEquals("Fulda", node4.getAssetRecord().getCity());
        assertEquals("Germany", node4.getAssetRecord().getCountry());
        assertEquals(9.67484665470827, node4.getAssetRecord().getLongitude().doubleValue(), 0.00000000000001);
        assertEquals(50.553159563701065, node4.getAssetRecord().getLatitude().doubleValue(), 0.00000000000001);
        assertEquals("D", node4.getAssetRecord().getBuilding());

        assertEquals("Raleigh", node5.getAssetRecord().getCity());
        assertEquals("USA", node5.getAssetRecord().getCountry());
        assertEquals(-78.86788904568587, node5.getAssetRecord().getLongitude().doubleValue(), 0.00000000000001);
        assertEquals(35.89503031832238, node5.getAssetRecord().getLatitude().doubleValue(), 0.00000000000001);
        assertEquals("F", node5.getAssetRecord().getBuilding());

        assertEquals("Atlantis", node6.getAssetRecord().getCity());
        assertEquals("Atlantic Ocean", node6.getAssetRecord().getCountry());
        assertEquals(-40.0, node6.getAssetRecord().getLongitude().doubleValue(), 0.00000000000001);
        assertEquals(30.0, node6.getAssetRecord().getLatitude().doubleValue(), 0.00000000000001);
        assertEquals(null, node6.getAssetRecord().getBuilding());
    }

    @Test
    public void testIpRanges() throws UnknownHostException {
        assertEquals(true, geoIpProvisioningAdapter.isPublicAddress(InetAddress.getByName("8.8.8.8")));
        assertEquals(true, geoIpProvisioningAdapter.isPublicAddress(InetAddress.getByName("1.2.3.4")));
        assertEquals(false, geoIpProvisioningAdapter.isPublicAddress(InetAddress.getByName("192.168.42.1")));
        assertEquals(false, geoIpProvisioningAdapter.isPublicAddress(InetAddress.getByName("10.11.12.13")));
        assertEquals(false, geoIpProvisioningAdapter.isPublicAddress(InetAddress.getByName("172.17.32.32")));
        assertEquals(false, geoIpProvisioningAdapter.isPublicAddress(InetAddress.getByName("fd99::1")));
        assertEquals(false, geoIpProvisioningAdapter.isPublicAddress(InetAddress.getByName("fc11::1")));
        assertEquals(true, geoIpProvisioningAdapter.isPublicAddress(InetAddress.getByName("2001::1")));
    }

    private boolean isInside(final String cidr, final String ip) {
        final GeoIpConfig geoIpConfig = new GeoIpConfig();
        final Subnet subnet = new Subnet();
        subnet.setCidr(cidr);
        org.opennms.netmgt.config.geoip.Location location = new org.opennms.netmgt.config.geoip.Location();
        location.setName("Dummy");
        location.getSubnets().add(subnet);
        location.getSubnets().add(subnet);
        geoIpConfig.getLocations().add(location);
        return geoIpProvisioningAdapter.getSubnet(geoIpConfig, "Dummy", ip) != null;
    }

    @Test
    public void testIPv4() {
        assertEquals(true, isInside("10.0.0.0/16", "10.0.0.1"));
        assertEquals(true, isInside("10.0.0.0/16", "10.0.0.2"));
        assertEquals(false, isInside("10.0.0.0/16", "10.1.0.1"));
    }

    @Test
    public void testIPv6() {
        assertEquals(true, isInside("2001::/64", "2001::1"));
        assertEquals(true, isInside("2001::/64", "2001::2"));
        assertEquals(false, isInside("2001::/64", "2001:1::1"));
    }
}
