/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.repository.local;

import org.easymock.EasyMock;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        LocalReportsDao m_localReportsDao = EasyMock.createMock(LocalReportsDao.class);
        LocalJasperReportsDao m_localJasperReportsDao = EasyMock.createMock(LocalJasperReportsDao.class);

        //TODO indigo: Mockup an InputStream with EasyMock is not trivial, InputStream isn't an interface
        // InputStream jrTemplateStream = EasyMock.createMock(InputStream.class);
        // EasyMock.expect(m_localJasperReportsDao.getTemplateStream("sample-report")).andReturn(jrTemplateStream);

        // Initialize the local report repository to provide reports from database-reports.xml and jasper-reports.xml
        m_legacyLocalReportRepository = new LegacyLocalReportRepository(m_localReportsDao, m_localJasperReportsDao);
        m_legacyLocalReportRepository.setLocalReportsDao(m_localReportsDao);
        m_legacyLocalReportRepository.setLocalJasperReportsDao(m_localJasperReportsDao);

        EasyMock.expect(m_localReportsDao.getOnlineReports()).andReturn(onlineReports);

        EasyMock.expect(m_localReportsDao.getDisplayName("sample-report")).andReturn("displayNameMockup");
        EasyMock.expect(m_localReportsDao.getReports()).andReturn(reports);
        EasyMock.expect(m_localReportsDao.getOnlineReports()).andReturn(onlineReports);
        EasyMock.expect(m_localReportsDao.getReportService("sample-report")).andReturn("jasperReportServiceMockup");

        EasyMock.expect(m_localJasperReportsDao.getTemplateLocation("sample-report")).andReturn("mocked-jdbc");
        EasyMock.expect(m_localJasperReportsDao.getEngine("sample-report")).andReturn("mocked-jdbc");

        EasyMock.replay(m_localReportsDao);
        EasyMock.replay(m_localJasperReportsDao);

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
        //TODO indigo: Mockup an InputStream with EasyMock is not trivial, InputStream isn't an interface
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
