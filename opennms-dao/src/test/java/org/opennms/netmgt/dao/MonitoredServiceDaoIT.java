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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MonitoredServiceDaoIT implements InitializingBean {

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        m_databasePopulator.populateDatabase();
    }

    @Test
    @Transactional
    public void testLazy() {
        final List<OnmsMonitoredService> allSvcs = m_monitoredServiceDao.findAll();
        assertTrue(allSvcs.size() > 1);

        final OnmsMonitoredService svc = allSvcs.iterator().next();
        assertEquals(addr("192.168.1.1"), svc.getIpAddress());
        assertEquals(1, svc.getIfIndex().intValue());
        assertEquals(m_databasePopulator.getNode1().getId(), svc.getIpInterface().getNode().getId());
        assertEquals("M", svc.getIpInterface().getIsManaged());
    }

    @Test
    @Transactional
    public void testFindAllServices() {
        final List<OnmsMonitoredService> allSvcs = m_monitoredServiceDao.findAllServices();
        assertTrue(allSvcs.size() > 1);
        for (OnmsMonitoredService ifservice: allSvcs) {
            assertNotNull(ifservice.getIpInterface());
            assertNotNull(ifservice.getIpInterface().getNode());
            assertNotNull(ifservice.getIpAddress());
            assertNotNull(ifservice.getNodeId());
            assertNotNull(ifservice.getServiceType());
        }
        
    }

    @Test
    @Transactional
    public void testFindAllServicesForScheduling() {
        // All services from DatabasePopulator have status 'A'.
        // Mark one service as 'D' (deleted) so we can verify it's excluded.
        final OnmsMonitoredService svcToDelete = m_monitoredServiceDao.get(
                m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), "SNMP");
        assertNotNull(svcToDelete);
        svcToDelete.setStatus("D");
        m_monitoredServiceDao.saveOrUpdate(svcToDelete);
        m_monitoredServiceDao.flush();

        // Mark another service as 'N' (not monitored) — should still be included.
        final OnmsMonitoredService svcNotMonitored = m_monitoredServiceDao.get(
                m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), "ICMP");
        assertNotNull(svcNotMonitored);
        svcNotMonitored.setStatus("N");
        m_monitoredServiceDao.saveOrUpdate(svcNotMonitored);
        m_monitoredServiceDao.flush();

        final List<OnmsMonitoredService> allSvcs = m_monitoredServiceDao.findAllServices();
        final List<OnmsMonitoredService> schedulingSvcs = m_monitoredServiceDao.findAllServicesForScheduling();

        // Detach all entities so lazy loading will fail — this proves the
        // query eagerly fetched the associations we need.
        m_monitoredServiceDao.clear();

        // Should have one fewer than findAllServices (the 'D' service is excluded)
        assertTrue(schedulingSvcs.size() > 0);
        assertTrue("findAllServicesForScheduling should return fewer services than findAllServices " +
                        "because deleted services are excluded",
                schedulingSvcs.size() < allSvcs.size());

        for (OnmsMonitoredService svc : schedulingSvcs) {
            // Only 'A' or 'N' statuses should be present
            assertTrue("Expected status A or N but got " + svc.getStatus(),
                    "A".equals(svc.getStatus()) || "N".equals(svc.getStatus()));

            // Verify associations are eagerly fetched (no lazy-load after clear)
            assertNotNull(svc.getServiceType());
            assertNotNull(svc.getIpInterface());
            assertNotNull(svc.getIpInterface().getNode());
            assertNotNull(svc.getIpInterface().getNode().getLocation());
            assertNotNull(svc.getIpInterface().getNode().getLocation().getLocationName());
        }

        // Verify the 'D' service is not in the result
        for (OnmsMonitoredService svc : schedulingSvcs) {
            assertFalse("Deleted service should not be in scheduling results",
                    svc.getId().equals(svcToDelete.getId()));
        }

        // Verify the 'N' service IS in the result
        boolean foundNotMonitored = false;
        for (OnmsMonitoredService svc : schedulingSvcs) {
            if (svc.getId().equals(svcNotMonitored.getId())) {
                foundNotMonitored = true;
                assertEquals("N", svc.getStatus());
            }
        }
        assertTrue("Service with status 'N' should be included in scheduling results", foundNotMonitored);
    }

    @Test
    @Transactional
    public void testGetByCompositeId() {
        final OnmsMonitoredService monSvc = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), "SNMP");
        assertNotNull(monSvc);

        final OnmsMonitoredService monSvc2 = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), monSvc.getIfIndex(), monSvc.getServiceId());
        assertNotNull(monSvc2);

    }

}
