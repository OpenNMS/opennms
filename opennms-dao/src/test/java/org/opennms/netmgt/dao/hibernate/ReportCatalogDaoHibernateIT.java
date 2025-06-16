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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ReportCatalogDaoHibernateIT implements InitializingBean {
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
