package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.detector.radius.RadiusAuthDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class RadiusAuthDetectorTest implements ApplicationContextAware{
    
    @Autowired
    public RadiusAuthDetector m_detector;

    @Before
    public void setUp(){
         MockLogAppender.setupLogging();
    }
    
	@Test
	public void testDetectorFail() throws UnknownHostException{
	    m_detector.onInit();
	    m_detector.setTimeout(1);
		assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.100"), new NullDetectorMonitor()));
	}

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        
    }
	
}