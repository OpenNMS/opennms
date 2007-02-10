package org.opennms.web.svclayer.support;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

public class DefaultRrdGraphServiceTest extends TestCase {
    private EasyMockUtils m_mockUtils;
    private FileAnticipator m_fileAnticipator;
    
    private DefaultRrdGraphService m_service;

    private ResourceDao m_resourceDao;

    private GraphDao m_graphDao;

    private RrdStrategy m_rrdStrategy;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_mockUtils = new EasyMockUtils();
        m_fileAnticipator = new FileAnticipator(false);
        m_service = new DefaultRrdGraphService();
    }
    
    @Override
    protected void tearDown() throws Exception {
        m_fileAnticipator.tearDown();
    }
    
    public void testAfterPropertiesSet() {
        setUpAll();
    }

    public void testNoResourceDao() throws Exception {
        setUpGraphDao();
        setUpRrdStrategy();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("resourceDao property has not been set"));
        
        m_mockUtils.replayAll();
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_mockUtils.verifyAll();
    }
    
    public void testNoGraphDao() {
        setUpResourceDao();
        setUpRrdStrategy();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("graphDao property has not been set"));
        
        m_mockUtils.replayAll();
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_mockUtils.verifyAll();
    }

    public void testNoRrdStrategy() {
        setUpResourceDao();
        setUpGraphDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("rrdStrategy property has not been set"));
        
        m_mockUtils.replayAll();
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        m_mockUtils.verifyAll();
    }
    
    public void testLoadPropertiesNullWorkDir() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("workDir argument cannot be null"));
        try {
            m_service.loadProperties(null, "foo");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testLoadPropertiesNullPropertiesFile() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("propertiesFile argument cannot be null"));
        try {
            m_service.loadProperties(new File(""), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testLoadPropertiesEmpty() throws Exception {
        m_fileAnticipator.initialize();
        m_fileAnticipator.tempFile("strings.properties", "");
        Properties p = m_service.loadProperties(m_fileAnticipator.getTempDir(), "strings.properties");
        assertNotNull("properties should not be null", p);
        assertEquals("properties size", 0, p.size());
    }
    
    public void testLoadPropertiesNonEmpty() throws Exception {
        m_fileAnticipator.initialize();
        m_fileAnticipator.tempFile("strings.properties", "foo=bar");
        Properties p = m_service.loadProperties(m_fileAnticipator.getTempDir(), "strings.properties");
        assertNotNull("properties should not be null", p);
        assertEquals("properties size", 1, p.size());
        assertNotNull("property 'foo' should exist", p.get("foo"));
        assertEquals("property 'foo' value", "bar", p.get("foo"));
    }

    public void testLoadPropertiesDoesNotExist() throws Exception {
        m_fileAnticipator.initialize();

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new ObjectRetrievalFailureException(Properties.class, "strings.properties", "This resource does not have a string properties file: " + new File(m_fileAnticipator.getTempDir(), "strings.properties").getAbsolutePath(), null));
        try {
            m_service.loadProperties(m_fileAnticipator.getTempDir(), "strings.properties");
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    private void setUpAll() {
        setUpResourceDao();
        setUpGraphDao();
        setUpRrdStrategy();
        m_service.afterPropertiesSet();
    }
    
    private void setUpResourceDao() {
        m_resourceDao = m_mockUtils.createMock(ResourceDao.class);
        m_service.setResourceDao(m_resourceDao);
    }
    
    private void setUpGraphDao() {
        m_graphDao = m_mockUtils.createMock(GraphDao.class);
        m_service.setGraphDao(m_graphDao);
    }
    
    private void setUpRrdStrategy() {
        m_rrdStrategy = m_mockUtils.createMock(RrdStrategy.class);
        m_service.setRrdStrategy(m_rrdStrategy);
    }
    
}
