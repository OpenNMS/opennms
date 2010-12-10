package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.mina.core.future.IoFutureListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.support.DefaultDetectFuture;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class AsyncDetectorFileDescriptorLeakTest implements ApplicationContextAware {
    
    private SimpleServer m_server;
    private AtomicReference<TcpDetector> m_detector = new AtomicReference<TcpDetector>();
    private ApplicationContext m_applicationContext;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_detector.set(getDetector(TcpDetector.class));
        m_detector.get().setServiceName("TCP");
        m_detector.get().setTimeout(1000);
        m_detector.get().setBanner(".*");
        m_detector.get().init();
    }
    
    @BeforeClass
    public static void beforeTest(){
        System.setProperty("org.opennms.netmgt.provision.maxConcurrentConnectors", "2000");
    }
    
    @After
    public void tearDown() throws IOException {
        if(m_server != null){
            m_server.stopServer();
            m_server = null;
        }
        
    }

    private void setUpServer() throws Exception {
        m_server = new SimpleServer() {
            
            public void onInit() {
               setBanner("Winner");
            }
            
        };
        
        m_server.init();
        m_server.startServer();
    }
    
    private void tearDownServer() throws IOException {
        m_server.stopServer();
        m_server = null;
    }

    @Test
    public void testSucessServer() throws Throwable {
        setUpServer();
        int port = m_server.getLocalPort();
        InetAddress address = m_server.getInetAddress();
        for (int i = 0; i < 10000; i++) {
            setUp();
            System.err.println("current loop: " + i);
            assertNotNull(m_detector);
            
            final TcpDetector detector = m_detector.get();
            
            detector.setPort(port);
            
            DefaultDetectFuture future = (DefaultDetectFuture)detector.isServiceDetected(address, new NullDetectorMonitor());
            future.addListener(new IoFutureListener<DetectFuture>() {
    
                public void operationComplete(DetectFuture future) {
                    detector.dispose();
                }
                
            });
            
            future.awaitUninterruptibly();
            assertNotNull(future);
            if (future.getException() != null) {
                System.err.println("got future exception: " + future.getException());
                throw future.getException();
            }
            System.err.println("got value: " + future.getObjectValue());
            assertTrue(future.isServiceDetected());

            m_detector.set(null);
        }
        tearDownServer();
    }
    
    @Test
    @Repeat(10000)
    public void testNoServerPresent() throws Exception {
        
        final TcpDetector detector = m_detector.get();
        detector.setPort(1999);
        
        DetectFuture future = detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        future.addListener(new IoFutureListener<DetectFuture>() {

            public void operationComplete(DetectFuture future) {
                detector.dispose();
            }
            
        });
        assertNotNull(future);
        future.awaitUninterruptibly();
        assertFalse(future.isServiceDetected());
        
        
        m_detector.set(null);
        System.err.println("Finish test");
    }
    
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
