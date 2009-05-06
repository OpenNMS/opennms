package org.opennms.netmgt.provision.detector;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.detector.ssh.SshDetector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class SSHDetectorTest implements ApplicationContextAware{
	//Tested on a local server with SSH
    
    @Autowired
    public SshDetector m_detector;
    
    @Before
    public void setUp() {
        m_detector = new SshDetector();
        m_detector.setTimeout(1);
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

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        // TODO Auto-generated method stub
        
    }
	
}