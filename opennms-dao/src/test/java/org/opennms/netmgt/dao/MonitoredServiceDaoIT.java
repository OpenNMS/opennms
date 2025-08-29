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
public class MonitoredServiceDaoIT extends AbstractJRobinIT implements InitializingBean {

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
    public void testGetByCompositeId() {
        final OnmsMonitoredService monSvc = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), "SNMP");
        assertNotNull(monSvc);

        final OnmsMonitoredService monSvc2 = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), monSvc.getIfIndex(), monSvc.getServiceId());
        assertNotNull(monSvc2);

    }

}
