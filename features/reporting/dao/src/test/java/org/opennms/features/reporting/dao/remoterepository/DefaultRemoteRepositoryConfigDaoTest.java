package org.opennms.features.reporting.dao.remoterepository;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.features.reporting.dao.remoterepository.DefaultRemoteRepositoryConfigDAO;

public class DefaultRemoteRepositoryConfigDaoTest {
    Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryConfigDaoTest.class.getSimpleName());
    private static final String OPENNMS_HOME = "src/test/resources";
    private DefaultRemoteRepositoryConfigDAO m_dao;
    private RemoteRepositoryDefinition m_remoteRepositoryDefinition;
    
    
    @BeforeClass
    public static void setup() {
        System.setProperty("opennms.home", OPENNMS_HOME);
        assertEquals(OPENNMS_HOME, System.getProperty("opennms.home"));
    }
    
    @Before
    public void init() {
        m_dao = new DefaultRemoteRepositoryConfigDAO();
        assertNotNull(m_dao);
        m_remoteRepositoryDefinition = m_dao.getRepositoryById("cioreporting");
        assertNotNull(m_remoteRepositoryDefinition);
    }
    
    @Test
    public void isRepositoryActiveTest() {
        assertTrue(m_remoteRepositoryDefinition.isRepositoryActive());
    }
    
    @Test
    public void getURITest() {
        assertEquals(URI.create("http://localhost:8080/opennms/connect/rest/repo/"), m_remoteRepositoryDefinition.getURI());        
    }
    
    @Test
    public void getLoginUserTest() {
        assertEquals("patrick", m_remoteRepositoryDefinition.getLoginUser());                
    }
    
    @Test
    public void getLoginRepoPasswordTest() {
        assertEquals("bateman", m_remoteRepositoryDefinition.getLoginRepoPassword());                
    }
    
    @Test
    public void getRepositoryNameTest() {
        assertEquals("OpenNMS Connect CIO-Reporting", m_remoteRepositoryDefinition.getRepositoryName());
    }
    
    @Test
    public void getRepositoryDescriptionTest() {
        assertEquals(43, m_remoteRepositoryDefinition.getRepositoryDescription().length());
    }
    
    @Test
    public void getRepositoryManagementURLTest() {
        assertEquals("http://www.opennms.com", m_remoteRepositoryDefinition.getRepositoryManagementURL());
    }
    
    @Test
    public void getAllRepositoriesTest() {
        List<RemoteRepositoryDefinition> allRepositoriesList = m_dao.getAllRepositories();
        assertEquals(2, allRepositoriesList.size());
        for (RemoteRepositoryDefinition repository : allRepositoriesList) {
            logger.debug("AllRepository: '{}'", repository.toString());
        }
    }
    
    @Test
    public void getActiveRepositoriesTest() {
        List<RemoteRepositoryDefinition> activeRepositoriesList = m_dao.getActiveRepositories();
        assertEquals(1, activeRepositoriesList.size());
        for (RemoteRepositoryDefinition repository : activeRepositoriesList) {
            logger.debug("ActiveRepository: '{}'", repository.toString());
        }
    }
    
    @Test
    public void getJasperReportsVersionTest() {
        assertEquals("4.2.3", m_dao.getJasperReportsVersion());
    }
}
