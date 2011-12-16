package org.opennms.features.reporting.dao.remoterepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRemoteRepositoryConfigDaoTest {
    Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryConfigDaoTest.class.getSimpleName());
    //TODO Tak: create useful test...
    @Test
    public void unmarshallRemoteRepositoryConfigTest() {
        System.setProperty("opennms.home", "src/test/resources");
        RemoteRepositoryConfigDao m_dao = new DefaultRemoteRepositoryConfigDao();
        assertTrue(m_dao.isRepositoryActive());
        assertEquals("patrick", m_dao.getLoginUser());
        assertEquals("bateman", m_dao.getLoginRepoPassword());
        assertEquals("cioreporting", m_dao.getRepositoryId());
        assertEquals("OpenNMS Connect CIO-Reporting", m_dao.getRepositoryName());
        assertEquals("OpenNMS.com provides high value reports for CIO needs.", m_dao.getRepositoryDescription());
        assertEquals("http://www.opennms.com", m_dao.getRepositoryManagementURL());
        assertEquals(URI.create("http://localhost:8080/opennms/connect/rest/repo/"), m_dao.getURI());
    }
}
