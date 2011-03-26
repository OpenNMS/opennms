/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 10th 2010 jonathan@opennms.org
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.ReportCatalogDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase()
public class ReportCatalogDaoHibernateTest {
    @Autowired
	private ReportCatalogDao m_reportCatalogDao;

	@Test
	@Transactional
    public void testSave() {
        
        Date date = new Date();
        ReportCatalogEntry catalogEntry = new ReportCatalogEntry();
        catalogEntry.setReportId("reportId_1");
        catalogEntry.setLocation("location_1");
        catalogEntry.setTitle("title_1");
        catalogEntry.setDate(date);
        m_reportCatalogDao.save(catalogEntry);
        
        Integer id = catalogEntry.getId();
        
        ReportCatalogEntry retrievedEntry = m_reportCatalogDao.get(id);
        
        assertEquals(catalogEntry.getReportId(),retrievedEntry.getReportId());
        assertEquals(catalogEntry.getTitle(), retrievedEntry.getTitle());
        assertEquals(catalogEntry.getLocation(), retrievedEntry.getLocation());
        assertEquals(0,catalogEntry.getDate().compareTo(retrievedEntry.getDate()));
        
        
    }
    
	@Test
	@Transactional
    public void testDelete() {
        
        Date date = new Date();
        ReportCatalogEntry catalogEntry = new ReportCatalogEntry();
        catalogEntry.setReportId("reportId_2");
        catalogEntry.setLocation("location_2");
        catalogEntry.setTitle("title_2");
        catalogEntry.setDate(date);
        m_reportCatalogDao.save(catalogEntry);
        
        Integer id = catalogEntry.getId();
        assertNotNull(m_reportCatalogDao.get(id));
        m_reportCatalogDao.delete(id);
        assertNull(m_reportCatalogDao.get(id));
        
    }

    

}
