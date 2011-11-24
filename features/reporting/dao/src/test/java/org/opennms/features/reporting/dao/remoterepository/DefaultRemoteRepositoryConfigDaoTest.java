package org.opennms.features.reporting.dao.remoterepository;

import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultRemoteRepositoryConfigDaoTest {

    @Test
    public void unmarshallRemoteRepositoryConfigTest() {
        System.setProperty("opennms.home", "/opt/opennms");
        RemoteRepositoryConfigDao m_dao = new DefaultRemoteRepositoryConfigDao();
        assertTrue(m_dao.isRepositoryActive());
        assertEquals("Patrick Bateman", m_dao.getLoginUser());
        assertEquals("hAvefUn", m_dao.getLoginRepoPassword());
        assertEquals("TEST_REST_REPO", m_dao.getRepositoryName());
        assertEquals("http://localhost:8080/opennms/connect/rest/repo/", m_dao.getURI());
        
    }
}
