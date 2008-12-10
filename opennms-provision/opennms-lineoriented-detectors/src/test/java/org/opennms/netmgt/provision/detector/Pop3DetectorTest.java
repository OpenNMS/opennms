/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications;
 * Created 10/16/2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.server.AsyncSimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


public class Pop3DetectorTest {
    private AsyncSimpleServer m_server;
    private Pop3Detector m_detector;
    
    @Before
    public void setUp() throws Exception {
        m_server = new AsyncSimpleServer() {
            
            public void onInit() {
                setBanner("+OK");
                setExpectedClose("QUIT", "+OK");
            }
        };
        m_server.init();
        m_server.startServer();
    }

    @After
    public void tearDown() throws Exception {
        if(m_server != null) {
            m_server.stopServer();
        }
    }
    
    @Test
    public void testSuccess() throws Exception {
        m_detector = createDetector(m_server.getPort());
        
        assertTrue( doCheck( m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor())));
    }
    
    @Test
    public void testFailureWithBogusResponse() throws Exception {
        m_server.setBanner("Oh Henry");
        
        m_detector = createDetector(m_server.getPort());
        
        assertFalse( doCheck( m_detector.isServiceDetected( InetAddress.getLocalHost(), new NullDetectorMonitor())));
        
    }
    
    @Test
    public void testMonitorFailureWithNoResponse() throws Exception {
        m_server.setBanner(null);
        m_detector = createDetector(m_server.getPort());
        
        assertFalse( doCheck( m_detector.isServiceDetected( InetAddress.getLocalHost(), new NullDetectorMonitor())));
        
    }
    
    @Test
    public void testDetectorFailWrongPort() throws Exception{
        
        m_detector = createDetector(9000);
        
        assertFalse( doCheck( m_detector.isServiceDetected( InetAddress.getLocalHost(), new NullDetectorMonitor())));
    }
    
    private Pop3Detector createDetector(int port) {
        Pop3Detector detector = new Pop3Detector();
        detector.setServiceName("POP3");
        detector.setTimeout(500);
        detector.setPort(port);
        detector.init();
        return detector;
    }
    
    private boolean  doCheck(DetectFuture future) throws Exception {
        
        future.await();
        
        return future.isServiceDetected();
    }
}
