package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dhcpd.Dhcpd;
import org.opennms.netmgt.provision.detector.dhcp.DhcpDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.bucknell.net.JDHCP.DHCPMessage;
import edu.bucknell.net.JDHCP.DHCPSocket;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class DhcpDetectorTest{
	
    //Tested local DHCP client
    private static String DHCP_SERVER_IP = "172.20.1.1";
    
    @Autowired
    public DhcpDetector m_detector;
    
    private Dhcpd m_dhcpd;
    
    private Thread m_dhcpdThread = null;
    
    @Before
    public void setup() {
        MockLogAppender.setupLogging();

        m_dhcpd = Dhcpd.getInstance();
        m_dhcpd.init();
        m_dhcpd.start();
    }
    
    @After
    public void tearDown(){
        m_dhcpd.stop();
    }
    
    @Ignore
	@Test
	public void testDetectorWired() {
	   assertNotNull(m_detector);
	}
	
    @Ignore
	@Test
	public void testDetectorSuccess() throws  IOException, MarshalException, ValidationException{
	    m_detector.setTimeout(5000);
	    m_detector.init();
	    assertTrue(m_detector.isServiceDetected(InetAddress.getByName(DHCP_SERVER_IP), new NullDetectorMonitor()));
	    
	}
	
	@Ignore
	@Test
	public void testJdhcp() throws IOException{
	    DHCPSocket mySocket = new DHCPSocket(68);
	    DHCPMessage messageOut = new DHCPMessage(InetAddress.getByName(DHCP_SERVER_IP)); 
	    
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
        try{
            mySocket.receive(messageIn);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    
        messageIn.printMessage();
        System.out.println("Destination Address:  " + messageIn.getDestinationAddress());
        System.out.println("Ch Address:  " + messageIn.getChaddr());
        System.out.println("Siaddr:  " + messageIn.getSiaddr());
        System.out.println("Ciaddr  " + messageIn.getCiaddr());
        
        System.out.println(InetAddress.getByAddress(messageIn.getOption(54)));
        
	}
	

	public void setDhcpdThread(Thread dhcpdThread) {
        m_dhcpdThread = dhcpdThread;
    }

    public Thread getDhcpdThread() {
        return m_dhcpdThread;
    }
	
	
}