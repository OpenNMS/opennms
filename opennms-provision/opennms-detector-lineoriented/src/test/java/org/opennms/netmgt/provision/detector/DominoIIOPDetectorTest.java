/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertFalse;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.simple.DominoIIOPDetector;
import org.opennms.netmgt.provision.detector.simple.DominoIIOPDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class DominoIIOPDetectorTest {

    @Autowired
    private  DominoIIOPDetectorFactory m_detectorFactory;
    private DominoIIOPDetector m_detector;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(); 
        m_detector.setTimeout(500);
    }

    /*
     * Testing against an open port that will connect to the socket. 
     * The DominoIIOPDetector simple connects to the socket to test
     * that it can connect. The default port is 63148. In this test I
     * used a pre-existing http server on a local machine that would
     * allow the connection. 
     */
    @Test(timeout=20000)
    @Ignore
    public void testDetectorSuccessTokenPort() throws UnknownHostException {
        //m_detector.setPort(8080);
        //m_detector.init();
        //assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.103")));
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws UnknownHostException {
        m_detector.setPort(65535);
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    @Test(timeout=20000)
    public void testDetectorFailNoHost() throws UnknownHostException {
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("1.1.1.1")));
    }
}
