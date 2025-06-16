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
package org.opennms.features.reporting.repository.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.reporting.dao.LocalReportsDao;
import org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportDefinition;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <p>LegacyLocalReportRepositoryTest class.</p>
 * <p/>
 * Test legacy local repository
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-reportingRepositoryTest.xml"})
public class LegacyLocalReportRepositoryTest {

    private LegacyLocalReportRepository m_legacyLocalReportRepository;

    private LocalReportsDao m_localReportsDao;
    private LocalJasperReportsDao m_localJasperReportsDao;

    /**
     * <p>setUp</p>
     * <p/>
     * Initialize and mockup the LegacyLocalReportRepository
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // Mocked report list as a result from DAOs
        List<BasicReportDefinition> reports = new ArrayList<>();
        List<BasicReportDefinition> onlineReports = new ArrayList<>();

        reports.add(new LegacyLocalReportDefinition());
        reports.add(new LegacyLocalReportDefinition());

        onlineReports.add(new LegacyLocalReportDefinition());

        // Mock DAOs for the local repository test
        m_localReportsDao = mock(LocalReportsDao.class);
        m_localJasperReportsDao = mock(LocalJasperReportsDao.class);

        //TODO indigo: Mockup an InputStream with Mockito is not trivial, InputStream isn't an interface
        // InputStream jrTemplateStream = mock(InputStream.class);
        // when(m_localJasperReportsDao.getTemplateStream("sample-report")).thenReturn(jrTemplateStream);

        // Initialize the local report repository to provide reports from database-reports.xml and jasper-reports.xml
        m_legacyLocalReportRepository = new LegacyLocalReportRepository(m_localReportsDao, m_localJasperReportsDao);
        m_legacyLocalReportRepository.setLocalReportsDao(m_localReportsDao);
        m_legacyLocalReportRepository.setLocalJasperReportsDao(m_localJasperReportsDao);

        when(m_localReportsDao.getOnlineReports()).thenReturn(onlineReports);

        when(m_localReportsDao.getDisplayName("sample-report")).thenReturn("displayNameMockup");
        when(m_localReportsDao.getReports()).thenReturn(reports);
        when(m_localReportsDao.getOnlineReports()).thenReturn(onlineReports);
        when(m_localReportsDao.getReportService("sample-report")).thenReturn("jasperReportServiceMockup");

        when(m_localJasperReportsDao.getTemplateLocation("sample-report")).thenReturn("mocked-jdbc");
        when(m_localJasperReportsDao.getEngine("sample-report")).thenReturn("mocked-jdbc");

        // Sanitycheck
        assertNotNull("Test if mocked DAO for database-reports.xml is not null", m_legacyLocalReportRepository.getLocalReportsDao());
        assertNotNull("Test if mocked DAo for jasper-reports.xml is not null", m_legacyLocalReportRepository.getLocalJasperReportsDao());
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
        verifyNoMoreInteractions(m_localReportsDao);
        verifyNoMoreInteractions(m_localJasperReportsDao);
        m_legacyLocalReportRepository = null;
    }

    /**
     * <p>testGetReports</p>
     * <p/>
     * Test to get local community reports from legacy local repository
     *
     * @throws Exception
     */
    @Test
    public void testGetReports() throws Exception {
        assertEquals("Test get mocked *ALL* reports from configured local repository", 2, m_legacyLocalReportRepository.getReports().size());

        verify(m_localReportsDao, atLeastOnce()).getReports();
    }

    /**
     * <p>testGetOnlineReports</p>
     * <p/>
     * Test to get *ONLINE* reports from the local repository
     *
     * @throws Exception
     */
    @Test
    public void testGetOnlineReports() throws Exception {
        //TODO indigo: Code covers no test for IllegalAccessException and InvocationTargetException
        assertEquals("Test get mocked *ONLINE* reports from configured local repository", 1, m_legacyLocalReportRepository.getOnlineReports().size());

        verify(m_localReportsDao, atLeastOnce()).getOnlineReports();
    }

    /**
     * <p>testGetReportService</p>
     * <p/>
     * Test to get report services by report ID
     *
     * @throws Exception
     */
    @Test
    public void testGetReportService() throws Exception {
        //TODO indigo: Code covers no test for IllegalAccessException and InvocationTargetException
        assertEquals("Test get mocket report service from repository by sample-report", "jasperReportServiceMockup", m_legacyLocalReportRepository.getReportService("sample-report"));

        verify(m_localReportsDao, atLeastOnce()).getReportService("sample-report");
    }

    /**
     * <p>testGetDisplayName</p>
     * <p/>
     * Test to get display name by report ID
     *
     * @throws Exception
     */
    @Test
    public void testGetDisplayName() throws Exception {
        assertEquals("Test get mocked display name from repository by sample-report", "displayNameMockup", m_legacyLocalReportRepository.getDisplayName("sample-report"));

        verify(m_localReportsDao, atLeastOnce()).getDisplayName("sample-report");
    }

    /**
     * <p>testGetEngine</p>
     * <p/>
     * Test to get report engine by report ID
     *
     * @throws Exception
     */
    @Test
    public void testGetEngine() throws Exception {
        assertEquals("Test get mocked engine from repository by ID", "mocked-jdbc", m_legacyLocalReportRepository.getEngine("sample-report"));

        verify(m_localJasperReportsDao, atLeastOnce()).getEngine("sample-report");
    }

    /**
     * <p>testGetTemplateStream</p>
     * <p/>
     * Test to get jasper report as template stream by report ID
     *
     * @throws Exception
     */
    @Test
    public void testGetTemplateStream() throws Exception {
        //TODO indigo: Mockup an InputStream is not trivial, InputStream isn't an interface
    }

    /**
     * <p>testGetRepositoryName</p>
     * <p/>
     * Test to get local repository ID
     *
     * @throws Exception
     */
    @Test
    public void testGetRepositoryId() throws Exception {
        assertEquals("Test get repository ID", "local", m_legacyLocalReportRepository.getRepositoryId());
    }

    /**
     * <p>testGetRepositoryName</p>
     * <p/>
     * Test to get local repository name
     *
     * @throws Exception
     */
    @Test
    public void testGetRepositoryName() throws Exception {
        assertEquals("Test get repository name", "Local Report Repository", m_legacyLocalReportRepository.getRepositoryName());
    }

    /**
     * <p>testGetRepositoryDescription</p>
     * <p/>
     * Test get local repository description
     *
     * @throws Exception
     */
    @Test
    public void testGetRepositoryDescription() throws Exception {
        assertEquals("Test get repository description", "Providing OpenNMS community reports from local disk.", m_legacyLocalReportRepository.getRepositoryDescription());
    }

    /**
     * <p>testGetManagementUrl</p>
     * <p/>
     * Test get local repository management URL
     *
     * @throws Exception
     */
    @Test
    public void testGetManagementUrl() throws Exception {
        assertEquals("Test get repository management url", "blank", m_legacyLocalReportRepository.getManagementUrl());
    }

    /**
     * <p>setLegacyLocalReportRepository</p>
     * <p/>
     * Set the repository implementation for test
     *
     * @param legacyLocalReportRepository a {@link org.opennms.features.reporting.repository.local.LegacyLocalReportRepository} object
     */
    public void setLegacyLocalReportRepository(LegacyLocalReportRepository legacyLocalReportRepository) {
        m_legacyLocalReportRepository = legacyLocalReportRepository;
    }
}
