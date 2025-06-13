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
package org.opennms.netmgt.provision.detector.dhcp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.dhcp.DhcpDetector;
import org.opennms.netmgt.provision.detector.dhcp.DhcpDetectorFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/META-INF/opennms/detectors.xml","classpath:/META-INF/opennms/applicationContext-soa.xml"})
@JUnitConfigurationEnvironment
public class DhcpDetectorTest implements InitializingBean {

    // This should be a real, working DHCP server on the network
    private static String DHCP_SERVER_IP = "192.168.0.1";

    // This should be the IP address of the machine running the test which DHCP_SERVER_IP will respond with
    private static String MY_IP = "192.168.0.123";

    // This should be the MAC address of the machine running the test
    private static String MY_MAC = "00:00:00:00:00:01";

    // Enable this if you have set the previous 3 things properly, and you're running as root :)
    private boolean m_extendedTests = false;

    @Autowired
    public DhcpDetectorFactory m_detectorFactory;
    
    public DhcpDetector m_detector;

    private Thread m_dhcpdThread = null;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
    }

    @Test(timeout=90000)
    public void testDetectorWired() {
        assertNotNull(m_detector);
    }

    @Test(timeout=90000)
    public void testDetectorSuccess() {
        assumeTrue(m_extendedTests);
        m_detector.setTimeout(5000);
        m_detector.init();
        assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr(DHCP_SERVER_IP)));
    }

    @Test(timeout=90000)
    public void testDhcp4Java() throws IOException {
        assumeTrue(m_extendedTests);

        final DatagramSocket mySocket = new DatagramSocket(DHCPConstants.BOOTP_REPLY_PORT);

        try {
            DHCPPacket messageOut = new DHCPPacket();
    
            // fill DHCPMessage object 
            messageOut.setOp((byte) 1);    
            messageOut.setHtype((byte) 1);
            messageOut.setHlen((byte) 6);
            messageOut.setHops((byte) 0);
            messageOut.setXid(191991743); // should be a random int
            messageOut.setSecs((short) 0);
            messageOut.setFlags((short) 0);
    
            byte[] hw = new byte[16];
            hw[0] = (byte) 0x00;
            hw[1] = (byte) 0x60;
            hw[2] = (byte) 0x97; 
            hw[3] = (byte) 0xC6; 
            hw[4] = (byte) 0x76;
            hw[5] = (byte) 0x64;
            messageOut.setChaddr(hw);
    
            // set message type option to DHCPDISCOVER
            messageOut.setOption(DHCPOption.newOptionAsByte((byte) 53, DHCPConstants.DHCPDISCOVER));

            final byte[] buf = messageOut.serialize();

            final DatagramPacket packetOut = new DatagramPacket(buf, buf.length);
            packetOut.setAddress(InetAddressUtils.addr(DHCP_SERVER_IP));
            packetOut.setPort(DHCPConstants.BOOTP_REQUEST_PORT);

            mySocket.send(packetOut);

            final DatagramPacket packetIn = new DatagramPacket(new byte[1500], 1500);
            mySocket.receive(packetIn);
            DHCPPacket messageIn = DHCPPacket.getPacket(packetIn);

            System.out.println(messageIn);
            System.out.println("Destination Address:  " + DHCP_SERVER_IP);
            System.out.println("Ch Address:  " + Arrays.toString(messageIn.getChaddr()));
            System.out.println("Siaddr:  " + Arrays.toString(messageIn.getSiaddrRaw()));
            System.out.println("Ciaddr: " + Arrays.toString(messageIn.getCiaddrRaw()));
    
            System.out.println("Option54: " + messageIn.getOption((byte)54));
            System.out.println(messageIn.getOption((byte)54).getValueAsInetAddr());
        } finally {
            IOUtils.closeQuietly(mySocket);
        }
    }


    public void setDhcpdThread(Thread dhcpdThread) {
        m_dhcpdThread = dhcpdThread;
    }

    public Thread getDhcpdThread() {
        return m_dhcpdThread;
    }
}
