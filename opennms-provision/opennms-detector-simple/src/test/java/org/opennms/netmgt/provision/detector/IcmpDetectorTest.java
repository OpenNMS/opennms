package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.detector.icmp.IcmpDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({}) 
public class IcmpDetectorTest {
    
    private IcmpDetector m_icmpDetector;
    
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccess() throws Exception {
        m_icmpDetector = new IcmpDetector();
        assertTrue(m_icmpDetector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor()));
    }
    
    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorFail() throws Exception {
        m_icmpDetector = new IcmpDetector();
        assertFalse(m_icmpDetector.isServiceDetected(InetAddress.getByName("0.0.0.0"), new NullDetectorMonitor()));
    }
}
