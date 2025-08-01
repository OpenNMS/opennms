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
package org.opennms.netmgt.bsm.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
@Transactional
public class BusinessServiceChildEdgeIT {

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private BusinessServiceEdgeDao m_businessServiceEdgeDao;

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void canCreateReadUpdateAndDeleteEdges() {
        // Create the Parent Business Service
        BusinessServiceEntity parent = new BusinessServiceEntityBuilder()
            .name("Parent Service")
            .reduceFunction(new HighestSeverityEntity())
            .toEntity();
        // Create the Child Business Service
        BusinessServiceEntity child = new BusinessServiceEntityBuilder()
                .name("Child Service")
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        Long parentServiceId = m_businessServiceDao.save(parent);
        Long childServiceId = m_businessServiceDao.save(child);
        m_businessServiceDao.flush();

        // Initially there should be no edges
        assertEquals(0, m_businessServiceEdgeDao.countAll());

        // Create an edge
        BusinessServiceChildEdgeEntity edge = new BusinessServiceChildEdgeEntity();
        edge.setMapFunction(new IdentityEntity());
        edge.setBusinessService(parent);
        edge.setChild(child);
        m_businessServiceEdgeDao.save(edge);
        m_businessServiceEdgeDao.flush();

        // Read an edge
        assertEquals(1, m_businessServiceEdgeDao.countAll());
        assertEquals(edge, m_businessServiceEdgeDao.get(edge.getId()));
        assertEquals(parentServiceId, edge.getBusinessService().getId());
        assertEquals(childServiceId, edge.getChild().getId());

        // Update an edge
        edge.setWeight(2);
        m_businessServiceEdgeDao.save(edge);
        m_businessServiceEdgeDao.flush();

        BusinessServiceEdgeEntity otherEdge = m_businessServiceEdgeDao.get(edge.getId());
        assertEquals(edge, otherEdge);
        assertEquals(1, m_businessServiceEdgeDao.countAll());

        // Delete an edge
        m_businessServiceEdgeDao.delete(edge);
        assertEquals(0, m_businessServiceEdgeDao.countAll());
    }
}
