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

package org.opennms.features.reporting.repository.global;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.reporting.dao.remoterepository.RemoteRepositoryConfigDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * <p>DefaultGlobalReportRepositoryTest class.</p>
 * <p/>
 * Test the implementation of the default global repository.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.8.1
 */
//TODO Tak: check this test and handle remote-repository connections
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-reportingRepositoryTest.xml"})
public class DefaultGlobalReportRepositoryTest {

    Logger logger = LoggerFactory.getLogger(DefaultGlobalReportRepositoryTest.class);

    private DefaultGlobalReportRepository m_globalReportRepository;

    private ReportRepository m_mockLocalReportRepository;

    private ReportRepository m_remoteReportRepository;

    private List<RemoteRepositoryDefinition> m_mockActiveRepositoriesList;

    private List<RemoteRepositoryDefinition> m_mockAllRepositoriesList;

    private List<BasicReportDefinition> m_mockReportList;

    private BasicReportDefinition m_mockReportDefinition1;

    private BasicReportDefinition m_mockReportDefinition2;

    private RemoteRepositoryConfigDao m_mockRemoteRepositoryConfigDao;

    private RemoteRepositoryDefinition m_mockActiveRemoteRepository;

    private RemoteRepositoryDefinition m_mockNotActiveRemoteRepository;

