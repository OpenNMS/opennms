package org.opennms.netmgt.provision.detector;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

public class SSHDetectorTest{
	//Tested on a local server with SSH
    
    private SshDetector m_detector;
    
    @Before
    public void setUp() {
        m_detector = new SshDetector();
    }
    
	@Test
	public void testDetectorSuccess() throws UnknownHostException{
		//m_detector.init();
		//assertTrue(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
	}
	
	@Test
    public void testDetectorFailWrongPort() throws UnknownHostException{
	    //m_detector.setPort(30);
        //m_detector.init();
        //assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
    }
	
	@Test
    public void testDetectorFailBanner() throws UnknownHostException{
	    //m_detector.setBanner("Hello there crazy");
        //m_detector.init();
        //assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
    }
	
}