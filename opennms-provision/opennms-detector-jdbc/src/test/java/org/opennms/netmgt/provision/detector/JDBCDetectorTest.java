package org.opennms.netmgt.provision.detector;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

public class JDBCDetectorTest{
	
    //Tested detector on a MySQL database on local network.
    
//    private JDBCDetector m_detector;
    
    @Before
    public void setUp() {
//      m_detector = new JDBCDetector();
    }
    
	@Test
	public void testDetectorSuccess() throws UnknownHostException{
//		m_detector.setDbDriver("com.mysql.jdbc.Driver");
//		m_detector.setUrl("jdbc:mysql://192.168.1.103:3306/test");
//		m_detector.setUser("root");
//		m_detector.setPassword("password1!");
//		m_detector.init();
//		
//		assertTrue(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
	}
	
//	@Test
//    public void testDetectorFailWrongUser() throws UnknownHostException{
//        m_detector.setDbDriver("com.mysql.jdbc.Driver");
//        m_detector.setUrl("jdbc:mysql://192.168.1.103:3306/test");
//        m_detector.setUser("roo");
//        m_detector.setPassword("password1!");
//        m_detector.init();
//        
//        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
//    }
//	
//	@Test
//    public void testDetectorFailWrongPassword() throws UnknownHostException{
//        m_detector.setDbDriver("com.mysql.jdbc.Driver");
//        m_detector.setUrl("jdbc:mysql://192.168.1.103:3306/test");
//        m_detector.setUser("root");
//        m_detector.setPassword("pass");
//        m_detector.init();
//        
//        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
//    }
//	
//	@Test
//    public void testDetectorFailWrongUrl() throws UnknownHostException{
//        m_detector.setDbDriver("com.mysql.jdbc.Driver");
//        m_detector.setUrl("jdbc:mysql://localhost:3306/test");
//        m_detector.setUser("root");
//        m_detector.setPassword("password");
//        m_detector.init();
//        
//        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
//    }
	
}