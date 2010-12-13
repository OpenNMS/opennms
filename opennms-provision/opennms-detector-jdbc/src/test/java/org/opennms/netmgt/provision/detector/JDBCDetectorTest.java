package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.provision.detector.jdbc.JdbcDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:/META-INF/opennms/detectors.xml", 
                                   "classpath:/META-INF/opennms/applicationContext-dao.xml",
                                   "classpath*:/META-INF/opennms/component-dao.xml"})
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@JUnitTemporaryDatabase
public class JDBCDetectorTest{
    
    @Autowired
    public JdbcDetector m_detector;
    
    @Autowired
    DataSource m_dataSource;
    
    @Before
    public void setUp() throws UnknownHostException {
        MockLogAppender.setupLogging();

        assertNotNull(m_dataSource);
        String url = null;
        String username = null;
        try {
            Connection conn = m_dataSource.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            url = metaData.getURL();
            username = metaData.getUserName();
            conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        
        m_detector.setDbDriver("org.postgresql.Driver");
        m_detector.setPort(5432);
        m_detector.setUrl(url);
        m_detector.setUser(username);
        m_detector.setPassword("");
        
        
        
    }
    
	@Test
	public void testDetectorSuccess() throws UnknownHostException{
		
		m_detector.init();
		
		assertTrue("Service wasn't detected", m_detector.isServiceDetected(InetAddress.getByName("127.0.0.1"), new NullDetectorMonitor()));
	}
	
	@Test
    public void testDetectorFailWrongUser() throws UnknownHostException{
	    m_detector.setUser("wrongUser");
        m_detector.init();
        
        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("127.0.0.1"), new NullDetectorMonitor()));
    }
	
	@Test
    public void testDetectorFailWrongUrl() throws UnknownHostException{
        m_detector.setUrl("jdbc:postgres://bogus:5432/blank");
        m_detector.init();
        
        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("127.0.0.1"), new NullDetectorMonitor()));
    }
	
}