package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class TcpDetectorTest {
    private SimpleServer m_server;
    private TcpDetector m_detector;
    
    @Before
    public void setUp() throws Exception {
        m_detector  = new TcpDetector();
        m_detector.setServiceName("TCP");
        m_detector.setTimeout(1000);
        m_detector.init();
    }
    
    @After
    public void tearDown() throws IOException {
        m_server.stopServer();
    }
    
    @Test
    public void testSucessServer() throws Exception {
        m_server = new SimpleServer() {
            
            public void onInit() {
               setBanner("Winner");
            }
            
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
        //assertTrue("Test should pass, TcpDetector checks for all wildcard banners", m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
        
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor());
        
        future.awaitUninterruptibly();
        assertNotNull(future);
        assertTrue(future.isServiceDetected());
    }
    
    @Test
    public void testFailureNoBannerSent() throws Exception {
       m_server = new SimpleServer() {
            
            public void onInit() {
               
            }
            
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor());
        assertNotNull(future);
        future.awaitUninterruptibly();
        assertFalse(future.isServiceDetected());
    
    }
    
    @Test
    public void testFailureClosedPort() throws Exception {
        m_server = new SimpleServer() {
            
            public void onInit() {
               setBanner("BLIP");
            }
            
        };
        m_server.init();
        m_detector.setPort(m_server.getLocalPort());
        
        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
        
        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor());
        assertNotNull(future);
        future.awaitUninterruptibly();
        assertFalse(future.isServiceDetected());
    
    }
}
