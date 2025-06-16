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
package org.opennms.netmgt.topology.persistence.impl;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.topology.persistence.api.LayoutDao;
import org.opennms.netmgt.topology.persistence.api.LayoutEntity;
import org.opennms.netmgt.topology.persistence.api.PointEntity;
import org.opennms.netmgt.topology.persistence.api.VertexPositionEntity;
import org.opennms.netmgt.topology.persistence.api.VertexRefEntity;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
public class LayoutDaoIT {

    @Autowired
    private LayoutDao layoutDao;

    @Before
    public void before() {
    }

    @Test
    @Transactional
    public void verifyCRUD() {
        // Nothing created yet
        Assert.assertEquals(0, layoutDao.countAll());
        Assert.assertEquals(0, layoutDao.countMatching(new Criteria(VertexPositionEntity.class)));

        // Create dummy
        LayoutEntity layout = new LayoutEntity();
        layout.setId("hash");
        layout.setCreated(new Date());
        layout.setCreator("mvrueden");
        layout.setUpdated(layout.getCreated());
        layout.setUpdator(layout.getCreator());
        layout.addVertexPosition(createVertexPosition("dummy", "1", 0, 0));
        layout.addVertexPosition(createVertexPosition("dummy", "2", 1, 1));

        // create and verify creation
        layoutDao.saveOrUpdate(layout);
        Assert.assertEquals(1, layoutDao.countAll());
        Assert.assertEquals(2, layoutDao.countMatching(new Criteria(VertexPositionEntity.class)));

        // Update
        // Remove Vertex
        layout.getVertexPositions().remove(0);
        layoutDao.update(layout);
        Assert.assertEquals(1, layoutDao.countAll());
        Assert.assertEquals(1, layoutDao.countMatching(new Criteria(VertexPositionEntity.class)));

        // Add Vertex
        layout.addVertexPosition(createVertexPosition("dummy", "3", 2,2));
        layoutDao.update(layout);
        Assert.assertEquals(1, layoutDao.countAll());
        Assert.assertEquals(2, layoutDao.countMatching(new Criteria(VertexPositionEntity.class)));

        // Update layout
        layout.setUpdated(new Date());
        layout.setUpdator("ulf");
        Assert.assertEquals(1, layoutDao.countAll());
        Assert.assertEquals(2, layoutDao.countMatching(new Criteria(VertexPositionEntity.class)));

        // Delete
        layoutDao.delete(layout);
        Assert.assertEquals(0, layoutDao.countAll());
        Assert.assertEquals(0, layoutDao.countMatching(new Criteria(VertexPositionEntity.class)));
    }

    private VertexPositionEntity createVertexPosition(String vertexNamespace, String vertexId, int x, int y) {
        VertexPositionEntity vertexPositionEntity = new VertexPositionEntity();
        vertexPositionEntity.setPosition(new PointEntity(x, y));
        vertexPositionEntity.setVertexRef(new VertexRefEntity(vertexNamespace, vertexId));
        return vertexPositionEntity;
    }
}
