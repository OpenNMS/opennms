/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class AuthorizationTest implements InitializingBean {

    @Autowired
    AlarmDao m_alarmDao;

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
    @Ignore("What does this even do?  Category 'groups' aren't even exposed in DAOs.")
    @JUnitTemporaryDatabase
    public void testGetCategoriesWithAuthorizedGroups() {

        List<OnmsCategory> categories = m_categoryDao.getCategoriesWithAuthorizedGroup("RoutersGroup");

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Routers", categories.get(0).getName());

    }

    public void enableAuthorizationFilter(final String... groupNames) {

        HibernateCallback<Object> cb = new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.enableFilter("authorizedOnly").setParameterList("userGroups", groupNames);
                return null;
            }

        };

        ((AlarmDaoHibernate)m_alarmDao).getHibernateTemplate().execute(cb);
    }

    public void disableAuthorizationFilter() {

        HibernateCallback<Object> cb = new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.disableFilter("authorizedOnly");
                return null;
            }

        };

        ((AlarmDaoHibernate)m_alarmDao).getHibernateTemplate().execute(cb);
    }
}
