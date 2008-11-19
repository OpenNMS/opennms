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
public class LdapDetectorTest {
    
    private LdapDetector m_detector;
    private static String DEFAULT_LOCAL_SERVER_IP = "192.168.1.103";
    /*
     * Setup a local Ldap server and test against that server. Then change the IP address to the server
     */
    
//    @Before
//    public void setUp() {
//        m_detector = new LdapDetector();
//        m_detector.init();
//    }
    
    @Test
    public void testMyDetector() throws UnknownHostException {
        //assertTrue(m_detector.isServiceDetected(InetAddress.getByName(DEFAULT_LOCAL_SERVER_IP), new NullDetectorMonitor()));
    }
//    
//    @Test
//    public void testDetectorFailWrongPort() throws UnknownHostException {
//        m_detector.setPort(1200);
//        assertFalse(m_detector.isServiceDetected(InetAddress.getByName(DEFAULT_LOCAL_SERVER_IP), new NullDetectorMonitor()));
//    }
//    
//    @Test
//    public void testDetectorFailNotALdapServer() throws UnknownHostException {
//        assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.101"), new NullDetectorMonitor()));
//    }
}
