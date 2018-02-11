/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.protocols.dhcp.detector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.jdhcp.DHCPMessage;
import org.opennms.jdhcp.DHCPSocket;
import org.opennms.netmgt.config.dhcpd.DhcpdConfigFactory;
import org.opennms.netmgt.dhcpd.Dhcpd;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/META-INF/opennms/detectors.xml"})
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

    private Dhcpd m_dhcpd;

    private Thread m_dhcpdThread = null;

    private String m_onmsHome;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        File etc = new File("target/test-work-dir/etc");
        etc.mkdirs();
        m_onmsHome = etc.getParent();
        System.setProperty("opennms.home", m_onmsHome);
        ConfigurationTestUtils.setRelativeHomeDirectory(m_onmsHome);
        File dhcpConfig = new File(etc, "dhcpd-configuration.xml");
        FileUtils.writeStringToFile(dhcpConfig, "<DhcpdConfiguration\n" + 
                "        port=\"5818\"\n" + 
                "        macAddress=\"" + MY_MAC + "\"\n" + 
                "        myIpAddress=\"" + MY_IP + "\"\n" + 
                "        extendedMode=\"false\"\n" + 
                "        requestIpAddress=\"" + MY_IP + "\">\n" + 
                "</DhcpdConfiguration>");

        DhcpdConfigFactory.init();
        m_detector = m_detectorFactory.createDetector();
        m_dhcpd = Dhcpd.getInstance();
        m_dhcpd.init();

        if (m_extendedTests) {
            // binds on port 68, hardcoded  :P
            m_dhcpd.start();
        }
    }

    @After
    public void tearDown(){
        if (m_extendedTests) {
            m_dhcpd.stop();
        }
    }

    @Test(timeout=90000)
    public void testDetectorWired() {
        assertNotNull(m_detector);
    }

    @Test(timeout=90000)
    public void testDetectorSuccess() throws  IOException {
        assumeTrue(m_extendedTests);
        m_detector.setTimeout(5000);
        m_detector.init();
        assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr(DHCP_SERVER_IP)));
    }

    @Test(timeout=90000)
    public void testJdhcp() throws IOException{
        assumeTrue(m_extendedTests);
        DHCPSocket mySocket = new DHCPSocket(68);

        try {
            DHCPMessage messageOut = new DHCPMessage(InetAddressUtils.addr(DHCP_SERVER_IP)); 
    
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
            byte[] opt = new byte[1];
            opt[0] = (byte) DHCPMessage.DISCOVER;
            messageOut.setOption(53,  opt);
    
            mySocket.send(messageOut);
    
            DHCPMessage messageIn = new DHCPMessage();
            mySocket.receive(messageIn);
    
            messageIn.printMessage();
            System.out.println("Destination Address:  " + messageIn.getDestination());
            System.out.println("Ch Address:  " + Arrays.toString(messageIn.getChaddr()));
            System.out.println("Siaddr:  " + Arrays.toString(messageIn.getSiaddr()));
            System.out.println("Ciaddr: " + Arrays.toString(messageIn.getCiaddr()));
    
            System.out.println("Option54: " + Arrays.toString(messageIn.getOption(54)));
            System.out.println(InetAddress.getByAddress(messageIn.getOption(54)));
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
