//
//  $Id$
//

package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
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

        Reader rdr = new StringReader(TRAPD_CONFIG);
        TrapdConfigFactory.setInstance(new TrapdConfigFactory(rdr));
        m_trapd = new Trapd();
		m_trapd.init();
        m_trapd.start();
	}

	public void tearDown() throws Exception {
		m_trapd.stop();
		m_trapd = null;
	}

	public void testSnmpV1TrapSend() throws UnknownHostException, IOException {

        Address address = new UdpAddress(myLocalHost()+"/"+m_port);
        
        TransportMapping transport = new DefaultUdpTransportMapping();
        
        Target target = new CommunityTarget();
        target.setAddress(address);
        target.setRetries(1);
        target.setTimeout(800);
        target.setVersion(SnmpConstants.version1);
        
        Snmp snmp = new Snmp(transport);
        snmp.listen();

        PDUv1 trapPdu = new PDUv1();
        trapPdu.setType(PDU.V1TRAP);
        OID eOID = new OID(".1.3.6.1.4.1.5813");
        trapPdu.setEnterprise(eOID);
        trapPdu.setGenericTrap(1);
        trapPdu.setSpecificTrap(0);
        snmp.send(trapPdu, target);
        
	}

    private String myLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }
}

