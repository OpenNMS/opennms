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

package org.opennms.features.reporting.repository.global;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

//TODO Tak: check this test and handel remote-repository connections
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/opennms/applicationContext-reportingRepositoryTest.xml"})
public class DefaultGlobalReportRepositoryTest {

    Logger logger = LoggerFactory.getLogger(DefaultGlobalReportRepositoryTest.class);

    private GlobalReportRepository m_globalReportRepository;

    private static final String OPENNMS_HOME = "src/test/resources";

    @BeforeClass
    public static void setup() {
        System.setProperty("opennms.home", OPENNMS_HOME);
        assertEquals(OPENNMS_HOME, System.getProperty("opennms.home"));
    }

    @Before
    public void init() {
        System.out.println("DefaultGlobalReportRepository: " + m_globalReportRepository);
    }

//    @Test
//    public void addReportRepositoryTest() {
//        ReportRepository newRepository = m_legacyLocalReportRepository;
//        assertEquals("local", newRepository.getRepositoryId());
//        assertEquals(2, globalRepo.getRepositoryList().size());
//        globalRepo.addReportRepository(newRepository);
//        assertEquals(3, globalRepo.getRepositoryList().size());
//
//        List<ReportRepository> repositoryList = globalRepo.getRepositoryList();
//        assertEquals("local", repositoryList.get(0).getRepositoryId());
//        assertEquals("cioreporting", repositoryList.get(1).getRepositoryId());
//        assertEquals("local", repositoryList.get(2).getRepositoryId());
//    }

    @Test
    public void getAllOnlineReportsTest() {
        System.out.println("DefaultGlobalReportRepository: " + m_globalReportRepository);
        List<BasicReportDefinition> resultList = m_globalReportRepository.getAllOnlineReports();
        //TODO Tak: check this test it was set to 23...
        assertEquals(16, resultList.size());
        for (BasicReportDefinition report : resultList) {
            logger.debug("getAllOnlineReportsTest: report '{}' is online '{}'", report.getId(), report.getOnline());
            assertTrue(report.getId().contains("_"));
            assertTrue(report.getOnline());
        }
    }

    @Test
    public void getAllReportsTest() {
        List<BasicReportDefinition> resultList = m_globalReportRepository.getAllReports();
        //TODO Tak: check this test it was set to 27...
        assertEquals(18, resultList.size());
        for (BasicReportDefinition report : resultList) {
            assertTrue(report.getId().contains("_"));
            logger.debug("getAllReportsTest: report '{}' is online '{}'", report.getId(), report.getOnline());
        }
    }

    @Test
    public void getEngineTest() {
        BasicReportDefinition report = m_globalReportRepository.getAllOnlineReports().get(0);
        assertEquals("local_sample-report", report.getId());
        assertEquals("Kochwurst sample JasperReport", report.getDisplayName());
        assertEquals("jdbc", m_globalReportRepository.getEngine("local_sample-report"));
    }

    @Test
    public void getOnlineReportsTest() {
        List<BasicReportDefinition> reportList = m_globalReportRepository.getOnlineReports("local");
        assertEquals(16, reportList.size());
    }

    @Test
    public void getRemplateStreamTest() throws IOException {
        BasicReportDefinition report = m_globalReportRepository.getAllOnlineReports().get(0);
        assertEquals("local_sample-report", report.getId());
        assertEquals("Kochwurst sample JasperReport", report.getDisplayName());
        InputStream templateStream = m_globalReportRepository.getTemplateStream("local_sample-report");
        assertEquals(4822, templateStream.available());
        templateStream.close();
    }

    @Test
    public void getReportServiceTest() {
        BasicReportDefinition report = m_globalReportRepository.getAllOnlineReports().get(0);
        assertEquals("local_sample-report", report.getId());
        assertEquals("Kochwurst sample JasperReport", report.getDisplayName());
        assertEquals("jasperReportService", m_globalReportRepository.getReportService("local_sample-report"));
    }

    @Test
    public void getReportsTest() {
        List<BasicReportDefinition> reportList = m_globalReportRepository.getReports("local");
        assertEquals(18, reportList.size());
    }

    @Test
    public void getRepositoryByIdTest() {
        assertNotNull(m_globalReportRepository);
        List<ReportRepository> repositoryList = m_globalReportRepository.getRepositoryList();
        assertEquals(2, repositoryList.size());
        for (ReportRepository repository : repositoryList) {
            assertEquals(repository.getRepositoryId(), m_globalReportRepository.getRepositoryById(repository.getRepositoryId()).getRepositoryId());
        }
        assertFalse(m_globalReportRepository.getRepositoryById("") != null);

        logger.debug(m_globalReportRepository.getDisplayName("local_sample-report"));
        logger.debug("local repository : '{}'", m_globalReportRepository.getRepositoryById("cioreporting"));
        ReportRepository localRepo = m_globalReportRepository.getRepositoryById("local");
        logger.debug(localRepo.toString());
        BasicReportDefinition report = localRepo.getOnlineReports().get(0);
        logger.debug(report.toString());
        logger.debug(report.getId());
        report.setId("LOCAL_" + report.getId());
        assertEquals("LOCAL_local_sample-report", report.getId());
        assertFalse(("local_LOCAL_local_sample-report".equals(localRepo.getOnlineReports().get(0).getId())));
        assertEquals("local_sample-report", localRepo.getOnlineReports().get(0).getId());
    }

    @Test
    public void getRepositoryListTest() {
        List<ReportRepository> repositoryList = m_globalReportRepository.getRepositoryList();
        assertEquals(2, repositoryList.size());
        assertEquals("local", repositoryList.get(0).getRepositoryId());
        assertEquals("cioreporting", repositoryList.get(1).getRepositoryId());
    }

    public void setReportRepository(GlobalReportRepository globalReportRepository) {
        m_globalReportRepository = globalReportRepository;
    }
}