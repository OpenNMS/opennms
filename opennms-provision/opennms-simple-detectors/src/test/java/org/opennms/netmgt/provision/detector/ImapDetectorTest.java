package org.opennms.netmgt.provision.detector;

import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ImapDetectorTest {
    private AbstractDetector m_detector;
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private static int TIMEOUT = 2000; 
    
    
    @Before
    public void setUp() throws Exception{
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null);
        
        m_detector = new ImapDetector();
        m_detector.setServiceName("Imap");
        m_detector.setPort(m_serverSocket.getLocalPort());
        m_detector.setTimeout(1000);
        m_detector.init();
    }
    
    @After
    public void tearDown() throws Exception{
        if (m_serverSocket != null && !m_serverSocket.isClosed()) {
            m_serverSocket.close();
        }
        
        if (m_serverThread != null) {
            m_serverThread.join(1500);
        }
    }
    
    @Test
    public void testSuccess(){
        
    }
    
}
