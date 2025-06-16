/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
            super("POP3", 110, 500, 1);
           
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
        
        m_detector = createDetector(65535);
        
        assertFalse( doCheck( m_detector.isServiceDetected( m_server.getInetAddress())));
    }
    
    private static TestDetector createDetector(int port) {
        TestDetector detector = new TestDetector();
        detector.setServiceName("TEST");
        detector.setPort(port);
        detector.init();
        return detector;
    }
    
    private static boolean doCheck(DetectFuture future) throws Exception {
        
        future.awaitFor();
        
        return future.isServiceDetected();
    }
}