    @Before
    public void setUp() {

        // Mockup for remote repository config dao
        m_mockRemoteRepositoryConfigDao = EasyMock.createNiceMock(RemoteRepositoryConfigDao.class);

        // Mockup for a list of reports
        m_mockReportList = new ArrayList<>();

        // Mockup for a online active report
        m_mockReportDefinition1 = EasyMock.createNiceMock(BasicReportDefinition.class);
        EasyMock.expect(m_mockReportDefinition1.getAllowAccess()).andReturn(Boolean.TRUE);
        EasyMock.expect(m_mockReportDefinition1.getDescription()).andReturn("MockReportDescription1");
        EasyMock.expect(m_mockReportDefinition1.getDisplayName()).andReturn("MockupReportDisplayName1");
        EasyMock.expect(m_mockReportDefinition1.getOnline()).andReturn(Boolean.TRUE);
        EasyMock.expect(m_mockReportDefinition1.getReportService()).andReturn("MockupReportService1");
        EasyMock.expect(m_mockReportDefinition1.getRepositoryId()).andReturn("local_MockupReportReportId1");
        EasyMock.expect(m_mockReportDefinition1.getId()).andReturn("local_MockupReportId1");
        EasyMock.replay(m_mockReportDefinition1);

        // Mockup for an inactive not online report
        m_mockReportDefinition2 = EasyMock.createNiceMock(BasicReportDefinition.class);
        EasyMock.expect(m_mockReportDefinition2.getAllowAccess()).andReturn(Boolean.FALSE);
        EasyMock.expect(m_mockReportDefinition2.getDescription()).andReturn("MockReportDescription2");
        EasyMock.expect(m_mockReportDefinition2.getDisplayName()).andReturn("MockupReportDisplayName2");
        EasyMock.expect(m_mockReportDefinition2.getOnline()).andReturn(Boolean.FALSE);
        EasyMock.expect(m_mockReportDefinition2.getReportService()).andReturn("MockupReportService2");
        EasyMock.expect(m_mockReportDefinition2.getRepositoryId()).andReturn("local_MockupReportReportId2");
        EasyMock.expect(m_mockReportDefinition2.getId()).andReturn("MockupReportId2");
        EasyMock.replay(m_mockReportDefinition2);

        // Add mockup report to the report list 
        m_mockReportList.add(m_mockReportDefinition1);
        m_mockReportList.add(m_mockReportDefinition2);

        // Mockup a local report repository 
        m_mockLocalReportRepository = EasyMock.createNiceMock(ReportRepository.class);
        EasyMock.expect(m_mockLocalReportRepository.getOnlineReports()).andReturn(m_mockReportList);
        EasyMock.expect(m_mockLocalReportRepository.getReports()).andReturn(m_mockReportList);
        EasyMock.expect(m_mockLocalReportRepository.getDisplayName("MockedReportId")).andReturn("MockedDisplayName");
        EasyMock.expect(m_mockLocalReportRepository.getEngine("MockupReportId")).andReturn("MockedEngine");
        EasyMock.expect(m_mockLocalReportRepository.getManagementUrl()).andReturn("MockupManagementUrl");
        EasyMock.expect(m_mockLocalReportRepository.getRepositoryDescription()).andReturn("MockedRepositoryDescription");
        EasyMock.expect(m_mockLocalReportRepository.getReportService("MockedReportId")).andReturn("MockedReportService");
        EasyMock.expect(m_mockLocalReportRepository.getRepositoryId()).andReturn("local");
        EasyMock.expect(m_mockLocalReportRepository.getRepositoryName()).andReturn("MockedRepositoryName");
        EasyMock.replay(m_mockLocalReportRepository);

        // Mockup for one remote repository
        m_remoteReportRepository = EasyMock.createNiceMock(ReportRepository.class);
        EasyMock.expect(m_remoteReportRepository.getOnlineReports()).andReturn(m_mockReportList);
        EasyMock.expect(m_remoteReportRepository.getReports()).andReturn(m_mockReportList);
        EasyMock.expect(m_remoteReportRepository.getDisplayName("MockedReportId")).andReturn("MockedRemoteDisplayName");
        EasyMock.expect(m_remoteReportRepository.getEngine("MockupReportId")).andReturn("MockedRemoteEngine");
        EasyMock.expect(m_remoteReportRepository.getManagementUrl()).andReturn("MockupRemoteManagementUrl");
        EasyMock.expect(m_remoteReportRepository.getRepositoryDescription()).andReturn("MockedRemoteRepositoryDescription");
        EasyMock.expect(m_remoteReportRepository.getReportService("MockedReportId")).andReturn("MockedTemoteReportService");
        EasyMock.expect(m_remoteReportRepository.getRepositoryId()).andReturn("MockedRemoteRepositoryId");
        EasyMock.expect(m_remoteReportRepository.getRepositoryName()).andReturn("MockedRemoteRepositoryName");
        EasyMock.replay(m_remoteReportRepository);

        // Mockup two remote repositories
        m_mockActiveRemoteRepository = new RemoteRepositoryDefinition();
        m_mockActiveRemoteRepository.setLoginRepoPassword("MockLoginRepoPasswordActive");
        m_mockActiveRemoteRepository.setLoginUser("MockLoginUserActive");
        m_mockActiveRemoteRepository.setRepositoryActive(Boolean.TRUE);
        m_mockActiveRemoteRepository.setRepositoryDescription("MockRepositoryDescriptionActive");
        m_mockActiveRemoteRepository.setRepositoryId("MockRepositoryIdActive");
        m_mockActiveRemoteRepository.setRepositoryManagementURL("MockRepositoryManagementURLActive");
        m_mockActiveRemoteRepository.setRepositoryName("MockRepositoryNameActive");

        m_mockNotActiveRemoteRepository = new RemoteRepositoryDefinition();
        m_mockNotActiveRemoteRepository.setLoginRepoPassword("MockLoginRepoPasswordDeactivated");
        m_mockNotActiveRemoteRepository.setLoginUser("MockLoginUserDeactivated");
        m_mockNotActiveRemoteRepository.setRepositoryActive(Boolean.FALSE);
        m_mockNotActiveRemoteRepository.setRepositoryDescription("MockRepositoryDescriptionDeactivated");
        m_mockNotActiveRemoteRepository.setRepositoryId("MockRepositoryIdDeactivated");
        m_mockNotActiveRemoteRepository.setRepositoryManagementURL("MockRepositoryManagementURLDeactivated");
        m_mockNotActiveRemoteRepository.setRepositoryName("MockRepositoryNameDeactivated");

        m_mockActiveRepositoriesList = new ArrayList<>();
        m_mockActiveRepositoriesList.add(m_mockActiveRemoteRepository);

        m_mockAllRepositoriesList = new ArrayList<>();
        m_mockAllRepositoriesList.add(m_mockActiveRemoteRepository);
        m_mockAllRepositoriesList.add(m_mockNotActiveRemoteRepository);

        EasyMock.expect(m_mockRemoteRepositoryConfigDao.getActiveRepositories()).andReturn(m_mockActiveRepositoriesList);
        EasyMock.expect(m_mockRemoteRepositoryConfigDao.getAllRepositories()).andReturn(m_mockAllRepositoriesList);
        EasyMock.expect(m_mockRemoteRepositoryConfigDao.getLoginRepoPassword("repositoryId")).andReturn("MockedDaoLoginPassword");
        EasyMock.expect(m_mockRemoteRepositoryConfigDao.getLoginUser("repositoryId")).andReturn("MockedDaoLoginUser");
        EasyMock.expect(m_mockRemoteRepositoryConfigDao.getRepositoryById("repositoryId")).andReturn(m_mockActiveRemoteRepository);
        EasyMock.expect(m_mockRemoteRepositoryConfigDao.getRepositoryDescription("repositoryId")).andReturn("MockedDaoRepositoryDescription");
        EasyMock.expect(m_mockRemoteRepositoryConfigDao.getRepositoryManagementURL("repositoryId")).andReturn("MockedDaoManagementUrl");
        EasyMock.expect(m_mockRemoteRepositoryConfigDao.getRepositoryName("repositoryId")).andReturn("MockedDaoRepositoryName");
        EasyMock.replay(m_mockRemoteRepositoryConfigDao);

        m_globalReportRepository = new DefaultGlobalReportRepository(m_mockRemoteRepositoryConfigDao, m_mockLocalReportRepository);
    }

