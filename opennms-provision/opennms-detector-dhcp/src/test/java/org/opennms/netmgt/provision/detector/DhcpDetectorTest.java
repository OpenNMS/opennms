package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.detector.dhcp.DhcpDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.netmgt.provision.support.dhcp.Dhcpd;
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
    private Thread m_dhcpdThread = null;
    
    @Before
    public void setup() throws Exception{
        setDhcpdThread((new Thread(getRunnable())));
        getDhcpdThread().start();
    }
    
    @After
    public void tearDown(){
        if(getDhcpdThread() != null){
            getDhcpdThread().stop();
        }
    }
    
	@Test
	public void testDetectorWired() {
	   assertNotNull(m_detector);
	}
	
	@Ignore
	@Test
	public void testDetectorSuccess() throws  IOException, MarshalException, ValidationException{
	    m_detector.setTimeout(5000);
	    m_detector.init();
	    assertTrue(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.1"), new NullDetectorMonitor()));
	    
	}
	
	protected static Runnable getRunnable() throws Exception {
        return new Runnable(){
            
            public void run(){
                Dhcpd dhcpdDeamon = Dhcpd.getInstance();
                try{
                    dhcpdDeamon.init();
                    dhcpdDeamon.start();
                    dhcpdDeamon.run();
                }catch(Exception e){
                    e.printStackTrace();
                } finally {
                    dhcpdDeamon.stop();
                }
            }
            
        };
    }

	public void setDhcpdThread(Thread dhcpdThread) {
        m_dhcpdThread = dhcpdThread;
    }

    public Thread getDhcpdThread() {
        return m_dhcpdThread;
    }
	
	
}