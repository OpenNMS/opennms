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
package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.hibernate.EventdServiceManagerHibernate;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml"
})
@JUnitConfigurationEnvironment
public class DaoEventdServiceManagerIT {
    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    EventdServiceManagerHibernate m_eventdServiceManager;

    @Before
    public void setUp() throws Exception {
        m_eventdServiceManager = new EventdServiceManagerHibernate();
        m_eventdServiceManager.setServiceTypeDao(m_serviceTypeDao);
        m_eventdServiceManager.afterPropertiesSet();
    }

    @Test
    public void testSync() {
        m_eventdServiceManager.dataSourceSync();
        m_serviceTypeDao.save(new OnmsServiceType("ICMP"));
        assertTrue(m_eventdServiceManager.getServiceId("ICMP") > 0);
    }

}
