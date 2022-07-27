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

package org.opennms.features.reporting.repository.remote;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.reporting.dao.remoterepository.DefaultRemoteRepositoryConfigDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>DefaultRemoteRepositoryTest class.</p>
 * <p/>
 * Test OpenNMS CONNECT remote repository
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
// TODO tak: test needs a full working remote repository server with configuration
@Ignore
public class DefaultRemoteRepositoryTest {
    
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryTest.class);
    
    private DefaultRemoteRepository m_defaultRemoteRepository;
    
    private static final String OPENNMS_HOME = "src/test/resources";

    private Resource m_configResource;

    @Before
    public void setUp() {
        System.setProperty("opennms.home", OPENNMS_HOME);
        assertEquals(OPENNMS_HOME, System.getProperty("opennms.home"));
        //TODO Tak: Jasper version is hard coded
        m_defaultRemoteRepository = new DefaultRemoteRepository(new DefaultRemoteRepositoryConfigDao(m_configResource).getActiveRepositories().get(0), "3.7.6");
        assertNotNull(System.getProperty("opennms.home"));
    }

    @Test
    public void getOnlineReports() {
        List<BasicReportDefinition> reports = m_defaultRemoteRepository.getOnlineReports();
        logger.debug("getOnlineReports");
        for (BasicReportDefinition report : reports) {
            logger.debug(report.toString());
        }
    }

    @Test
    public void getReports() {
        List<BasicReportDefinition> reports = m_defaultRemoteRepository.getReports();
        logger.debug("getReports");
        for (BasicReportDefinition report : reports) {
            logger.debug(report.toString());
        }
    }

    @Test
    public void reportIdsStartWithRepositoryIdTest() {
        List<BasicReportDefinition> reports = m_defaultRemoteRepository.getReports();
        logger.debug("reportIdsStartWithRepositoryIdTest");
        for (BasicReportDefinition report : reports) {
            assertTrue(report.getId().startsWith(m_defaultRemoteRepository.getRepositoryId()));
            logger.debug(report.getId());
            logger.debug("'{}'", report.getRepositoryId());
        }
    }

    @Test
    public void reportIdsWithRepoIdgetMappedToRemoteReportTest() {
        List<BasicReportDefinition> reports = m_defaultRemoteRepository.getReports();
        for (BasicReportDefinition report : reports) {
            assertTrue(m_defaultRemoteRepository.getDisplayName(report.getId()).length() > 0);
            logger.debug("'{}' \t '{}'", report.getId(), m_defaultRemoteRepository.getDisplayName(report.getId()));
        }
    }

    @Test
    public void catchUniformExceptionTest() {
        String report = m_defaultRemoteRepository.getReportService("null");
        logger.debug("ReportService for null-report '{}'", report);
    }
}
