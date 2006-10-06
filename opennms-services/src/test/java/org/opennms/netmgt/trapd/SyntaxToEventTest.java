package org.opennms.netmgt.trapd;

import junit.framework.TestSuite;

import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.snmp.PropertySettingTestSuite;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.xml.event.Parm;

public class SyntaxToEventTest extends OpenNMSTestCase {
    public static TestSuite suite() {
        Class testClass = SyntaxToEventTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new PropertySettingTestSuite(testClass, "JoeSnmp Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy"));
        suite.addTest(new PropertySettingTestSuite(testClass, "Snmp4J Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy"));
        return suite;
    }
    

    protected void setUp() throws Exception {
        super.setUp();
    }
    
	public void testProcessSyntax() {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
        assertNotNull(valueFactory);
        byte [] macAddr = {000, 000, 000, 000, 000, 000};

        SnmpValue octetString = valueFactory.getOctetString(macAddr);
        
		Parm parm = SyntaxToEvent.processSyntax("Test",octetString);

		assertEquals("Test", parm.getParmName());
		assertEquals("......", parm.getValue().getContent());	
	}
	
}
