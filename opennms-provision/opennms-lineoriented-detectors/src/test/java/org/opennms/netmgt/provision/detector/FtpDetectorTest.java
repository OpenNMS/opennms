package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class FtpDetectorTest {

    private FtpDetector m_detector;
    private SimpleServer m_server;
    
    @Before
    public void setUp() {
       m_detector = new FtpDetector();
       m_detector.init();
       
    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void testMyServer() throws Exception {
        m_server = new SimpleServer() {
          public void onInit() {
             setBanner("667 Wrong Code Sent in for banner");
             addResponseHandler(matches("quit"), shutdownServer("690 Goodbye."));
          }
        };
        
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(),new NullDetectorMonitor())); 
    }
    
    @Test
    public void testDetectorSingleLineResponseSuccess() throws Exception {
        m_server = new SimpleServer() {
          public void onInit() {
             setBanner("220 ProFTPD 1.3.0 Server (ProFTPD)");
             addResponseHandler(matches("quit"), shutdownServer("221 Goodbye."));
          }
        };
        
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(),new NullDetectorMonitor())); 
    }
    
    @Test
    public void testDetectorMultilineSuccess() throws Exception {
        m_server = new SimpleServer() {
          public void onInit() {
             setBanner("220---------- Welcome to Pure-FTPd [TLS] ----------\r\n220-You are user number 1 of 50 allowed.\r\n220-Local time is now 07:47. Server port: 21.\r\n220 You will be disconnected after 15 minutes of inactivity.");
             addResponseHandler(matches("quit"), shutdownServer("221-Goodbye. You uploaded 0 and downloaded 0 kbytes.\r\n221 Logout."));
          }
        };
        
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(),new NullDetectorMonitor())); 
    }
    
    @Test
    public void testFailureClosedPort() throws Exception {
        m_server = new SimpleServer() {
            
            public void onInit() {
               setBanner("WRONG BANNER");
            }
            
        };
        m_server.init();
        m_detector.setPort(m_server.getLocalPort());
        
        assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    
    }
    
    @Test
    public void testFailureNoBannerSent() throws Exception {
       m_server = new SimpleServer();
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        assertFalse("Test should fail because the banner doesn't even get sent", m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    
    }
}
