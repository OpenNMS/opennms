/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
    "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class HibernateCriteriaConverterTest implements InitializingBean {
    @Autowired
    DatabasePopulator m_populator;

    @Autowired
    NodeDao m_nodeDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging(true);
        m_populator.populateDatabase();
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodeQuery() throws Exception {
        List<OnmsNode> nodes;

        // first, try with OnmsCriteria
        final OnmsCriteria crit = new OnmsCriteria(OnmsNode.class);
        crit.add(org.hibernate.criterion.Restrictions.isNotNull("id"));
        nodes = m_nodeDao.findMatching(crit);
        assertEquals(6, nodes.size());

        // then the same with the builder
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.isNotNull("id");
        nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(6, nodes.size());

        cb.eq("label", "node1").join("ipInterfaces", "ipInterface").eq("ipInterface.ipAddress", "192.168.1.1");
        nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(1, nodes.size());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testNodeIlikeQuery() {
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.isNotNull("id").eq("label", "node1").alias("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN).ilike("ipInterface.ipAddress", "1%");
        final List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(3, nodes.size());
    }

    @Test
    @Transactional
    @JUnitTemporaryDatabase
    public void testDistinctQuery() {
        List<OnmsNode> nodes = null;

        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.isNotNull("id").distinct();
        cb.eq("label", "node1").join("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN).eq("ipInterface.ipAddress", "192.168.1.1");

        nodes = m_nodeDao.findMatching(cb.toCriteria());
        assertEquals(1, nodes.size());
        assertEquals(Integer.valueOf(1), nodes.get(0).getId());
    }
}
