package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ForwardTest extends XmlTestNoCastor<Forward> {

	public ForwardTest(final Forward sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Forward forward0 = new Forward();
		Forward forward1 = new Forward();
		forward1.setMechanism("snmpudp");
		forward1.setState("on");
		return Arrays.asList(new Object[][] {
				{forward0,
				"<forward/>",
				"target/classes/xsds/eventconf.xsd" },
				{forward1,
				"<forward state=\"on\" mechanism=\"snmpudp\"/>",
				"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