    @Test
    @Ignore
    public void addReportRepositoryTest() {
        assertEquals("Repository is initialized with one configured repository", 1, m_globalReportRepository.getRepositoryList().size());
        m_globalReportRepository.addReportRepository(m_mockLocalReportRepository);
        assertEquals("One repository is added", 2, m_globalReportRepository.getRepositoryList().size());
    }

    @Test
    public void getAllOnlineReportsTest() {
        assertEquals("Test size of online reports", 2, m_globalReportRepository.getAllOnlineReports().size());
    }

    @Test
    public void getAllReportsTest() {
        assertEquals("Test size of online reports", 2, m_globalReportRepository.getAllReports().size());
    }

    @Test
    public void getEngineTest() {
        // TODO tak: improve this tests
        //assertNotNull("Test to get online reports by repository id", m_globalReportRepository.getEngine("local_MockupReportReportId2"));
    }

    @Test
    public void getOnlineReportsTest() {
        // TODO tak: improve this tests
        //assertNotNull("Test to get online reports by repository id", m_globalReportRepository.getOnlineReports("local_MockupReportRepositoryId1"));
    }

    @Ignore
    @Test
    public void getRemplateStreamTest() {
        // TODO tak: improve this tests
    }

    @Ignore
    @Test
    public void getReportServiceTest() {
        // TODO tak: improve this tests
        // assertNotNull("Test to get report repository by id", m_globalReportRepository.getReportService("local_MockupReportReportId2"));
    }

    @Ignore
    @Test
    public void getReportsTest() {
        // TODO tak: improve this tests
        // assertNotNull("Test to get reports by repository id", m_globalReportRepository.getReports("local_MockupReportReportId2"));
    }

    @Ignore
    @Test
    public void getRepositoryByIdTest() {
        // TODO tak: improve this tests
//        assertNotNull(m_globalReportRepository);
//        List<ReportRepository> repositoryList = m_globalReportRepository.getRepositoryList();
//        assertEquals(1, repositoryList.size());
//        for (ReportRepository repository : repositoryList) {
//            assertEquals(repository.getRepositoryId(), m_globalReportRepository.getRepositoryById(repository.getRepositoryId()).getRepositoryId());
//        }
//        assertFalse(m_globalReportRepository.getRepositoryById("") != null);
//
//        logger.debug(m_globalReportRepository.getDisplayName("local_sample-report"));
//        logger.debug("local repository : '{}'", m_globalReportRepository.getRepositoryById("cioreporting"));
//        ReportRepository localRepo = m_globalReportRepository.getRepositoryById("local");
//        logger.debug(localRepo.toString());
//        BasicReportDefinition report = localRepo.getOnlineReports().get(0);
//        logger.debug(report.toString());
//        logger.debug(report.getId());
//        report.setId("LOCAL_" + report.getId());
//        assertEquals("LOCAL_local_sample-report", report.getId());
//        assertFalse(("local_LOCAL_local_sample-report".equals(localRepo.getOnlineReports().get(0).getId())));
//        assertEquals("local_sample-report", localRepo.getOnlineReports().get(0).getId());
    }

    @Ignore
    @Test
    public void getRepositoryListTest() {
        // TODO tak: improve this tests
//        List<ReportRepository> repositoryList = m_globalReportRepository.getRepositoryList();
//        assertEquals(2, repositoryList.size());
//        assertEquals("local", repositoryList.get(0).getRepositoryId());
//        assertEquals("cioreporting", repositoryList.get(1).getRepositoryId());
    }

    public void setReportRepository(DefaultGlobalReportRepository globalReportRepository) {
        m_globalReportRepository = globalReportRepository;
    }
}