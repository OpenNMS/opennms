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
package org.opennms.features.reporting.dao.remoterepository;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <p>DefaultRemoteRepositoryConfigDaoTest class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.8.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-reportingDaoTest.xml"})
public class DefaultRemoteRepositoryConfigDaoTest implements InitializingBean {

    /**
     * Default implementation for remote repository to test
     */
    @Autowired
    private RemoteRepositoryConfigDao m_remoteRepositoryConfigDao;

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
        m_remoteRepositoryConfigDao = null;
    }

    /**
     * <p>testIsRepositoryActive</p>
     * <p/>
     * Test to read all as active configured repositories
     *
     * @throws Exception
     */
    @Test
    public void testIsRepositoryActive() throws Exception {
        assertEquals("Test 1 active repository configured", 1, m_remoteRepositoryConfigDao.getActiveRepositories().size());
    }

    /**
     * <p>testGetURI</p>
     * <p/>
     * Test to get repository URI for all configured remote repositories
     *
     * @throws Exception
     */
    @Test
    public void testGetURI() throws Exception {
        assertEquals("Get URI for configured CIO remote repository", new URI("http://localhost:8080/opennms/connect/rest/repo/"), m_remoteRepositoryConfigDao.getAllRepositories().get(0).getURI());
        assertEquals("Get URI for configured CONNECT remote repository", new URI("http://localhost:8080/opennms/connect/rest/repo/connect"), m_remoteRepositoryConfigDao.getAllRepositories().get(1).getURI());
    }

    /**
     * <p>testGetLoginUser</p>
     * <p/>
     * Test to get user names for all configured remote repositories
     *
     * @throws Exception
     */
    @Test
    public void testGetLoginUser() throws Exception {
        assertEquals("Get user name for CIO remote repository", "patrick", m_remoteRepositoryConfigDao.getAllRepositories().get(0).getLoginUser());
        assertEquals("Get user name for CONNECT remote repository", "ethan", m_remoteRepositoryConfigDao.getAllRepositories().get(1).getLoginUser());
    }

    /**
     * <p>testGetLoginRepoPassword</p>
     * <p/>
     * Test to get passwords for all configured remote repositories
     *
     * @throws Exception
     */
    @Test
    public void testGetLoginRepoPassword() throws Exception {
        assertEquals("Get password for CIO remote repository", "bateman", m_remoteRepositoryConfigDao.getAllRepositories().get(0).getLoginRepoPassword());
        assertEquals("Get password for CONNECT remote repository", "galstad", m_remoteRepositoryConfigDao.getAllRepositories().get(1).getLoginRepoPassword());
    }

    /**
     * <p>testGetRepositoryName</p>
     * <p/>
     * Test to get all names for all configured remote repositories
     *
     * @throws Exception
     */
    @Test
    public void testGetRepositoryName() throws Exception {
        assertEquals("Get CIO remote repository name", "OpenNMS CIO-Reporting", m_remoteRepositoryConfigDao.getAllRepositories().get(0).getRepositoryName());
        assertEquals("Get CONNECT remote repository name", "OpenNMS Connect Reports", m_remoteRepositoryConfigDao.getAllRepositories().get(1).getRepositoryName());
    }

    /**
     * <p>testGetRepositoryDescription</p>
     * <p/>
     * Test to get all descriptions for all configured remote repositories
     *
     * @throws Exception
     */
    @Test
    public void testGetRepositoryDescription() throws Exception {
        assertEquals("Get CIO remote repository name", "OpenNMS.com provides high value reports.", m_remoteRepositoryConfigDao.getAllRepositories().get(0).getRepositoryDescription());
        assertEquals("Get CONNECT remote repository name", "OpenNMS Community provides free reports.", m_remoteRepositoryConfigDao.getAllRepositories().get(1).getRepositoryDescription());
    }

    /**
     * <p>testGetRepositoryManagementURL</p>
     * <p/>
     * Test to get all management URLs for all configured remote repositories
     *
     * @throws Exception
     */
    @Test
    public void testGetRepositoryManagementURL() throws Exception {
        assertEquals("Get CIO remote repository management URL", "http://www.opennms.com", m_remoteRepositoryConfigDao.getAllRepositories().get(0).getRepositoryManagementURL());
        assertEquals("Get CONNECT remote repository management URL", "http://www.opennms.org", m_remoteRepositoryConfigDao.getAllRepositories().get(1).getRepositoryManagementURL());
    }

    /**
     * <p>testGetRepositoryById</p>
     * <p/>
     * Test to get a repository by ID
     *
     * @throws Exception
     */
    @Test
    public void testGetRepositoryById() throws Exception {
        assertEquals("Get CIO remote repository by ID", "cioreporting", m_remoteRepositoryConfigDao.getRepositoryById("cioreporting").getRepositoryId());
        assertEquals("Get CONNECT remote repository by ID", "connectreporting", m_remoteRepositoryConfigDao.getRepositoryById("connectreporting").getRepositoryId());
    }
}
