/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Test;

/**
 * @author Donald Desloge
 *
 */
public class SmbDetectorTest {
    
//    private SmbDetector m_detector;
    
//    @Before
//    public void setUp() {
//        MockLogAppender.setupLogging();
//        m_detector = new SmbDetector();
//        
//    }
    
    @After
    public void tearDown() {
        
    }
    
    //Tested against a Windows XP machine on local network. 
    @Test(timeout=30000)
    public void testMyDetector() throws UnknownHostException {
        //m_detector.init();
        //FIXME: This needs to be fixed
        //assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.103")));
    }
}
