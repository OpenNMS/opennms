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
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ApplicationDaoIT implements InitializingBean {

    @Autowired
    private MonitoringLocationDao m_monitoringLocationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private ApplicationDao m_applicationDao;

    @Autowired
    private TransactionTemplate m_transTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                if (m_serviceTypeDao.findByName("ICMP") == null) {
                    m_serviceTypeDao.save(new OnmsServiceType("ICMP"));
                }
            }
        });
    }

    @Test
    @Transactional
    public void testGetPerspectiveLocationsForService() {
        final OnmsMonitoringLocation rdu = new OnmsMonitoringLocation();
        rdu.setLocationName("RDU");
        rdu.setMonitoringArea("USA");
        rdu.setPriority(1L);
        m_monitoringLocationDao.save(rdu);

        final OnmsMonitoringLocation fulda = new OnmsMonitoringLocation();
        fulda.setLocationName("Fulda");
        fulda.setMonitoringArea("Germany");
        fulda.setPriority(1L);
        m_monitoringLocationDao.save(fulda);

        final OnmsNode node = new OnmsNode(m_monitoringLocationDao.getDefaultLocation(), "app-node");
        m_nodeDao.save(node);

        final OnmsIpInterface ipInterface = new OnmsIpInterface(addr("172.16.1.1"), node);
        ipInterface.setIsManaged("M");
        m_ipInterfaceDao.save(ipInterface);

        final OnmsServiceType icmp = m_serviceTypeDao.findByName("ICMP");
        assertNotNull(icmp);
        final OnmsMonitoredService service = new OnmsMonitoredService(ipInterface, icmp);
        m_monitoredServiceDao.save(service);

        final OnmsApplication app = new OnmsApplication();
        app.setName("MyApp");
        app.getPerspectiveLocations().add(rdu);
        app.getPerspectiveLocations().add(fulda);
        m_applicationDao.save(app);

        service.setApplications(Sets.newHashSet(app));
        m_monitoredServiceDao.saveOrUpdate(service);
        m_monitoredServiceDao.flush();

        final List<OnmsMonitoringLocation> perspectives =
                m_applicationDao.getPerspectiveLocationsForService(node.getId(), addr("172.16.1.1"), "ICMP");
        assertEquals(2, perspectives.size());
        assertEquals(Sets.newHashSet("RDU", "Fulda"),
                     perspectives.stream().map(OnmsMonitoringLocation::getLocationName).collect(Collectors.toSet()));

        // any non-matching key must yield no perspectives
        assertTrue(m_applicationDao.getPerspectiveLocationsForService(node.getId(), addr("172.16.1.1"), "SNMP").isEmpty());
        assertTrue(m_applicationDao.getPerspectiveLocationsForService(node.getId(), addr("172.16.1.2"), "ICMP").isEmpty());
        assertTrue(m_applicationDao.getPerspectiveLocationsForService(node.getId() + 1, addr("172.16.1.1"), "ICMP").isEmpty());
    }
}
