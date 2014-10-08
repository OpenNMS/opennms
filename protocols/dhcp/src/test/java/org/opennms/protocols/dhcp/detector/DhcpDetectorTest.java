/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.dhcp.detector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dhcpd.Dhcpd;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import edu.bucknell.net.JDHCP.DHCPMessage;
import edu.bucknell.net.JDHCP.DHCPSocket;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/META-INF/opennms/detectors.xml"})
public class DhcpDetectorTest implements InitializingBean {
	
    //Tested local DHCP client
    private static String DHCP_SERVER_IP = "172.20.1.1";
    
    @Autowired
    public DhcpDetector m_detector;
    
    private Dhcpd m_dhcpd;
    
    private Thread m_dhcpdThread = null;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

        m_dhcpd = Dhcpd.getInstance();
        m_dhcpd.init();
        // binds on port 68, hardcoded  :P
        //m_dhcpd.start();
    }
    
    @After
    public void tearDown(){
        // m_dhcpd.stop();
    }
    
	@Test(timeout=90000)
	public void testDetectorWired() {
	   assertNotNull(m_detector);
	}
	
	@Test(timeout=90000)
	@Ignore
	public void testDetectorSuccess() throws  IOException, MarshalException, ValidationException{
	    m_detector.setTimeout(5000);
	    m_detector.init();
	    assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr(DHCP_SERVER_IP)));
	    
	}
	
	@Test(timeout=90000)
	@Ignore
	public void testJdhcp() throws IOException{
	    DHCPSocket mySocket = new DHCPSocket(68);
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
        System.out.println("Destination Address:  " + messageIn.getDestinationAddress());
        System.out.println("Ch Address:  " + Arrays.toString(messageIn.getChaddr()));
        System.out.println("Siaddr:  " + Arrays.toString(messageIn.getSiaddr()));
        System.out.println("Ciaddr: " + Arrays.toString(messageIn.getCiaddr()));
        
        System.out.println("Option54: " + Arrays.toString(messageIn.getOption(54)));
        System.out.println(InetAddress.getByAddress(messageIn.getOption(54)));
        
	}
	

	public void setDhcpdThread(Thread dhcpdThread) {
        m_dhcpdThread = dhcpdThread;
    }

    public Thread getDhcpdThread() {
        return m_dhcpdThread;
    }
	
	
}