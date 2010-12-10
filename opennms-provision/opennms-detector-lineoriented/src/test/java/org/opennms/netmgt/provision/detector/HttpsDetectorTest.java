package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.HttpsDetector;
import org.opennms.netmgt.provision.server.SSLServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class HttpsDetectorTest implements ApplicationContextAware{
    
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
        + "\r\n"
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
                    + "Content-Length: 52\r\n"
                    + "Vary: Accept-Encoding,User-Agent\r\n"
                    + "Connection: close\rn"
                    + "Content-Type: text/html\r\n"
                    + "\r\n"
                    + "<html>\r\n"
                    + "<body>\r\n"
                    + "<!-- default -->\r\n"
                    + "</body>\r\n"
                    + "</html>";
    
    private String notAServerResponse = "NOT A SERVER";
    private ApplicationContext m_applicationContext;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_detector = getDetector(HttpsDetector.class);
        m_detector.setRetries(0);
    }
    
    @After
    public void tearDown() throws IOException {
       if(m_server != null) {
           m_server.stopServer();
           m_server = null;
       }
    }
    
    @Test
    public void testDetectorFailWrongPort() throws Exception {
        m_detector.setPort(2000);
        m_server = createServer(serverOKResponse);
        
        assertFalse(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    }
    
    @Test
    public void testDetectorFailNotAServerResponse() throws Exception {
        m_detector.init();
        m_server = createServer(notAServerResponse);
        m_detector.setPort(m_server.getLocalPort());
        
       assertFalse(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    }
    
    @Test
    public void testDetectorFailNotFoundResponseMaxRetCode399() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(301);
        m_detector.init();
        
        m_server = createServer(notFoundResponse);
        m_detector.setPort(m_server.getLocalPort());
        
       assertFalse(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    }
    
    @Test
    public void testDetectorSucessMaxRetCode399() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(399);
        
        
        m_server = createServer(serverOKResponse);
        m_detector.setPort(m_server.getLocalPort());
        m_detector.init();
       assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    }
    
    @Test
    public void testDetectorFailMaxRetCodeBelow200() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(199);
        m_detector.init();
        
        m_server = createServer(getServerOKResponse());
        m_detector.setPort(m_server.getLocalPort());
        
       assertFalse(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    }
    
    @Test
    public void testDetectorMaxRetCode600() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setMaxRetCode(600);
        m_detector.init();
        
        m_server = createServer(getServerOKResponse());
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    }
    
    
    @Test
    public void testDetectorSucessCheckCodeTrue() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("http://localhost/");
        m_detector.init();
        m_detector.setIdleTime(1000);
        m_server = createServer(getServerOKResponse());
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    }
    
    @Test
    public void testDetectorSuccessCheckCodeFalse() throws Exception {
        m_detector.setCheckRetCode(false);
        m_detector.init();
        
        m_server = createServer(getServerOKResponse());
        m_detector.setPort(m_server.getLocalPort());
        
       assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));
    }

    @Test
    public void testDetectorSuccess() throws Exception {
        m_server = createServer(serverOKResponse);
        
        m_detector.setPort(m_server.getLocalPort());
        m_detector.init();
        assertTrue(doCheck(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor())));        
    }
    
    
    /**
     * @param serviceDetected
     * @return
     * @throws InterruptedException 
     */
    private boolean doCheck(DetectFuture serviceDetected) throws InterruptedException {
        DetectFuture future = serviceDetected;
        future.await();
        
        return future.isServiceDetected();
    }
    
    private SSLServer createServer(final String httpResponse) throws Exception {
        SSLServer server = new SSLServer() {
            
            public void onInit() {
                addResponseHandler(contains("GET"), shutdownServer(httpResponse));
            }
        };
        server.init();
        server.startServer();
        
        return server;
    }
    
    public void setServerOKResponse(String serverOKResponse) {
        this.serverOKResponse = serverOKResponse;
    }

    public String getServerOKResponse() {
        return serverOKResponse;
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
        
    }
    
    private HttpsDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (HttpsDetector)bean;
    }
}
