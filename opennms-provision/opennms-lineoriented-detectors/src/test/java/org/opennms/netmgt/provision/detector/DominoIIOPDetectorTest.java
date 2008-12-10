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

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;


/**
 * @author Donald Desloge
 *
 */
public class DominoIIOPDetectorTest {
    
    private DominoIIOPDetector m_detector;
    
    @Before
    public void setUp() {
       m_detector = new DominoIIOPDetector(); 
    }
    
    /*
     * Testing against an open port that will connect to the socket. 
     * The DominoIIOPDetector simple connects to the socket to test
     * that it can connect. The default port is 63148. In this test I
     * used a pre-existing http server on a local machine that would
     * allow the connection. 
     */
    @Test
    public void testDetectorSuccessTokenPort() throws UnknownHostException {
        //m_detector.setPort(8080);
        //m_detector.init();
        //assertTrue(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailWrongPort() throws UnknownHostException {
        m_detector.setPort(10000);
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("127.0.0.1"), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailWrongIORPort() throws UnknownHostException {
//        m_detector.setIorPort(1000);
//        m_detector.setPort(80);
//        m_detector.init();
//        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
    }
}
