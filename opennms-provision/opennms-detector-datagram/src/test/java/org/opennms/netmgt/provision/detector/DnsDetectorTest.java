/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertTrue;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.dns.JUnitDNSServerExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.datagram.DnsDetector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/empty-context.xml"})
@TestExecutionListeners(JUnitDNSServerExecutionListener.class)
@JUnitDNSServer(port=9153, zones={
    @DNSZone(name = "google.com.", entries = {
        @DNSEntry(hostname = "www", data = "72.14.204.99")
    })
})
public class DnsDetectorTest {

    private DnsDetector m_detector;

    @Before
    public void setUp() throws SocketException {
        MockLogAppender.setupLogging();

        m_detector = new DnsDetector();
        m_detector.setTimeout(500);

        //m_socket = new DatagramSocket(4445);
        //m_serverThread = createThread();
        //m_serverThread.start();
    }

    @After
    public void tearDown() {
        //m_serverThread.stop();
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws UnknownHostException {
        m_detector.setPort(9153);
        m_detector.setLookup("www.google.com");
        m_detector.init();

        assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("localhost")));
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws UnknownHostException {
        m_detector.setPort(5000);
        m_detector.setLookup("www.google.com");
        m_detector.init();

        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("localhost")));

    }
}
