package org.opennms.netmgt.provision.adapters.link.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.DefaultEndPointConfigurationDao;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ClassPathResource;

public class EndPointConfigurationTest {

    private EndPointConfigurationDao m_endPointDao;
    
    @Before
    public void setUp() throws Exception {
        DefaultEndPointConfigurationDao dao = new DefaultEndPointConfigurationDao();
        dao.setConfigResource(new ClassPathResource("/test-endpoint-configuration.xml"));
        dao.afterPropertiesSet();
        m_endPointDao = dao;

        Properties props = new Properties();
        props.setProperty("log4j.logger.org.springframework", "WARN");
        props.setProperty("log4j.logger.org.hibernate", "WARN");
        props.setProperty("log4j.logger.org.opennms", "DEBUG");
        props.setProperty("log4j.logger.org.opennms.netmgt.dao.castor", "WARN");
        MockLogAppender.setupLogging(props);
        
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);

    }
    
    @Test
    public void testGetFromDao() throws Exception {
        EndPointTypeValidator validator = m_endPointDao.getValidator();
        assertEquals("test config has 2 entries", 2, validator.getConfigs().size());
    }

    @Test
    public void testGetXsd() throws Exception {
        String xsd = m_endPointDao.getXsd();
        System.err.println(xsd);
        assertTrue(xsd.contains("endpoint-type"));
    }
}
