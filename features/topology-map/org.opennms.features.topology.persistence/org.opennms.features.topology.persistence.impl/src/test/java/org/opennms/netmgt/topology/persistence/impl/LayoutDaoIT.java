/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
