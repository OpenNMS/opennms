package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.detector.dhcp.DhcpDetector;
import org.opennms.netmgt.provision.support.dhcp.DhcpdConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class DhcpDetectorTest{
	
    @Autowired
    public DhcpDetector m_detector;
    
    public DhcpdConfigFactory m_factory;
    
    
	@Test
	public void testDetectorWired() {
	   assertNotNull(m_detector);
	}
	
	
}