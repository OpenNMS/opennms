//
//  $Id: TrapdTest.java 3132 2006-04-16 01:34:14 +0000 (Sun, 16 Apr 2006) mhuot $
//

package org.opennms.netmgt.syslogd;

import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;

public class SyslogdTest extends OpenNMSTestCase {
    private static int m_port = 1162;
	private static Syslogd m_syslogd = new Syslogd();
    private static String SYSLOGD_CONFIG = "<?xml version=\"1.0\"?>\n" + 
            "<syslogd-configuration syslog-port=\""+m_port+"\" new-suspect-on-message=\"true\"/>\n" + 
            "\n";

	protected void setUp() throws Exception {
        super.setUp();

        assertNotNull(DataSourceFactory.getInstance());
        Reader rdr = new StringReader(SYSLOGD_CONFIG);
        SyslogdConfigFactory.setInstance(new SyslogdConfigFactory(rdr));
        m_syslogd = new Syslogd();
		m_syslogd.init();
        m_syslogd.start();
	}

	public void tearDown() throws Exception {
		m_syslogd.stop();
		m_syslogd = null;
        super.tearDown();
	}

	public void testSnmpV1TrapSend() throws Exception {
		String localhost = myLocalHost();
		InetAddress localAddr = InetAddress.getByName(myLocalHost());
        
        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
        pdu.setEnterprise(SnmpObjId.get(".1.3.6.1.4.1.5813"));
        pdu.setGeneric(1);
        pdu.setSpecific(0);
        pdu.setTimeStamp(System.currentTimeMillis());
        pdu.setAgentAddress(localAddr);
        

        
        Event e = new Event();
        e.setUei("uei.opennms.org/default/trap");
        e.setSource("trapd");
        e.setInterface(localhost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        e.setLogmsg(logmsg);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);

        pdu.send(localhost, m_port, "public");
        pdu.send(localhost, m_port, "public");
        pdu.send(localhost, m_port, "public");
        pdu.send(localhost, m_port, "public");
        
        assertEquals(1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());

	}
}

