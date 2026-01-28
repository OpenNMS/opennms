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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
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

    @Test (expected = IllegalArgumentException.class)
    @Transactional
    public void testAlarmDaoInvalidColumns() {
        List<HeatMapElement> heatMapElements = m_alarmDao.getHeatMapItemsForEntity("not a column name", "arbitrary", true, null, null);
    }
    
    @Test (expected = IllegalArgumentException.class)
    @Transactional
    public void testOutageDaoInvalidColumns() {
        List<HeatMapElement> heatMapElements = m_outageDao.getHeatMapItemsForEntity("not a column name", "arbitrary", null, null);
    }

}
