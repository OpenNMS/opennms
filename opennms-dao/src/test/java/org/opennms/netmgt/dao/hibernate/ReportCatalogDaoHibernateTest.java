/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.ReportCatalogDao;
import org.opennms.netmgt.model.ReportCatalogEntry;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ReportCatalogDaoHibernateTest implements InitializingBean {
    @Autowired
	private ReportCatalogDao m_reportCatalogDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

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
