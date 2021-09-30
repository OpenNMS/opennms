/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Assert;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext = false)
public class HeatMapIT implements InitializingBean {
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private CategoryDao m_categoryDao;

    @Autowired
    private DatabasePopulator m_databasePopulator;

    private static boolean m_populated = false;

    private Map<String, Integer> numberOfNodesInCategory;

    @BeforeTransaction
    public void setUp() {
        if (!m_populated) {
            m_databasePopulator.populateDatabase();
            m_populated = true;
        }

        numberOfNodesInCategory= new HashMap();

        List<OnmsCategory> onmsCategories = m_categoryDao.findAll();

        for (OnmsCategory onmsCategory : onmsCategories) {
            int n = m_nodeDao.findByCategory(onmsCategory).size();
            if (n > 0) {
                numberOfNodesInCategory.put(onmsCategory.getName(), n);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @Transactional
    public void testOutagesForTestDB() {
        List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("categories.categoryname", "categories.categoryid", null, null);
        Assert.assertEquals(numberOfNodesInCategory.size(), heatMapElements.size());
        for (HeatMapElement heatMapElement : heatMapElements) {
            Assert.assertEquals(numberOfNodesInCategory.get(heatMapElement.getName()).intValue(), heatMapElement.getNodesTotal());
        }
    }

    @Test
    @Transactional
    public void testAlarmsForTestDB() {
        List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("categories.categoryname", "categories.categoryid", true, null, null);
        Assert.assertEquals(numberOfNodesInCategory.size(), heatMapElements.size());
        for (HeatMapElement heatMapElement : heatMapElements) {
            Assert.assertEquals(numberOfNodesInCategory.get(heatMapElement.getName()).intValue(), heatMapElement.getNodesTotal());
        }
    }
}
