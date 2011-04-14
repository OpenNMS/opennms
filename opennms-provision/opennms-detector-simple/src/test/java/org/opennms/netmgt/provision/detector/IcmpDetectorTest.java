package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.jicmp.JnaPinger;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.ping.JniPinger;
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
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }
    
    @After
    public void tearDown() {
        
    }

    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccessJni() throws Exception {
        PingerFactory.setInstance(new JniPinger());
        m_icmpDetector = new IcmpDetector();
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor()));
    }

    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorFailJni() throws Exception {
        PingerFactory.setInstance(new JniPinger());
        m_icmpDetector = new IcmpDetector();
        assertFalse("ICMP was incorrectly identified on " + InetAddressUtils.UNPINGABLE_ADDRESS.getHostAddress(), m_icmpDetector.isServiceDetected(InetAddressUtils.UNPINGABLE_ADDRESS, new NullDetectorMonitor()));
    }

    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorSuccess() throws Exception {
        PingerFactory.setInstance(new JnaPinger());
        m_icmpDetector = new IcmpDetector();
        assertTrue("ICMP could not be detected on localhost", m_icmpDetector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor()));
    }

    @Test
    @IfProfileValue(name="runPingTests", value="true")
    public void testDetectorFail() throws Exception {
        PingerFactory.setInstance(new JnaPinger());
        m_icmpDetector = new IcmpDetector();
        assertFalse("ICMP was incorrectly identified on " + InetAddressUtils.UNPINGABLE_ADDRESS.getHostAddress(), m_icmpDetector.isServiceDetected(InetAddressUtils.UNPINGABLE_ADDRESS, new NullDetectorMonitor()));
    }
}
