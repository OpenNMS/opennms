package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.mina.core.future.IoFutureListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class TcpDetectorTest implements ApplicationContextAware {
    private SimpleServer m_server;
    private TcpDetector m_detector;
    private ApplicationContext m_applicationContext;
    
    @Before
    public void setUp() throws Exception {
        m_detector  = getDetector(TcpDetector.class);
        m_detector.setServiceName("TCP");
        m_detector.setTimeout(1000);
        m_detector.init();
    }
    
    @After
    public void tearDown() throws IOException {
        if(m_server != null){
            m_server.stopServer();
        }
        
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
    
    @Test
    //@Repeat(10000)
    public void testNoServerPresent() throws Exception {
            
        m_detector.setPort(1999);
        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        future.addListener(new IoFutureListener<DetectFuture>() {

            public void operationComplete(DetectFuture future) {
                TcpDetector detector = m_detector;
                m_detector = null;
                detector.dispose();
            }
            
        });
        assertNotNull(future);
        future.awaitUninterruptibly();
        assertFalse(future.isServiceDetected());
        
        
        
        System.err.println("Finish test");
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    private TcpDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (TcpDetector)bean;
    }
}
