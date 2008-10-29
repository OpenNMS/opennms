package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class HttpDetectorTest {
    
    private HttpDetector m_detector;
    private SimpleServer m_server;
    
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
    
    private String notFoundResponse = "HTTP/1.1 404 Not Found\r\n"
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
    
    private String notAServerResponse = "NOT A SERVER";
    

    
    @Before
    public void setUp() {
        m_detector = new HttpDetector();        
    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void testDetectorFailNotAServerResponse() throws Exception {
        m_detector.init();
        
        m_server = new SimpleServer() {
          
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(notAServerResponse));
            }
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailNotFoundResponseMaxRetCode399() throws Exception {
        m_detector.isCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(399);
        m_detector.init();
        
        m_server = new SimpleServer() {
          
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(notFoundResponse));
            }
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorSucessMaxRetCode399() throws Exception {
        m_detector.isCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(399);
        m_detector.init();
        
        m_server = new SimpleServer() {
          
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(serverOKResponse));
            }
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailMaxRetCodeBelow200() throws Exception {
        m_detector.isCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(199);
        m_detector.init();
        
        m_server = new SimpleServer() {
          
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(serverOKResponse));
            }
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorMaxRetCode600() throws Exception {
        m_detector.isCheckRetCode(true);
        m_detector.setMaxRetCode(600);
        m_detector.init();
        
        m_server = new SimpleServer() {
          
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(serverOKResponse));
            }
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorSucessCheckCodeTrue() throws Exception {
        m_detector.isCheckRetCode(true);
        m_detector.init();
        
        m_server = new SimpleServer() {
          
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(serverOKResponse));
            }
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorSuccessCheckCodeFalse() throws Exception {
        m_detector.isCheckRetCode(false);
        m_detector.init();
        m_server = new SimpleServer() {
          
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(serverOKResponse));
            }
        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
}
