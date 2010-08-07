package org.opennms.netmgt.dao.jaxb;

import java.io.InputStream;

import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.JAXBDataAccessFailureException;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import junit.framework.TestCase;

public class AbstractJaxbConfigDaoTest extends TestCase {

    public void testAfterPropertiesSetWithNoConfigSet() {
        TestJaxbConfigDao dao = new TestJaxbConfigDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property configResource must be set and be non-null"));
        
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetWithBogusFileResource() throws Exception {
        Resource resource = new FileSystemResource("/bogus-file");
        TestJaxbConfigDao dao = new TestJaxbConfigDao();
        dao.setConfigResource(resource);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JAXBDataAccessFailureException(ThrowableAnticipator.IGNORE_MESSAGE));
        
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetWithGoodConfigFile() throws Exception {
        TestJaxbConfigDao dao = new TestJaxbConfigDao();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("jdbc-datacollection-config.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        
        assertNotNull("jdbc data collection should not be null", dao.getDataCollectionConfig());
    }
    
    
    public static class TestJaxbConfigDao extends AbstractJaxbConfigDao<JdbcDataCollectionConfig, JdbcDataCollectionConfig> {
        public TestJaxbConfigDao() {
            super(JdbcDataCollectionConfig.class, "jdbc data collection configuration");
        }
        
        @Override
        public JdbcDataCollectionConfig translateConfig(JdbcDataCollectionConfig jaxbConfig) {
            return jaxbConfig;
        }
        
        public JdbcDataCollectionConfig getDataCollectionConfig() {
            return getContainer().getObject();
        }
    }
}
