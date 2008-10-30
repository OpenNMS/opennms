package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.server.SSLServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class HttpsDetectorTest {
    
    private HttpsDetector m_detector;
    private SSLServer m_server;
    
    private String serverOKResponse = "HTTP/1.1 200 OK\r\n"
        + "Date: Tue, 28 Oct 2008 20:47:55 GMT\r\n"
        + "Server: Apache/2.0.54\r\n"
        + "Last-Modified: Fri, 16 Jun 2006 01:52:14 GMT\r\n"
        + "ETag: \"778216aa-2f-aa66cf80\"\r\n"
        + "Accept-Ranges: bytes\r\n"
        + "Content-Length: 47\r\n"
        + "Vary: Accept-Encoding,User-Agent\r\n"
        + "Connection: close\rn"
        + "Content-Type: text/html\r\n"
        + "<html>\r\n"
        + "<body>\r\n"
        + "<!-- default -->\r\n"
        + "</body>\r\n"
        + "</html>";
    
    @Before
    public void setUp() throws Exception {
        System.out.println("Seting up");
        m_detector = new HttpsDetector();
        m_detector.init();
        
        m_server = getServer();
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
    }
    
    @After
    public void tearDown() throws IOException {
       System.out.println("Tear Down");
       m_server.stopServer();
       m_detector.closeDetector();
    }
    
    @Test
    public void testMyDetector() throws Exception {
        
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(),new NullDetectorMonitor()));
        
    }
    
    @Test
    public void testDetectorFailWrongPort() throws Exception {
        m_detector.setPort(2000);
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(),new NullDetectorMonitor()));
        
    }
    
    @Test
    public void testDetectorSuccess() throws Exception {
        
        assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(),new NullDetectorMonitor()));
        
    }
    
    private SSLServer getServer() {
        return new SSLServer() {
            
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(getServerOKResponse()));
            }
            
        };
    }

    public void setServerOKResponse(String serverOKResponse) {
        this.serverOKResponse = serverOKResponse;
    }

    public String getServerOKResponse() {
        return serverOKResponse;
    }
}
