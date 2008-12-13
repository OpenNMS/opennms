package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class FtpDetectorTest {
    
    private FtpDetector m_detector;
    private SimpleServer m_server;
    
    @Before
    public void setUp() throws Exception {
       m_detector = new FtpDetector();
       m_detector.init();
       
       m_server = new SimpleServer() {
           
           public void onInit() {
               setBanner("220 ProFTPD 1.3.0 Server (ProFTPD)");
               addResponseHandler(matches("quit"), shutdownServer("221 Goodbye."));
           }
       };
       
       m_server.init();
       m_server.startServer();
       
    }
    
    @After
    public void tearDown() throws IOException {
        m_server.stopServer();
        m_server = null;
    }
    
    
    @Test
    public void testDetectorSingleLineResponseSuccess() throws Exception {
        
        m_server.setBanner("220 ProFTPD 1.3.0 Server (ProFTPD)");
        m_detector.setPort(m_server.getLocalPort());
        m_detector.setIdleTime(100);
       assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(),new NullDetectorMonitor()))); 
    }
    
    @Test
    public void testDetectorMultilineSuccess() throws Exception {
       
        m_server.setBanner("220---------- Welcome to Pure-FTPd [TLS] ----------\r\n220-You are user number 1 of 50 allowed.\r\n220-Local time is now 07:47. Server port: 21.\r\n220 You will be disconnected after 15 minutes of inactivity.");
        m_detector.setPort(m_server.getLocalPort());

       assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(),new NullDetectorMonitor()))); 
    }
    
    @Test
    public void testFailureClosedPort() throws Exception {
        
        m_server.setBanner("WRONG BANNER");
        m_detector.setPort(m_server.getLocalPort());
        
        assertFalse("Test should fail because the server closes before detection takes place", doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    
    }
    
    @Test
    public void testFailureNoBannerSent() throws Exception {
       m_server = new SimpleServer();
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        assertFalse("Test should fail because the banner doesn't even get sent", doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    
    }
    
    private boolean doCheck(DetectFuture future) throws InterruptedException {
        
        future.await();
        
        return future.isServiceDetected();
    }
}
