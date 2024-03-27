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
package org.opennms.features.reporting.repository.global;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
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
        m_mockRemoteRepositoryConfigDao = mock(RemoteRepositoryConfigDao.class);

        // Mockup for a list of reports
        m_mockReportList = new ArrayList<>();

        // Mockup for a online active report
        m_mockReportDefinition1 = mock(BasicReportDefinition.class);
        when(m_mockReportDefinition1.getAllowAccess()).thenReturn(Boolean.TRUE);
        when(m_mockReportDefinition1.getDescription()).thenReturn("MockReportDescription1");
        when(m_mockReportDefinition1.getDisplayName()).thenReturn("MockupReportDisplayName1");
        when(m_mockReportDefinition1.getOnline()).thenReturn(Boolean.TRUE);
        when(m_mockReportDefinition1.getReportService()).thenReturn("MockupReportService1");
        when(m_mockReportDefinition1.getRepositoryId()).thenReturn("local_MockupReportReportId1");
        when(m_mockReportDefinition1.getId()).thenReturn("local_MockupReportId1");

        // Mockup for an inactive not online report
        m_mockReportDefinition2 = mock(BasicReportDefinition.class);
        when(m_mockReportDefinition2.getAllowAccess()).thenReturn(Boolean.FALSE);
        when(m_mockReportDefinition2.getDescription()).thenReturn("MockReportDescription2");
        when(m_mockReportDefinition2.getDisplayName()).thenReturn("MockupReportDisplayName2");
        when(m_mockReportDefinition2.getOnline()).thenReturn(Boolean.FALSE);
        when(m_mockReportDefinition2.getReportService()).thenReturn("MockupReportService2");
        when(m_mockReportDefinition2.getRepositoryId()).thenReturn("local_MockupReportReportId2");
        when(m_mockReportDefinition2.getId()).thenReturn("MockupReportId2");

        // Add mockup report to the report list 
        m_mockReportList.add(m_mockReportDefinition1);
        m_mockReportList.add(m_mockReportDefinition2);

        // Mockup a local report repository 
        m_mockLocalReportRepository = mock(ReportRepository.class);
        when(m_mockLocalReportRepository.getOnlineReports()).thenReturn(m_mockReportList);
        when(m_mockLocalReportRepository.getReports()).thenReturn(m_mockReportList);
        when(m_mockLocalReportRepository.getDisplayName("MockedReportId")).thenReturn("MockedDisplayName");
        when(m_mockLocalReportRepository.getEngine("MockupReportId")).thenReturn("MockedEngine");
        when(m_mockLocalReportRepository.getManagementUrl()).thenReturn("MockupManagementUrl");
        when(m_mockLocalReportRepository.getRepositoryDescription()).thenReturn("MockedRepositoryDescription");
        when(m_mockLocalReportRepository.getReportService("MockedReportId")).thenReturn("MockedReportService");
        when(m_mockLocalReportRepository.getRepositoryId()).thenReturn("local");
        when(m_mockLocalReportRepository.getRepositoryName()).thenReturn("MockedRepositoryName");

        // Mockup for one remote repository
        m_remoteReportRepository = mock(ReportRepository.class);
        when(m_remoteReportRepository.getOnlineReports()).thenReturn(m_mockReportList);
        when(m_remoteReportRepository.getReports()).thenReturn(m_mockReportList);
        when(m_remoteReportRepository.getDisplayName("MockedReportId")).thenReturn("MockedRemoteDisplayName");
        when(m_remoteReportRepository.getEngine("MockupReportId")).thenReturn("MockedRemoteEngine");
        when(m_remoteReportRepository.getManagementUrl()).thenReturn("MockupRemoteManagementUrl");
        when(m_remoteReportRepository.getRepositoryDescription()).thenReturn("MockedRemoteRepositoryDescription");
        when(m_remoteReportRepository.getReportService("MockedReportId")).thenReturn("MockedTemoteReportService");
        when(m_remoteReportRepository.getRepositoryId()).thenReturn("MockedRemoteRepositoryId");
        when(m_remoteReportRepository.getRepositoryName()).thenReturn("MockedRemoteRepositoryName");

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

        when(m_mockRemoteRepositoryConfigDao.getActiveRepositories()).thenReturn(m_mockActiveRepositoriesList);
        when(m_mockRemoteRepositoryConfigDao.getAllRepositories()).thenReturn(m_mockAllRepositoriesList);
        when(m_mockRemoteRepositoryConfigDao.getLoginRepoPassword("repositoryId")).thenReturn("MockedDaoLoginPassword");
        when(m_mockRemoteRepositoryConfigDao.getLoginUser("repositoryId")).thenReturn("MockedDaoLoginUser");
        when(m_mockRemoteRepositoryConfigDao.getRepositoryById("repositoryId")).thenReturn(m_mockActiveRemoteRepository);
        when(m_mockRemoteRepositoryConfigDao.getRepositoryDescription("repositoryId")).thenReturn("MockedDaoRepositoryDescription");
        when(m_mockRemoteRepositoryConfigDao.getRepositoryManagementURL("repositoryId")).thenReturn("MockedDaoManagementUrl");
        when(m_mockRemoteRepositoryConfigDao.getRepositoryName("repositoryId")).thenReturn("MockedDaoRepositoryName");

        m_globalReportRepository = new DefaultGlobalReportRepository(m_mockRemoteRepositoryConfigDao, m_mockLocalReportRepository);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_mockRemoteRepositoryConfigDao);
        verifyNoMoreInteractions(m_mockReportDefinition1);
        verifyNoMoreInteractions(m_mockReportDefinition2);
        verifyNoMoreInteractions(m_mockLocalReportRepository);
        verifyNoMoreInteractions(m_remoteReportRepository);
    }

    @Test
    public void getAllOnlineReportsTest() {
        assertEquals("Test size of online reports", 2, m_globalReportRepository.getAllOnlineReports().size());
        verify(m_mockRemoteRepositoryConfigDao, times(1)).getActiveRepositories();
        verify(m_mockLocalReportRepository, times(1)).getOnlineReports();
    }

    @Test
    public void getAllReportsTest() {
        assertEquals("Test size of online reports", 2, m_globalReportRepository.getAllReports().size());
        verify(m_mockRemoteRepositoryConfigDao, times(1)).getActiveRepositories();
        verify(m_mockLocalReportRepository, times(1)).getReports();
    }

    public void setReportRepository(DefaultGlobalReportRepository globalReportRepository) {
        m_globalReportRepository = globalReportRepository;
    }
}
