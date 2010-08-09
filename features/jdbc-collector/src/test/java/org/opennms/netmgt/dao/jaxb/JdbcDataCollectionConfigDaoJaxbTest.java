package org.opennms.netmgt.dao.jaxb;

import java.io.InputStream;

import org.opennms.netmgt.dao.JAXBDataAccessFailureException;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import junit.framework.TestCase;

public class JdbcDataCollectionConfigDaoJaxbTest extends TestCase {
    
    public void testAfterPropertiesSetWithNoConfigSet() {
        JdbcDataCollectionConfigDaoJaxb dao = new JdbcDataCollectionConfigDaoJaxb();
        
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
        JdbcDataCollectionConfigDaoJaxb dao = new JdbcDataCollectionConfigDaoJaxb();
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
        JdbcDataCollectionConfigDaoJaxb dao = new JdbcDataCollectionConfigDaoJaxb();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("jdbc-datacollection-config.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        
        assertNotNull("jdbc data collection should not be null", dao.getConfig());
    }

}
