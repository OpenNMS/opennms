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
import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.inventory.OnmsInventoryCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={		
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-invDatabasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase()
public class InventoryCategoryDaoHibernateTest {
	@Autowired
    InventoryDatabasePopulator m_dbPopulator;
	
	@Before
	public void setUp() throws Exception {
        m_dbPopulator.populateDatabase();
    }
	
	@Test
	@Transactional
    public void testInitialize() {
        // do nothing, just test that setUp() / tearDown() works
    }

	@Test
	@Transactional
    public void testSaveOnmsInventoryCategory() {
        // Create a new inventory category.
        OnmsInventoryCategory invCat = new OnmsInventoryCategory("Example Category");

        getDbPopulator().getInventoryCategoryDao().save(invCat);
        getDbPopulator().getInventoryCategoryDao().flush();
        getDbPopulator().getInventoryCategoryDao().clear();

        // Now pull it back up and make sure it saved.
        //Object [] args = { invCat.getId() };
        //assertEquals(1, getJdbcTemplate().queryForInt("select count(*) from inventorycategory where id = ?", args));

        OnmsInventoryCategory invCat2 = getDbPopulator().getInventoryCategoryDao().findCategoryId(invCat.getId());
        Assert.assertNotSame(invCat, invCat2);
        Assert.assertEquals(invCat.getId(), invCat2.getId());
        Assert.assertEquals(invCat.getCategoryId(), invCat2.getCategoryId());
        Assert.assertEquals(invCat.getCategoryName(), invCat2.getCategoryName());
    }

	@Test
	@Transactional
    public void testFindByName() {
        OnmsInventoryCategory invCat = getDbPopulator().getInventoryCategoryDao().findByName("Network Equipment");
        Assert.assertEquals("Network Equipment", invCat.getCategoryName());
    }
    
	@Test
	@Transactional
	public void testfindCategoriesUsedByNode() {
		OnmsNode node = getDbPopulator().getNode1();
		
		Collection<OnmsInventoryCategory> categories = getDbPopulator().getInventoryCategoryDao().findCategoriesUsedByNode(node);
		assertEquals("There should be 1 category used by default node",1,categories.size());
	}
	
	public InventoryDatabasePopulator getDbPopulator() {
		return m_dbPopulator;
	}

	public void setDbPopulator(InventoryDatabasePopulator dbPopulator) {
		m_dbPopulator = dbPopulator;
	}
}
