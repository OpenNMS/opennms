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
package org.opennms.features.reporting.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <p>LegacyLocalReportsDaoTest class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-reportingDaoTest.xml"})
public class LegacyLocalReportsDaoTest implements InitializingBean {

    /**
     * Local report data access object to test
     */
    @Autowired
    private LocalReportsDao m_legacyLocalReportsDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /**
     * <p>tearDown</p>
     * <p/>
     * Cleanup
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        m_legacyLocalReportsDao = null;
    }

    /**
     * <p>testReportDescription</p>
     * <p/>
     * Tests to retrieve all descriptions from report configuration file
     *
     * @throws Exception
     */
    @Test
    public void testReportDescription() throws Exception {
        assertEquals("First report description test", "sample Jasper report using jdbc datasource", m_legacyLocalReportsDao.getReports().get(0).getDescription());
        assertEquals("Second report description test", "online sample Jasper report using jdbc datasource", m_legacyLocalReportsDao.getReports().get(1).getDescription());
        assertEquals("Third report description test", "NOT online sample Jasper report using jdbc datasource", m_legacyLocalReportsDao.getReports().get(2).getDescription());
    }

    /**
     * <p>testReportId</p>
     * <p/>
     * Tests to retrieve all IDs from report file
     *
     * @throws Exception
     */
    @Test
    public void testReportId() throws Exception {
        assertEquals("First report id test", "sample-report", m_legacyLocalReportsDao.getReports().get(0).getId());
        assertEquals("Second report id test", "online-sample-report", m_legacyLocalReportsDao.getReports().get(1).getId());
        assertEquals("Third report id test", "not-online-sample-report", m_legacyLocalReportsDao.getReports().get(2).getId());
    }

    /**
     * <p>testReportDisplayName</p>
     * <p/>
     * Test to retrieve all DisplayNames from report configuration file
     *
     * @throws Exception
     */
    @Test
    public void testReportDisplayName() throws Exception {
        assertEquals("First report display name test", "sample JasperReport", m_legacyLocalReportsDao.getReports().get(0).getDisplayName());
        assertEquals("Second report display name test", "online sample JasperReport", m_legacyLocalReportsDao.getReports().get(1).getDisplayName());
        assertEquals("Third report display name test", "NOT online sample JasperReport", m_legacyLocalReportsDao.getReports().get(2).getDisplayName());
    }

    /**
     * <p>testReportService</p>
     * <p/>
     * Test to retrieve all report services from report configuration file
     *
     * @throws Exception
     */
    @Test
    public void testReportService() throws Exception {
        assertEquals("First report report-service test", "jasperReportService", m_legacyLocalReportsDao.getReports().get(0).getReportService());
        assertEquals("Second report report-service test", "jasperReportService", m_legacyLocalReportsDao.getReports().get(1).getReportService());
        assertEquals("Third report report-service test", "jasperReportService", m_legacyLocalReportsDao.getReports().get(2).getReportService());
    }

    /**
     * <p>testReportRepositoryId</p>
     * <p/>
     * Test to retrieve repository IDs. The configuration is read in a repository. The repository will set his
     * repository id. For this reason the repository ID should be NULL.
     *
     * @throws Exception
     */
    @Test
    public void testReportRepositoryId() throws Exception {
        assertNull("First report repository id test", m_legacyLocalReportsDao.getReports().get(0).getRepositoryId());
        assertNull("Second report repository id test", m_legacyLocalReportsDao.getReports().get(1).getRepositoryId());
        assertNull("Third report repository id test", m_legacyLocalReportsDao.getReports().get(2).getRepositoryId());
    }

    /**
     * <p>testReportAllowAccess</p>
     * <p/>
     * Test to retrieve the allow access configuration. The implementation of a repository configure this property. By
     * default the allowAccess property is set to "FALSE"
     *
     * @throws Exception
     */
    @Test
    public void testReportAllowAccess() throws Exception {
        assertFalse("First report allow access test", m_legacyLocalReportsDao.getReports().get(0).getAllowAccess());
        assertFalse("Second report allow access test", m_legacyLocalReportsDao.getReports().get(1).getAllowAccess());
        assertFalse("Third report allow access test", m_legacyLocalReportsDao.getReports().get(2).getAllowAccess());
    }

    /**
     * <p>testReportIsOnline</p>
     * <p/>
     * Test to retrieve is online property.
     *
     * @throws Exception
     */
    @Test
    public void testReportIsOnline() throws Exception {
        assertTrue("First report is online test", m_legacyLocalReportsDao.getReports().get(0).getOnline());
        assertTrue("Second report is online test", m_legacyLocalReportsDao.getReports().get(1).getOnline());
        assertFalse("Third report is online test", m_legacyLocalReportsDao.getReports().get(2).getOnline());
        assertFalse("Third report is online test", m_legacyLocalReportsDao.getReports().get(2).getOnline());
    }
}
