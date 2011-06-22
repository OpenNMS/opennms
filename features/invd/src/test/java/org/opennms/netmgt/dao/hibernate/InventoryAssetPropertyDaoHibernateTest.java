//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.inventory.OnmsInventoryAsset;
import org.opennms.netmgt.model.inventory.OnmsInventoryAssetProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
	InvdConfigurationExecutionListener.class,
	TemporaryDatabaseExecutionListener.class,
	DependencyInjectionTestExecutionListener.class
})
@ContextConfiguration(locations={		
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-invDatabasePopulator.xml"
})
@JUnitTemporaryDatabase()
public class InventoryAssetPropertyDaoHibernateTest {
	@Autowired
    InventoryDatabasePopulator m_dbPopulator;
	
	@Before
	public void setUp() throws Exception {
        m_dbPopulator.populateDatabase();
    }
	
	@Test
	public void testInitialize() {
        // do nothing, just test that setUp() / tearDown() works
    }

	@Test
    public void testSaveOnmsInventoryAssetProperty() {
		OnmsInventoryAsset invAsset = getDbPopulator().getInventoryAssetDao().findByAssetId(getDbPopulator().getInvAsset1().getId());

        OnmsInventoryAssetProperty invAssetProp = new OnmsInventoryAssetProperty(
        		getDbPopulator().getInvAsset1(),
                "mac-addr",
                "00:00:00:00:00:00");
        invAssetProp.setInventoryAsset(invAsset);
        getDbPopulator().getInventoryAssetPropertyDao().save(invAssetProp);
        getDbPopulator().getInventoryAssetPropertyDao().flush();
        getDbPopulator().getInventoryAssetPropertyDao().clear();

        Collection<OnmsInventoryAssetProperty> props = getDbPopulator().getInventoryAssetPropertyDao().findAll();
        Assert.assertEquals("number of asset properties found", 3, props.size());
    }
	
	public InventoryDatabasePopulator getDbPopulator() {
		return m_dbPopulator;
	}

	public void setDbPopulator(InventoryDatabasePopulator dbPopulator) {
		m_dbPopulator = dbPopulator;
	}
}
