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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.server.AsyncSimpleServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.netmgt.provision.support.codec.LineOrientedCodecFactory;


/**
 * @author thedesloge
 *
 */
public class AsyncPop3DetectorTest {
    
    private AsyncPop3Detector m_detector;
    private AsyncSimpleServer m_server;
    
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
        
        m_detector = new AsyncPop3Detector();
        
    }
    
    @After
    public void tearDown() throws Exception {
        m_server.stopServer();
    }
    
    @Test
    public void testAsyncDetector() throws Exception{
        m_detector.setPort(9123);
        m_detector.setProtocolCodecFilter(new ProtocolCodecFilter( new LineOrientedCodecFactory( Charset.forName( "UTF-8" ))));
        m_detector.init();
        
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        assertNotNull(future);
        
        future.await();
        
        assertTrue(future.isServiceDetected());
        System.out.printf("future is complete, isServiceDetected: %s\n", future.isServiceDetected());
    }
    
    @Test
    public void testAsyncDetectorFailWrongBanner() throws Exception{
        m_server.setBanner("+BINGO");
        
        m_detector.setPort(9123);
        m_detector.init();
        
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        assertNotNull(future);
        
        future.await();
        
        assertFalse(future.isServiceDetected());
        System.out.printf("future is complete, isServiceDetected: %s\n", future.isServiceDetected());
    }
    
    @Test
    public void testAsyncDetectorFailWrongQuitResponse() throws Exception{
        m_server.setExpectedClose("QUIT", "+OUT");
        m_detector.setPort(9123);
        m_detector.init();
        
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        assertNotNull(future);
        
        future.await();
        
        assertFalse(future.isServiceDetected());
        System.out.printf("future is complete, isServiceDetected: %s\n", future.isServiceDetected());
    }
    
    @Test
    public void testAsyncDetectorFailServerNoResponse() throws Exception{
        m_server.setExpectedClose("LOGOUT", "+OK");
        m_detector.setPort(9123);
        m_detector.setTimeout(1000);
        m_detector.setIdleTime(1);
        m_detector.init();
        
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        assertNotNull(future);
        
        future.await();
        
        assertFalse(future.isServiceDetected());
        System.out.printf("future is complete, isServiceDetected: %s\n", future.isServiceDetected());
    }
    
    @Test
    public void testAsyncDetectorFailWrongPort() throws Exception{
        m_detector.setPort(9120);
        m_detector.setTimeout(1000);
        m_detector.setIdleTime(1);
        m_detector.setRetries(3);
        m_detector.init();
        
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor());
        assertNotNull(future);
        
        future.await();
        
        assertFalse(future.isServiceDetected());
        System.out.printf("future is complete, isServiceDetected: %s\n", future.isServiceDetected());
    }
}
