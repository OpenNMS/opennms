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

import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AuthorizationIT implements InitializingBean {

    @Autowired
    AlarmDao m_alarmDao;

    @Autowired
    OutageDao m_outageDao;

    @Autowired
    CategoryDao m_categoryDao;

    @Autowired
    DatabasePopulator m_populator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @BeforeTransaction
    public void setUp() {
        m_populator.populateDatabase();
    }

    @AfterTransaction
    public void tearDown() {
        m_populator.resetDatabase();
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAuthorizedAlarms() {

        Collection<OnmsAlarm> matching = m_alarmDao.findAll();

        assertNotNull(matching);
        assertEquals(1, matching.size());

        System.err.println(matching);

        enableAuthorizationFilter("NonExistentGroup");

        Collection<OnmsAlarm> matching2 = m_alarmDao.findAll();

        assertNotNull(matching2);
        assertEquals(0, matching2.size());

        System.err.println(matching2);

        disableAuthorizationFilter();

        Collection<OnmsAlarm> matching3 = m_alarmDao.findAll();

        assertNotNull(matching3);
        assertEquals(1, matching3.size());

        System.err.println(matching3);
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAuthorizedOutages() {
        Collection<OnmsOutage> matching = m_outageDao.findMatching(new CriteriaBuilder(OnmsOutage.class).isNull("perspective").toCriteria());

        OnmsMonitoringLocation l = m_populator.getMonitoringLocationDao().get("RDU");

        assertNotNull(matching);
        assertEquals(2, matching.size());

        System.err.println(matching);

        enableAuthorizationFilter("NonExistentGroup");

        Collection<OnmsOutage> matching2 = m_outageDao.findMatching(new CriteriaBuilder(OnmsOutage.class).isNull("perspective").toCriteria());

        assertNotNull(matching2);
        assertEquals(0, matching2.size());

        System.err.println(matching2);

        disableAuthorizationFilter();

        Collection<OnmsOutage> matching3 = m_outageDao.findMatching(new CriteriaBuilder(OnmsOutage.class).isNull("perspective").toCriteria());

        assertNotNull(matching3);
        assertEquals(2, matching3.size());

        System.err.println(matching3);
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testAuthorizedOutageSumaries() {

        List<OutageSummary> matching = m_outageDao.getNodeOutageSummaries(10);

        assertNotNull(matching);
        assertEquals(1, matching.size());

        System.err.println(matching);

        enableAuthorizationFilter("NonExistentGroup");

        List<OutageSummary> matching2 = m_outageDao.getNodeOutageSummaries(10);

        assertNotNull(matching2);
        assertEquals(0, matching2.size());

        System.err.println(matching2);

        disableAuthorizationFilter();

        List<OutageSummary> matching3 = m_outageDao.getNodeOutageSummaries(10);

        assertNotNull(matching3);
        assertEquals(1, matching3.size());

        System.err.println(matching3);
    }

    public void enableAuthorizationFilter(final String... groupNames) {

        HibernateCallback<Object> cb = new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                session.enableFilter("authorizedOnly").setParameterList("userGroups", groupNames);
                return null;
            }

        };

        ((AlarmDaoHibernate)m_alarmDao).getHibernateTemplate().execute(cb);
    }

    public void disableAuthorizationFilter() {

        HibernateCallback<Object> cb = new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException {
                session.disableFilter("authorizedOnly");
                return null;
            }

        };

        ((AlarmDaoHibernate)m_alarmDao).getHibernateTemplate().execute(cb);
    }
}
