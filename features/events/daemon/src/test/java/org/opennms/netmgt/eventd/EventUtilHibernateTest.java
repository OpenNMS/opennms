/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class EventUtilHibernateTest {

    @Autowired
    private EventUtilDaoImpl eventUtilDaoImpl;

    @Autowired
    private DatabasePopulator m_populator;

    @Autowired
    private AssetRecordDao m_assetRecordDao;

    @Autowired
    private HwEntityDao m_hwEntityDao;

    @Before
    public void setUp() throws Exception {
    	m_populator.populateDatabase();
    }

    @Test
    public void testGetNodeLabel() {
    	String label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode3().getId());
		assertEquals("node3",label);
		label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode1().getId());
		assertEquals("node1",label);
		label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode2().getId());
		assertEquals("node2",label);
    }
    
    @Test
    public void testGetIfAlias() {
    	String alias = eventUtilDaoImpl.getIfAlias(m_populator.getNode1().getId(), "192.168.1.1");
    	assertEquals("Initial ifAlias value", alias);
    }
    
    @Test
    public void testGetAssetFieldValue() {
        OnmsNode node1 = m_populator.getNode1();
        OnmsAssetRecord asset1 = node1.getAssetRecord();
        asset1.setAdmin("some-adm1n-label");
        asset1.setSerialNumber("42");
        m_assetRecordDao.saveOrUpdate(asset1);

        String asset = eventUtilDaoImpl.getAssetFieldValue("asset[admin]", node1.getId());
        assertEquals("some-adm1n-label", asset);

        asset = eventUtilDaoImpl.getAssetFieldValue("asset[serialNumber]", node1.getId());
        assertEquals("42", asset);

        // Checking case sensitivity
        asset = eventUtilDaoImpl.getAssetFieldValue("asset[serialnumber]", node1.getId());
        assertEquals("42", asset);
    }

    @Test
    public void getHardwareFieldValue() {
        OnmsNode node1 = m_populator.getNode1();
        OnmsHwEntity hwEntity = new OnmsHwEntity();
        hwEntity.setNode(node1);
        hwEntity.setEntPhysicalIndex(0);
        hwEntity.setEntPhysicalName("Chassis");
        hwEntity.setEntPhysicalDescr("some-physical-d3scr");
        m_hwEntityDao.save(hwEntity);

        // Access the field by index
        String hwfield = eventUtilDaoImpl.getHardwareFieldValue("hardware[0:entPhysicalDescr]", node1.getId());
        assertEquals("some-physical-d3scr", hwfield);

        // Access the field by name
        hwfield = eventUtilDaoImpl.getHardwareFieldValue("hardware[Chassis:entPhysicalDescr]", node1.getId());
        assertEquals("some-physical-d3scr", hwfield);

        // Access the field by regex
        hwfield = eventUtilDaoImpl.getHardwareFieldValue("hardware[~%Cha%:entPhysicalDescr]", node1.getId());
        assertEquals("some-physical-d3scr", hwfield);
    }
}
