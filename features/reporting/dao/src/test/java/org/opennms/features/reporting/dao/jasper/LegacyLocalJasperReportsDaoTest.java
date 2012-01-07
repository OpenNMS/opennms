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

package org.opennms.features.reporting.dao.jasper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * <p>LegacyLocalJasperReportsDaoTest class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:reportingDaoTest-context.xml"})
public class LegacyLocalJasperReportsDaoTest {

    /**
     * Local report data access object to test
     */
    @Autowired
    private LegacyLocalJasperReportsDao m_legacyLocalJasperReportsDao;

    /**
     * Resource for local-jasper-reports.xml
     */
    private String m_configFile;

    /**
     * Resource for jasper report templates
     */
    private String m_jasperReportResource;

    /**
     * <p>setUp</p>
     *
     * Initialize the configuration file. Check if the configuration file exist. Try retrieve the jasper report
     * templates from configuration.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // Injected configuration
        assertNotNull("Inject legacy local report data access.", m_legacyLocalJasperReportsDao);

        m_configFile = m_legacyLocalJasperReportsDao.getConfigResource().getFile().getAbsolutePath();
        assertTrue("Config file " + m_configFile + " exist", m_legacyLocalJasperReportsDao.getConfigResource().exists());
        assertTrue("Config file " + m_configFile + " is readable", m_legacyLocalJasperReportsDao.getConfigResource().isReadable());
        
        m_jasperReportResource = m_legacyLocalJasperReportsDao.getJasperReportResources().getFile().getPath();
        assertTrue("Report template folder " + m_jasperReportResource + " exist", m_legacyLocalJasperReportsDao.getJasperReportResources().exists());

        // Unmarshal with JAXB to load XML into POJO's
        m_legacyLocalJasperReportsDao.afterPropertiesSet();
        assertNotNull("Test to retrieve 3 jasper reports from " + m_configFile, m_legacyLocalJasperReportsDao);
    }

    /**
     * <p>testJasperTemplateLocation</p>
     *
     * Tests to retrieve all jasper report template locations
     *
     * @throws Exception
     */
    @Test
    public void testJasperTemplateLocation() throws Exception {
        assertEquals("Test jasper sample-report template location", "sample-report.jrxml", m_legacyLocalJasperReportsDao.getTemplateLocation("sample-report"));
        assertEquals("Test jasper online-sample-report template location", "sample-report.jrxml", m_legacyLocalJasperReportsDao.getTemplateLocation("online-sample-report"));
        assertEquals("Test jasper not-online-sample-report template location","sample-report.jrxml", m_legacyLocalJasperReportsDao.getTemplateLocation("not-online-sample-report"));
    }

    /**
     * <p>testJasperReportEngine</p>
     *
     * Tests to retrieve all jasper report engines
     *
     * @throws Exception
     */
    @Test
    public void testJasperReportEngine() throws Exception {
        assertEquals("Test jasper sample-report engine","jdbc", m_legacyLocalJasperReportsDao.getEngine("sample-report"));
        assertEquals("Test jasper online-sample-report engine","jdbc", m_legacyLocalJasperReportsDao.getEngine("online-sample-report"));
        assertEquals("Test jasper not-online-sample-report engine","jdbc", m_legacyLocalJasperReportsDao.getEngine("not-online-sample-report"));
    }

    /**
     * <p>testTemplateStream</p>
     *
     * Tests to retrieve all jasper report template streams
     *
     * @throws Exception
     */
    @Test
    public void testTemplateStream() throws Exception {
        assertNotNull("Test to retrieve sample-report", m_legacyLocalJasperReportsDao.getTemplateStream("sample-report"));
        assertNotNull("Test to retrieve online-sample-report", m_legacyLocalJasperReportsDao.getTemplateStream("online-sample-report"));
        assertNotNull("Test to retrieve not-online-sample-report", m_legacyLocalJasperReportsDao.getTemplateStream("not-online-sample-report"));
    }

    /**
     * <p>setLegacyLocalJasperReportsDao</p>
     *
     * Set the configured data access object with injection
     *
     * @param legacyLocalJasperReportsDao a {@link org.opennms.features.reporting.dao.jasper.LegacyLocalJasperReportsDao} object
     */
    public void setLegacyLocalJasperReportsDao(LegacyLocalJasperReportsDao legacyLocalJasperReportsDao) {
        this.m_legacyLocalJasperReportsDao = legacyLocalJasperReportsDao;
    }

    /**
     * <p>getLegacyLocalJasperReportsDao</p>
     *
     * Get the configured data access object injected by Spring
     *
     * @return a {@link org.opennms.features.reporting.dao.jasper.LegacyLocalJasperReportsDao} object
     */
    public LegacyLocalJasperReportsDao getLegacyLocalJasperReportsDao() {
        return this.m_legacyLocalJasperReportsDao;
    }
}
