/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.reporting.repository.local;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.reporting.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@ContextConfiguration(locations = {"classpath:reportingRepositoryTest-context.xml"})
public class LegacyLocalReportRepositoryTest {

    /**
     * Local repository for local community reports to test
     */
    @Autowired
    @Qualifier("reportRepository")
    private ReportRepository m_reportRepository;

    /**
     * <p>setUp</p>
     * <p/>
     * Initialize and mockup the LegacyLocalReportRepository
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        System.out.println("Setup: " + m_reportRepository.getReportService("sample-report"));
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
        m_reportRepository = null;
    }

    /**
     * Test to get local community reports from legacy local repository
     *
     * @throws Exception
     */
    @Test
    public void testGetReports() throws Exception {
        //assertNotNull("Test if legacy local repository is null", m_reportRepository.getReports());
        //assertEquals("Test the size of report list from local repository", "", m_reportRepository.getReports().size());
    }

    @Test
    public void testGetOnlineReports() throws Exception {
        //assertEquals("Test get online reports from *ALL* configured local repository","", m_reportRepository.getOnlineReports());
    }

    @Test
    public void testGetReportService() throws Exception {
        System.out.println("Huhu: " + m_reportRepository.getRepositoryName());
        System.out.println("Huhu: " + m_reportRepository.getReports());

//        assertEquals("", "", m_reportRepository.getReportService("sample-report"));
//        assertEquals("","",m_reportRepository.getReportService("online-sample-report"));
//        assertEquals("", "",m_reportRepository.getReportService("not-online-sample-report"));
    }

    @Test
    public void testGetDisplayName() throws Exception {

    }

    @Test
    public void testGetEngine() throws Exception {

    }

    @Test
    public void testGetTemplateStream() throws Exception {

    }

    @Test
    public void testGetRepositoryId() throws Exception {

    }

    @Test
    public void testGetRepositoryName() throws Exception {

    }

    @Test
    public void testGetRepositoryDescription() throws Exception {

    }

    @Test
    public void testGetManagementUrl() throws Exception {

    }

    public void setReportRepository(LegacyLocalReportRepository legacyLocalReportRepository) {
        m_reportRepository = legacyLocalReportRepository;
    }
}
