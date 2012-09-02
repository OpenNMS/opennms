package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class TticketTest extends XmlTestNoCastor<Tticket> {

	public TticketTest(final Tticket sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Tticket tticket0 = new Tticket();
		tticket0.setContent("This is a test");
		Tticket tticket1 = new Tticket();
		tticket1.setContent("This is a test");
		tticket1.setState("on");
		return Arrays.asList(new Object[][] {
				{tticket0,
				"<tticket>This is a test</tticket>",
				"target/classes/xsds/eventconf.xsd" },
				{tticket1,
				"<tticket state=\"on\">This is a test</tticket>",
				"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
