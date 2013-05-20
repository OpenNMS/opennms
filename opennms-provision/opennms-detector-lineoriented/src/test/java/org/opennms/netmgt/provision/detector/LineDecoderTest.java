/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.AsyncLineOrientedDetectorMinaImpl;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.opennms.netmgt.provision.server.exchange.RequestHandler;

/**
 * @author Donald Desloge
 *
 */
public class LineDecoderTest {
    
    public static class TestServer extends SimpleServer{
        
        @Override
        protected void sendBanner(OutputStream out) throws IOException {
            String[] tokens = getBanner().split("");
            
            for(int i = 0; i < tokens.length; i++) {
                String str = tokens[i];
                out.write(str.getBytes());
                out.flush();
                
            }
            out.write("\r\n".getBytes());
            
        }
        
        @Override
        protected RequestHandler errorString(final String error) {
            return new RequestHandler() {

                @Override
                public void doRequest(OutputStream out) throws IOException {
                    out.write(String.format("%s", error).getBytes());
                    
                }
                
            };
        }
        
        @Override
        protected RequestHandler shutdownServer(final String response) {
            return new RequestHandler() {
                
                @Override
                public void doRequest(OutputStream out) throws IOException {
                    out.write(String.format("%s\r\n", response).getBytes());
                    stopServer();
                }
                
            };
        }
    }
    
    public static class TestDetector extends AsyncLineOrientedDetectorMinaImpl {

        public TestDetector() {
            super("POP3", 110, 5000, 1);
           
        }

        @Override
        protected void onInit() {
            expectBanner(startsWith("+OK"));
            send(request("QUIT"), startsWith("+OK"));
        }
        
    }
    
    private TestServer m_server;
    private TestDetector m_detector;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_server = new TestServer() {
            
            @Override
            public void onInit() {
                setBanner("+OK");
                addResponseHandler(contains("QUIT"), shutdownServer("+OK"));
            }
        };
        m_server.init();
        m_server.startServer();
    }

    @After
    public void tearDown() throws Exception {
        if(m_server != null) {
            m_server.stopServer();
            m_server = null;
        }
    }
    
    @Test
    public void testSuccess() throws Exception {
        
        m_detector = createDetector(m_server.getLocalPort());
        m_detector.setIdleTime(1000);
        assertTrue( doCheck( m_detector.isServiceDetected(m_server.getInetAddress())));
    }
    
    
    @Test
    public void testFailureWithBogusResponse() throws Exception {
        m_server.setBanner("Oh Henry");
        
        m_detector = createDetector(m_server.getLocalPort());
        
        assertFalse( doCheck( m_detector.isServiceDetected( m_server.getInetAddress())));
        
    }
    
    @Test
    public void testMonitorFailureWithNoResponse() throws Exception {
        m_server.setBanner(null);
        m_detector = createDetector(m_server.getLocalPort());
        
        assertFalse( doCheck( m_detector.isServiceDetected( m_server.getInetAddress())));
        
    }
    
    @Test
    public void testDetectorFailWrongPort() throws Exception{
        
        m_detector = createDetector(9000);
        
        assertFalse( doCheck( m_detector.isServiceDetected( m_server.getInetAddress())));
    }
    
    private static TestDetector createDetector(int port) {
        TestDetector detector = new TestDetector();
        detector.setServiceName("TEST");
        detector.setTimeout(1000);
        detector.setPort(port);
        detector.init();
        return detector;
    }
    
    private static boolean doCheck(DetectFuture future) throws Exception {
        
        future.awaitFor();
        
        return future.isServiceDetected();
    }
}