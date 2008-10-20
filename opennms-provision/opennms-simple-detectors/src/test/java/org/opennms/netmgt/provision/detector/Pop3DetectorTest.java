package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class Pop3DetectorTest {
    private AbstractDetector m_detector;
    private ServerSocket m_serverSocket = null;
    private Thread m_serverThread = null;
    private static int TIMEOUT = 2000;
    
    @Before
    public void setUp() throws Exception {
                
        m_serverSocket = new ServerSocket();
        m_serverSocket.bind(null); // don't care what address, just gimme a port

        m_detector = new Pop3Detector();
        m_detector.setServiceName("POP3");
        m_detector.setPort(m_serverSocket.getLocalPort());
        m_detector.setTimeout(1000);
        m_detector.init();
    }

    @After
    public void tearDown() throws Exception {
        if (m_serverSocket != null && !m_serverSocket.isClosed()) {
            m_serverSocket.close();
        }
        
        if (m_serverThread != null) {
            m_serverThread.join(1500);
        }
        
    }
    
    @Test
    public void testSuccess() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    OutputStream out = s.getOutputStream();
                    out.write("+OK\r\n".getBytes());
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String command = r.readLine();
                    System.out.println(command);
                    if (command != null && command.equals("QUIT")) {
                        out.write("+OK\r\n".getBytes());
                    }
                    s.close();
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        assertTrue("Test for protocol Pop3 should have passed", doCheck());
    }
    
    @Test
    public void testFailureWithBogusResponse() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    Socket s = m_serverSocket.accept();
                    s.getOutputStream().write("Go away!".getBytes());
                    s.close();
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        assertFalse("Test for protocol FTP should have failed", doCheck());
    }
    
    @Test
    public void testMonitorFailureWithNoResponse() throws Exception {
        Thread m_serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m_serverSocket.setSoTimeout(1000);
                    m_serverSocket.accept();
                    Thread.sleep(TIMEOUT);
                    m_serverSocket.close();
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });
        
        m_serverThread.start();
        
        assertFalse("Test for protocol FTP should have failed", doCheck());
    }
    
    @Test
    public void testMonitorFailureWithClosedPort() throws Exception {
        m_serverSocket.close();
        
        assertFalse("Test for protocol FTP should have failed", doCheck());
    }

    private boolean  doCheck() {
        return m_detector.isServiceDetected(m_serverSocket.getInetAddress(), new NullDetectorMonitor());
    }
}
