//
//  $Id$
//

package org.opennms.netmgt.trapd;

import java.io.Reader;
import java.io.StringReader;

import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class TrapdTest extends OpenNMSTestCase {
    private static String m_port = "1162";
	private static Trapd m_trapd = new Trapd();
    private static TrapdConfig m_config;
    private static String TRAPD_CONFIG = "<?xml version=\"1.0\"?>\n" + 
            "<trapd-configuration snmp-trap-port=\""+m_port+"\" new-suspect-on-trap=\"true\"/>\n" + 
            "\n";

	protected void setUp() throws Exception {
        super.setUp();

        assertNotNull(DatabaseConnectionFactory.getInstance());
        Reader rdr = new StringReader(TRAPD_CONFIG);
        TrapdConfigFactory.setInstance(new TrapdConfigFactory(rdr));
        m_trapd = new Trapd();
		m_trapd.init();
        m_trapd.start();
	}

	public void tearDown() throws Exception {
		m_trapd.stop();
		m_trapd = null;
        super.tearDown();
	}

	public void testSnmpV1TrapSend() throws Exception {


        UdpAddress address = new UdpAddress(myLocalHost()+"/"+m_port);
        TransportMapping transport = new DefaultUdpTransportMapping();
        
        Target target = new CommunityTarget();
        target.setAddress(address);
        target.setRetries(1);
        target.setTimeout(800);
        target.setVersion(SnmpConstants.version1);
        
        Snmp snmp = new Snmp(transport);
        //snmp.listen();

        PDUv1 trapPdu = new PDUv1();
        trapPdu.setType(PDU.V1TRAP);
        OID eOID = new OID(".1.3.6.1.4.1.5813");
        trapPdu.setEnterprise(eOID);
        trapPdu.setGenericTrap(1);
        trapPdu.setSpecificTrap(0);
        trapPdu.setAgentAddress(new IpAddress(address.getInetAddress()));
        System.err.println(trapPdu.getBERLength());
        
        Event e = new Event();
        e.setUei("uei.opennms.org/default/trap");
        e.setSource("trapd");
        e.setInterface(((UdpAddress)address).toString());
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        e.setLogmsg(logmsg);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);

        snmp.send(trapPdu, target);
        snmp.send(trapPdu, target);
        snmp.send(trapPdu, target);
        snmp.send(trapPdu, target);
        
        assertEquals(1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());

	}
}

