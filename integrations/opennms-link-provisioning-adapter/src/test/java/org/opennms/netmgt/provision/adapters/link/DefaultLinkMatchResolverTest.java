package org.opennms.netmgt.provision.adapters.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/testConfigContext.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultLinkMatchResolverTest {
    @Autowired
    private DefaultLinkMatchResolverImpl m_resolver;

    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.springframework", "WARN");
        props.setProperty("log4j.logger.org.hibernate", "WARN");
        props.setProperty("log4j.logger.org.opennms", "DEBUG");
        props.setProperty("log4j.logger.org.opennms.netmgt.dao.castor", "WARN");
        MockLogAppender.setupLogging(props);
    }
    
    @Test
    public void testSimpleMatch() {
        assertEquals("nc-ral0002-to-ral0001-dwave", m_resolver.getAssociatedEndPoint("nc-ral0001-to-ral0002-dwave"));
    }
    
    @Test
    public void testMultiplePatterns() {
        assertEquals("middle-was-bar", m_resolver.getAssociatedEndPoint("foo-bar-baz"));
        assertEquals("middle-was-now", m_resolver.getAssociatedEndPoint("before-now-after"));
        assertNull(m_resolver.getAssociatedEndPoint("after-wasn't-before"));
    }
    
    @Test
    public void testPatternsFromConfig() {
        assertEquals("middle-was-bar", m_resolver.getAssociatedEndPoint("foo-bar-baz"));
        assertEquals("middle-was-now", m_resolver.getAssociatedEndPoint("before-now-after"));
        assertNull(m_resolver.getAssociatedEndPoint("after-wasn't-before"));
    }
}
